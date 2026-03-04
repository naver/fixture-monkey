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

package com.navercorp.fixturemonkey.adapter.projection;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.NodeTreeAdapter;
import com.navercorp.fixturemonkey.adapter.analysis.AnalysisResult;
import com.navercorp.fixturemonkey.adapter.tracing.TraceContext;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.context.MonkeyGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorLoggingContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.option.InterfaceSelectionStrategy;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Context for ValueProjection.assemble() containing all necessary information
 * to generate objects from a ValueProjection.
 * <p>
 * This class encapsulates:
 * <ul>
 *   <li>MonkeyContext for options and generator context</li>
 *   <li>TreeRootProperty for root type information</li>
 *   <li>Just paths (for Values.just() - truly immutable values)</li>
 *   <li>Filters by path (post-conditions)</li>
 *   <li>Limits by path (for wildcard patterns)</li>
 * </ul>
 * <p>
 * Path resolution follows the "more specific path wins" rule:
 * <ul>
 *   <li>If both "$.object" and "$.object.str" have values, "$.object.str" takes precedence for that field.</li>
 *   <li>This applies to all manipulations including setNull(), set(), etc.</li>
 * </ul>
 * <p>
 * The only exception is Values.just() (tracked in justPaths):
 * <ul>
 *   <li>Values.just() creates truly immutable values where child paths are ignored.</li>
 *   <li>This is intentional: when a user sets Values.just(object), they want that exact object.</li>
 * </ul>
 * <p>
 * Use the builder pattern to create instances:
 * <pre>
 * AssembleContext context = AssembleContext.builder(monkeyContext)
 *     .rootProperty(rootProperty)
 *     .justPaths(justPaths)
 *     .filtersByPath(filtersByPath)
 *     .limitsByPath(limitsByPath)
 *     .build();
 * </pre>
 *
 * @see ValueProjection#assemble(AssembleContext)
 * @since 1.1.0
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
public final class AssembleContext {
	private final MonkeyContext monkeyContext;
	private final TreeRootProperty rootProperty;
	private final FixtureMonkeyOptions options;
	private final MonkeyGeneratorContext generatorContext;
	private final ArbitraryGeneratorLoggingContext loggingContext;
	private final Set<PathExpression> justPaths;
	private final Set<PathExpression> notNullPaths;
	private final Map<PathExpression, List<AnalysisResult.PostConditionFilter>> filtersByPath;
	private final Map<PathExpression, Integer> limitsByPath;
	private final InterfaceSelectionStrategy interfaceSelectionStrategy;
	/**
	 * TypeSelector-based path values converted from typedValues.
	 * Maps TypeSelector paths -> value, used as lower-priority entries in valuesByPath.
	 */
	private final Map<PathExpression, @Nullable Object> typedPathValues;

	/**
	 * Order numbers for typedPathValues entries.
	 * Negative values ensure they have lower priority than user-set values.
	 */
	private final Map<PathExpression, Integer> typedPathOrders;

	/**
	 * Trace context for collecting assembly debugging information.
	 * Uses NoOp pattern - never null, always safe to call methods on.
	 */
	private final TraceContext traceContext;

	/**
	 * Order of values by path. Used to determine priority when wildcard and specific paths conflict.
	 * Higher order values take precedence over lower order values.
	 */
	private final Map<PathExpression, Integer> valueOrderByPath;

	/**
	 * Customizers by path for customizeProperty operations.
	 * These are applied during assembly to transform the generated CombinableArbitrary.
	 */
	private final Map<PathExpression, List<AnalysisResult.PropertyCustomizer>> customizersByPath;

	/**
	 * Type-specific introspectors from instantiate() calls.
	 * Maps target type -> ArbitraryIntrospector for that type.
	 * Used for determining how to construct objects of specific types during assembly.
	 */
	private final Map<Class<?>, ArbitraryIntrospector> introspectorsByType;

	/**
	 * NodeTreeAdapter for building concrete type trees.
	 * Used to get properly structured trees for interface implementations.
	 */
	private final @Nullable NodeTreeAdapter nodeTreeAdapter;

	/**
	 * Paths where the user has explicitly set container sizes via size() calls.
	 * These paths should block type-based values from registered builders,
	 * since the user's size() should take precedence.
	 */
	private final Set<PathExpression> userContainerSizePaths;

	private AssembleContext(Builder builder) {
		this.monkeyContext = builder.monkeyContext;
		this.rootProperty = builder.rootProperty;
		this.options = builder.monkeyContext.getFixtureMonkeyOptions();
		this.generatorContext = builder.monkeyContext.newGeneratorContext(builder.rootProperty);
		this.loggingContext = new ArbitraryGeneratorLoggingContext(this.options.isEnableLoggingFail());
		this.justPaths = Collections.unmodifiableSet(new HashSet<>(builder.justPaths));
		this.notNullPaths = Collections.unmodifiableSet(new HashSet<>(builder.notNullPaths));
		this.filtersByPath = Collections.unmodifiableMap(new HashMap<>(builder.filtersByPath));
		this.limitsByPath = new HashMap<>(builder.limitsByPath); // Mutable for tracking during generation
		this.interfaceSelectionStrategy = builder.interfaceSelectionStrategy;
		this.traceContext = builder.traceContext != null ? builder.traceContext : TraceContext.noOp();
		this.valueOrderByPath = Collections.unmodifiableMap(new HashMap<>(builder.valueOrderByPath));
		this.customizersByPath = Collections.unmodifiableMap(new HashMap<>(builder.customizersByPath));
		this.introspectorsByType = Collections.unmodifiableMap(new HashMap<>(builder.introspectorsByType));
		this.nodeTreeAdapter = builder.nodeTreeAdapter;
		this.userContainerSizePaths = Collections.unmodifiableSet(new HashSet<>(builder.userContainerSizePaths));
		this.typedPathValues = Collections.unmodifiableMap(new HashMap<>(builder.typedPathValues));
		this.typedPathOrders = Collections.unmodifiableMap(new HashMap<>(builder.typedPathOrders));
	}

	/**
	 * Returns the MonkeyContext.
	 *
	 * @return the MonkeyContext
	 */
	public MonkeyContext getMonkeyContext() {
		return monkeyContext;
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
	 * Returns the just paths (paths set via Values.just() which are truly immutable).
	 * <p>
	 * Child values under these paths are intentionally ignored.
	 * This is the ONLY exception to the "more specific path wins" rule.
	 * <p>
	 * Rationale: Values.just() means "use this exact object as-is".
	 * If a user wanted to modify child properties, they should use regular set() instead.
	 *
	 * @return unmodifiable set of just paths
	 */
	public Set<PathExpression> getJustPaths() {
		return justPaths;
	}

	/**
	 * Returns the paths set via setNotNull() which require null injection to be 0.
	 * <p>
	 * When a path is in this set, the assembly process will override the null injection
	 * probability to 0 for that path, ensuring a non-null value is generated.
	 *
	 * @return unmodifiable set of not-null paths
	 */
	public Set<PathExpression> getNotNullPaths() {
		return notNullPaths;
	}

	/**
	 * Returns the filters by path.
	 *
	 * @return unmodifiable map of filters by path
	 */
	public Map<PathExpression, List<AnalysisResult.PostConditionFilter>> getFiltersByPath() {
		return filtersByPath;
	}

	/**
	 * Returns the mutable limits map for tracking during generation.
	 * <p>
	 * This map is mutable to allow decrementing limits as values are generated.
	 *
	 * @return mutable map of limits by path
	 */
	public Map<PathExpression, Integer> getLimitsByPath() {
		return limitsByPath;
	}

	/**
	 * Returns the interface selection strategy used for selecting implementations
	 * of interface or abstract types during assembly.
	 *
	 * @return the interface selection strategy
	 */
	public InterfaceSelectionStrategy getInterfaceSelectionStrategy() {
		return interfaceSelectionStrategy;
	}

	/**
	 * Returns the TypeSelector-based path values converted from typedValues.
	 *
	 * @return unmodifiable map of TypeSelector paths to values
	 */
	public Map<PathExpression, @Nullable Object> getTypedPathValues() {
		return typedPathValues;
	}

	/**
	 * Returns the order numbers for typedPathValues entries.
	 *
	 * @return unmodifiable map of TypeSelector paths to order numbers
	 */
	public Map<PathExpression, Integer> getTypedPathOrders() {
		return typedPathOrders;
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
	 * Returns the order of values by path.
	 * <p>
	 * Used to determine priority when wildcard and specific paths conflict.
	 * Higher order values take precedence over lower order values.
	 *
	 * @return unmodifiable map of value orders by path
	 */
	public Map<PathExpression, Integer> getValueOrderByPath() {
		return valueOrderByPath;
	}

	/**
	 * Returns the customizers by path for customizeProperty operations.
	 *
	 * @return unmodifiable map of customizers by path
	 */
	public Map<PathExpression, List<AnalysisResult.PropertyCustomizer>> getCustomizersByPath() {
		return customizersByPath;
	}

	/**
	 * Returns the type-specific introspectors from instantiate() calls.
	 *
	 * @return unmodifiable map of introspectors by type
	 */
	public Map<Class<?>, ArbitraryIntrospector> getIntrospectorsByType() {
		return introspectorsByType;
	}

	/**
	 * Returns the NodeTreeAdapter for building concrete type trees.
	 *
	 * @return the node tree adapter, or null if not set
	 */
	public @Nullable NodeTreeAdapter getNodeTreeAdapter() {
		return nodeTreeAdapter;
	}

	/**
	 * Returns the set of paths where the user has explicitly set container sizes.
	 *
	 * @return the user container size paths
	 */
	public Set<PathExpression> getUserContainerSizePaths() {
		return userContainerSizePaths;
	}

	/**
	 * Creates a new builder for AssembleContext.
	 *
	 * @param monkeyContext the MonkeyContext to use
	 * @return a new Builder instance
	 */
	public static Builder builder(MonkeyContext monkeyContext) {
		return new Builder(monkeyContext);
	}

	/**
	 * Builder for creating AssembleContext instances.
	 */
	public static final class Builder {

		private final MonkeyContext monkeyContext;
		private TreeRootProperty rootProperty;
		private Set<PathExpression> justPaths = Collections.emptySet();
		private Set<PathExpression> notNullPaths = Collections.emptySet();
		private Map<PathExpression, List<AnalysisResult.PostConditionFilter>> filtersByPath = Collections.emptyMap();
		private Map<PathExpression, Integer> limitsByPath = Collections.emptyMap();
		private InterfaceSelectionStrategy interfaceSelectionStrategy = InterfaceSelectionStrategy.RANDOM;
		private TraceContext traceContext;
		private Map<PathExpression, Integer> valueOrderByPath = Collections.emptyMap();
		private Map<PathExpression, List<AnalysisResult.PropertyCustomizer>> customizersByPath =
			Collections.emptyMap();
		private Map<Class<?>, ArbitraryIntrospector> introspectorsByType = Collections.emptyMap();
		private @Nullable NodeTreeAdapter nodeTreeAdapter;
		private Set<PathExpression> userContainerSizePaths = Collections.emptySet();
		private Map<PathExpression, @Nullable Object> typedPathValues = Collections.emptyMap();
		private Map<PathExpression, Integer> typedPathOrders = Collections.emptyMap();

		private Builder(MonkeyContext monkeyContext) {
			this.monkeyContext = monkeyContext;
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
		 * Sets the just paths (paths set via Values.just() which are truly immutable).
		 * <p>
		 * These are the ONLY paths where child path values are ignored.
		 * All other path resolution follows "more specific path wins" rule.
		 *
		 * @param justPaths the just paths
		 * @return this builder
		 */
		public Builder justPaths(Set<PathExpression> justPaths) {
			this.justPaths = justPaths;
			return this;
		}

		/**
		 * Sets the not-null paths (paths set via setNotNull() which require null injection = 0).
		 *
		 * @param notNullPaths the not-null paths
		 * @return this builder
		 */
		public Builder notNullPaths(Set<PathExpression> notNullPaths) {
			this.notNullPaths = notNullPaths;
			return this;
		}

		/**
		 * Sets the filters by path.
		 *
		 * @param filtersByPath the filters by path
		 * @return this builder
		 */
		public Builder filtersByPath(Map<PathExpression, List<AnalysisResult.PostConditionFilter>> filtersByPath) {
			this.filtersByPath = filtersByPath;
			return this;
		}

		/**
		 * Sets the limits by path.
		 *
		 * @param limitsByPath the limits by path
		 * @return this builder
		 */
		public Builder limitsByPath(Map<PathExpression, Integer> limitsByPath) {
			this.limitsByPath = limitsByPath;
			return this;
		}

		/**
		 * Sets the interface selection strategy for selecting implementations
		 * of interface or abstract types.
		 *
		 * @param strategy the interface selection strategy
		 * @return this builder
		 */
		public Builder interfaceSelectionStrategy(InterfaceSelectionStrategy strategy) {
			this.interfaceSelectionStrategy = strategy;
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
		 * Sets the value order by path.
		 * <p>
		 * Used to determine priority when wildcard and specific paths conflict.
		 * Higher order values take precedence over lower order values.
		 *
		 * @param valueOrderByPath the value order map
		 * @return this builder
		 */
		public Builder valueOrderByPath(Map<PathExpression, Integer> valueOrderByPath) {
			this.valueOrderByPath = valueOrderByPath;
			return this;
		}

		/**
		 * Sets the customizers by path for customizeProperty operations.
		 *
		 * @param customizersByPath the customizers map
		 * @return this builder
		 */
		public Builder customizersByPath(
			Map<PathExpression, List<AnalysisResult.PropertyCustomizer>> customizersByPath
		) {
			this.customizersByPath = customizersByPath;
			return this;
		}

		/**
		 * Sets the type-specific introspectors from instantiate() calls.
		 *
		 * @param introspectorsByType the introspectors map
		 * @return this builder
		 */
		public Builder introspectorsByType(Map<Class<?>, ArbitraryIntrospector> introspectorsByType) {
			this.introspectorsByType = introspectorsByType;
			return this;
		}

		/**
		 * Sets the NodeTreeAdapter for building concrete type trees.
		 *
		 * @param nodeTreeAdapter the node tree adapter
		 * @return this builder
		 */
		public Builder nodeTreeAdapter(@Nullable NodeTreeAdapter nodeTreeAdapter) {
			this.nodeTreeAdapter = nodeTreeAdapter;
			return this;
		}

		/**
		 * Sets the user container size paths.
		 *
		 * @param userContainerSizePaths paths where user called size()
		 * @return this builder
		 */
		public Builder userContainerSizePaths(Set<PathExpression> userContainerSizePaths) {
			this.userContainerSizePaths = userContainerSizePaths;
			return this;
		}

		/**
		 * Sets the TypeSelector-based path values converted from typedValues.
		 *
		 * @param typedPathValues the converted path values
		 * @return this builder
		 */
		public Builder typedPathValues(Map<PathExpression, @Nullable Object> typedPathValues) {
			this.typedPathValues = typedPathValues;
			return this;
		}

		/**
		 * Sets the order numbers for typedPathValues entries.
		 *
		 * @param typedPathOrders the order numbers
		 * @return this builder
		 */
		public Builder typedPathOrders(Map<PathExpression, Integer> typedPathOrders) {
			this.typedPathOrders = typedPathOrders;
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
