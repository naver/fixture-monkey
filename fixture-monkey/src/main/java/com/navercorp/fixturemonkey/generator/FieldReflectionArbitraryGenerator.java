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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Builders.BuilderCombinator;

import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.customizer.WithFixtureCustomizer;
import com.navercorp.fixturemonkey.property.DefaultPropertyNameResolver;

public final class FieldReflectionArbitraryGenerator extends AbstractArbitraryGenerator
	implements WithFixtureCustomizer {
	public static final FieldReflectionArbitraryGenerator INSTANCE = new FieldReflectionArbitraryGenerator();
	private static final Map<String, Field> TYPE_FIELD_CACHE = new ConcurrentHashMap<>();
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final ArbitraryCustomizers arbitraryCustomizers;

	private final PropertyNameResolver propertyNameResolver = new DefaultPropertyNameResolver();

	public FieldReflectionArbitraryGenerator() {
		this(new ArbitraryCustomizers());
	}

	private FieldReflectionArbitraryGenerator(ArbitraryCustomizers arbitraryCustomizers) {
		this.arbitraryCustomizers = arbitraryCustomizers;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	protected <T> Arbitrary<T> generateObject(ArbitraryType type, List<ArbitraryNode> nodes) {
		Class<T> clazz = type.getType();
		if (clazz.isInterface()) {
			return Arbitraries.just(null);
		}

		FieldArbitraries fieldArbitraries = new FieldArbitraries(
			toArbitrariesByFieldName(nodes, ArbitraryNode::getPropertyName, (node, arbitrary) -> arbitrary)
		);

		this.arbitraryCustomizers.customizeFields(clazz, fieldArbitraries);

		BuilderCombinator builderCombinator = Builders.withBuilder(() -> ReflectionUtils.newInstance(clazz));
		for (Map.Entry<String, Arbitrary> entry : fieldArbitraries.entrySet()) {
			String fieldName = entry.getKey();
			String fieldKey = clazz.getName() + "#" + fieldName;

			Field field = TYPE_FIELD_CACHE.computeIfAbsent(fieldKey, k -> {
				List<Field> fields = ReflectionUtils.findFields(
					clazz, f -> f.getName().equals(fieldName), HierarchyTraversalMode.TOP_DOWN);
				if (fields.isEmpty()) {
					return null;
				}
				Field result = fields.get(0);
				result.setAccessible(true);
				return result;
			});

			if (field == null || Modifier.isFinal(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
				continue;
			}

			builderCombinator = builderCombinator.use(entry.getValue()).in((object, value) -> {
				try {
					if (value != null) {
						field.set(object, value);
					}
				} catch (IllegalAccessException e) {
					log.warn(e,
						() -> "set field by reflection is failed. field: " + fieldName + " value: " + value
					);
				}
				return object;
			});
		}

		return builderCombinator.build(b -> this.arbitraryCustomizers.customizeFixture(clazz, (T)b));
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

		return new FieldReflectionArbitraryGenerator(arbitraryCustomizers);
	}
}
