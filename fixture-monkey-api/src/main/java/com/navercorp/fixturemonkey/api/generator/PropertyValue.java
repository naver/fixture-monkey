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

package com.navercorp.fixturemonkey.api.generator;

import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class PropertyValue {
	private Object value;
	private final Supplier<?> supplier;
	private final boolean fixed;

	public PropertyValue(Supplier<?> supplier) {
		this(supplier, false);
	}

	public PropertyValue(Supplier<?> supplier, boolean fixed) {
		this.supplier = supplier;
		this.fixed = fixed;
	}

	public Object get() {
		if (this.value == null) {
			this.value = supplier.get();
		}
		return value;
	}

	public boolean isEmpty() {
		return get() == null;
	}

	public boolean isFixed() {
		return this.fixed;
	}
}
