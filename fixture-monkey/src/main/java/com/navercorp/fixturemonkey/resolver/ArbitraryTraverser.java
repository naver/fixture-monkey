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
import java.util.Map;
import java.util.Map.Entry;

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
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryTraverser {
	private final GenerateOptions generateOptions;

	public ArbitraryTraverser(GenerateOptions generateOptions) {
		this.generateOptions = generateOptions;
	}

	public ArbitraryNode traverse(
		Property property,
		Map<NodeResolver, ArbitraryContainerInfo> arbitraryContainerInfosByNodeResolver
	) {
		ContainerPropertyGenerator containerPropertyGenerator =
			this.generateOptions.getContainerPropertyGenerator(property);
		boolean container = containerPropertyGenerator != null;

		ObjectPropertyGenerator objectPropertyGenerator;
		if (container) {
			objectPropertyGenerator = SingleValueObjectPropertyGenerator.INSTANCE;
		} else {
			objectPropertyGenerator = this.generateOptions.getObjectPropertyGenerator(property);
		}

		ObjectProperty objectProperty = objectPropertyGenerator.generate(
			new ObjectPropertyGeneratorContext(
				property,
				null,
				null,
				container,
				this.generateOptions
			)
		);

		ContainerProperty containerProperty = null;
		if (container) {
			ArbitraryContainerInfo containerInfo =
				arbitraryContainerInfosByNodeResolver.get(IdentityNodeResolver.INSTANCE);
			containerProperty = containerPropertyGenerator.generate(
				new ContainerPropertyGeneratorContext(
					property,
					null,
					containerInfo,
					generateOptions
				)
			);
		}

		ArbitraryProperty arbitraryProperty = new ArbitraryProperty(
			objectProperty,
			containerProperty
		);
		return this.traverse(
			arbitraryProperty,
			new TraverseContext(
				new ArrayList<>(),
				arbitraryContainerInfosByNodeResolver
			)
		);
	}

	private ArbitraryNode traverse(
		ArbitraryProperty arbitraryProperty,
		TraverseContext context
	) {
		ObjectProperty objectProperty = arbitraryProperty.getObjectProperty();
		ContainerProperty containerProperty = arbitraryProperty.getContainerProperty();

		List<ArbitraryNode> children;
		if (containerProperty != null) {
			List<Property> elementProperties = containerProperty.getElementProperties();
			children = generateChildrenNodes(elementProperties, arbitraryProperty, context);
		} else {
			List<Property> childProperties = objectProperty.getChildProperties();
			children = generateChildrenNodes(childProperties, arbitraryProperty, context);
		}

		return new ArbitraryNode(
			arbitraryProperty,
			children
		);
	}

	private List<ArbitraryNode> generateChildrenNodes(
		List<Property> properties,
		ArbitraryProperty parentArbitraryProperty,
		TraverseContext context
	) {
		List<ArbitraryNode> children = new ArrayList<>();
		Map<NodeResolver, ArbitraryContainerInfo> arbitraryContainerInfosByNodeResolver =
			context.getArbitraryContainerInfosByNodeResolver();
		boolean container = parentArbitraryProperty.getContainerProperty() != null;

		for (int sequence = 0; sequence < properties.size(); sequence++) {
			Property childProperty = properties.get(sequence);

			ContainerPropertyGenerator containerPropertyGenerator =
				this.generateOptions.getContainerPropertyGenerator(childProperty);
			boolean childContainer = containerPropertyGenerator != null;

			ObjectPropertyGenerator objectPropertyGenerator;
			if (childContainer) {
				objectPropertyGenerator = SingleValueObjectPropertyGenerator.INSTANCE;
			} else {
				objectPropertyGenerator = this.generateOptions.getObjectPropertyGenerator(childProperty);
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
					this.generateOptions
				)
			);

			ContainerProperty childContainerProperty = null;
			if (childContainer) {
				ArbitraryContainerInfo containerInfo = arbitraryContainerInfosByNodeResolver.entrySet().stream()
					.filter(it -> isMatch(
							context.getArbitraryProperties(),
							childObjectProperty,
							it.getKey().toNextNodePredicate()
						)
					)
					.map(Entry::getValue)
					.findAny()
					.orElse(null);

				childContainerProperty = containerPropertyGenerator.generate(
					new ContainerPropertyGeneratorContext(
						childProperty,
						container ? index : null,
						containerInfo,
						generateOptions
					)
				);
			}

			ArbitraryProperty childArbitraryProperty = new ArbitraryProperty(
				childObjectProperty,
				childContainerProperty
			);
			ArbitraryNode childNode = this.traverse(
				childArbitraryProperty,
				context.appendArbitraryProperty(childArbitraryProperty)
			);
			children.add(childNode);
		}
		return children;
	}

	private boolean isMatch(
		List<ArbitraryProperty> parentArbitraryProperties,
		ObjectProperty currentObjectProperty,
		List<NextNodePredicate> nextNodePredicates
	) {
		int parentArbitraryPropertySize = parentArbitraryProperties.size();
		int nextNodePredicateSize = nextNodePredicates.size();

		if (parentArbitraryPropertySize + 1 != nextNodePredicateSize) {
			return false;
		}

		for (int i = 0; i < parentArbitraryPropertySize; i++) {
			NextNodePredicate nextNodePredicate = nextNodePredicates.get(i);
			ArbitraryProperty parentArbitraryProperty = i == 0 ? null : parentArbitraryProperties.get(i - 1);
			ArbitraryProperty currentArbitraryProperty = parentArbitraryProperties.get(i);

			if (!nextNodePredicate.test(
				parentArbitraryProperty,
				currentArbitraryProperty.getObjectProperty(),
				currentArbitraryProperty.getContainerProperty()
			)) {
				return false;
			}
		}

		ArbitraryProperty parentArbitraryProperty = parentArbitraryPropertySize == 0
			? null
			: parentArbitraryProperties.get(parentArbitraryPropertySize - 1);
		NextNodePredicate nextNodePredicate = nextNodePredicates.get(nextNodePredicateSize - 1);
		return nextNodePredicate.test(
			parentArbitraryProperty,
			currentObjectProperty,
			null
		);
	}
}
