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

package com.navercorp.fixturemonkey.tests.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import lombok.Getter;

import com.navercorp.fixturemonkey.api.expression.JavaGetterPropertyFieldNameResolver;

class JavaGetterPropertyFieldNameResolverTest {

	private final JavaGetterPropertyFieldNameResolver resolver = new JavaGetterPropertyFieldNameResolver();

	@Test
	void testNonBooleanFieldWithIsPrefix() {
		assertDoesNotThrow(() -> {
			Method method = TestClass.class.getDeclaredMethod("getIsStatus");
			String fieldName = resolver.resolveFieldName(TestClass.class, method.getName());
			assertEquals("isStatus", fieldName,
				"Should strip 'get' from a non-boolean getter with the 'is' prefix.");
		}, "Method 'getIsStatus' should exist and not throw an exception.");
	}

	@Test
	void testPrimitiveTypeBooleanFieldWithIsPrefix() {
		assertDoesNotThrow(() -> {
			Method method = TestClass.class.getDeclaredMethod("isActive");
			String fieldName = resolver.resolveFieldName(TestClass.class, method.getName());
			assertEquals("isActive", fieldName,
				"Should not strip the 'is' prefix from a getter for a primitive boolean field.");
		}, "Method 'isActive' should exist and not throw an exception.");
	}

	@Test
	void testBooleanFieldWithoutIsPrefix() {
		assertDoesNotThrow(() -> {
			Method method = TestClass.class.getDeclaredMethod("isEnabled");
			String fieldName = resolver.resolveFieldName(TestClass.class, method.getName());
			assertEquals(
				"enabled", fieldName,
				"Should strip the 'is' prefix from "
					+ "a getter for a boolean field without the 'is' prefix in the field name.");
		}, "Method 'isEnabled' should exist and not throw an exception.");
	}

	@Test
	void testNonBooleanFieldWithoutIsPrefix() {
		assertDoesNotThrow(() -> {
			Method method = TestClass.class.getDeclaredMethod("getName");
			String fieldName = resolver.resolveFieldName(TestClass.class, method.getName());
			assertEquals("name", fieldName,
				"Should strip 'get' from a getter for a non-boolean field without the 'is' prefix.");
		}, "Method 'getName' should exist and not throw an exception.");
	}

	@Test
	void testWrapperTypeBooleanFieldWithIsPrefix() {
		assertDoesNotThrow(() -> {
			Method method = TestClass.class.getDeclaredMethod("getIsDeleted");
			String fieldName = resolver.resolveFieldName(TestClass.class, method.getName());
			assertEquals("isDeleted", fieldName,
				"Should strip 'get' from a getter for a wrapper type Boolean field with the 'is' prefix.");
		}, "Method 'getIsDeleted' should exist and not throw an exception.");
	}

	@Getter
	private static class TestClass {
		private String isStatus;
		private boolean isActive;
		private boolean enabled;
		private String name;
		private Boolean isDeleted;
	}
}
