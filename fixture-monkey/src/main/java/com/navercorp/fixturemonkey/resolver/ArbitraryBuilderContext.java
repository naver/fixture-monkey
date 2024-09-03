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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryBuilderContext {
	private final List<ArbitraryManipulator> manipulators;
	private final List<ContainerInfoManipulator> containerInfoManipulators;
	private final Map<Class<?>, List<Property>> propertyConfigurers;
	private final Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorsByType;
	private final List<MatcherOperator<? extends ArbitraryBuilder<?>>> selectedArbitraryBuilders;

	private boolean validOnly;

	@Nullable
	private FixedState fixedState = null;
	@Nullable
	private CombinableArbitrary<?> fixedCombinableArbitrary;

	public ArbitraryBuilderContext(
		List<ArbitraryManipulator> manipulators,
		List<ContainerInfoManipulator> containerInfoManipulators,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorsByType,
		boolean validOnly,
		@Nullable FixedState fixedState,
		@Nullable CombinableArbitrary<?> fixedCombinableArbitrary,
		List<MatcherOperator<? extends ArbitraryBuilder<?>>> selectedArbitraryBuilders
	) {
		this.manipulators = manipulators;
		this.containerInfoManipulators = containerInfoManipulators;
		this.propertyConfigurers = propertyConfigurers;
		this.arbitraryIntrospectorsByType = arbitraryIntrospectorsByType;
		this.validOnly = validOnly;
		this.fixedState = fixedState;
		this.fixedCombinableArbitrary = fixedCombinableArbitrary;
		this.selectedArbitraryBuilders = selectedArbitraryBuilders;
	}

	public ArbitraryBuilderContext() {
		this(
			new ArrayList<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>(), true, null, null, new ArrayList<>()
		);
	}

	public ArbitraryBuilderContext copy() {
		List<ContainerInfoManipulator> copiedContainerInfoManipulators = this.containerInfoManipulators.stream()
			.map(ContainerInfoManipulator::copy)
			.collect(Collectors.toList());

		return new ArbitraryBuilderContext(
			new ArrayList<>(this.manipulators),
			copiedContainerInfoManipulators,
			new HashMap<>(propertyConfigurers),
			new HashMap<>(arbitraryIntrospectorsByType),
			this.validOnly,
			fixedState,
			fixedCombinableArbitrary,
			selectedArbitraryBuilders
		);
	}

	public void addManipulator(ArbitraryManipulator arbitraryManipulator) {
		this.manipulators.add(arbitraryManipulator);
	}

	public void addManipulators(Collection<ArbitraryManipulator> arbitraryManipulators) {
		this.manipulators.addAll(arbitraryManipulators);
	}

	public List<ArbitraryManipulator> getManipulators() {
		return Collections.unmodifiableList(manipulators);
	}

	public void addContainerInfoManipulator(ContainerInfoManipulator containerInfo) {
		this.containerInfoManipulators.add(containerInfo);
	}

	public void addContainerInfoManipulators(List<ContainerInfoManipulator> containerInfoManipulators) {
		this.containerInfoManipulators.addAll(containerInfoManipulators);
	}

	public List<ContainerInfoManipulator> getContainerInfoManipulators() {
		return Collections.unmodifiableList(containerInfoManipulators);
	}

	public void putPropertyConfigurer(Class<?> type, List<Property> propertyConfigurer) {
		this.propertyConfigurers.put(type, propertyConfigurer);
	}

	public void putArbitraryIntrospector(Class<?> type, ArbitraryIntrospector arbitraryIntrospector) {
		this.arbitraryIntrospectorsByType.put(type, arbitraryIntrospector);
	}

	public void addSelectedArbitraryBuilders(List<MatcherOperator<? extends ArbitraryBuilder<?>>> arbitraryBuilders) {
		this.selectedArbitraryBuilders.addAll(arbitraryBuilders);
	}

	public List<MatcherOperator<? extends ArbitraryBuilder<?>>> getSelectedArbitraryBuilders() {
		return Collections.unmodifiableList(selectedArbitraryBuilders);
	}

	public Map<Class<?>, ArbitraryIntrospector> getArbitraryIntrospectorsByType() {
		return arbitraryIntrospectorsByType;
	}

	public Map<Class<?>, List<Property>> getPropertyConfigurers() {
		return propertyConfigurers;
	}

	public void setValidOnly(boolean validOnly) {
		this.validOnly = validOnly;
	}

	public boolean isValidOnly() {
		return validOnly;
	}

	public void markFixed() {
		if (fixedState != null
			&& fixedState.getFixedManipulateSize() == this.manipulators.size()
			&& fixedState.getFixedContainerManipulatorSize() == this.containerInfoManipulators.size()) {
			return;
		}

		fixedState = new FixedState(this.manipulators.size(), this.containerInfoManipulators.size());
		fixedCombinableArbitrary = null;
	}

	public boolean isFixed() {
		return fixedState != null;
	}

	public boolean fixedExpired() {
		return manipulators.size() > Objects.requireNonNull(fixedState).getFixedManipulateSize()
			|| containerInfoManipulators.size() > fixedState.getFixedContainerManipulatorSize();
	}

	public void renewFixed(CombinableArbitrary<?> fixedCombinableArbitrary) {
		this.markFixed();
		this.fixedCombinableArbitrary = fixedCombinableArbitrary;
	}

	@Nullable
	public CombinableArbitrary<?> getFixedCombinableArbitrary() {
		return fixedCombinableArbitrary;
	}

	private static class FixedState {
		private final int fixedManipulateSize;
		private final int fixedContainerManipulatorSize;

		public FixedState(int fixedManipulateSize, int fixedContainerManipulatorSize) {
			this.fixedManipulateSize = fixedManipulateSize;
			this.fixedContainerManipulatorSize = fixedContainerManipulatorSize;
		}

		public int getFixedManipulateSize() {
			return fixedManipulateSize;
		}

		public int getFixedContainerManipulatorSize() {
			return fixedContainerManipulatorSize;
		}
	}
}
