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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.PropertyPredicate;
import com.navercorp.fixturemonkey.tree.StartNodePredicate;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ContainerInfoManipulator {
	private final List<NextNodePredicate> nextNodePredicates;
	private ArbitraryContainerInfo containerInfo;
	/**
	 * The sequence of a size manipulation.
	 * It may dismiss if a sequence of set is bigger.
	 */
	private final int manipulatingSequence;

	public ContainerInfoManipulator(
		List<NextNodePredicate> nextNodePredicates,
		ArbitraryContainerInfo containerInfo,
		int manipulatingSequence
	) {
		this.nextNodePredicates = nextNodePredicates;
		this.containerInfo = containerInfo;
		this.manipulatingSequence = manipulatingSequence;
	}

	public ContainerInfoManipulator copy() {
		return new ContainerInfoManipulator(
			this.getNextNodePredicates(),
			new ArbitraryContainerInfo(
				containerInfo.getElementMinSize(),
				containerInfo.getElementMaxSize()
			),
			manipulatingSequence
		);
	}

	public List<NextNodePredicate> getNextNodePredicates() {
		return nextNodePredicates;
	}

	public ArbitraryContainerInfo getContainerInfo() {
		return containerInfo;
	}

	public ContainerInfoManipulator withPrependNextNodePredicate(NextNodePredicate nextNodePredicate) {
		List<NextNodePredicate> nodePredicatesWithoutRoot = this.nextNodePredicates.stream()
			.filter(it -> !(it instanceof StartNodePredicate))
			.collect(Collectors.toList());

		List<NextNodePredicate> newNextNodePredicates = new ArrayList<>();
		newNextNodePredicates.add(nextNodePredicate);
		newNextNodePredicates.addAll(nodePredicatesWithoutRoot);

		return new ContainerInfoManipulator(
			newNextNodePredicates,
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
		int nextNodePredicateSize = nextNodePredicates.size();

		boolean registered = nextNodePredicates.get(0) instanceof PropertyPredicate;
		if (!registered && nextNodePredicateSize != objectPropertiesSize) {
			return false;
		}

		for (int i = 0; i < nextNodePredicateSize; i++) {
			int reversedNextNodePredicateIndex = nextNodePredicateSize - 1 - i;
			int reversedCurrentObjectPropertyIndex = objectPropertiesSize - 1 - i;
			NextNodePredicate nextNodePredicate = nextNodePredicates.get(reversedNextNodePredicateIndex);
			ObjectProperty objectProperty = objectProperties.get(reversedCurrentObjectPropertyIndex);

			if (!nextNodePredicate.test(objectProperty)) {
				return false;
			}
		}
		return true;
	}
}
