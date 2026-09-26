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

package com.navercorp.fixturemonkey.tests.java17;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;
import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;

class RegisterNestedPrecedenceTest {
	public record Leaf(Long id, String name) {
	}

	public record Mid(Leaf leaf, String tag) {
	}

	public record Root(Mid mid) {
	}

	public record Pair(Leaf a, Leaf b) {
	}

	public record LeafList(List<Leaf> leaves) {
	}

	public record LeafListHolder(LeafList list) {
	}

	public record Top(Root root) {
	}

	private static FixtureMonkeyBuilder withLongRegistered() {
		return FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Long.class,
				fixture -> fixture.giveMeBuilder(Long.class).set(Arbitraries.longs().between(0, Integer.MAX_VALUE))
			);
	}

	private static FixtureMonkeyBuilder withThreeLevelsRegistered() {
		return withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNull("id"))
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).set("leaf.id", 7L))
			.register(Root.class, fixture -> fixture.giveMeBuilder(Root.class).set("mid.leaf.id", 9L));
	}

	@RepeatedTest(TEST_COUNT)
	void outermostRegisteredTypeWinsWhenSamplingRoot() {
		// given
		FixtureMonkey sut = withThreeLevelsRegistered().build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf().id()).isEqualTo(9L);
	}

	@RepeatedTest(TEST_COUNT)
	void outermostRegisteredTypeWinsWhenSamplingMiddle() {
		// given
		FixtureMonkey sut = withThreeLevelsRegistered().build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf().id()).isEqualTo(7L);
	}

	@RepeatedTest(TEST_COUNT)
	void outermostRegisteredTypeWinsWhenSamplingLeaf() {
		// given
		FixtureMonkey sut = withThreeLevelsRegistered().build();

		// when
		Leaf actual = sut.giveMeOne(Leaf.class);

		// then
		then(actual.id()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fieldTypeRegisterStillAppliesWhenSamplingFieldType() {
		// given
		FixtureMonkey sut = withThreeLevelsRegistered().build();

		// when
		Long actual = sut.giveMeOne(Long.class);

		// then
		then(actual).isBetween(0L, (long)Integer.MAX_VALUE);
	}

	@RepeatedTest(TEST_COUNT)
	void middleRegisterWinsOverLeafRegisterWhenRootHasNoRegister() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNull("id"))
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).set("leaf.id", 7L))
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf().id()).isEqualTo(7L);
	}

	@RepeatedTest(TEST_COUNT)
	void rootRegisterWinsOverLeafRegisterTwoLevelsDown() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNull("id"))
			.register(Root.class, fixture -> fixture.giveMeBuilder(Root.class).set("mid.leaf.id", 9L))
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf().id()).isEqualTo(9L);
	}

	@RepeatedTest(TEST_COUNT)
	void leafRegisterSetNullWinsOverFieldTypeRegisterThreeLevelsDown() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNull("id"))
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf().id()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void leafRegisterSetWinsOverFieldTypeRegisterWhenParentHasUnrelatedRegister() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).set("id", 3L))
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).set("tag", "tag"))
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf().id()).isEqualTo(3L);
		then(actual.tag()).isEqualTo("tag");
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterOnOnePropertyKeepsInnerRegisterOnOtherProperties() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).set("name", "inner"))
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).set("leaf.id", 7L))
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf().id()).isEqualTo(7L);
		then(actual.leaf().name()).isEqualTo("inner");
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterWildcardElementPathWinsOverElementTypeRegister() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNull("id"))
			.register(
				LeafList.class,
				fixture -> fixture.giveMeBuilder(LeafList.class).size("leaves", 2).set("leaves[*].id", 5L)
			)
			.build();

		// when
		LeafList actual = sut.giveMeOne(LeafList.class);

		// then
		then(actual.leaves()).hasSize(2).allMatch(it -> Long.valueOf(5L).equals(it.id()));
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterIndexedElementPathWinsOnlyForThatElement() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNull("id"))
			.register(
				LeafList.class,
				fixture -> fixture.giveMeBuilder(LeafList.class).size("leaves", 2).set("leaves[0].id", 5L)
			)
			.build();

		// when
		LeafList actual = sut.giveMeOne(LeafList.class);

		// then
		then(actual.leaves().get(0).id()).isEqualTo(5L);
		then(actual.leaves().get(1).id()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterOnOneOfTwoSameTypePropertiesLeavesTheOtherToInnerRegister() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).set("id", 3L))
			.register(Pair.class, fixture -> fixture.giveMeBuilder(Pair.class).setNull("a.id"))
			.build();

		// when
		Pair actual = sut.giveMeOne(Pair.class);

		// then
		then(actual.a().id()).isNull();
		then(actual.b().id()).isEqualTo(3L);
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterPropertyWinsOverInnerRegisterThenApply() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Leaf.class,
				fixture -> fixture.giveMeBuilder(Leaf.class).thenApply((it, builder) -> builder.set("id", 3L))
			)
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).set("leaf.id", 7L))
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf().id()).isEqualTo(7L);
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterSetNullPropertyWinsOverInnerRegisterThenApply() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Leaf.class,
				fixture -> fixture.giveMeBuilder(Leaf.class).thenApply((it, builder) -> builder.set("id", 3L))
			)
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).setNull("leaf.id"))
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf().id()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterSetNullObjectWinsOverInnerRegisterThenApply() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Leaf.class,
				fixture -> fixture.giveMeBuilder(Leaf.class).thenApply((it, builder) -> builder.set("id", 3L))
			)
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).setNull("leaf"))
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void rootRegisterPropertyWinsOverInnerRegisterThenApplyTwoLevelsDown() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Leaf.class,
				fixture -> fixture.giveMeBuilder(Leaf.class).thenApply((it, builder) -> builder.set("id", 3L))
			)
			.register(Root.class, fixture -> fixture.giveMeBuilder(Root.class).set("mid.leaf.id", 9L))
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf().id()).isEqualTo(9L);
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterPropertyWinsOverInnerRegisterSetRoot() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).set("$", new Leaf(3L, "whole")))
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).set("leaf.id", 7L))
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf().id()).isEqualTo(7L);
		then(actual.leaf().name()).isEqualTo("whole");
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterThenApplyWinsOverInnerRegisterProperty() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).set("id", 3L))
			.register(
				Mid.class,
				fixture -> fixture.giveMeBuilder(Mid.class).thenApply((it, builder) -> builder.set("leaf.id", 7L))
			)
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf().id()).isEqualTo(7L);
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterWholeObjectWinsOverInnerRegisterProperty() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNull("id"))
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).set("leaf", new Leaf(5L, "whole")))
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf().id()).isEqualTo(5L);
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterSetNullObjectWinsOverInnerRegisterProperty() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).set("id", 3L))
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).setNull("leaf"))
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterPropertyWinsOverInnerRegisterSetLazy() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setLazy("id", () -> 3L))
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).set("leaf.id", 7L))
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf().id()).isEqualTo(7L);
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterSetNotNullWinsOverInnerRegisterSetNullAndKeepsFieldTypeRegister() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNull("id"))
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).setNotNull("leaf.id"))
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf().id()).isBetween(0L, (long)Integer.MAX_VALUE);
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterSetNullWinsOverInnerRegisterSetNotNull() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNotNull("id"))
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).setNull("leaf.id"))
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf().id()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void rootRegisterPropertyWinsOverMiddleRegisterWholeObject() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).set("leaf", new Leaf(5L, "whole")))
			.register(Root.class, fixture -> fixture.giveMeBuilder(Root.class).set("mid.leaf.id", 9L))
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf().id()).isEqualTo(9L);
		then(actual.mid().leaf().name()).isEqualTo("whole");
	}

	@RepeatedTest(TEST_COUNT)
	void rootRegisterSetNullPropertyWinsOverMiddleRegisterWholeObject() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).set("leaf", new Leaf(5L, "whole")))
			.register(Root.class, fixture -> fixture.giveMeBuilder(Root.class).setNull("mid.leaf.id"))
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf().id()).isNull();
		then(actual.mid().leaf().name()).isEqualTo("whole");
	}

	@RepeatedTest(TEST_COUNT)
	void rootRegisterSetNotNullFillsNullInsideMiddleRegisterWholeObject() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).set("leaf", new Leaf(null, "whole")))
			.register(Root.class, fixture -> fixture.giveMeBuilder(Root.class).setNotNull("mid.leaf.id"))
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf().id()).isBetween(0L, (long)Integer.MAX_VALUE);
		then(actual.mid().leaf().name()).isEqualTo("whole");
	}

	@RepeatedTest(TEST_COUNT)
	void middleRegisterWholeObjectStillWinsOverInnerRegisterWhenRootRegisterIsUnrelated() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNull("id"))
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).set("leaf", new Leaf(5L, "whole")))
			.register(Root.class, fixture -> fixture.giveMeBuilder(Root.class).set("mid.tag", "tag"))
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf().id()).isEqualTo(5L);
		then(actual.mid().leaf().name()).isEqualTo("whole");
		then(actual.mid().tag()).isEqualTo("tag");
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterElementPropertyWinsOverMiddleRegisterWholeContainer() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				LeafList.class,
				fixture -> fixture.giveMeBuilder(LeafList.class)
					.set("leaves", List.of(new Leaf(1L, "a"), new Leaf(2L, "b")))
			)
			.register(
				LeafListHolder.class,
				fixture -> fixture.giveMeBuilder(LeafListHolder.class).set("list.leaves[0].id", 9L)
			)
			.build();

		// when
		LeafListHolder actual = sut.giveMeOne(LeafListHolder.class);

		// then
		then(actual.list().leaves()).extracting(Leaf::id).containsExactly(9L, 2L);
		then(actual.list().leaves()).extracting(Leaf::name).containsExactly("a", "b");
	}

	@RepeatedTest(TEST_COUNT)
	void registeredSetPostConditionApplies() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Leaf.class,
				fixture -> fixture.giveMeBuilder(Leaf.class)
					.setPostCondition("id", Long.class, id -> id != null && id % 2 == 0)
			)
			.build();

		// when
		Leaf actual = sut.giveMeOne(Leaf.class);

		// then
		then(actual.id() % 2).isZero();
	}

	@RepeatedTest(TEST_COUNT)
	void registeredSetPostConditionAppliesToNestedProperty() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Leaf.class,
				fixture -> fixture.giveMeBuilder(Leaf.class)
					.setPostCondition("id", Long.class, id -> id != null && id % 2 == 0)
			)
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf().id() % 2).isZero();
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisteredSetPostConditionAppliesToNamedProperty() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Mid.class,
				fixture -> fixture.giveMeBuilder(Mid.class)
					.setPostCondition("leaf.id", Long.class, id -> id != null && id % 2 == 0)
			)
			.build();

		// when
		Mid actual = sut.giveMeOne(Mid.class);

		// then
		then(actual.leaf().id() % 2).isZero();
	}

	@RepeatedTest(TEST_COUNT)
	void registeredCustomizePropertyApplies() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Leaf.class,
				fixture -> fixture.giveMeBuilder(Leaf.class)
					.customizeProperty(javaGetter(Leaf::name), arbitrary -> arbitrary.map(it -> "customized"))
			)
			.build();

		// when
		Leaf actual = sut.giveMeOne(Leaf.class);

		// then
		then(actual.name()).isEqualTo("customized");
	}

	@RepeatedTest(TEST_COUNT)
	void registeredCustomizePropertyAppliesToNestedProperty() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Leaf.class,
				fixture -> fixture.giveMeBuilder(Leaf.class)
					.customizeProperty(javaGetter(Leaf::name), arbitrary -> arbitrary.map(it -> "customized"))
			)
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf().name()).isEqualTo("customized");
	}

	@RepeatedTest(TEST_COUNT)
	void directSetOverridesRegisteredCustomizeProperty() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Leaf.class,
				fixture -> fixture.giveMeBuilder(Leaf.class)
					.customizeProperty(javaGetter(Leaf::name), arbitrary -> arbitrary.map(it -> "customized"))
			)
			.build();

		// when
		Leaf actual = sut.giveMeBuilder(Leaf.class)
			.set("name", "direct")
			.sample();

		// then
		then(actual.name()).isEqualTo("direct");
	}

	@Test
	void registeredSetNotNullPreventsNullInjection() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNotNull("id"))
			.build();

		// when
		List<Leaf> actual = sut.giveMe(Leaf.class, 500);

		// then
		then(actual).allMatch(it -> it.id() != null);
	}

	@Test
	void registeredSetNotNullDoesNotAffectOtherProperties() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNotNull("id"))
			.build();

		// when
		List<Leaf> actual = sut.giveMe(Leaf.class, 500);

		// then
		then(actual).anyMatch(it -> it.name() == null);
	}

	@RepeatedTest(TEST_COUNT)
	void registeredWildcardSetAppliesWithoutInnerRegister() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				LeafList.class,
				fixture -> fixture.giveMeBuilder(LeafList.class).size("leaves", 2).set("leaves[*].name", "wild")
			)
			.build();

		// when
		LeafList actual = sut.giveMeOne(LeafList.class);

		// then
		then(actual.leaves()).hasSize(2).allMatch(it -> "wild".equals(it.name()));
	}

	@RepeatedTest(TEST_COUNT)
	void registeredSetNullThenSetNotNullInSameBuilderIsNotNull() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNull("id").setNotNull("id"))
			.build();

		// when
		Leaf actual = sut.giveMeOne(Leaf.class);

		// then
		then(actual.id()).isBetween(0L, (long)Integer.MAX_VALUE);
	}

	@RepeatedTest(TEST_COUNT)
	void registeredSetNotNullThenSetNullInSameBuilderIsNull() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNotNull("id").setNull("id"))
			.build();

		// when
		Leaf actual = sut.giveMeOne(Leaf.class);

		// then
		then(actual.id()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void registeredSetNotNullOnWildcardElementPathWinsOverElementTypeRegister() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNull("id"))
			.register(
				LeafList.class,
				fixture -> fixture.giveMeBuilder(LeafList.class).size("leaves", 2).setNotNull("leaves[*].id")
			)
			.build();

		// when
		LeafList actual = sut.giveMeOne(LeafList.class);

		// then
		then(actual.leaves()).hasSize(2)
			.allMatch(it -> it.id() != null && it.id() >= 0 && it.id() <= Integer.MAX_VALUE);
	}

	@RepeatedTest(TEST_COUNT)
	void directSetNullOverridesRegisteredSetNotNull() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNotNull("id"))
			.build();

		// when
		Leaf actual = sut.giveMeBuilder(Leaf.class)
			.setNull("id")
			.sample();

		// then
		then(actual.id()).isNull();
	}

	@Test
	void registeredRootSetNotNullPreventsNullInjectionOfProperty() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNotNull("$"))
			.build();

		// when
		List<Mid> actual = sut.giveMe(Mid.class, 500);

		// then
		then(actual).allMatch(it -> it.leaf() != null);
		then(actual).anyMatch(it -> it.tag() == null);
	}

	@RepeatedTest(TEST_COUNT)
	void outermostRegisterPropertyWinsOverDecomposedNestedPieces() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Root.class,
				fixture -> fixture.giveMeBuilder(Root.class).set("mid", new Mid(new Leaf(5L, "whole"), "tag"))
			)
			.register(Top.class, fixture -> fixture.giveMeBuilder(Top.class).set("root.mid.leaf.id", 9L))
			.build();

		// when
		Top actual = sut.giveMeOne(Top.class);

		// then
		then(actual.root().mid().leaf().id()).isEqualTo(9L);
		then(actual.root().mid().leaf().name()).isEqualTo("whole");
		then(actual.root().mid().tag()).isEqualTo("tag");
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterPropertyWinsOverInnerRegisterSetLazyWholeObject() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Mid.class,
				fixture -> fixture.giveMeBuilder(Mid.class).setLazy("leaf", () -> new Leaf(5L, "lazy"))
			)
			.register(Root.class, fixture -> fixture.giveMeBuilder(Root.class).set("mid.leaf.id", 9L))
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf().id()).isEqualTo(9L);
		then(actual.mid().leaf().name()).isEqualTo("lazy");
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterElementPropertyWinsOverInnerRegisterThenApplyContainer() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				LeafList.class,
				fixture -> fixture.giveMeBuilder(LeafList.class)
					.thenApply((it, builder) -> builder.size("leaves", 2).set("leaves[*].id", 1L))
			)
			.register(
				LeafListHolder.class,
				fixture -> fixture.giveMeBuilder(LeafListHolder.class).set("list.leaves[0].id", 9L)
			)
			.build();

		// when
		LeafListHolder actual = sut.giveMeOne(LeafListHolder.class);

		// then
		then(actual.list().leaves()).extracting(Leaf::id).containsExactly(9L, 1L);
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterElementPropertyWinsOverInnerRegisterThenApplyLargeContainer() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				LeafList.class,
				fixture -> fixture.giveMeBuilder(LeafList.class)
					.thenApply((it, builder) -> builder.size("leaves", 5).set("leaves[*].id", 1L))
			)
			.register(
				LeafListHolder.class,
				fixture -> fixture.giveMeBuilder(LeafListHolder.class).set("list.leaves[4].id", 9L)
			)
			.build();

		// when
		LeafListHolder actual = sut.giveMeOne(LeafListHolder.class);

		// then
		then(actual.list().leaves()).extracting(Leaf::id).containsExactly(1L, 1L, 1L, 1L, 9L);
	}

	@Test
	void registeredSetNotNullIsRecordedInTrace() {
		// given
		List<String> traces = new ArrayList<>();
		FixtureMonkey sut = withLongRegistered()
			.tracer(trace -> traces.add(trace.toTreeFormat()))
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).setNotNull("leaf.id"))
			.build();

		// when
		sut.giveMeOne(Mid.class);

		// then
		then(traces).isNotEmpty();
		then(traces.get(0)).contains("Mid.leaf.id").contains("SetNotNull").contains("[REGISTER]");
	}
}
