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

package com.navercorp.fixturemonkey.api.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.Test;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
class TypeReferenceTest {

	@Test
	void construct() {
		TypeReference<String> actual = new TypeReference<String>() {
		};
		then(actual.getType()).isEqualTo(String.class);
	}

	@Test
	void constructClassType() {
		TypeReference<String> actual = new TypeReference<String>(String.class) {
		};
		then(actual.getType()).isEqualTo(String.class);
	}

	@Test
	void constructListGenerics() {
		TypeReference<List<String>> actual = new TypeReference<List<String>>() {
		};

		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual.getType();
		then(parameterizedType.getRawType()).isEqualTo(List.class);
		then(parameterizedType.getActualTypeArguments()[0]).isEqualTo(String.class);
	}

	@Test
	void constructMap() {
		TypeReference<Map<Integer, String>> actual = new TypeReference<Map<Integer, String>>() {
		};

		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual.getType();
		then(parameterizedType.getRawType()).isEqualTo(Map.class);
		then(parameterizedType.getActualTypeArguments()[0]).isEqualTo(Integer.class);
		then(parameterizedType.getActualTypeArguments()[1]).isEqualTo(String.class);
	}
}
