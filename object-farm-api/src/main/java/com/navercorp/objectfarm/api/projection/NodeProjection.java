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

package com.navercorp.objectfarm.api.projection;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * A projection that maps JvmNodes to values of type T.
 * <p>
 * NodeProjection provides a structured way to associate values with nodes in a JvmNodeTree.
 * It supports both node-based and path-based access to values.
 * <p>
 * Example usage:
 * <pre>
 * NodeProjection&lt;Object&gt; values = ...;
 *
 * // Access by node
 * Object value = values.get(node);
 *
 * // Access by path
 * Object valueByPath = values.getByPath(PathExpression.of("$.name"));
 *
 * // Iterate over all mappings
 * values.forEach((node, value) -&gt; System.out.println(node + " = " + value));
 *
 * // Filter nodes by predicate
 * List&lt;JvmNode&gt; nonNullNodes = values.filter((node, value) -&gt; value != null);
 * </pre>
 *
 * @param <T> the type of values in this projection
 */
public interface NodeProjection<T> {

	/**
	 * Returns the value associated with the given node.
	 *
	 * @param node the node to look up
	 * @return the value associated with the node, or null if not present
	 */
	@Nullable T get(JvmNode node);

	/**
	 * Returns the value associated with the node at the given path.
	 * <p>
	 * This first resolves the path to a node using the structure's resolve method,
	 * then retrieves the value associated with that node.
	 *
	 * @param path the path expression to resolve
	 * @return the value associated with the resolved node, or null if the path
	 *         cannot be resolved or no value is associated
	 */
	@Nullable T getByPath(PathExpression path);

	/**
	 * Performs the given action for each node-value pair in this projection.
	 *
	 * @param consumer the action to perform for each entry
	 */
	void forEach(BiConsumer<JvmNode, @Nullable T> consumer);

	/**
	 * Returns a list of nodes that match the given predicate.
	 *
	 * @param predicate the predicate to test each node-value pair
	 * @return list of nodes that match the predicate
	 */
	List<JvmNode> filter(BiPredicate<JvmNode, @Nullable T> predicate);
}
