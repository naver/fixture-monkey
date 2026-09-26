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

import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.constructor;
import static org.assertj.core.api.BDDAssertions.then;

import java.beans.ConstructorProperties;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;

class RegisterInstantiateTest {
	@Test
	void registeredInstantiateAppliesWhenAnEarlierRegisteredBuilderHasNone() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(First.class, fm -> fm.giveMeBuilder(First.class).set("value", "registered"))
			.register(
				Tagged.class,
				fm -> fm.giveMeBuilder(Tagged.class).instantiate(Tagged.class, constructor().parameter(String.class))
			)
			.build();

		// when
		String actual = sut.giveMeOne(Holder.class).getTagged().getOrigin();

		// then
		then(actual).isEqualTo("string-constructor");
	}

	@Test
	void registeredInstantiateAppliesWhenALaterRegisteredBuilderHasNone() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Tagged.class,
				fm -> fm.giveMeBuilder(Tagged.class).instantiate(Tagged.class, constructor().parameter(String.class))
			)
			.register(First.class, fm -> fm.giveMeBuilder(First.class).set("value", "registered"))
			.build();

		// when
		String actual = sut.giveMeOne(Holder.class).getTagged().getOrigin();

		// then
		then(actual).isEqualTo("string-constructor");
	}

	@Test
	void registeredInstantiateWithLowerPriorityNumberWins() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Tagged.class,
				fm -> fm.giveMeBuilder(Tagged.class).instantiate(Tagged.class, constructor().parameter(long.class)),
				2
			)
			.register(
				Tagged.class,
				fm -> fm.giveMeBuilder(Tagged.class).instantiate(Tagged.class, constructor().parameter(String.class)),
				1
			)
			.build();

		// when
		String actual = sut.giveMeOne(Holder.class).getTagged().getOrigin();

		// then
		then(actual).isEqualTo("string-constructor");
	}

	@Test
	void registeredInstantiateWithLowerPriorityNumberWinsRegardlessOfOrder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Tagged.class,
				fm -> fm.giveMeBuilder(Tagged.class).instantiate(Tagged.class, constructor().parameter(String.class)),
				1
			)
			.register(
				Tagged.class,
				fm -> fm.giveMeBuilder(Tagged.class).instantiate(Tagged.class, constructor().parameter(long.class)),
				2
			)
			.build();

		// when
		String actual = sut.giveMeOne(Holder.class).getTagged().getOrigin();

		// then
		then(actual).isEqualTo("string-constructor");
	}

	@Test
	void userInstantiateWinsOverRegisteredInstantiate() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Tagged.class,
				fm -> fm.giveMeBuilder(Tagged.class).instantiate(Tagged.class, constructor().parameter(String.class))
			)
			.build();

		// when
		String actual = sut.giveMeBuilder(Holder.class)
			.instantiate(Tagged.class, constructor().parameter(long.class))
			.sample()
			.getTagged()
			.getOrigin();

		// then
		then(actual).isEqualTo("long-constructor");
	}

	@Test
	void registeredInstantiateAppliesToContainerElements() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Tagged.class,
				fm -> fm.giveMeBuilder(Tagged.class).instantiate(Tagged.class, constructor().parameter(String.class))
			)
			.build();

		// when
		List<Tagged> actual = sut.giveMeBuilder(ListHolder.class)
			.size("taggeds", 3)
			.sample()
			.getTaggeds();

		// then
		then(actual).extracting(Tagged::getOrigin).containsOnly("string-constructor");
	}

	@Test
	void registeredInstantiateAppliesTwoLevelsDown() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Tagged.class,
				fm -> fm.giveMeBuilder(Tagged.class).instantiate(Tagged.class, constructor().parameter(String.class))
			)
			.build();

		// when
		String actual = sut.giveMeOne(DeepHolder.class).getHolder().getTagged().getOrigin();

		// then
		then(actual).isEqualTo("string-constructor");
	}

	@Test
	void instantiateInsideRegisteredBuilderDoesNotApplyOutsideRegisteredType() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Scoped.class,
				fm -> fm.giveMeBuilder(Scoped.class).instantiate(Tagged.class, constructor().parameter(String.class))
			)
			.build();

		// when
		ScopeHolder actual = sut.giveMeOne(ScopeHolder.class);

		// then
		then(actual.getScoped().getTagged().getOrigin()).isEqualTo("string-constructor");
		then(actual.getTagged().getOrigin()).isEqualTo("int-constructor");
	}

	@Test
	void instantiateInsideRegisteredBuilderWinsOverTargetTypeRegisterOnlyInsideOwner() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Tagged.class,
				fm -> fm.giveMeBuilder(Tagged.class).instantiate(Tagged.class, constructor().parameter(long.class))
			)
			.register(
				Scoped.class,
				fm -> fm.giveMeBuilder(Scoped.class).instantiate(Tagged.class, constructor().parameter(String.class))
			)
			.build();

		// when
		ScopeHolder actual = sut.giveMeOne(ScopeHolder.class);

		// then
		then(actual.getScoped().getTagged().getOrigin()).isEqualTo("string-constructor");
		then(actual.getTagged().getOrigin()).isEqualTo("long-constructor");
	}

	@Test
	void userInstantiateWinsOverInstantiateInsideRegisteredBuilder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Scoped.class,
				fm -> fm.giveMeBuilder(Scoped.class).instantiate(Tagged.class, constructor().parameter(String.class))
			)
			.build();

		// when
		ScopeHolder actual = sut.giveMeBuilder(ScopeHolder.class)
			.instantiate(Tagged.class, constructor().parameter(long.class))
			.sample();

		// then
		then(actual.getScoped().getTagged().getOrigin()).isEqualTo("long-constructor");
		then(actual.getTagged().getOrigin()).isEqualTo("long-constructor");
	}

	@Test
	void instantiateInsideRegisteredBuilderAppliesWhenOwnerIsTwoLevelsDown() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Scoped.class,
				fm -> fm.giveMeBuilder(Scoped.class).instantiate(Tagged.class, constructor().parameter(String.class))
			)
			.build();

		// when
		DeepScopeHolder actual = sut.giveMeOne(DeepScopeHolder.class);

		// then
		then(actual.getScopeHolder().getScoped().getTagged().getOrigin()).isEqualTo("string-constructor");
		then(actual.getScopeHolder().getTagged().getOrigin()).isEqualTo("int-constructor");
	}

	@Test
	void instantiateOfSupertypeInsideSubtypeScopeDoesNotApplyOutsideIt() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Special.class,
				fm -> fm.giveMeBuilder(Special.class)
					.instantiate(BaseTagged.class, constructor().parameter(String.class, "text"))
			)
			.build();

		// when
		BaseTagged actual = sut.giveMeOne(TaggedHolder.class).getPlain();

		// then
		then(actual.getOrigin()).isEqualTo("int-constructor");
	}

	public static class Holder {
		private final First first;
		private final Tagged tagged;

		@ConstructorProperties({"first", "tagged"})
		public Holder(First first, Tagged tagged) {
			this.first = first;
			this.tagged = tagged;
		}

		public First getFirst() {
			return first;
		}

		public Tagged getTagged() {
			return tagged;
		}
	}

	public static class First {
		private final String value;

		@ConstructorProperties("value")
		public First(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
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

		public Tagged(long number) {
			this.origin = "long-constructor";
		}

		public String getOrigin() {
			return origin;
		}
	}

	public static class ListHolder {
		private final List<Tagged> taggeds;

		@ConstructorProperties("taggeds")
		public ListHolder(List<Tagged> taggeds) {
			this.taggeds = taggeds;
		}

		public List<Tagged> getTaggeds() {
			return taggeds;
		}
	}

	public static class DeepHolder {
		private final Holder holder;

		@ConstructorProperties("holder")
		public DeepHolder(Holder holder) {
			this.holder = holder;
		}

		public Holder getHolder() {
			return holder;
		}
	}

	public static class ScopeHolder {
		private final Scoped scoped;
		private final Tagged tagged;

		@ConstructorProperties({"scoped", "tagged"})
		public ScopeHolder(Scoped scoped, Tagged tagged) {
			this.scoped = scoped;
			this.tagged = tagged;
		}

		public Scoped getScoped() {
			return scoped;
		}

		public Tagged getTagged() {
			return tagged;
		}
	}

	public static class Scoped {
		private final Tagged tagged;

		@ConstructorProperties("tagged")
		public Scoped(Tagged tagged) {
			this.tagged = tagged;
		}

		public Tagged getTagged() {
			return tagged;
		}
	}

	public static class DeepScopeHolder {
		private final ScopeHolder scopeHolder;

		@ConstructorProperties("scopeHolder")
		public DeepScopeHolder(ScopeHolder scopeHolder) {
			this.scopeHolder = scopeHolder;
		}

		public ScopeHolder getScopeHolder() {
			return scopeHolder;
		}
	}

	public static class BaseTagged {
		private final String origin;

		@ConstructorProperties("number")
		public BaseTagged(int number) {
			this.origin = "int-constructor";
		}

		public BaseTagged(String text) {
			this.origin = "string-constructor";
		}

		public String getOrigin() {
			return origin;
		}
	}

	public static class Special extends BaseTagged {
		@ConstructorProperties("number")
		public Special(int number) {
			super(number);
		}
	}

	public static class TaggedHolder {
		private final BaseTagged plain;
		private final Special special;

		@ConstructorProperties({"plain", "special"})
		public TaggedHolder(BaseTagged plain, Special special) {
			this.plain = plain;
			this.special = special;
		}

		public BaseTagged getPlain() {
			return plain;
		}

		public Special getSpecial() {
			return special;
		}
	}
}
