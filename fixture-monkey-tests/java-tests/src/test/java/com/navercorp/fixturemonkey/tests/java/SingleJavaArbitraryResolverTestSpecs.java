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

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.Past;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;

class SingleJavaArbitraryResolverTestSpecs {
	@Getter
	@Setter
	public static class IntObject {
		@Min(100)
		private int javaxMinValue;

		@Max(100)
		private int jakartaMaxValue;
	}

	@Getter
	@Setter
	public static class DateObject {
		@Past
		private Date javaxDatePast;

		@Future
		private Date jakartaDateFuture;
	}
}
