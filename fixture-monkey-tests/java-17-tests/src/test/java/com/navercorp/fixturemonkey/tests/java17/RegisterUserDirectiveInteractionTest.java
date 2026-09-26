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

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.jupiter.api.RepeatedTest;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;

class RegisterUserDirectiveInteractionTest {
	public record Leaf(Long id, String name) {
	}

	public record Mid(Leaf leaf, String tag) {
	}

	public record Root(Mid mid) {
	}

	public record LeafList(List<Leaf> leaves) {
	}

	public record Holder(LeafList list) {
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

	@RepeatedTest(TEST_COUNT)
	void directSetInsideRegisteredThenApplyContainer() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				LeafList.class,
				fixture -> fixture.giveMeBuilder(LeafList.class)
					.thenApply((it, builder) -> builder.size("leaves", 2).set("leaves[*].id", 1L))
			)
			.build();

		// when
		Holder actual = sut.giveMeBuilder(Holder.class)
			.set("list.leaves[0].id", 9L)
			.sample();

		// then
		then(actual.list().leaves()).extracting(Leaf::id).containsExactly(9L, 1L);
	}

	@RepeatedTest(TEST_COUNT)
	void directSetSiblingFieldInsideRegisteredThenApplyObject() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Leaf.class,
				fixture -> fixture.giveMeBuilder(Leaf.class).thenApply((it, builder) -> builder.set("id", 3L))
			)
			.build();

		// when
		Mid actual = sut.giveMeBuilder(Mid.class)
			.set("leaf.name", "direct")
			.sample();

		// then
		then(actual.leaf().id()).isEqualTo(3L);
		then(actual.leaf().name()).isEqualTo("direct");
	}

	@RepeatedTest(TEST_COUNT)
	void directSetElementInsideRegisteredWholeContainer() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				LeafList.class,
				fixture -> fixture.giveMeBuilder(LeafList.class)
					.set("leaves", List.of(new Leaf(1L, "a"), new Leaf(1L, "b")))
			)
			.build();

		// when
		Holder actual = sut.giveMeBuilder(Holder.class)
			.set("list.leaves[0].id", 9L)
			.sample();

		// then
		then(actual.list().leaves()).extracting(Leaf::id).containsExactly(9L, 1L);
		then(actual.list().leaves()).extracting(Leaf::name).containsExactly("a", "b");
	}

	@RepeatedTest(TEST_COUNT)
	void directWildcardSetNotNullOverridesRegisteredSetNull() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Leaf.class, fixture -> fixture.giveMeBuilder(Leaf.class).setNull("id"))
			.build();

		// when
		LeafList actual = sut.giveMeBuilder(LeafList.class)
			.size("leaves", 2)
			.setNotNull("leaves[*].id")
			.sample();

		// then
		then(actual.leaves()).hasSize(2).allMatch(it -> it.id() != null);
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterElementSetWinsOverInnerRegisterSetNullContainer() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(LeafList.class, fixture -> fixture.giveMeBuilder(LeafList.class).setNull("leaves"))
			.register(
				Holder.class,
				fixture -> fixture.giveMeBuilder(Holder.class).set("list.leaves[0].id", 9L)
			)
			.build();

		// when
		Holder actual = sut.giveMeOne(Holder.class);

		// then
		then(actual.list().leaves()).isNotNull();
		then(actual.list().leaves().get(0).id()).isEqualTo(9L);
	}

	@RepeatedTest(TEST_COUNT)
	void outerRegisterFieldSetWinsOverInnerRegisterSetNullObject() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).setNull("leaf"))
			.register(Root.class, fixture -> fixture.giveMeBuilder(Root.class).set("mid.leaf.id", 9L))
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf()).isNotNull();
		then(actual.mid().leaf().id()).isEqualTo(9L);
	}

	@RepeatedTest(TEST_COUNT)
	void directSizeOutranksRegisteredContainerValueSize() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				LeafList.class,
				fixture -> fixture.giveMeBuilder(LeafList.class)
					.thenApply((it, builder) -> builder.size("leaves", 2).set("leaves[*].id", 1L))
			)
			.build();

		// when
		Holder actual = sut.giveMeBuilder(Holder.class)
			.size("list.leaves", 3)
			.sample();

		// then
		then(actual.list().leaves()).hasSize(3);
		then(actual.list().leaves().get(0).id()).isEqualTo(1L);
		then(actual.list().leaves().get(1).id()).isEqualTo(1L);
	}

	@RepeatedTest(TEST_COUNT)
	void directSetNullInsideRegisteredThenApplyContainer() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				LeafList.class,
				fixture -> fixture.giveMeBuilder(LeafList.class)
					.thenApply((it, builder) -> builder.size("leaves", 2).set("leaves[*].id", 1L))
			)
			.build();

		// when
		Holder actual = sut.giveMeBuilder(Holder.class)
			.setNull("list.leaves[0].id")
			.sample();

		// then
		then(actual.list().leaves()).extracting(Leaf::id).containsExactly(null, 1L);
	}

	@RepeatedTest(TEST_COUNT)
	void innerRegisterSetNullObjectStillWinsWhenNoOuterDirectiveTargetsInside() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Mid.class, fixture -> fixture.giveMeBuilder(Mid.class).setNull("leaf"))
			.register(Root.class, fixture -> fixture.giveMeBuilder(Root.class).set("mid.tag", "tag"))
			.build();

		// when
		Root actual = sut.giveMeOne(Root.class);

		// then
		then(actual.mid().leaf()).isNull();
		then(actual.mid().tag()).isEqualTo("tag");
	}
}
