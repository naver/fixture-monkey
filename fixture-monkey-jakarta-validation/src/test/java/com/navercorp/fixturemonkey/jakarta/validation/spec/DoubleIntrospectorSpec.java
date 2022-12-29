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
