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

import org.junit.jupiter.api.RepeatedTest;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;

class RegisterOuterTypeDirectivePrecedenceTest {
	public record Entity(Long id, String name) {
	}

	public record Wrapper(Entity entity) {
	}

	private static FixtureMonkeyBuilder withLongRegistered() {
		return FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Long.class,
				fixture -> fixture.giveMeJavaBuilder(Long.class).set(Arbitraries.longs().between(0, Integer.MAX_VALUE))
			);
	}

	@RepeatedTest(TEST_COUNT)
	void registeredSetNullOnly() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Entity.class, fixture -> fixture.giveMeJavaBuilder(Entity.class).setNull(javaGetter(Entity::id)))
			.build();

		// when
		Entity actual = sut.giveMeOne(Entity.class);

		// then
		then(actual.id()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void registeredSetNullOverridesRegisteredFieldType() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Entity.class, fixture -> fixture.giveMeJavaBuilder(Entity.class).setNull(javaGetter(Entity::id)))
			.build();

		// when
		Entity actual = sut.giveMeOne(Entity.class);

		// then
		then(actual.id()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void registeredSetNullValueOverridesRegisteredFieldType() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(
				Entity.class,
				fixture -> fixture.giveMeJavaBuilder(Entity.class).set(javaGetter(Entity::id), null)
			)
			.build();

		// when
		Entity actual = sut.giveMeOne(Entity.class);

		// then
		then(actual.id()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void registeredSetOverridesRegisteredFieldType() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Entity.class, fixture -> fixture.giveMeJavaBuilder(Entity.class).set(javaGetter(Entity::id), -1L))
			.build();

		// when
		Entity actual = sut.giveMeOne(Entity.class);

		// then
		then(actual.id()).isEqualTo(-1L);
	}

	@RepeatedTest(TEST_COUNT)
	void registeredSetNullOverridesRegisteredFieldTypeRegardlessOfOrder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Entity.class, fixture -> fixture.giveMeJavaBuilder(Entity.class).setNull(javaGetter(Entity::id)))
			.register(
				Long.class,
				fixture -> fixture.giveMeJavaBuilder(Long.class).set(Arbitraries.longs().between(0, Integer.MAX_VALUE))
			)
			.build();

		// when
		Entity actual = sut.giveMeOne(Entity.class);

		// then
		then(actual.id()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void registeredSetNullOverridesRegisteredFieldTypeInNestedProperty() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Entity.class, fixture -> fixture.giveMeJavaBuilder(Entity.class).setNull(javaGetter(Entity::id)))
			.build();

		// when
		Wrapper actual = sut.giveMeOne(Wrapper.class);

		// then
		then(actual.entity().id()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void registeredFieldTypeStillAppliesToOtherProperties() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Entity.class, fixture -> fixture.giveMeJavaBuilder(Entity.class).setNull(javaGetter(Entity::id)))
			.build();

		// when
		Long actual = sut.giveMeOne(Long.class);

		// then
		then(actual).isBetween(0L, (long)Integer.MAX_VALUE);
	}

	@RepeatedTest(TEST_COUNT)
	void directSetNullOverridesRegisteredFieldType() {
		// given
		FixtureMonkey sut = withLongRegistered().build();

		// when
		Entity actual = sut.giveMeJavaBuilder(Entity.class)
			.setNull(javaGetter(Entity::id))
			.sample();

		// then
		then(actual.id()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void directSetNotNullOverridesRegisteredSetNull() {
		// given
		FixtureMonkey sut = withLongRegistered()
			.register(Entity.class, fixture -> fixture.giveMeJavaBuilder(Entity.class).setNull(javaGetter(Entity::id)))
			.build();

		// when
		Entity actual = sut.giveMeJavaBuilder(Entity.class)
			.setNotNull(javaGetter(Entity::id))
			.sample();

		// then
		then(actual.id()).isBetween(0L, (long)Integer.MAX_VALUE);
	}
}
