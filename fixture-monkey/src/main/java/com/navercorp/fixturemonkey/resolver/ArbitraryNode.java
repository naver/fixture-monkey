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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.MAINTAINED)
final class ArbitraryNode {
	@Nullable
	private final Property resolvedParentProperty;

	private Property resolvedProperty;

	private ArbitraryProperty arbitraryProperty;

	private List<ArbitraryNode> children;

	@Nullable
	private Arbitrary<?> arbitrary;

	private boolean manipulated = false;

	@SuppressWarnings("rawtypes")
	private final List<Predicate> arbitraryFilters = new ArrayList<>();

	ArbitraryNode(
		@Nullable Property resolvedParentProperty,
		Property resolvedProperty,
		ArbitraryProperty arbitraryProperty,
		List<ArbitraryNode> children
	) {
		this.resolvedParentProperty = resolvedParentProperty;
		this.resolvedProperty = resolvedProperty;
		this.arbitraryProperty = arbitraryProperty;
		this.children = children;
	}

	public void setArbitraryProperty(ArbitraryProperty arbitraryProperty) {
		this.arbitraryProperty = arbitraryProperty;
	}

	public void setChildren(List<ArbitraryNode> children) {
		this.children = children;
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

	public List<ArbitraryNode> getChildren() {
		return this.children;
	}

	@Nullable
	public Arbitrary<?> getArbitrary() {
		return this.arbitrary;
	}

	public void setArbitrary(@Nullable Arbitrary<?> arbitrary) {
		this.arbitrary = arbitrary;
	}

	public Property getResolvedProperty() {
		return resolvedProperty;
	}

	@SuppressWarnings("rawtypes")
	public void addArbitraryFilter(Predicate filter) {
		this.arbitraryFilters.add(filter);
	}

	@SuppressWarnings("rawtypes")
	public List<Predicate> getArbitraryFilters() {
		return arbitraryFilters;
	}

	public boolean isNotManipulated() {
		boolean sized = arbitraryProperty.getContainerProperty() != null
			&& arbitraryProperty.getContainerProperty().getContainerInfo().isManipulated();

		return !manipulated && !sized;
	}

	public void setManipulated(boolean manipulated) {
		this.manipulated = manipulated;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryNode that = (ArbitraryNode)obj;
		return Objects.equals(resolvedParentProperty, that.resolvedParentProperty)
			&& arbitraryProperty.equals(that.arbitraryProperty)
			&& children.equals(that.children)
			&& Objects.equals(arbitrary, that.arbitrary)
			&& arbitraryFilters.equals(that.arbitraryFilters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			resolvedParentProperty,
			arbitraryProperty,
			children,
			arbitrary,
			arbitraryFilters
		);
	}
}
