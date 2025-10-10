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

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

/**
 * It is for internal use only.
 */
@API(since = "1.1.16", status = INTERNAL)
public interface MatcherOperatorRegistry<T> {
	void addFirst(MatcherOperator<T> matcherOperator);

	void addLast(MatcherOperator<T> matcherOperator);
}
