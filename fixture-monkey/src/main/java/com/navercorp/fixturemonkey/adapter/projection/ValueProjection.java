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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.projection.NodeProjection;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;

/**
 * A projection that maps paths to Object values and provides assembly capability.
 * <p>
 * ValueProjection implements {@link NodeProjection} for Object values and provides
 * a structured way to store and retrieve values associated with paths in a JvmNodeTree.
 * It also supports assembling these values into a complete object using the
 * {@link #assemble(AssembleContext)} method.
 * <p>
 * Values are stored internally by PathExpression to avoid JvmNode aliasing issues
 * caused by JvmNodeSubtreeContext sharing nodes across container elements.
 *
 * @see NodeProjection
 * @see JvmNodeTree
 * @see AssembleContext
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class ValueProjection implements NodeProjection<Object> {
	private final JvmNodeTree structure;
	private final Map<PathExpression, @Nullable Object> valuesByPath;

	private ValueProjection(JvmNodeTree structure, Map<PathExpression, @Nullable Object> valuesByPath) {
		this.structure = structure;
		this.valuesByPath = Collections.unmodifiableMap(new HashMap<>(valuesByPath));
	}

	/**
	 * Returns the value associated with the given node.
	 *
	 * @param node the node to look up
	 * @return the value associated with the node, or null if not present
	 */
	@Override
	public @Nullable Object get(JvmNode node) {
		PathExpression path = structure.getPath(node);
		return path != null ? valuesByPath.get(path) : null;
	}

	/**
	 * Returns the value associated with the node at the given path.
	 *
	 * @param path the path expression to resolve
	 * @return the value at the path, or null if no value exists
	 */
	@Override
	public @Nullable Object getByPath(PathExpression path) {
		return valuesByPath.get(path);
	}

	/**
	 * Returns the value associated with the node at the given path string.
	 *
	 * @param pathExpression the path expression string (e.g., "$.name", "$.items[0]")
	 * @return the value at the path, or null if no value exists
	 */
	public @Nullable Object getByPath(String pathExpression) {
		return getByPath(PathExpression.of(pathExpression));
	}

	/**
	 * Performs the given action for each node-value pair in this projection.
	 *
	 * @param consumer the action to perform for each entry
	 */
	@Override
	public void forEach(BiConsumer<JvmNode, @Nullable Object> consumer) {
		for (Map.Entry<PathExpression, @Nullable Object> entry : valuesByPath.entrySet()) {
			JvmNode node = structure.resolve(entry.getKey());
			if (node != null) {
				consumer.accept(node, entry.getValue());
			}
		}
	}

	/**
	 * Returns a list of nodes that match the given predicate.
	 *
	 * @param predicate the predicate to test each node-value pair
	 * @return list of nodes that match the predicate
	 */
	@Override
	public List<JvmNode> filter(BiPredicate<JvmNode, @Nullable Object> predicate) {
		List<JvmNode> result = new ArrayList<>();
		for (Map.Entry<PathExpression, @Nullable Object> entry : valuesByPath.entrySet()) {
			JvmNode node = structure.resolve(entry.getKey());
			if (node != null && predicate.test(node, entry.getValue())) {
				result.add(node);
			}
		}
		return result;
	}

	/**
	 * Returns the number of path-value mappings in this projection.
	 *
	 * @return the number of mappings
	 */
	public int size() {
		return valuesByPath.size();
	}

	/**
	 * Returns whether this projection contains no mappings.
	 *
	 * @return true if this projection is empty
	 */
	public boolean isEmpty() {
		return valuesByPath.isEmpty();
	}

	/**
	 * Returns whether this projection contains a mapping for the given node.
	 *
	 * @param node the node to check
	 * @return true if a mapping exists for the node
	 */
	public boolean containsNode(JvmNode node) {
		PathExpression path = structure.getPath(node);
		return path != null && valuesByPath.containsKey(path);
	}

	private Map<PathExpression, @Nullable Object> toPathExpressionMap() {
		return new HashMap<>(valuesByPath);
	}

	/**
	 * Returns paths that could not be resolved to nodes and are not wildcard patterns.
	 * <p>
	 * These are paths that don't exist in the type structure. In strict mode,
	 * the presence of such paths should cause an exception.
	 *
	 * @return set of invalid path expressions
	 */
	public Set<String> getUnresolvedNonWildcardPaths() {
		Set<String> result = new HashSet<>();
		for (Map.Entry<PathExpression, @Nullable Object> entry : valuesByPath.entrySet()) {
			PathExpression path = entry.getKey();
			String pathStr = path.toExpression();
			if (!isWildcardPath(pathStr) && structure.resolve(path) == null) {
				result.add(pathStr);
			}
		}
		return result;
	}

	/**
	 * Returns unresolved paths with diagnostic information about why they failed.
	 *
	 * @return map of path strings to their diagnostic info
	 */
	public Map<String, UnresolvedPathInfo> getUnresolvedPathsWithDiagnostics() {
		Map<String, UnresolvedPathInfo> result = new HashMap<>();
		for (Map.Entry<PathExpression, @Nullable Object> entry : valuesByPath.entrySet()) {
			PathExpression path = entry.getKey();
			String pathStr = path.toExpression();
			if (!isWildcardPath(pathStr) && structure.resolve(path) == null) {
				JvmNodeTree.ResolutionDiagnostic diagnostic = structure.diagnose(path);
				result.put(pathStr, new UnresolvedPathInfo(entry.getValue(), diagnostic));
			}
		}
		return result;
	}

	/**
	 * Holds an unresolved path's value and optional diagnostic information.
	 */
	public static final class UnresolvedPathInfo {

		private final @Nullable Object value;
		private final JvmNodeTree.@Nullable ResolutionDiagnostic diagnostic;

		public UnresolvedPathInfo(@Nullable Object value, JvmNodeTree.@Nullable ResolutionDiagnostic diagnostic) {
			this.value = value;
			this.diagnostic = diagnostic;
		}

		public @Nullable Object getValue() {
			return value;
		}

		public JvmNodeTree.@Nullable ResolutionDiagnostic getDiagnostic() {
			return diagnostic;
		}
	}

	private static boolean isWildcardPath(String path) {
		return path.contains("*");
	}

	/**
	 * Assembles the values in this projection into a CombinableArbitrary.
	 * <p>
	 * This method traverses the node tree and generates objects based on the stored values.
	 * Values that are explicitly set in the projection are used directly; missing values
	 * are generated using fixture-monkey's arbitrary generation infrastructure.
	 *
	 * @param context the assembly context containing options and configuration
	 * @return a CombinableArbitrary that produces the assembled object
	 */
	public CombinableArbitrary<?> assemble(AssembleContext context) {
		return new ValueProjectionAssembler(structure, toPathExpressionMap(), context).assemble();
	}


	/**
	 * Creates a new builder for building a ValueProjection.
	 *
	 * @param structure the JvmNodeTree that provides the structure
	 * @return a new Builder instance
	 */
	public static Builder builder(JvmNodeTree structure) {
		return new Builder(structure);
	}

	/**
	 * Creates a ValueProjection from a map of path strings to values.
	 *
	 * @param tree the JvmNodeTree to use for path resolution
	 * @param valuesByPath the map of path expression strings to values
	 * @return a new ValueProjection containing the resolved mappings
	 */
	public static ValueProjection of(JvmNodeTree tree, Map<String, @Nullable Object> valuesByPath) {
		Builder builder = builder(tree);
		valuesByPath.forEach(builder::putByPath);
		return builder.build();
	}

	/**
	 * Creates a ValueProjection from a map of PathExpression to values.
	 *
	 * @param tree the JvmNodeTree to use for path resolution
	 * @param valuesByPath the map of PathExpression to values
	 * @return a new ValueProjection containing the resolved mappings
	 */
	public static ValueProjection fromPathExpressionMap(
		JvmNodeTree tree,
		Map<PathExpression, @Nullable Object> valuesByPath
	) {
		Builder builder = builder(tree);
		valuesByPath.forEach(builder::putByPath);
		return builder.build();
	}

	/**
	 * Builder for creating ValueProjection instances.
	 */
	public static final class Builder {

		private final JvmNodeTree structure;
		private final Map<PathExpression, @Nullable Object> valuesByPath;

		private Builder(JvmNodeTree structure) {
			this.structure = structure;
			this.valuesByPath = new HashMap<>();
		}

		/**
		 * Associates a value with a node.
		 * The node is resolved to its path in the tree structure.
		 *
		 * @param node the node to associate the value with
		 * @param value the value to associate
		 * @return this builder for method chaining
		 */
		public Builder put(JvmNode node, @Nullable Object value) {
			PathExpression path = structure.getPath(node);
			if (path != null) {
				valuesByPath.put(path, value);
			}
			return this;
		}

		/**
		 * Associates a value with the given path.
		 *
		 * @param path the path expression
		 * @param value the value to associate
		 * @return this builder for method chaining
		 */
		public Builder putByPath(PathExpression path, @Nullable Object value) {
			valuesByPath.put(path, value);
			return this;
		}

		/**
		 * Associates a value with the given path string.
		 *
		 * @param pathExpression the path expression string
		 * @param value the value to associate
		 * @return this builder for method chaining
		 */
		public Builder putByPath(String pathExpression, @Nullable Object value) {
			return putByPath(PathExpression.of(pathExpression), value);
		}

		/**
		 * Builds the ValueProjection.
		 *
		 * @return a new ValueProjection instance
		 */
		public ValueProjection build() {
			return new ValueProjection(structure, valuesByPath);
		}
	}
}
