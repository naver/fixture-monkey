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

import static com.navercorp.fixturemonkey.Constants.ALL_INDEX_STRING;
import static com.navercorp.fixturemonkey.Constants.HEAD_NAME;
import static com.navercorp.fixturemonkey.Constants.KEY_ANY_INTEGER_VALUE;
import static com.navercorp.fixturemonkey.Constants.KEY_INDEX_INTEGER_VALUE;
import static com.navercorp.fixturemonkey.Constants.NO_OR_ALL_INDEX_INTEGER_VALUE;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;

public final class NewArbitraryExpression implements Comparable<NewArbitraryExpression> {
	private final List<Exp> expList;

	private NewArbitraryExpression(List<Exp> expList) {
		this.expList = expList;
	}

	private NewArbitraryExpression(String expression) {
		expList = Arrays.stream(expression.split("\\."))
			.map(it -> new Exp(it, null, null))
			.collect(toList());
	}

	private NewArbitraryExpression(String expression, List<Object> keys, List<Boolean> isSetKey) {
		expList = Arrays.stream(expression.split("\\."))
			//Todo: keys, isSetKey를 잘라서 넣어야함
			.map(it -> new Exp(it, keys, isSetKey))
			.collect(toList());
	}

	public static NewArbitraryExpression from(String expression) {
		return new NewArbitraryExpression(expression);
	}

	public static NewArbitraryExpression from(String expression, List<Object> keys, List<Boolean> isSetKey) {
		return new NewArbitraryExpression(expression, keys, isSetKey);
	}

	public NewArbitraryExpression addFirst(String expression) {
		String newStringExpression = expression + "." + this;
		return new NewArbitraryExpression(newStringExpression);
	}

	public NewArbitraryExpression addLast(String expression) {
		String newStringExpression = this + "." + expression;
		return new NewArbitraryExpression(newStringExpression);
	}

	@API(since = "0.4.0", status = Status.EXPERIMENTAL)
	public NewArbitraryExpression pollLast() {
		//Todo:
		if (expList.isEmpty()) {
			return this;
		}

		List<Exp> newExpList = new ArrayList<>(this.expList);
		// int lastIndex = newExpList.size() - 1;
		// Exp lastExp = newExpList.get(lastIndex);
		// newExpList.remove(lastIndex);
		//
		// if (!lastExp.index.isEmpty()) {
		// 	List<ExpIndex> newExpIndexList = new ArrayList<>(lastExp.index);
		// 	newExpIndexList.remove(newExpIndexList.size() - 1);
		// 	lastExp = new Exp(lastExp.name, newExpIndexList);
		// 	newExpList.add(lastExp);
		// }
		return new NewArbitraryExpression(newExpList);
	}

	@Override
	public int compareTo(NewArbitraryExpression arbitraryExpression) {
		List<Exp> oExpList = arbitraryExpression.expList;

		if (expList.size() != oExpList.size()) {
			return Integer.compare(expList.size(), oExpList.size());
		}

		for (int i = 0; i < expList.size(); i++) {
			Exp exp = expList.get(i);
			Exp oExp = oExpList.get(i);
			int expCompare = exp.compareTo(oExp);
			if (expCompare != 0) {
				return expCompare;
			}
		}

		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		NewArbitraryExpression other = (NewArbitraryExpression)obj;
		return expList.equals(other.expList);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.expList);
	}

	public String toString() {
		return expList.stream()
			.map(Exp::toString)
			.collect(Collectors.joining("."));
	}

	public List<Cursor> toCursors() {
		return this.expList.stream()
			.flatMap(it -> it.toCursors().stream())
			.filter(Cursor::isNotHeadName)
			.collect(toList());
	}

	private static final class ExpIndex implements Comparable<ExpIndex>, ExpPath {
		public static final ExpIndex ALL_INDEX_EXP_INDEX = new ExpIndex(NO_OR_ALL_INDEX_INTEGER_VALUE);

		private final int index;

		public ExpIndex(int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		public boolean equalsIgnoreAllIndex(ExpIndex expIndex) {
			return this.index == expIndex.index;
		}

		@Override
		public int compareTo(ExpIndex expIndex) {
			return Integer.compare(this.index, expIndex.index);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			ExpIndex expIndex = (ExpIndex)obj;
			return index == expIndex.index || index == NO_OR_ALL_INDEX_INTEGER_VALUE
				|| expIndex.index == NO_OR_ALL_INDEX_INTEGER_VALUE;
		}

		@Override
		public int hashCode() {
			return 0; // for allIndex, hash always return 0.
		}

		public String toString() {
			return index == NO_OR_ALL_INDEX_INTEGER_VALUE ? ALL_INDEX_STRING : String.valueOf(index);
		}
	}

	private static final class ExpKey implements Comparable<ExpKey>, ExpPath {
		private final Object key;
		private final Boolean isSetKey;

		public ExpKey(Object key, boolean isSetKey) {
			this.key = key;
			this.isSetKey = isSetKey;
		}

		public Object getKey() {
			return key;
		}

		@Override
		public int compareTo(ExpKey expKey) {
			// Object인 key는 어떻게 Compare?
			return Boolean.compare(this.isSetKey, expKey.isSetKey);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			ExpKey expKey = (ExpKey)obj;
			return key.equals(expKey.key) && isSetKey.equals(expKey.isSetKey);
		}

		@Override
		public int hashCode() {
			return 0;
		}

		public String toString() {
			return "[]";
		}
	}
	private interface ExpPath {
		String toString();

		Cursor toCursor(); // Index, NameCursor, MapCursor
	}

	private static final class Exp implements Comparable<Exp> {
		private final String name;
		private final List<ExpPath> path;

		private Exp(String name, List<ExpPath> path) {
			this.name = name;
			this.path = path;
		}

		public Exp(String expression, List<Object> keys, List<Boolean> isSetKey) {
			this.path = new ArrayList<>();
			int li = expression.indexOf('[');
			int ri = expression.indexOf(']');
			int keyCnt = 0;
			if ((li != -1 && ri == -1) || (li == -1 && ri != -1)) {
				throw new IllegalArgumentException("expression is invalid. expression : " + expression);
			}

			if (li == -1) {
				this.name = expression;
			} else {
				this.name = expression.substring(0, li);
				while (li != -1 && ri != -1) {
					if (ri - li > 1) {
						String indexString = expression.substring(li + 1, ri);
						//* 인 경우
						if (indexString.equals(ALL_INDEX_STRING)) {
							this.path.add(new ExpIndex(NO_OR_ALL_INDEX_INTEGER_VALUE));
						}
						//?인 경우
						else if (indexString.equals("?")) {
							this.path.add(new ExpKey(keys.get(keyCnt), isSetKey.get(keyCnt)));
							keyCnt++;
						}
						//Index일 경우
						else {
							this.path.add(new ExpIndex(Integer.parseInt(indexString)));
						}
					} //Key일 경우
					else if (ri - li == 1) {
						this.path.add(new ExpKey(keys.get(keyCnt), isSetKey.get(keyCnt)));
						keyCnt++;
					}
					expression = expression.substring(ri + 1);
					li = expression.indexOf('[');
					ri = expression.indexOf(']');
				}
			}
		}

		public List<Cursor> toCursors() {
			List<Cursor> steps = new ArrayList<>();
			String expName = this.getName();
			steps.add(new ExpNameCursor(expName));
			steps.addAll(this.getPath().stream()
				.map(ExpPath::toCursor)
				.collect(toList()));
			return steps;
		}

		public String getName() {
			return name;
		}

		public List<ExpPath> getPath() {
			return path;
		}

		public String toString() {
			String indexBrackets = path.stream()
				.map(i -> "[" + i.toString() + "]")
				.collect(Collectors.joining());
			return name + indexBrackets;
		}

		@Override
		public int compareTo(Exp exp) {
			//ToDo:
			return 0;
			// List<ExpIndex> indices = this.getIndex();
			// List<ExpIndex> oIndices = exp.getIndex();
			//
			// if (exp.name.equals(this.name)) {
			// 	int indexLength = Math.min(oIndices.size(), indices.size());
			// 	for (int i = 0; i < indexLength; i++) {
			// 		ExpIndex index = indices.get(i);
			// 		ExpIndex oIndex = oIndices.get(i);
			// 		int indexCompare = oIndex.compareTo(index);
			// 		if (indexCompare != 0) {
			// 			return indexCompare;
			// 		}
			// 	}
			// }
			// return Integer.compare(indices.size(), oIndices.size());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			Exp exp = (Exp)obj;
			return name.equals(exp.name) && path.equals(exp.path);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, path);
		}
	}

	public abstract static class Cursor {
		private final String name;
		private final int index;
		public Cursor(String name, int index) {
			this.name = name;
			this.index = index;
		}

		public boolean match(ArbitraryProperty arbitraryProperty) {
			boolean samePropertyName = nameEquals(arbitraryProperty.getResolvePropertyName());
			boolean sameIndex = true;
			if (arbitraryProperty.getElementIndex() != null) {
				sameIndex = indexEquals(arbitraryProperty.getElementIndex()); // notNull
			}
			return samePropertyName && sameIndex;
		}

		//Todo: match method for Key

		public boolean isNotHeadName() {
			return !(this instanceof ExpNameCursor) || !HEAD_NAME.equals(this.getName());
		}

		private boolean indexEquals(int index) {
			return this.index == index
				|| index == NO_OR_ALL_INDEX_INTEGER_VALUE
				|| this.index == NO_OR_ALL_INDEX_INTEGER_VALUE;
		}

		private boolean nameEquals(String name) {
			return this.name.equals(name)
				|| name.equals(ALL_INDEX_STRING)
				|| this.name.equals(ALL_INDEX_STRING);
		}

		public String getName() {
			return name;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Cursor)) {
				return false;
			}
			Cursor cursor = (Cursor)obj;

			boolean indexEqual = indexEquals(cursor.getIndex());
			boolean nameEqual = nameEquals(cursor.getName());
			return nameEqual && indexEqual;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
		}
	}

	static final class ExpIndexCursor extends Cursor {
		ExpIndexCursor(String name, int index) {
			super(name, index, null);
		}
	}

	public static final class ExpNameCursor extends Cursor {
		ExpNameCursor(String name) {
			super(name, NO_OR_ALL_INDEX_INTEGER_VALUE, null);
		}
	}

	public static final class ExpKeyCursor extends Cursor {
		// null 대신 키를 나타내는 특정 int?
		ExpKeyCursor(String name, Object key) {
			super(name, -1, key);
		}
	}

}
