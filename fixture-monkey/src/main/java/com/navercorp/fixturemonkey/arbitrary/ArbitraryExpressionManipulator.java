package com.navercorp.fixturemonkey.arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;

interface ArbitraryExpressionManipulator {
	ArbitraryExpression getArbitraryExpression();

	void addPrefix(String expression);
}
