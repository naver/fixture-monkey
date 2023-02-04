package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.test.ExpressionGeneratorTestSpecs.MapKeyIntegerValueInteger;
import com.navercorp.fixturemonkey.test.ExpressionGeneratorTestSpecs.StringList;
import com.navercorp.fixturemonkey.test.ExpressionGeneratorTestSpecs.StringValue;

public class ExpressionGeneratorTest {
	private static final FixtureMonkey SUT = FixtureMonkey.create();

	@Property
	void setWithExpressionGenerator() {
		StringValue actual = SUT.giveMeBuilder(StringValue.class)
			.set((resolver) -> "value", "test")
			.sample();

		then(actual.getValue()).isEqualTo("test");
	}

	@Property
	void sizeWithExpressionGenerator() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.size((resolver) -> "values", 3)
			.sample();

		then(actual.getValues().size()).isEqualTo(3);
	}

	@Property
	void sizeMapWithExpressionGenerator() {
		// when
		MapKeyIntegerValueInteger actual = SUT.giveMeBuilder(MapKeyIntegerValueInteger.class)
			.size((resolver) -> "values", 2, 2)
			.sample();

		then(actual.getValues()).hasSize(2);
	}

	@Property
	void minSizeWithExpressionGenerator() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.minSize((resolver) -> "values", 2)
			.sample();

		then(actual.getValues().size()).isGreaterThanOrEqualTo(2);
	}

	@Property
	void maxSizeWithExpressionGenerator() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.maxSize((resolver) -> "values", 10)
			.sample();

		then(actual.getValues().size()).isLessThanOrEqualTo(10);
	}

	@Property
	void postConditionLimitIndexWithExpressionGenerator() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.size("values", 2, 2)
			.setPostCondition((resolver) -> "values[*]", String.class, it -> it.length() > 0)
			.setPostCondition((resolver) -> "values[*]", String.class, it -> it.length() > 5, 1)
			.sample();

		then(actual.getValues()).anyMatch(it -> it.length() > 5);
	}

	@Property
	void setArbitraryBuilderWithExpressionGenerator() {
		// when
		StringValue actual = SUT.giveMeBuilder(StringValue.class)
			.set((resolver) -> "value", SUT.giveMeBuilder(String.class).set("$", "test"))
			.sample();

		then(actual.getValue()).isEqualTo("test");
	}

	@Property
	void setNotNullWithExpressionGeneratorReturnsNewValue() {
		// given
		StringValue decomposed = new StringValue();

		// when
		StringValue actual = SUT.giveMeBuilder(decomposed)
			.setNotNull((resolver) -> "value")
			.sample();

		then(actual.getValue()).isNotNull();
	}

	@Property
	void setNullWithExpressionGeneratorReturnsNull() {
		// given
		StringValue decomposed = new StringValue();

		// when
		StringValue actual = SUT.giveMeBuilder(decomposed)
			.setNull((resolver) -> "value")
			.sample();

		then(actual.getValue()).isNull();
	}
}
