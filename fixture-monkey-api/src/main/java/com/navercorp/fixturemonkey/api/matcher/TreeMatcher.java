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

package com.navercorp.fixturemonkey.api.matcher;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It is mainly used to customize the ObjectTree that meets the specific condition with {@link TreeMatcherOperator}.
 * <p>
 * The main difference to the {@link Matcher} is the scope.
 * {@link Matcher} is intended to reference the nodes of the ObjectTree,
 * but {@link TreeMatcher} is intended to reference the ObjectTree itself.
 *
 * @see Matcher
 */
@API(since = "1.0.4", status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface TreeMatcher {
	/**
	 * Determines if the ObjectTree meets the condition.
	 *
	 * @param metadata the metadata of the ObjectTree
	 * @return {@code true} if the condition matches, {@code false} if not matches
	 */
	boolean match(TreeMatcherMetadata metadata);
}
