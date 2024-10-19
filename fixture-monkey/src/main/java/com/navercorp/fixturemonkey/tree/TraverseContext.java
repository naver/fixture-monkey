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
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.tree.TreeProperty;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;

@API(since = "0.4.0", status = Status.INTERNAL)
final class TraverseContext {
	private final List<TreeProperty> treeProperties;
	private final List<ContainerInfoManipulator> containerInfoManipulators;
	private final List<MatcherOperator<List<ContainerInfoManipulator>>> registeredContainerInfoManipulators;
	private final Map<Class<?>, List<Property>> propertyConfigurers;

	TraverseContext(
		List<TreeProperty> treeProperties,
		List<ContainerInfoManipulator> containerInfoManipulators,
		List<MatcherOperator<List<ContainerInfoManipulator>>> registeredContainerInfoManipulators,
		Map<Class<?>, List<Property>> propertyConfigurers
	) {
		this.treeProperties = treeProperties;
		this.containerInfoManipulators = containerInfoManipulators;
		this.registeredContainerInfoManipulators = registeredContainerInfoManipulators;
		this.propertyConfigurers = propertyConfigurers;
	}

	@Nullable
	public TreeProperty getRootTreeProperty() {
		return treeProperties.get(0);
	}

	public List<TreeProperty> getTreeProperties() {
		return treeProperties;
	}

	public List<ContainerInfoManipulator> getContainerInfoManipulators() {
		return containerInfoManipulators;
	}

	public Map<Class<?>, List<Property>> getPropertyConfigurers() {
		return propertyConfigurers;
	}

	public TraverseContext appendArbitraryProperty(
		TreeProperty treeProperty
	) {
		List<TreeProperty> treeProperties = new ArrayList<>(this.treeProperties);
		treeProperties.add(treeProperty);

		List<ContainerInfoManipulator> registeredContainerManipulators =
			this.registeredContainerInfoManipulators.stream()
				.filter(it -> it.match(treeProperty.getObjectProperty().getProperty()))
				.map(MatcherOperator::getOperator)
				.findFirst()
				.orElse(Collections.emptyList());

		List<ContainerInfoManipulator> concatRegisteredContainerManipulator = registeredContainerManipulators.stream()
			.map(it -> it.withPrependNextNodePredicate(
				new PropertyPredicate(treeProperty.getObjectProperty().getProperty())
			))
			.collect(Collectors.toList());

		List<ContainerInfoManipulator> concat = new ArrayList<>();
		concat.addAll(concatRegisteredContainerManipulator);
		concat.addAll(containerInfoManipulators);
		return new TraverseContext(
			treeProperties,
			concat,
			this.registeredContainerInfoManipulators,
			propertyConfigurers
		);
	}

	public boolean isTraversed(Property property) {
		return treeProperties.stream()
			.skip(1)
			.anyMatch(it -> isSameType(property, it.getObjectProperty().getProperty()));
	}

	public void addContainerInfoManipulator(ContainerInfoManipulator containerInfoManipulator) {
		if (!this.containerInfoManipulators.contains(containerInfoManipulator)) {
			this.containerInfoManipulators.add(containerInfoManipulator);
		}
	}

	@Nullable
	public TreeProperty getLastTreeProperty() {
		if (this.treeProperties.isEmpty()) {
			return null;
		}
		return this.treeProperties.get(this.treeProperties.size() - 1);
	}

	public TraverseContext withNotRecursiveTreeProperties() {
		List<TreeProperty> newTreeProperties = new ArrayList<>(this.treeProperties);
		for (int i = 1; i < this.treeProperties.size(); i++) {
			Property rootProperty = getRootTreeProperty().getObjectProperty().getProperty();
			TreeProperty treeProperty = treeProperties.get(i);
			if (isSameType(rootProperty, treeProperty.getObjectProperty().getProperty())) {
				newTreeProperties = newTreeProperties.subList(i, newTreeProperties.size());
				break;
			}
		}

		return new TraverseContext(
			newTreeProperties,
			new ArrayList<>(this.containerInfoManipulators),
			this.registeredContainerInfoManipulators,
			this.propertyConfigurers
		);
	}

	private static boolean isSameType(Property p1, Property p2) {
		boolean notMapEntry = !(p1 instanceof MapEntryElementProperty) || !(p2 instanceof MapEntryElementProperty);
		return notMapEntry && p1.getAnnotatedType().getType().equals(p2.getAnnotatedType().getType());
	}
}
