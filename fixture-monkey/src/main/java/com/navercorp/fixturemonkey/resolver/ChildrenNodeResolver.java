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

import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.NOT_NULL_INJECT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression.Cursor;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ChildrenNodeResolver implements NodeResolver {
	private final String expression;

	public ChildrenNodeResolver(String expression) {
		this.expression = expression;
	}

	@Override
	public List<ArbitraryNode> resolve(ArbitraryTree arbitraryTree) {
		return null;
	}

	public List<ArbitraryNode> getNext(List<ArbitraryNode> nodes) {
		if (expression == "[-1]") {
			List<ArbitraryNode> selectedNodes = new ArrayList<>();
			for (ArbitraryNode node: nodes) {
				selectedNodes.add(node.getChildren().get(node.getChildren().size()-1));
			}
			return selectedNodes;
		} else if (expression.contains("[")) {
			List<ArbitraryNode> selectedNodes = new ArrayList<>();
			for (ArbitraryNode node: nodes) {
				selectedNodes.add(node.getChildren().get(Integer.parseInt(expression.substring(1,expression.length()-1))));
			}
			return selectedNodes;
		}
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		List<ArbitraryNode> selectedNodes = new ArrayList<>();
		selectedNodes.addAll(nodes);

		List<Cursor> cursors = arbitraryExpression.toCursors();
		for (Cursor cursor : cursors) {
			selectedNodes = retrieveNextMatchingNodes(selectedNodes, cursor);
		}
		Collections.shuffle(selectedNodes, Randoms.current());
		return selectedNodes;
	}
	private LinkedList<ArbitraryNode> retrieveNextMatchingNodes(List<ArbitraryNode> selectedNodes, Cursor cursor) {
		LinkedList<ArbitraryNode> nextNodes = new LinkedList<>();
		for (ArbitraryNode selectedNode : selectedNodes) {
			selectedNode.setArbitraryProperty(selectedNode.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
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
}
