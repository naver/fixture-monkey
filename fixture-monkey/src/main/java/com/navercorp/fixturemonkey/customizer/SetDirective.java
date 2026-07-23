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
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Sets a decomposable value at a path. Container values are decomposed via the supplied
 * {@link DecomposedContainerValueFactory}; objects are decomposed by the analyzer.
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
public final class SetDirective implements PathDirective {
	private final PathExpression path;
	private final int sequence;
	private final int limit;
	private final boolean strict;
	private final @Nullable Object value;
	private final DecomposedContainerValueFactory decomposedContainerValueFactory;

	public SetDirective(
		PathExpression path,
		int sequence,
		int limit,
		boolean strict,
		@Nullable Object value,
		DecomposedContainerValueFactory decomposedContainerValueFactory
	) {
		this.path = path;
		this.sequence = sequence;
		this.limit = limit;
		this.strict = strict;
		this.value = value;
		this.decomposedContainerValueFactory = decomposedContainerValueFactory;
	}

	@Override
	public PathExpression path() {
		return path;
	}

	@Override
	public int sequence() {
		return sequence;
	}

	@Override
	public int limit() {
		return limit;
	}

	@Override
	public boolean strict() {
		return strict;
	}

	public @Nullable Object value() {
		return value;
	}

	public DecomposedContainerValueFactory decomposedContainerValueFactory() {
		return decomposedContainerValueFactory;
	}
}
