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

import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.MapKeyElementProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
final class ArbitraryTree {
	private final ArbitraryNode rootNode;
	private final GenerateOptions generateOptions;
	private final ArbitraryTreeMetadata metadata;
	@SuppressWarnings("rawtypes")
	private final List<MatcherOperator<? extends FixtureCustomizer>> customizers;
	private final List<MatcherOperator<Arbitrary<?>>> cachedArbitraries;

	@SuppressWarnings("rawtypes")
	ArbitraryTree(
		ArbitraryNode rootNode,
		GenerateOptions generateOptions,
		List<MatcherOperator<? extends FixtureCustomizer>> customizers,
		List<MatcherOperator<Arbitrary<?>>> cachedArbitraries
	) {
		this.rootNode = rootNode;
		this.generateOptions = generateOptions;
		this.customizers = customizers;
		this.cachedArbitraries = cachedArbitraries;
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
		ArbitraryGeneratorContext context = generateContext(rootNode, customizers, null);
		return generateArbitrary(context, rootNode);
	}

	@SuppressWarnings("rawtypes")
	private ArbitraryGeneratorContext generateContext(
		ArbitraryNode arbitraryNode,
		List<MatcherOperator<? extends FixtureCustomizer>> customizers,
		@Nullable ArbitraryGeneratorContext parentContext
	) {
		Map<ArbitraryProperty, ArbitraryNode> childNodesByArbitraryProperty = new HashMap<>();
		List<ArbitraryProperty> childrenProperties = new ArrayList<>();
		for (ArbitraryNode childNode : arbitraryNode.getChildren()) {
			childNodesByArbitraryProperty.put(childNode.getArbitraryProperty(), childNode);
			childrenProperties.add(childNode.getArbitraryProperty());
		}
		List<MatcherOperator<? extends FixtureCustomizer>> arbitraryCustomizers = new ArrayList<>();
		arbitraryCustomizers.addAll(generateOptions.getArbitraryCustomizers());
		arbitraryCustomizers.addAll(customizers);

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
			},
			arbitraryCustomizers
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
				.injectNull(node.getArbitraryProperty().getObjectProperty().getNullInject());
		} else {
			ArbitraryGeneratorContext childArbitraryGeneratorContext = this.generateContext(node, customizers, ctx);

			Arbitrary<?> cached = cachedArbitraries.stream()
				.filter(it -> it.match(node.getProperty()))
				.findAny()
				.map(MatcherOperator::getOperator)
				.orElse(null);

			if (node.isNotManipulated() && cached != null) {
				generated = cached;
			} else {
				generated = this.generateOptions.getArbitraryGenerator(prop.getObjectProperty().getProperty())
					.generate(childArbitraryGeneratorContext);

				if (node.isNotManipulated()) {
					if (isUnique(node)) {
						generated = generated.injectDuplicates(0.0d);
					}

					cachedArbitraries.add(
						new MatcherOperator<>(
							it -> it.equals(node.getProperty()),
							generated
						)
					);
				}
			}
		}

		List<Predicate> arbitraryFilters = node.getArbitraryFilters();
		for (Predicate predicate : arbitraryFilters) {
			generated = generated.filter(predicate);
		}

		return generated.map(
			object -> ctx.getArbitraryCustomizers().stream()
				.filter(it -> it.match(node.getProperty()))
				.map(MatcherOperator::getOperator)
				.findFirst()
				.map(it -> it.customizeFixture(object))
				.orElse(object)
		);
	}

	private boolean isUnique(ArbitraryNode node) {
		return node.getProperty() instanceof MapKeyElementProperty;
	}

}
