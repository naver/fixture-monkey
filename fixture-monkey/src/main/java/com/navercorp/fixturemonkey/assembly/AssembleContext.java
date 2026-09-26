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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.context.MonkeyGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorLoggingContext;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.planner.AssemblyPlan;
import com.navercorp.fixturemonkey.tracing.TraceContext;

/**
 * Context for ValueProjectionAssembler.assemble() containing all necessary information
 * to generate objects from a ValueProjection.
 * <p>
 * This class encapsulates:
 * <ul>
 *   <li>The {@link AssemblyPlan} being assembled, with the scopes it was made from</li>
 *   <li>MonkeyContext for options and generator context</li>
 *   <li>TreeRootProperty for root type information</li>
 *   <li>TraceContext for collecting assembly debugging information</li>
 * </ul>
 * <p>
 * What the scopes declared and which declaration wins at a path are not kept here; assembly looks them up in the
 * plan's scopes.
 * <p>
 * Use the builder pattern to create instances:
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

	/**
	 * Trace context for collecting assembly debugging information.
	 * Uses NoOp pattern - never null, always safe to call methods on.
	 */
	private final TraceContext traceContext;

	private AssembleContext(Builder builder) {
		this.plan = builder.plan;
		this.rootProperty = builder.rootProperty;
		this.options = builder.monkeyContext.getFixtureMonkeyOptions();
		this.generatorContext = builder.monkeyContext.newGeneratorContext(builder.rootProperty);
		this.loggingContext = new ArbitraryGeneratorLoggingContext(this.options.isEnableLoggingFail());
		this.traceContext = builder.traceContext != null ? builder.traceContext : TraceContext.noOp();
	}

	/**
	 * Returns the plan being assembled: its tree and values, the scopes it was made from, and the node tree factory
	 * that builds the trees assembly needs on demand.
	 *
	 * @return the plan
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
	 * Returns the trace context for collecting assembly debugging information.
	 * Never returns null - uses NoOp pattern when tracing is disabled.
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
