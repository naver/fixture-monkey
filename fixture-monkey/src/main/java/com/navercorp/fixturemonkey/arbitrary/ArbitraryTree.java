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

import static com.navercorp.fixturemonkey.Constants.HEAD_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

public final class ArbitraryTree<T> {
	private final ArbitraryNode<T> head;

	public ArbitraryTree(ArbitraryNode<T> head) {
		this.head = head;
	}

	@SuppressWarnings("rawtypes")
	public Collection<ArbitraryNode> findAll(ArbitraryExpression arbitraryExpression) {
		LinkedList<ArbitraryNode> selectNodes = new LinkedList<>();
		selectNodes.add(head);
		head.setManipulated(true);
		List<ArbitraryNode> nextNodes = new ArrayList<>();

		List<Cursor> cursors = arbitraryExpression.toCursors();
		for (Cursor cursor : cursors) {
			if (isHeadName(cursor)) {
				continue;
			}
			while (!selectNodes.isEmpty()) {
				ArbitraryNode<?> selectNode = selectNodes.poll();

				nextNodes.addAll(selectNode.findChildrenByCursor(cursor));
			}
			selectNodes.addAll(nextNodes);
			nextNodes.clear();
		}
		Collections.shuffle(selectNodes, Randoms.current());
		return selectNodes;
	}

	private boolean isHeadName(Cursor cursor) {
		return cursor instanceof ExpNameCursor && HEAD_NAME.equals(cursor.getName());
	}

	@Nullable
	public ArbitraryNode<?> findFirstResetNode() {
		return doFindFirstResetNode(this.head);
	}

	@SuppressWarnings("rawtypes")
	@Nullable
	private ArbitraryNode<?> doFindFirstResetNode(ArbitraryNode<?> node) {
		boolean reset = node.isReset();
		node.setReset(false);
		if (reset) {
			return node;
		}
		List<ArbitraryNode> children = node.getChildren();

		for (ArbitraryNode child : children) {
			ArbitraryNode result = doFindFirstResetNode(child);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public void update(ArbitraryGenerator defaultGenerator, Map<Class<?>, ArbitraryGenerator> generatorMap) {
		update(head, defaultGenerator, generatorMap);
	}

	private <U> void update(
		ArbitraryNode<U> entryNode,
		ArbitraryGenerator defaultGenerator,
		Map<Class<?>, ArbitraryGenerator> generatorMap
	) {

		if (!entryNode.isLeafNode() && !entryNode.isFixed() && entryNode.isActive()) {
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

	public ArbitraryGenerator getGenerator(
		Class<?> clazz,
		ArbitraryGenerator defaultGenerator,
		Map<Class<?>, ArbitraryGenerator> generatorMap
	) {
		return generatorMap.getOrDefault(clazz, defaultGenerator);
	}

	@SuppressWarnings("rawtypes")
	public Arbitrary<T> result(
		Supplier<Arbitrary<T>> generateArbitrary,
		ArbitraryValidator validator,
		boolean validOnly
	) {
		return new ArbitraryValue<>(generateArbitrary, validator, validOnly, new ConcurrentHashMap<>());
	}

	@SuppressWarnings("unchecked")
	public Class<T> getClazz() {
		return (Class<T>)head.getType().getType();
	}

	public Arbitrary<T> getArbitrary() {
		return head.getArbitrary();
	}

	public ArbitraryTree<T> copy() {
		return new ArbitraryTree<>(this.head.copy());
	}

	ArbitraryNode<T> getHead() {
		return head;
	}
}
