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

import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.instantiator.InstantiatorProcessResult;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.nodecandidate.InterfaceMethodNodeCandidateGenerator;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.JavaNodeContext;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.node.JvmNodeContext;
import com.navercorp.objectfarm.api.tree.ExpansionContext;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTree;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTreeContext;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.tree.JvmNodeTreeTransformer;
import com.navercorp.objectfarm.api.tree.PathResolverContext;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Builds the root tree and every subtree assembly asks for the same way — with one set of options, path resolvers,
 * global instantiators and the paths the root scope's values reach — as the subtree at the requested position.
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public final class DefaultNodeTreeFactory implements NodeTreeFactory {
	private final TreeContextCache treeCache;
	private final @Nullable FixtureMonkeyOptions options;
	private final PathResolverContext resolverContext;
	private final Map<Class<?>, InstantiatorProcessResult> instantiators;
	private final @Nullable ExpansionContext expansionContext;
	private final boolean shared;

	public DefaultNodeTreeFactory(
		TreeContextCache treeCache,
		@Nullable FixtureMonkeyOptions options,
		PathResolverContext resolverContext,
		Map<Class<?>, InstantiatorProcessResult> instantiators,
		@Nullable ExpansionContext expansionContext
	) {
		this.treeCache = treeCache;
		this.options = options;
		this.resolverContext = resolverContext;
		this.instantiators = instantiators;
		this.expansionContext = expansionContext;
		this.shared = instantiators.isEmpty();
	}

	@Override
	public JvmNodeTree createConcreteNodeTree(
		JvmType concreteType,
		JvmType declaredType,
		PathExpression path,
		List<JvmNode> ancestors
	) {
		return createConcrete(concreteType, declaredType, path, ancestors, null);
	}

	@Override
	public JvmNodeTree createContainerNodeTree(
		JvmType containerType,
		JvmType declaredType,
		PathExpression path,
		List<JvmNode> ancestors,
		int size
	) {
		return createConcrete(containerType, declaredType, path, ancestors, size);
	}

	@Override
	public @Nullable JvmNodeTree createAnonymousNodeTree(
		JvmType interfaceType,
		PathExpression path,
		List<JvmNode> ancestors
	) {
		if (options == null) {
			return null;
		}
		JavaNodeContext baseContext =
			(JavaNodeContext)treeCache.getOrBuildNodeContext(interfaceType, options, instantiators);
		JavaNodeContext anonymousContext = baseContext.withAdditionalGenerator(
			new InterfaceMethodNodeCandidateGenerator()
		);
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(interfaceType, anonymousContext)
			.withTreeContext(shared ? treeCache.getTreeContext() : new JvmNodeCandidateTreeContext())
			.withPreBuildResolvedTypes(true)
			.withSkipAbstractLeafCheck(true)
			.build();
		return transform(anonymousContext, candidateTree, path, ancestors, interfaceType, null);
	}

	private JvmNodeTree createConcrete(
		JvmType concreteType,
		JvmType declaredType,
		PathExpression path,
		List<JvmNode> ancestors,
		@Nullable Integer rootContainerSize
	) {
		JvmNodeContext context = treeCache.getOrBuildNodeContext(concreteType, options, instantiators);
		JvmNodeCandidateTree candidateTree = treeCache.getOrBuildCandidateTree(concreteType, context, options, !shared);
		return transform(context, candidateTree, path, ancestors, declaredType, rootContainerSize);
	}

	private JvmNodeTree transform(
		JvmNodeContext context,
		JvmNodeCandidateTree candidateTree,
		PathExpression path,
		List<JvmNode> ancestors,
		JvmType declaredType,
		@Nullable Integer rootContainerSize
	) {
		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			context,
			shared ? treeCache.getTreeContext() : new JvmNodeCandidateTreeContext(),
			resolverContext,
			expansionContext,
			shared ? treeCache.getSubtreeContext() : null
		);
		return transformer.transform(candidateTree, declaredType, rootContainerSize, ancestors, path);
	}
}
