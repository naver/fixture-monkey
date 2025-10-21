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

import java.lang.reflect.Field;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class FieldPropertyTest {
	@Test
	void getField() throws NoSuchFieldException {
		Field field = PropertyValue.class.getDeclaredField("name");
		FieldProperty sut = new FieldProperty(field);
		then(sut.getField()).isSameAs(field);
	}

	@Test
	void getAnnotations() throws NoSuchFieldException {
		Field field = PropertyValue.class.getDeclaredField("name");
		FieldProperty sut = new FieldProperty(field);
		then(sut.getAnnotations()).hasSize(1);
		then(sut.getAnnotations().get(0).annotationType()).isEqualTo(Nullable.class);
	}

	@Test
	void getAnnotation() throws NoSuchFieldException {
		Field field = PropertyValue.class.getDeclaredField("name");
		FieldProperty sut = new FieldProperty(field);
		then(sut.getAnnotation(Nullable.class)).isPresent();
	}

	@Test
	void getAnnotationNotFound() throws NoSuchFieldException {
		Field field = PropertyValue.class.getDeclaredField("name");
		FieldProperty sut = new FieldProperty(field);
		then(sut.getAnnotation(NonNull.class)).isEmpty();
	}

	@Test
	void getName() throws NoSuchFieldException {
		Field field = PropertyValue.class.getDeclaredField("name");
		FieldProperty sut = new FieldProperty(field);
		then(sut.getName()).isEqualTo("name");
	}

	@Test
	void getAnnotatedType() throws NoSuchFieldException {
		Field field = PropertyValue.class.getDeclaredField("name");
		FieldProperty sut = new FieldProperty(field);
		then(sut.getAnnotatedType().getType()).isEqualTo(String.class);
	}

	@Test
	void getValue() throws NoSuchFieldException {
		Field field = PropertyValue.class.getDeclaredField("name");
		FieldProperty sut = new FieldProperty(field);

		PropertyValue propertyValue = new PropertyValue("hello world");
		then(sut.getValue(propertyValue)).isEqualTo("hello world");
	}
}
