/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.generator;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.customizer.WithFixtureCustomizer;

public final class BeanArbitraryGenerator extends AbstractArbitraryGenerator
	implements WithFixtureCustomizer {
	public static final BeanArbitraryGenerator INSTANCE = new BeanArbitraryGenerator();
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final ArbitraryCustomizers arbitraryCustomizers;

	public BeanArbitraryGenerator() {
		this(new ArbitraryCustomizers());
	}

	private BeanArbitraryGenerator(ArbitraryCustomizers arbitraryCustomizers) {
		this.arbitraryCustomizers = arbitraryCustomizers;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected <T> Arbitrary<T> generateObject(ArbitraryType type, List<ArbitraryNode> nodes) {
		Class<T> clazz = type.getType();
		if (clazz.isInterface()) {
			return Arbitraries.just(null);
		}

		FieldArbitraries fieldArbitraries = new FieldArbitraries(
			toArbitrariesByFieldName(nodes, ArbitraryNode::getFieldName, (node, arbitrary) -> arbitrary)
		);

		this.arbitraryCustomizers.customizeFields(clazz, fieldArbitraries);

		Map<String, PropertyDescriptor> propertyDescriptorMap = this.getPropertyDescriptorsByName(clazz);
		Combinators.BuilderCombinator builderCombinator = Combinators.withBuilder(
			() -> ReflectionUtils.newInstance(clazz));
		for (Map.Entry<String, Arbitrary> entry : fieldArbitraries.entrySet()) {
			String fieldName = entry.getKey();
			PropertyDescriptor propertyDescriptor = propertyDescriptorMap.get(fieldName);
			if (propertyDescriptor != null && propertyDescriptor.getWriteMethod() != null) {
				Method writeMethod = propertyDescriptor.getWriteMethod();
				builderCombinator = builderCombinator.use(entry.getValue()).in((b, v) -> {
					try {
						if (v != null) {
							writeMethod.invoke(b, v);
						}
					} catch (IllegalAccessException | InvocationTargetException e) {
						log.warn(e,
							() -> "set bean property is failed. field: " + fieldName + " value: " + v
						);
					}
					return b;
				});
			}
		}

		return builderCombinator.build(b -> this.arbitraryCustomizers.customizeFixture(clazz, (T)b));
	}

	@Override
	public ArbitraryGenerator withFixtureCustomizers(ArbitraryCustomizers arbitraryCustomizers) {
		if (this.arbitraryCustomizers == arbitraryCustomizers) {
			return this;
		}
		return new BeanArbitraryGenerator(arbitraryCustomizers);
	}

	private Map<String, PropertyDescriptor> getPropertyDescriptorsByName(Class<?> clazz) {
		Map<String, PropertyDescriptor> result = new HashMap<>();
		try {
			PropertyDescriptor[] descriptors = Introspector.getBeanInfo(clazz)
				.getPropertyDescriptors();
			for (PropertyDescriptor descriptor : descriptors) {
				result.put(descriptor.getName(), descriptor);
			}
		} catch (IntrospectionException e) {
			log.warn(e, () -> "Introspect bean property is failed. type: " + clazz);
		}

		return result;
	}
}
