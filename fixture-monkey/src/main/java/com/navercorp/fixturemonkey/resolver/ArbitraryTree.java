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

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.RootProperty;

final class ArbitraryTree {
	private final RootProperty rootProperty;
	private final ArbitraryNode rootNode;
	private final GenerateOptions generateOptions;

	ArbitraryTree(
		RootProperty rootProperty,
		ArbitraryNode rootNode,
		GenerateOptions generateOptions
	) {
		this.rootProperty = rootProperty;
		this.rootNode = rootNode;
		this.generateOptions = generateOptions;
	}

	Arbitrary<?> generate() {
		return this.generateOptions.getArbitraryGenerator(rootProperty).generate(generateContext());
	}

	private ArbitraryGeneratorContext generateContext() {
		return doGenerateContext(rootNode, null);
	}

	private ArbitraryGeneratorContext doGenerateContext(
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

				Arbitrary<?> arbitrary = node.getArbitrary();
				if (arbitrary != null) {
					return arbitrary;
				}

				return this.generateOptions.getArbitraryGenerator(prop.getProperty())
					.generate(this.doGenerateContext(node, ctx));
			}
		);
	}
}
