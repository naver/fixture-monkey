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

package com.navercorp.fixturemonkey.api.experimental;

/**
 * It is deprecated. Use {@link com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector} instead.
 */
@Deprecated
public interface TypedExpressionGenerator<T> {
	@Deprecated
	static <U> com.navercorp.fixturemonkey.api.expression.TypedExpressionGenerator<U> typedRoot() {
		return com.navercorp.fixturemonkey.api.expression.TypedExpressionGenerator.typedRoot();
	}

	@Deprecated
	static <U> com.navercorp.fixturemonkey.api.expression.TypedExpressionGenerator<U> typedString(String expression) {
		return com.navercorp.fixturemonkey.api.expression.TypedExpressionGenerator.typedString(expression);
	}
}
