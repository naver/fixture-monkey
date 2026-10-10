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
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import lombok.Data;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.ExactTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.NamedMatcher;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.experimental.ExperimentalArbitraryBuilder;

class RegisterByNameTest {
	private static final int SAMPLE_COUNT = 50;
	private static final String DELIVERED = "DELIVERED";
	private static final String CANCELLED = "CANCELLED";

	@Test
	void builderRegisteredByNameIsUsedWithoutSelectingIt() {
		// given
		FixtureMonkey sut = builder()
			.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED))
			.build();

		// when
		Set<String> actual = statusesOf(() -> sut.giveMeOne(Order.class));

		// then
		then(actual).containsExactly(DELIVERED);
	}

	@Test
	void buildersRegisteredByNameForSameTypeAreChosenAcrossSamples() {
		// given
		FixtureMonkey sut = withOrderVariants().build();

		// when
		Set<String> actual = statusesOf(() -> sut.giveMeOne(Order.class));

		// then
		then(actual).containsExactlyInAnyOrder(DELIVERED, CANCELLED);
	}

	@Test
	void onlyOneBuilderRegisteredByNameForSameTypeAppliesToEachSample() {
		// given
		FixtureMonkey sut = builder()
			.registerByName(
				"delivered",
				Order.class,
				fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED).set("memo", "delivered")
			)
			.registerByName(
				"cancelled",
				Order.class,
				fm -> fm.giveMeBuilder(Order.class).set("status", CANCELLED).set("memo", "cancelled")
			)
			.build();

		// when
		Set<Map.Entry<String, String>> actual = IntStream.range(0, SAMPLE_COUNT)
			.mapToObj(it -> sut.giveMeOne(Order.class))
			.map(it -> new SimpleEntry<>(it.getStatus(), it.getMemo()))
			.collect(Collectors.toSet());

		// then
		then(actual).containsExactlyInAnyOrder(
			new SimpleEntry<>(DELIVERED, "delivered"),
			new SimpleEntry<>(CANCELLED, "cancelled")
		);
	}

	@Test
	void sameSeedChoosesSameBuildersRegisteredByName() {
		// given
		long seed = 777L;

		// when
		List<String> first = statusesWithSeed(seed);
		List<String> second = statusesWithSeed(seed);

		// then
		then(first).contains(DELIVERED, CANCELLED);
		then(second).isEqualTo(first);
	}

	@Test
	void builderRegisteredByNameWithLowerPriorityNumberIsAlwaysUsed() {
		// given
		FixtureMonkey sut = builder()
			.registerByName("cancelled", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", CANCELLED), 2)
			.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED), 1)
			.build();

		// when
		Set<String> actual = statusesOf(() -> sut.giveMeOne(Order.class));

		// then
		then(actual).containsExactly(DELIVERED);
	}

	@Test
	void buildersRegisteredByNameSharingLowestPriorityNumberCompete() {
		// given
		FixtureMonkey sut = builder()
			.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED), 1)
			.registerByName("cancelled", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", CANCELLED), 1)
			.registerByName("returned", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", "RETURNED"), 2)
			.build();

		// when
		Set<String> actual = statusesOf(() -> sut.giveMeOne(Order.class));

		// then
		then(actual).containsExactlyInAnyOrder(DELIVERED, CANCELLED);
	}

	@Test
	void builderRegisteredByNameWithHigherWeightIsChosenMoreOften() {
		// given
		FixtureMonkey sut = builder()
			.seed(777L)
			.registerByName(
				"delivered",
				Order.class,
				fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED),
				Integer.MAX_VALUE,
				1
			)
			.registerByName(
				"cancelled",
				Order.class,
				fm -> fm.giveMeBuilder(Order.class).set("status", CANCELLED),
				Integer.MAX_VALUE,
				9
			)
			.build();

		// when
		Map<String, Long> actual = IntStream.range(0, 200)
			.mapToObj(it -> sut.giveMeOne(Order.class).getStatus())
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		// then
		then(actual).containsOnlyKeys(DELIVERED, CANCELLED);
		then(actual.get(CANCELLED)).isGreaterThan(actual.get(DELIVERED) * 3);
	}

	@Test
	void selectedNameIsUsedWhateverItsWeight() {
		// given
		FixtureMonkey sut = builder()
			.registerByName(
				"delivered",
				Order.class,
				fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED),
				Integer.MAX_VALUE,
				1
			)
			.registerByName(
				"cancelled",
				Order.class,
				fm -> fm.giveMeBuilder(Order.class).set("status", CANCELLED),
				Integer.MAX_VALUE,
				1000
			)
			.build();

		// when
		Set<String> actual = statusesOf(
			() -> sut.giveMeExperimentalBuilder(Order.class).selectName("delivered").sample()
		);

		// then
		then(actual).containsExactly(DELIVERED);
	}

	@Test
	void higherWeightDoesNotMakeHigherPriorityNumberCompete() {
		// given
		FixtureMonkey sut = builder()
			.registerByName(
				"delivered",
				Order.class,
				fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED),
				1,
				1
			)
			.registerByName(
				"cancelled",
				Order.class,
				fm -> fm.giveMeBuilder(Order.class).set("status", CANCELLED),
				2,
				1000
			)
			.build();

		// when
		Set<String> actual = statusesOf(() -> sut.giveMeOne(Order.class));

		// then
		then(actual).containsExactly(DELIVERED);
	}

	@Test
	void registeringNonPositiveWeightThrows() {
		thenThrownBy(() -> builder()
			.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class), Integer.MAX_VALUE, 0)
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessage("weight must be positive. weight: 0");
	}

	@Test
	void registeredBuilderOfSameTypeAppliesAlongsideBuilderRegisteredByName() {
		// given
		FixtureMonkey sut = builder()
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).size("tags", 2))
			.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED))
			.build();

		// when
		Order actual = sut.giveMeOne(Order.class);

		// then
		then(actual.getStatus()).isEqualTo(DELIVERED);
		then(actual.getTags()).hasSize(2);
	}

	@Test
	void registeringSameNameTwiceThrows() {
		thenThrownBy(() -> builder()
			.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED))
			.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", CANCELLED))
			.build()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessage("Duplicated ArbitraryBuilder name: delivered");
	}

	@Test
	void registeringNameOfBuilderRegisteredByNameAgainWithNamedMatcherThrows() {
		thenThrownBy(() -> withOrderVariants()
			.register(
				new MatcherOperator<>(
					new NamedMatcher(new AssignableTypeMatcher(Order.class), "delivered"),
					fm -> fm.giveMeBuilder(Order.class).set("status", "RETURNED")
				)
			)
			.build()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessage("Duplicated ArbitraryBuilder name: delivered");
	}

	@Test
	void selectedNameIsAlwaysUsed() {
		// given
		FixtureMonkey sut = withOrderVariants().build();

		// when
		Set<String> actual = statusesOf(
			() -> sut.giveMeExperimentalBuilder(Order.class).selectName("cancelled").sample()
		);

		// then
		then(actual).containsExactly(CANCELLED);
	}

	@Test
	void selectedNameAppliesToEverySampleOfSampleList() {
		// given
		FixtureMonkey sut = withOrderVariants().build();

		// when
		List<Order> actual = sut.giveMeExperimentalBuilder(Order.class)
			.selectName("cancelled")
			.sampleList(SAMPLE_COUNT);

		// then
		then(actual).hasSize(SAMPLE_COUNT).allSatisfy(it -> then(it.getStatus()).isEqualTo(CANCELLED));
	}

	@Test
	void selectedNameAppliesToSampledTypeWithoutProperties() {
		// given
		FixtureMonkey sut = withSimpleObjectAndStringNames().build();

		// when
		String actual = sut.giveMeExperimentalBuilder(String.class)
			.selectName("string")
			.sample();

		// then
		then(actual).isEqualTo("string");
	}

	@Test
	void otherBuildersRegisteredByNameForSameTypeAreLeftOutWhenOneIsSelected() {
		// given
		FixtureMonkey sut = builder()
			.registerByName(
				"delivered",
				Order.class,
				fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED).set("memo", "delivered")
			)
			.registerByName("cancelled", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", CANCELLED))
			.build();

		// when
		Set<String> actual = IntStream.range(0, SAMPLE_COUNT)
			.mapToObj(it -> sut.giveMeExperimentalBuilder(Order.class).selectName("cancelled").sample())
			.map(Order::getMemo)
			.collect(Collectors.toSet());

		// then
		then(actual).doesNotContain("delivered");
	}

	@Test
	void selectedNameAppliesToEveryPropertyOfItsType() {
		// given
		FixtureMonkey sut = withOrderVariants().build();

		// when
		Customer actual = sut.giveMeExperimentalBuilder(Customer.class)
			.selectName("cancelled")
			.size("orders", 3)
			.sample();

		// then
		then(actual.getOrder().getStatus()).isEqualTo(CANCELLED);
		then(actual.getOrders()).allSatisfy(it -> then(it.getStatus()).isEqualTo(CANCELLED));
	}

	@Test
	void laterSelectedNameWinsAtSameProperty() {
		// given
		FixtureMonkey sut = withOrderVariants().build();

		// when
		Set<String> actual = statusesOf(
			() -> sut.giveMeExperimentalBuilder(Order.class).selectName("cancelled", "delivered").sample()
		);

		// then
		then(actual).containsExactly(DELIVERED);
	}

	@Test
	void chainedSelectNameIsSameAsSelectingNamesTogether() {
		// given
		FixtureMonkey sut = withOrderVariants().build();

		// when
		Set<String> actual = statusesOf(
			() -> sut.giveMeExperimentalBuilder(Order.class)
				.selectName("cancelled")
				.selectName("delivered")
				.sample()
		);

		// then
		then(actual).containsExactly(DELIVERED);
	}

	@Test
	void selectingNameAgainMakesItTheLatestSelected() {
		// given
		FixtureMonkey sut = withOrderVariants().build();

		// when
		Set<String> actual = statusesOf(
			() -> sut.giveMeExperimentalBuilder(Order.class)
				.selectName("cancelled", "delivered")
				.selectName("cancelled")
				.sample()
		);

		// then
		then(actual).containsExactly(CANCELLED);
	}

	@Test
	void registeredBuilderDeclaredLaterWinsOverUnselectedBuilderRegisteredByName() {
		// given
		FixtureMonkey sut = builder()
			.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED))
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", "REGISTERED"))
			.build();

		// when
		Set<String> actual = statusesOf(() -> sut.giveMeOne(Order.class));

		// then
		then(actual).containsExactly("REGISTERED");
	}

	@Test
	void selectedNameWinsOverRegisteredBuilderOfSameType() {
		// given
		FixtureMonkey sut = builder()
			.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED))
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", "REGISTERED"), 0)
			.build();

		// when
		Set<String> actual = statusesOf(
			() -> sut.giveMeExperimentalBuilder(Order.class).selectName("delivered").sample()
		);

		// then
		then(actual).containsExactly(DELIVERED);
	}

	@Test
	void userSetWinsOverSelectedNameDeclaredAfterIt() {
		// given
		FixtureMonkey sut = withOrderVariants().build();
		ExperimentalArbitraryBuilder<Order> builder = sut.giveMeExperimentalBuilder(Order.class);
		builder.set("status", "DIRECT");

		// when
		Order actual = builder.selectName("cancelled").sample();

		// then
		then(actual.getStatus()).isEqualTo("DIRECT");
	}

	@Test
	void userSetWinsOverSelectedNameDeclaredBeforeIt() {
		// given
		FixtureMonkey sut = withOrderVariants().build();

		// when
		Order actual = sut.giveMeExperimentalBuilder(Order.class)
			.selectName("cancelled")
			.set("status", "DIRECT")
			.sample();

		// then
		then(actual.getStatus()).isEqualTo("DIRECT");
	}

	@Test
	void laterSelectedOuterTypeWinsOverSelectedPropertyType() {
		// given
		FixtureMonkey sut = withSimpleObjectAndStringNames().build();

		// when
		String actual = sut.giveMeExperimentalBuilder(SimpleObject.class)
			.selectName("string", "simpleObject")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("simpleObject");
	}

	@Test
	void laterSelectedPropertyTypeWinsOverSelectedOuterType() {
		// given
		FixtureMonkey sut = withSimpleObjectAndStringNames().build();

		// when
		String actual = sut.giveMeExperimentalBuilder(SimpleObject.class)
			.selectName("simpleObject", "string")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("string");
	}

	@Test
	void laterSelectedPropertyTypeWinsOverWholeValueOfSelectedOuterType() {
		// given
		FixtureMonkey sut = withWholeSimpleObjectAndStringNames().build();

		// when
		String actual = sut.giveMeExperimentalBuilder(SimpleObject.class)
			.selectName("simpleObject", "string")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("string");
	}

	@Test
	void laterSelectedWholeValueOfOuterTypeWinsOverSelectedPropertyType() {
		// given
		FixtureMonkey sut = withWholeSimpleObjectAndStringNames().build();

		// when
		String actual = sut.giveMeExperimentalBuilder(SimpleObject.class)
			.selectName("string", "simpleObject")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("simpleObject");
	}

	@Test
	void selectedPropertyTypeWinsOverRegisteredOuterType() {
		// given
		FixtureMonkey sut = builder()
			.register(SimpleObject.class, fm -> fm.giveMeBuilder(SimpleObject.class).set("str", "registered"))
			.registerByName("string", String.class, fm -> fm.giveMeBuilder("string"))
			.build();

		// when
		String actual = sut.giveMeExperimentalBuilder(SimpleObject.class)
			.selectName("string")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("string");
	}

	@Test
	void selectedPropertyTypeWinsOverWholeValueOfRegisteredOuterType() {
		// given
		FixtureMonkey sut = builder()
			.register(SimpleObject.class, fm -> fm.giveMeBuilder(simpleObject("registered")))
			.registerByName("string", String.class, fm -> fm.giveMeBuilder("string"))
			.build();

		// when
		String actual = sut.giveMeExperimentalBuilder(SimpleObject.class)
			.selectName("string")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("string");
	}

	@Test
	void laterSelectedPropertyTypeWinsOverNullOfSelectedOuterType() {
		// given
		FixtureMonkey sut = withOrderVariants()
			.registerByName("withoutOrder", Customer.class, fm -> fm.giveMeBuilder(Customer.class).setNull("order"))
			.build();

		// when
		Order actual = sut.giveMeExperimentalBuilder(Customer.class)
			.selectName("withoutOrder", "cancelled")
			.sample()
			.getOrder();

		// then
		then(actual.getStatus()).isEqualTo(CANCELLED);
	}

	@Test
	void laterSelectedNullOfOuterTypeWinsOverSelectedPropertyType() {
		// given
		FixtureMonkey sut = withOrderVariants()
			.registerByName("withoutOrder", Customer.class, fm -> fm.giveMeBuilder(Customer.class).setNull("order"))
			.build();

		// when
		Order actual = sut.giveMeExperimentalBuilder(Customer.class)
			.selectName("cancelled", "withoutOrder")
			.sample()
			.getOrder();

		// then
		then(actual).isNull();
	}

	@Test
	void laterSelectedOuterTypeSizeWinsOverSelectedPropertyTypeSize() {
		// given
		FixtureMonkey sut = builder()
			.registerByName("twoTags", Order.class, fm -> fm.giveMeBuilder(Order.class).size("tags", 2))
			.registerByName("fiveTags", Customer.class, fm -> fm.giveMeBuilder(Customer.class).size("order.tags", 5))
			.build();

		// when
		List<String> actual = sut.giveMeExperimentalBuilder(Customer.class)
			.selectName("twoTags", "fiveTags")
			.sample()
			.getOrder()
			.getTags();

		// then
		then(actual).hasSize(5);
	}

	@Test
	void selectedOuterTypeSizeWinsOverRegisteredPropertyTypeSize() {
		// given
		FixtureMonkey sut = builder()
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).size("tags", 2))
			.registerByName("fiveTags", Customer.class, fm -> fm.giveMeBuilder(Customer.class).size("order.tags", 5))
			.build();

		// when
		List<String> actual = sut.giveMeExperimentalBuilder(Customer.class)
			.selectName("fiveTags")
			.sample()
			.getOrder()
			.getTags();

		// then
		then(actual).hasSize(5);
	}

	@Test
	void userWildcardSizeWinsOverSizeOfSelectedSampledType() {
		// given
		FixtureMonkey sut = builder()
			.registerByName(
				"fiveTags",
				Customer.class,
				fm -> fm.giveMeBuilder(Customer.class).size("orders[*].tags", 5)
			)
			.build();

		// when
		List<Order> actual = sut.giveMeExperimentalBuilder(Customer.class)
			.selectName("fiveTags")
			.size("orders", 2)
			.size("orders[*].tags", 1)
			.sample()
			.getOrders();

		// then
		then(actual).allSatisfy(it -> then(it.getTags()).hasSize(1));
	}

	@Test
	void laterSelectedPropertyTypeCustomizesValueOfSelectedOuterType() {
		// given
		FixtureMonkey sut = withSimpleObjectAndStringNames()
			.registerByName(
				"bang",
				String.class,
				fm -> fm.giveMeBuilder(String.class).customizeProperty(typedString("$"), it -> it.map(str -> str + "!"))
			)
			.build();

		// when
		String actual = sut.giveMeExperimentalBuilder(SimpleObject.class)
			.selectName("simpleObject", "bang")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("simpleObject!");
	}

	@Test
	void laterSelectedPropertyTypeInstantiateWinsOverInstantiateOfSelectedOuterType() {
		// given
		FixtureMonkey sut = withLabeledInstantiateNames().build();

		// when
		String actual = sut.giveMeExperimentalBuilder(LabeledHolder.class)
			.selectName("stringInHolder", "longLabeled")
			.sample()
			.getLabeled()
			.getOrigin();

		// then
		then(actual).isEqualTo("long-constructor");
	}

	@Test
	void laterSelectedOuterTypeInstantiateWinsOverInstantiateOfSelectedPropertyType() {
		// given
		FixtureMonkey sut = withLabeledInstantiateNames().build();

		// when
		String actual = sut.giveMeExperimentalBuilder(LabeledHolder.class)
			.selectName("longLabeled", "stringInHolder")
			.sample()
			.getLabeled()
			.getOrigin();

		// then
		then(actual).isEqualTo("string-constructor");
	}

	@Test
	void selectedPropertyTypeInstantiateWinsOverInstantiateOfRegisteredOuterType() {
		// given
		FixtureMonkey sut = builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.register(
				LabeledHolder.class,
				fm -> fm.giveMeBuilder(LabeledHolder.class)
					.instantiate(Labeled.class, constructor().parameter(String.class))
			)
			.registerByName(
				"longLabeled",
				Labeled.class,
				fm -> fm.giveMeBuilder(Labeled.class).instantiate(Labeled.class, constructor().parameter(long.class))
			)
			.build();

		// when
		String actual = sut.giveMeExperimentalBuilder(LabeledHolder.class)
			.selectName("longLabeled")
			.sample()
			.getLabeled()
			.getOrigin();

		// then
		then(actual).isEqualTo("long-constructor");
	}

	@Test
	void selectedPropertyTypeAppliesWhereSelectedOuterTypeDeclaresNothing() {
		// given
		FixtureMonkey sut = withSimpleObjectAndStringNames().build();

		// when
		String actual = sut.giveMeExperimentalBuilder(SimpleObject.class)
			.selectName("simpleObject", "string")
			.sample()
			.getOther();

		// then
		then(actual).isEqualTo("string");
	}

	@Test
	void selectedNameOfTypeNotInSampleIsIgnored() {
		// given
		FixtureMonkey sut = withOrderVariants().build();

		// when
		SimpleObject actual = sut.giveMeExperimentalBuilder(SimpleObject.class)
			.selectName("cancelled")
			.sample();

		// then
		then(actual).isNotNull();
	}

	@Test
	void selectingUnregisteredNameThrows() {
		// given
		FixtureMonkey sut = withOrderVariants().build();

		// when
		ExperimentalArbitraryBuilder<Order> builder = sut.giveMeExperimentalBuilder(Order.class)
			.selectName("unknown");

		// then
		thenThrownBy(builder::sample)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessage("Given name is not registered. name: unknown");
	}

	@Test
	void selectingNameAfterFixedSampleThrows() {
		// given
		FixtureMonkey sut = withOrderVariants().build();
		ExperimentalArbitraryBuilder<Order> builder = sut.giveMeExperimentalBuilder(Order.class);
		builder.fixed();
		builder.sample();

		// when, then
		thenThrownBy(() -> builder.selectName("cancelled"))
			.isExactlyInstanceOf(IllegalStateException.class)
			.hasMessage(
				"selectName has no effect once a value is set to the whole object, "
					+ "such as by set(\"$\", value), a sampled fixed() or thenApply()"
			);
	}

	@Test
	void selectingNameAfterThenApplyThrows() {
		// given
		FixtureMonkey sut = withOrderVariants().build();
		ExperimentalArbitraryBuilder<Order> builder = sut.giveMeExperimentalBuilder(Order.class);
		builder.thenApply((order, it) -> it.set("memo", order.getStatus()));

		// when, then
		thenThrownBy(() -> builder.selectName("cancelled"))
			.isExactlyInstanceOf(IllegalStateException.class);
	}

	@Test
	void nameSelectedBeforeThenApplyIsUsedForItsValue() {
		// given
		FixtureMonkey sut = withOrderVariants().build();

		// when
		Set<String> actual = IntStream.range(0, SAMPLE_COUNT)
			.mapToObj(it -> {
				ExperimentalArbitraryBuilder<Order> builder = sut.giveMeExperimentalBuilder(Order.class);
				builder.selectName("cancelled");
				return builder.thenApply((order, applied) -> applied.set("memo", order.getStatus())).sample();
			})
			.map(Order::getMemo)
			.collect(Collectors.toSet());

		// then
		then(actual).containsExactly(CANCELLED);
	}

	@Test
	void selectedNameWinsOverRegisteredBuilderWithNegativePriority() {
		// given
		FixtureMonkey sut = builder()
			.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED))
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", "REGISTERED"), -100)
			.build();

		// when
		Set<String> actual = statusesOf(
			() -> sut.giveMeExperimentalBuilder(Order.class).selectName("delivered").sample()
		);

		// then
		then(actual).containsExactly(DELIVERED);
	}

	@Test
	void selectedNameWinsOverRegisteredBuilderWithLowestPossiblePriority() {
		// given
		FixtureMonkey sut = withOrderVariants()
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", "REGISTERED"), Integer.MIN_VALUE)
			.build();

		// when
		Set<String> actual = statusesOf(
			() -> sut.giveMeExperimentalBuilder(Customer.class).selectName("delivered").sample().getOrder()
		);

		// then
		then(actual).containsExactly(DELIVERED);
	}

	@Test
	void laterSelectedNameWinsWhenRegisteredPriorityIsNearLowestPossible() {
		// given
		FixtureMonkey sut = withOrderVariants()
			.register(
				Order.class,
				fm -> fm.giveMeBuilder(Order.class).set("status", "REGISTERED"),
				Integer.MIN_VALUE + 1
			)
			.build();

		// when
		Set<String> actual = statusesOf(
			() -> sut.giveMeExperimentalBuilder(Order.class).selectName("cancelled", "delivered").sample()
		);

		// then
		then(actual).containsExactly(DELIVERED);
	}

	@Test
	void selectedPropertyTypeSizeAppliesInsideValueOfRegisteredOuterType() {
		// given
		Order order = new Order();
		order.setTags(Collections.singletonList("registered"));
		FixtureMonkey sut = builder()
			.register(Customer.class, fm -> fm.giveMeBuilder(Customer.class).set("order", order))
			.registerByName("threeTags", Order.class, fm -> fm.giveMeBuilder(Order.class).size("tags", 3))
			.build();

		// when
		List<String> actual = sut.giveMeExperimentalBuilder(Customer.class)
			.selectName("threeTags")
			.sample()
			.getOrder()
			.getTags();

		// then
		then(actual).hasSize(3).startsWith("registered");
	}

	@Test
	void wholeValueOfRegisteredTypeIsKeptWhenSelectedTypeCannotAppearInsideIt() {
		// given
		SimpleObject registered = simpleObject("registered");
		FixtureMonkey sut = withOrderVariants()
			.register(SimpleObject.class, fm -> fm.giveMeBuilder(registered))
			.build();

		// when
		Basket actual = sut.giveMeExperimentalBuilder(Basket.class)
			.selectName("cancelled")
			.sample();

		// then
		then(actual.getSimpleObject()).isSameAs(registered);
		then(actual.getOrder().getStatus()).isEqualTo(CANCELLED);
	}

	@Test
	void selectedNullWinsOverNotNullOfRegisteredOuterType() {
		// given
		FixtureMonkey sut = builder()
			.register(Basket.class, fm -> fm.giveMeBuilder(Basket.class).setNotNull("simpleObject.str"))
			.registerByName(
				"nullStr",
				SimpleObject.class,
				fm -> fm.giveMeBuilder(new SimpleObject()).set("other", "other")
			)
			.build();

		// when
		SimpleObject actual = sut.giveMeExperimentalBuilder(Basket.class)
			.selectName("nullStr")
			.sample()
			.getSimpleObject();

		// then
		then(actual.getStr()).isNull();
		then(actual.getOther()).isEqualTo("other");
	}

	@Test
	void selectedExactTypeSizeAppliesInsideValueOfRegisteredOuterType() {
		// given
		Order order = new Order();
		order.setTags(Collections.singletonList("registered"));
		FixtureMonkey sut = builder()
			.register(Customer.class, fm -> fm.giveMeBuilder(Customer.class).set("order", order))
			.register(
				new MatcherOperator<>(
					new NamedMatcher(new ExactTypeMatcher(List.class), "threeItems"),
					fm -> fm.giveMeBuilder(new TypeReference<List<String>>() {
					}).size("$", 3)
				)
			)
			.build();

		// when
		List<String> actual = sut.giveMeExperimentalBuilder(Customer.class)
			.selectName("threeItems")
			.sample()
			.getOrder()
			.getTags();

		// then
		then(actual).hasSize(3);
	}

	@Test
	void selectingNameAfterSettingWholeObjectNullThrows() {
		// given
		FixtureMonkey sut = withOrderVariants().build();
		ExperimentalArbitraryBuilder<Order> builder = sut.giveMeExperimentalBuilder(Order.class);
		builder.setNull("$");

		// when, then
		thenThrownBy(() -> builder.selectName("cancelled"))
			.isExactlyInstanceOf(IllegalStateException.class);
	}

	@Test
	void selectingNameAfterSettingWholeValueThrows() {
		// given
		FixtureMonkey sut = withOrderVariants().build();
		ExperimentalArbitraryBuilder<Order> builder = sut.giveMeExperimentalBuilder(Order.class);
		builder.set("$", new Order());

		// when, then
		thenThrownBy(() -> builder.selectName("cancelled"))
			.isExactlyInstanceOf(IllegalStateException.class);
	}

	@Test
	void selectingNameInRegisteredBuilderThrows() {
		thenThrownBy(() -> withOrderVariants()
			.register(Customer.class, fm -> fm.giveMeExperimentalBuilder(Customer.class).selectName("cancelled"))
			.build()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessage("selectName is not supported in a registered ArbitraryBuilder. names: [cancelled]");
	}

	@Test
	void fixedBuilderKeepsBuilderRegisteredByNameItChose() {
		// given
		FixtureMonkey sut = withOrderVariants().build();
		ArbitraryBuilder<Order> builder = sut.giveMeBuilder(Order.class).fixed();

		// when
		Set<String> actual = statusesOf(builder::sample);

		// then
		then(actual).hasSize(1);
	}

	private static FixtureMonkeyBuilder builder() {
		return FixtureMonkey.builder()
			.defaultNotNull(true);
	}

	private static FixtureMonkeyBuilder withOrderVariants() {
		return builder()
			.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", DELIVERED))
			.registerByName("cancelled", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", CANCELLED));
	}

	private static FixtureMonkeyBuilder withSimpleObjectAndStringNames() {
		return builder()
			.registerByName(
				"simpleObject",
				SimpleObject.class,
				fm -> fm.giveMeBuilder(SimpleObject.class).set("str", "simpleObject")
			)
			.registerByName("string", String.class, fm -> fm.giveMeBuilder("string"));
	}

	private static FixtureMonkeyBuilder withWholeSimpleObjectAndStringNames() {
		return builder()
			.registerByName("simpleObject", SimpleObject.class, fm -> fm.giveMeBuilder(simpleObject("simpleObject")))
			.registerByName("string", String.class, fm -> fm.giveMeBuilder("string"));
	}

	private static FixtureMonkeyBuilder withLabeledInstantiateNames() {
		return builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.registerByName(
				"stringInHolder",
				LabeledHolder.class,
				fm -> fm.giveMeBuilder(LabeledHolder.class)
					.instantiate(Labeled.class, constructor().parameter(String.class))
			)
			.registerByName(
				"longLabeled",
				Labeled.class,
				fm -> fm.giveMeBuilder(Labeled.class).instantiate(Labeled.class, constructor().parameter(long.class))
			);
	}

	private static SimpleObject simpleObject(String value) {
		SimpleObject simpleObject = new SimpleObject();
		simpleObject.setStr(value);
		simpleObject.setOther(value);
		return simpleObject;
	}

	private static Set<String> statusesOf(Supplier<Order> sampler) {
		return IntStream.range(0, SAMPLE_COUNT)
			.mapToObj(it -> sampler.get().getStatus())
			.collect(Collectors.toSet());
	}

	private static List<String> statusesWithSeed(long seed) {
		FixtureMonkey sut = withOrderVariants()
			.seed(seed)
			.build();
		return IntStream.range(0, SAMPLE_COUNT)
			.mapToObj(it -> sut.giveMeOne(Order.class).getStatus())
			.collect(Collectors.toList());
	}

	@Data
	public static class Order {
		private String status;
		private String memo;
		private List<String> tags;
	}

	@Data
	public static class Customer {
		private Order order;
		private List<Order> orders;
	}

	@Data
	public static class SimpleObject {
		private String str;
		private String other;
	}

	@Data
	public static class Basket {
		private SimpleObject simpleObject;
		private Order order;
	}

	public static class Labeled {
		private final String origin;

		@ConstructorProperties("number")
		public Labeled(int number) {
			this.origin = "int-constructor";
		}

		public Labeled(String text) {
			this.origin = "string-constructor";
		}

		public Labeled(long number) {
			this.origin = "long-constructor";
		}

		public String getOrigin() {
			return origin;
		}
	}

	public static class LabeledHolder {
		private final Labeled labeled;

		@ConstructorProperties("labeled")
		public LabeledHolder(Labeled labeled) {
			this.labeled = labeled;
		}

		public Labeled getLabeled() {
			return labeled;
		}
	}
}
