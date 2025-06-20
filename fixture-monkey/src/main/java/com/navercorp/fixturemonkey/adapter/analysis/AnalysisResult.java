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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;

/**
 * Result of analyzing a list of ArbitraryManipulators.
 * <p>
 * Path resolution follows the "more specific path wins" rule:
 * - If both "$.object" and "$.object.str" have values, "$.object.str" takes precedence for that field.
 * - This applies to all manipulations including setNull(), set(), etc.
 * <p>
 * The only exception is Values.just() (tracked in justPaths):
 * - Values.just() creates truly immutable values where child paths are ignored.
 * - This is intentional: when a user sets Values.just(object), they want that exact object.
 */
public final class AnalysisResult {
	private final List<PathResolver<InterfaceResolver>> interfaceResolvers;
	private final List<PathResolver<GenericTypeResolver>> genericTypeResolvers;
	private final List<PathResolver<ContainerSizeResolver>> containerSizeResolvers;
	private final Map<PathExpression, Integer> containerSizeSequenceByPath;
	private final List<PathExpression> justPaths;
	private final Set<PathExpression> notNullPaths;
	private final Map<PathExpression, @Nullable Object> valuesByPath;
	private final List<LazyManipulatorDescriptor> lazyManipulators;
	private final Map<PathExpression, List<PostConditionFilter>> filtersByPath;
	private final Map<PathExpression, Integer> limitsByPath;
	private final Map<PathExpression, Integer> valueOrderByPath;
	private final Map<PathExpression, List<PropertyCustomizer>> customizersByPath;
	private final List<ResolutionTrace.NodeCollision> nodeCollisions;
	private final boolean strictMode;

	AnalysisResult(
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		Map<PathExpression, Integer> containerSizeSequenceByPath,
		List<PathExpression> justPaths,
		Set<PathExpression> notNullPaths,
		Map<PathExpression, @Nullable Object> valuesByPath,
		List<LazyManipulatorDescriptor> lazyManipulators,
		Map<PathExpression, List<PostConditionFilter>> filtersByPath,
		Map<PathExpression, Integer> limitsByPath,
		Map<PathExpression, Integer> valueOrderByPath,
		Map<PathExpression, List<PropertyCustomizer>> customizersByPath,
		List<ResolutionTrace.NodeCollision> nodeCollisions,
		boolean strictMode
	) {
		this.interfaceResolvers = Collections.unmodifiableList(interfaceResolvers);
		this.genericTypeResolvers = Collections.unmodifiableList(genericTypeResolvers);
		this.containerSizeResolvers = Collections.unmodifiableList(containerSizeResolvers);
		this.containerSizeSequenceByPath = Collections.unmodifiableMap(containerSizeSequenceByPath);
		this.justPaths = Collections.unmodifiableList(justPaths);
		this.notNullPaths = Collections.unmodifiableSet(notNullPaths);
		this.valuesByPath = Collections.unmodifiableMap(valuesByPath);
		this.lazyManipulators = Collections.unmodifiableList(lazyManipulators);
		this.filtersByPath = Collections.unmodifiableMap(filtersByPath);
		this.limitsByPath = Collections.unmodifiableMap(limitsByPath);
		this.valueOrderByPath = Collections.unmodifiableMap(valueOrderByPath);
		this.customizersByPath = Collections.unmodifiableMap(customizersByPath);
		this.nodeCollisions = Collections.unmodifiableList(nodeCollisions);
		this.strictMode = strictMode;
	}

	public List<PathResolver<InterfaceResolver>> getInterfaceResolvers() {
		return interfaceResolvers;
	}

	public List<PathResolver<GenericTypeResolver>> getGenericTypeResolvers() {
		return genericTypeResolvers;
	}

	public List<PathResolver<ContainerSizeResolver>> getContainerSizeResolvers() {
		return containerSizeResolvers;
	}

	/**
	 * Returns the sequence (order) of container size resolvers extracted from lazy/decomposed values.
	 * <p>
	 * This is used to determine priority when both explicit size() and lazy-derived sizes exist.
	 * A size with higher sequence should take precedence over one with lower sequence.
	 */
	public Map<PathExpression, Integer> getContainerSizeSequenceByPath() {
		return containerSizeSequenceByPath;
	}

	/**
	 * Returns paths set via Values.just() which are truly immutable.
	 * <p>
	 * Child values under these paths are intentionally ignored.
	 * This is the ONLY exception to the "more specific path wins" rule.
	 * <p>
	 * Rationale: Values.just() means "use this exact object as-is".
	 * If a user wanted to modify child properties, they should use regular set() instead.
	 */
	public List<PathExpression> getJustPaths() {
		return justPaths;
	}

	/**
	 * Returns paths set via setNotNull() which require null injection to be 0.
	 * <p>
	 * When a path is in this set, the assembly process will override the null injection
	 * probability to 0 for that path, ensuring a non-null value is generated.
	 */
	public Set<PathExpression> getNotNullPaths() {
		return notNullPaths;
	}

	public Map<PathExpression, @Nullable Object> getValuesByPath() {
		return valuesByPath;
	}

	public List<LazyManipulatorDescriptor> getLazyManipulators() {
		return lazyManipulators;
	}

	public Map<PathExpression, List<PostConditionFilter>> getFiltersByPath() {
		return filtersByPath;
	}

	public Map<PathExpression, Integer> getLimitsByPath() {
		return limitsByPath;
	}

	/**
	 * Returns the order index of each value in valuesByPath.
	 * Used to determine which values came before/after lazy manipulators.
	 * A value with order > lazyManipulator.getOrder() came AFTER the lazy manipulator
	 * and should override the lazy result.
	 */
	public Map<PathExpression, Integer> getValueOrderByPath() {
		return valueOrderByPath;
	}

	/**
	 * Returns customizers by path for customizeProperty operations.
	 * These are applied during assembly to transform the generated CombinableArbitrary.
	 */
	public Map<PathExpression, List<PropertyCustomizer>> getCustomizersByPath() {
		return customizersByPath;
	}

	/**
	 * Returns value collisions detected during analysis.
	 * A collision occurs when the same path gets overwritten by a later set() call.
	 */
	public List<ResolutionTrace.NodeCollision> getNodeCollisions() {
		return nodeCollisions;
	}

	/**
	 * Returns whether strict mode is enabled.
	 * <p>
	 * When strict mode is enabled, all path expressions must match valid paths in the type structure.
	 * If a path doesn't exist, an IllegalArgumentException will be thrown.
	 */
	public boolean isStrictMode() {
		return strictMode;
	}

	/**
	 * Information about a lazy manipulator that needs to be executed at runtime.
	 */
	public static final class LazyManipulatorDescriptor {

		private final PathExpression pathExpression;
		private final LazyArbitrary<?> lazyArbitrary;
		private final int order;

		public LazyManipulatorDescriptor(PathExpression pathExpression, LazyArbitrary<?> lazyArbitrary, int order) {
			this.pathExpression = pathExpression;
			this.lazyArbitrary = lazyArbitrary;
			this.order = order;
		}

		public PathExpression getPathExpression() {
			return pathExpression;
		}

		public LazyArbitrary<?> getLazyArbitrary() {
			return lazyArbitrary;
		}

		/**
		 * Returns the order index of this lazy manipulator in the manipulator list.
		 * Used to determine which explicit values should override the lazy result.
		 */
		public int getOrder() {
			return order;
		}
	}

	/**
	 * Information about a filter (post-condition) to apply to a path.
	 */
	public static final class PostConditionFilter {

		private final Class<?> type;
		private final Predicate<?> filter;

		public PostConditionFilter(Class<?> type, Predicate<?> filter) {
			this.type = type;
			this.filter = filter;
		}

		public Class<?> getType() {
			return type;
		}

		public Predicate<?> getFilter() {
			return filter;
		}
	}

	/**
	 * Information about a customizer (from customizeProperty) to apply to a path.
	 */
	public static final class PropertyCustomizer {

		private final Function<CombinableArbitrary<?>, CombinableArbitrary<?>> customizer;
		private final int sequence;
		private final boolean afterSet;

		public PropertyCustomizer(
			Function<CombinableArbitrary<?>, CombinableArbitrary<?>> customizer,
			int sequence,
			boolean afterSet
		) {
			this.customizer = customizer;
			this.sequence = sequence;
			this.afterSet = afterSet;
		}

		public Function<CombinableArbitrary<?>, CombinableArbitrary<?>> getCustomizer() {
			return customizer;
		}

		public int getSequence() {
			return sequence;
		}

		/**
		 * Returns true if this customizer was registered after a set() call for the same path.
		 * In this case, the customizer should always be applied (matching non-adapter path behavior
		 * where NodeCustomizerManipulator applies directly when getArbitrary() != null).
		 */
		public boolean isAfterSet() {
			return afterSet;
		}
	}
}
