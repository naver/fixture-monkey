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

class ByteIntrospectorSpec {
	private byte byteValue;

	@Digits(integer = 2, fraction = 0)
	private byte digitsValue;

	@Min(100)
	private byte minValue;

	@Max(100)
	private byte maxValue;

	@DecimalMin(value = "100")
	private byte decimalMin;

	@DecimalMin(value = "100", inclusive = false)
	private byte decimalMinExclusive;

	@DecimalMax(value = "100")
	private byte decimalMax;

	@DecimalMax(value = "100", inclusive = false)
	private byte decimalMaxExclusive;

	@Negative
	private byte negative;

	@NegativeOrZero
	private byte negativeOrZero;

	@Positive
	private byte positive;

	@PositiveOrZero
	private byte positiveOrZero;
}
