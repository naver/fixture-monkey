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

import java.util.Objects;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;

/**
 * A {@link PathResolver} implementation for {@link ContainerSizeResolver}.
 * <p>
 * This resolver provides a ContainerSizeResolver for paths matching a specific pattern,
 * allowing fine-grained control over container sizes at different locations in the tree.
 * <p>
 * Example usage:
 * <pre>
 * // Set size to 3 for "$.items" container
 * PathContainerSizeResolver resolver = new PathContainerSizeResolver(
 *     PathExpression.of("$.items"),
 *     containerType -&gt; 3
 * );
 *
 * // Set size to 5 for all elements in "$.items[*]" (nested containers)
 * PathContainerSizeResolver nestedResolver = new PathContainerSizeResolver(
 *     PathExpression.of("$.items[*]"),
 *     containerType -&gt; 5
 * );
 * </pre>
 */
public final class PathContainerSizeResolver implements PathResolver<ContainerSizeResolver> {
	private final PathExpression pattern;
	private final ContainerSizeResolver customizer;

	/**
	 * Creates a new PathContainerSizeResolver.
	 *
	 * @param pattern    the pattern to match
	 * @param customizer the container size resolver to use for matching paths
	 */
	public PathContainerSizeResolver(PathExpression pattern, ContainerSizeResolver customizer) {
		this.pattern = Objects.requireNonNull(pattern, "pattern must not be null");
		this.customizer = Objects.requireNonNull(customizer, "customizer must not be null");
	}

	@Override
	public boolean matches(PathExpression path) {
		return pattern.matches(path);
	}

	@Override
	public ContainerSizeResolver getCustomizer() {
		return customizer;
	}

	/**
	 * Returns the path pattern this resolver matches against.
	 *
	 * @return the path pattern
	 */
	public PathExpression getPattern() {
		return pattern;
	}
}
