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

package com.navercorp.fixturemonkey.customizer;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Provides static methods or constants for extensions of ArbitraryBuilder {@code set}.
 */
@API(since = "0.5.1", status = Status.MAINTAINED)
public final class Values {
	public static final Object NOT_NULL = new Object();

	private Values() {
	}

	public static Just just(@Nullable Object value) {
		return new Just(value);
	}

	/**
	 * An instance wrapped by {@code Just} represents setting value directly instead of decomposing
	 * {@link NodeSetDecomposedValueManipulator}.
	 * Setting a property in ArbitraryBuilder does not use an instance of given value, it performs a deep copy.
	 * If you would like to set an instance of value, use {@code set("expression", Values.just(value))}
	 * Most common example would be setting a property to a mock instance when using mocking framework.
	 * <p>
	 * After setting an instance, you could not set a child property.
	 * For example,
	 * <pre>{@code
	 * Order order = fixture.giveMeBuilder(Order.class)
	 * 		.set("items", Values.just(List.of("1","2","3"))
	 * 		.set("items[0]", "0")
	 * 		.sample();
	 * }</pre>
	 * <p>
	 * Elements of {@code items} would be "1", "2", "3".
	 */
	public static final class Just {
		@Nullable
		private final Object value;

		private Just(@Nullable Object value) {
			this.value = value;
		}

		@Nullable
		public Object getValue() {
			return value;
		}
	}
}
