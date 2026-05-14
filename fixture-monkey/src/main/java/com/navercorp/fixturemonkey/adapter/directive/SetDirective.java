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

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Sets a decomposable value at a path. Container values are decomposed via the supplied
 * {@link DecomposedContainerValueFactory}; objects are decomposed by the analyzer.
 */
@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class SetDirective implements PathDirective {
	private final PathExpression path;
	private final int sequence;
	private final int limit;
	private final boolean strict;
	private final boolean registered;
	private final @Nullable Object value;
	private final DecomposedContainerValueFactory decomposedContainerValueFactory;
	private final List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators;

	public SetDirective(
		PathExpression path,
		int sequence,
		int limit,
		boolean strict,
		boolean registered,
		@Nullable Object value,
		DecomposedContainerValueFactory decomposedContainerValueFactory,
		List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators
	) {
		this.path = path;
		this.sequence = sequence;
		this.limit = limit;
		this.strict = strict;
		this.registered = registered;
		this.value = value;
		this.decomposedContainerValueFactory = decomposedContainerValueFactory;
		this.containerPropertyGenerators = containerPropertyGenerators;
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

	public @Nullable Object value() {
		return value;
	}

	public DecomposedContainerValueFactory decomposedContainerValueFactory() {
		return decomposedContainerValueFactory;
	}

	public List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators() {
		return containerPropertyGenerators;
	}
}
