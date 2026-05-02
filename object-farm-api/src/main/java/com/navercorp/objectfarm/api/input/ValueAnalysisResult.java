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

package com.navercorp.objectfarm.api.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.tree.PathResolverContext;

/**
 * Result of analyzing a value with {@link ValueAnalyzer}.
 * <p>
 * This immutable class contains:
 * <ul>
 *   <li>Interface resolvers extracted from concrete types</li>
 *   <li>Generic type resolvers for parameterized types</li>
 *   <li>Container size resolvers for collections, maps, and arrays</li>
 *   <li>Values by path for decomposed object fields</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * ValueAnalyzer analyzer = new ValueAnalyzer();
 * ValueAnalysisResult result = analyzer.analyze(myObject, "$");
 *
 * // Convert to PathResolverContext for tree transformation
 * PathResolverContext resolverContext = result.toResolverContext();
 *
 * // Or access individual components
 * Map&lt;String, Object&gt; values = result.getValuesByPath();
 * </pre>
 */
public final class ValueAnalysisResult {
	private static final ValueAnalysisResult EMPTY = new ValueAnalysisResult(
		Collections.emptyList(),
		Collections.emptyList(),
		Collections.emptyList(),
		Collections.emptyMap()
	);

	private final List<PathResolver<InterfaceResolver>> interfaceResolvers;
	private final List<PathResolver<GenericTypeResolver>> genericTypeResolvers;
	private final List<PathResolver<ContainerSizeResolver>> containerSizeResolvers;
	private final Map<String, @Nullable Object> valuesByPath;

	private ValueAnalysisResult(
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		Map<String, @Nullable Object> valuesByPath
	) {
		this.interfaceResolvers = interfaceResolvers;
		this.genericTypeResolvers = genericTypeResolvers;
		this.containerSizeResolvers = containerSizeResolvers;
		this.valuesByPath = valuesByPath;
	}

	/**
	 * Returns an empty result with no resolvers or values.
	 *
	 * @return an empty ValueAnalysisResult
	 */
	public static ValueAnalysisResult empty() {
		return EMPTY;
	}

	/**
	 * Creates a new builder for constructing a ValueAnalysisResult.
	 *
	 * @return a new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns the interface resolvers extracted from the analyzed value.
	 *
	 * @return unmodifiable list of interface resolvers
	 */
	public List<PathResolver<InterfaceResolver>> getInterfaceResolvers() {
		return interfaceResolvers;
	}

	/**
	 * Returns the generic type resolvers extracted from the analyzed value.
	 *
	 * @return unmodifiable list of generic type resolvers
	 */
	public List<PathResolver<GenericTypeResolver>> getGenericTypeResolvers() {
		return genericTypeResolvers;
	}

	/**
	 * Returns the container size resolvers extracted from the analyzed value.
	 *
	 * @return unmodifiable list of container size resolvers
	 */
	public List<PathResolver<ContainerSizeResolver>> getContainerSizeResolvers() {
		return containerSizeResolvers;
	}

	/**
	 * Returns the values by path extracted from the analyzed value.
	 * <p>
	 * The map keys are path expressions (e.g., "$.field", "$.items[0]")
	 * and values are the corresponding field/element values.
	 *
	 * @return unmodifiable map of path to value
	 */
	public Map<String, @Nullable Object> getValuesByPath() {
		return valuesByPath;
	}

	/**
	 * Converts this result to a {@link PathResolverContext} for tree transformation.
	 *
	 * @return a PathResolverContext containing all resolvers from this result
	 */
	public PathResolverContext toResolverContext() {
		if (interfaceResolvers.isEmpty()
			&& genericTypeResolvers.isEmpty()
			&& containerSizeResolvers.isEmpty()) {
			return PathResolverContext.empty();
		}

		PathResolverContext.Builder builder = PathResolverContext.builder();

		for (PathResolver<InterfaceResolver> resolver : interfaceResolvers) {
			builder.addInterfaceResolver(resolver);
		}

		for (PathResolver<GenericTypeResolver> resolver : genericTypeResolvers) {
			builder.addGenericTypeResolver(resolver);
		}

		for (PathResolver<ContainerSizeResolver> resolver : containerSizeResolvers) {
			builder.addContainerSizeResolver(resolver);
		}

		return builder.build();
	}

	/**
	 * Checks if this result is empty (no resolvers and no values).
	 *
	 * @return true if empty
	 */
	public boolean isEmpty() {
		return interfaceResolvers.isEmpty()
			&& genericTypeResolvers.isEmpty()
			&& containerSizeResolvers.isEmpty()
			&& valuesByPath.isEmpty();
	}

	/**
	 * Builder for constructing a ValueAnalysisResult.
	 */
	public static final class Builder {
		private final List<PathResolver<InterfaceResolver>> interfaceResolvers = new ArrayList<>();
		private final List<PathResolver<GenericTypeResolver>> genericTypeResolvers = new ArrayList<>();
		private final List<PathResolver<ContainerSizeResolver>> containerSizeResolvers = new ArrayList<>();
		private final Map<String, @Nullable Object> valuesByPath = new HashMap<>();

		private Builder() {
		}

		/**
		 * Adds an interface resolver.
		 *
		 * @param resolver the resolver to add
		 * @return this builder
		 */
		public Builder addInterfaceResolver(PathResolver<InterfaceResolver> resolver) {
			interfaceResolvers.add(resolver);
			return this;
		}

		/**
		 * Adds all interface resolvers from a list.
		 *
		 * @param resolvers the resolvers to add
		 * @return this builder
		 */
		public Builder addAllInterfaceResolvers(List<PathResolver<InterfaceResolver>> resolvers) {
			interfaceResolvers.addAll(resolvers);
			return this;
		}

		/**
		 * Adds a generic type resolver.
		 *
		 * @param resolver the resolver to add
		 * @return this builder
		 */
		public Builder addGenericTypeResolver(PathResolver<GenericTypeResolver> resolver) {
			genericTypeResolvers.add(resolver);
			return this;
		}

		/**
		 * Adds all generic type resolvers from a list.
		 *
		 * @param resolvers the resolvers to add
		 * @return this builder
		 */
		public Builder addAllGenericTypeResolvers(List<PathResolver<GenericTypeResolver>> resolvers) {
			genericTypeResolvers.addAll(resolvers);
			return this;
		}

		/**
		 * Adds a container size resolver.
		 *
		 * @param resolver the resolver to add
		 * @return this builder
		 */
		public Builder addContainerSizeResolver(PathResolver<ContainerSizeResolver> resolver) {
			containerSizeResolvers.add(resolver);
			return this;
		}

		/**
		 * Adds all container size resolvers from a list.
		 *
		 * @param resolvers the resolvers to add
		 * @return this builder
		 */
		public Builder addAllContainerSizeResolvers(List<PathResolver<ContainerSizeResolver>> resolvers) {
			containerSizeResolvers.addAll(resolvers);
			return this;
		}

		/**
		 * Puts a value at the given path.
		 *
		 * @param path  the path expression
		 * @param value the value (may be null)
		 * @return this builder
		 */
		public Builder putValue(String path, @Nullable Object value) {
			valuesByPath.put(path, value);
			return this;
		}

		/**
		 * Puts all values from a map.
		 *
		 * @param values the values to add
		 * @return this builder
		 */
		public Builder putAllValues(Map<String, @Nullable Object> values) {
			valuesByPath.putAll(values);
			return this;
		}

		/**
		 * Builds the ValueAnalysisResult.
		 *
		 * @return a new ValueAnalysisResult
		 */
		public ValueAnalysisResult build() {
			if (interfaceResolvers.isEmpty()
				&& genericTypeResolvers.isEmpty()
				&& containerSizeResolvers.isEmpty()
				&& valuesByPath.isEmpty()) {
				return EMPTY;
			}

			return new ValueAnalysisResult(
				Collections.unmodifiableList(new ArrayList<>(interfaceResolvers)),
				Collections.unmodifiableList(new ArrayList<>(genericTypeResolvers)),
				Collections.unmodifiableList(new ArrayList<>(containerSizeResolvers)),
				Collections.unmodifiableMap(new HashMap<>(valuesByPath))
			);
		}
	}
}
