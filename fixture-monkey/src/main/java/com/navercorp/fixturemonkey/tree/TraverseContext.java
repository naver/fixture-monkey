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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.tree.TreeProperty;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;

/**
 * {@link FixtureMonkey} → {@link ArbitraryBuilder} → {@link ObjectTree} → {@link CombinableArbitrary}
 * 						1:N							1:N					1:1
 * <p>
 * It is a context within {@link ObjectTree}. It represents a status of the {@link ObjectTree}.
 * The {@link ObjectTree} should be the same if the {@link TraverseContext} is the same.
 * <p>
 * It is for internal use only. It can be changed or removed at any time.
 */
@API(since = "0.4.0", status = Status.INTERNAL)
public final class TraverseContext {
	private final List<TreeProperty> treeProperties;
	private final List<ContainerInfoManipulator> containerInfoManipulators;
	private final List<MatcherOperator<List<ContainerInfoManipulator>>> registeredContainerInfoManipulators;
	private final Map<Class<?>, List<Property>> propertyConfigurers;
	private final Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorConfigurer;
	private final Supplier<Boolean> validOnly;
	private final MonkeyContext monkeyContext;

	public TraverseContext(
		List<TreeProperty> treeProperties,
		List<ContainerInfoManipulator> containerInfoManipulators,
		List<MatcherOperator<List<ContainerInfoManipulator>>> registeredContainerInfoManipulators,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorConfigurer,
		Supplier<Boolean> validOnly,
		MonkeyContext monkeyContext
	) {
		this.treeProperties = treeProperties;
		this.containerInfoManipulators = containerInfoManipulators;
		this.registeredContainerInfoManipulators = registeredContainerInfoManipulators;
		this.propertyConfigurers = propertyConfigurers;
		this.arbitraryIntrospectorConfigurer = arbitraryIntrospectorConfigurer;
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

	public List<ContainerInfoManipulator> getContainerInfoManipulators() {
		return containerInfoManipulators;
	}

	public Map<Class<?>, List<Property>> getPropertyConfigurers() {
		return propertyConfigurers;
	}

	public Map<Class<?>, ArbitraryIntrospector> getArbitraryIntrospectorConfigurer() {
		return arbitraryIntrospectorConfigurer;
	}

	public boolean isValidOnly() {
		return validOnly.get();
	}

	public MonkeyContext getMonkeyContext() {
		return monkeyContext;
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
			propertyConfigurers,
			this.arbitraryIntrospectorConfigurer,
			this.validOnly,
			this.monkeyContext
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

	public TraverseContext withParentProperties() {
		List<TreeProperty> newTreeProperties = new ArrayList<>();

		if (!this.treeProperties.isEmpty()) {
			newTreeProperties.add(this.treeProperties.get(this.treeProperties.size() - 1));
		}

		return new TraverseContext(
			newTreeProperties,
			new ArrayList<>(this.containerInfoManipulators),
			this.registeredContainerInfoManipulators,
			this.propertyConfigurers,
			this.arbitraryIntrospectorConfigurer,
			this.validOnly,
			this.monkeyContext
		);
	}

	private static boolean isSameType(Property p1, Property p2) {
		boolean notMapEntry = !(p1 instanceof MapEntryElementProperty) || !(p2 instanceof MapEntryElementProperty);
		return notMapEntry && p1.getAnnotatedType().getType().equals(p2.getAnnotatedType().getType());
	}
}
