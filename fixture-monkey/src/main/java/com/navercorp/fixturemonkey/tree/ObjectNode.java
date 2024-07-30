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

package com.navercorp.fixturemonkey.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyPath;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.customizer.NodeManipulator;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ObjectNode {
	@Nullable
	private final Property resolvedParentProperty;

	private Property resolvedProperty;

	private ArbitraryProperty arbitraryProperty;

	@Nullable
	private ObjectNode parent = null;

	private List<ObjectNode> children;

	@Nullable
	private CombinableArbitrary<?> arbitrary;

	private final List<NodeManipulator> manipulators = new ArrayList<>();

	private final List<ContainerInfoManipulator> containerInfoManipulators = new ArrayList<>();

	@SuppressWarnings("rawtypes")
	private final List<Predicate> arbitraryFilters = new ArrayList<>();
	private final List<Function<CombinableArbitrary<?>, CombinableArbitrary<?>>> arbitraryCustomizers =
		new ArrayList<>();

	private final LazyArbitrary<Boolean> childNotCacheable = LazyArbitrary.lazy(() -> {
		for (ObjectNode child : children) {
			if (child.manipulated() || child.childNotCacheable.getValue() || child.arbitraryProperty.isContainer()) {
				return true;
			}
		}

		return false;
	});

	private final LazyArbitrary<PropertyPath> lazyPropertyPath = LazyArbitrary.lazy(() -> {
		if (parent == null) {
			return new PropertyPath(resolvedProperty, null, 1);
		}

		PropertyPath parentPropertyPath = parent.getLazyPropertyPath().getValue();
		return new PropertyPath(
			resolvedProperty,
			parentPropertyPath,
			parentPropertyPath.getDepth() + 1
		);
	});

	ObjectNode(
		@Nullable Property resolvedParentProperty,
		Property resolvedProperty,
		ArbitraryProperty arbitraryProperty,
		List<ObjectNode> children
	) {
		this.resolvedParentProperty = resolvedParentProperty;
		this.resolvedProperty = resolvedProperty;
		this.arbitraryProperty = arbitraryProperty;
		this.setChildren(children);
	}

	public void setArbitraryProperty(ArbitraryProperty arbitraryProperty) {
		this.arbitraryProperty = arbitraryProperty;
	}

	public void setChildren(List<ObjectNode> children) {
		this.children = children;
		for (ObjectNode child : this.children) {
			child.parent = this;
		}
	}

	public void setResolvedProperty(Property resolvedProperty) {
		this.resolvedProperty = resolvedProperty;
	}

	@Nullable
	public Property getResolvedParentProperty() {
		return resolvedParentProperty;
	}

	public ArbitraryProperty getArbitraryProperty() {
		return this.arbitraryProperty;
	}

	public Property getProperty() {
		return this.getArbitraryProperty().getObjectProperty().getProperty();
	}

	public List<ObjectNode> getChildren() {
		return this.children;
	}

	@Nullable
	public CombinableArbitrary<?> getArbitrary() {
		return this.arbitrary;
	}

	public void setArbitrary(@Nullable CombinableArbitrary<?> arbitrary) {
		this.arbitrary = arbitrary;
	}

	public Property getResolvedProperty() {
		return resolvedProperty;
	}

	@SuppressWarnings("rawtypes")
	public void addArbitraryFilter(Predicate filter) {
		this.arbitraryFilters.add(filter);
	}

	public void addManipulator(NodeManipulator nodeManipulator) {
		this.manipulators.add(nodeManipulator);
	}

	public void addContainerManipulator(ContainerInfoManipulator containerInfoManipulator) {
		this.containerInfoManipulators.add(containerInfoManipulator);
	}

	@SuppressWarnings("rawtypes")
	public List<Predicate> getArbitraryFilters() {
		return arbitraryFilters;
	}

	public void addGeneratedArbitraryCustomizer(
		Function<CombinableArbitrary<?>, CombinableArbitrary<?>> arbitraryCustomizer
	) {
		this.arbitraryCustomizers.add(arbitraryCustomizer);
	}

	public List<Function<CombinableArbitrary<?>, CombinableArbitrary<?>>> getGeneratedArbitraryCustomizers() {
		return arbitraryCustomizers;
	}

	public boolean manipulated() {
		return !manipulators.isEmpty() || !containerInfoManipulators.isEmpty();
	}

	public boolean cacheable() {
		return !manipulated() && !arbitraryProperty.isContainer() && !childNotCacheable.getValue();
	}

	@Nullable
	public ObjectNode getParent() {
		return parent;
	}

	@Nullable
	public ContainerInfoManipulator getAppliedContainerInfoManipulator() {
		if (containerInfoManipulators.isEmpty()) {
			return null;
		}

		return containerInfoManipulators.get(containerInfoManipulators.size() - 1);
	}

	public LazyArbitrary<PropertyPath> getLazyPropertyPath() {
		return lazyPropertyPath;
	}
}
