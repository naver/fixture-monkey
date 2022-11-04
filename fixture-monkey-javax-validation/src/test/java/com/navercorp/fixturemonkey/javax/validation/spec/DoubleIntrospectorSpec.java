package com.navercorp.fixturemonkey.javax.validation.spec;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoubleIntrospectorSpec {
	private double doubleValue;

	@Digits(integer = 3, fraction = 0)
	private double digitsValue;

	@Min(100)
	private double minValue;

	@Max(100)
	private double maxValue;

	@DecimalMin(value = "100.1")
	private double decimalMin;

	@DecimalMin(value = "100.1", inclusive = false)
	private double decimalMinExclusive;

	@DecimalMax(value = "100.1")
	private double decimalMax;

	@DecimalMax(value = "100.1", inclusive = false)
	private double decimalMaxExclusive;

	@Negative
	private double negative;

	@NegativeOrZero
	private double negativeOrZero;

	@Positive
	private double positive;

	@PositiveOrZero
	private double positiveOrZero;
}
