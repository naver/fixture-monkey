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
import com.navercorp.objectfarm.api.node.InterfaceResolver;

/**
 * A {@link PathResolver} implementation for {@link InterfaceResolver}.
 * <p>
 * This resolver provides an InterfaceResolver for paths matching a specific pattern,
 * allowing fine-grained control over interface resolution at different locations in the tree.
 * <p>
 * Example usage:
 * <pre>
 * // Use specific implementation for List interface at "$.items"
 * PathInterfaceResolver resolver = new PathInterfaceResolver(
 *     PathExpression.of("$.items"),
 *     interfaceType -&gt; {
 *         if (interfaceType.getRawType() == List.class) {
 *             return JvmTypes.of(LinkedList.class, interfaceType.getTypeVariables());
 *         }
 *         return null;
 *     }
 * );
 * </pre>
 */
public final class PathInterfaceResolver implements PathResolver<InterfaceResolver> {
	private final PathExpression pattern;
	private final InterfaceResolver customizer;

	/**
	 * Creates a new PathInterfaceResolver.
	 *
	 * @param pattern    the pattern to match
	 * @param customizer the interface resolver to use for matching paths
	 */
	public PathInterfaceResolver(PathExpression pattern, InterfaceResolver customizer) {
		this.pattern = Objects.requireNonNull(pattern, "pattern must not be null");
		this.customizer = Objects.requireNonNull(customizer, "customizer must not be null");
	}

	@Override
	public boolean matches(PathExpression path) {
		return pattern.matches(path);
	}

	@Override
	public InterfaceResolver getCustomizer() {
		return customizer;
	}

	/**
	 * Returns the path pattern this resolver matches against.
	 *
	 * @return the path expression pattern
	 */
	public PathExpression getPattern() {
		return pattern;
	}
}
