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

import static org.assertj.core.api.BDDAssertions.entry;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class SetNestedContainerTest {
	private static final FixtureMonkey SUT = fixtureMonkeyWithContainerSize(3);
	private static final FixtureMonkey NOT_NULL_SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 3))
		.build();

	@Test
	void setListKeepsContainerTwoLevelsInsideElementWhenDefaultSizeIsLarger() {
		// given
		Note note = note("a", "b");

		// when
		Feed actual = SUT.giveMeBuilder(Feed.class)
			.set("notes", Collections.singletonList(note))
			.sample();

		// then
		then(actual.notes).hasSize(1);
		then(actual.notes.get(0).tags.values).containsExactly("a", "b");
	}

	@Test
	void setListKeepsContainerTwoLevelsInsideElementWhenDefaultSizeIsSmaller() {
		// given
		FixtureMonkey sut = fixtureMonkeyWithContainerSize(1);
		Note note = note("a", "b");

		// when
		Feed actual = sut.giveMeBuilder(Feed.class)
			.set("notes", Collections.singletonList(note))
			.sample();

		// then
		then(actual.notes).hasSize(1);
		then(actual.notes.get(0).tags.values).containsExactly("a", "b");
	}

	@Test
	void setListKeepsEmptyContainerTwoLevelsInsideElement() {
		// given
		Note note = note();

		// when
		Feed actual = SUT.giveMeBuilder(Feed.class)
			.set("notes", Collections.singletonList(note))
			.sample();

		// then
		then(actual.notes).hasSize(1);
		then(actual.notes.get(0).tags.values).isEmpty();
	}

	@Test
	void setListKeepsEachElementsOwnContainerSize() {
		// given
		List<Note> notes = Arrays.asList(note("a", "b"), note(), note("c", "d", "e", "f"));

		// when
		Feed actual = SUT.giveMeBuilder(Feed.class)
			.set("notes", notes)
			.sample();

		// then
		then(actual.notes).hasSize(3);
		then(actual.notes.get(0).tags.values).containsExactly("a", "b");
		then(actual.notes.get(1).tags.values).isEmpty();
		then(actual.notes.get(2).tags.values).containsExactly("c", "d", "e", "f");
	}

	@Test
	void setListKeepsNullElementNextToElementWithNestedContainer() {
		// given
		List<Note> notes = Arrays.asList(null, note("a", "b"));

		// when
		Feed actual = SUT.giveMeBuilder(Feed.class)
			.set("notes", notes)
			.sample();

		// then
		then(actual.notes.get(0)).isNull();
		then(actual.notes.get(1).tags.values).containsExactly("a", "b");
	}

	@Test
	void setListKeepsNullInsideContainerTwoLevelsInsideElement() {
		// given
		Note note = note("a", null);

		// when
		Feed actual = SUT.giveMeBuilder(Feed.class)
			.set("notes", Collections.singletonList(note))
			.sample();

		// then
		then(actual.notes.get(0).tags.values).containsExactly("a", null);
	}

	@Test
	void setArrayKeepsContainerInsideElement() {
		// given
		Tags tags = new Tags(Arrays.asList("a", "b"));

		// when
		TagsArrayFeed actual = SUT.giveMeBuilder(TagsArrayFeed.class)
			.set("tags", new Tags[] {tags})
			.sample();

		// then
		then(actual.tags).hasSize(1);
		then(actual.tags[0].values).containsExactly("a", "b");
	}

	@Test
	void setArrayKeepsContainerTwoLevelsInsideElement() {
		// given
		Note note = note("a", "b");

		// when
		NoteArrayFeed actual = SUT.giveMeBuilder(NoteArrayFeed.class)
			.set("notes", new Note[] {note})
			.sample();

		// then
		then(actual.notes).hasSize(1);
		then(actual.notes[0].tags.values).containsExactly("a", "b");
	}

	@Test
	void setMapKeepsContainerTwoLevelsInsideValue() {
		// given
		Note note = note("a", "b");

		// when
		NoteMapFeed actual = SUT.giveMeBuilder(NoteMapFeed.class)
			.set("notes", Collections.singletonMap("key", note))
			.sample();

		// then
		then(actual.notes).containsOnlyKeys("key");
		then(actual.notes.get("key").tags.values).containsExactly("a", "b");
	}

	@Test
	void setMapKeepsContainerInsideKey() {
		// given
		Map<Tags, String> map = new HashMap<>();
		map.put(new Tags(Arrays.asList("a", "b")), "value");

		// when
		TagsKeyMapFeed actual = SUT.giveMeBuilder(TagsKeyMapFeed.class)
			.set("map", map)
			.sample();

		// then
		then(actual.map).hasSize(1);
		then(actual.map.keySet().iterator().next().values).containsExactly("a", "b");
	}

	@Test
	void setListKeepsNestedListTwoLevelsInsideElement() {
		// given
		NestedListNote note = new NestedListNote(new NestedListTags(
			Arrays.asList(Arrays.asList("a", "b"), Collections.singletonList("c"))
		));

		// when
		NestedListFeed actual = SUT.giveMeBuilder(NestedListFeed.class)
			.set("notes", Collections.singletonList(note))
			.sample();

		// then
		then(actual.notes.get(0).tags.values).hasSize(2);
		then(actual.notes.get(0).tags.values.get(0)).containsExactly("a", "b");
		then(actual.notes.get(0).tags.values.get(1)).containsExactly("c");
	}

	@Test
	void setListKeepsMapTwoLevelsInsideElement() {
		// given
		Map<String, Integer> counts = new HashMap<>();
		counts.put("x", 1);
		MapNote note = new MapNote(new MapTags(counts));

		// when
		MapFeed actual = SUT.giveMeBuilder(MapFeed.class)
			.set("notes", Collections.singletonList(note))
			.sample();

		// then
		then(actual.notes.get(0).tags.counts).containsOnly(entry("x", 1));
	}

	@Test
	void setListKeepsArrayTwoLevelsInsideElement() {
		// given
		ArrayNote note = new ArrayNote(new ArrayTags(new String[] {"a", "b"}));

		// when
		ArrayFeed actual = SUT.giveMeBuilder(ArrayFeed.class)
			.set("notes", Collections.singletonList(note))
			.sample();

		// then
		then(actual.notes.get(0).tags.values).containsExactly("a", "b");
	}

	@Test
	void setListKeepsContainerInsideGenericObjectInsideElement() {
		// given
		GenericNote note = new GenericNote(new Box<>(Arrays.asList("a", "b")));

		// when
		GenericFeed actual = SUT.giveMeBuilder(GenericFeed.class)
			.set("notes", Collections.singletonList(note))
			.sample();

		// then
		then(actual.notes.get(0).box.values).containsExactly("a", "b");
	}

	@Test
	void setListKeepsImplementationOfInterfaceTypedObjectInsideElement() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 3))
			.plugin(new InterfacePlugin()
				.interfaceImplements(TagsInterface.class, Arrays.asList(ListTags.class, NameTags.class)))
			.build();
		InterfaceNote note = new InterfaceNote(new ListTags(Arrays.asList("a", "b")));

		// when
		InterfaceFeed actual = sut.giveMeBuilder(InterfaceFeed.class)
			.set("notes", Collections.singletonList(note))
			.sample();

		// then
		then(actual.notes.get(0).tags).isExactlyInstanceOf(ListTags.class);
		then(((ListTags)actual.notes.get(0).tags).values).containsExactly("a", "b");
	}

	@Test
	void setListKeepsSubclassOfAbstractTypedObjectInsideElement() {
		// given
		AbstractNote note = new AbstractNote(new ConcreteTags(Arrays.asList("a", "b")));

		// when
		AbstractFeed actual = SUT.giveMeBuilder(AbstractFeed.class)
			.set("notes", Collections.singletonList(note))
			.sample();

		// then
		then(actual.notes.get(0).tags).isExactlyInstanceOf(ConcreteTags.class);
		then(((ConcreteTags)actual.notes.get(0).tags).values).containsExactly("a", "b");
	}

	@Test
	void setListOfInterfaceKeepsContainerTwoLevelsInsideElement() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 3))
			.plugin(new InterfacePlugin()
				.interfaceImplements(NoteInterface.class, Arrays.asList(TaggedNote.class, TitledNote.class)))
			.build();
		TaggedNote note = new TaggedNote(new Tags(Arrays.asList("a", "b")));

		// when
		InterfaceElementFeed actual = sut.giveMeBuilder(InterfaceElementFeed.class)
			.set("notes", Collections.singletonList(note))
			.sample();

		// then
		then(actual.notes.get(0)).isExactlyInstanceOf(TaggedNote.class);
		then(((TaggedNote)actual.notes.get(0)).tags.values).containsExactly("a", "b");
	}

	@Test
	void setRootKeepsContainerTwoLevelsInsideListElement() {
		// given
		Feed feed = new Feed();
		feed.notes = Collections.singletonList(note("a", "b"));

		// when
		Feed actual = SUT.giveMeBuilder(Feed.class)
			.set("$", feed)
			.sample();

		// then
		then(actual.notes.get(0).tags.values).containsExactly("a", "b");
	}

	@Test
	void giveMeBuilderWithValueKeepsContainerTwoLevelsInsideListElement() {
		// given
		Feed feed = new Feed();
		feed.notes = Collections.singletonList(note("a", "b"));

		// when
		Feed actual = SUT.giveMeBuilder(feed).sample();

		// then
		then(actual.notes.get(0).tags.values).containsExactly("a", "b");
	}

	@Test
	void setRootListKeepsContainerTwoLevelsInsideElement() {
		// when
		List<Note> actual = SUT.giveMeBuilder(new TypeReference<List<Note>>() {
			})
			.set("$", Collections.singletonList(note("a", "b")))
			.sample();

		// then
		then(actual).hasSize(1);
		then(actual.get(0).tags.values).containsExactly("a", "b");
	}

	@Test
	void setLazyListKeepsContainerTwoLevelsInsideElement() {
		// when
		Feed actual = SUT.giveMeBuilder(Feed.class)
			.setLazy("notes", () -> Collections.singletonList(note("a", "b")))
			.sample();

		// then
		then(actual.notes).hasSize(1);
		then(actual.notes.get(0).tags.values).containsExactly("a", "b");
	}

	@Test
	void registeredSetListKeepsContainerTwoLevelsInsideElement() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 3))
			.register(Feed.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(Feed.class)
				.set("notes", Collections.singletonList(note("a", "b"))))
			.build();

		// when
		FeedHolder actual = sut.giveMeOne(FeedHolder.class);

		// then
		then(actual.feed.notes).hasSize(1);
		then(actual.feed.notes.get(0).tags.values).containsExactly("a", "b");
	}

	@Test
	@Timeout(value = 30, unit = TimeUnit.SECONDS)
	void setListWithBidirectionalReferenceInsideElementTerminates() {
		// given
		Parent parent = parentWithChildren(10);

		// when
		ParentFeed actual = SUT.giveMeBuilder(ParentFeed.class)
			.set("parents", Collections.singletonList(parent))
			.sample();

		// then
		then(actual.parents.get(0).children).hasSize(10);
		then(actual.parents.get(0).children.get(0).name).isEqualTo("child0");
	}

	@Test
	@Timeout(value = 30, unit = TimeUnit.SECONDS)
	void setObjectWithBidirectionalReferenceInsideContainerTerminates() {
		// given
		Parent parent = parentWithChildren(10);

		// when
		ParentHolder actual = SUT.giveMeBuilder(ParentHolder.class)
			.set("parent", parent)
			.sample();

		// then
		then(actual.parent.children).hasSize(10);
		then(actual.parent.children.get(0).name).isEqualTo("child0");
	}

	@Test
	void sizeAfterSetListResizesContainerTwoLevelsInsideElement() {
		// when
		Feed actual = NOT_NULL_SUT.giveMeBuilder(Feed.class)
			.set("notes", Collections.singletonList(note("a", "b")))
			.size("notes[0].tags.values", 5)
			.sample();

		// then
		then(actual.notes.get(0).tags.values).hasSize(5).startsWith("a", "b");
	}

	@Test
	void setListAfterSizeKeepsContainerTwoLevelsInsideElement() {
		// when
		Feed actual = NOT_NULL_SUT.giveMeBuilder(Feed.class)
			.size("notes[0].tags.values", 5)
			.set("notes", Collections.singletonList(note("a", "b")))
			.sample();

		// then
		then(actual.notes.get(0).tags.values).containsExactly("a", "b");
	}

	@Test
	void setLargerContainerAfterSetListReplacesContainerTwoLevelsInsideElement() {
		// when
		Feed actual = NOT_NULL_SUT.giveMeBuilder(Feed.class)
			.set("notes", Collections.singletonList(note("a", "b")))
			.set("notes[0].tags.values", Arrays.asList("w", "x", "y", "z"))
			.sample();

		// then
		then(actual.notes.get(0).tags.values).containsExactly("w", "x", "y", "z");
	}

	@Test
	void setObjectAfterSetListReplacesContainerTwoLevelsInsideElement() {
		// when
		Feed actual = NOT_NULL_SUT.giveMeBuilder(Feed.class)
			.set("notes", Collections.singletonList(note("a", "b")))
			.set("notes[0].tags", new Tags(Arrays.asList("w", "x", "y", "z")))
			.sample();

		// then
		then(actual.notes.get(0).tags.values).containsExactly("w", "x", "y", "z");
	}

	@Test
	void setElementAfterSetListChangesOnlyThatElementTwoLevelsInsideElement() {
		// when
		Feed actual = NOT_NULL_SUT.giveMeBuilder(Feed.class)
			.set("notes", Collections.singletonList(note("a", "b")))
			.set("notes[0].tags.values[0]", "z")
			.sample();

		// then
		then(actual.notes.get(0).tags.values).containsExactly("z", "b");
	}

	@Test
	void setListTwiceUsesContainerTwoLevelsInsideElementOfLastList() {
		// when
		Feed actual = NOT_NULL_SUT.giveMeBuilder(Feed.class)
			.set("notes", Collections.singletonList(note("a", "b", "c", "d")))
			.set("notes", Collections.singletonList(note("x")))
			.sample();

		// then
		then(actual.notes.get(0).tags.values).containsExactly("x");
	}

	@Test
	void growingSetListKeepsContainerTwoLevelsInsideSetElementOnly() {
		// when
		Feed actual = NOT_NULL_SUT.giveMeBuilder(Feed.class)
			.set("notes", Collections.singletonList(note("a", "b")))
			.size("notes", 3)
			.sample();

		// then
		then(actual.notes).hasSize(3);
		then(actual.notes.get(0).tags.values).containsExactly("a", "b");
		then(actual.notes.get(1).tags.values).hasSize(3);
		then(actual.notes.get(2).tags.values).hasSize(3);
	}

	@Test
	void shrinkingSetListKeepsContainerTwoLevelsInsideRemainingElement() {
		// when
		Feed actual = NOT_NULL_SUT.giveMeBuilder(Feed.class)
			.set("notes", Arrays.asList(note("a", "b"), note("c")))
			.size("notes", 1)
			.sample();

		// then
		then(actual.notes).hasSize(1);
		then(actual.notes.get(0).tags.values).containsExactly("a", "b");
	}

	@Test
	void wildcardSetAfterSetListReplacesContainerTwoLevelsInsideEveryElement() {
		// when
		Feed actual = NOT_NULL_SUT.giveMeBuilder(Feed.class)
			.set("notes", Arrays.asList(note("a", "b"), note("c")))
			.set("notes[*].tags.values", Collections.singletonList("w"))
			.sample();

		// then
		then(actual.notes).hasSize(2);
		then(actual.notes.get(0).tags.values).containsExactly("w");
		then(actual.notes.get(1).tags.values).containsExactly("w");
	}

	@Test
	void setListAfterWildcardSetKeepsContainerTwoLevelsInsideEveryElement() {
		// when
		Feed actual = NOT_NULL_SUT.giveMeBuilder(Feed.class)
			.set("notes[*].tags.values", Collections.singletonList("w"))
			.set("notes", Arrays.asList(note("a", "b"), note("c")))
			.sample();

		// then
		then(actual.notes).hasSize(2);
		then(actual.notes.get(0).tags.values).containsExactly("a", "b");
		then(actual.notes.get(1).tags.values).containsExactly("c");
	}

	@Test
	void thenApplyAfterSetListChangesElementTwoLevelsInsideElement() {
		// when
		Feed actual = NOT_NULL_SUT.giveMeBuilder(Feed.class)
			.set("notes", Collections.singletonList(note("a", "b")))
			.thenApply((feed, builder) -> builder.set("notes[0].tags.values[1]", "z"))
			.sample();

		// then
		then(actual.notes.get(0).tags.values).containsExactly("a", "z");
	}

	@Test
	void setObjectKeepsContainerOfObjectReachedAgainThroughField() {
		// given
		Order order = cyclicOrder();

		// when
		Root actual = SUT.giveMeBuilder(Root.class)
			.set("order", order)
			.sample();

		// then
		then(actual.order.holder.order.tags).containsExactly("t1", "t2");
	}

	@Test
	void setObjectKeepsContainerOfObjectReachedAgainThroughElement() {
		// given
		Order order = cyclicOrder();

		// when
		Root actual = SUT.giveMeBuilder(Root.class)
			.set("order", order)
			.sample();

		// then
		then(actual.order.items).hasSize(2);
		then(actual.order.items.get(0).codes).containsExactly("c1", "c2");
		then(actual.order.items.get(0).order.tags).containsExactly("t1", "t2");
	}

	@Test
	void setListKeepsContainerOfElementReachedAgainThroughElement() {
		// given
		Order order = cyclicOrder();

		// when
		Root actual = SUT.giveMeBuilder(Root.class)
			.set("orders", Collections.singletonList(order))
			.sample();

		// then
		then(actual.orders.get(0).items).hasSize(2);
		then(actual.orders.get(0).items.get(0).order.tags).containsExactly("t1", "t2");
	}

	@Test
	void setObjectKeepsContainerSevenLevelsInsideObject() {
		// when
		Root actual = SUT.giveMeBuilder(Root.class)
			.set("deep", deep("a", "b"))
			.sample();

		// then
		then(actual.deep.next.next.next.next.next.next.values).containsExactly("a", "b");
	}

	@Test
	void setListKeepsContainerSevenLevelsInsideElement() {
		// when
		Root actual = SUT.giveMeBuilder(Root.class)
			.set("deeps", Collections.singletonList(deep("a", "b")))
			.sample();

		// then
		then(actual.deeps.get(0).next.next.next.next.next.next.values).containsExactly("a", "b");
	}

	private static FixtureMonkey fixtureMonkeyWithContainerSize(int size) {
		return FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(size, size))
			.build();
	}

	private static Note note(String... values) {
		return new Note(new Tags(Arrays.asList(values)));
	}

	private static Parent parentWithChildren(int count) {
		Parent parent = new Parent();
		for (int i = 0; i < count; i++) {
			parent.children.add(new Child("child" + i, parent));
		}
		return parent;
	}

	private static Order cyclicOrder() {
		Order order = new Order();
		order.tags = Arrays.asList("t1", "t2");
		order.items = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			Item item = new Item();
			item.order = order;
			item.codes = Arrays.asList("c1", "c2");
			order.items.add(item);
		}
		order.holder = new Holder();
		order.holder.order = order;
		return order;
	}

	private static Level0 deep(String... values) {
		Level6 level6 = new Level6();
		level6.values = Arrays.asList(values);
		Level5 level5 = new Level5();
		level5.next = level6;
		Level4 level4 = new Level4();
		level4.next = level5;
		Level3 level3 = new Level3();
		level3.next = level4;
		Level2 level2 = new Level2();
		level2.next = level3;
		Level1 level1 = new Level1();
		level1.next = level2;
		Level0 level0 = new Level0();
		level0.next = level1;
		return level0;
	}

	public static class Tags {
		private List<String> values;

		public Tags() {
		}

		public Tags(List<String> values) {
			this.values = values;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Tags && Objects.equals(values, ((Tags)obj).values);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(values);
		}
	}

	public static class Note {
		private Tags tags;

		public Note() {
		}

		public Note(Tags tags) {
			this.tags = tags;
		}
	}

	public static class Feed {
		private List<Note> notes;
	}

	public static class FeedHolder {
		private Feed feed;
	}

	public static class TagsArrayFeed {
		private Tags[] tags;
	}

	public static class NoteArrayFeed {
		private Note[] notes;
	}

	public static class NoteMapFeed {
		private Map<String, Note> notes;
	}

	public static class TagsKeyMapFeed {
		private Map<Tags, String> map;
	}

	public static class NestedListTags {
		private List<List<String>> values;

		public NestedListTags() {
		}

		public NestedListTags(List<List<String>> values) {
			this.values = values;
		}
	}

	public static class NestedListNote {
		private NestedListTags tags;

		public NestedListNote() {
		}

		public NestedListNote(NestedListTags tags) {
			this.tags = tags;
		}
	}

	public static class NestedListFeed {
		private List<NestedListNote> notes;
	}

	public static class MapTags {
		private Map<String, Integer> counts;

		public MapTags() {
		}

		public MapTags(Map<String, Integer> counts) {
			this.counts = counts;
		}
	}

	public static class MapNote {
		private MapTags tags;

		public MapNote() {
		}

		public MapNote(MapTags tags) {
			this.tags = tags;
		}
	}

	public static class MapFeed {
		private List<MapNote> notes;
	}

	public static class ArrayTags {
		private String[] values;

		public ArrayTags() {
		}

		public ArrayTags(String[] values) {
			this.values = values;
		}
	}

	public static class ArrayNote {
		private ArrayTags tags;

		public ArrayNote() {
		}

		public ArrayNote(ArrayTags tags) {
			this.tags = tags;
		}
	}

	public static class ArrayFeed {
		private List<ArrayNote> notes;
	}

	public static class Box<T> {
		private List<T> values;

		public Box() {
		}

		public Box(List<T> values) {
			this.values = values;
		}
	}

	public static class GenericNote {
		private Box<String> box;

		public GenericNote() {
		}

		public GenericNote(Box<String> box) {
			this.box = box;
		}
	}

	public static class GenericFeed {
		private List<GenericNote> notes;
	}

	public interface TagsInterface {
	}

	public static class ListTags implements TagsInterface {
		private List<String> values;

		public ListTags() {
		}

		public ListTags(List<String> values) {
			this.values = values;
		}
	}

	public static class NameTags implements TagsInterface {
		private String name;
	}

	public static class InterfaceNote {
		private TagsInterface tags;

		public InterfaceNote() {
		}

		public InterfaceNote(TagsInterface tags) {
			this.tags = tags;
		}
	}

	public static class InterfaceFeed {
		private List<InterfaceNote> notes;
	}

	public abstract static class AbstractTags {
	}

	public static class ConcreteTags extends AbstractTags {
		private List<String> values;

		public ConcreteTags() {
		}

		public ConcreteTags(List<String> values) {
			this.values = values;
		}
	}

	public static class AbstractNote {
		private AbstractTags tags;

		public AbstractNote() {
		}

		public AbstractNote(AbstractTags tags) {
			this.tags = tags;
		}
	}

	public static class AbstractFeed {
		private List<AbstractNote> notes;
	}

	public interface NoteInterface {
	}

	public static class TaggedNote implements NoteInterface {
		private Tags tags;

		public TaggedNote() {
		}

		public TaggedNote(Tags tags) {
			this.tags = tags;
		}
	}

	public static class TitledNote implements NoteInterface {
		private String title;
	}

	public static class InterfaceElementFeed {
		private List<NoteInterface> notes;
	}

	public static class Parent {
		private List<Child> children = new ArrayList<>();
	}

	public static class Child {
		private String name;
		private Parent parent;

		public Child() {
		}

		public Child(String name, Parent parent) {
			this.name = name;
			this.parent = parent;
		}
	}

	public static class ParentFeed {
		private List<Parent> parents;
	}

	public static class ParentHolder {
		private Parent parent;
	}

	public static class Root {
		private Order order;
		private List<Order> orders;
		private Level0 deep;
		private List<Level0> deeps;
	}

	public static class Order {
		private List<String> tags;
		private List<Item> items;
		private Holder holder;
	}

	public static class Item {
		private Order order;
		private List<String> codes;
	}

	public static class Holder {
		private Order order;
	}

	public static class Level0 {
		private Level1 next;
	}

	public static class Level1 {
		private Level2 next;
	}

	public static class Level2 {
		private Level3 next;
	}

	public static class Level3 {
		private Level4 next;
	}

	public static class Level4 {
		private Level5 next;
	}

	public static class Level5 {
		private Level6 next;
	}

	public static class Level6 {
		private List<String> values;
	}
}
