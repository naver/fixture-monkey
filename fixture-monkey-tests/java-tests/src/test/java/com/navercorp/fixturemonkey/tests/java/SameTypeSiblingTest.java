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

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;

class SameTypeSiblingTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@RepeatedTest(10)
	void userSizeAppliesOnlyToTargetedSibling() {
		// when
		BagHolder actual = SUT.giveMeBuilder(BagHolder.class)
			.size("bag.items", 5)
			.sample();

		// then
		then(actual.getBag().getItems()).hasSize(5);
		then(actual.getOther().getItems()).hasSizeLessThan(5);
	}

	@RepeatedTest(10)
	void userSizesOnSiblingsApplyIndependently() {
		// when
		BagHolder actual = SUT.giveMeBuilder(BagHolder.class)
			.size("bag.items", 4)
			.size("other.items", 1)
			.sample();

		// then
		then(actual.getBag().getItems()).hasSize(4);
		then(actual.getOther().getItems()).hasSize(1);
	}

	@Test
	void defaultNotNullAppliesInsideSameTypeSiblingsOfContainerElement() {
		// when
		List<Line> actual = SUT.giveMe(LineList.class, 50)
			.stream()
			.flatMap(it -> it.getLines().stream())
			.collect(Collectors.toList());

		// then
		then(actual).isNotEmpty();
		then(actual).allSatisfy(line -> {
			then(line.getItem().getTags()).isNotNull();
			then(line.getGift().getTags()).isNotNull();
		});
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

	public static class Item {
		private final List<String> tags;

		@ConstructorProperties("tags")
		public Item(List<String> tags) {
			this.tags = tags;
		}

		public List<String> getTags() {
			return tags;
		}
	}

	public static class Line {
		private final Item item;
		private final Item gift;

		@ConstructorProperties({"item", "gift"})
		public Line(Item item, Item gift) {
			this.item = item;
			this.gift = gift;
		}

		public Item getItem() {
			return item;
		}

		public Item getGift() {
			return gift;
		}
	}

	public static class LineList {
		private final List<Line> lines;
		private final Item main;

		@ConstructorProperties({"lines", "main"})
		public LineList(List<Line> lines, Item main) {
			this.lines = lines;
			this.main = main;
		}

		public Item getMain() {
			return main;
		}

		public List<Line> getLines() {
			return lines;
		}
	}
}
