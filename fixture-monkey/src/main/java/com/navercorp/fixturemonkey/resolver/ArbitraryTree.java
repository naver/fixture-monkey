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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
final class ArbitraryTree {
	private final ArbitraryNode rootNode;
	private final GenerateOptions generateOptions;
	private final ArbitraryTreeMetadata metadata;

	ArbitraryTree(
		ArbitraryNode rootNode,
		GenerateOptions generateOptions
	) {
		this.rootNode = rootNode;
		this.generateOptions = generateOptions;
		MetadataCollector metadataCollector = new MetadataCollector(rootNode);
		this.metadata = metadataCollector.collect();
	}

	public ArbitraryTreeMetadata getMetadata() {
		return metadata;
	}

	ArbitraryNode findRoot() {
		return rootNode;
	}

	Arbitrary<?> generate() {
		ArbitraryGeneratorContext context = generateContext(rootNode, null);
		return generateArbitrary(context, rootNode);
	}

	private ArbitraryGeneratorContext generateContext(
		ArbitraryNode arbitraryNode,
		@Nullable ArbitraryGeneratorContext parentContext
	) {
		Map<ArbitraryProperty, ArbitraryNode> childNodesByArbitraryProperty = new HashMap<>();
		List<ArbitraryProperty> childrenProperties = new ArrayList<>();
		for (ArbitraryNode childNode : arbitraryNode.getChildren()) {
			childNodesByArbitraryProperty.put(childNode.getArbitraryProperty(), childNode);
			childrenProperties.add(childNode.getArbitraryProperty());
		}

		return new ArbitraryGeneratorContext(
			arbitraryNode.getArbitraryProperty(),
			childrenProperties,
			parentContext,
			(ctx, prop) -> {
				ArbitraryNode node = childNodesByArbitraryProperty.get(prop);
				if (node == null) {
					return Arbitraries.just(null);
				}

				return generateArbitrary(ctx, node);
			}
		);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Arbitrary<?> generateArbitrary(
		ArbitraryGeneratorContext ctx,
		ArbitraryNode node
	) {
		ArbitraryProperty prop = node.getArbitraryProperty();

		Arbitrary<?> generated;
		if (node.getArbitrary() != null) {
			generated = node.getArbitrary() // fixed
				.injectNull(node.getArbitraryProperty().getNullInject());
		} else {
			ArbitraryGeneratorContext childArbitraryGeneratorContext = this.generateContext(node, ctx);
			generated = this.generateOptions.getArbitraryGenerator(prop.getProperty())
				.generate(childArbitraryGeneratorContext);
		}

		List<Predicate> arbitraryFilters = node.getArbitraryFilters();
		for (Predicate predicate : arbitraryFilters) {
			generated = generated.filter(predicate);
		}
		return generated;
	}
}
