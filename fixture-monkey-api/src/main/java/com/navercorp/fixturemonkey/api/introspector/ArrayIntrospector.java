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
import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Builders.BuilderCombinator;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.generator.LazyCombinableArbitrary;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArrayIntrospector implements ArbitraryIntrospector, Matcher {
	@Override
	public boolean match(Property property) {
		return Types.getActualType(property.getType()).isArray();
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		ContainerProperty containerProperty = property.getContainerProperty();
		if (containerProperty == null) {
			throw new IllegalArgumentException(
				"container property should not null. type : " + property.getObjectProperty().getProperty().getName()
			);
		}
		ArbitraryContainerInfo containerInfo = containerProperty.getContainerInfo();
		if (containerInfo == null) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		return new ArbitraryIntrospectorResult(
			new LazyCombinableArbitrary(
				LazyArbitrary.lazy(
					() -> {
						List<Arbitrary<?>> childrenArbitraries = context.getArbitraries();
						BuilderCombinator<ArrayBuilder> builderCombinator = Builders.withBuilder(() ->
							new ArrayBuilder(
								Types.getArrayComponentType(
									property.getObjectProperty().getProperty().getAnnotatedType()
								),
								childrenArbitraries.size()
							)
						);
						for (Arbitrary<?> childArbitrary : childrenArbitraries) {
							builderCombinator = builderCombinator.use(childArbitrary).in((list, element) -> {
								list.add(element);
								return list;
							});
						}
						return builderCombinator.build(ArrayBuilder::build);
					}
				)
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
