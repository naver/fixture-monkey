package com.navercorp.fixturemonkey.javax.validation.spec;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FractionalDecimalIntrospectorSpec {
	@DecimalMin("10.5")
	@DecimalMax("11")
	private long longMin;

	@DecimalMin("10")
	@DecimalMax("10.5")
	private long longMax;

	@DecimalMin(value = "10.5", inclusive = false)
	@DecimalMax("11")
	private long longMinExclusive;

	@DecimalMin("10")
	@DecimalMax(value = "10.5", inclusive = false)
	private long longMaxExclusive;

	@DecimalMin("1000.00")
	@DecimalMax("1000")
	private int intMin;

	@DecimalMin("100")
	@DecimalMax("1E+2")
	private int intMaxExponent;

	@DecimalMin("-11")
	@DecimalMax("-10.5")
	private short shortMax;
}
