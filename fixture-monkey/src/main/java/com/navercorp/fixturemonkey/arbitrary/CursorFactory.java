package com.navercorp.fixturemonkey.arbitrary;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression.Exp;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression.ExpIndex;

class CursorFactory {

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
