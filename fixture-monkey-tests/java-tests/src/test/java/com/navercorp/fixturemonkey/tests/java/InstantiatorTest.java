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
import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.factoryMethod;
import static org.assertj.core.api.BDDAssertions.then;

import java.beans.ConstructorProperties;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.tests.java.specs.ConstructorSpecs;
import com.navercorp.fixturemonkey.tests.java.specs.ConstructorSpecs.SimpleContainerObject;
import com.navercorp.fixturemonkey.tests.java.specs.MutableSpecs;

class InstantiatorTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void instantiateParametersInOrder() {
		String actual = SUT.giveMeBuilder(ConstructorSpecs.JavaTypeObject.class)
			.instantiate(
				ConstructorSpecs.JavaTypeObject.class,
				constructor()
					.parameter(int.class)
					.parameter(float.class)
					.parameter(long.class)
					.parameter(double.class)
					.parameter(byte.class)
					.parameter(char.class)
					.parameter(short.class)
					.parameter(boolean.class)
			)
			.sample()
			.getString();

		then(actual).isEqualTo("first");
	}

	@Test
	void instantiateNoArgsConstructor() {
		String actual = SUT.giveMeBuilder(ConstructorSpecs.JavaTypeObject.class)
			.instantiate(
				ConstructorSpecs.JavaTypeObject.class,
				constructor()
			)
			.sample()
			.getString();

		then(actual).isEqualTo("second");
	}

	@Test
	void instantiateParameterNameHint() {
		String actual = SUT.giveMeBuilder(ConstructorSpecs.JavaTypeObject.class)
			.instantiate(
				ConstructorSpecs.JavaTypeObject.class,
				constructor()
					.parameter(String.class, "str")
			)
			.set("str", "third")
			.sample()
			.getString();

		then(actual).isEqualTo("third");
	}

	@Test
	void instantiateConstructorContainer() {
		List<ConstructorSpecs.JavaTypeObject> actual = SUT.giveMeBuilder(SimpleContainerObject.class)
			.instantiate(
				SimpleContainerObject.class,
				constructor()
					.parameter(new TypeReference<List<ConstructorSpecs.JavaTypeObject>>() {
					}, "list")
			)
			.instantiate(
				ConstructorSpecs.JavaTypeObject.class,
				constructor()
					.parameter(int.class)
					.parameter(float.class)
					.parameter(long.class)
					.parameter(double.class)
					.parameter(byte.class)
					.parameter(char.class)
					.parameter(short.class)
					.parameter(boolean.class)
			)
			.size("list", 1)
			.sample()
			.getList();

		then(actual).hasSize(1);
	}

	@Test
	void instantiateConstructorGenericContainer() {
		String actual = SUT.giveMeBuilder(ConstructorSpecs.ContainerObject.class)
			.instantiate(
				ConstructorSpecs.ContainerObject.class,
				constructor()
					.parameter(new TypeReference<List<String>>() {
					})
					.parameter(new TypeReference<List<ConstructorSpecs.JavaTypeObject>>() {
					})
					.parameter(new TypeReference<java.util.Set<String>>() {
					})
					.parameter(new TypeReference<java.util.Set<ConstructorSpecs.JavaTypeObject>>() {
					})
					.parameter(new TypeReference<Map<String, Integer>>() {
					})
					.parameter(new TypeReference<Map<String, ConstructorSpecs.JavaTypeObject>>() {
					})
					.parameter(new TypeReference<Entry<String, Integer>>() {
					})
					.parameter(new TypeReference<Entry<String, ConstructorSpecs.JavaTypeObject>>() {
					})
					.parameter(new TypeReference<java.util.Optional<String>>() {
					})
					.parameter(new TypeReference<OptionalInt>() {
					})
					.parameter(new TypeReference<OptionalLong>() {
					})
					.parameter(new TypeReference<OptionalDouble>() {
					})
			)
			.instantiate(
				ConstructorSpecs.JavaTypeObject.class,
				constructor()
					.parameter(int.class)
					.parameter(float.class)
					.parameter(long.class)
					.parameter(double.class)
					.parameter(byte.class)
					.parameter(char.class)
					.parameter(short.class)
					.parameter(boolean.class)
			)
			.sample()
			.getArray()[0];

		then(actual).isEqualTo("test");
	}

	@Test
	void instantiateGenericObjectByConstructor() {
		ConstructorSpecs.GenericObject<String> actual = SUT.giveMeBuilder(
				new TypeReference<ConstructorSpecs.GenericObject<String>>() {
				})
			.instantiate(
				new TypeReference<ConstructorSpecs.GenericObject<String>>() {
				},
				constructor()
					.parameter(String.class)
			)
			.sample();

		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@Test
	void instantiateTwoGenericObjectByConstructor() {
		ConstructorSpecs.TwoGenericObject<String, Integer> actual = SUT.giveMeBuilder(
				new TypeReference<ConstructorSpecs.TwoGenericObject<String, Integer>>() {
				})
			.instantiate(
				new TypeReference<ConstructorSpecs.TwoGenericObject<String, Integer>>() {
				},
				constructor()
					.parameter(String.class)
					.parameter(Integer.class)
			)
			.sample();

		then(actual).isNotNull();
		then(actual.getTValue()).isNotNull();
		then(actual.getUValue()).isNotNull();
	}

	@Test
	void instantiateGenericObjectWithHintByConstructor() {
		ConstructorSpecs.GenericObject<String> actual = SUT.giveMeBuilder(
				new TypeReference<ConstructorSpecs.GenericObject<String>>() {
				})
			.instantiate(
				new TypeReference<ConstructorSpecs.GenericObject<String>>() {
				},
				constructor()
					.parameter(String.class)
			)
			.sample();

		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@Test
	void instantiateByFactoryMethod() {
		String actual = SUT.giveMeBuilder(ConstructorSpecs.JavaTypeObject.class)
			.instantiate(
				factoryMethod("from")
			)
			.sample()
			.getString();

		then(actual).isEqualTo("factory");
	}

	@Test
	void instantiateByFactoryMethodWithParameter() {
		String actual = SUT.giveMeBuilder(ConstructorSpecs.JavaTypeObject.class)
			.instantiate(
				factoryMethod("from")
					.parameter(String.class)
			)
			.sample()
			.getString();

		then(actual).isEqualTo("factory");
	}

	@Test
	void instantiateFactoryMethodAndField() {
		Integer actual = SUT.giveMeBuilder(ConstructorSpecs.JavaTypeObject.class)
			.instantiate(
				factoryMethod("from")
					.parameter(String.class)
					.field()
			)
			.sample()
			.getWrapperInteger();

		then(actual).isNotNull();
	}

	@Test
	void instantiateConstructorField() {
		String actual = SUT.giveMeBuilder(MutableSpecs.JavaTypeObject.class)
			.instantiate(constructor().field())
			.sample()
			.getString();

		then(actual).isNotNull();
	}

	@Test
	void instantiateConstructorJavaBeansProperty() {
		String actual = SUT.giveMeBuilder(MutableSpecs.JavaTypeObject.class)
			.instantiate(constructor().javaBeansProperty())
			.sample()
			.getString();

		then(actual).isNotNull();
	}

	@Test
	void instantiateConstructorFieldFilter() {
		MutableSpecs.JavaTypeObject actual =
			SUT.giveMeBuilder(MutableSpecs.JavaTypeObject.class)
				.instantiate(
					constructor()
						.field(it -> it.filter(field -> !Modifier.isPrivate(field.getModifiers())))
				)
				.sample();

		then(actual.getString()).isNull();
		then(actual.getWrapperBoolean()).isNull();
	}

	@Test
	void instantiateConstructorJavaBeansPropertyFilter() {
		MutableSpecs.JavaTypeObject actual =
			SUT.giveMeBuilder(MutableSpecs.JavaTypeObject.class)
				.instantiate(
					constructor()
						.javaBeansProperty(it -> it.filter(property -> !"string".equals(property.getName())))
				)
				.sample();

		then(actual.getString()).isNull();
		then(actual.getWrapperBoolean()).isNotNull();
	}

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

	@Test
	void userInstantiateAppliesToElementsBeyondSetLimit() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		// when
		List<EdgeCaseTagged> actual = sut.giveMeBuilder(TaggedList.class)
			.instantiate(EdgeCaseTagged.class, constructor().parameter(String.class))
			.size("taggeds", 3)
			.set("taggeds[*]", new EdgeCaseTagged("fixed"), 1)
			.sample()
			.getTaggeds();

		// then
		then(actual).extracting(EdgeCaseTagged::getOrigin).containsOnly("string-constructor");
	}

	@Test
	void userInstantiateAppliesToInterfaceImplementation() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Shape.class, Collections.singletonList(Circle.class)))
			.build();

		// when
		Shape actual = sut.giveMeBuilder(ShapeHolder.class)
			.instantiate(Circle.class, constructor().parameter(String.class))
			.sample()
			.getShape();

		// then
		then(actual).isInstanceOf(Circle.class);
		then(((Circle)actual).getOrigin()).isEqualTo("string-constructor");
	}

	@Test
	void userSetAppliesInsideSubtreeRebuiltForNestedInstantiate() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				EdgeCaseScoped.class,
				fm -> fm.giveMeBuilder(EdgeCaseScoped.class)
					.instantiate(EdgeCaseTagged.class, constructor().parameter(String.class, "text"))
			)
			.build();

		// when
		EdgeCaseTagged actual = sut.giveMeBuilder(EdgeCaseScopeHolder.class)
			.set("scoped.tagged.text", "user")
			.sample()
			.getScoped()
			.getTagged();

		// then
		then(actual.getOrigin()).isEqualTo("string-constructor");
		then(actual.getText()).isEqualTo("user");
	}

	@Test
	void nestedInstantiateAppliesInsideEachContainerElementOwner() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				EdgeCaseScoped.class,
				fm -> fm.giveMeBuilder(EdgeCaseScoped.class)
					.instantiate(EdgeCaseTagged.class, constructor().parameter(String.class))
			)
			.build();

		// when
		List<EdgeCaseScoped> actual = sut.giveMeBuilder(ScopedList.class)
			.size("scopeds", 2)
			.sample()
			.getScopeds();

		// then
		then(actual).extracting(it -> it.getTagged().getOrigin()).containsOnly("string-constructor");
	}

	@Test
	void userSizeAppliesInsideSubtreeRebuiltForNestedInstantiate() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				BagScope.class,
				fm -> fm.giveMeBuilder(BagScope.class)
					.instantiate(
						Bag.class,
						constructor().parameter(new TypeReference<List<String>>() {
						}, "items")
					)
			)
			.build();

		// when
		Bag actual = sut.giveMeBuilder(BagHolder.class)
			.size("bagScope.bag.items", 5)
			.sample()
			.getBagScope()
			.getBag();

		// then
		then(actual.getOrigin()).isEqualTo("list-constructor");
		then(actual.getItems()).hasSize(5);
	}

	@Test
	void nestedInstantiateAppliesToTypeTheDefaultIntrospectorCannotConstruct() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				PlainScoped.class,
				fm -> fm.giveMeBuilder(PlainScoped.class)
					.instantiate(Plain.class, constructor().parameter(String.class, "text"))
			)
			.build();

		// when
		Plain actual = sut.giveMeOne(PlainScopeHolder.class).getPlainScoped().getPlain();

		// then
		then(actual.getOrigin()).isEqualTo("string-constructor");
		then(actual.getText()).isNotNull();
	}

	@Test
	void nestedInstantiateAppliesToMapValueInsideOwner() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				MapScoped.class,
				fm -> fm.giveMeBuilder(MapScoped.class)
					.instantiate(EdgeCaseTagged.class, constructor().parameter(String.class, "text"))
			)
			.build();

		// when
		Map<String, EdgeCaseTagged> actual = sut.giveMeBuilder(MapScopeHolder.class)
			.size("mapScoped.taggedByKey", 2)
			.sample()
			.getMapScoped()
			.getTaggedByKey();

		// then
		then(actual.values()).extracting(EdgeCaseTagged::getOrigin).containsOnly("string-constructor");
		then(actual.values()).extracting(EdgeCaseTagged::getText).doesNotContainNull();
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

	public static class EdgeCaseTagged {
		private final String origin;
		private final String text;

		@ConstructorProperties("number")
		public EdgeCaseTagged(int number) {
			this.origin = "int-constructor";
			this.text = null;
		}

		public EdgeCaseTagged(String text) {
			this.origin = "string-constructor";
			this.text = text;
		}

		public String getOrigin() {
			return origin;
		}

		public String getText() {
			return text;
		}
	}

	public static class TaggedList {
		private final List<EdgeCaseTagged> taggeds;

		@ConstructorProperties("taggeds")
		public TaggedList(List<EdgeCaseTagged> taggeds) {
			this.taggeds = taggeds;
		}

		public List<EdgeCaseTagged> getTaggeds() {
			return taggeds;
		}
	}

	public interface Shape {
	}

	public static class Circle implements Shape {
		private final String origin;

		@ConstructorProperties("radius")
		public Circle(int radius) {
			this.origin = "int-constructor";
		}

		public Circle(String name) {
			this.origin = "string-constructor";
		}

		public String getOrigin() {
			return origin;
		}
	}

	public static class ShapeHolder {
		private final Shape shape;

		@ConstructorProperties("shape")
		public ShapeHolder(Shape shape) {
			this.shape = shape;
		}

		public Shape getShape() {
			return shape;
		}
	}

	public static class EdgeCaseScoped {
		private final EdgeCaseTagged tagged;

		@ConstructorProperties("tagged")
		public EdgeCaseScoped(EdgeCaseTagged tagged) {
			this.tagged = tagged;
		}

		public EdgeCaseTagged getTagged() {
			return tagged;
		}
	}

	public static class EdgeCaseScopeHolder {
		private final EdgeCaseScoped scoped;

		@ConstructorProperties("scoped")
		public EdgeCaseScopeHolder(EdgeCaseScoped scoped) {
			this.scoped = scoped;
		}

		public EdgeCaseScoped getScoped() {
			return scoped;
		}
	}

	public static class ScopedList {
		private final List<EdgeCaseScoped> scopeds;

		@ConstructorProperties("scopeds")
		public ScopedList(List<EdgeCaseScoped> scopeds) {
			this.scopeds = scopeds;
		}

		public List<EdgeCaseScoped> getScopeds() {
			return scopeds;
		}
	}

	public static class Bag {
		private final String origin;
		private final List<String> items;

		@ConstructorProperties("count")
		public Bag(int count) {
			this.origin = "int-constructor";
			this.items = Collections.emptyList();
		}

		public Bag(List<String> items) {
			this.origin = "list-constructor";
			this.items = items;
		}

		public String getOrigin() {
			return origin;
		}

		public List<String> getItems() {
			return items;
		}
	}

	public static class BagScope {
		private final Bag bag;

		@ConstructorProperties("bag")
		public BagScope(Bag bag) {
			this.bag = bag;
		}

		public Bag getBag() {
			return bag;
		}
	}

	public static class BagHolder {
		private final BagScope bagScope;

		@ConstructorProperties("bagScope")
		public BagHolder(BagScope bagScope) {
			this.bagScope = bagScope;
		}

		public BagScope getBagScope() {
			return bagScope;
		}
	}

	public static class Plain {
		private final String origin;
		private final String text;

		public Plain(int number) {
			this.origin = "int-constructor";
			this.text = null;
		}

		public Plain(String text) {
			this.origin = "string-constructor";
			this.text = text;
		}

		public String getOrigin() {
			return origin;
		}

		public String getText() {
			return text;
		}
	}

	public static class PlainScoped {
		private final Plain plain;

		@ConstructorProperties("plain")
		public PlainScoped(Plain plain) {
			this.plain = plain;
		}

		public Plain getPlain() {
			return plain;
		}
	}

	public static class PlainScopeHolder {
		private final PlainScoped plainScoped;

		@ConstructorProperties("plainScoped")
		public PlainScopeHolder(PlainScoped plainScoped) {
			this.plainScoped = plainScoped;
		}

		public PlainScoped getPlainScoped() {
			return plainScoped;
		}
	}

	public static class MapScoped {
		private final Map<String, EdgeCaseTagged> taggedByKey;

		@ConstructorProperties("taggedByKey")
		public MapScoped(Map<String, EdgeCaseTagged> taggedByKey) {
			this.taggedByKey = taggedByKey;
		}

		public Map<String, EdgeCaseTagged> getTaggedByKey() {
			return taggedByKey;
		}
	}

	public static class MapScopeHolder {
		private final MapScoped mapScoped;

		@ConstructorProperties("mapScoped")
		public MapScopeHolder(MapScoped mapScoped) {
			this.mapScoped = mapScoped;
		}

		public MapScoped getMapScoped() {
			return mapScoped;
		}
	}
}
