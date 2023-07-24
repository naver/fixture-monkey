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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryBuilderContext {
	private final List<ArbitraryManipulator> manipulators;
	private final List<ContainerInfoManipulator> containerInfoManipulators;

	private boolean validOnly;

	@Nullable
	private FixedState fixedState = null;
	@Nullable
	private CombinableArbitrary<?> fixedCombinableArbitrary;

	public ArbitraryBuilderContext(
		List<ArbitraryManipulator> manipulators,
		List<ContainerInfoManipulator> containerInfoManipulators,
		boolean validOnly,
		@Nullable FixedState fixedState,
		@Nullable CombinableArbitrary<?> fixedCombinableArbitrary
	) {
		this.manipulators = manipulators;
		this.containerInfoManipulators = containerInfoManipulators;
		this.validOnly = validOnly;
		this.fixedState = fixedState;
		this.fixedCombinableArbitrary = fixedCombinableArbitrary;
	}

	public ArbitraryBuilderContext() {
		this(new ArrayList<>(), new ArrayList<>(), true, null, null);
	}

	public ArbitraryBuilderContext copy() {
		List<ContainerInfoManipulator> copiedContainerInfoManipulators = this.containerInfoManipulators.stream()
			.map(ContainerInfoManipulator::copy)
			.collect(Collectors.toList());

		return new ArbitraryBuilderContext(
			new ArrayList<>(this.manipulators),
			copiedContainerInfoManipulators,
			this.validOnly,
			fixedState,
			fixedCombinableArbitrary
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
