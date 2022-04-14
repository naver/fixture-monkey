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

package com.navercorp.fixturemonkey.api.introspector;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Builders.BuilderCombinator;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class BeanArbitraryIntrospector implements ArbitraryIntrospector {
	public static final BeanArbitraryIntrospector INSTANCE = new BeanArbitraryIntrospector();
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getProperty();
		Class<?> type = Types.getActualType(property.getType());
		if (type.isInterface()) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		List<ArbitraryProperty> childrenProperties = context.getChildren();
		Map<String, Arbitrary<?>> childrenArbitraries = context.getChildrenArbitraries();
		Map<String, PropertyDescriptor> propertyDescriptors = PropertyCache.getPropertyDescriptors(type);
		BuilderCombinator<?> builderCombinator = Builders.withBuilder(() -> ReflectionUtils.newInstance(type));
		for (ArbitraryProperty arbitraryProperty : childrenProperties) {
			String originPropertyName = arbitraryProperty.getProperty().getName();
			PropertyDescriptor propertyDescriptor = propertyDescriptors.get(originPropertyName);
			Method writeMethod = propertyDescriptor.getWriteMethod();
			if (writeMethod == null) {
				continue;
			}

			String resolvePropertyName = arbitraryProperty.getResolvePropertyName();
			Arbitrary<?> arbitrary = childrenArbitraries.get(resolvePropertyName);
			if (arbitrary != null) {
				builderCombinator = builderCombinator.use(arbitrary).in((b, v) -> {
					try {
						if (v != null) {
							writeMethod.invoke(b, v);
						}
					} catch (IllegalAccessException | InvocationTargetException e) {
						log.warn(e,
							() -> "set bean property is failed. name: " + writeMethod.getName() + " value: " + v
						);
					}
					return b;
				});
			}
		}

		return new ArbitraryIntrospectorResult(builderCombinator.build());
	}
}
