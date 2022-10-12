package com.navercorp.fixturemonkey.javax.validation.introspector;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

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
