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

package com.navercorp.fixturemonkey.assembly;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.context.MonkeyGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorLoggingContext;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.customizer.ScopeSet;
import com.navercorp.fixturemonkey.planner.AnalysisResult;
import com.navercorp.fixturemonkey.planner.AnalyzedScope;
import com.navercorp.fixturemonkey.planner.AssemblyPlan;
import com.navercorp.fixturemonkey.tracing.TraceContext;
import com.navercorp.fixturemonkey.tree.NodeTreeFactory;
import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Context for ValueProjectionAssembler.assemble() containing all necessary information
 * to generate objects from a ValueProjection.
 * <p>
 * This class encapsulates:
 * <ul>
 *   <li>The {@link AssemblyPlan} being assembled, with the scopes it was made from</li>
 *   <li>MonkeyContext for options and generator context</li>
 *   <li>TreeRootProperty for root type information</li>
 * </ul>
 * <p>
 * Path resolution follows the "more specific path wins" rule:
 * <ul>
 *   <li>If both "$.object" and "$.object.str" have values, "$.object.str" takes precedence for that field.</li>
 *   <li>This applies to all manipulations including setNull(), set(), etc.</li>
 * </ul>
 * <p>
 * The only exception is Values.just() (tracked in rootJustPaths):
 * <ul>
 *   <li>Values.just() creates truly immutable values where child paths are ignored.</li>
 *   <li>This is intentional: when a user sets Values.just(object), they want that exact object.</li>
 * </ul>
 * <p>
 * Use the builder path to create instances:
 * <pre>
 * AssembleContext context = AssembleContext.builder(monkeyContext, assemblyPlan)
 *     .rootProperty(rootProperty)
 *     .build();
 * </pre>
 *
 * @see ValueProjectionAssembler#assemble(AssembleContext)
 * @since 1.1.0
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class AssembleContext {
	private final AssemblyPlan plan;
	private final TreeRootProperty rootProperty;
	private final FixtureMonkeyOptions options;
	private final MonkeyGeneratorContext generatorContext;
	private final ArbitraryGeneratorLoggingContext loggingContext;
	private final Set<PathExpression> rootJustPaths;

	/**
	 * Paths the root scope declared a container size at via size() calls.
	 * These paths should block type-based values from defined scopes,
	 * since the root scope's size() should take precedence.
	 */
	private final Set<PathExpression> rootContainerSizePaths;

	/**
	 * Trace context for collecting assembly debugging information.
	 * Uses NoOp path - never null, always safe to call methods on.
	 */
	private final TraceContext traceContext;

	private AssembleContext(Builder builder) {
		this.plan = builder.plan;
		this.rootProperty = builder.rootProperty;
		this.options = builder.monkeyContext.getFixtureMonkeyOptions();
		this.generatorContext = builder.monkeyContext.newGeneratorContext(builder.rootProperty);
		this.loggingContext = new ArbitraryGeneratorLoggingContext(this.options.isEnableLoggingFail());
		AnalysisResult analysisResult = builder.plan.getAnalysisResult();
		this.rootJustPaths = Collections.unmodifiableSet(new HashSet<>(analysisResult.getJustPaths()));
		this.rootContainerSizePaths =
			Collections.unmodifiableSet(analysisResult.getLatestSizeDirectiveByPath().keySet());
		this.traceContext = builder.traceContext != null ? builder.traceContext : TraceContext.noOp();
	}

	/**
	 * Returns the plan being assembled: its tree and values, and the scopes it was made from.
	 */
	public AssemblyPlan getPlan() {
		return plan;
	}

	/**
	 * Returns the root property.
	 *
	 * @return the root property
	 */
	public TreeRootProperty getRootProperty() {
		return rootProperty;
	}

	/**
	 * Returns the fixture monkey options.
	 *
	 * @return the options
	 */
	public FixtureMonkeyOptions getOptions() {
		return options;
	}

	/**
	 * Returns the generator context.
	 *
	 * @return the generator context
	 */
	public MonkeyGeneratorContext getGeneratorContext() {
		return generatorContext;
	}

	/**
	 * Returns the logging context.
	 *
	 * @return the logging context
	 */
	public ArbitraryGeneratorLoggingContext getLoggingContext() {
		return loggingContext;
	}

	/**
	 * Returns the paths the root scope set via Values.just(), whose values are kept whole.
	 * <p>
	 * Child values under these paths are intentionally ignored.
	 * This is the ONLY exception to the "more specific path wins" rule.
	 * <p>
	 * Rationale: Values.just() means "use this exact object as-is".
	 * If a user wanted to modify child properties, they should use regular set() instead.
	 *
	 * @return unmodifiable set of just paths
	 */
	public Set<PathExpression> getRootJustPaths() {
		return rootJustPaths;
	}

	/**
	 * Returns the set of paths the root scope declared a container size at.
	 *
	 * @return the root scope's container size paths
	 */
	public Set<PathExpression> getRootContainerSizePaths() {
		return rootContainerSizePaths;
	}

	/**
	 * Returns the order of the root scope's values by path.
	 * <p>
	 * Used to determine priority when wildcard and specific paths conflict.
	 * Higher order values take precedence over lower order values.
	 *
	 * @return unmodifiable map of value orders by path
	 */
	public Map<PathExpression, Integer> getRootValueOrderByPath() {
		return plan.getAnalysisResult().getValueOrderByPath();
	}

	/**
	 * Returns what the root scope declared: not-null paths, filters, customizers and limits, by path from the root.
	 *
	 * @return the root scope's declarations
	 */
	public AnalyzedScope getAnalyzedRootScope() {
		return plan.getAnalyzedRootScope();
	}

	/**
	 * Returns the directives of each defined scope, by path relative to the node the scope selects.
	 *
	 * @return the directives of each defined scope
	 */
	public List<AnalyzedScope> getAnalyzedDefinedScopes() {
		return plan.getAnalyzedDefinedScopes();
	}

	/**
	 * Returns the scopes the plan was made from, which decide the instantiators to build with where an instance
	 * sits.
	 *
	 * @return the scope set
	 */
	public ScopeSet getScopeSet() {
		return plan.getScopeSet();
	}

	/**
	 * Returns the {@link NodeTreeFactory} used for building runtime-resolved trees during assembly.
	 *
	 * @return the node tree factory
	 */
	public NodeTreeFactory getNodeTreeFactory() {
		return plan.getNodeTreeFactory();
	}

	/**
	 * Returns the trace context for collecting assembly debugging information.
	 * Never returns null - uses NoOp path when tracing is disabled.
	 *
	 * @return the trace context (never null)
	 */
	public TraceContext getTraceContext() {
		return traceContext;
	}

	/**
	 * Creates a new builder for AssembleContext.
	 *
	 * @param monkeyContext the MonkeyContext to use
	 * @param plan          the plan to assemble
	 * @return a new Builder instance
	 */
	public static Builder builder(MonkeyContext monkeyContext, AssemblyPlan plan) {
		return new Builder(monkeyContext, plan);
	}

	/**
	 * Builder for creating AssembleContext instances.
	 */
	public static final class Builder {

		private final MonkeyContext monkeyContext;
		private final AssemblyPlan plan;
		private TreeRootProperty rootProperty;
		private TraceContext traceContext;

		private Builder(MonkeyContext monkeyContext, AssemblyPlan plan) {
			this.monkeyContext = monkeyContext;
			this.plan = plan;
		}

		/**
		 * Sets the root property.
		 *
		 * @param rootProperty the root property
		 * @return this builder
		 */
		public Builder rootProperty(TreeRootProperty rootProperty) {
			this.rootProperty = rootProperty;
			return this;
		}

		/**
		 * Sets the trace context for collecting assembly debugging information.
		 *
		 * @param traceContext the trace context (null will use NoOp)
		 * @return this builder
		 */
		public Builder traceContext(TraceContext traceContext) {
			this.traceContext = traceContext;
			return this;
		}

		/**
		 * Builds the AssembleContext.
		 *
		 * @return a new AssembleContext instance
		 * @throws IllegalStateException if rootProperty is not set
		 */
		public AssembleContext build() {
			if (rootProperty == null) {
				throw new IllegalStateException("rootProperty must be set");
			}
			return new AssembleContext(this);
		}
	}
}
