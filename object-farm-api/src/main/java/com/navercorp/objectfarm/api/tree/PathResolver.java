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

package com.navercorp.objectfarm.api.tree;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.NodeCustomizer;

/**
 * A resolver that provides a {@link NodeCustomizer} for paths matching a specific pattern.
 * <p>
 * PathResolver combines a {@link PathExpression} pattern with a customizer,
 * allowing path-based resolution during tree transformation. This enables targeting
 * specific locations in the tree structure, including dynamically generated container elements.
 * <p>
 * Example usage:
 * <pre>
 * // Create a resolver that sets container size to 5 for "$.items[*]"
 * PathResolver&lt;ContainerSizeResolver&gt; resolver =
 *     new PathContainerSizeResolver(
 *         PathExpression.of("$.items[*]"),
 *         containerType -&gt; 5
 *     );
 *
 * // Check if it matches a path
 * PathExpression path = PathExpression.root().child("items").index(0);
 * if (resolver.matches(path)) {
 *     ContainerSizeResolver sizeResolver = resolver.getCustomizer();
 *     int size = sizeResolver.resolveContainerSize(containerType);
 * }
 * </pre>
 *
 * @param <T> the type of customizer this resolver provides
 */
public interface PathResolver<T extends NodeCustomizer> {
	/**
	 * Checks if this resolver matches the given path.
	 *
	 * @param path the path to match
	 * @return true if the path matches this resolver's pattern
	 */
	boolean matches(PathExpression path);

	/**
	 * Returns the customizer for this resolver.
	 *
	 * @return the customizer
	 */
	T getCustomizer();
}
