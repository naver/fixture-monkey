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

package com.navercorp.fixturemonkey.tests.java.specs;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;

public class RecursiveTypeSpecs {
	@Value
	public static class SelfRecursiveObject {
		String value;
		SelfRecursiveObject selfRecursiveObject;
	}

	@Value
	public static class SelfRecursiveListObject {
		String value;
		List<SelfRecursiveListObject> selfRecursiveListObjects;
	}

	@Value
	public static class SelfRecursiveMapObject {
		String value;
		Map<String, SelfRecursiveMapObject> selfRecursiveMap;
	}

	@Value
	public static class Node {
		Edge edge;
	}

	@Value
	public static class Edge {
		Node node;
	}
}
