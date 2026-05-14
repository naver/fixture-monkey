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

package com.navercorp.fixturemonkey.adapter.directive;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * A self-describing record of a single user-issued instruction at a path.
 * <p>
 * Implemented by typed payload classes ({@link SetDirective}, {@link JustDirective},
 * {@link LazyDirective}, {@link NullityDirective}, {@link FilterDirective},
 * {@link CustomizerDirective}). The fold step in
 * {@link com.navercorp.fixturemonkey.adapter.analysis.ManipulatorAnalyzer} dispatches by subtype.
 *
 * @since 0.4.0
 */
@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public interface PathDirective {
	/**
	 * The fully resolved path this directive targets.
	 */
	PathExpression path();

	/**
	 * Order index used to determine precedence between directives that target the same path.
	 */
	int sequence();

	/**
	 * Optional application count limit; {@code -1} means unlimited.
	 */
	int limit();

	/**
	 * Whether this directive runs in strict mode. When {@code true}, a non-existent path triggers
	 * an exception instead of being silently ignored.
	 */
	boolean strict();

	/**
	 * Whether this directive originates from a registered builder. Registered directives are
	 * skipped by direct path-based analysis and flow through the typed-value path instead.
	 */
	boolean registered();
}
