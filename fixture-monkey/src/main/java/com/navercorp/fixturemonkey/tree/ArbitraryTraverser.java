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
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
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
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.type.Types;
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
		List<MatcherOperator<List<ContainerInfoManipulator>>> registeredContainerInfoManipulators,
		Map<Class<?>, List<Property>> propertyConfigurers
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
				getPropertyGenerator(propertyConfigurers),
				fixtureMonkeyOptions.getPropertyNameResolver(property),
				fixtureMonkeyOptions.getNullInjectGenerator(property)
			)
		);

		ContainerProperty containerProperty = null;
		ContainerInfoManipulator containerInfoManipulator = null;
		if (container) {
			containerInfoManipulator = resolveAppliedContainerInfoManipulator(
				containerInfoManipulators,
				Collections.singletonList(objectProperty)
			);
			ArbitraryContainerInfo containerInfo = containerInfoManipulator != null
				? containerInfoManipulator.getContainerInfo()
				: null;

			containerProperty = containerPropertyGenerator.generate(
				new ContainerPropertyGeneratorContext(
					property,
					null,
					containerInfo,
					fixtureMonkeyOptions.getArbitraryContainerInfoGenerator(property)
				)
			);
		}

		Map<Property, List<Property>> childPropertyListsByCandidateProperty;
		if (container) {
			childPropertyListsByCandidateProperty = Collections.singletonMap(
				property,
				containerProperty.getElementProperties()
			);
		} else {
			childPropertyListsByCandidateProperty = objectProperty.getChildPropertyListsByCandidateProperty();
		}

		ArbitraryProperty arbitraryProperty = new ArbitraryProperty(
			objectProperty,
			container,
			objectProperty.getNullInject(),
			childPropertyListsByCandidateProperty
		);

		List<ArbitraryProperty> parentArbitraryProperties = new ArrayList<>();
		parentArbitraryProperties.add(arbitraryProperty);

		ObjectNode rootNode = generateObjectNode(
			arbitraryProperty,
			null,
			container,
			new TraverseContext(
				arbitraryProperty,
				parentArbitraryProperties,
				containerInfoManipulators,
				registeredContainerInfoManipulators,
				propertyConfigurers
			)
		);

		if (containerInfoManipulator != null) {
			rootNode.addContainerManipulator(containerInfoManipulator);
		}
		return rootNode;
	}

	private PropertyGenerator getPropertyGenerator(Map<Class<?>, List<Property>> propertyConfigurers) {
		return property -> {
			Class<?> type = Types.getActualType(property.getType());
			List<Property> propertyConfigurer = propertyConfigurers.get(type);
			if (propertyConfigurer != null) {
				return propertyConfigurer;
			}

			PropertyGenerator propertyGenerator = fixtureMonkeyOptions.getOptionalPropertyGenerator(property);
			if (propertyGenerator != null) {
				return propertyGenerator.generateChildProperties(property);
			}

			ArbitraryGenerator defaultArbitraryGenerator = fixtureMonkeyOptions.getDefaultArbitraryGenerator();

			PropertyGenerator defaultArbitraryGeneratorPropertyGenerator =
				defaultArbitraryGenerator.getRequiredPropertyGenerator(property);

			if (defaultArbitraryGeneratorPropertyGenerator != null) {
				return defaultArbitraryGeneratorPropertyGenerator.generateChildProperties(property);
			}

			return fixtureMonkeyOptions.getDefaultPropertyGenerator().generateChildProperties(property);
		};
	}

	private ObjectNode generateObjectNode(
		ArbitraryProperty arbitraryProperty,
		@Nullable Property resolvedParentProperty,
		boolean container,
		TraverseContext context
	) {
		List<ObjectNode> children = new ArrayList<>();

		Map<Property, List<Property>> childPropertyListsByCandidateProperty =
			arbitraryProperty.getChildPropertyListsByCandidateProperty();

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
					container,
					candidateProperty,
					context
				)
			);
		}

		Property resolvedProperty = new ArrayList<>(childPropertyListsByCandidateProperty.keySet())
			.get(Randoms.nextInt(childPropertyListsByCandidateProperty.size()));

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
		boolean parentContainer,
		Property resolvedParentProperty,
		TraverseContext context
	) {
		List<ObjectNode> children = new ArrayList<>();
		List<ContainerInfoManipulator> containerInfoManipulators = context.getContainerInfoManipulators();

		for (int sequence = 0; sequence < childProperties.size(); sequence++) {
			Property childProperty = childProperties.get(sequence);

			if (context.isTraversed(childProperty) && !(resolvedParentProperty instanceof MapEntryElementProperty)) {
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
					parentContainer ? index : null,
					parentArbitraryProperty,
					childContainer,
					getPropertyGenerator(context.getPropertyConfigurers()),
					fixtureMonkeyOptions.getPropertyNameResolver(childProperty),
					fixtureMonkeyOptions.getNullInjectGenerator(childProperty)
				)
			);

			Map<Property, List<Property>> childPropertyListsByCandidateProperty;

			ContainerProperty childContainerProperty = null;
			ContainerInfoManipulator appliedContainerInfoManipulator = null;
			if (childContainer) {
				List<ObjectProperty> objectProperties =
					context.getArbitraryProperties().stream()
						.map(ArbitraryProperty::getObjectProperty).collect(Collectors.toList());
				objectProperties.add(childObjectProperty);

				appliedContainerInfoManipulator = resolveAppliedContainerInfoManipulator(
					containerInfoManipulators,
					objectProperties
				);
				ArbitraryContainerInfo containerInfo = appliedContainerInfoManipulator != null
					? appliedContainerInfoManipulator.getContainerInfo()
					: null;
				childContainerProperty = containerPropertyGenerator.generate(
					new ContainerPropertyGeneratorContext(
						childProperty,
						parentContainer ? index : null,
						containerInfo,
						fixtureMonkeyOptions.getArbitraryContainerInfoGenerator(childProperty)
					)
				);
				childPropertyListsByCandidateProperty = Collections.singletonMap(
					childProperty,
					childContainerProperty.getElementProperties()
				);
			} else {
				childPropertyListsByCandidateProperty = childObjectProperty.getChildPropertyListsByCandidateProperty();
			}

			ArbitraryProperty childArbitraryProperty = new ArbitraryProperty(
				childObjectProperty,
				childContainer,
				childObjectProperty.getNullInject(),
				childPropertyListsByCandidateProperty
			);

			ObjectNode childNode = generateObjectNode(
				childArbitraryProperty,
				resolvedParentProperty,
				childContainer,
				context.appendArbitraryProperty(childArbitraryProperty)
			);

			if (appliedContainerInfoManipulator != null) {
				childNode.addContainerManipulator(appliedContainerInfoManipulator);
			}
			children.add(childNode);
		}
		return children;
	}

	@Nullable
	private ContainerInfoManipulator resolveAppliedContainerInfoManipulator(
		List<ContainerInfoManipulator> containerInfoManipulators,
		List<ObjectProperty> objectProperties
	) {
		ContainerInfoManipulator appliedContainerInfoManipulator = null;
		for (ContainerInfoManipulator containerInfoManipulator : containerInfoManipulators) {
			if (containerInfoManipulator.isMatch(objectProperties)) {
				appliedContainerInfoManipulator = containerInfoManipulator;
			}
		}
		return appliedContainerInfoManipulator;
	}
}
