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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.Values;

class SetCollectionIsolationTest {
	private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.build();

	@Test
	void eachSampleOwnsTheListPassedToSet() {
		List<String> supplied = new ArrayList<>(Arrays.asList("a", "b"));
		ArbitraryBuilder<Holder> builder = FIXTURE_MONKEY.giveMeBuilder(Holder.class)
			.set("items", supplied);

		Holder first = builder.sample();
		Holder second = builder.sample();
		first.items.add("c");

		assertThat(first.items).isNotSameAs(supplied);
		assertThat(second.items).isNotSameAs(first.items).containsExactly("a", "b");
		assertThat(supplied).containsExactly("a", "b");
	}

	@Test
	void fixedSizeInputDoesNotRestrictTheSampledList() {
		Holder sampled = FIXTURE_MONKEY.giveMeBuilder(Holder.class)
			.set("items", Arrays.asList("a", "b"))
			.sample();

		sampled.items.remove(0);
		assertThat(sampled.items).containsExactly("b");
	}

	@Test
	void concreteArrayListSamplesAreIndependent() {
		assertIndependentField("arrayList", new ArrayList<>(Arrays.asList("b", "a", "b")), it -> it.arrayList);
	}

	@Test
	void concreteLinkedListSamplesAreIndependent() {
		assertIndependentField("linkedList", new LinkedList<>(Arrays.asList("b", "a", "b")), it -> it.linkedList);
	}

	@Test
	void concreteHashSetSamplesAreIndependent() {
		assertIndependentField("hashSet", new HashSet<>(Arrays.asList("b", "a")), it -> it.hashSet);
	}

	@Test
	void concreteLinkedHashSetSamplesAreIndependent() {
		assertIndependentField("linkedHashSet", new LinkedHashSet<>(Arrays.asList("b", "a")), it -> it.linkedHashSet);
	}

	@Test
	void setInterfaceSamplesAreIndependent() {
		Set<String> supplied = new LinkedHashSet<>(Arrays.asList("b", "a"));
		assertIndependentRoot(new TypeReference<Set<String>>() { }, supplied);
	}

	@Test
	void emptyArrayListSamplesAreIndependent() {
		assertIndependentField("arrayList", new ArrayList<>(), it -> it.arrayList);
	}

	@Test
	void emptyLinkedListSamplesAreIndependent() {
		assertIndependentField("linkedList", new LinkedList<>(), it -> it.linkedList);
	}

	@Test
	void emptyHashSetSamplesAreIndependent() {
		assertIndependentField("hashSet", new HashSet<>(), it -> it.hashSet);
	}

	@Test
	void emptyLinkedHashSetSamplesAreIndependent() {
		assertIndependentField("linkedHashSet", new LinkedHashSet<>(), it -> it.linkedHashSet);
	}

	@Test
	void unmodifiableListProducesMutableSamples() {
		List<String> supplied = Collections.unmodifiableList(Arrays.asList("b", "a", "b"));
		assertIndependentRoot(new TypeReference<List<String>>() { }, supplied);
	}

	@Test
	void unmodifiableSetProducesMutableSamples() {
		Set<String> supplied = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList("b", "a")));
		assertIndependentRoot(new TypeReference<Set<String>>() { }, supplied);
	}

	@Test
	void linkedListWildcardPreservesTheDeclaredType() {
		LinkedList<String> supplied = new LinkedList<>(Arrays.asList("b", "a", "b"));
		ConcreteHolder sampled = FIXTURE_MONKEY.giveMeBuilder(ConcreteHolder.class)
			.set("linkedList", supplied)
			.set("linkedList[*]", "overridden")
			.sample();

		assertThat(sampled.linkedList).isExactlyInstanceOf(LinkedList.class)
			.containsExactly("overridden", "overridden", "overridden");
		assertThat(supplied).containsExactly("b", "a", "b");
	}

	@Test
	void linkedListCanBeExpanded() {
		ConcreteHolder sampled = FIXTURE_MONKEY.giveMeBuilder(ConcreteHolder.class)
			.set("linkedList", new LinkedList<>(Arrays.asList("b", "a")))
			.size("linkedList", 3)
			.sample();

		assertThat(sampled.linkedList).isExactlyInstanceOf(LinkedList.class).hasSize(3).startsWith("b", "a");
	}

	@Test
	void linkedListCanBeTruncated() {
		ConcreteHolder sampled = FIXTURE_MONKEY.giveMeBuilder(ConcreteHolder.class)
			.set("linkedList", new LinkedList<>(Arrays.asList("b", "a", "b")))
			.size("linkedList", 2)
			.sample();

		assertThat(sampled.linkedList).isExactlyInstanceOf(LinkedList.class).containsExactly("b", "a");
	}

	@Test
	void linkedListSamplesFromOneArbitraryAreIndependent() {
		LinkedList<String> supplied = new LinkedList<>(Arrays.asList("b", "a", "b"));
		List<LinkedList<String>> samples = FIXTURE_MONKEY.giveMeBuilder(new TypeReference<LinkedList<String>>() { })
			.set("$", supplied)
			.sampleList(2);

		assertIndependentValues(supplied, samples.get(0), samples.get(1));
	}

	@Test
	void emptySortedSetKeepsItsComparator() {
		TreeSet<String> supplied = new TreeSet<>(Comparator.reverseOrder());
		ArbitraryBuilder<TreeSet<String>> builder = FIXTURE_MONKEY
			.giveMeBuilder(new TypeReference<TreeSet<String>>() { })
			.set("$", supplied);
		TreeSet<String> first = builder.sample();
		TreeSet<String> second = builder.sample();

		assertThat(first.comparator()).isSameAs(supplied.comparator());
		assertIndependentValues(supplied, first, second);
		first.addAll(Arrays.asList("a", "b"));
		assertThat(first).containsExactly("c", "b", "a");
	}

	@Test
	void listWildcardTypeKeepsTheSuppliedValues() {
		List<? extends Number> supplied = Arrays.asList(1, 2, 1);
		ArbitraryBuilder<List<? extends Number>> builder = FIXTURE_MONKEY
			.giveMeBuilder(new TypeReference<List<? extends Number>>() { })
			.set("$", supplied);
		List<? extends Number> first = builder.sample();
		List<? extends Number> second = builder.sample();

		assertThat(first).isNotSameAs(supplied).isNotSameAs(second);
		assertThat(first).containsExactlyElementsOf(supplied);
		first.remove(0);
		assertThat(second).containsExactlyElementsOf(supplied);
	}

	@Test
	void setWithOnlyNullIsStillIndependent() {
		assertIndependentField("hashSet", new HashSet<>(Collections.singletonList(null)), it -> it.hashSet);
	}

	@Test
	void listWithNullKeepsItsOrderAndDuplicates() {
		assertIndependentField("linkedList", new LinkedList<>(Arrays.asList("b", null, "b")), it -> it.linkedList);
	}

	@Test
	void collectionCopyConstructorSupportsOtherConcreteTypes() {
		CopyableList<String> supplied = new CopyableList<>(Arrays.asList("b", "a", "b"));
		ArbitraryBuilder<CopyableHolder> builder = FIXTURE_MONKEY.giveMeBuilder(CopyableHolder.class)
			.set("items", supplied);

		assertIndependentValues(supplied, builder.sample().items, builder.sample().items);
	}

	@Test
	void failingCopyConstructorIsNotHidden() {
		ThrowingCopyList<String> supplied = new ThrowingCopyList<>();
		assertThatThrownBy(() -> FIXTURE_MONKEY.giveMeBuilder(ThrowingHolder.class)
			.set("items", supplied)
			.sample())
			.hasRootCauseMessage("copy rejected");
	}

	@Test
	void justStillPreservesTheSuppliedIdentity() {
		LinkedList<String> supplied = new LinkedList<>(Arrays.asList("b", "a"));
		LinkedList<String> sampled = FIXTURE_MONKEY.giveMeBuilder(new TypeReference<LinkedList<String>>() { })
			.set("$", Values.just(supplied))
			.sample();

		assertThat(sampled).isSameAs(supplied);
	}

	@Test
	void listSizeAndWildcardStillApply() {
		Holder sampled = FIXTURE_MONKEY.giveMeBuilder(Holder.class)
			.set("items", Arrays.asList("a", "b", "c"))
			.size("items", 2)
			.set("items[*]", "overridden")
			.sample();

		assertThat(sampled.items).containsExactly("overridden", "overridden");
	}

	private <C extends Collection<String>> void assertIndependentField(
		String field,
		C supplied,
		Function<ConcreteHolder, C> getter
	) {
		ArbitraryBuilder<ConcreteHolder> builder = FIXTURE_MONKEY.giveMeBuilder(ConcreteHolder.class)
			.set(field, supplied);
		C first = getter.apply(builder.sample());
		C second = getter.apply(builder.sample());

		assertThat(first).isExactlyInstanceOf(supplied.getClass());
		assertThat(second).isExactlyInstanceOf(supplied.getClass());
		if (supplied instanceof LinkedHashSet) {
			assertThat(first).containsExactlyElementsOf(supplied);
			assertThat(second).containsExactlyElementsOf(supplied);
		}
		assertIndependentValues(supplied, first, second);
	}

	private <C extends Collection<String>> void assertIndependentRoot(TypeReference<C> type, C supplied) {
		ArbitraryBuilder<C> builder = FIXTURE_MONKEY.giveMeBuilder(type).set("$", supplied);
		assertIndependentValues(supplied, builder.sample(), builder.sample());
	}

	private void assertIndependentValues(
		Collection<String> supplied,
		Collection<String> first,
		Collection<String> second
	) {
		List<String> expected = new ArrayList<>(supplied);
		assertThat(first).isNotNull();
		assertThat(second).isNotNull();
		assertThat(first).isNotSameAs(supplied).isNotSameAs(second);
		assertThat(second).isNotSameAs(supplied);
		assertThat(first).containsExactlyInAnyOrderElementsOf(expected);
		assertThat(second).containsExactlyInAnyOrderElementsOf(expected);
		if (supplied instanceof List) {
			assertThat(first).containsExactlyElementsOf(expected);
			assertThat(second).containsExactlyElementsOf(expected);
		}

		first.add("c");
		assertThat(second).containsExactlyInAnyOrderElementsOf(expected);
		assertThat(supplied).containsExactlyInAnyOrderElementsOf(expected);
		second.add("d");
		assertThat(first).contains("c").doesNotContain("d");
		assertThat(second).doesNotContain("c");
		assertThat(supplied).containsExactlyInAnyOrderElementsOf(expected);
	}

	private static class ConcreteHolder {
		private ArrayList<String> arrayList;
		private LinkedList<String> linkedList;
		private HashSet<String> hashSet;
		private LinkedHashSet<String> linkedHashSet;
	}

	private static class CopyableHolder {
		private CopyableList<String> items;
	}

	private static class ThrowingHolder {
		private ThrowingCopyList<String> items;
	}

	public static class CopyableList<T> extends ArrayList<T> {
		private static final long serialVersionUID = 1L;

		public CopyableList(Collection<? extends T> values) {
			super(values);
		}
	}

	public static class ThrowingCopyList<T> extends ArrayList<T> {
		private static final long serialVersionUID = 1L;

		public ThrowingCopyList() {
		}

		public ThrowingCopyList(Collection<? extends T> values) {
			throw new IllegalStateException("copy rejected");
		}
	}

	private static class Holder {
		private List<String> items;
	}
}
