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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryBuilderContext {
	private final List<ArbitraryManipulator> manipulators;
	private final Set<LazyArbitrary<?>> lazyArbitraries;
	@SuppressWarnings("rawtypes")
	private final List<MatcherOperator<? extends FixtureCustomizer>> customizers;
	private final List<ContainerInfoManipulator> containerInfoManipulators;

	private boolean validOnly;

	@SuppressWarnings("rawtypes")
	public ArbitraryBuilderContext(
		List<ArbitraryManipulator> manipulators,
		Set<LazyArbitrary<?>> lazyArbitraries,
		List<MatcherOperator<? extends FixtureCustomizer>> customizers,
		List<ContainerInfoManipulator> containerInfoManipulators,
		boolean validOnly
	) {
		this.manipulators = manipulators;
		this.lazyArbitraries = lazyArbitraries;
		this.customizers = customizers;
		this.containerInfoManipulators = containerInfoManipulators;
		this.validOnly = validOnly;
	}

	public ArbitraryBuilderContext() {
		this(new ArrayList<>(), new HashSet<>(), new ArrayList<>(), new ArrayList<>(), true);
	}

	public ArbitraryBuilderContext copy() {
		List<ContainerInfoManipulator> copiedContainerInfoManipulators = this.containerInfoManipulators.stream()
			.map(it -> new ContainerInfoManipulator(
					it.getNodeResolver(),
					it.getContainerInfo()
				)
			)
			.collect(Collectors.toList());

		return new ArbitraryBuilderContext(
			new ArrayList<>(this.manipulators),
			new HashSet<>(this.lazyArbitraries),
			new ArrayList<>(this.customizers),
			copiedContainerInfoManipulators,
			this.validOnly
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

	public void addLazyArbitrary(LazyArbitrary<?> lazyArbitrary) {
		this.lazyArbitraries.add(lazyArbitrary);
	}

	public void addLazyArbitraries(Collection<LazyArbitrary<?>> lazyArbitraries) {
		this.lazyArbitraries.addAll(lazyArbitraries);
	}

	public Set<LazyArbitrary<?>> getLazyArbitraries() {
		return Collections.unmodifiableSet(lazyArbitraries);
	}

	@SuppressWarnings("rawtypes")
	public void addCustomizer(MatcherOperator<? extends FixtureCustomizer> fixtureCustomizer) {
		this.customizers.add(fixtureCustomizer);
	}

	@SuppressWarnings("rawtypes")
	public List<MatcherOperator<? extends FixtureCustomizer>> getCustomizers() {
		return Collections.unmodifiableList(customizers);
	}

	public void addContainerInfoManipulator(NodeResolver nodeResolver, ArbitraryContainerInfo containerInfo) {
		this.containerInfoManipulators.add(new ContainerInfoManipulator(nodeResolver, containerInfo));
	}

	public void addContainerInfoManipulators(List<ContainerInfoManipulator> containerInfoManipulators) {
		this.containerInfoManipulators.addAll(containerInfoManipulators);
	}

	public List<ContainerInfoManipulator> getContainerInfoManipulators() {
		return Collections.unmodifiableList(containerInfoManipulators);
	}

	public void setValidOnly(boolean validOnly) {
		this.validOnly = validOnly;
	}

	public boolean isValidOnly() {
		return validOnly;
	}
}
