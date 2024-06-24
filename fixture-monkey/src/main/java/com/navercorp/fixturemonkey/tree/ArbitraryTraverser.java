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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache;
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
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.DefaultCandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryTraverser {
	private final FixtureMonkeyOptions fixtureMonkeyOptions;
	private final ConcurrentLruCache<Property, List<Property>> candidateConcretePropertiesByProperty;

	public ArbitraryTraverser(FixtureMonkeyOptions fixtureMonkeyOptions) {
		this.fixtureMonkeyOptions = fixtureMonkeyOptions;
		this.candidateConcretePropertiesByProperty = new ConcurrentLruCache<>(1024);
	}

	public ObjectNode traverse(
		Property property,
		List<ContainerInfoManipulator> containerInfoManipulators,
		List<MatcherOperator<List<ContainerInfoManipulator>>> registeredContainerInfoManipulators,
		Map<Class<?>, List<Property>> propertyConfigurers
	) {
		TraverseContext traverseContext = new TraverseContext(
			new ArrayList<>(),
			containerInfoManipulators,
			registeredContainerInfoManipulators,
			propertyConfigurers
		);

		return generateObjectNode(
			null,
			property,
			null,
			traverseContext
		);
	}

	private ObjectNode generateObjectNode(
		@Nullable Property resolvedParentProperty,
		Property property,
		@Nullable Integer propertySequence,
		TraverseContext context
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

		ArbitraryProperty parentArbitraryProperty = context.getLastArbitraryProperty();
		Integer index = null;
		if (resolvedParentProperty != null && parentArbitraryProperty != null) {
			index = getIndex(resolvedParentProperty, parentArbitraryProperty, propertySequence);
		}

		ObjectPropertyGeneratorContext objectPropertyGeneratorContext = new ObjectPropertyGeneratorContext(
			property,
			index,
			parentArbitraryProperty,
			container,
			getPropertyGenerator(context.getPropertyConfigurers()),
			fixtureMonkeyOptions.getPropertyNameResolver(property),
			fixtureMonkeyOptions.getNullInjectGenerator(property)
		);

		ObjectProperty objectProperty = objectPropertyGenerator.generate(objectPropertyGeneratorContext);

		Map<Property, List<Property>> childPropertyListsByCandidateProperty;
		ContainerInfoManipulator appliedContainerInfoManipulator = null;
		if (container) {
			List<ObjectProperty> objectProperties =
				context.getArbitraryProperties().stream()
					.map(ArbitraryProperty::getObjectProperty).collect(Collectors.toList());
			objectProperties.add(objectProperty);

			appliedContainerInfoManipulator = resolveAppliedContainerInfoManipulator(
				context.getContainerInfoManipulators(),
				objectProperties
			);

			ArbitraryContainerInfo containerInfo = appliedContainerInfoManipulator != null
				? appliedContainerInfoManipulator.getContainerInfo()
				: null;
			ContainerProperty childContainerProperty = containerPropertyGenerator.generate(
				new ContainerPropertyGeneratorContext(
					property,
					index,
					containerInfo,
					fixtureMonkeyOptions.getArbitraryContainerInfoGenerator(property)
				)
			);

			List<Property> candidateProperties = resolveCandidateProperties(property);
			childPropertyListsByCandidateProperty = candidateProperties.stream()
				.collect(
					Collectors.toMap(
						Function.identity(),
						it -> childContainerProperty.getElementProperties()
					)
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
		TraverseContext nextTraverseContext = context.appendArbitraryProperty(arbitraryProperty);

		List<ObjectNode> children = new ArrayList<>();
		for (
			Entry<Property, List<Property>> childPropertiesByCandidateProperty :
			childPropertyListsByCandidateProperty.entrySet()
		) {
			List<Property> childProperties = childPropertiesByCandidateProperty.getValue();
			Property candidateProperty = childPropertiesByCandidateProperty.getKey();

			children.addAll(
				generateChildrenNodes(
					candidateProperty,
					childProperties,
					nextTraverseContext
				)
			);
		}

		Property resolvedProperty = new ArrayList<>(childPropertyListsByCandidateProperty.keySet())
			.get(Randoms.nextInt(childPropertyListsByCandidateProperty.size()));

		ObjectNode objectNode = new ObjectNode(
			resolvedParentProperty,
			resolvedProperty,
			arbitraryProperty,
			children
		);

		if (appliedContainerInfoManipulator != null) {
			objectNode.addContainerManipulator(appliedContainerInfoManipulator);
		}
		return objectNode;
	}

	@Nullable
	private Integer getIndex(
		Property resolvedParentProperty,
		ArbitraryProperty parentArbitraryProperty,
		Integer propertySequence
	) {
		boolean parentContainer =
			fixtureMonkeyOptions.getContainerPropertyGenerator(resolvedParentProperty) != null;
		if (!parentContainer) {
			return null;
		}

		int index = propertySequence;
		if (parentArbitraryProperty.getObjectProperty().getProperty() instanceof MapEntryElementProperty) {
			index /= 2;
		}
		return index;
	}

	private List<ObjectNode> generateChildrenNodes(
		Property resolvedParentProperty,
		List<Property> childProperties,
		TraverseContext context
	) {
		List<ObjectNode> children = new ArrayList<>();

		for (int sequence = 0; sequence < childProperties.size(); sequence++) {
			Property childProperty = childProperties.get(sequence);

			if (context.isTraversed(childProperty)
				&& !(resolvedParentProperty instanceof MapEntryElementProperty)) {
				continue;
			}

			ObjectNode childNode = generateObjectNode(
				resolvedParentProperty,
				childProperty,
				sequence,
				context
			);
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

	private List<Property> resolveCandidateProperties(Property property) {
		CandidateConcretePropertyResolver candidateConcretePropertyResolver =
			fixtureMonkeyOptions.getCandidateConcretePropertyResolver(property);

		if (candidateConcretePropertyResolver == null) {
			return DefaultCandidateConcretePropertyResolver.INSTANCE.resolve(property);
		}

		return candidateConcretePropertiesByProperty.computeIfAbsent(
			property,
			p -> {
				List<Property> resolvedCandidateProperties = new ArrayList<>();
				List<Property> candidateProperties = candidateConcretePropertyResolver.resolve(p);
				for (Property candidateProperty : candidateProperties) {
					resolvedCandidateProperties.addAll(resolveCandidateProperties(candidateProperty));
				}
				return resolvedCandidateProperties;
			}
		);
	}
}
