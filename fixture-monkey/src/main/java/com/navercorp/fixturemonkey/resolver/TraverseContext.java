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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
final class TraverseContext {
	private final ArbitraryProperty rootArbitraryProperty;
	private final List<ArbitraryProperty> arbitraryProperties;
	private final List<ContainerInfoManipulator> containerInfoManipulators;

	public TraverseContext(
		ArbitraryProperty rootArbitraryProperty,
		List<ArbitraryProperty> arbitraryProperties,
		List<ContainerInfoManipulator> containerInfoManipulators
	) {
		this.rootArbitraryProperty = rootArbitraryProperty;
		this.arbitraryProperties = arbitraryProperties;
		this.containerInfoManipulators = containerInfoManipulators;
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
		return new TraverseContext(rootArbitraryProperty, arbitraryProperties, containerInfoManipulators);
	}

	public boolean isTraversed(Property property) {
		return property.equals(rootArbitraryProperty.getObjectProperty().getProperty())
			|| arbitraryProperties.stream()
			.anyMatch(it -> property.equals(it.getObjectProperty().getProperty()));
	}
}
