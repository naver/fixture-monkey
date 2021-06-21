package com.navercorp.fixturemonkey.arbitrary;

import java.util.List;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression.Exp;

interface PriorityManipulator extends ArbitraryExpressionManipulator, Comparable<PriorityManipulator> {
	Priority getPriority();

	@Override
	default int compareTo(PriorityManipulator o) {
		List<Exp> expList = getArbitraryExpression().getExpList();
		List<Exp> oExpList = o.getArbitraryExpression().getExpList();

		int priorityCompare = Integer.compare(this.getPriority().ordinal(), o.getPriority().ordinal());
		if (priorityCompare != 0) {
			return priorityCompare;
		}

		int compare = Integer.compare(expList.size(), oExpList.size());
		if (compare != 0) {
			return compare;
		}

		int length = expList.size();
		for (int i = 0; i < length; i++) {
			Exp exp = expList.get(i);
			Exp oExp = oExpList.get(i);
			int expIndexSize = exp.getIndex().size();
			int oExpIndexSize = oExp.getIndex().size();

			if (expIndexSize != oExpIndexSize) {
				return Integer.compare(expIndexSize, oExpIndexSize);
			}
		}
		return 0;
	}

	enum Priority {
		HIGH,
		LOW
	}
}
