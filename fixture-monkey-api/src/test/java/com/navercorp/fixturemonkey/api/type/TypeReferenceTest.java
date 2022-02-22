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

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.jqwik.api.constraints.Size;

class TypeReferenceTest {

	@Test
	void construct() {
		TypeReference<String> actual = new TypeReference<@Size String>() {
		};
		then(actual.getType()).isEqualTo(String.class);
		then(actual.getAnnotatedType().getType()).isEqualTo(String.class);
		then(actual.getAnnotatedType().getAnnotations()[0].annotationType()).isEqualTo(Size.class);
	}

	@Test
	void constructClassType() {
		Class<?> type = String.class;
		TypeReference<?> actual = new TypeReference(type) {
		};
		then(actual.getType()).isEqualTo(String.class);
		then(actual.getAnnotatedType().getType()).isEqualTo(String.class);
	}

	@Test
	void constructListGenerics() {
		TypeReference<List<String>> actual = new TypeReference<List<@Size String>>() {
		};

		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual.getType();
		then(parameterizedType.getRawType()).isEqualTo(List.class);
		then(parameterizedType.getActualTypeArguments()[0]).isEqualTo(String.class);

		AnnotatedParameterizedType annotatedParameterizedType = (AnnotatedParameterizedType)actual.getAnnotatedType();
		then(annotatedParameterizedType.getType()).isEqualTo(actual.getType());
		then(annotatedParameterizedType.getAnnotatedActualTypeArguments()[0].getType()).isEqualTo(String.class);
		then(annotatedParameterizedType.getAnnotatedActualTypeArguments()[0].getAnnotations()[0].annotationType())
			.isEqualTo(Size.class);
	}

	@Test
	void constructMap() {
		TypeReference<Map<Integer, String>> actual = new TypeReference<Map<Integer, @Size String>>() {
		};

		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual.getType();
		then(parameterizedType.getRawType()).isEqualTo(Map.class);
		then(parameterizedType.getActualTypeArguments()[0]).isEqualTo(Integer.class);
		then(parameterizedType.getActualTypeArguments()[1]).isEqualTo(String.class);

		AnnotatedParameterizedType annotatedParameterizedType = (AnnotatedParameterizedType)actual.getAnnotatedType();
		then(annotatedParameterizedType.getType()).isEqualTo(actual.getType());
		then(annotatedParameterizedType.getAnnotatedActualTypeArguments()[0].getType()).isEqualTo(Integer.class);
		then(annotatedParameterizedType.getAnnotatedActualTypeArguments()[1].getType()).isEqualTo(String.class);
		then(annotatedParameterizedType.getAnnotatedActualTypeArguments()[1].getAnnotations()[0].annotationType())
			.isEqualTo(Size.class);
	}
}
