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
 * Forces a path to be {@code null} ({@code toNull = true}) or non-null ({@code toNull = false}).
 */
@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class NullityDirective implements PathDirective {
	private final PathExpression path;
	private final int sequence;
	private final int limit;
	private final boolean strict;
	private final boolean registered;
	private final boolean toNull;

	public NullityDirective(
		PathExpression path,
		int sequence,
		int limit,
		boolean strict,
		boolean registered,
		boolean toNull
	) {
		this.path = path;
		this.sequence = sequence;
		this.limit = limit;
		this.strict = strict;
		this.registered = registered;
		this.toNull = toNull;
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

	@Override
	public boolean registered() {
		return registered;
	}

	public boolean toNull() {
		return toNull;
	}
}
