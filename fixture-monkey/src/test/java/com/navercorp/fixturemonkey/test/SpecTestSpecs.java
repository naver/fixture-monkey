package com.navercorp.fixturemonkey.test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.LabMonkey;
import lombok.Data;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;

import java.util.List;
import java.util.Map;

public class SpecTestSpecs {
	public static final LabMonkey SUT = LabMonkey.create();

	@Data
	public static class IntValue {
		private int value;
	}

	@Provide
	Arbitrary<SpecTestSpecs.IntValue> intValue() {
		return SUT.giveMeArbitrary(SpecTestSpecs.IntValue.class);
	}

	@Data
	public static class IntegerList {
		private List<Integer> values;
	}

	@Provide
	Arbitrary<SpecTestSpecs.IntegerList> integerList() {
		return SUT.giveMeArbitrary(SpecTestSpecs.IntegerList.class);
	}

	@Data
	public static class StringValue {
		private String value;
	}

	@Provide
	Arbitrary<SpecTestSpecs.StringValue> stringValue() {
		return SUT.giveMeArbitrary(SpecTestSpecs.StringValue.class);
	}

	@Data
	public static class StringList {
		private List<String> values;
	}

	@Provide
	Arbitrary<SpecTestSpecs.StringList> stringList() {
		return SUT.giveMeArbitrary(SpecTestSpecs.StringList.class);
	}

	@Data
	public static class TwoString {
		private String value1;
		private String value2;
	}

	@Provide
	Arbitrary<SpecTestSpecs.TwoString> twoString() {
		return SUT.giveMeArbitrary(SpecTestSpecs.TwoString.class);
	}

	@Data
	public static class MapKeyIntegerValueInteger {
		private Map<Integer, Integer> values;
	}

	@Provide
	Arbitrary<SpecTestSpecs.MapKeyIntegerValueInteger> mapKeyIntegerValueInteger() {
		return SUT.giveMeArbitrary(SpecTestSpecs.MapKeyIntegerValueInteger.class);
	}

	@Data
	public static class NestedStringValueList {
		private List<SpecTestSpecs.StringValue> values;
	}

	@Provide
	Arbitrary<SpecTestSpecs.NestedStringValueList> nestedStringValueList() {
		return SUT.giveMeArbitrary(SpecTestSpecs.NestedStringValueList.class);
	}

	@Data
	public static class ListListString {
		private List<List<String>> values;
	}

	@Provide
	Arbitrary<SpecTestSpecs.ListListString> listListString() {
		return SUT.giveMeArbitrary(SpecTestSpecs.ListListString.class);
	}

	@Data
	public static class StringAndInt {
		private SpecTestSpecs.StringValue value1;
		private SpecTestSpecs.IntValue value2;
	}

	@Provide
	Arbitrary<SpecTestSpecs.StringAndInt> stringAndInt() {
		return SUT.giveMeArbitrary(SpecTestSpecs.StringAndInt.class);
	}
}
