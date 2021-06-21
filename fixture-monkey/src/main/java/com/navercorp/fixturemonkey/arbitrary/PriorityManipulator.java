/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.arbitrary;

import java.util.List;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression.Exp;

interface PriorityManipulator extends ArbitraryExpressionManipulator, Comparable<PriorityManipulator> {
	Priority getPriority();

	@Override
	default int compareTo(PriorityManipulator priorityManipulator) {
		List<Exp> expList = getArbitraryExpression().getExpList();
		List<Exp> oExpList = priorityManipulator.getArbitraryExpression().getExpList();

		int priorityCompare =
			Integer.compare(this.getPriority().ordinal(), priorityManipulator.getPriority().ordinal());
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
