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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Supplier;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

public final class ArbitraryTree<T> {
	private ArbitraryNode<T> head;

	public ArbitraryTree(ArbitraryNode<T> head) {
		this.head = head;
	}

	@SuppressWarnings("rawtypes")
	public Collection<ArbitraryNode> findAll(ArbitraryExpression arbitraryExpression) {
		Queue<ArbitraryNode> selectNodes = new LinkedList<>();
		selectNodes.add(getHead());

		List<ArbitraryNode> nextNodes = new ArrayList<>();

		CursorHolder cursorHolder = new CursorHolder(arbitraryExpression);
		for (Cursor cursor : cursorHolder.getCursors()) {
			while (!selectNodes.isEmpty()) {
				ArbitraryNode<?> selectNode = selectNodes.poll();

				nextNodes.addAll(selectNode.findChildrenByCursor(cursor));
			}
			selectNodes.addAll(nextNodes);
			nextNodes.clear();
		}
		return selectNodes;
	}

	public void update(ArbitraryGenerator defaultGenerator, Map<Class<?>, ArbitraryGenerator> generatorMap) {
		update(getHead(), defaultGenerator, generatorMap);
	}

	<U> void update(
		ArbitraryNode<U> entryNode,
		ArbitraryGenerator defaultGenerator,
		Map<Class<?>, ArbitraryGenerator> generatorMap
	) {

		if (!entryNode.isLeafNode() && !entryNode.isFixed()) {
			for (ArbitraryNode<?> nextChild : entryNode.getChildren()) {
				update(nextChild, defaultGenerator, generatorMap);
			}

			Class<?> clazz = entryNode.getType().getType();
			ArbitraryGenerator generator = getGenerator(clazz, defaultGenerator, generatorMap);
			entryNode.setArbitrary(
				generator.generate(entryNode.getType(), entryNode.getChildren())
			);

		}

		entryNode.getPostArbitraryManipulators().forEach(
			operation -> entryNode.setArbitrary(operation.apply(entryNode.getArbitrary()))
		);

		if (entryNode.isNullable() && !entryNode.isManipulated()) {
			entryNode.setArbitrary(entryNode.getArbitrary().injectNull(entryNode.getNullInject()));
		}
	}

	public ArbitraryNode<T> getHead() {
		return head;
	}

	public ArbitraryGenerator getGenerator(
		Class<?> clazz,
		ArbitraryGenerator defaultGenerator,
		Map<Class<?>, ArbitraryGenerator> generatorMap
	) {
		return generatorMap.getOrDefault(clazz, defaultGenerator);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public Arbitrary<T> result(
		Supplier<Arbitrary<T>> generateArbitrary,
		ArbitraryValidator<T> validator,
		boolean validOnly
	) {
		return new ArbitraryValue(generateArbitrary, validator, validOnly);
	}

	public ArbitraryTree<T> copy() {
		return new ArbitraryTree<>(this.getHead().copy());
	}
}
