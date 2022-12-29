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
