
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

import java.beans.ConstructorProperties;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.jqwik.api.Arbitraries;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderGroup;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateFactory;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateList;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ListStringObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;

class FixtureMonkeyOptionsAdditionalTestSpecs {
	public interface GetFixedValue {
		Object get();
	}

	public interface GetFixedValueChild extends GetFixedValue {
	}

	public interface GetterInterface {
		String getValue();
	}

	public static class SimpleObjectChild extends SimpleObject {
	}

	@Data
	public static class NestedListStringObject {
		List<ListStringObject> values;
	}

	public static class RegisterGroup {
		public static final ConcreteIntValue FIXED_INT_VALUE = new ConcreteIntValue();

		public ArbitraryBuilder<String> string(FixtureMonkey fixtureMonkey) {
			return fixtureMonkey.giveMeBuilder(String.class)
				.set(Arbitraries.strings().numeric().ofMinLength(1).ofMaxLength(3));
		}

		public ArbitraryBuilder<List<String>> stringList(FixtureMonkey fixtureMonkey) {
			return fixtureMonkey.giveMeBuilder(new TypeReference<List<String>>() {
				})
				.setInner(
					new InnerSpec()
						.maxSize(2)
				);
		}

		public ArbitraryBuilder<ConcreteIntValue> concreteIntValue(FixtureMonkey fixtureMonkey) {
			return fixtureMonkey.giveMeBuilder(FIXED_INT_VALUE);
		}
	}

	public static class ChildBuilderGroup implements ArbitraryBuilderGroup {
		public static final ConcreteIntValue FIXED_INT_VALUE = new ConcreteIntValue();

		@Override
		public ArbitraryBuilderCandidateList generateCandidateList() {
			return ArbitraryBuilderCandidateList.create()
				.add(
					ArbitraryBuilderCandidateFactory
						.of(String.class)
						.builder(
							arbitraryBuilder -> arbitraryBuilder.set(
								Arbitraries.strings()
									.numeric()
									.ofMinLength(1)
									.ofMaxLength(3)
							)
						)
				)
				.add(
					ArbitraryBuilderCandidateFactory
						.of(new TypeReference<List<String>>() {
						})
						.builder(
							builder -> builder.setInner(
								new InnerSpec()
									.maxSize(2)
							)
						)
				)
				.add(
					ArbitraryBuilderCandidateFactory
						.of(ConcreteIntValue.class)
						.value(FIXED_INT_VALUE)
				);
		}
	}

	@EqualsAndHashCode
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

	public static class PairContainerPropertyGenerator implements ContainerPropertyGenerator {
		@Override
		public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
			com.navercorp.fixturemonkey.api.property.Property property = context.getProperty();

			List<AnnotatedType> elementTypes = Types.getGenericsTypes(property.getAnnotatedType());
			if (elementTypes.size() != 2) {
				throw new IllegalArgumentException(
					"Pair elementsTypes must be have 1 generics type for element. "
						+ "propertyType: " + property.getType()
						+ ", elementTypes: " + elementTypes
				);
			}

			AnnotatedType firstElementType = elementTypes.get(0);
			AnnotatedType secondElementType = elementTypes.get(1);
			List<com.navercorp.fixturemonkey.api.property.Property> elementProperties = new ArrayList<>();
			elementProperties.add(
				new ElementProperty(
					property,
					firstElementType,
					0,
					0
				)
			);
			elementProperties.add(
				new ElementProperty(
					property,
					secondElementType,
					1,
					1
				)
			);

			return new ContainerProperty(
				elementProperties,
				new ArbitraryContainerInfo(1, 1)
			);
		}
	}

	public static class PairIntrospector implements ArbitraryIntrospector, Matcher {
		private static final Matcher MATCHER = new AssignableTypeMatcher(Pair.class);

		@Override
		public boolean match(Property property) {
			return MATCHER.match(property);
		}

		@SuppressWarnings("ConstantConditions")
		@Override
		public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
			ArbitraryProperty property = context.getArbitraryProperty();
			if (!property.isContainer()) {
				return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
			}

			List<CombinableArbitrary<?>> elementCombinableArbitraryList = context.getElementCombinableArbitraryList();

			return new ArbitraryIntrospectorResult(
				CombinableArbitrary.containerBuilder()
					.elements(elementCombinableArbitraryList)
					.build(elements -> new Pair<>(elements.get(0), elements.get(1)))
			);
		}

	}

	@Getter
	@Builder
	public static class BuilderInteger {
		private int value;
	}

	@Getter
	@Builder
	public static class CustomBuilderMethodInteger {
		private int value;

		public static CustomBuilderMethodIntegerBuilder customBuilder() {
			return new CustomBuilderMethodIntegerBuilder();
		}
	}

	@Getter
	@Builder
	public static class CustomBuildMethodInteger {
		private int value;

		public static class CustomBuildMethodIntegerBuilder {
			public CustomBuildMethodInteger customBuild() {
				return new CustomBuildMethodInteger(value);
			}
		}
	}

	public static class GetIntegerFixedValue implements GetFixedValue {
		@Override
		public Object get() {
			return 1;
		}
	}

	public static class GetStringFixedValue implements GetFixedValue {
		@Override
		public Object get() {
			return "fixed";
		}
	}

	public static class GetIntegerFixedValueChild implements GetFixedValueChild {

		@Override
		public Object get() {
			return 2;
		}
	}

	@Data
	public static class GenericGetFixedValue<T extends GetFixedValue> {
		T value;
	}

	@Getter
	@Setter
	public abstract static class AbstractSamePropertyValue {
		private String value;

		public AbstractSamePropertyValue(String value) {
			this.value = value;
		}
	}

	@Setter
	public static class ConcreteSamePropertyValue extends AbstractSamePropertyValue {
		private int value;

		@ConstructorProperties({"value", "intValue"})
		public ConcreteSamePropertyValue(String value, int intValue) {
			super(value);
			this.value = intValue;
		}

		public int getIntValue() {
			return value;
		}
	}

	@Data
	public abstract static class AbstractNoneValue {
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class AbstractNoneConcreteIntValue extends AbstractNoneValue {
		private int intValue;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class AbstractNoneConcreteStringValue extends AbstractNoneValue {
		private String stringValue;
	}

	@Data
	public abstract static class AbstractValue {
		private String value;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class ConcreteStringValue extends AbstractValue {
		private String stringValue;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class ConcreteIntValue extends AbstractValue {
		private int intValue;
	}

	@Getter
	public abstract static class SelfRecursiveAbstractValue {
		private final List<SelfRecursiveAbstractValue> recursives;

		@ConstructorProperties("recursives")
		public SelfRecursiveAbstractValue(List<SelfRecursiveAbstractValue> recursives) {
			this.recursives = recursives;
		}
	}

	@Getter
	public static class SelfRecursiveImplementationValue extends SelfRecursiveAbstractValue {
		private final String value;

		@ConstructorProperties({"recursives", "value"})
		public SelfRecursiveImplementationValue(List<SelfRecursiveAbstractValue> recursives, String value) {
			super(recursives);
			this.value = value;
		}
	}

	@Data
	public static class GetterInterfaceImplementation implements GetterInterface {
		private String value;
	}

	@Data
	public static class GetterInterfaceImplementation2 implements GetterInterface {
		private String value;
	}

	public static class UniqueArbitraryGenerator implements ArbitraryGenerator {
		private static final Set<Object> UNIQUE = new HashSet<>();

		private final ArbitraryGenerator delegate;

		public UniqueArbitraryGenerator(ArbitraryGenerator delegate) {
			this.delegate = delegate;
		}

		@Override
		public CombinableArbitrary generate(ArbitraryGeneratorContext context) {
			return delegate.generate(context)
				.filter(
					obj -> {
						if (!UNIQUE.contains(obj)) {
							UNIQUE.add(obj);
							return true;
						}
						return false;
					}
				);
		}
	}
}
