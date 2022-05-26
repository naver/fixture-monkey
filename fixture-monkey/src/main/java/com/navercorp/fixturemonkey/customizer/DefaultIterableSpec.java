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

package com.navercorp.fixturemonkey.customizer;

import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MAX_SIZE;
import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MIN_SIZE;
import static com.navercorp.fixturemonkey.Constants.MAX_MANIPULATION_COUNT;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.navercorp.fixturemonkey.api.random.Randoms;

final class DefaultIterableSpec implements IterableSpec, ExpressionSpecVisitor {
	private static final String EMPTY_FIELD = "";

	private final String iterableName;
	private final List<ExpressionSpecVisitor> next;
	@SuppressWarnings("rawtypes")
	private final List<IterableSpecSet> setList;
	@SuppressWarnings("rawtypes")
	private final List<IterableSpecPostCondition> postConditionList;

	private Integer minSize = null;
	private Integer maxSize = null;
	private boolean notNull = false;

	public DefaultIterableSpec(String iterableName) {
		this.iterableName = iterableName;
		this.next = new ArrayList<>();
		this.setList = new ArrayList<>();
		this.postConditionList = new ArrayList<>();
	}

	@Override
	public IterableSpec ofMinSize(int size) {
		this.minSize = size;
		return this;
	}

	@Override
	public IterableSpec ofMaxSize(int size) {
		this.maxSize = size;
		return this;
	}

	@Override
	public IterableSpec ofSize(int size) {
		return this.ofMinSize(size).ofMaxSize(size);
	}

	@Override
	public IterableSpec ofSizeBetween(int min, int max) {
		this.maxSize = max;
		this.minSize = min;
		return this;
	}

	@Override
	public IterableSpec ofNotNull() {
		this.notNull = true;
		return this;
	}

	@Override
	public IterableSpec setElement(long fieldIndex, Object object) {
		String expression = getFieldExpression(this.iterableName, fieldIndex, EMPTY_FIELD);
		addSet(expression, object);
		return this;
	}

	@Override
	public <T> IterableSpec setElementPostCondition(long fieldIndex, Class<T> clazz, Predicate<T> postCondition) {
		return this.setElementPostCondition(fieldIndex, clazz, postCondition, MAX_MANIPULATION_COUNT);
	}

	@Override
	public <T> IterableSpec setElementPostCondition(
		long fieldIndex,
		Class<T> clazz,
		Predicate<T> postCondition,
		long limit
	) {
		String expression = getFieldExpression(this.iterableName, fieldIndex, EMPTY_FIELD);
		addPostCondition(expression, clazz, postCondition, limit);
		return this;
	}

	@Override
	public IterableSpec listElement(long fieldIndex, Consumer<IterableSpec> spec) {
		return this.listFieldElement(fieldIndex, EMPTY_FIELD, spec);
	}

	@Override
	public IterableSpec listFieldElement(long fieldIndex, String fieldName, Consumer<IterableSpec> consumer) {
		String listName = getFieldExpression(this.iterableName, fieldIndex, fieldName);
		DefaultIterableSpec iterableSpec = new DefaultIterableSpec(listName);
		consumer.accept(iterableSpec);
		addNext(iterableSpec);
		return this;
	}

	@Override
	public IterableSpec setElementField(long fieldIndex, String fieldName, Object object) {
		String expression = getFieldExpression(this.iterableName, fieldIndex, fieldName);
		addSet(expression, object);
		return this;
	}

	@Override
	public <T> IterableSpec setElementFieldPostCondition(
		long fieldIndex,
		String fieldName,
		Class<T> clazz,
		Predicate<T> postCondition
	) {
		return this.setElementFieldPostCondition(fieldIndex, fieldName, clazz, postCondition, MAX_MANIPULATION_COUNT);
	}

	@Override
	public <T> IterableSpec setElementFieldPostCondition(
		long fieldIndex,
		String fieldName,
		Class<T> clazz,
		Predicate<T> postCondition,
		long limit
	) {
		String expression = getFieldExpression(this.iterableName, fieldIndex, fieldName);
		addPostCondition(expression, clazz, postCondition, limit);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(ExpressionSpec expressionSpec) {
		this.setList.forEach(it -> expressionSpec.set(it.expression, it.value, it.limit));
		this.postConditionList.forEach(
			it -> expressionSpec.setPostCondition(it.expression, it.clazz, it.postCondition, it.limit));

		for (ExpressionSpecVisitor nextIterableSpec : this.next) {
			nextIterableSpec.visit(expressionSpec);
		}

		if (this.notNull) {
			expressionSpec.setNotNull(this.iterableName);
		}

		if (this.minSize != null || this.maxSize != null) {
			this.minSize = this.minSize == null ? DEFAULT_ELEMENT_MIN_SIZE : this.minSize;
			this.maxSize = this.maxSize == null ? DEFAULT_ELEMENT_MAX_SIZE : this.maxSize;
			expressionSpec.size(this.iterableName, this.minSize, this.maxSize);
		}
	}

	@Override
	public IterableSpec any(Object object) {
		String allExpression = getFieldExpression(this.iterableName, "*", EMPTY_FIELD);
		long limit = getRandomLimit();
		this.addSet(allExpression, object, limit);
		return this;
	}

	@Override
	public IterableSpec all(Object object) {
		String allExpression = getFieldExpression(this.iterableName, "*", EMPTY_FIELD);
		this.addSet(allExpression, object);
		return this;
	}

	private void addNext(ExpressionSpecVisitor specVisitor) {
		this.next.add(specVisitor);
	}

	private String getFieldExpression(String collectionName, long fieldIndex, String fieldName) {
		return this.getFieldExpression(collectionName, String.valueOf(fieldIndex), fieldName);
	}

	private String getFieldExpression(String collectionName, String fieldIndex, String fieldName) {
		String expression = collectionName + "[" + fieldIndex + "]";
		if (fieldName != null && !EMPTY_FIELD.equals(fieldName)) {
			expression += "." + fieldName;
		}
		return expression;
	}

	private <T> void addSet(String expression, T object) {
		this.setList.add(new IterableSpecSet<>(expression, object));
	}

	private <T> void addSet(String expression, T object, long limit) {
		this.setList.add(new IterableSpecSet<>(expression, object, limit));
	}

	private <T> void addPostCondition(String expression, Class<T> clazz, Predicate<T> postCondition) {
		this.postConditionList.add(new IterableSpecPostCondition<>(expression, clazz, postCondition));
	}

	private <T> void addPostCondition(String expression, Class<T> clazz, Predicate<T> postCondition, long limit) {
		this.postConditionList.add(new IterableSpecPostCondition<>(expression, clazz, postCondition, limit));
	}

	private long getRandomLimit() {
		int minSize = this.minSize == null ? DEFAULT_ELEMENT_MIN_SIZE : this.minSize;
		int maxSize = this.maxSize == null ? DEFAULT_ELEMENT_MAX_SIZE : this.maxSize;
		if (minSize == maxSize) {
			return minSize;
		}

		return Randoms.nextInt(maxSize - minSize);
	}

	private static class IterableSpecSet<T> {
		private final String expression;
		private final T value;
		private final long limit;

		public IterableSpecSet(String expression, T value) {
			this.expression = expression;
			this.value = value;
			this.limit = MAX_MANIPULATION_COUNT;
		}

		public IterableSpecSet(String expression, T value, long limit) {
			this.expression = expression;
			this.value = value;
			this.limit = limit;
		}
	}

	private static class IterableSpecPostCondition<T> {
		private final String expression;
		private final Class<T> clazz;
		private final Predicate<T> postCondition;
		private final long limit;

		public IterableSpecPostCondition(String expression, Class<T> clazz, Predicate<T> postCondition) {
			this.expression = expression;
			this.clazz = clazz;
			this.postCondition = postCondition;
			this.limit = MAX_MANIPULATION_COUNT;
		}

		public IterableSpecPostCondition(String expression, Class<T> clazz, Predicate<T> postCondition, long limit) {
			this.expression = expression;
			this.clazz = clazz;
			this.postCondition = postCondition;
			this.limit = limit;
		}
	}
}
