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
public class IntIntrospectorSpec {
	private int intValue;

	@Digits(integer = 3, fraction = 0)
	private int digitsValue;

	@Min(100)
	private int minValue;

	@Max(100)
	private int maxValue;

	@DecimalMin(value = "100")
	private int decimalMin;

	@DecimalMin(value = "100", inclusive = false)
	private int decimalMinExclusive;

	@DecimalMax(value = "100")
	private int decimalMax;

	@DecimalMax(value = "100", inclusive = false)
	private int decimalMaxExclusive;

	@Negative
	private int negative;

	@NegativeOrZero
	private int negativeOrZero;

	@Positive
	private int positive;

	@PositiveOrZero
	private int positiveOrZero;
}
