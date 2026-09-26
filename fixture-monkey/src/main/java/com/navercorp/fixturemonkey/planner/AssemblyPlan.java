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

package com.navercorp.fixturemonkey.planner;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.customizer.ScopeSet;
import com.navercorp.fixturemonkey.tree.NodeTreeFactory;
import com.navercorp.objectfarm.api.input.InlinedValueResolver;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;

/**
 * Plan produced by {@link AssemblyPlanner} that downstream {@code Assembler}s consume to assemble values.
 * <p>
 * This plan contains:
 * <ul>
 *   <li>Values projection extracted from manipulators, over the generated JvmNodeTree</li>
 *   <li>The analyzed root scope and defined scopes</li>
 *   <li>What assembly builds with the same way planning did: the node tree factory and the inlined value
 *   resolver</li>
 * </ul>
 *
 * @see AssemblyPlanner
 * @see AnalysisResult
 * @see ValueProjection
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class AssemblyPlan {
	private final ValueProjection values;
	private final AnalysisResult analysisResult;
	private final List<AnalyzedScope> analyzedDefinedScopes;
	private final ScopeSet scopeSet;
	private final NodeTreeFactory nodeTreeFactory;
	private final InlinedValueResolver inlinedValueResolver;
	private final ConcurrentHashMap<?, ?> typeMetadataCache;
	private final long analyzeTimeNanos;
	private final long treeBuildTimeNanos;

	/**
	 * Creates a new AssemblyPlan.
	 *
	 * @param values                the values projection extracted from manipulators, over the generated tree
	 * @param analysisResult        the analysis of the root scope
	 * @param analyzedDefinedScopes the analyzed defined scopes, the one with the lowest precedence first
	 * @param scopeSet              the scopes of the sample, asked which instantiators apply where
	 * @param nodeTreeFactory       builds the trees assembly needs on demand the way this plan builds its own
	 * @param inlinedValueResolver  decomposes a value passed to {@code set(...)} the way planning did
	 * @param typeMetadataCache     metadata assembly derives per type, kept across samples
	 * @param analyzeTimeNanos      time spent in ManipulatorAnalyzer.analyze() in nanoseconds
	 * @param treeBuildTimeNanos    time spent building the JvmNodeTree in nanoseconds
	 */
	public AssemblyPlan(
		ValueProjection values,
		AnalysisResult analysisResult,
		List<AnalyzedScope> analyzedDefinedScopes,
		ScopeSet scopeSet,
		NodeTreeFactory nodeTreeFactory,
		InlinedValueResolver inlinedValueResolver,
		ConcurrentHashMap<?, ?> typeMetadataCache,
		long analyzeTimeNanos,
		long treeBuildTimeNanos
	) {
		this.values = values;
		this.analysisResult = analysisResult;
		this.analyzedDefinedScopes = analyzedDefinedScopes;
		this.scopeSet = scopeSet;
		this.nodeTreeFactory = nodeTreeFactory;
		this.inlinedValueResolver = inlinedValueResolver;
		this.typeMetadataCache = typeMetadataCache;
		this.analyzeTimeNanos = analyzeTimeNanos;
		this.treeBuildTimeNanos = treeBuildTimeNanos;
	}

	/**
	 * Returns the generated JvmNodeTree.
	 *
	 * @return the JvmNodeTree with immutable topology
	 */
	public JvmNodeTree getNodeTree() {
		return values.getStructure();
	}

	/**
	 * Returns the values projection extracted from manipulators.
	 *
	 * @return the ValueProjection containing values mapped to nodes
	 */
	public ValueProjection getValues() {
		return values;
	}

	/**
	 * Returns the analysis of the root scope.
	 *
	 * @return the analysis result
	 */
	public AnalysisResult getAnalysisResult() {
		return analysisResult;
	}

	/**
	 * Returns what the root scope declared, by path from the root.
	 *
	 * @return the analyzed root scope
	 */
	public AnalyzedScope getAnalyzedRootScope() {
		return analysisResult.getAnalyzedRootScope();
	}

	/**
	 * Returns what each defined scope declared, by path relative to the node the scope selects.
	 *
	 * @return the analyzed defined scopes, the one with the lowest precedence first
	 */
	public List<AnalyzedScope> getAnalyzedDefinedScopes() {
		return analyzedDefinedScopes;
	}

	/**
	 * Returns the scopes of the sample, which decide the instantiators to build with where an instance sits.
	 *
	 * @return the scope set
	 */
	public ScopeSet getScopeSet() {
		return scopeSet;
	}

	/**
	 * Returns the factory building the trees assembly needs on demand the way this plan builds its own.
	 *
	 * @return the node tree factory
	 */
	public NodeTreeFactory getNodeTreeFactory() {
		return nodeTreeFactory;
	}

	/**
	 * Returns the {@link InlinedValueResolver} applied while decomposing a value passed to {@code set(...)}, so that
	 * assembly decomposes the value the same way planning did.
	 *
	 * @return the inlined value resolver
	 */
	public InlinedValueResolver getInlinedValueResolver() {
		return inlinedValueResolver;
	}

	/**
	 * Returns the metadata assembly derives per type, kept across samples. Its entries are typed by assembly.
	 *
	 * @return the type metadata cache
	 */
	public ConcurrentHashMap<?, ?> getTypeMetadataCache() {
		return typeMetadataCache;
	}

	/**
	 * Returns the time spent analyzing the root scope.
	 *
	 * @return the analysis time in nanoseconds
	 */
	public long getAnalyzeTimeNanos() {
		return analyzeTimeNanos;
	}

	/**
	 * Returns the time spent building the tree.
	 *
	 * @return the tree build time in nanoseconds
	 */
	public long getTreeBuildTimeNanos() {
		return treeBuildTimeNanos;
	}
}
