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

import java.util.List;
import java.util.Map;

import lombok.Data;

class InnerSpecTestSpecs {
	@Data
	public static class MapObject {
		private Map<String, String> strMap;
		private Map<String, Map<String, String>> mapValueMap;
		private Map<String, List<String>> listValueMap;
		private Map<String, List<List<String>>> listListValueMap;
		private Map<String, SimpleObject> objectValueMap;
		private Map<SimpleObject, String> objectKeyMap;
	}

	@Data
	public static class NestedKeyMapObject {
		private Map<Map<String, String>, String> mapKeyMap;
	}

	@Data
	public static class ListStringObject {
		private List<String> values;
	}

	@Data
	public static class NestedListStringObject {
		private List<List<String>> values;
	}

	@Data
	public static class ComplexObjectObject {
		private ComplexObject value;
	}

	@Data
	public static class ComplexObject {
		private SimpleObject value;
	}

	@Data
	public static class SimpleObject {
		private String str;
		private Integer integer;
	}

	@Data
	public static class IntegerMapObject {
		private Map<Integer, Integer> integerMap;
	}
}
