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

package com.navercorp.fixturemonkey.tests.java.specs;

import lombok.Value;

public class DefaultNullInjectGeneratorSpecs {
	@Value
	public static class NonNullAnnotationObject {
		@javax.validation.constraints.NotNull
		String javaxNonNullField;

		@org.jspecify.annotations.NonNull
		String jspecifyNonNullField;

		@org.checkerframework.checker.nullness.qual.NonNull
		String checkerNonNullField;
	}

	@Value
	public static class NullableAnnotationObject {
		@javax.annotation.Nullable
		String javaxNullableField;

		@org.jspecify.annotations.Nullable
		String jspecifyNullableField;

		@org.checkerframework.checker.nullness.qual.Nullable
		String checkerNullableField;
	}
}
