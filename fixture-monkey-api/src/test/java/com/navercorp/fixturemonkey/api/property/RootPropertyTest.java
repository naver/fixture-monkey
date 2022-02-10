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

package com.navercorp.fixturemonkey.api.property;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.AnnotatedType;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.type.TypeReference;

class RootPropertyTest {
	@Test
	void construct() {
		TypeReference<PropertyValue> typeReference = new TypeReference<PropertyValue>() {
		};
		RootProperty sut = new RootProperty(typeReference.getType());

		then(sut.getType()).isEqualTo(typeReference.getType());

		AnnotatedType annotatedType = sut.getAnnotatedType();
		then(annotatedType.getType()).isEqualTo(typeReference.getType());

		then(sut.getAnnotations()).isEmpty();
		then(sut.getName()).isEqualTo("$");
	}
}
