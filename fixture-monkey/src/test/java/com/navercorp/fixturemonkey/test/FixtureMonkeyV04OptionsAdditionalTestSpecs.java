
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

package com.navercorp.fixturemonkey.test;

import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Builders.BuilderCombinator;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.SimpleObject;

class FixtureMonkeyV04OptionsAdditionalTestSpecs {
	public static class SimpleObjectChild extends SimpleObject {
	}

	public static class RegisterGroup {
		public ArbitraryBuilder<String> string(LabMonkey labMonkey) {
			return labMonkey.giveMeBuilder("test");
		}
	}

	public static class Pair<S, T> {
		private final S first;
		private final T second;

		public Pair(S first, T second) {
			this.first = first;
			this.second = second;
		}

		public S getFirst() {
			return first;
		}

		public T getSecond() {
			return second;
		}
	}

	public static class PairArbitraryPropertyGenerator implements ArbitraryPropertyGenerator {
		@Override
		public ArbitraryProperty generate(ArbitraryPropertyGeneratorContext context) {
			com.navercorp.fixturemonkey.api.property.Property property = context.getProperty();

			List<AnnotatedType> elementTypes = Types.getGenericsTypes(property.getAnnotatedType());
			if (elementTypes.size() != 2) {
				throw new IllegalArgumentException(
					"Pair elementsTypes must be have 1 generics type for element. "
						+ "propertyType: " + property.getType()
						+ ", elementTypes: " + elementTypes
				);
			}

			ArbitraryContainerInfo containerInfo = context.getContainerInfo();
			if (containerInfo == null) {
				containerInfo = context.getGenerateOptions()
					.getArbitraryContainerInfoGenerator(property)
					.generate(context);
			}

			int size = containerInfo.getRandomSize();
			AnnotatedType firstElementType = elementTypes.get(0);
			AnnotatedType secondElementType = elementTypes.get(1);
			List<com.navercorp.fixturemonkey.api.property.Property> childProperties = new ArrayList<>();
			childProperties.add(
				new ElementProperty(
					property,
					firstElementType,
					0,
					0
				)
			);
			childProperties.add(
				new ElementProperty(
					property,
					secondElementType,
					1,
					1
				)
			);

			double nullInject = context.getGenerateOptions().getNullInjectGenerator(property)
				.generate(context, containerInfo);

			return new ArbitraryProperty(
				property,
				context.getPropertyNameResolver(),
				nullInject,
				context.getElementIndex(),
				childProperties,
				containerInfo
			);
		}
	}

	public static class PairIntrospector implements ArbitraryIntrospector, Matcher {
		private static final Matcher MATCHER = new AssignableTypeMatcher(Pair.class);

		@Override
		public boolean match(Property property) {
			return MATCHER.match(property);
		}

		@Override
		public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
			ArbitraryProperty property = context.getArbitraryProperty();
			ArbitraryContainerInfo containerInfo = property.getContainerInfo();
			if (containerInfo == null) {
				return ArbitraryIntrospectorResult.EMPTY;
			}

			List<Arbitrary<?>> childrenArbitraries = context.getChildrenArbitraryContexts().getArbitraries();
			BuilderCombinator<List<Object>> builderCombinator = Builders.withBuilder(ArrayList::new);
			for (Arbitrary<?> childArbitrary : childrenArbitraries) {
				builderCombinator = builderCombinator.use(childArbitrary).in((list, element) -> {
					list.add(element);
					return list;
				});
			}

			return new ArbitraryIntrospectorResult(
				builderCombinator.build(it -> new Pair<>(it.get(0), it.get(1)))
			);
		}

	}
}
