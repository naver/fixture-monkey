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

package com.navercorp.fixturemonkey.test;

import com.navercorp.fixturemonkey.LabMonkey;

import lombok.Data;

import java.util.List;
import java.util.Map;

public class SpecTestSpecs {
	@Data
	public static class IntValue {
		private int value;
	}

	@Data
	public static class IntegerList {
		private List<Integer> values;
	}

	@Data
	public static class StringValue {
		private String value;
	}

	@Data
	public static class StringList {
		private List<String> values;
	}

	@Data
	public static class TwoString {
		private String value1;
		private String value2;
	}

	@Data
	public static class MapKeyIntegerValueInteger {
		private Map<Integer, Integer> values;
	}

	@Data
	public static class NestedStringValueList {
		private List<SpecTestSpecs.StringValue> values;
	}

	@Data
	public static class ListListString {
		private List<List<String>> values;
	}

	@Data
	public static class StringAndInt {
		private SpecTestSpecs.StringValue value1;
		private SpecTestSpecs.IntValue value2;
	}
}
