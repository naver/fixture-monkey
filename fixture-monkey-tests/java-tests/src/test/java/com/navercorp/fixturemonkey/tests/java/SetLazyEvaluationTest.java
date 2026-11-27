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

import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class SetLazyEvaluationTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void setLazyIsCalledOncePerSample() {
		// given
		AtomicInteger sequence = new AtomicInteger();
		ArbitraryBuilder<NumberHolder> builder = SUT.giveMeBuilder(NumberHolder.class)
			.setLazy("number", sequence::incrementAndGet);

		// when
		List<Integer> actual = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			actual.add(builder.sample().number);
		}

		// then
		then(actual).containsExactly(1, 2, 3);
	}

	@Test
	void setLazyIsCalledOncePerSampleOfSampleList() {
		// given
		AtomicInteger sequence = new AtomicInteger();

		// when
		List<Integer> actual = SUT.giveMeBuilder(NumberHolder.class)
			.setLazy("number", sequence::incrementAndGet)
			.sampleList(3)
			.stream()
			.map(it -> it.number)
			.collect(Collectors.toList());

		// then
		then(actual).containsExactly(1, 2, 3);
	}

	@Test
	void setLazyIsCalledOncePerSampleInRegisteredBuilder() {
		// given
		AtomicInteger sequence = new AtomicInteger();
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(NumberHolder.class, fm -> fm.giveMeBuilder(NumberHolder.class)
				.setLazy("number", sequence::incrementAndGet))
			.build();

		// when
		List<Integer> actual = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			actual.add(sut.giveMeOne(NumberHolderParent.class).holder.number);
		}

		// then
		then(actual).containsExactly(1, 2, 3);
	}

	@Test
	void setLazyForEachElementIsCalledPerElement() {
		// given
		AtomicInteger sequence = new AtomicInteger();
		ArbitraryBuilder<NumbersHolder> builder = SUT.giveMeBuilder(NumbersHolder.class)
			.size("numbers", 3)
			.setLazy("numbers[*]", sequence::incrementAndGet);

		// when
		List<List<Integer>> actual = Arrays.asList(builder.sample().numbers, builder.sample().numbers);

		// then
		then(actual).containsExactly(Arrays.asList(1, 2, 3), Arrays.asList(4, 5, 6));
	}

	@Test
	void setLazyForEachElementIsCalledPerElementInRegisteredBuilder() {
		// given
		AtomicInteger sequence = new AtomicInteger();
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(NumbersHolder.class, fm -> fm.giveMeBuilder(NumbersHolder.class)
				.size("numbers", 3)
				.setLazy("numbers[*]", sequence::incrementAndGet))
			.build();

		// when
		List<List<Integer>> actual = Arrays.asList(
			sut.giveMeOne(NumbersHolderParent.class).holder.numbers,
			sut.giveMeOne(NumbersHolderParent.class).holder.numbers
		);

		// then
		then(actual).containsExactly(Arrays.asList(1, 2, 3), Arrays.asList(4, 5, 6));
	}

	@Test
	void setLazyForEachElementObjectIsCalledPerElement() {
		// given
		AtomicInteger sequence = new AtomicInteger();
		ArbitraryBuilder<NumberHoldersHolder> builder = SUT.giveMeBuilder(NumberHoldersHolder.class)
			.size("holders", 3)
			.setLazy("holders[*]", () -> new NumberHolder(sequence.incrementAndGet()));

		// when
		List<Integer> actual = builder.sample().holders.stream().map(it -> it.number).collect(Collectors.toList());

		// then
		then(actual).containsExactly(1, 2, 3);
	}

	@Test
	void setLazyForEachInterfaceElementIsCalledOncePerElement() {
		// given
		AtomicInteger sequence = new AtomicInteger();
		ArbitraryBuilder<NumberedHolder> builder = SUT.giveMeBuilder(NumberedHolder.class)
			.size("numbereds", 3)
			.setLazy("numbereds[*]", () -> new NumberedImpl(sequence.incrementAndGet()));

		// when
		List<Integer> actual = builder.sample().numbereds.stream()
			.map(it -> ((NumberedImpl)it).number)
			.collect(Collectors.toList());

		// then
		then(actual).containsExactly(1, 2, 3);
		then(sequence.get()).isEqualTo(3);
	}

	@Test
	void setLazyForEachElementKeepsElementContainerSize() {
		// when
		NestedTagsHolder actual = SUT.giveMeBuilder(NestedTagsHolder.class)
			.size("tagsList", 2)
			.setLazy("tagsList[*]", () -> Arrays.asList("a", "b"))
			.sample();

		// then
		then(actual.tagsList).containsExactly(Arrays.asList("a", "b"), Arrays.asList("a", "b"));
	}

	@Test
	void setLazyForEachElementKeepsNestedContainerSize() {
		// when
		ChildrenHolder actual = SUT.giveMeBuilder(ChildrenHolder.class)
			.size("children", 2)
			.setLazy("children[*]", () -> child("a"))
			.sample();

		// then
		then(actual.children).allSatisfy(it -> {
			then(it.name).isEqualTo("a");
			then(it.tags).containsExactly("x", "y");
		});
	}

	@Test
	void setLazyForEachElementMergesWithDirectiveBelow() {
		// when
		ChildrenHolder actual = SUT.giveMeBuilder(ChildrenHolder.class)
			.size("children", 2)
			.setLazy("children[*]", () -> child("a"))
			.set("children[0].name", "b")
			.sample();

		// then
		then(actual.children.get(0).name).isEqualTo("b");
		then(actual.children.get(0).tags).containsExactly("x", "y");
		then(actual.children.get(1).name).isEqualTo("a");
	}

	@Test
	void registeredLazyArbitraryIsSampled() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Child.class, fm -> fm.giveMeBuilder(Child.class).setLazy("name", () -> Arbitraries.just("a")))
			.build();

		// when
		Child actual = sut.giveMeOne(Child.class);

		// then
		then(actual.name).isEqualTo("a");
	}

	private static Child child(String name) {
		Child child = new Child();
		child.name = name;
		child.tags = new ArrayList<>(Arrays.asList("x", "y"));
		return child;
	}

	public interface Numbered {
	}

	public static class NumberedImpl implements Numbered {
		private Integer number;

		public NumberedImpl() {
		}

		NumberedImpl(Integer number) {
			this.number = number;
		}
	}

	public static class NumberedHolder {
		private List<Numbered> numbereds;
	}

	public static class NumberHolder {
		private Integer number;

		public NumberHolder() {
		}

		NumberHolder(Integer number) {
			this.number = number;
		}
	}

	public static class NumberHolderParent {
		private NumberHolder holder;
	}

	public static class NumbersHolder {
		private List<Integer> numbers;
	}

	public static class NumbersHolderParent {
		private NumbersHolder holder;
	}

	public static class NumberHoldersHolder {
		private List<NumberHolder> holders;
	}

	public static class NestedTagsHolder {
		private List<List<String>> tagsList;
	}

	public static class Child {
		private String name;
		private List<String> tags;
	}

	public static class ChildrenHolder {
		private List<Child> children;
	}
}
