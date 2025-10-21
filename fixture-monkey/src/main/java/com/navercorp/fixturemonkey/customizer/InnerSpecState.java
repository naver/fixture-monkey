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

import java.util.List;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.expression.DefaultDeclarativeExpression;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;

@API(since = "0.5.0", status = Status.MAINTAINED)
final class InnerSpecState {
	@Nullable
	private NodeSetManipulatorSnapshot nodeSetManipulatorSnapshot;
	@Nullable
	private ContainerInfoSnapshot containerInfoSnapshot;
	@Nullable
	private FilterSnapshot filterSnapshot;

	void setNodeManipulatorSnapshot(@Nullable NodeSetManipulatorSnapshot nodeSetManipulatorSnapshot) {
		this.nodeSetManipulatorSnapshot = nodeSetManipulatorSnapshot;
	}

	void setContainerInfoSnapshot(@Nullable ContainerInfoSnapshot containerInfoSnapshot) {
		this.containerInfoSnapshot = containerInfoSnapshot;
	}

	void setFilterSnapshot(@Nullable FilterSnapshot filterSnapshot) {
		this.filterSnapshot = filterSnapshot;
	}

	@Nullable
	NodeSetManipulatorSnapshot getNodeManipulatorSnapshot() {
		return nodeSetManipulatorSnapshot;
	}

	@Nullable
	ContainerInfoSnapshot getContainerInfoHolder() {
		return containerInfoSnapshot;
	}

	@Nullable
	FilterSnapshot getFilterHolder() {
		return filterSnapshot;
	}

	InnerSpecState withPrefix(DefaultDeclarativeExpression parentDeclarativeExpression) {
		InnerSpecState newState = new InnerSpecState();

		if (this.nodeSetManipulatorSnapshot != null) {
			newState.nodeSetManipulatorSnapshot = new NodeSetManipulatorSnapshot(
				this.nodeSetManipulatorSnapshot.sequence,
				this.nodeSetManipulatorSnapshot.declarativeExpression.prepend(parentDeclarativeExpression),
				this.nodeSetManipulatorSnapshot.value
			);
		}

		if (this.filterSnapshot != null) {
			newState.filterSnapshot = new FilterSnapshot(
				this.filterSnapshot.sequence,
				this.filterSnapshot.declarativeExpression.prepend(parentDeclarativeExpression),
				this.filterSnapshot.type,
				this.filterSnapshot.predicate
			);
		}

		if (this.containerInfoSnapshot != null) {
			newState.containerInfoSnapshot = new ContainerInfoSnapshot(
				this.containerInfoSnapshot.sequence,
				this.containerInfoSnapshot.declarativeExpression.prepend(parentDeclarativeExpression),
				this.containerInfoSnapshot.elementMinSize,
				this.containerInfoSnapshot.elementMaxSize
			);
		}

		return newState;
	}

	public static class ContainerInfoSnapshot {
		private final int sequence;
		private final DefaultDeclarativeExpression declarativeExpression;
		private final int elementMinSize;
		private final int elementMaxSize;

		public ContainerInfoSnapshot(
			int sequence,
			DefaultDeclarativeExpression declarativeExpression,
			int elementMinSize,
			int elementMaxSize
		) {
			this.sequence = sequence;
			this.declarativeExpression = declarativeExpression;
			this.elementMinSize = elementMinSize;
			this.elementMaxSize = elementMaxSize;
		}

		int getSequence() {
			return sequence;
		}

		List<NextNodePredicate> getNextNodePredicates() {
			return this.declarativeExpression.getNestedNextNodePredicates();
		}

		int getElementMinSize() {
			return elementMinSize;
		}

		int getElementMaxSize() {
			return elementMaxSize;
		}
	}

	public static class FilterSnapshot {
		private final int sequence;
		private final DefaultDeclarativeExpression declarativeExpression;
		private final Class<?> type;
		private final Predicate<?> predicate;

		public FilterSnapshot(
			int sequence,
			DefaultDeclarativeExpression declarativeExpression,
			Class<?> type,
			Predicate<?> predicate
		) {
			this.sequence = sequence;
			this.declarativeExpression = declarativeExpression;
			this.type = type;
			this.predicate = predicate;
		}

		public int getSequence() {
			return sequence;
		}

		List<NextNodePredicate> getNextNodePredicates() {
			return this.declarativeExpression.getNestedNextNodePredicates();
		}

		Class<?> getType() {
			return type;
		}

		Predicate<?> getPredicate() {
			return predicate;
		}
	}

	public static class NodeSetManipulatorSnapshot {
		private final int sequence;
		private final DefaultDeclarativeExpression declarativeExpression;
		private final Object value;

		public NodeSetManipulatorSnapshot(
			int sequence,
			DefaultDeclarativeExpression declarativeExpression,
			Object value
		) {
			this.sequence = sequence;
			this.declarativeExpression = declarativeExpression;
			this.value = value;
		}

		List<NextNodePredicate> getNextNodePredicates() {
			return this.declarativeExpression.getNestedNextNodePredicates();
		}

		Object getValue() {
			return value;
		}

		int getSequence() {
			return sequence;
		}
	}

	static final class ManipulatorHolderSet {
		private final List<NodeSetManipulatorSnapshot> nodeSetManipulatorSnapshots;
		private final List<ContainerInfoSnapshot> containerInfoManipulators;
		private final List<FilterSnapshot> postConditionManipulators;

		public ManipulatorHolderSet(
			List<NodeSetManipulatorSnapshot> nodeSetManipulatorSnapshots,
			List<ContainerInfoSnapshot> containerInfoManipulators,
			List<FilterSnapshot> postConditionManipulators
		) {
			this.nodeSetManipulatorSnapshots = nodeSetManipulatorSnapshots;
			this.containerInfoManipulators = containerInfoManipulators;
			this.postConditionManipulators = postConditionManipulators;
		}

		public List<NodeSetManipulatorSnapshot> getNodeResolverObjectHolders() {
			return nodeSetManipulatorSnapshots;
		}

		public List<ContainerInfoSnapshot> getContainerInfoManipulators() {
			return containerInfoManipulators;
		}

		public List<FilterSnapshot> getPostConditionManipulators() {
			return postConditionManipulators;
		}
	}

}
