package com.navercorp.fixturemonkey;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression.ExpIndex;

public class Constants {
	public static final int DEFAULT_ELEMENT_MIN_SIZE = 0;
	public static final int DEFAULT_ELEMENT_MIN_LIMIT = 0;
	public static final int DEFAULT_ELEMENT_MAX_SIZE = 3;
	public static final int DEFAULT_ELEMENT_MAX_LIMIT = 100;
	public static final int NO_OR_ALL_INDEX_INTEGER_VALUE = Integer.MAX_VALUE;
	public static final String ALL_INDEX_STRING = "*";
	public static final ExpIndex ALL_INDEX_EXP_INDEX = new ExpIndex(NO_OR_ALL_INDEX_INTEGER_VALUE);
}
