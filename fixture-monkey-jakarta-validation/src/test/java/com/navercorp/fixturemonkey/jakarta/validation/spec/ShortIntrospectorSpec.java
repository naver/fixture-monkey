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

package com.navercorp.fixturemonkey.jakarta.validation.spec;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortIntrospectorSpec {
	private short shortValue;

	@Digits(integer = 3, fraction = 0)
	private short digitsValue;

	@Min(100)
	private short minValue;

	@Max(100)
	private short maxValue;

	@DecimalMin(value = "100")
	private short decimalMin;

	@DecimalMin(value = "100", inclusive = false)
	private short decimalMinExclusive;

	@DecimalMax(value = "100")
	private short decimalMax;

	@DecimalMax(value = "100", inclusive = false)
	private short decimalMaxExclusive;

	@Negative
	private short negative;

	@NegativeOrZero
	private short negativeOrZero;

	@Positive
	private short positive;

	@PositiveOrZero
	private short positiveOrZero;
}
