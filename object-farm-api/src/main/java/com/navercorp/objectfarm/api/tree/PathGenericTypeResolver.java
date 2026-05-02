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
import com.navercorp.objectfarm.api.node.GenericTypeResolver;

/**
 * A {@link PathResolver} implementation for {@link GenericTypeResolver}.
 * <p>
 * This resolver provides a GenericTypeResolver for paths matching a specific pattern,
 * allowing fine-grained control over generic type resolution at different locations in the tree.
 * <p>
 * Example usage:
 * <pre>
 * // Resolve generic types at "$.data" to specific concrete types
 * PathGenericTypeResolver resolver = new PathGenericTypeResolver(
 *     PathExpression.of("$.data"),
 *     genericType -&gt; {
 *         if (genericType.getRawType() == Map.class) {
 *             return JvmTypes.of(Map.class,
 *                 JvmTypes.of(String.class),
 *                 JvmTypes.of(Integer.class));
 *         }
 *         return null;
 *     }
 * );
 * </pre>
 */
public final class PathGenericTypeResolver implements PathResolver<GenericTypeResolver> {
	private final PathExpression pattern;
	private final GenericTypeResolver customizer;

	/**
	 * Creates a new PathGenericTypeResolver.
	 *
	 * @param pattern    the pattern to match
	 * @param customizer the generic type resolver to use for matching paths
	 */
	public PathGenericTypeResolver(PathExpression pattern, GenericTypeResolver customizer) {
		this.pattern = Objects.requireNonNull(pattern, "pattern must not be null");
		this.customizer = Objects.requireNonNull(customizer, "customizer must not be null");
	}

	@Override
	public boolean matches(PathExpression path) {
		return pattern.matches(path);
	}

	@Override
	public GenericTypeResolver getCustomizer() {
		return customizer;
	}
}
