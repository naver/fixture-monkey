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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArrayIntrospector implements ArbitraryIntrospector, Matcher {
	private static final Logger LOGGER = LoggerFactory.getLogger(ArrayIntrospector.class);

	@Override
	public boolean match(Property property) {
		return Types.getActualType(property.getType()).isArray()
			|| GenericArrayType.class.isAssignableFrom(property.getType().getClass());
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		if (!property.isContainer() || !match(property.getObjectProperty().getProperty())) {
			LOGGER.info("Given type {} is not array type.", context.getResolvedType());
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.containerBuilder()
				.elements(context.getElementCombinableArbitraryList())
				.build(
					elements -> {
						ArrayBuilder arrayBuilder = new ArrayBuilder(
							Types.getArrayComponentType(
								property.getObjectProperty().getProperty().getAnnotatedType()
							),
							elements.size()
						);
						for (Object element : elements) {
							arrayBuilder.add(element);
						}
						return arrayBuilder.build();
					}
				)
		);
	}

	private static final class ArrayBuilder {
		private final List<Object> array;
		private final Class<?> componentType;
		private final int size;

		public ArrayBuilder(Class<?> componentType, int size) {
			this.array = new ArrayList<>();
			this.componentType = componentType;
			this.size = size;
		}

		@SuppressWarnings("UnusedReturnValue")
		ArrayBuilder add(Object value) {
			if (array.size() >= size) {
				return this;
			}

			array.add(value);
			return this;
		}

		// cast to Object for preventing ClassCastException when primitive type
		Object build() {
			Object array = Array.newInstance(componentType, size);

			for (int i = 0; i < this.array.size(); i++) {
				Array.set(array, i, this.array.get(i));
			}

			return array;
		}
	}
}
