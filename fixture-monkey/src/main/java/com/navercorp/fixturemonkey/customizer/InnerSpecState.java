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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.tree.CompositeNodeResolver;
import com.navercorp.fixturemonkey.tree.NodeResolver;

@API(since = "0.5.0", status = Status.MAINTAINED)
final class InnerSpecState {
	@Nullable
	private NodeResolverObjectHolder objectHolder;
	@Nullable
	private ContainerInfoHolder containerInfoHolder;
	@Nullable
	private FilterHolder filterHolder;

	void setObjectHolder(@Nullable NodeResolverObjectHolder objectHolder) {
		this.objectHolder = objectHolder;
	}

	void setContainerInfoHolder(@Nullable ContainerInfoHolder containerInfoHolder) {
		this.containerInfoHolder = containerInfoHolder;
	}

	void setFilterHolder(@Nullable FilterHolder filterHolder) {
		this.filterHolder = filterHolder;
	}

	@Nullable
	NodeResolverObjectHolder getObjectHolder() {
		return objectHolder;
	}

	@Nullable
	ContainerInfoHolder getContainerInfoHolder() {
		return containerInfoHolder;
	}

	@Nullable
	FilterHolder getFilterHolder() {
		return filterHolder;
	}

	InnerSpecState withPrefix(NodeResolver nodeResolver) {
		InnerSpecState newState = new InnerSpecState();

		if (this.objectHolder != null) {
			newState.objectHolder = new NodeResolverObjectHolder(
				this.objectHolder.sequence,
				new CompositeNodeResolver(
					nodeResolver,
					this.objectHolder.nodeResolver
				),
				this.objectHolder.value
			);
		}

		if (this.filterHolder != null) {
			newState.filterHolder = new FilterHolder(
				this.filterHolder.sequence,
				new CompositeNodeResolver(nodeResolver, this.filterHolder.nodeResolver),
				this.filterHolder.type,
				this.filterHolder.predicate
			);
		}

		if (this.containerInfoHolder != null) {
			newState.containerInfoHolder = new ContainerInfoHolder(
				this.containerInfoHolder.sequence,
				new CompositeNodeResolver(nodeResolver, this.containerInfoHolder.nodeResolver),
				this.containerInfoHolder.elementMinSize,
				this.containerInfoHolder.elementMaxSize
			);
		}

		return newState;
	}

	public static class ContainerInfoHolder {
		private final int sequence;
		private final NodeResolver nodeResolver;
		private final int elementMinSize;
		private final int elementMaxSize;

		public ContainerInfoHolder(int sequence, NodeResolver nodeResolver, int elementMinSize, int elementMaxSize) {
			this.sequence = sequence;
			this.nodeResolver = nodeResolver;
			this.elementMinSize = elementMinSize;
			this.elementMaxSize = elementMaxSize;
		}

		int getSequence() {
			return sequence;
		}

		NodeResolver getNodeResolver() {
			return nodeResolver;
		}

		int getElementMinSize() {
			return elementMinSize;
		}

		int getElementMaxSize() {
			return elementMaxSize;
		}
	}

	public static class FilterHolder {
		private final int sequence;
		private final NodeResolver nodeResolver;
		private final Class<?> type;
		private final Predicate<?> predicate;

		public FilterHolder(int sequence, NodeResolver nodeResolver, Class<?> type, Predicate<?> predicate) {
			this.sequence = sequence;
			this.nodeResolver = nodeResolver;
			this.type = type;
			this.predicate = predicate;
		}

		public int getSequence() {
			return sequence;
		}

		NodeResolver getNodeResolver() {
			return nodeResolver;
		}

		Class<?> getType() {
			return type;
		}

		Predicate<?> getPredicate() {
			return predicate;
		}
	}

	public static class NodeResolverObjectHolder {
		private final int sequence;
		private final NodeResolver nodeResolver;
		private final Object value;

		public NodeResolverObjectHolder(int sequence, NodeResolver nodeResolver, Object value) {
			this.sequence = sequence;
			this.nodeResolver = nodeResolver;
			this.value = value;
		}

		NodeResolver getNodeResolver() {
			return nodeResolver;
		}

		Object getValue() {
			return value;
		}

		int getSequence() {
			return sequence;
		}
	}

	static final class ManipulatorHolderSet {
		private final List<NodeResolverObjectHolder> nodeResolverObjectHolders;
		private final List<ContainerInfoHolder> containerInfoManipulators;
		private final List<FilterHolder> postConditionManipulators;

		public ManipulatorHolderSet(
			List<NodeResolverObjectHolder> nodeResolverObjectHolders,
			List<ContainerInfoHolder> containerInfoManipulators,
			List<FilterHolder> postConditionManipulators
		) {
			this.nodeResolverObjectHolders = nodeResolverObjectHolders;
			this.containerInfoManipulators = containerInfoManipulators;
			this.postConditionManipulators = postConditionManipulators;
		}

		public List<NodeResolverObjectHolder> getNodeResolverObjectHolders() {
			return nodeResolverObjectHolders;
		}

		public List<ContainerInfoHolder> getContainerInfoManipulators() {
			return containerInfoManipulators;
		}

		public List<FilterHolder> getPostConditionManipulators() {
			return postConditionManipulators;
		}
	}

}
