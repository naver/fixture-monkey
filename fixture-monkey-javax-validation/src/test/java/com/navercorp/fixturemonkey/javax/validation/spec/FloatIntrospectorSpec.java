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
public class FloatIntrospectorSpec {
	private float floatValue;

	@Digits(integer = 3, fraction = 0)
	private float digitsValue;

	@Min(100)
	private float minValue;

	@Max(100)
	private float maxValue;

	@DecimalMin(value = "100.1")
	private float decimalMin;

	@DecimalMin(value = "100.1", inclusive = false)
	private float decimalMinExclusive;

	@DecimalMax(value = "100.1")
	private float decimalMax;

	@DecimalMax(value = "100.1", inclusive = false)
	private float decimalMaxExclusive;

	@Negative
	private float negative;

	@NegativeOrZero
	private float negativeOrZero;

	@Positive
	private float positive;

	@PositiveOrZero
	private float positiveOrZero;
}
