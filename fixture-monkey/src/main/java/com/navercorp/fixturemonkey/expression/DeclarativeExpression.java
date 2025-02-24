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

package com.navercorp.fixturemonkey.expression;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.PropertySelector;

/**
 * It is a public interface to declare a declarative expression.
 * It is used to declare a declarative expression that is used to customize a fixture by user.
 */
@API(since = "1.1.10", status = Status.EXPERIMENTAL)
public interface DeclarativeExpression extends PropertySelector {
	/**
	 * Declares a directive expression that contains the property.
	 *
	 * @param propertyName The name of the property.
	 * @return A directive expression contains the property.
	 */
	DeclarativeExpression property(String propertyName);

	/**
	 * Declares a directive expression that contains the element.
	 *
	 * @param sequence The sequence of the element.
	 * @return A directive expression contains the element.
	 */
	DeclarativeExpression element(int sequence);

	/**
	 * Declares a directive expression that contains all elements.
	 *
	 * @return A directive expression contains all elements.
	 */
	DeclarativeExpression allElement();

	/**
	 * Declares a directive expression that contains the key.
	 *
	 * @return A directive expression contains the key.
	 */
	DeclarativeExpression key();

	/**
	 * Declares a directive expression that contains the value.
	 *
	 * @return A directive expression contains the value.
	 */
	DeclarativeExpression value();
}
