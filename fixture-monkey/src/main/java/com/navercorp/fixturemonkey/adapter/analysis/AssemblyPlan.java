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

package com.navercorp.fixturemonkey.adapter.analysis;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.AssemblyPlanner;
import com.navercorp.fixturemonkey.adapter.projection.ValueProjection;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.tree.PathResolverContext;

/**
 * Plan produced by {@link AssemblyPlanner} that downstream {@code Assembler}s consume to assemble values.
 * <p>
 * This plan contains:
 * <ul>
 *   <li>The generated JvmNodeTree with immutable topology</li>
 *   <li>Values projection extracted from manipulators</li>
 *   <li>The original analysis result for further processing</li>
 * </ul>
 * <p>
 * Values can be accessed via {@link #getValues()} which returns a {@link ValueProjection}
 * for node-based and path-based access.
 *
 * @see AssemblyPlanner
 * @see AnalysisResult
 * @see ValueProjection
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class AssemblyPlan {
	private final JvmNodeTree nodeTree;
	private final ValueProjection values;
	/**
	 * May be null for default implementations.
	 */
	private final AnalysisResult analysisResult;

	private final long analyzeTimeNanos;
	private final long treeBuildTimeNanos;
	private final boolean cacheHit;
	private final @Nullable PathResolverContext resolverContext;

	/**
	 * Creates a new AssemblyPlan.
	 *
	 * @param nodeTree       the generated JvmNodeTree
	 * @param values         the values projection extracted from manipulators
	 * @param analysisResult the original analysis result (may be null for default implementations)
	 */
	public AssemblyPlan(JvmNodeTree nodeTree, ValueProjection values, AnalysisResult analysisResult) {
		this(nodeTree, values, analysisResult, 0, 0, false, null);
	}

	/**
	 * Creates a new AssemblyPlan with timing information.
	 *
	 * @param nodeTree          the generated JvmNodeTree
	 * @param values            the values projection extracted from manipulators
	 * @param analysisResult    the original analysis result (may be null for default implementations)
	 * @param analyzeTimeNanos  time spent in ManipulatorAnalyzer.analyze() in nanoseconds
	 * @param treeBuildTimeNanos time spent building the JvmNodeTree in nanoseconds
	 */
	public AssemblyPlan(
		JvmNodeTree nodeTree,
		ValueProjection values,
		AnalysisResult analysisResult,
		long analyzeTimeNanos,
		long treeBuildTimeNanos
	) {
		this(nodeTree, values, analysisResult, analyzeTimeNanos, treeBuildTimeNanos, false, null);
	}

	/**
	 * Creates a new AssemblyPlan with timing, cache, and resolver-context information.
	 *
	 * @param nodeTree           the generated JvmNodeTree
	 * @param values             the values projection extracted from manipulators
	 * @param analysisResult     the original analysis result (may be null for default implementations)
	 * @param analyzeTimeNanos   time spent in ManipulatorAnalyzer.analyze() in nanoseconds
	 * @param treeBuildTimeNanos time spent building the JvmNodeTree in nanoseconds
	 * @param cacheHit           whether this result was retrieved from cache
	 * @param resolverContext    the path resolver context produced during planning, used by the
	 *                           {@code RuntimeTreeFactory} during assembly
	 */
	public AssemblyPlan(
		JvmNodeTree nodeTree,
		ValueProjection values,
		AnalysisResult analysisResult,
		long analyzeTimeNanos,
		long treeBuildTimeNanos,
		boolean cacheHit,
		@Nullable PathResolverContext resolverContext
	) {
		this.nodeTree = nodeTree;
		this.values = values;
		this.analysisResult = analysisResult;
		this.analyzeTimeNanos = analyzeTimeNanos;
		this.treeBuildTimeNanos = treeBuildTimeNanos;
		this.cacheHit = cacheHit;
		this.resolverContext = resolverContext;
	}

	/**
	 * Returns the generated JvmNodeTree.
	 *
	 * @return the JvmNodeTree with immutable topology
	 */
	public JvmNodeTree getNodeTree() {
		return nodeTree;
	}

	/**
	 * Returns the values projection extracted from manipulators.
	 * <p>
	 * The projection provides node-based and path-based access to values.
	 * This is the preferred way to access values.
	 *
	 * @return the ValueProjection containing values mapped to nodes
	 */
	public ValueProjection getValues() {
		return values;
	}

	/**
	 * Returns the original analysis result.
	 *
	 * @return the analysis result, or null if not available
	 */
	public AnalysisResult getAnalysisResult() {
		return analysisResult;
	}

	/**
	 * Returns the time spent in ManipulatorAnalyzer.analyze() in nanoseconds.
	 *
	 * @return analyze time in nanoseconds
	 */
	public long getAnalyzeTimeNanos() {
		return analyzeTimeNanos;
	}

	/**
	 * Returns the time spent building the JvmNodeTree in nanoseconds.
	 *
	 * @return tree build time in nanoseconds
	 */
	public long getTreeBuildTimeNanos() {
		return treeBuildTimeNanos;
	}

	/**
	 * Returns whether this result was retrieved from cache.
	 *
	 * @return true if the result was a cache hit, false otherwise
	 */
	public boolean isCacheHit() {
		return cacheHit;
	}

	/**
	 * Returns the {@link PathResolverContext} produced during planning.
	 * <p>
	 * This is the same context the {@code RuntimeTreeFactory} should see during assembly so
	 * that anonymous-tree creation makes the same resolution decisions as the planned tree.
	 *
	 * @return the resolver context, or {@code null} when not produced by the planner
	 */
	public @Nullable PathResolverContext getResolverContext() {
		return resolverContext;
	}
}
