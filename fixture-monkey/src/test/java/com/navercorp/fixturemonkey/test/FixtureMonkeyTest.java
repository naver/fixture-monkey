package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.validation.constraints.Positive;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;

import lombok.Builder;
import lombok.Value;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.ArbitraryOption;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import com.navercorp.fixturemonkey.generator.BeanArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.BuilderArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.FieldArbitraries;
import com.navercorp.fixturemonkey.generator.FieldReflectionArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.JacksonArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.NullArbitraryGenerator;

class FixtureMonkeyTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.defaultGenerator(JacksonArbitraryGenerator.INSTANCE)
		.addCustomizer(CustomizerIntegerClass.class, new ArbitraryCustomizer<CustomizerIntegerClass>() {
			@Override
			public void customizeFields(Class<CustomizerIntegerClass> type, FieldArbitraries fieldArbitraries) {
				fieldArbitraries.replaceArbitrary("value", Arbitraries.just(1));
			}

			@Override
			public CustomizerIntegerClass customizeFixture(CustomizerIntegerClass object) {
				return object;
			}
		})
		.putGenerator(BuilderIntegerClass.class, BuilderArbitraryGenerator.INSTANCE)
		.putGenerator(FieldReflectionIntegerClass.class, FieldReflectionArbitraryGenerator.INSTANCE)
		.putGenerator(NullIntegerClass.class, NullArbitraryGenerator.INSTANCE)
		.putGenerator(BeanIntegerClass.class, BeanArbitraryGenerator.INSTANCE)
		.build();

	@Property
	void giveMeRegisteredReference() {
		Integer actual = this.sut.giveMeOne(Integer.class);

		then(actual).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeRegisteredPrimitive() {
		ArbitraryBuilder<Integer> builder = this.sut.giveMeBuilder(Integer.class);

		int actual = builder.sample();

		then(actual).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWithAnnotation() {
		IntegerWrapperClassWithAnnotation actual = this.sut.giveMeOne(IntegerWrapperClassWithAnnotation.class);

		then(actual.getValue()).isPositive();
	}

	@Property
	void giveMeWithCustomAnnotatedArbitraryGenerator() {
		ArbitraryOption customOption = ArbitraryOption.builder()
			.addAnnotatedArbitraryGenerator(
				IntegerWrapperClass.class,
				annotationSource -> Arbitraries.just(new IntegerWrapperClass(1))
			)
			.build();
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(JacksonArbitraryGenerator.INSTANCE)
			.options(customOption)
			.build();

		IntegerWrapperClass actual = sut.giveMeOne(IntegerWrapperClass.class);

		then(actual.value).isEqualTo(1);
	}

	@Property
	void giveMeWithCustomizer() {
		CustomizerIntegerClass actual = this.sut.giveMeBuilder(CustomizerIntegerClass.class)
			.sample();

		then(actual.value).isEqualTo(1);
	}

	@Property
	void giveMeCustomize() {
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.customize(IntegerWrapperClass.class, new ArbitraryCustomizer<IntegerWrapperClass>() {
				@Override
				public void customizeFields(Class<IntegerWrapperClass> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(1));
				}

				@Nullable
				@Override
				public IntegerWrapperClass customizeFixture(@Nullable IntegerWrapperClass object) {
					return object;
				}
			})
			.sample();

		then(actual.value).isEqualTo(1);
	}

	@Property
	void giveMeWhenPutBuilderArbitraryGenerator() {
		BuilderIntegerClass actual = this.sut.giveMeOne(BuilderIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBuilderArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();

		BuilderIntegerClass actual = sut.giveMeOne(BuilderIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsFieldReflectionArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(FieldReflectionArbitraryGenerator.INSTANCE)
			.build();
		FieldReflectionIntegerClass actual = sut.giveMeOne(FieldReflectionIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenPutFieldReflectionArbitraryGenerator() {
		FieldReflectionIntegerClass actual = this.sut.giveMeOne(FieldReflectionIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsNullArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(NullArbitraryGenerator.INSTANCE)
			.build();

		IntegerWrapperClass actual = sut.giveMeOne(IntegerWrapperClass.class);

		then(actual).isNull();
	}

	@Property
	void giveMeWhenPutNullArbitraryGenerator() {
		NullIntegerClass actual = this.sut.giveMeOne(NullIntegerClass.class);

		then(actual).isNull();
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBeanArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BeanArbitraryGenerator.INSTANCE)
			.build();

		BeanIntegerClass actual = sut.giveMeOne(BeanIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenPutBeanArbitraryGenerator() {
		BeanIntegerClass actual = this.sut.giveMeOne(BeanIntegerClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBuilderArbitraryGeneratorWithCustomizer() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		BuilderIntegerClass actual = sut.giveMeOne(BuilderIntegerClass.class,
			new ArbitraryCustomizer<BuilderIntegerClass>() {
				@Override
				public void customizeFields(Class<BuilderIntegerClass> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.putArbitrary("value", Arbitraries.just(1));
				}

				@Nullable
				@Override
				public BuilderIntegerClass customizeFixture(@Nullable BuilderIntegerClass fixture) {
					return fixture;
				}
			});

		then(actual.value).isEqualTo(1);
	}

	@Property
	void giveMeSpecSet() {
		int expected = -1;

		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().set("value", expected))
			.build()
			.sample();

		then(actual.getValue()).isEqualTo(expected);
	}

	@Property
	void giveMeSpecSetArbitrary() {
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().set("value", Arbitraries.just(1)))
			.sample();

		then(actual.value).isEqualTo(1);
	}

	@Property
	void giveMeSetArbitrary() {
		Arbitrary<IntegerWrapperClass> builder = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.set("value", Arbitraries.just(1)).build();

		then(builder.sample().value).isEqualTo(1);
	}

	@Property
	void giveMeListSize() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().size("values", 1, 1))
			.build()
			.sample();

		then(actual.values).hasSize(1);
	}

	@Property
	void giveMeSetNull() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().setNull("values"))
			.sample();

		then(actual.values).isNull();
	}

	@Property
	void giveMeSizeAfterSetNullReturnsNull() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
			)
			.sample();

		then(actual.values).isNull();
	}

	@Property
	void giveMeSetAfterSetNullReturnsNotNull() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
				.set("values[0]", 0)
			)
			.sample();

		then(actual.values).isNotNull();
		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isEqualTo(0);
	}

	@Property
	void giveMeSetNotNullAfterSetNullReturnsNotNull() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.setNotNull("values")
			)
			.sample();

		then(actual.values).isNotNull();
	}

	@Property
	void giveMeSetNullAfterSetNotNullReturnsNull() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNotNull("values")
				.setNull("values")
			)
			.sample();

		then(actual.values).isNull();
	}

	@Property
	void giveMeSetNullAfterSetReturnsNull() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.size("values", 1, 1)
				.set("values[0]", 0)
				.setNull("values")
			)
			.sample();

		then(actual.values).isNull();
	}

	@Property
	void giveMeSpecSetPrefix() {
		StringWrapperClass actual = this.sut.giveMeBuilder(StringWrapperClass.class)
			.spec(new ExpressionSpec().setPrefix("value", "prefix"))
			.sample();

		then(actual.value).startsWith("prefix");
	}

	@Property
	void giveMeSpecSetSuffix() {
		StringWrapperClass actual = this.sut.giveMeBuilder(StringWrapperClass.class)
			.spec(new ExpressionSpec().setSuffix("value", "suffix"))
			.sample();

		then(actual.value).endsWith("suffix");
	}

	@Property
	void giveMeSpecFilter() {
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().<Integer>filter(
				"value",
				value -> value >= 0 && value <= 100
			))
			.sample();

		then(actual.value).isBetween(0, 100);
	}

	@Property
	void giveMeSpecFilterType() {
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().filterInteger(
				"value",
				value -> value >= 0 && value <= 100
			))
			.sample();

		then(actual.value).isBetween(0, 100);
	}

	@Property
	void giveMeFilterIndex() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.filterInteger("values[0]", value -> value >= 0 && value <= 100)
				.size("values", 1, 1))
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isBetween(0, 100);
	}

	@Property
	void giveMeObjectToBuilder() {
		IntegerWrapperClass expected = this.sut.giveMeOne(IntegerWrapperClass.class);

		ArbitraryBuilder<IntegerWrapperClass> actual = this.sut.giveMeBuilder(expected);

		then(actual.sample()).isEqualTo(expected);
	}

	@Property
	void giveMeObjectToBuilderSet() {
		IntegerWrapperClass expected = this.sut.giveMeOne(IntegerWrapperClass.class);

		ArbitraryBuilder<IntegerWrapperClass> actual = this.sut.giveMeBuilder(expected)
			.set("value", 1);

		then(actual.sample().value).isEqualTo(1);
	}

	@Property
	void giveMeObjectToBuilderSetIndex() {
		IntegerListClass expected = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().size("values", 2, 2))
			.sample();

		ArbitraryBuilder<IntegerListClass> actual = this.sut.giveMeBuilder(expected)
			.set("values[1]", 1);

		then(actual.sample().values.get(1)).isEqualTo(1);
	}

	@Property
	void giveMeArrayToBuilder() {
		IntegerArrayClass expected = new IntegerArrayClass(new Integer[] {1, 2, 3});

		IntegerArrayClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void giveMePrimitiveArrayToBuilder() {
		IntArrayClass expected = new IntArrayClass(new int[] {1, 2, 3});

		IntArrayClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void giveMeSameKeyValueMapToBuilder() {
		Map<Integer, Integer> values = new HashMap<>();
		values.put(1, 1);
		MapKeyIntegerValueIntegerClass expected = new MapKeyIntegerValueIntegerClass(values);

		MapKeyIntegerValueIntegerClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void giveMeDiffKeyValueMapToBuilder() {
		Map<Integer, String> values = new HashMap<>();
		values.put(1, "1");
		MapKeyIntegerValueStringClass expected = new MapKeyIntegerValueStringClass(values);

		MapKeyIntegerValueStringClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void giveMeMapEntryToBuilder() {
		Map.Entry<Integer, String> value = new SimpleEntry<>(1, "1");
		MapEntryKeyIntegerValueStringClass expected = new MapEntryKeyIntegerValueStringClass(value);

		MapEntryKeyIntegerValueStringClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void giveMeSetToBuilder() {
		Set<Integer> values = new HashSet<>();
		values.add(1);
		IntegerSetClass expected = new IntegerSetClass(values);

		IntegerSetClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void giveMeIterableToBuilder() {
		List<Integer> values = new ArrayList<>();
		values.add(1);
		IntegerIterableClass expected = new IntegerIterableClass(values);

		IntegerIterableClass actual = sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(actual);
	}

	@Property
	void giveMeIteratorToBuilder() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		List<Integer> values = new ArrayList<>();
		values.add(1);
		IntegerIteratorClass expected = new IntegerIteratorClass(values.iterator());

		IntegerIteratorClass actual = sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(actual);
	}

	@Property
	void giveMeStreamToBuilder() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		List<Integer> values = new ArrayList<>();
		values.add(1);
		IntegerStreamClass expected = new IntegerStreamClass(values.stream());

		IntegerStreamClass actual = sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(actual);
	}

	@Property
	void giveMeOptionalToBuilder() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		Optional<Integer> value = Optional.of(1);
		IntegerOptionalClass expected = new IntegerOptionalClass(value);

		IntegerOptionalClass actual = sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(actual);
	}

	@Property
	void giveMeOptionalEmptyToBuilder() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		Optional<Integer> value = Optional.empty();
		IntegerOptionalClass expected = new IntegerOptionalClass(value);

		IntegerOptionalClass actual = sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(actual);
	}

	@Property
	void giveMeSetLimitReturnsNotSet() {
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.set("value", 1, 0)
			.sample();

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeSpecSetWithLimitReturnsNotSet() {
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec()
				.set("value", 1, 0))
			.sample();

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeSpecSetIndexWithLimitReturns() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.size("values", 2, 2)
				.set("values[*]", "set", 1))
			.sample();

		then(actual.values).anyMatch(it -> !it.equals("set"));
	}

	@Property
	void giveMeSetIndexWithLimitReturns() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.size("values", 2, 2)
			.set("values[*]", "set", 1)
			.sample();

		then(actual.values).anyMatch(it -> !it.equals("set"));
	}

	@Property
	void copy() {
		IntegerArrayClass expected = new IntegerArrayClass(new Integer[] {1, 2, 3});

		ArbitraryBuilder<IntegerArrayClass> builder = this.sut.giveMeBuilder(expected);
		ArbitraryBuilder<IntegerArrayClass> copiedBuilder = builder.copy();

		IntegerArrayClass actual = builder.set("value[1]", 3).sample();
		IntegerArrayClass copied = copiedBuilder.sample();

		then(actual.value[0]).isEqualTo(copied.value[0]);
		then(actual.value[1]).isNotEqualTo(copied.value[1]);
		then(actual.value[1]).isEqualTo(3);
		then(actual.value[2]).isEqualTo(copied.value[2]);
	}

	@Property
	void giveMeFilterLimitIndex() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.size("values", 2, 2)
			.filter(String.class, "values[*]", it -> it.length() > 0)
			.filter(String.class, "values[*]", it -> it.length() > 5, 1)
			.sample();

		then(actual.values).anyMatch(it -> it.length() > 5);
	}

	@Property
	void giveMeFilterLimitIndexReturnsNotFilter() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.filter(String.class, "values[*]", it -> it.length() > 5)
			.filter(String.class, "values[*]", it -> it.length() == 0, 0)
			.sample();

		then(actual.values).allMatch(it -> it.length() > 5);
	}

	@Property
	void giveMeSpecListSetSize() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> it.ofSize(1)))
			.sample();

		then(actual.values).hasSize(1);
	}

	@Property
	void giveMeSpecListSetElement() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElement(0, 1);
			}))
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isEqualTo(1);
	}

	@Property
	void giveMeSpecListAnySet() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.any(1);
			}))
			.sample();

		then(actual.values).anyMatch(it -> it == 1);
	}

	@Property
	void giveMeSpecListAllSet() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.all(1);
			}))
			.sample();

		then(actual.values).allMatch(it -> it == 1);
	}

	@Property
	void giveMeSpecListFilterElement() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.<Integer>filterElement(0, filtered -> filtered > 1);
			}))
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isGreaterThan(1);
	}

	@Property
	void giveMeSpecListAnyFilter() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.<Integer>any(filtered -> filtered > 1);
			}))
			.sample();

		then(actual.values).anyMatch(it -> it > 1);
	}

	@Property
	void giveMeSpecListAllFilter() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.<Integer>all(filtered -> filtered > 1);
			}))
			.sample();

		then(actual.values).allMatch(it -> it > 1);
	}

	@Property
	void giveMeSpecListFilterElementField() {
		NestedStringList actual = this.sut.giveMeBuilder(NestedStringList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.<String>filterElementField(0, "value", filtered -> filtered.length() > 5);
			}))
			.sample();

		then(actual.values).allMatch(it -> it.value.length() > 5);
	}

	@Property
	void giveMeSpecListListElementSet() {
		ListListString actual = this.sut.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.setElement(0, "set");
				});
			}))
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).hasSize(1);
		then(actual.values.get(0).get(0)).isEqualTo("set");
	}

	@Property
	void giveMeSpecListListElementFilter() {
		ListListString actual = this.sut.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.<String>filterElement(0, filtered -> filtered.length() > 5);
				});
			}))
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).hasSize(1);
		then(actual.values.get(0).get(0).length()).isGreaterThan(5);
	}

	@Property
	void giveMeMap() {
		StringWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.value))
			.build()
			.sample();

		then(actual.getValue()).isNotNull();
		then(actual.getValue()).isInstanceOf(String.class);
	}

	@Property
	void giveMeMapAndSet() {
		StringWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.value))
			.set("value", "test")
			.build()
			.sample();

		then(actual.getValue()).isEqualTo("test");
	}

	@Property
	void giveMeMapAndSetAndMap() {
		String actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.value))
			.set("value", "test")
			.map(StringWrapperClass::getValue)
			.build()
			.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void giveMeList() {
		List<IntegerWrapperClass> actual = this.sut.giveMe(IntegerWrapperClass.class, 5);

		then(actual).hasSize(5);
		then(actual).allMatch(Objects::nonNull);
	}

	@Property
	void giveMeOptional() {
		OptionalClass actual = this.sut.giveMeOne(OptionalClass.class);

		then(actual).isNotNull();
	}

	@Property
	void giveMeSizeMap() {
		MapKeyIntegerValueIntegerClass actual = this.sut.giveMeBuilder(MapKeyIntegerValueIntegerClass.class)
			.size("values", 2, 2)
			.sample();

		then(actual.values).hasSize(2);
	}

	@Property
	void giveMeSetRightOrder() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofSize(3)
						.setElement(0, "field1")
						.setElement(1, "field2")
						.setElement(2, "field3")
				)
			)
			.sample();

		List<String> values = actual.getValues();
		then(values.size()).isEqualTo(3);
		then(values.get(0)).isEqualTo("field1");
		then(values.get(1)).isEqualTo("field2");
		then(values.get(2)).isEqualTo("field3");
	}

	@Property
	void giveMeFilterRightOrder() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					(it) -> it.ofSize(2)
						.filterElement(0, (Predicate<String>)s -> s.length() > 5)
						.filterElement(1, (Predicate<String>)s -> s.length() > 10)
				))
			.sample();

		List<String> values = actual.getValues();
		then(values.size()).isEqualTo(2);
		then(values.get(0).length()).isGreaterThan(5);
		then(values.get(1).length()).isGreaterThan(10);
	}

	@Property
	void giveMeListSpecMinSize() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMinSize(1)
				)
			).sample();

		then(actual.values.size()).isGreaterThanOrEqualTo(1);
	}

	@Property
	void giveMeListSpecMaxSize() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMaxSize(2)
				)
			)
			.sample();

		then(actual.values.size()).isLessThanOrEqualTo(2);
	}

	@Property
	void giveMeListSpecSizeBetween() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofSizeBetween(1, 3)
				)
			)
			.sample();

		then(actual.values.size()).isBetween(1, 3);
	}

	@Value
	private static class IntegerWrapperClass {
		int value;
	}

	@Value
	private static class IntegerWrapperClassWithAnnotation {
		@Positive
		int value;
	}

	@Value
	private static class IntegerListClass {
		List<Integer> values;
	}

	@Value
	private static class CustomizerIntegerClass {
		Integer value;
	}

	@Builder
	private static class BuilderIntegerClass {
		int value;
	}

	private static class FieldReflectionIntegerClass {
		private int value;
	}

	private static class NullIntegerClass {
		int value;
	}

	public static class BeanIntegerClass {
		private int value;
	}

	@Value
	private static class StringWrapperClass {
		String value;
	}

	@Value
	private static class IntegerArrayClass {
		Integer[] value;
	}

	@Value
	private static class IntArrayClass {
		int[] value;
	}

	@Value
	private static class MapKeyIntegerValueIntegerClass {
		Map<Integer, Integer> values;
	}

	@Value
	private static class MapKeyIntegerValueStringClass {
		Map<Integer, String> values;
	}

	@Value
	private static class MapEntryKeyIntegerValueStringClass {
		Map.Entry<Integer, String> value;
	}

	@Value
	private static class IntegerSetClass {
		Set<Integer> values;
	}

	@Builder
	private static class IntegerIterableClass {
		Iterable<Integer> values;
	}

	@Builder
	private static class IntegerIteratorClass {
		Iterator<Integer> values;
	}

	@Builder
	private static class IntegerStreamClass {
		Stream<Integer> values;
	}

	@Builder
	private static class IntegerOptionalClass {
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		Optional<Integer> value;
	}

	@Value
	private static class StringListClass {
		List<String> values;
	}

	@Value
	private static class NestedStringList {
		List<StringWrapperClass> values;
	}

	@Value
	private static class ListListString {
		List<List<String>> values;
	}

	@Value
	private static class OptionalClass {
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		Optional<Integer> value;
	}
}
