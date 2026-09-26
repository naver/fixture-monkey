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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.Data;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonTypeInfoIdName;

class SeedDeterminismTest {
	private static final long SEED = 20260925L;
	private static final int SAMPLE_SIZE = 20;

	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
		.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
		.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
		.build();

	private static final Map<String, Function<FixtureMonkey, Object>> SCENARIOS = scenarios();

	@Test
	void sameSeedGivesSameResultAcrossInstances() {
		// given
		Map<String, String> first = dumpEachWithNewInstance(SEED);

		// when
		Map<String, String> second = dumpEachWithNewInstance(SEED);

		// then
		then(mismatchedScenarios(first, second)).isEmpty();
	}

	@Test
	void sameSeedGivesSameResultWhenCachesAreWarm() {
		// given
		Map<String, String> cold = new LinkedHashMap<>();
		Map<String, String> warm = new LinkedHashMap<>();

		// when
		SCENARIOS.forEach((name, scenario) -> {
			FixtureMonkey fixtureMonkey = newFixtureMonkey(SEED);
			Randoms.newGlobalSeed(SEED);
			cold.put(name, dump(scenario.apply(fixtureMonkey)));
			Randoms.newGlobalSeed(SEED);
			warm.put(name, dump(scenario.apply(fixtureMonkey)));
		});

		// then
		then(mismatchedScenarios(cold, warm)).isEmpty();
	}

	@Test
	void sizeDecisionDoesNotDependOnOtherDecisions() {
		// given
		List<String> plain = newNotNullFixtureMonkey(SEED).giveMeBuilder(Order.class)
			.sampleList(SAMPLE_SIZE)
			.stream()
			.map(SeedDeterminismTest::shapeExceptItems)
			.collect(toList());

		// when
		List<String> itemsSized = newNotNullFixtureMonkey(SEED).giveMeBuilder(Order.class)
			.size("items", 2)
			.sampleList(SAMPLE_SIZE)
			.stream()
			.map(SeedDeterminismTest::shapeExceptItems)
			.collect(toList());

		// then
		then(itemsSized).isEqualTo(plain);
	}

	@Test
	void explicitSizeRangeIsDeterministic() {
		// given
		List<Integer> first = itemSizes(newNotNullFixtureMonkey(SEED));

		// when
		List<Integer> second = itemSizes(newNotNullFixtureMonkey(SEED));

		// then
		then(second).isEqualTo(first);
	}

	@Test
	void builderResultDoesNotDependOnOtherBuildersSampling() {
		// given
		List<String> alone = shapesOfBuilderB(0);

		// when
		List<String> afterOtherBuilderSampled = shapesOfBuilderB(5);

		// then
		then(afterOtherBuilderSampled).isEqualTo(alone);
	}

	@Test
	void valuesDoNotDependOnOtherBuildersSampling() {
		// given
		String alone = valuesOfBuilderB(0);

		// when
		String afterOtherBuilderSampled = valuesOfBuilderB(5);

		// then
		then(afterOtherBuilderSampled).isEqualTo(alone);
	}

	@Test
	void valuesDoNotDependOnOtherFieldsBeingSet() {
		// given
		List<String> plain = newFixtureMonkey(SEED).giveMeBuilder(Profile.class)
			.sampleList(SAMPLE_SIZE)
			.stream()
			.map(profile -> profile.getName() + profile.getTags())
			.collect(toList());

		// when
		List<String> countSet = newFixtureMonkey(SEED).giveMeBuilder(Profile.class)
			.set("count", 7)
			.sampleList(SAMPLE_SIZE)
			.stream()
			.map(profile -> profile.getName() + profile.getTags())
			.collect(toList());

		// then
		then(countSet).isEqualTo(plain);
	}

	@Test
	void valuesOfEveryKindDoNotDependOnOtherBuildersSampling() {
		// given
		String alone = orderValuesOfBuilderB(0);

		// when
		String afterOtherBuilderSampled = orderValuesOfBuilderB(5);

		// then
		then(afterOtherBuilderSampled).isEqualTo(alone);
	}

	@Test
	void fixedSizesDoNotDependOnOtherBuildersBeingFixed() {
		// given
		List<Integer> alone = fixedSizesOfBuilderB(false);

		// when
		List<Integer> afterOtherBuilderFixed = fixedSizesOfBuilderB(true);

		// then
		then(afterOtherBuilderFixed).isEqualTo(alone);
	}

	@Test
	void thenApplySizesDoNotDependOnOtherBuildersThenApply() {
		// given
		List<Integer> alone = thenApplySizesOfBuilderB(false);

		// when
		List<Integer> afterOtherBuilderThenApply = thenApplySizesOfBuilderB(true);

		// then
		then(afterOtherBuilderThenApply).isEqualTo(alone);
	}

	@Test
	void jsonSubTypeChoiceDoesNotDependOnOtherBuildersSampling() {
		// given
		List<String> alone = jsonSubTypesOfBuilderB(0);

		// when
		List<String> afterOtherBuilderSampled = jsonSubTypesOfBuilderB(5);

		// then
		then(afterOtherBuilderSampled).isEqualTo(alone);
	}

	private static List<Integer> fixedSizesOfBuilderB(boolean fixOtherBuilderFirst) {
		FixtureMonkey fixtureMonkey = newNotNullFixtureMonkey(SEED);
		ArbitraryBuilder<Order> builderA = sizedOrderBuilder(fixtureMonkey);
		ArbitraryBuilder<Order> builderB = sizedOrderBuilder(fixtureMonkey);
		if (fixOtherBuilderFirst) {
			builderA.fixed();
		}
		return containerSizes(builderB.fixed().sample());
	}

	private static List<Integer> thenApplySizesOfBuilderB(boolean thenApplyOtherBuilderFirst) {
		FixtureMonkey fixtureMonkey = newNotNullFixtureMonkey(SEED);
		ArbitraryBuilder<Order> builderA = sizedOrderBuilder(fixtureMonkey);
		ArbitraryBuilder<Order> builderB = sizedOrderBuilder(fixtureMonkey);
		if (thenApplyOtherBuilderFirst) {
			builderA.thenApply((order, builder) -> builder.set("count", 1));
		}
		return containerSizes(builderB.thenApply((order, builder) -> builder.set("count", 1)).sample());
	}

	private static ArbitraryBuilder<Order> sizedOrderBuilder(FixtureMonkey fixtureMonkey) {
		return fixtureMonkey.giveMeBuilder(Order.class)
			.size("items", 0, 30)
			.size("tags", 0, 30)
			.size("stock", 0, 30)
			.size("matrix", 0, 30);
	}

	private static List<Integer> containerSizes(Order order) {
		return Arrays.asList(
			order.getItems().size(),
			order.getTags().size(),
			order.getStock().size(),
			order.getMatrix().size()
		);
	}

	private static List<String> jsonSubTypesOfBuilderB(int builderASampleCount) {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.seed(SEED)
			.plugin(new JacksonPlugin())
			.defaultNotNull(true)
			.build();
		ArbitraryBuilder<JsonTypeInfoIdName> builderA = fixtureMonkey.giveMeBuilder(JsonTypeInfoIdName.class);
		ArbitraryBuilder<JsonTypeInfoIdName> builderB = fixtureMonkey.giveMeBuilder(JsonTypeInfoIdName.class);
		builderA.sampleList(builderASampleCount);
		return builderB.sampleList(SAMPLE_SIZE)
			.stream()
			.map(value -> value.getType().getClass().getSimpleName())
			.collect(toList());
	}

	private static String orderValuesOfBuilderB(int builderASampleCount) {
		FixtureMonkey fixtureMonkey = newFixtureMonkey(SEED);
		ArbitraryBuilder<Order> builderA = fixtureMonkey.giveMeBuilder(Order.class);
		ArbitraryBuilder<Order> builderB = fixtureMonkey.giveMeBuilder(Order.class)
			.set("name", Arbitraries.strings().alpha().ofLength(8));
		builderA.sampleList(builderASampleCount);

		return dump(builderB.sampleList(SAMPLE_SIZE));
	}

	private static String valuesOfBuilderB(int builderASampleCount) {
		FixtureMonkey fixtureMonkey = newFixtureMonkey(SEED);
		ArbitraryBuilder<Profile> builderA = fixtureMonkey.giveMeBuilder(Profile.class);
		ArbitraryBuilder<Profile> builderB = fixtureMonkey.giveMeBuilder(Profile.class);
		builderA.sampleList(builderASampleCount);

		return dump(builderB.sampleList(SAMPLE_SIZE));
	}

	private static List<String> shapesOfBuilderB(int builderASampleCount) {
		FixtureMonkey fixtureMonkey = newNotNullFixtureMonkey(SEED);
		ArbitraryBuilder<Order> builderA = fixtureMonkey.giveMeBuilder(Order.class)
			.setLazy("name", () -> fixtureMonkey.giveMeBuilder(Item.class).sample().getCode())
			.thenApply((order, builder) -> builder.set("count", order.getItems().size()));
		ArbitraryBuilder<Zoo> builderB = fixtureMonkey.giveMeBuilder(Zoo.class);
		builderA.sampleList(builderASampleCount);

		return builderB.sampleList(SAMPLE_SIZE).stream().map(SeedDeterminismTest::shapeOf).collect(toList());
	}

	private static List<Integer> itemSizes(FixtureMonkey fixtureMonkey) {
		return fixtureMonkey.giveMeBuilder(Order.class)
			.size("items", 1, 3)
			.sampleList(SAMPLE_SIZE)
			.stream()
			.map(order -> order.getItems().size())
			.collect(toList());
	}

	private static String shapeExceptItems(Order order) {
		List<Integer> rowSizes = order.getMatrix().stream().map(List::size).collect(toList());
		return "tags=" + order.getTags().size() + ",stock=" + order.getStock().size() + ",matrix=" + rowSizes;
	}

	private static String shapeOf(Zoo zoo) {
		List<String> animalTypes = zoo.getAnimals()
			.stream()
			.map(animal -> animal.getClass().getSimpleName())
			.collect(toList());
		return zoo.getAnimal().getClass().getSimpleName() + animalTypes;
	}

	private static FixtureMonkey newNotNullFixtureMonkey(long seed) {
		return FixtureMonkey.builder()
			.seed(seed)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Animal.class, Arrays.asList(Dog.class, Cat.class)))
			.build();
	}

	private static FixtureMonkey newFixtureMonkey(long seed) {
		return FixtureMonkey.builder()
			.seed(seed)
			.plugin(new InterfacePlugin().interfaceImplements(Animal.class, Arrays.asList(Dog.class, Cat.class)))
			.register(Item.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(Item.class).size("labels", 2))
			.build();
	}

	private static Map<String, Function<FixtureMonkey, Object>> scenarios() {
		Map<String, Function<FixtureMonkey, Object>> scenarios = new LinkedHashMap<>();
		scenarios.put("primitivesAndContainers", fixtureMonkey -> fixtureMonkey.giveMe(Order.class, SAMPLE_SIZE));
		scenarios.put("interfaceField", fixtureMonkey -> fixtureMonkey.giveMe(Zoo.class, SAMPLE_SIZE));
		scenarios.put("abstractRoot", fixtureMonkey -> fixtureMonkey.giveMe(Animal.class, SAMPLE_SIZE));
		scenarios.put(
			"generic",
			fixtureMonkey -> fixtureMonkey.giveMe(new TypeReference<GenericBox<String>>() {
			}, SAMPLE_SIZE)
		);
		scenarios.put(
			"customized",
			fixtureMonkey -> fixtureMonkey.giveMeBuilder(Order.class)
				.size("items", 1, 3)
				.set("name", "fixed")
				.thenApply((order, builder) -> builder.set("count", order.getItems().size()))
				.sampleList(SAMPLE_SIZE)
		);
		return scenarios;
	}

	private static Map<String, String> dumpEachWithNewInstance(long seed) {
		Map<String, String> dumps = new LinkedHashMap<>();
		SCENARIOS.forEach((name, scenario) -> dumps.put(name, dump(scenario.apply(newFixtureMonkey(seed)))));
		return dumps;
	}

	private static String dump(Object value) {
		try {
			return OBJECT_MAPPER.writeValueAsString(value);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static List<String> mismatchedScenarios(Map<String, String> expected, Map<String, String> actual) {
		List<String> mismatched = new ArrayList<>();
		expected.forEach((name, dump) -> {
			if (!dump.equals(actual.get(name))) {
				mismatched.add(name);
			}
		});
		return mismatched;
	}

	public enum Grade {
		BRONZE, SILVER, GOLD
	}

	@Data
	public static class Order {
		private String name;
		private int count;
		private long amount;
		private double rate;
		private boolean paid;
		private Grade grade;
		private List<Item> items;
		private Set<String> tags;
		private Map<String, Integer> stock;
		private List<List<String>> matrix;
	}

	@Data
	public static class Profile {
		private String name;
		private int count;
		private List<String> tags;
	}

	@Data
	public static class Item {
		private String code;
		private int quantity;
		private List<String> labels;
	}

	public interface Animal {
		String getName();
	}

	@Data
	public static class Dog implements Animal {
		private String name;
		private int barkVolume;
	}

	@Data
	public static class Cat implements Animal {
		private String name;
		private int lives;
	}

	@Data
	public static class Zoo {
		private Animal animal;
		private List<Animal> animals;
	}

	@Data
	public static class GenericBox<T> {
		private T value;
		private List<T> values;
	}
}
