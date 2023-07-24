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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.context.MonkeyGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.customizer.NodeManipulator;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ObjectTree {
	private final RootProperty rootProperty;
	private final ObjectNode rootNode;
	private final FixtureMonkeyOptions fixtureMonkeyOptions;
	private final ObjectTreeMetadata metadata;
	private final MonkeyContext monkeyContext;

	public ObjectTree(
		RootProperty rootProperty,
		ObjectNode rootNode,
		FixtureMonkeyOptions fixtureMonkeyOptions,
		MonkeyContext monkeyContext
	) {
		this.rootProperty = rootProperty;
		this.rootNode = rootNode;
		this.fixtureMonkeyOptions = fixtureMonkeyOptions;
		this.monkeyContext = monkeyContext;
		MetadataCollector metadataCollector = new MetadataCollector(rootNode);
		this.metadata = metadataCollector.collect();
	}

	public ObjectTreeMetadata getMetadata() {
		return metadata;
	}

	public void manipulate(NodeResolver nodeResolver, NodeManipulator nodeManipulator) {
		List<ObjectNode> nodes = nodeResolver.resolve(rootNode);

		for (ObjectNode node : nodes) {
			nodeManipulator.manipulate(node);
			node.addManipulator(nodeManipulator);
		}
	}

	public CombinableArbitrary<?> generate() {
		return generateIntrospected(rootNode, null);
	}

	private ArbitraryGeneratorContext generateContext(
		ObjectNode objectNode,
		@Nullable ArbitraryGeneratorContext parentContext
	) {
		Map<ArbitraryProperty, ObjectNode> childNodesByArbitraryProperty = new HashMap<>();
		List<ArbitraryProperty> childrenProperties = new ArrayList<>();

		ArbitraryProperty arbitraryProperty = objectNode.getArbitraryProperty();
		Property resolvedParentProperty = objectNode.getResolvedProperty();
		List<ObjectNode> children = objectNode.getChildren().stream()
			.filter(it -> resolvedParentProperty.equals(it.getResolvedParentProperty()))
			.collect(Collectors.toList());

		for (ObjectNode childNode : children) {
			childNodesByArbitraryProperty.put(childNode.getArbitraryProperty(), childNode);
			childrenProperties.add(childNode.getArbitraryProperty());
		}

		MonkeyGeneratorContext monkeyGeneratorContext = monkeyContext.retrieveGeneratorContext(rootProperty);
		return new ArbitraryGeneratorContext(
			resolvedParentProperty,
			arbitraryProperty,
			childrenProperties,
			parentContext,
			(currentContext, prop) -> {
				ObjectNode node = childNodesByArbitraryProperty.get(prop);
				if (node == null) {
					return CombinableArbitrary.NOT_GENERATED;
				}

				return generateIntrospected(node, currentContext);
			},
			monkeyGeneratorContext,
			fixtureMonkeyOptions.getGenerateUniqueMaxTries()
		);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private CombinableArbitrary<?> generateIntrospected(
		ObjectNode node,
		@Nullable ArbitraryGeneratorContext currentContext
	) {
		CombinableArbitrary<?> generated;
		if (node.getArbitrary() != null) {
			generated = node.getArbitrary()
				.injectNull(node.getArbitraryProperty().getObjectProperty().getNullInject());
		} else {
			ArbitraryGeneratorContext childArbitraryGeneratorContext = this.generateContext(node, currentContext);

			CombinableArbitrary<?> cached = monkeyContext.getCachedArbitrary(node.getProperty());

			if (node.isNotManipulated() && cached != null) {
				generated = cached;
			} else {
				generated = this.fixtureMonkeyOptions.getArbitraryGenerator(node.getResolvedProperty())
					.generate(childArbitraryGeneratorContext);

				if (node.isNotManipulated()) {
					monkeyContext.putCachedArbitrary(
						node.getProperty(),
						generated
					);
				}
			}
		}

		List<Predicate> arbitraryFilters = node.getArbitraryFilters();
		for (Predicate predicate : arbitraryFilters) {
			generated = generated.filter(fixtureMonkeyOptions.getGenerateMaxTries(), predicate);
		}

		return generated;
	}
}
