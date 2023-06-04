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

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

class JakartaValidationTestSpecs {
	@Getter
	@Setter
	public static class IntObject {
		@Min(100)
		private int minValue;
	}

	@Getter
	@Setter
	public static class DateObject {
		@Past
		private Date datePast;
	}

	@Getter
	@Setter
	public static class ContainerObject {
		@Size(min = 5, max = 10)
		private List<String> strList;
	}

	@Getter
	@Setter
	public static class BooleanObject {
		@AssertTrue
		private Boolean bool;
	}
}
