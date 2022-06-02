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
import com.navercorp.fixturemonkey.api.property.MapKeyElementProperty;
import com.navercorp.fixturemonkey.api.property.MapValueElementProperty;

public final class ArbitraryExpression implements Comparable<ArbitraryExpression> {
	private final List<Exp> expList;

	private ArbitraryExpression(List<Exp> expList) {
		this.expList = expList;
	}

	private ArbitraryExpression(String expression) {
		expList = Arrays.stream(expression.split("\\."))
			.map(it -> new Exp(it, null))
			.collect(toList());
	}

	private ArbitraryExpression(String expression, List<Boolean> isSetKey) {
		expList = Arrays.stream(expression.split("\\."))
			//Todo: isSetKey를 잘라서 넣어야함
			.map(it -> new Exp(it, isSetKey))
			.collect(toList());
	}

	public static ArbitraryExpression from(String expression) {
		return new ArbitraryExpression(expression);
	}

	public static ArbitraryExpression from(String expression, List<Boolean> isSetKey) {
		return new ArbitraryExpression(expression, isSetKey);
	}

	public ArbitraryExpression addFirst(String expression) {
		String newStringExpression = expression + "." + this;
		return new ArbitraryExpression(newStringExpression);
	}

	public ArbitraryExpression addLast(String expression) {
		String newStringExpression = this + "." + expression;
		return new ArbitraryExpression(newStringExpression);
	}

	@API(since = "0.4.0", status = Status.EXPERIMENTAL)
	public ArbitraryExpression pollLast() {
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
		return new ArbitraryExpression(newExpList);
	}

	@Override
	public int compareTo(ArbitraryExpression arbitraryExpression) {
		List<Exp> oExpList = arbitraryExpression.expList;
		//
		// if (expList.size() != oExpList.size()) {
		// 	return Integer.compare(expList.size(), oExpList.size());
		// }
		//
		// for (int i = 0; i < expList.size(); i++) {
		// 	Exp exp = expList.get(i);
		// 	Exp oExp = oExpList.get(i);
		// 	int expCompare = exp.compareTo(oExp);
		// 	if (expCompare != 0) {
		// 		return expCompare;
		// 	}
		// }

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
		ArbitraryExpression other = (ArbitraryExpression)obj;
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
			// .filter(Cursor::isNotHeadName)
			.collect(toList());
	}

	private interface ExpElement {
		String toString();

		Cursor toCursor();
	}

	// private static final class ExpAll implements Comparable<ExpAll> ExpElement {
	// 	// public static final ExpIndex ALL_INDEX_EXP_INDEX = new ExpIndex(NO_OR_ALL_INDEX_INTEGER_VALUE);
	// }

	private static final class ExpIndex implements Comparable<ExpIndex>, ExpElement {
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

		@Override
		public String toString() {
			return index == NO_OR_ALL_INDEX_INTEGER_VALUE ? ALL_INDEX_STRING : String.valueOf(index);
		}

		@Override
		public Cursor toCursor() {
			return new ExpIndexCursor(index);
		}
	}

	private static final class ExpMap implements Comparable<ExpMap>, ExpElement {
		private final Boolean isSetKey;

		public ExpMap(Boolean isSetKey) {
			this.isSetKey = isSetKey;
		}

		@Override
		public int compareTo(ExpMap expMap) {
			return Boolean.compare(this.isSetKey, expMap.isSetKey);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			ExpMap expMap = (ExpMap)obj;
			return isSetKey.equals(expMap.isSetKey);
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			return "";
		}

		@Override
		public Cursor toCursor() {
			return new ExpMapCursor(isSetKey);
		}
	}

	private static final class Exp implements Comparable<Exp> {
		private final String name;
		private final List<ExpElement> element;

		private Exp(String name) {
			this.name = name;
			this.element = new ArrayList<>();
		}

		public Exp(String expression, List<Boolean> isSetKey) {
			this.element = new ArrayList<>();
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
						//* 인 경우 - 수정 필요!
						if (indexString.equals(ALL_INDEX_STRING)) {
							this.element.add(new ExpIndex(NO_OR_ALL_INDEX_INTEGER_VALUE));
						} else {
							this.element.add(new ExpIndex(Integer.parseInt(indexString)));
						}
					} else if (ri - li == 1) {
						this.element.add(new ExpMap(isSetKey.get(keyCnt)));
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
			steps.addAll(this.getElement().stream()
				.map(ExpElement::toCursor)
				.collect(toList()));
			return steps;
		}

		public String getName() {
			return name;
		}

		public List<ExpElement> getElement() {
			return element;
		}

		public String toString() {
			String indexBrackets = element.stream()
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
			return name.equals(exp.name) && element.equals(exp.element);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, element);
		}
	}

	public interface Cursor {
		boolean match(ArbitraryProperty arbitraryProperty);

		boolean equals(Object obj);

		int hashCode();
	}
	// public abstract static class Cursor {
	// 	public boolean isNotHeadName() {
	// 		return !(this instanceof ExpNameCursor) || !HEAD_NAME.equals(this.getName());
	// 	}
	// }

	static final class ExpIndexCursor implements Cursor {
		private final int index;
		ExpIndexCursor(int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public boolean match(ArbitraryProperty arbitraryProperty) {
			boolean sameIndex = true;
			if (arbitraryProperty.getElementIndex() != null) {
				sameIndex = indexEquals(arbitraryProperty.getElementIndex()); // notNull
			}
			return sameIndex;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ExpIndexCursor)) {
				return false;
			}
			ExpIndexCursor cursor = (ExpIndexCursor)obj;

			return indexEquals(cursor.getIndex());
		}

		@Override
		public int hashCode() {
			return Objects.hash(index);
		}

		private boolean indexEquals(int index) {
			return this.index == index
				|| index == NO_OR_ALL_INDEX_INTEGER_VALUE
				|| this.index == NO_OR_ALL_INDEX_INTEGER_VALUE;
		}
	}

	public static final class ExpNameCursor implements Cursor {
		private final String name;
		ExpNameCursor(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public boolean match(ArbitraryProperty arbitraryProperty) {
			String resolvePropertyName = arbitraryProperty.getResolvePropertyName();
			boolean samePropertyName;
			if (resolvePropertyName == null) {
				samePropertyName = true; // ignore property name equivalence.
			} else {
				samePropertyName = nameEquals(resolvePropertyName);
			}
			return samePropertyName;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ExpNameCursor)) {
				return false;
			}
			ExpNameCursor cursor = (ExpNameCursor)obj;

			return nameEquals(cursor.getName());
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
		}

		private boolean nameEquals(String name) {
			return this.name.equals(name)
				|| ALL_INDEX_STRING.equals(name)
				|| ALL_INDEX_STRING.equals(this.name);
		}
	}

	public static final class ExpMapCursor implements Cursor {
		private final boolean isSetKey;
		ExpMapCursor(boolean isSetKey) {
			this.isSetKey = isSetKey;
		}

		public boolean getIsSetKey() {
			return isSetKey;
		}

		@Override
		public boolean match(ArbitraryProperty arbitraryProperty) {
			if (arbitraryProperty.getProperty() instanceof MapKeyElementProperty && isSetKey) {
				return true;
			}
			return arbitraryProperty.getProperty() instanceof MapValueElementProperty && !isSetKey;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ExpMapCursor)) {
				return false;
			}
			ExpMapCursor cursor = (ExpMapCursor)obj;

			return isSetKey == cursor.getIsSetKey();
		}

		@Override
		public int hashCode() {
			return Objects.hash(isSetKey);
		}
	}
}
