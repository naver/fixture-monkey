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

import static java.util.stream.Collectors.toList;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;

import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.customizer.WithFixtureCustomizer;
import com.navercorp.fixturemonkey.property.DefaultPropertyNameResolver;

public final class ConstructorPropertiesArbitraryGenerator extends AbstractArbitraryGenerator
	implements WithFixtureCustomizer {
	public static final ConstructorPropertiesArbitraryGenerator INSTANCE =
		new ConstructorPropertiesArbitraryGenerator();
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final ArbitraryCustomizers arbitraryCustomizers;

	private final PropertyNameResolver propertyNameResolver = new DefaultPropertyNameResolver();

	public ConstructorPropertiesArbitraryGenerator() {
		this(new ArbitraryCustomizers());
	}

	private ConstructorPropertiesArbitraryGenerator(ArbitraryCustomizers arbitraryCustomizers) {
		this.arbitraryCustomizers = arbitraryCustomizers;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected <T> Arbitrary<T> generateObject(ArbitraryType type, List<ArbitraryNode> nodes) {
		Class<T> clazz = type.getType();

		List<Constructor<?>> constructors = Arrays.stream(clazz.getConstructors())
			.filter(c -> c.isAnnotationPresent(ConstructorProperties.class))
			.collect(toList());

		if (constructors.isEmpty()) {
			throw new IllegalArgumentException(
				clazz + " doesn't have constructor with 'ConstructorProperties' annotation.");
		}

		if (constructors.size() > 1) {
			throw new IllegalArgumentException(
				clazz + " has more then one constructor with 'ConstructorProperties' annotation.");
		}

		FieldArbitraries fieldArbitraries = new FieldArbitraries(
			toArbitrariesByFieldName(nodes, ArbitraryNode::getPropertyName, (node, arbitrary) -> arbitrary)
		);

		this.arbitraryCustomizers.customizeFields(clazz, fieldArbitraries);

		Constructor<T> constructor = (Constructor<T>)constructors.get(0);

		ConstructorProperties constructorProperties = constructor.getAnnotation(ConstructorProperties.class);
		String[] providedParameterNames = constructorProperties.value();

		Builders.BuilderCombinator<List<Object>> builderCombinator = Builders.withBuilder(
			() -> new ArrayList(providedParameterNames.length));
		for (String fieldName : providedParameterNames) {
			Arbitrary<?> arbitrary = fieldArbitraries.getArbitrary(fieldName);
			if (arbitrary == null) {
				throw new IllegalArgumentException("No field for the corresponding constructor argument '" + fieldName
					+ "'. Use 'ArbitraryCustomizer#customizeFields' for it.");
			}

			builderCombinator = builderCombinator.use(arbitrary).in((list, value) -> {
				list.add(value);
				return list;
			});
		}

		return builderCombinator.build(list -> {
			T fixture = ReflectionUtils.newInstance(constructor, list.toArray());
			return this.arbitraryCustomizers.customizeFixture(clazz, fixture);
		});
	}

	@Override
	public String resolveFieldName(Field field) {
		return this.propertyNameResolver.resolve(new FieldProperty(field));
	}

	@Override
	public ArbitraryGenerator withFixtureCustomizers(ArbitraryCustomizers arbitraryCustomizers) {
		if (this.arbitraryCustomizers == arbitraryCustomizers) {
			return this;
		}
		return new ConstructorPropertiesArbitraryGenerator(arbitraryCustomizers);
	}
}
