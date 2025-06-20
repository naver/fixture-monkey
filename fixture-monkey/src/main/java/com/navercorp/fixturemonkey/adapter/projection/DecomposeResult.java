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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Immutable result of value decomposition.
 * <p>
 * Describes what mutations should be applied to the assembly state's maps
 * without performing them directly. The caller is responsible for applying
 * the result via explicit merge.
 */
final class DecomposeResult {
	private static final DecomposeResult NONE = new DecomposeResult(
		null,
		Collections.emptyMap(),
		Collections.emptySet(),
		null,
		-1
	);

	private final @Nullable Object earlyReturnValue;
	private final Map<PathExpression, ValueCandidate> valuesToPut;
	private final Set<PathExpression> subtreesToRemove;
	private final @Nullable PathExpression limitPath;
	private final int limitValue;

	private DecomposeResult(
		@Nullable Object earlyReturnValue,
		Map<PathExpression, ValueCandidate> valuesToPut,
		Set<PathExpression> subtreesToRemove,
		@Nullable PathExpression limitPath,
		int limitValue
	) {
		this.earlyReturnValue = earlyReturnValue;
		this.valuesToPut = valuesToPut;
		this.subtreesToRemove = subtreesToRemove;
		this.limitPath = limitPath;
		this.limitValue = limitValue;
	}

	static DecomposeResult none() {
		return NONE;
	}

	static DecomposeResult earlyReturn(Object value) {
		return new DecomposeResult(value, Collections.emptyMap(), Collections.emptySet(), null, -1);
	}

	static DecomposeResult of(
		Map<PathExpression, ValueCandidate> valuesToPut,
		Set<PathExpression> subtreesToRemove,
		@Nullable PathExpression limitPath,
		int limitValue
	) {
		return new DecomposeResult(null, valuesToPut, subtreesToRemove, limitPath, limitValue);
	}

	boolean hasEarlyReturn() {
		return earlyReturnValue != null;
	}

	@Nullable
	Object getEarlyReturnValue() {
		return earlyReturnValue;
	}

	Map<PathExpression, ValueCandidate> getValuesToPut() {
		return valuesToPut;
	}

	Set<PathExpression> getSubtreesToRemove() {
		return subtreesToRemove;
	}

	@Nullable
	PathExpression getLimitPath() {
		return limitPath;
	}

	int getLimitValue() {
		return limitValue;
	}
}
