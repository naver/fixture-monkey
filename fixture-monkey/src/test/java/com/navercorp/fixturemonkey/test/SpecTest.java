package com.navercorp.fixturemonkey.test;

import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;

import java.util.ArrayList;
import java.util.List;

import static com.navercorp.fixturemonkey.test.SpecTestSpecs.SUT;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

public class SpecTest {
	@Property
	void giveMeSpecSet() {
		// when
		SpecTestSpecs.IntValue actual = SUT.giveMeBuilder(SpecTestSpecs.IntValue.class)
			.spec(new ExpressionSpec().set("value", -1))
			.sample();

		then(actual.getValue()).isEqualTo(-1);
	}

	@Property
	void giveMeSpecSetArbitrary() {
		// when
		SpecTestSpecs.IntValue actual = SUT.giveMeBuilder(SpecTestSpecs.IntValue.class)
			.spec(new ExpressionSpec().set("value", Arbitraries.just(1)))
			.sample();

		then(actual.getValue()).isEqualTo(1);
	}

	@Property
	void giveMeListSize() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec().size("values", 1, 1))
			.sample();

		then(actual.getValues()).hasSize(1);
	}

	@Property
	void giveMeSetNull() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec().setNull("values"))
			.sample();

		then(actual.getValues()).isNull();
	}

	@Property
	void giveMeSizeAfterSetNullReturnsNull() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
			)
			.sample();

		then(actual.getValues()).isNull();
	}

	@Property
	void giveMeSetAfterSetNullReturnsNotNull() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
				.set("values[0]", 0)
			)
			.sample();

		then(actual.getValues()).isNotNull();
		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).isEqualTo(0);
	}

	@Property
	void giveMeSetNotNullAfterSetNullReturnsNotNull() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.setNotNull("values")
			)
			.sample();

		then(actual.getValues()).isNotNull();
	}

	@Property
	void giveMeSetNullAfterSetNotNullReturnsNull() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec()
				.setNotNull("values")
				.setNull("values")
			)
			.sample();

		then(actual.getValues()).isNull();
	}

	@Property
	void giveMeSetNullAfterSetReturnsNull() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec()
				.size("values", 1, 1)
				.set("values[0]", 0)
				.setNull("values")
			)
			.sample();

		then(actual.getValues()).isNull();
	}

	@Property
	void giveMeSpecPostCondition() {
		// when
		SpecTestSpecs.IntValue actual = SUT.giveMeBuilder(SpecTestSpecs.IntValue.class)
			.spec(new ExpressionSpec().setPostCondition(
				"value",
				int.class,
				value -> value >= 0 && value <= 100
			))
			.sample();

		then(actual.getValue()).isBetween(0, 100);
	}

	@Property
	void giveMeSpecPostConditionType() {
		// when
		SpecTestSpecs.IntValue actual = SUT.giveMeBuilder(SpecTestSpecs.IntValue.class)
			.spec(new ExpressionSpec().setPostCondition(
				"value",
				int.class,
				value -> value >= 0 && value <= 100
			))
			.sample();

		then(actual.getValue()).isBetween(0, 100);
	}

	@Property
	void giveMePostConditionIndex() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec()
				.setPostCondition("values[0]", int.class, value -> value >= 0 && value <= 100)
				.size("values", 1, 1))
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).isBetween(0, 100);
	}

	@Property
	void giveMeObjectToBuilderSetIndex() {
		// given
		SpecTestSpecs.IntegerList expected = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec().size("values", 2, 2))
			.sample();

		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(expected)
			.set("values[1]", 1)
			.sample();

		then(actual.getValues().get(1)).isEqualTo(1);
	}

	@Property
	void giveMeListSpecMaxSize() {
		// when
		SpecTestSpecs.StringList actual = SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMaxSize(2)
				)
			)
			.sample();

		then(actual.getValues().size()).isLessThanOrEqualTo(2);
	}

	@Property
	void giveMeListSpecSizeBetween() {
		// when
		SpecTestSpecs.StringList actual = SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofSizeBetween(1, 3)
				)
			)
			.sample();

		then(actual.getValues().size()).isBetween(1, 3);
	}

	@Property
	void giveMeSetRightOrder() {
		// when
		SpecTestSpecs.StringList actual = SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofSize(3)
						.setElement(0, "field1")
						.setElement(1, "field2")
						.setElement(2, "field3")
				)
			)
			.sample();

		// then
		List<String> values = actual.getValues();
		then(values.size()).isEqualTo(3);
		then(values.get(0)).isEqualTo("field1");
		then(values.get(1)).isEqualTo("field2");
		then(values.get(2)).isEqualTo("field3");
	}

	@Property
	void giveMePostConditionRightOrder() {
		// when
		SpecTestSpecs.StringList actual = SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
			.spec(new ExpressionSpec()
				.list("values",
					(it) -> it.ofSize(2)
						.setElementPostCondition(0, String.class, s -> s.length() > 5)
						.setElementPostCondition(1, String.class, s -> s.length() > 10)
				))
			.sample();

		// then
		List<String> values = actual.getValues();
		then(values.size()).isEqualTo(2);
		then(values.get(0).length()).isGreaterThan(5);
		then(values.get(1).length()).isGreaterThan(10);
	}

	@Property
	void giveMeListSpecMinSize() {
		// when
		SpecTestSpecs.StringList actual = SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMinSize(1)
				)
			)
			.sample();

		then(actual.getValues().size()).isGreaterThanOrEqualTo(1);
	}

	@Property
	void giveMeSpecAny() {
		// given
		ExpressionSpec specOne = new ExpressionSpec()
			.list("values", it -> it
				.ofSize(1)
				.setElement(0, 1)
			);
		ExpressionSpec specTwo = new ExpressionSpec()
			.list("values", it -> it
				.ofSize(2)
				.setElement(0, 1)
				.setElement(1, 2)
			);

		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.specAny(specOne, specTwo)
			.sample();

		// then
		SpecTestSpecs.IntegerList expectedOne = new SpecTestSpecs.IntegerList();
		expectedOne.setValues(new ArrayList<>());
		expectedOne.getValues().add(1);

		SpecTestSpecs.IntegerList expectedTwo = new SpecTestSpecs.IntegerList();
		expectedTwo.setValues(new ArrayList<>());
		expectedTwo.getValues().add(1);
		expectedTwo.getValues().add(2);

		then(actual).isIn(expectedOne, expectedTwo);
	}

	@Property
	void giveMeSpecAnyWithEmpty() {
		thenNoException().isThrownBy(
			() -> SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
				.specAny()
				.sample()
		);
	}

	@Property
	void giveMeSpecAnyWithNull() {
		thenNoException().isThrownBy(
			() -> SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
				.specAny((ExpressionSpec[])null)
				.sample()
		);
	}

	@Property(tries = 2)
	void giveMeSpecAnyReturnsDiff() {
		// given
		Arbitrary<SpecTestSpecs.StringValue> complex = SpecTestSpecs.SUT.giveMeBuilder(
				SpecTestSpecs.StringValue.class)
			.specAny(
				new ExpressionSpec().set("value", "test1"),
				new ExpressionSpec().set("value", "test2"),
				new ExpressionSpec().set("value", "test3"),
				new ExpressionSpec().set("value", "test4"),
				new ExpressionSpec().set("value", "test5"),
				new ExpressionSpec().set("value", "test6"),
				new ExpressionSpec().set("value", "test7"),
				new ExpressionSpec().set("value", "test8"),
				new ExpressionSpec().set("value", "test9"),
				new ExpressionSpec().set("value", "test10")
			)
			.build();

		// when
		List<SpecTestSpecs.StringValue> sampled = complex.list().ofSize(100).sample();

		// then
		List<SpecTestSpecs.StringValue> distinct = sampled.stream().distinct().collect(toList());
		then(distinct.size()).isNotEqualTo(sampled.size());
	}

	@Property
	void giveMeSpecAnyFirstWithMetadataManipulatorReturnsGivenOrder() {
		// when
		SpecTestSpecs.StringList actual = SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
			.specAny(new ExpressionSpec().size("values", 2))
			.size("values", 1)
			.sample();

		then(actual.getValues()).hasSize(1);
	}

	@Property
	void giveMeSpecAnyLastWithMetadataManipulatorReturnsGivenOrder() {
		// when
		SpecTestSpecs.StringList actual = SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
			.size("values", 1)
			.specAny(new ExpressionSpec().size("values", 2))
			.sample();

		then(actual.getValues()).hasSize(2);
	}

	@Property
	void giveMeSpecListSetSize() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec().list("values", it -> it.ofSize(1)))
			.sample();

		then(actual.getValues()).hasSize(1);
	}

	@Property
	void giveMeSpecListSetElement() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElement(0, 1);
			}))
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).isEqualTo(1);
	}

	@Property
	void giveMeSpecListAnySet() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.any(1);
			}))
			.sample();

		then(actual.getValues()).anyMatch(it -> it == 1);
	}

	@Property
	void giveMeSpecListAnyWithoutSize() {
		// when
		SpecTestSpecs.StringList actual = SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
			.spec(new ExpressionSpec().list("values", it -> it.any("set")))
			.sample();

		then(actual.getValues()).anyMatch(it -> it.equals("set"));
	}

	@Property
	void giveMeSpecListAnyWithoutMaxSize() {
		// when
		SpecTestSpecs.StringList actual = SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofMinSize(5);
				it.any("set");
			}))
			.sample();

		then(actual.getValues()).anyMatch(it -> it.equals("set"));
	}

	@Property
	void giveMeSpecListAnyWithoutMinSize() {
		// when
		SpecTestSpecs.StringList actual = SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofMaxSize(2);
				it.any("set");
			}))
			.sample();

		then(actual.getValues()).anyMatch(it -> it.equals("set"));
	}

	@Property
	void giveMeSpecListAllSet() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.all(1);
			}))
			.sample();

		then(actual.getValues()).allMatch(it -> it == 1);
	}

	@Property
	void giveMeSpecListPostConditionElement() {
		// when
		SpecTestSpecs.IntegerList actual = SUT.giveMeBuilder(SpecTestSpecs.IntegerList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElementPostCondition(0, int.class, postConditioned -> postConditioned > 1);
			}))
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).isGreaterThan(1);
	}

	@Property
	void giveMeSpecListPostConditionElementField() {
		// when
		SpecTestSpecs.NestedStringValueList actual = SUT.giveMeBuilder(SpecTestSpecs.NestedStringValueList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElementFieldPostCondition(0, "value", String.class,
					postConditioned -> postConditioned.length() > 5);
			}))
			.sample();

		then(actual.getValues()).allMatch(it -> it.getValue().length() > 5);
	}

	@Property
	void giveMeSpecListListElementSet() {
		// when
		SpecTestSpecs.ListListString actual = SUT.giveMeBuilder(SpecTestSpecs.ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.setElement(0, "set");
				});
			}))
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).hasSize(1);
		then(actual.getValues().get(0).get(0)).isEqualTo("set");
	}

	@Property
	void giveMeSpecListListElementPostCondition() {
		// when
		SpecTestSpecs.ListListString actual = SUT.giveMeBuilder(SpecTestSpecs.ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.setElementPostCondition(0, String.class, postConditioned -> postConditioned.length() > 5);
				});
			}))
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).hasSize(1);
		then(actual.getValues().get(0).get(0).length()).isGreaterThan(5);
	}

	@Property
	void giveMeSpecSetWithLimit() {
		// when
		SpecTestSpecs.IntValue actual = SUT.giveMeBuilder(SpecTestSpecs.IntValue.class)
			.spec(new ExpressionSpec()
				.set("value", 1, 0))
			.sample();

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeSpecSetIndexWithLimitReturns() {
		// when
		SpecTestSpecs.StringList actual = SUT.giveMeBuilder(SpecTestSpecs.StringList.class)
			.spec(new ExpressionSpec()
				.size("values", 2, 2)
				.set("values[*]", "set", 1))
			.sample();

		then(actual.getValues()).anyMatch(it -> !it.equals("set"));
	}

	@Property
	void giveMeSpecSetArbitraryBuilder() {
		// when
		SpecTestSpecs.StringAndInt actual = SUT.giveMeBuilder(SpecTestSpecs.StringAndInt.class)
			.spec(new ExpressionSpec().set("value2",
				SUT.giveMeBuilder(SpecTestSpecs.IntValue.class).set("value", 1))
			)
			.sample();

		then(actual.getValue2().getValue()).isEqualTo(1);
	}
}
