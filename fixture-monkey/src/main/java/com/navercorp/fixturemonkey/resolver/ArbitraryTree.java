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

package com.navercorp.fixturemonkey.resolver;

import static com.navercorp.fixturemonkey.Constants.KEY_INDEX_INTEGER_VALUE;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.NOT_NULL_INJECT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.MapKeyElementProperty;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression.Cursor;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
final class ArbitraryTree {
	private final ArbitraryNode rootNode;
	private final GenerateOptions generateOptions;

	ArbitraryTree(
		ArbitraryNode rootNode,
		GenerateOptions generateOptions
	) {
		this.rootNode = rootNode;
		this.generateOptions = generateOptions;
	}

	List<ArbitraryNode> findAll(ArbitraryExpression arbitraryExpression) {
		LinkedList<ArbitraryNode> selectedNodes = new LinkedList<>();
		selectedNodes.add(rootNode);

		List<Cursor> cursors = arbitraryExpression.toCursors();
		int keyIdx = 0;
		for (Cursor cursor : cursors) {
			if (cursor.getIndex() == KEY_INDEX_INTEGER_VALUE){
				selectedNodes = retrieveNextMatchingMapNodes(selectedNodes,
					arbitraryExpression.keys.get(keyIdx), arbitraryExpression.isSetKey.get(keyIdx));
				keyIdx++;
			} else {
				selectedNodes = retrieveNextMatchingNodes(selectedNodes, cursor);
			}
		}
		Collections.shuffle(selectedNodes, Randoms.current());
		return selectedNodes;
	}

	private LinkedList<ArbitraryNode> retrieveNextMatchingNodes(List<ArbitraryNode> selectedNodes, Cursor cursor) {
		LinkedList<ArbitraryNode> nextNodes = new LinkedList<>();
		for (ArbitraryNode selectedNode : selectedNodes) {
			List<ArbitraryNode> children = selectedNode.getChildren();
			for (ArbitraryNode child : children) {
				if (cursor.match(child.getArbitraryProperty())) {
					child.setArbitraryProperty(child.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
					nextNodes.add(child);
				}
			}
		}
		return nextNodes;
	}

	private LinkedList<ArbitraryNode> retrieveNextMatchingMapNodes(List<ArbitraryNode> selectedNodes, Object key, Boolean isSetKey) {
		LinkedList<ArbitraryNode> nextNodes = new LinkedList<>();
		// selectedNode는 map
		for (ArbitraryNode selectedNode: selectedNodes) {
			List<ArbitraryNode> children = selectedNode.getChildren();
			boolean hasKey = false;
			// child는 mapEntry
			for (ArbitraryNode child: children) {
				MapKeyElementProperty mapKeyElementProperty = (MapKeyElementProperty)child.getChildren().get(0).getProperty();
				// 키 값이 일치하는지 비교하는 부분. 비교 방법 수정해야함.
				if (key.equals(mapKeyElementProperty.getFixedValue())) {
					hasKey = true;
					// NULL INJECT 부분 수정하기
					child.setArbitraryProperty(child.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
					if (isSetKey) {
						nextNodes.add(child.getChildren().get(0));
					} else {
						nextNodes.add(child.getChildren().get(1));
					}
				}
			}
			//Todo: key가 없는 경우 entry를 새로 생성.
			// if (!hasKey) {
			// 	MapInsertNewEntry(selectedNode, key);
			// }
		}
		return nextNodes;
	}

	Arbitrary<?> generate() {
		ArbitraryGeneratorContext context = generateContext(rootNode, null);
		return this.generateOptions.getArbitraryGenerator(rootNode.getProperty())
			.generate(context);
	}

	private ArbitraryGeneratorContext generateContext(
		ArbitraryNode arbitraryNode,
		@Nullable ArbitraryGeneratorContext parentContext
	) {
		Map<ArbitraryProperty, ArbitraryNode> childNodesByArbitraryProperty = new HashMap<>();
		List<ArbitraryProperty> childrenProperties = new ArrayList<>();
		for (ArbitraryNode childNode : arbitraryNode.getChildren()) {
			childNodesByArbitraryProperty.put(childNode.getArbitraryProperty(), childNode);
			childrenProperties.add(childNode.getArbitraryProperty());
		}

		return new ArbitraryGeneratorContext(
			arbitraryNode.getArbitraryProperty(),
			childrenProperties,
			parentContext,
			(ctx, prop) -> {
				ArbitraryNode node = childNodesByArbitraryProperty.get(prop);
				if (node == null) {
					return Arbitraries.just(null);
				}

				Arbitrary<?> arbitrary = node.getArbitrary();
				if (arbitrary != null) {
					return arbitrary;
				}

				ArbitraryGeneratorContext childArbitraryGeneratorContext = this.generateContext(node, ctx);
				return this.generateOptions.getArbitraryGenerator(prop.getProperty())
					.generate(childArbitraryGeneratorContext);
			}
		);
	}
}
