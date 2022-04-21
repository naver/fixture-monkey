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

public final class ArbitraryResolver {
	private final ArbitraryTraverser traverser;
	private final GenerateOptions generateOptions;

	public ArbitraryResolver(ArbitraryTraverser traverser, GenerateOptions generateOptions) {
		this.traverser = traverser;
		this.generateOptions = generateOptions;
	}

	public Arbitrary<?> resolve(RootProperty rootProperty) {
		ArbitraryNode arbitraryNode = this.traverser.traverse(rootProperty);

		// manipulating 표현식 개수만큼 순회

		ArbitraryGeneratorContext context = this.generateContext(arbitraryNode, null);
		return this.generateOptions.getArbitraryGenerator(rootProperty).generate(context);
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

				Arbitrary<?> arbitrary = node.getArbitrary();
				if (arbitrary != null) {
					return arbitrary;
				}

				return this.generateOptions.getArbitraryGenerator(prop.getProperty())
					.generate(this.generateContext(node, ctx));
			}
		);
	}
}
