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

package com.navercorp.fixturemonkey.adapter;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.AbstractGenericBox;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.AbstractNoneConcreteIntValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.AbstractNoneConcreteStringValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.AbstractNoneValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.AbstractSamePropertyValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.AbstractValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.AbstractWithNestedInterface;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.ConcreteIntValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.ConcreteSamePropertyValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.ConcreteStringBox;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.ConcreteStringValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.ConcreteWithNestedInterface;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.GenericGetFixedValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.GenericHolder;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.GenericHolderContainer;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.GetFixedValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.GetFixedValueChild;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.GetIntegerFixedValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.GetIntegerFixedValueChild;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.GetStringFixedValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.GetterInterface;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.GetterInterfaceImplementation;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.GetterInterfaceImplementation2;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.IntegerGenericHolder;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.Interface;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.InterfaceHolder;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.InterfaceImplementation;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.SelfRecursiveAbstractValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.SelfRecursiveImplementationValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.SelfRecursiveWithFieldAbstractValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.SelfRecursiveWithFieldImplementation;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringGenericHolder;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringValueProvider;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.ValueProvider;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.ValueProviderContainer;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class ValueProjectionInterfaceTest {

	private static final long SEED = 12345L;

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void generateWithInterfaceField() {
		InterfaceHolder actual = SUT.giveMeOne(InterfaceHolder.class);

		then(actual).isNotNull();
		then(actual.getItems()).isNotNull();
		then(actual.getItems()).isInstanceOf(java.util.ArrayList.class);
	}

	@Property
	void setInterfaceField() {
		List<String> expected = java.util.Arrays.asList("x", "y", "z");

		InterfaceHolder actual = SUT.giveMeBuilder(InterfaceHolder.class).set("items", expected).sample();

		then(actual.getItems()).isEqualTo(expected);
	}

	@Property
	void sizeInterfaceField() {
		InterfaceHolder actual = SUT.giveMeBuilder(InterfaceHolder.class).size("items", 4).sample();

		then(actual.getItems()).hasSize(4);
	}

	@Property
	void interfaceImplements() {
		// given
		List<Class<? extends GetFixedValue>> implementations = new ArrayList<>();
		implementations.add(GetIntegerFixedValue.class);
		implementations.add(GetStringFixedValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(new InterfacePlugin().interfaceImplements(GetFixedValue.class, implementations))
			.build();

		// when
		GetFixedValue result = sut.giveMeOne(GetFixedValue.class);
		Object actual = result.get();

		// then
		then(actual).isIn(1, "fixed");
	}

	@Property
	void sampleGenericInterface() {
		// given
		List<Class<? extends GetFixedValue>> implementations = new ArrayList<>();
		implementations.add(GetIntegerFixedValue.class);
		implementations.add(GetStringFixedValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(new InterfacePlugin().interfaceImplements(GetFixedValue.class, implementations))
			.build();

		// when
		Object actual = sut
			.giveMeBuilder(new TypeReference<GenericGetFixedValue<GetFixedValue>>() {
			})
			.setNotNull("value")
			.sample()
			.getValue()
			.get();

		// then
		then(actual).isIn(1, "fixed");
	}

	@Property(tries = 1)
	void sampleGenericInterfaceReturnsDiff() {
		// given
		List<Class<? extends GetFixedValue>> implementations = new ArrayList<>();
		implementations.add(GetIntegerFixedValue.class);
		implementations.add(GetStringFixedValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(new InterfacePlugin().interfaceImplements(GetFixedValue.class, implementations))
			.build();

		// when
		Set<Class<? extends GetFixedValue>> actual = sut
			.giveMeBuilder(new TypeReference<GenericGetFixedValue<GetFixedValue>>() {
			})
			.setNotNull("value")
			.sampleList(100)
			.stream()
			.map(it -> it.getValue().getClass())
			.collect(Collectors.toSet());

		// then
		then(actual).hasSize(2);
	}

	@Property
	void sampleInterfaceChildWhenOptionHasHierarchy() {
		// given
		List<Class<? extends GetFixedValue>> implementations = new ArrayList<>();
		implementations.add(GetIntegerFixedValue.class);
		implementations.add(GetStringFixedValue.class);

		List<Class<? extends GetFixedValueChild>> childImplementations = new ArrayList<>();
		childImplementations.add(GetIntegerFixedValueChild.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(GetFixedValueChild.class, childImplementations)
					.interfaceImplements(GetFixedValue.class, implementations)
			)
			.build();

		// when
		Object actual = sut.giveMeOne(new TypeReference<GetFixedValueChild>() {
		}).get();

		// then
		then(actual).isEqualTo(2);
	}

	@Property
	void sampleConcreteWhenHasSameNameProperty() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new InterfacePlugin().abstractClassExtends(
					AbstractSamePropertyValue.class,
					Collections.singletonList(ConcreteSamePropertyValue.class)
				)
			)
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

		// when
		AbstractSamePropertyValue actual = sut.giveMeOne(AbstractSamePropertyValue.class);

		// then
		then(actual).isNotNull();
	}

	@Property
	void sampleSelfRecursiveAbstract() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new InterfacePlugin().abstractClassExtends(
					SelfRecursiveAbstractValue.class,
					Collections.singletonList(SelfRecursiveImplementationValue.class)
				)
			)
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

		// when
		SelfRecursiveAbstractValue actual = sut.giveMeOne(SelfRecursiveAbstractValue.class);

		// then
		then(actual).isNotNull();
	}

	@Property
	void setConcreteClassWhenHasParentValue() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new InterfacePlugin().abstractClassExtends(
					AbstractValue.class,
					Collections.singletonList(ConcreteStringValue.class)
				)
			)
			.build();

		ConcreteStringValue expected = new ConcreteStringValue();
		expected.setValue("stringValue");

		// when
		AbstractValue actual = sut.giveMeBuilder(new TypeReference<AbstractValue>() {
		}).set(expected).sample();

		// then
		then(actual).isEqualTo(expected);
	}

	@Property
	void setConcreteClassWhenHasNoParentValue() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new InterfacePlugin().abstractClassExtends(
					AbstractNoneValue.class,
					Collections.singletonList(AbstractNoneConcreteStringValue.class)
				)
			)
			.build();

		AbstractNoneConcreteStringValue expected = new AbstractNoneConcreteStringValue();
		expected.setStringValue("stringValue");

		// when
		AbstractNoneValue actual = sut.giveMeBuilder(new TypeReference<AbstractNoneValue>() {
		}).set(expected).sample();

		// then
		then(actual).isEqualTo(expected);
	}

	@Property
	void setConcreteList() {
		// given
		List<Class<? extends AbstractValue>> implementations = new ArrayList<>();
		implementations.add(ConcreteStringValue.class);
		implementations.add(ConcreteIntValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(new InterfacePlugin().abstractClassExtends(AbstractValue.class, implementations))
			.build();

		ConcreteStringValue concreteStringValue = new ConcreteStringValue();
		concreteStringValue.setValue("stringValue");
		concreteStringValue.setStringValue("test");
		ConcreteIntValue concreteIntValue = new ConcreteIntValue();
		concreteIntValue.setValue("intValue");
		concreteIntValue.setIntValue(-999);
		List<AbstractValue> expected = new ArrayList<>();
		expected.add(concreteStringValue);
		expected.add(concreteIntValue);

		// when
		List<AbstractValue> actual = sut
			.giveMeBuilder(new TypeReference<List<AbstractValue>>() {
			})
			.set(expected)
			.sample();

		// then
		then(actual).isEqualTo(expected);
	}

	@Property
	void setConcreteListWithNoParentValue() {
		// given
		List<Class<? extends AbstractNoneValue>> implementations = new ArrayList<>();
		implementations.add(AbstractNoneConcreteStringValue.class);
		implementations.add(AbstractNoneConcreteIntValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(new InterfacePlugin().abstractClassExtends(AbstractNoneValue.class, implementations))
			.build();

		AbstractNoneConcreteStringValue abstractNoneConcreteStringValue = new AbstractNoneConcreteStringValue();
		abstractNoneConcreteStringValue.setStringValue("test");
		AbstractNoneConcreteIntValue abstractNoneConcreteIntValue = new AbstractNoneConcreteIntValue();
		abstractNoneConcreteIntValue.setIntValue(-999);
		List<AbstractNoneValue> expected = new ArrayList<>();
		expected.add(abstractNoneConcreteStringValue);
		expected.add(abstractNoneConcreteIntValue);

		// when
		List<AbstractNoneValue> actual = sut
			.giveMeBuilder(new TypeReference<List<AbstractNoneValue>>() {
			})
			.set(expected)
			.sample();

		// then
		then(actual).isEqualTo(expected);
	}

	@Property
	void setConcrete() {
		// given
		List<Class<? extends AbstractValue>> implementations = new ArrayList<>();
		implementations.add(ConcreteStringValue.class);
		implementations.add(ConcreteIntValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(new InterfacePlugin().abstractClassExtends(AbstractValue.class, implementations))
			.build();

		ConcreteStringValue expected = new ConcreteStringValue();
		expected.setValue("stringValue");
		expected.setStringValue("test");

		// when
		AbstractValue actual = sut.giveMeBuilder(AbstractValue.class).set(expected).sample();

		// then
		then(actual).isEqualTo(expected);
	}

	@Property
	void samePropertyDiffImplementations() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new InterfacePlugin().interfaceImplements(
					GetterInterface.class,
					Arrays.asList(GetterInterfaceImplementation.class, GetterInterfaceImplementation2.class)
				)
			)
			.build();

		// when
		String actual = sut.giveMeBuilder(GetterInterface.class).set("value", "expected").sample().getValue();

		// then
		then(actual).isEqualTo("expected");
	}

	@Property
	void sampleDifferentGenericImplementations() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new InterfacePlugin().interfaceImplements(
					GenericHolder.class,
					Arrays.asList(StringGenericHolder.class, IntegerGenericHolder.class)
				)
			)
			.build();

		// when
		GenericHolderContainer actual = sut.giveMeOne(GenericHolderContainer.class);

		// then
		then(actual).isNotNull();
		then(actual.getStringHolder()).isNotNull();
		then(actual.getIntegerHolder()).isNotNull();
	}

	@Property
	void sampleAbstractWithNestedInterface() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new InterfacePlugin()
					.abstractClassExtends(
						AbstractWithNestedInterface.class,
						Collections.singletonList(ConcreteWithNestedInterface.class)
					)
					.interfaceImplements(Interface.class, Collections.singletonList(InterfaceImplementation.class))
			)
			.build();

		// when
		AbstractWithNestedInterface actual = sut.giveMeOne(AbstractWithNestedInterface.class);

		// then
		then(actual).isNotNull();
		then(actual).isInstanceOf(ConcreteWithNestedInterface.class);
		then(actual.getNestedInterface()).isNotNull();
		then(actual.getNestedInterface()).isInstanceOf(InterfaceImplementation.class);
	}

	@Property
	void setSelfRecursiveAbstractField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new InterfacePlugin().abstractClassExtends(
					SelfRecursiveWithFieldAbstractValue.class,
					Collections.singletonList(SelfRecursiveWithFieldImplementation.class)
				)
			)
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

		// when
		SelfRecursiveWithFieldAbstractValue actual = sut
			.giveMeBuilder(SelfRecursiveWithFieldAbstractValue.class)
			.set("name", "root")
			.sample();

		// then
		then(actual).isNotNull();
		then(actual.getName()).isEqualTo("root");
	}

	@Property
	void sizeSelfRecursiveAbstractList() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new InterfacePlugin().abstractClassExtends(
					SelfRecursiveAbstractValue.class,
					Collections.singletonList(SelfRecursiveImplementationValue.class)
				)
			)
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

		// when
		SelfRecursiveAbstractValue actual = sut
			.giveMeBuilder(SelfRecursiveAbstractValue.class)
			.size("recursives", 2)
			.sample();

		// then
		then(actual).isNotNull();
		then(actual.getRecursives()).hasSize(2);
	}

	@Property
	void sampleAbstractGenericBoxResolvesFieldTypes() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new InterfacePlugin().abstractClassExtends(
					AbstractGenericBox.class,
					Collections.singletonList(ConcreteStringBox.class)
				)
			)
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		// when
		AbstractGenericBox<?> actual = sut.giveMeOne(AbstractGenericBox.class);

		// then
		then(actual).isNotNull();
		then(actual).isInstanceOf(ConcreteStringBox.class);
		then(actual.getContent()).isInstanceOf(String.class);
		then(actual.getItems()).isNotNull();
		then(actual.getItems()).allSatisfy(item -> then(item).isInstanceOf(String.class));
	}

	@Property
	void sampleInterfaceWithDifferentReturnType() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new InterfacePlugin().interfaceImplements(
					ValueProvider.class,
					Collections.singletonList(StringValueProvider.class)
				)
			)
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		// when
		ValueProviderContainer actual = sut.giveMeOne(ValueProviderContainer.class);

		// then
		then(actual).isNotNull();
		then(actual.getProvider()).isInstanceOf(StringValueProvider.class);
		then(actual.getProvider().getValue()).isNotNull();
	}
}
