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

package com.navercorp.fixturemonkey.api.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.tree.TraverseNodePredicate.PropertyTraverseNodePredicate;
import com.navercorp.fixturemonkey.api.tree.TraverseNodePredicate.StartTraverseNodePredicate;

@API(since = "1.1.4", status = Status.EXPERIMENTAL)
public class TreeNodeManipulator {
	private final List<TraverseNodePredicate> traverseNodePredicates;
	private ArbitraryContainerInfo containerInfo;
	/**
	 * The sequence of a size manipulation.
	 * It may dismiss if a sequence of set is bigger.
	 */
	private final int manipulatingSequence;

	public TreeNodeManipulator(
		List<TraverseNodePredicate> traverseNodePredicates,
		ArbitraryContainerInfo containerInfo,
		int manipulatingSequence
	) {
		this.traverseNodePredicates = traverseNodePredicates;
		this.containerInfo = containerInfo;
		this.manipulatingSequence = manipulatingSequence;
	}

	public TreeNodeManipulator copy() {
		return new TreeNodeManipulator(
			this.getTraverseNodePredicates(),
			new ArbitraryContainerInfo(
				containerInfo.getElementMinSize(),
				containerInfo.getElementMaxSize()
			),
			manipulatingSequence
		);
	}

	public List<TraverseNodePredicate> getTraverseNodePredicates() {
		return traverseNodePredicates;
	}

	public ArbitraryContainerInfo getContainerInfo() {
		return containerInfo;
	}

	public TreeNodeManipulator withPrependNextNodePredicate(TraverseNodePredicate nextNodePredicate) {
		List<TraverseNodePredicate> nodePredicatesWithoutRoot = this.traverseNodePredicates.stream()
			.filter(it -> !(it instanceof StartTraverseNodePredicate))
			.collect(Collectors.toList());

		List<TraverseNodePredicate> newtraverseNodePredicates = new ArrayList<>();
		newtraverseNodePredicates.add(nextNodePredicate);
		newtraverseNodePredicates.addAll(nodePredicatesWithoutRoot);

		return new TreeNodeManipulator(
			newtraverseNodePredicates,
			this.containerInfo,
			manipulatingSequence
		);
	}

	public int getManipulatingSequence() {
		return manipulatingSequence;
	}

	public void fixed() {
		int fixedSize = this.containerInfo.getRandomSize();

		this.containerInfo = new ArbitraryContainerInfo(
			fixedSize,
			fixedSize
		);
	}

	public boolean isMatch(List<ObjectProperty> objectProperties) {
		int objectPropertiesSize = objectProperties.size();
		int traverseNodePredicatesize = traverseNodePredicates.size();

		boolean registered = traverseNodePredicates.get(0) instanceof PropertyTraverseNodePredicate;
		if (!registered && traverseNodePredicatesize != objectPropertiesSize) {
			return false;
		}

		for (int i = 0; i < traverseNodePredicatesize; i++) {
			int reversedNextNodePredicateIndex = traverseNodePredicatesize - 1 - i;
			int reversedCurrentObjectPropertyIndex = objectPropertiesSize - 1 - i;
			TraverseNodePredicate nextNodePredicate = traverseNodePredicates.get(reversedNextNodePredicateIndex);
			ObjectProperty objectProperty = objectProperties.get(reversedCurrentObjectPropertyIndex);

			if (!nextNodePredicate.test(objectProperty)) {
				return false;
			}
		}
		return true;
	}
}
