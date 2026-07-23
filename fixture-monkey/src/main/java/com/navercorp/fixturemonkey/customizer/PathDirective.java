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

package com.navercorp.fixturemonkey.customizer;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * A self-describing record of a single user-issued instruction at a path.
 * <p>
 * Implemented by typed payload classes ({@link SetDirective}, {@link JustDirective},
 * {@link LazyDirective}, {@link NullityDirective}, {@link FilterDirective},
 * {@link CustomizerDirective}). The fold step in
 * {@link com.navercorp.fixturemonkey.planner.ManipulatorAnalyzer} dispatches by subtype.
 *
 * @since 0.4.0
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
public interface PathDirective {
	/**
	 * Sentinel {@link #limit()} value meaning the directive is applied an unlimited number of times.
	 */
	int UNLIMITED = -1;

	/**
	 * The fully resolved path this directive targets.
	 */
	PathExpression path();

	/**
	 * Order index used to determine precedence between directives that target the same path.
	 */
	int sequence();

	/**
	 * Optional application count limit; {@link #UNLIMITED} means unlimited.
	 */
	int limit();

	/**
	 * Whether this directive runs in strict mode. When {@code true}, a non-existent path triggers
	 * an exception instead of being silently ignored.
	 */
	boolean strict();
}
