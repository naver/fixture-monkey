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

package com.navercorp.fixturemonkey.customizer;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.matcher.ExactTypeMatcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.customizer.ScopeSelector;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.FixedContainerSizeResolver;
import com.navercorp.objectfarm.api.node.JavaDefaultNodePromoter;
import com.navercorp.objectfarm.api.node.JavaMapNodePromoter;
import com.navercorp.objectfarm.api.node.JavaNodeContext;
import com.navercorp.objectfarm.api.node.JavaObjectNodePromoter;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.node.JvmNodePromoter;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTree;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.tree.JvmNodeTreeTransformer;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

class ScopeChainTest {
	private static final List<JvmNodePromoter> PROMOTERS = Arrays.asList(
		new JavaMapNodePromoter(),
		new JavaObjectNodePromoter()
	);

	private static final JavaNodeContext CONTEXT = JavaNodeContext.builder()
		.seed(-1L)
		.nodePromoters(Collections.singletonList(new JavaDefaultNodePromoter(PROMOTERS)))
		.containerSizeResolver(new FixedContainerSizeResolver(2))
		.build();

	private static final Function<JvmNode, Property> NO_PROPERTY = node -> {
		throw new AssertionError("property is not needed");
	};

	@Test
	void rootScopeSelectsOnlyTheNodeASampleStartsFrom() {
		// given
		ScopeChain chain = chainOf("$.child.name");
		ScopeSelector root = ScopeSelector.root();

		// when, then
		then(chain.selects(root, 0)).isTrue();
		then(chain.selects(root, 1)).isFalse();
		then(chain.selects(root, 2)).isFalse();
	}

	@Test
	void typeScopeSelectsNodeOfItsTypeAtAnyDepth() {
		// given
		ScopeChain chain = chainOf("$.child.name");
		ScopeSelector childScope = ScopeSelector.of(new ExactTypeMatcher(Child.class));

		// when, then
		then(chain.selects(childScope, 0)).isFalse();
		then(chain.selects(childScope, 1)).isTrue();
		then(chain.selects(childScope, 2)).isFalse();
	}

	@Test
	void pathBelowScopeNodeMatchesFieldWildcard() {
		// given
		ScopeChain chain = chainOf("$.child.name");

		// when, then
		then(chain.matchesBelow(1, PathExpression.of("$.*"))).isTrue();
		then(chain.matchesBelow(0, PathExpression.of("$.*"))).isFalse();
		then(chain.matchesBelow(0, PathExpression.of("$.child.*"))).isTrue();
	}

	private static ScopeChain chainOf(String path) {
		JvmNodeCandidateTree candidateTree =
			new JvmNodeCandidateTree.Builder(new ReflectiveJvmType(Parent.class), CONTEXT).build();
		JvmNodeTree tree = new JvmNodeTreeTransformer(CONTEXT).transform(candidateTree);
		return ScopeChain.ofPath(PathExpression.of(path), tree::resolve, NO_PROPERTY);
	}

	@SuppressWarnings("unused")
	private static class Parent {
		private Child child;
	}

	@SuppressWarnings("unused")
	private static class Child {
		private String name;
	}
}
