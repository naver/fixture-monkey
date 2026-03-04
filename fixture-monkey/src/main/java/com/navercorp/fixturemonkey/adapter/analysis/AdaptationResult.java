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

import com.navercorp.fixturemonkey.adapter.projection.ValueProjection;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;

/**
 * Result of adapting a ManipulatorSet to a JvmNodeTree.
 * <p>
 * This class contains:
 * <ul>
 *   <li>The generated JvmNodeTree with immutable topology</li>
 *   <li>Values projection extracted from manipulators</li>
 *   <li>The original analysis result for further processing</li>
 * </ul>
 * <p>
 * Values can be accessed via {@link #getValues()} which returns a {@link ValueProjection}
 * for node-based and path-based access.
 *
 * @see NodeTreeAdapter
 * @see AnalysisResult
 * @see ValueProjection
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
public final class AdaptationResult {
	private final JvmNodeTree nodeTree;
	private final ValueProjection values;
	/**
	 * May be null for default implementations.
	 */
	private final AnalysisResult analysisResult;

	private final long analyzeTimeNanos;
	private final long treeBuildTimeNanos;
	private final boolean cacheHit;

	/**
	 * Creates a new AdaptationResult.
	 *
	 * @param nodeTree       the generated JvmNodeTree
	 * @param values         the values projection extracted from manipulators
	 * @param analysisResult the original analysis result (may be null for default implementations)
	 */
	public AdaptationResult(JvmNodeTree nodeTree, ValueProjection values, AnalysisResult analysisResult) {
		this(nodeTree, values, analysisResult, 0, 0, false);
	}

	/**
	 * Creates a new AdaptationResult with timing information.
	 *
	 * @param nodeTree          the generated JvmNodeTree
	 * @param values            the values projection extracted from manipulators
	 * @param analysisResult    the original analysis result (may be null for default implementations)
	 * @param analyzeTimeNanos  time spent in ManipulatorAnalyzer.analyze() in nanoseconds
	 * @param treeBuildTimeNanos time spent building the JvmNodeTree in nanoseconds
	 */
	public AdaptationResult(
		JvmNodeTree nodeTree,
		ValueProjection values,
		AnalysisResult analysisResult,
		long analyzeTimeNanos,
		long treeBuildTimeNanos
	) {
		this(nodeTree, values, analysisResult, analyzeTimeNanos, treeBuildTimeNanos, false);
	}

	/**
	 * Creates a new AdaptationResult with timing and cache information.
	 *
	 * @param nodeTree          the generated JvmNodeTree
	 * @param values            the values projection extracted from manipulators
	 * @param analysisResult    the original analysis result (may be null for default implementations)
	 * @param analyzeTimeNanos  time spent in ManipulatorAnalyzer.analyze() in nanoseconds
	 * @param treeBuildTimeNanos time spent building the JvmNodeTree in nanoseconds
	 * @param cacheHit          whether this result was retrieved from cache
	 */
	public AdaptationResult(
		JvmNodeTree nodeTree,
		ValueProjection values,
		AnalysisResult analysisResult,
		long analyzeTimeNanos,
		long treeBuildTimeNanos,
		boolean cacheHit
	) {
		this.nodeTree = nodeTree;
		this.values = values;
		this.analysisResult = analysisResult;
		this.analyzeTimeNanos = analyzeTimeNanos;
		this.treeBuildTimeNanos = treeBuildTimeNanos;
		this.cacheHit = cacheHit;
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
}
