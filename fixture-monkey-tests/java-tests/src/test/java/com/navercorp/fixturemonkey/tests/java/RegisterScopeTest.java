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
package com.navercorp.fixturemonkey.tests.java;

import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedString;
import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.constructor;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.beans.ConstructorProperties;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.exception.FixedValueFilterMissException;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin;

class RegisterScopeTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(Child.class, fm -> fm.giveMeBuilder(Child.class).size("values", 5))
		.build();

	private static final FixtureMonkey OUTER_SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(Outer.class, fm -> fm.giveMeBuilder(Outer.class).size("holders", 2).size("holders[*].first", 4))
		.build();

	private static final FixtureMonkey LIST_SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(
			List.class,
			fm -> fm.giveMeBuilder(new TypeReference<List<String>>() {
				})
				.size("$", 2)
				.set("$[0]", "first")
		)
		.build();

	private static final FixtureMonkey NESTED_SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(
			Nested.class,
			fm -> fm.giveMeBuilder(Nested.class).size("values", 2).size("values[*].values", 3)
		)
		.build();

	private static final FixtureMonkey TYPE_MATCHER_SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(
			new MatcherOperator<>(
				property -> property.getJvmType().getRawType() == MatcherChild.class,
				fm -> fm.giveMeBuilder(MatcherChild.class).set("name", "custom")
			)
		)
		.build();

	private static final FixtureMonkey NAME_MATCHER_SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(
			new MatcherOperator<>(
				property -> "child".equals(property.getName()),
				fm -> fm.giveMeBuilder(MatcherChild.class).set("name", "named")
			)
		)
		.build();

	private static final FixtureMonkey WHOLE_VALUE_SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(
			PostConditionChild.class,
			fm -> fm.giveMeBuilder(PostConditionChild.class).set("$", new PostConditionChild("whole"))
		)
		.build();

	private static final FixtureMonkey COUNT_SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(
			CountChild.class,
			fm -> fm.giveMeBuilder(CountChild.class).setPostCondition("count", int.class, it -> it >= 0)
		)
		.build();

	private static final FixtureMonkey SCOPE_LIMIT_SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(Bag.class, fm -> fm.giveMeBuilder(Bag.class).size("items", 3).set("items[*]", "x", 2))
		.build();

	private static final FixtureMonkey SAME_TYPE_SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(
			SameTypeChild.class,
			fm -> fm.giveMeBuilder(SameTypeChild.class).set("name", "low").set("tag", "lowTag"),
			2
		)
		.register(
			SameTypeChild.class,
			fm -> fm.giveMeBuilder(SameTypeChild.class).set("name", "high").set("count", 5),
			1
		)
		.build();

	@RepeatedTest(10)
	void registeredSizeAppliesToRegisteredType() {
		// when
		List<String> actual = SUT.giveMeOne(Parent.class).getChild().getValues();

		// then
		then(actual).hasSize(5);
	}

	@RepeatedTest(10)
	void registeredSizeDoesNotApplyToSameNamedPropertyOfEnclosingType() {
		// when
		List<String> actual = SUT.giveMeOne(Parent.class).getValues();

		// then
		then(actual).hasSizeLessThanOrEqualTo(3);
	}

	@RepeatedTest(10)
	void rootTypeRegisteredSizeAppliesWhenUserDeclaresNone() {
		// when
		Outer actual = OUTER_SUT.giveMeOne(Outer.class);

		// then
		then(actual.getHolders()).hasSize(2);
		then(actual.getHolders().get(0).getFirst()).hasSize(4);
		then(actual.getHolders().get(1).getFirst()).hasSize(4);
	}

	@RepeatedTest(10)
	void userSizeAtSamePathWinsOverRootTypeRegisteredSize() {
		// when
		Outer actual = OUTER_SUT.giveMeBuilder(Outer.class).size("holders", 3).sample();

		// then
		then(actual.getHolders()).hasSize(3);
	}

	@RepeatedTest(10)
	void userExactSizeWinsOverRootTypeRegisteredWildcardSizeAtItsPath() {
		// when
		Outer actual = OUTER_SUT.giveMeBuilder(Outer.class).size("holders[0].first", 1).sample();

		// then
		then(actual.getHolders().get(0).getFirst()).hasSize(1);
		then(actual.getHolders().get(1).getFirst()).hasSize(4);
	}

	@RepeatedTest(10)
	void registeredSizeAlsoAppliesWhenTypeIsNested() {
		// when
		List<Holder> actual = OUTER_SUT.giveMeOne(Wrapper.class).getOuter().getHolders();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getFirst()).hasSize(4);
	}

	@RepeatedTest(10)
	void userWildcardSizeWinsOverRootTypeRegisteredExactSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Outer.class, fm -> fm.giveMeBuilder(Outer.class).size("holders", 2).size("holders[0].first", 4))
			.build();

		// when
		Outer actual = sut.giveMeBuilder(Outer.class).size("holders[*].first", 1).sample();

		// then
		then(actual.getHolders()).hasSize(2);
		then(actual.getHolders().get(0).getFirst()).hasSize(1);
		then(actual.getHolders().get(1).getFirst()).hasSize(1);
	}

	@RepeatedTest(10)
	void registeredSizeOfSampledContainerItselfApplies() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				List.class,
				fm -> fm.giveMeBuilder(new TypeReference<List<String>>() {
					})
					.size("$", 2)
			)
			.build();

		// when
		List<String> actual = sut.giveMeOne(new TypeReference<List<String>>() {
		});

		// then
		then(actual).hasSize(2);
	}

	@RepeatedTest(10)
	void userSizeOfSampledContainerWinsOverRegisteredSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				List.class,
				fm -> fm.giveMeBuilder(new TypeReference<List<String>>() {
					})
					.size("$", 2)
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.size("$", 4)
			.sample();

		// then
		then(actual).hasSize(4);
	}

	@RepeatedTest(10)
	void registeredSizeOfContainerTypeItselfApplies() {
		// when
		List<String> actual = LIST_SUT.giveMeOne(NamesHolder.class).getNames();

		// then
		then(actual).hasSize(2);
	}

	@RepeatedTest(10)
	void registeredElementValueOfContainerTypeApplies() {
		// when
		List<String> actual = LIST_SUT.giveMeOne(NamesHolder.class).getNames();

		// then
		then(actual.get(0)).isEqualTo("first");
	}

	@RepeatedTest(5)
	void registeredNestedPathSizeAppliesAtNestedPosition() {
		// when
		Nested actual = NESTED_SUT.giveMeOne(NestedHolder.class).getNested();

		// then
		then(actual.getValues()).hasSize(2);
		then(actual.getValues()).allSatisfy(inner -> then(inner.getValues()).hasSize(3));
	}

	@RepeatedTest(5)
	void registeredNestedPathSizeAppliesWhenSamplingScopeRoot() {
		// when
		Nested actual = NESTED_SUT.giveMeOne(Nested.class);

		// then
		then(actual.getValues()).hasSize(2);
		then(actual.getValues()).allSatisfy(inner -> then(inner.getValues()).hasSize(3));
	}

	@RepeatedTest(5)
	void laterSizeWinsWithinRegisteredBuilder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Inner.class, fm -> fm.giveMeBuilder(Inner.class).size("values", 5).size("values", 1))
			.build();

		// when
		Nested actual = sut.giveMeBuilder(Nested.class).size("values", 2).sample();

		// then
		then(actual.getValues()).allSatisfy(inner -> then(inner.getValues()).hasSize(1));
	}

	@Test
	void customMatcherBuilderAppliesToEveryMatchingProperty() {
		// when
		MatcherParent actual = TYPE_MATCHER_SUT.giveMeOne(MatcherParent.class);

		// then
		then(actual.getChild().getName()).isEqualTo("custom");
		then(actual.getOther().getName()).isEqualTo("custom");
	}

	@Test
	void customMatcherBuilderAppliesWhenSamplingMatchingType() {
		// when
		MatcherChild actual = TYPE_MATCHER_SUT.giveMeOne(MatcherChild.class);

		// then
		then(actual.getName()).isEqualTo("custom");
	}

	@Test
	void customMatcherBuilderAppliesOnlyToMatchingProperty() {
		// when
		MatcherParent actual = NAME_MATCHER_SUT.giveMeOne(MatcherParent.class);

		// then
		then(actual.getChild().getName()).isEqualTo("named");
		then(actual.getOther().getName()).isNotEqualTo("named");
	}

	@Test
	void userSetWinsOverCustomMatcherBuilder() {
		// when
		MatcherParent actual = TYPE_MATCHER_SUT.giveMeBuilder(MatcherParent.class)
			.set("child.name", "user")
			.sample();

		// then
		then(actual.getChild().getName()).isEqualTo("user");
		then(actual.getOther().getName()).isEqualTo("custom");
	}

	@Test
	void customMatcherBuilderSizeAppliesOnlyToMatchingProperty() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				new MatcherOperator<>(
					property -> "bag".equals(property.getName()),
					fm -> fm.giveMeBuilder(Bag.class).size("items", 5)
				)
			)
			.build();

		// when
		BagHolder actual = sut.giveMeOne(BagHolder.class);

		// then
		then(actual.getBag().getItems()).hasSize(5);
		then(actual.getOther().getItems()).hasSizeLessThan(5);
	}

	@Test
	void customMatcherBuilderInstantiateAppliesOnlyInsideMatchingProperty() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				new MatcherOperator<>(
					property -> "box".equals(property.getName()),
					fm -> fm.giveMeBuilder(TaggedBox.class)
						.instantiate(Tagged.class, constructor().parameter(String.class, "text"))
				)
			)
			.build();

		// when
		BoxHolder actual = sut.giveMeOne(BoxHolder.class);

		// then
		then(actual.getBox().getTagged().getOrigin()).isEqualTo("string-constructor");
		then(actual.getOther().getTagged().getOrigin()).isEqualTo("int-constructor");
	}

	@Test
	void customMatcherBuilderInstantiateAppliesToMatchingPropertyItself() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				new MatcherOperator<>(
					property -> "tagged".equals(property.getName()),
					fm -> fm.giveMeBuilder(Tagged.class)
						.instantiate(Tagged.class, constructor().parameter(String.class, "text"))
				)
			)
			.build();

		// when
		TaggedPair actual = sut.giveMeOne(TaggedPair.class);

		// then
		then(actual.getTagged().getOrigin()).isEqualTo("string-constructor");
		then(actual.getOther().getOrigin()).isEqualTo("int-constructor");
	}

	@Test
	void customMatcherBuilderAppliesToMatchingPropertiesAtDifferentDepths() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				new MatcherOperator<>(
					property -> property.getAnnotation(Special.class).isPresent(),
					fm -> fm.giveMeBuilder(MatcherChild.class).set("name", "special")
				)
			)
			.build();

		// when
		SpecialHolder actual = sut.giveMeOne(SpecialHolder.class);

		// then
		then(actual.getMain().getName()).isEqualTo("special");
		then(actual.getWrapper().getInner().getName()).isEqualTo("special");
		then(actual.getPlain().getName()).isNotEqualTo("special");
	}

	@Test
	void outerMatchedNodeWinsWhenMatchedNodesAreNested() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				new MatcherOperator<>(
					property -> property.getAnnotation(Special.class).isPresent(),
					fm -> fm.giveMeBuilder(Group.class)
						.set("name", "inner")
						.set("child.name", "outer")
				)
			)
			.build();

		// when
		Group actual = sut.giveMeOne(GroupHolder.class).getGroup();

		// then
		then(actual.getName()).isEqualTo("inner");
		then(actual.getChild().getName()).isEqualTo("outer");
	}

	@RepeatedTest(5)
	void decomposedRegisteredValueKeepsFieldRenamedByPlugin() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JacksonPlugin())
			.defaultNotNull(true)
			.register(
				ExpansionChild.class,
				fm -> fm.giveMeBuilder(ExpansionChild.class).set("$", new ExpansionChild("registered", "registered"))
			)
			.build();

		// when
		ExpansionChild actual = sut.giveMeBuilder(ExpansionHolder.class)
			.set("child.other", "user")
			.sample()
			.getChild();

		// then
		then(actual.getUserName()).isEqualTo("registered");
		then(actual.getOther()).isEqualTo("user");
	}

	@RepeatedTest(10)
	void decomposedRegisteredValueAtInterfaceKeepsUserDirectiveInsideIt() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Shape.class, Arrays.asList(Circle.class, Square.class)))
			.register(
				ShapeHolder.class,
				fm -> fm.giveMeBuilder(ShapeHolder.class).set("$", new ShapeHolder(new Circle("registered", 3)))
			)
			.build();

		// when
		Shape actual = sut.giveMeBuilder(ShapeOwner.class)
			.set("holder.shape.name", "user")
			.sample()
			.getHolder()
			.getShape();

		// then
		then(actual).isInstanceOf(Circle.class);
		then(((Circle)actual).getName()).isEqualTo("user");
		then(((Circle)actual).getRadius()).isEqualTo(3);
	}

	@Test
	void postConditionAfterWholeValueRejectsFieldOfIt() {
		// given
		FixtureMonkey sut = registeringChild(builder -> shortName(builder.set("$", new PostConditionChild("whole"))));

		// when, then
		thenThrownBy(() -> sut.giveMeOne(PostConditionParent.class))
			.hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void postConditionBeforeWholeValueRejectsFieldOfIt() {
		// given
		FixtureMonkey sut = registeringChild(builder -> shortName(builder).set("$", new PostConditionChild("whole")));

		// when, then
		thenThrownBy(() -> sut.giveMeOne(PostConditionParent.class))
			.hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void wholeValueSatisfyingPostConditionIsKept() {
		// given
		FixtureMonkey sut = registeringChild(
			builder -> builder.set("$", new PostConditionChild("whole"))
				.setPostCondition("name", String.class, it -> it.startsWith("w"))
		);

		// when
		String actual = sut.giveMeOne(PostConditionParent.class).getChild().getName();

		// then
		then(actual).isEqualTo("whole");
	}

	@Test
	void registeredPostConditionRejectsFieldOfUserWholeValue() {
		// given
		FixtureMonkey sut = registeringChild(RegisterScopeTest::shortName);

		// when, then
		thenThrownBy(() -> sut.giveMeBuilder(PostConditionParent.class)
			.set("child", new PostConditionChild("whole"))
			.sample()
		)
			.hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void rootPostConditionRejectsFieldOfRegisteredWholeValue() {
		// when, then
		thenThrownBy(
			() -> WHOLE_VALUE_SUT.giveMeBuilder(ChildrenParent.class)
				.setPostCondition("child.name", String.class, it -> it.length() < 3)
				.sample()
		)
			.hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void rootWildcardPostConditionRejectsFieldOfRegisteredWholeValue() {
		// when, then
		thenThrownBy(
			() -> WHOLE_VALUE_SUT.giveMeBuilder(ChildrenParent.class)
				.size("children", 1)
				.setPostCondition("children[*].name", String.class, it -> it.length() < 3)
				.sample()
		)
			.hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void registeredWholeValueSatisfyingRootPostConditionIsKept() {
		// when
		String actual = WHOLE_VALUE_SUT.giveMeBuilder(ChildrenParent.class)
			.setPostCondition("child.name", String.class, it -> it.startsWith("w"))
			.sample()
			.getChild()
			.getName();

		// then
		then(actual).isEqualTo("whole");
	}

	@Test
	void rootCustomizerAppliesToFieldOfRegisteredWholeValue() {
		// when
		String actual = WHOLE_VALUE_SUT.giveMeBuilder(ChildrenParent.class)
			.<String>customizeProperty(typedString("child.name"), it -> it.map(name -> name + "!"))
			.sample()
			.getChild()
			.getName();

		// then
		then(actual).isEqualTo("whole!");
	}

	@Test
	void registeredPostConditionRejectsUserRootContainerElement() {
		// given
		List<CountChild> invalid = Collections.singletonList(new CountChild(-1));

		// when, then
		thenThrownBy(() -> COUNT_SUT.giveMeBuilder(new TypeReference<List<CountChild>>() {
			})
				.set("$", invalid)
				.sample()
		).hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void registeredPostConditionRejectsUserNestedContainerElement() {
		// given
		List<CountChild> invalid = Collections.singletonList(new CountChild(-1));

		// when, then
		thenThrownBy(() -> COUNT_SUT.giveMeBuilder(CountParent.class)
			.set("children", invalid)
			.sample()
		).hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void registeredPostConditionRejectsUserNestedObject() {
		// when, then
		thenThrownBy(() -> COUNT_SUT.giveMeBuilder(CountParent.class)
			.set("child", new CountChild(-1))
			.sample()
		).hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void userRootContainerElementSatisfyingRegisteredPostConditionIsKept() {
		// given
		List<CountChild> valid = Collections.singletonList(new CountChild(1));

		// when
		List<CountChild> actual = COUNT_SUT.giveMeBuilder(new TypeReference<List<CountChild>>() {
			})
			.set("$", valid)
			.sample();

		// then
		then(actual).isEqualTo(valid);
	}

	private static ArbitraryBuilder<PostConditionChild> shortName(ArbitraryBuilder<PostConditionChild> builder) {
		return builder.setPostCondition("name", String.class, it -> it.length() < 3);
	}

	private static FixtureMonkey registeringChild(
		Function<ArbitraryBuilder<PostConditionChild>, ArbitraryBuilder<PostConditionChild>> declare
	) {
		return FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(PostConditionChild.class, fm -> declare.apply(fm.giveMeBuilder(PostConditionChild.class)))
			.build();
	}

	@RepeatedTest(5)
	void registeredSizeOfHigherPriorityWinsOnSameNode() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				MatcherOperator.exactTypeMatchOperator(Bag.class, fm -> fm.giveMeBuilder(Bag.class).size("items", 1)),
				5
			)
			.register(Bag.class, fm -> fm.giveMeBuilder(Bag.class).size("items", 4), 1)
			.build();

		// when
		List<String> actual = sut.giveMeOne(SameNodeBagHolder.class).getBag().getItems();

		// then
		then(actual).hasSize(4);
	}

	@RepeatedTest(5)
	void higherPriorityBuilderSetNullWinsOverLowerPrioritySetNotNullOfSameType() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.register(Bag.class, fm -> fm.giveMeBuilder(Bag.class).setNotNull("items"), 5)
			.register(Bag.class, fm -> fm.giveMeBuilder(Bag.class).setNull("items"), 1)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(SameNodeBagHolder.class).setNotNull("bag").sample().getBag().getItems();

		// then
		then(actual).isNull();
	}

	@RepeatedTest(5)
	void higherPriorityBuilderRootValueWinsOverLowerPriorityFieldOfSameType() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Bag.class, fm -> fm.giveMeBuilder(Bag.class).size("items", 2).set("items[0]", "lower"), 5)
			.register(
				Bag.class,
				fm -> fm.giveMeBuilder(Bag.class).set("$", new Bag(Collections.singletonList("higher"))),
				1
			)
			.build();

		// when
		List<String> actual = sut.giveMeOne(SameNodeBagHolder.class).getBag().getItems();

		// then
		then(actual).containsExactly("higher");
	}

	@RepeatedTest(5)
	void registeredLimitAppliesWithinEachScope() {
		// when
		BagPair actual = SCOPE_LIMIT_SUT.giveMeOne(BagPair.class);

		// then
		then(actual.getFirst().getItems()).containsExactly("x", "x", actual.getFirst().getItems().get(2));
		then(actual.getFirst().getItems().get(2)).isNotEqualTo("x");
		then(actual.getSecond().getItems()).containsExactly("x", "x", actual.getSecond().getItems().get(2));
		then(actual.getSecond().getItems().get(2)).isNotEqualTo("x");
	}

	@RepeatedTest(5)
	void registeredLimitAppliesWhenSamplingScopeRoot() {
		// when
		List<String> actual = SCOPE_LIMIT_SUT.giveMeOne(Bag.class).getItems();

		// then
		then(actual).filteredOn("x"::equals).hasSize(2);
	}

	@RepeatedTest(10)
	void higherPriorityValueWinsWhenSameTypeIsRegisteredTwice() {
		// when
		SameTypeChild actual = SAME_TYPE_SUT.giveMeOne(SameTypeParent.class).getChild();

		// then
		then(actual.getName()).isEqualTo("high");
	}

	@RepeatedTest(10)
	void pathsDeclaredOnlyByOneOfSameTypeRegistrationsApply() {
		// when
		SameTypeChild actual = SAME_TYPE_SUT.giveMeOne(SameTypeParent.class).getChild();

		// then
		then(actual.getTag()).isEqualTo("lowTag");
		then(actual.getCount()).isEqualTo(5);
	}

	@RepeatedTest(10)
	void rootContainerValueKeepsItsElementsOverRegisteredElementType() {
		// given
		List<SameTypeChild> expected = Arrays.asList(new SameTypeChild("a", "t", 1), new SameTypeChild("b", "u", 2));

		// when
		List<SameTypeChild> actual = SAME_TYPE_SUT.giveMeBuilder(new TypeReference<List<SameTypeChild>>() {
			})
			.set("$", expected)
			.sample();

		// then
		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(10)
	void rootValueWinsOverScopeSelectingSampledRoot() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(RootScopeOuter.class, fm -> fm.giveMeBuilder(RootScopeOuter.class).set("name", "defined"))
			.build();

		// when
		String actual = sut.giveMeBuilder(RootScopeOuter.class).set("name", "root").sample().getName();

		// then
		then(actual).isEqualTo("root");
	}

	@RepeatedTest(10)
	void rootValueWinsOverInnerScope() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(RootScopeInner.class, fm -> fm.giveMeBuilder(RootScopeInner.class).set("name", "defined"))
			.build();

		// when
		String actual = sut.giveMeBuilder(RootScopeOuter.class).set("inner.name", "root").sample().getInner().getName();

		// then
		then(actual).isEqualTo("root");
	}

	@RepeatedTest(10)
	void scopeSelectingSampledRootCustomizerSkipsRootValue() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(
				RootScopeOuter.class,
				fm -> fm.giveMeBuilder(RootScopeOuter.class)
					.<String>customizeProperty(typedString("name"), it -> it.map(name -> name + "!"))
			)
			.build();

		// when
		String actual = sut.giveMeBuilder(RootScopeOuter.class).set("name", "root").sample().getName();

		// then
		then(actual).isEqualTo("root");
	}

	@RepeatedTest(10)
	void innerScopeCustomizerSkipsRootValue() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(
				RootScopeInner.class,
				fm -> fm.giveMeBuilder(RootScopeInner.class)
					.<String>customizeProperty(typedString("name"), it -> it.map(name -> name + "!"))
			)
			.build();

		// when
		String actual = sut.giveMeBuilder(RootScopeOuter.class).set("inner.name", "root").sample().getInner().getName();

		// then
		then(actual).isEqualTo("root");
	}

	@RepeatedTest(10)
	void rootCustomizerAppliesToValueOfScopeSelectingSampledRoot() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(RootScopeOuter.class, fm -> fm.giveMeBuilder(RootScopeOuter.class).set("name", "defined"))
			.build();

		// when
		String actual = sut.giveMeBuilder(RootScopeOuter.class)
			.<String>customizeProperty(typedString("name"), it -> it.map(name -> name + "!"))
			.sample()
			.getName();

		// then
		then(actual).isEqualTo("defined!");
	}

	@RepeatedTest(10)
	void rootNotNullWinsOverNullOfScopeSelectingSampledRoot() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(RootScopeOuter.class, fm -> fm.giveMeBuilder(RootScopeOuter.class).setNull("name"))
			.build();

		// when
		String actual = sut.giveMeBuilder(RootScopeOuter.class).setNotNull("name").sample().getName();

		// then
		then(actual).isNotNull();
	}

	@RepeatedTest(10)
	void rootNullWinsOverNotNullOfScopeSelectingSampledRoot() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(RootScopeOuter.class, fm -> fm.giveMeBuilder(RootScopeOuter.class).setNotNull("name"))
			.build();

		// when
		String actual = sut.giveMeBuilder(RootScopeOuter.class).setNull("name").sample().getName();

		// then
		then(actual).isNull();
	}

	@RepeatedTest(10)
	void rootLimitAndScopeLimitAreCountedSeparately() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(RootScopeInner.class, fm -> fm.giveMeBuilder(RootScopeInner.class).set("name", "defined"))
			.build();

		// when
		List<RootScopeInner> actual = sut.giveMeBuilder(RootScopeOuter.class)
			.size("inners", 3)
			.set("inners[*].name", "root", 1)
			.sample()
			.getInners();

		// then
		then(actual.get(0).getName()).isEqualTo("root");
		then(actual.get(1).getName()).isEqualTo("defined");
		then(actual.get(2).getName()).isEqualTo("defined");
	}

	private static FixtureMonkeyBuilder fixtureMonkey() {
		return FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true);
	}

	@Data
	public static class Parent {
		private Child child;
		private List<String> values;
	}

	@Data
	public static class Child {
		private List<String> values;
	}

	@Data
	public static class Wrapper {
		private Outer outer;
	}

	@Data
	public static class Outer {
		private List<Holder> holders;
	}

	@Data
	public static class Holder {
		private List<String> first;
	}

	@Data
	public static class NamesHolder {
		private List<String> names;
	}

	public static class Inner {
		private final List<String> values;

		@ConstructorProperties("values")
		public Inner(List<String> values) {
			this.values = values;
		}

		public List<String> getValues() {
			return values;
		}
	}

	public static class Nested {
		private final List<Inner> values;

		@ConstructorProperties("values")
		public Nested(List<Inner> values) {
			this.values = values;
		}

		public List<Inner> getValues() {
			return values;
		}
	}

	public static class NestedHolder {
		private final Nested nested;

		@ConstructorProperties("nested")
		public NestedHolder(Nested nested) {
			this.nested = nested;
		}

		public Nested getNested() {
			return nested;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.PARAMETER})
	public @interface Special {
	}

	public static class MatcherChild {
		private final String name;

		@ConstructorProperties("name")
		public MatcherChild(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public static class MatcherParent {
		private final MatcherChild child;
		private final MatcherChild other;

		@ConstructorProperties({"child", "other"})
		public MatcherParent(MatcherChild child, MatcherChild other) {
			this.child = child;
			this.other = other;
		}

		public MatcherChild getChild() {
			return child;
		}

		public MatcherChild getOther() {
			return other;
		}
	}

	public static class Bag {
		private final List<String> items;

		@ConstructorProperties("items")
		public Bag(List<String> items) {
			this.items = items;
		}

		public List<String> getItems() {
			return items;
		}
	}

	public static class BagHolder {
		private final Bag bag;
		private final Bag other;

		@ConstructorProperties({"bag", "other"})
		public BagHolder(Bag bag, Bag other) {
			this.bag = bag;
			this.other = other;
		}

		public Bag getBag() {
			return bag;
		}

		public Bag getOther() {
			return other;
		}
	}

	public static class Tagged {
		private final String origin;

		@ConstructorProperties("number")
		public Tagged(int number) {
			this.origin = "int-constructor";
		}

		public Tagged(String text) {
			this.origin = "string-constructor";
		}

		public String getOrigin() {
			return origin;
		}
	}

	public static class TaggedBox {
		private final Tagged tagged;

		@ConstructorProperties("tagged")
		public TaggedBox(Tagged tagged) {
			this.tagged = tagged;
		}

		public Tagged getTagged() {
			return tagged;
		}
	}

	public static class BoxHolder {
		private final TaggedBox box;
		private final TaggedBox other;

		@ConstructorProperties({"box", "other"})
		public BoxHolder(TaggedBox box, TaggedBox other) {
			this.box = box;
			this.other = other;
		}

		public TaggedBox getBox() {
			return box;
		}

		public TaggedBox getOther() {
			return other;
		}
	}

	public static class TaggedPair {
		private final Tagged tagged;
		private final Tagged other;

		@ConstructorProperties({"tagged", "other"})
		public TaggedPair(Tagged tagged, Tagged other) {
			this.tagged = tagged;
			this.other = other;
		}

		public Tagged getTagged() {
			return tagged;
		}

		public Tagged getOther() {
			return other;
		}
	}

	public static class MatcherWrapper {
		@Special
		private final MatcherChild inner;

		@ConstructorProperties("inner")
		public MatcherWrapper(MatcherChild inner) {
			this.inner = inner;
		}

		public MatcherChild getInner() {
			return inner;
		}
	}

	public static class SpecialHolder {
		@Special
		private final MatcherChild main;
		private final MatcherChild plain;
		private final MatcherWrapper wrapper;

		@ConstructorProperties({"main", "plain", "wrapper"})
		public SpecialHolder(MatcherChild main, MatcherChild plain, MatcherWrapper wrapper) {
			this.main = main;
			this.plain = plain;
			this.wrapper = wrapper;
		}

		public MatcherChild getMain() {
			return main;
		}

		public MatcherChild getPlain() {
			return plain;
		}

		public MatcherWrapper getWrapper() {
			return wrapper;
		}
	}

	public static class Group {
		private final String name;
		@Special
		private final MatcherChild child;

		@ConstructorProperties({"name", "child"})
		public Group(String name, MatcherChild child) {
			this.name = name;
			this.child = child;
		}

		public String getName() {
			return name;
		}

		public MatcherChild getChild() {
			return child;
		}
	}

	public static class GroupHolder {
		@Special
		private final Group group;

		@ConstructorProperties("group")
		public GroupHolder(Group group) {
			this.group = group;
		}

		public Group getGroup() {
			return group;
		}
	}

	public interface Shape {
	}

	public static class Circle implements Shape {
		private final String name;
		private final int radius;

		@ConstructorProperties({"name", "radius"})
		public Circle(String name, int radius) {
			this.name = name;
			this.radius = radius;
		}

		public String getName() {
			return name;
		}

		public int getRadius() {
			return radius;
		}
	}

	public static class Square implements Shape {
		private final String name;

		@ConstructorProperties("name")
		public Square(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public static class ShapeHolder {
		private final Shape shape;

		@ConstructorProperties("shape")
		public ShapeHolder(Shape shape) {
			this.shape = shape;
		}

		public Shape getShape() {
			return shape;
		}
	}

	public static class ShapeOwner {
		private final ShapeHolder holder;

		@ConstructorProperties("holder")
		public ShapeOwner(ShapeHolder holder) {
			this.holder = holder;
		}

		public ShapeHolder getHolder() {
			return holder;
		}
	}

	public static class ExpansionChild {
		@JsonProperty("user_name")
		private String userName;
		private String other;

		public ExpansionChild() {
		}

		public ExpansionChild(String userName, String other) {
			this.userName = userName;
			this.other = other;
		}

		public String getUserName() {
			return userName;
		}

		public String getOther() {
			return other;
		}
	}

	public static class ExpansionHolder {
		private ExpansionChild child;

		public ExpansionChild getChild() {
			return child;
		}
	}

	@Data
	public static class PostConditionParent {
		private PostConditionChild child;
	}

	@Data
	public static class ChildrenParent {
		private PostConditionChild child;
		private List<PostConditionChild> children;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PostConditionChild {
		private String name;
	}

	@Data
	public static class CountParent {
		private CountChild child;
		private List<CountChild> children;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CountChild {
		private int count;
	}

	public static class SameNodeBagHolder {
		private final Bag bag;

		@ConstructorProperties("bag")
		public SameNodeBagHolder(Bag bag) {
			this.bag = bag;
		}

		public Bag getBag() {
			return bag;
		}
	}

	public static class BagPair {
		private final Bag first;
		private final Bag second;

		@ConstructorProperties({"first", "second"})
		public BagPair(Bag first, Bag second) {
			this.first = first;
			this.second = second;
		}

		public Bag getFirst() {
			return first;
		}

		public Bag getSecond() {
			return second;
		}
	}

	@Data
	public static class SameTypeParent {
		private SameTypeChild child;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SameTypeChild {
		private String name;
		private String tag;
		private int count;
	}

	@Data
	public static class RootScopeOuter {
		private String name;
		private RootScopeInner inner;
		private List<RootScopeInner> inners;
	}

	@Data
	public static class RootScopeInner {
		private String name;
	}
}
