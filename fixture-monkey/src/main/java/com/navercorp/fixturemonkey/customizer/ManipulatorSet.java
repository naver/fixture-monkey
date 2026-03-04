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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.objectfarm.api.type.JvmType;

@API(since = "0.5.0", status = Status.MAINTAINED)
public final class ManipulatorSet {
	private final List<ArbitraryManipulator> arbitraryManipulators;
	private final List<ContainerInfoManipulator> containerInfoManipulators;

	/**
	 * Type-based container sizes from registered builders.
	 * Maps owner type -> (field name -> container info with min/max size).
	 * Used for applying container sizes to all instances of a type, regardless of path.
	 */
	private final Map<JvmType, Map<String, ArbitraryContainerInfo>> typedContainerSizes;

	/**
	 * Type-based set values from registered builders.
	 * Maps owner type -> (field path -> value).
	 * Used for applying set values to all instances of a type, regardless of path.
	 * Inner type values have higher priority than outer type wildcard values.
	 */
	private final Map<JvmType, Map<String, @Nullable Object>> typedValues;

	/**
	 * Property configurers from instantiate() calls.
	 * Maps target type -> list of properties to generate for that type.
	 * Used for applying property-based node generation via customGenerators.
	 */
	private final Map<Class<?>, List<Property>> propertyConfigurers;

	/**
	 * Type-specific introspectors from instantiate() calls.
	 * Maps target type -> ArbitraryIntrospector for that type.
	 * Used for determining how to construct objects of specific types.
	 */
	private final Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorsByType;
	private final boolean fixed;

	public ManipulatorSet(
		List<ArbitraryManipulator> arbitraryManipulators,
		List<ContainerInfoManipulator> containerInfoManipulators
	) {
		this(
			arbitraryManipulators,
			containerInfoManipulators,
			Collections.emptyMap(),
			Collections.emptyMap(),
			Collections.emptyMap(),
			Collections.emptyMap(),
			false
		);
	}

	public ManipulatorSet(
		List<ArbitraryManipulator> arbitraryManipulators,
		List<ContainerInfoManipulator> containerInfoManipulators,
		Map<JvmType, Map<String, ArbitraryContainerInfo>> typedContainerSizes,
		Map<JvmType, Map<String, @Nullable Object>> typedValues,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorsByType,
		boolean fixed
	) {
		this.arbitraryManipulators = arbitraryManipulators;
		this.containerInfoManipulators = containerInfoManipulators;
		this.typedContainerSizes = typedContainerSizes;
		this.typedValues = typedValues;
		this.propertyConfigurers = propertyConfigurers;
		this.arbitraryIntrospectorsByType = arbitraryIntrospectorsByType;
		this.fixed = fixed;
	}

	public List<ArbitraryManipulator> getArbitraryManipulators() {
		return arbitraryManipulators;
	}

	public List<ContainerInfoManipulator> getContainerInfoManipulators() {
		return containerInfoManipulators;
	}

	public Map<JvmType, Map<String, ArbitraryContainerInfo>> getTypedContainerSizes() {
		return typedContainerSizes;
	}

	public Map<JvmType, Map<String, @Nullable Object>> getTypedValues() {
		return typedValues;
	}

	public Map<Class<?>, List<Property>> getPropertyConfigurers() {
		return propertyConfigurers;
	}

	public Map<Class<?>, ArbitraryIntrospector> getArbitraryIntrospectorsByType() {
		return arbitraryIntrospectorsByType;
	}

	public boolean isFixed() {
		return fixed;
	}

	public boolean isEmpty() {
		return arbitraryManipulators.isEmpty()
			&& containerInfoManipulators.isEmpty()
			&& typedContainerSizes.isEmpty()
			&& typedValues.isEmpty()
			&& propertyConfigurers.isEmpty()
			&& arbitraryIntrospectorsByType.isEmpty();
	}
}
