package com.navercorp.fixturemonkey.javax.validation.spec;

import java.math.BigInteger;

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
public class BigIntegerIntrospectorSpec {
	private BigInteger bigIntegerValue;

	@Digits(integer = 3, fraction = 0)
	private BigInteger digitsValue;

	@Min(100)
	private BigInteger minValue;

	@Max(100)
	private BigInteger maxValue;

	@DecimalMin(value = "100")
	private BigInteger decimalMin;

	@DecimalMin(value = "100", inclusive = false)
	private BigInteger decimalMinExclusive;

	@DecimalMax(value = "100")
	private BigInteger decimalMax;

	@DecimalMax(value = "100", inclusive = false)
	private BigInteger decimalMaxExclusive;

	@Negative
	private BigInteger negative;

	@NegativeOrZero
	private BigInteger negativeOrZero;

	@Positive
	private BigInteger positive;

	@PositiveOrZero
	private BigInteger positiveOrZero;
}
