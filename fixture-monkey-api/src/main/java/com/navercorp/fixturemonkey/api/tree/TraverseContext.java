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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.tree.TraverseNodePredicate.PropertyTraverseNodePredicate;

@API(since = "1.0.4", status = Status.EXPERIMENTAL)
public final class TraverseContext {
	private final List<TreeProperty> treeProperties;
	private final List<TreeNodeManipulator> treeManipulators;
	private final List<MatcherOperator<List<TreeNodeManipulator>>> registeredTreeManipulators;
	private final Map<Class<?>, List<Property>> propertyConfigurers;
	private final boolean validOnly;
	private final MonkeyContext monkeyContext;

	public TraverseContext(
		List<TreeProperty> treeProperties,
		List<TreeNodeManipulator> treeManipulators,
		List<MatcherOperator<List<TreeNodeManipulator>>> registeredTreeManipulators,
		Map<Class<?>, List<Property>> propertyConfigurers,
		boolean validOnly,
		MonkeyContext monkeyContext
	) {
		this.treeProperties = treeProperties;
		this.treeManipulators = treeManipulators;
		this.registeredTreeManipulators = registeredTreeManipulators;
		this.propertyConfigurers = propertyConfigurers;
		this.validOnly = validOnly;
		this.monkeyContext = monkeyContext;
	}

	@Nullable
	public TreeProperty getRootTreeProperty() {
		return treeProperties.get(0);
	}

	public List<TreeProperty> getTreeProperties() {
		return treeProperties;
	}

	public List<TreeNodeManipulator> getTreeManipulators() {
		return treeManipulators;
	}

	public Map<Class<?>, List<Property>> getPropertyConfigurers() {
		return propertyConfigurers;
	}

	public boolean isValidOnly() {
		return validOnly;
	}

	public MonkeyContext getMonkeyContext() {
		return monkeyContext;
	}

	public TraverseContext appendArbitraryProperty(
		TreeProperty treeProperty
	) {
		List<TreeProperty> treeProperties = new ArrayList<>(this.treeProperties);
		treeProperties.add(treeProperty);

		List<TreeNodeManipulator> registeredContainerManipulators =
			this.registeredTreeManipulators.stream()
				.filter(it -> it.match(treeProperty.getObjectProperty().getProperty()))
				.map(MatcherOperator::getOperator)
				.findFirst()
				.orElse(Collections.emptyList());

		List<TreeNodeManipulator> concatRegisteredContainerManipulator = registeredContainerManipulators.stream()
			.map(it -> it.withPrependNextNodePredicate(
				new PropertyTraverseNodePredicate(treeProperty.getObjectProperty().getProperty())
			))
			.collect(Collectors.toList());

		List<TreeNodeManipulator> concat = new ArrayList<>();
		concat.addAll(concatRegisteredContainerManipulator);
		concat.addAll(treeManipulators);
		return new TraverseContext(
			treeProperties,
			concat,
			this.registeredTreeManipulators,
			propertyConfigurers,
			this.validOnly,
			this.monkeyContext
		);
	}

	public boolean isTraversed(Property property) {
		return treeProperties.stream()
			.skip(1)
			.anyMatch(it -> isSameType(property, it.getObjectProperty().getProperty()));
	}

	public void addContainerInfoManipulator(TreeNodeManipulator containerInfoManipulator) {
		if (!this.treeManipulators.contains(containerInfoManipulator)) {
			this.treeManipulators.add(containerInfoManipulator);
		}
	}

	@Nullable
	public TreeProperty getLastTreeProperty() {
		if (this.treeProperties.isEmpty()) {
			return null;
		}
		return this.treeProperties.get(this.treeProperties.size() - 1);
	}

	public TraverseContext withParentProperties() {
		List<TreeProperty> newTreeProperties = new ArrayList<>();

		if (!this.treeProperties.isEmpty()) {
			newTreeProperties.add(this.treeProperties.get(this.treeProperties.size() - 1));
		}

		return new TraverseContext(
			newTreeProperties,
			new ArrayList<>(this.treeManipulators),
			this.registeredTreeManipulators,
			this.propertyConfigurers,
			this.validOnly,
			this.monkeyContext
		);
	}

	private static boolean isSameType(Property p1, Property p2) {
		boolean notMapEntry = !(p1 instanceof MapEntryElementProperty) || !(p2 instanceof MapEntryElementProperty);
		return notMapEntry && p1.getAnnotatedType().getType().equals(p2.getAnnotatedType().getType());
	}
}
