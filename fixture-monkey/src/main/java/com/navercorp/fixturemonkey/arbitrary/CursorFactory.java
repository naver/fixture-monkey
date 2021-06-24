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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression.Exp;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression.ExpIndex;

final class CursorFactory {

	public static List<Cursor> create(Exp exp) {
		List<Cursor> steps = new ArrayList<>();
		String expName = exp.getName();
		steps.add(new ExpNameCursor(expName));
		steps.addAll(exp.getIndex().stream()
			.map(it -> CursorFactory.create(expName, it))
			.collect(toList()));
		return steps;
	}

	public static Cursor create(String expName, ExpIndex expIndex) {
		return new ExpIndexCursor(expName, expIndex.getIndex());
	}

	public static List<Cursor> create(ArbitraryExpression arbitraryExpression) {
		return arbitraryExpression.getExpList().stream()
			.map(CursorFactory::create)
			.reduce(new ArrayList<>(), (list1, list2) -> {
				list1.addAll(list2);
				return list1;
			});
	}
}
