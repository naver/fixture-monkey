package com.navercorp.fixturemonkey.jakarta.validation.spec;

import java.math.BigInteger;

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
