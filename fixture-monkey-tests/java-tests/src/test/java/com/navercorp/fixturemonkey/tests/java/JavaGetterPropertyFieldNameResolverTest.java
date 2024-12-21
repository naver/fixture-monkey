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

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import lombok.Getter;

import com.navercorp.fixturemonkey.api.expression.JavaGetterPropertyFieldNameResolver;

class JavaGetterPropertyFieldNameResolverTest {

	private final JavaGetterPropertyFieldNameResolver sut = new JavaGetterPropertyFieldNameResolver();

	@Test
	void testNonBooleanFieldWithIsPrefix() throws NoSuchMethodException {
		Method method = JavaGetterObject.class.getDeclaredMethod("getIsStatus");

		then(sut.resolveFieldName(JavaGetterObject.class, method.getName())).isEqualTo("isStatus");
	}

	@Test
	void testPrimitiveTypeBooleanFieldWithIsPrefix() throws NoSuchMethodException {
		Method method = JavaGetterObject.class.getDeclaredMethod("isActive");

		then(sut.resolveFieldName(JavaGetterObject.class, method.getName())).isEqualTo("isActive");
	}

	@Test
	void testBooleanFieldWithoutIsPrefix() throws NoSuchMethodException {
		Method method = JavaGetterObject.class.getDeclaredMethod("isEnabled");

		then(sut.resolveFieldName(JavaGetterObject.class, method.getName())).isEqualTo("enabled");
	}

	@Test
	void testNonBooleanFieldWithoutIsPrefix() throws NoSuchMethodException {
		Method method = JavaGetterObject.class.getDeclaredMethod("getName");

		then(sut.resolveFieldName(JavaGetterObject.class, method.getName())).isEqualTo("name");
	}

	@Test
	void testWrapperTypeBooleanFieldWithIsPrefix() throws NoSuchMethodException {
		Method method = JavaGetterObject.class.getDeclaredMethod("getIsDeleted");

		then(sut.resolveFieldName(JavaGetterObject.class, method.getName())).isEqualTo("isDeleted");
	}

	@Getter
	private static class JavaGetterObject {
		private String isStatus;
		private boolean isActive;
		private boolean enabled;
		private String name;
		private Boolean isDeleted;
	}
}
