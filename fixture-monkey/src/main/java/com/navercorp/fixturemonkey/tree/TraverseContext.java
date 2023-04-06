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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;

@API(since = "0.4.0", status = Status.MAINTAINED)
final class TraverseContext {
	private final ArbitraryProperty rootArbitraryProperty;
	private final List<ArbitraryProperty> arbitraryProperties;
	private final List<ContainerInfoManipulator> containerInfoManipulators;
	private final List<MatcherOperator<List<ContainerInfoManipulator>>> registeredContainerInfoManipulators;

	public TraverseContext(
		ArbitraryProperty rootArbitraryProperty,
		List<ArbitraryProperty> arbitraryProperties,
		List<ContainerInfoManipulator> containerInfoManipulators,
		List<MatcherOperator<List<ContainerInfoManipulator>>> registeredContainerInfoManipulators
	) {
		this.rootArbitraryProperty = rootArbitraryProperty;
		this.arbitraryProperties = arbitraryProperties;
		this.containerInfoManipulators = containerInfoManipulators;
		this.registeredContainerInfoManipulators = registeredContainerInfoManipulators;
	}

	public ArbitraryProperty getRootArbitraryProperty() {
		return rootArbitraryProperty;
	}

	public List<ArbitraryProperty> getArbitraryProperties() {
		return arbitraryProperties;
	}

	public List<ContainerInfoManipulator> getContainerInfoManipulators() {
		return containerInfoManipulators;
	}

	public TraverseContext appendArbitraryProperty(
		ArbitraryProperty arbitraryProperty
	) {
		List<ArbitraryProperty> arbitraryProperties = new ArrayList<>(this.arbitraryProperties);
		arbitraryProperties.add(arbitraryProperty);

		List<ContainerInfoManipulator> registeredContainerManipulators =
			this.registeredContainerInfoManipulators.stream()
				.filter(it -> it.match(arbitraryProperty.getObjectProperty().getProperty()))
				.map(MatcherOperator::getOperator)
				.findFirst()
				.orElse(Collections.emptyList());

		List<ContainerInfoManipulator> concatRegisteredContainerManipulator = registeredContainerManipulators.stream()
			.map(it -> it.withPrependNextNodePredicate(
				new PropertyPredicate(arbitraryProperty.getObjectProperty().getProperty())
			))
			.collect(Collectors.toList());

		List<ContainerInfoManipulator> concat = new ArrayList<>();
		concat.addAll(concatRegisteredContainerManipulator);
		concat.addAll(containerInfoManipulators);
		return new TraverseContext(
			rootArbitraryProperty,
			arbitraryProperties,
			concat,
			this.registeredContainerInfoManipulators
		);
	}

	public boolean isTraversed(Property property) {
		return property.equals(rootArbitraryProperty.getObjectProperty().getProperty())
			|| arbitraryProperties.stream()
			.filter(it -> it != rootArbitraryProperty)
			.anyMatch(it -> isSameType(property, it.getObjectProperty().getProperty()));
	}

	private static boolean isSameType(Property p1, Property p2) {
		boolean notMapEntry = !(p1 instanceof MapEntryElementProperty) || !(p2 instanceof MapEntryElementProperty);
		return notMapEntry && p1.getAnnotatedType().getType().equals(p2.getAnnotatedType().getType());
	}
}
