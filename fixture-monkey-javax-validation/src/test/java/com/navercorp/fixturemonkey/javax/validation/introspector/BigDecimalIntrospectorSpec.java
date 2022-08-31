package com.navercorp.fixturemonkey.javax.validation.introspector;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

public class BigDecimalIntrospectorSpec {
	private BigDecimal bigDecimalValue;

	@Digits(integer = 3, fraction = 0)
	private BigDecimal digitsValue;

	@Min(100)
	private BigDecimal minValue;

	@Max(100)
	private BigDecimal maxValue;

	@DecimalMin(value = "100.1")
	private BigDecimal decimalMin;

	@DecimalMin(value = "100.1", inclusive = false)
	private BigDecimal decimalMinExclusive;

	@DecimalMax(value = "100.1")
	private BigDecimal decimalMax;

	@DecimalMax(value = "100.1", inclusive = false)
	private BigDecimal decimalMaxExclusive;

	@Negative
	private BigDecimal negative;

	@NegativeOrZero
	private BigDecimal negativeOrZero;

	@Positive
	private BigDecimal positive;

	@PositiveOrZero
	private BigDecimal positiveOrZero;
}
