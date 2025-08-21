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

package com.navercorp.objectfarm.api.node.specs;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImmutableObject {
	public static class StringObject {
		String value;
	}

	public static class SimpleObject {
		String string;
		int integer;
		List<String> list;
		StringObject obj;
	}

	public static class ListObject {
		List<String> values;
	}

	public static class ArrayObject {
		String[] values;
	}

	public static class ListWildcardObject {
		List<? extends String> values;
	}

	public static class SetObject {
		Set<String> values;
	}

	public static class ObjectListObject {
		List<StringObject> values;
	}

	public static class MapObject {
		Map<String, Integer> values;
	}
}
