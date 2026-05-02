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

package com.navercorp.objectfarm.api.input;

import org.jspecify.annotations.Nullable;

/**
 * Represents a field extracted from an object, containing both the runtime value
 * and the declared type of the field.
 */
public final class ExtractedField {
	private final @Nullable Object value;
	private final Class<?> declaredType;

	public ExtractedField(@Nullable Object value, Class<?> declaredType) {
		this.value = value;
		this.declaredType = declaredType;
	}

	public @Nullable Object getValue() {
		return value;
	}

	public Class<?> getDeclaredType() {
		return declaredType;
	}
}
