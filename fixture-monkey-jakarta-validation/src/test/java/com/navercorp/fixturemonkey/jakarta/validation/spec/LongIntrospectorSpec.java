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
public class LongIntrospectorSpec {
	private long longValue;

	@Digits(integer = 3, fraction = 0)
	private long digitsValue;

	@Min(100)
	private long minValue;

	@Max(100)
	private long maxValue;

	@DecimalMin(value = "100")
	private long decimalMin;

	@DecimalMin(value = "100", inclusive = false)
	private long decimalMinExclusive;

	@DecimalMax(value = "100")
	private long decimalMax;

	@DecimalMax(value = "100", inclusive = false)
	private long decimalMaxExclusive;

	@Negative
	private long negative;

	@NegativeOrZero
	private long negativeOrZero;

	@Positive
	private long positive;

	@PositiveOrZero
	private long positiveOrZero;
}
