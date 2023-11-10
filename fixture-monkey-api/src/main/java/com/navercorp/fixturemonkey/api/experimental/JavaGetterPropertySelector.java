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

import java.util.Arrays;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator;
import com.navercorp.fixturemonkey.api.property.PropertySelector;

@API(since = "1.0.0", status = Status.EXPERIMENTAL)
interface JavaGetterPropertySelector<T, U> extends PropertySelector, ExpressionGenerator {
	default <R> JoinJavaGetterPropertySelector<U, R> into(JavaGetterMethodReference<U, R> methodReference) {
		JavaGetterMethodPropertySelector<U, R> next =
			JavaGetterPropertySelectors.resolvePropertySelector(methodReference);

		return new JoinJavaGetterPropertySelector<>(
			Arrays.asList(
				this,
				propertyNameResolver -> ".",
				next
			)
		);
	}

	default <E> JoinJavaGetterPropertySelector<U, E> index(Class<E> elementType, int index) {
		return new JoinJavaGetterPropertySelector<U, E>(
			Arrays.asList(
				this,
				propertyNameResolver -> "[" + index + "]"
			)
		);
	}

	default <E> JoinJavaGetterPropertySelector<U, E> allIndex(Class<E> elementType) {
		return new JoinJavaGetterPropertySelector<U, E>(
			Arrays.asList(
				this,
				propertyNameResolver -> "[*]"
			)
		);
	}
}
