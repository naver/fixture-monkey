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
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.SingleValueObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryTraverser {
	private final FixtureMonkeyOptions fixtureMonkeyOptions;

	public ArbitraryTraverser(FixtureMonkeyOptions fixtureMonkeyOptions) {
		this.fixtureMonkeyOptions = fixtureMonkeyOptions;
	}

	public ObjectNode traverse(
		Property property,
		List<ContainerInfoManipulator> containerInfoManipulators,
		List<MatcherOperator<List<ContainerInfoManipulator>>> registeredContainerInfoManipulators
	) {
		ContainerPropertyGenerator containerPropertyGenerator =
			this.fixtureMonkeyOptions.getContainerPropertyGenerator(property);
		boolean container = containerPropertyGenerator != null;

		ObjectPropertyGenerator objectPropertyGenerator;
		if (container) {
			objectPropertyGenerator = SingleValueObjectPropertyGenerator.INSTANCE;
		} else {
			objectPropertyGenerator = this.fixtureMonkeyOptions.getObjectPropertyGenerator(property);
		}

		ObjectProperty objectProperty = objectPropertyGenerator.generate(
			new ObjectPropertyGeneratorContext(
				property,
				null,
				null,
				container,
				this.fixtureMonkeyOptions
			)
		);

		ContainerProperty containerProperty = null;
		ContainerInfoManipulator containerInfoManipulator = null;
		if (container) {
			containerInfoManipulator = containerInfoManipulators.stream()
				.filter(it -> it.isMatch(Collections.singletonList(objectProperty)))
				.findFirst()
				.orElse(null);
			ArbitraryContainerInfo containerInfo = containerInfoManipulator == null
				? null
				: containerInfoManipulator.getContainerInfo();

			containerProperty = containerPropertyGenerator.generate(
				new ContainerPropertyGeneratorContext(
					property,
					null,
					containerInfo,
					fixtureMonkeyOptions
				)
			);
		}

		ArbitraryProperty arbitraryProperty = new ArbitraryProperty(objectProperty, container);

		List<ArbitraryProperty> parentArbitraryProperties = new ArrayList<>();
		parentArbitraryProperties.add(arbitraryProperty);

		ObjectNode rootNode = this.traverse(
			arbitraryProperty,
			containerProperty,
			null,
			new TraverseContext(
				arbitraryProperty,
				parentArbitraryProperties,
				containerInfoManipulators,
				registeredContainerInfoManipulators
			)
		);

		if (containerInfoManipulator != null) {
			rootNode.addContainerManipulator(containerInfoManipulator);
		}
		return rootNode;
	}

	private ObjectNode traverse(
		ArbitraryProperty arbitraryProperty,
		@Nullable ContainerProperty containerProperty,
		@Nullable Property resolvedParentProperty,
		TraverseContext context
	) {
		List<ObjectNode> children = new ArrayList<>();
		ObjectProperty objectProperty = arbitraryProperty.getObjectProperty();
		boolean container = containerProperty != null;

		Property resolvedProperty;
		if (container) {
			resolvedProperty = objectProperty.getProperty();
			List<Property> elementProperties = containerProperty.getElementProperties();
			children.addAll(
				generateChildrenNodes(
					elementProperties,
					arbitraryProperty,
					containerProperty,
					objectProperty.getProperty(),
					context
				)
			);
		} else {
			Map<Property, List<Property>> childPropertyListsByCandidateProperty =
				objectProperty.getChildPropertyListsByCandidateProperty();

			for (
				Entry<Property, List<Property>> childPropertiesByCandidateProperty :
				childPropertyListsByCandidateProperty.entrySet()
			) {
				List<Property> childProperties = childPropertiesByCandidateProperty.getValue();
				Property candidateProperty = childPropertiesByCandidateProperty.getKey();

				children.addAll(
					generateChildrenNodes(
						childProperties,
						arbitraryProperty,
						null,
						candidateProperty,
						context
					)
				);
			}

			resolvedProperty = new ArrayList<>(childPropertyListsByCandidateProperty.keySet())
				.get(Randoms.nextInt(childPropertyListsByCandidateProperty.size()));
		}

		return new ObjectNode(
			resolvedParentProperty,
			resolvedProperty,
			arbitraryProperty,
			children
		);
	}

	private List<ObjectNode> generateChildrenNodes(
		List<Property> childProperties,
		ArbitraryProperty parentArbitraryProperty,
		@Nullable ContainerProperty parentContainerProperty,
		Property resolvedParentProperty,
		TraverseContext context
	) {
		List<ObjectNode> children = new ArrayList<>();
		List<ContainerInfoManipulator> containerInfoManipulators = context.getContainerInfoManipulators();
		boolean container = parentContainerProperty != null;

		for (int sequence = 0; sequence < childProperties.size(); sequence++) {
			Property childProperty = childProperties.get(sequence);

			if (context.isTraversed(childProperty)) {
				continue;
			}

			ContainerPropertyGenerator containerPropertyGenerator =
				this.fixtureMonkeyOptions.getContainerPropertyGenerator(childProperty);
			boolean childContainer = containerPropertyGenerator != null;

			ObjectPropertyGenerator objectPropertyGenerator;
			if (childContainer) {
				objectPropertyGenerator = SingleValueObjectPropertyGenerator.INSTANCE;
			} else {
				objectPropertyGenerator = this.fixtureMonkeyOptions.getObjectPropertyGenerator(childProperty);
			}

			int index = sequence;
			if (parentArbitraryProperty.getObjectProperty().getProperty() instanceof MapEntryElementProperty) {
				index /= 2;
			}

			ObjectProperty childObjectProperty = objectPropertyGenerator.generate(
				new ObjectPropertyGeneratorContext(
					childProperty,
					container ? index : null,
					parentArbitraryProperty,
					childContainer,
					this.fixtureMonkeyOptions
				)
			);

			ContainerProperty childContainerProperty = null;
			ContainerInfoManipulator appliedContainerInfoManipulator = null;
			if (childContainer) {
				List<ObjectProperty> objectProperties =
					context.getArbitraryProperties().stream()
						.map(ArbitraryProperty::getObjectProperty).collect(Collectors.toList());
				objectProperties.add(childObjectProperty);

				ArbitraryContainerInfo containerInfo = null;
				for (ContainerInfoManipulator containerInfoManipulator : containerInfoManipulators) {
					if (containerInfoManipulator.isMatch(objectProperties)) {
						containerInfo = containerInfoManipulator.getContainerInfo();
						appliedContainerInfoManipulator = containerInfoManipulator;
					}
				}
				childContainerProperty = containerPropertyGenerator.generate(
					new ContainerPropertyGeneratorContext(
						childProperty,
						container ? index : null,
						containerInfo,
						fixtureMonkeyOptions
					)
				);
			}

			ArbitraryProperty childArbitraryProperty = new ArbitraryProperty(
				childObjectProperty,
				childContainerProperty != null
			);

			ObjectNode childNode = this.traverse(
				childArbitraryProperty,
				childContainerProperty,
				resolvedParentProperty,
				context.appendArbitraryProperty(childArbitraryProperty)
			);

			if (appliedContainerInfoManipulator != null) {
				childNode.addContainerManipulator(appliedContainerInfoManipulator);
			}
			children.add(childNode);
		}
		return children;
	}
}
