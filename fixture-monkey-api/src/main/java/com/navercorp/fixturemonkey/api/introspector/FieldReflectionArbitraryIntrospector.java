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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.commons.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Builders.BuilderCombinator;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class FieldReflectionArbitraryIntrospector implements ArbitraryIntrospector {
	public static final FieldReflectionArbitraryIntrospector INSTANCE = new FieldReflectionArbitraryIntrospector();
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		Class<?> type = Types.getActualType(property.getType());
		if (Modifier.isAbstract(type.getModifiers())) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		List<ArbitraryProperty> childrenProperties = context.getChildren();
		Map<String, Arbitrary<?>> childrenArbitraries = context.getChildrenArbitraryContexts()
			.getArbitrariesByResolvedName();
		Map<String, Field> fields = PropertyCache.getFields(type);
		BuilderCombinator<?> builderCombinator = Builders.withBuilder(() -> ReflectionUtils.newInstance(type));
		for (ArbitraryProperty arbitraryProperty : childrenProperties) {
			String originPropertyName = arbitraryProperty.getObjectProperty().getProperty().getName();
			Field field = fields.get(originPropertyName);

			if (field == null || Modifier.isFinal(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
				continue;
			}

			String resolvePropertyName = arbitraryProperty.getObjectProperty().getResolvedPropertyName();
			Arbitrary<?> arbitrary = childrenArbitraries.get(resolvePropertyName);
			builderCombinator = builderCombinator.use(arbitrary).in((object, value) -> {
				try {
					if (value != null) {
						field.set(object, value);
					}
				} catch (IllegalAccessException e) {
					log.warn("set field by reflection is failed. field: {} value: {}", resolvePropertyName, value, e);
				}
				return object;
			});
		}

		return new ArbitraryIntrospectorResult(builderCombinator.build());
	}
}
