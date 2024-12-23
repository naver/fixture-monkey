package com.navercorp.fixturemonkey.jakarta.validation.spec;

import java.math.BigDecimal;

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

	@DecimalMin(value = "100.1")
	@DecimalMax(value = "100.1")
	private BigDecimal decimalEqual;

	@Max(value = 100)
	@Min(value = 100)
	private BigDecimal integerEqual;
}
