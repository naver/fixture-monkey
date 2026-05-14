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

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Sets a container's element count range at a path. {@code min}/{@code max} bound the
 * randomly chosen size; once {@link #fix()} is called the directive locks to a single value.
 * <p>
 * Replaces the legacy {@code SizeDirective}.
 */
@API(since = "1.1.18", status = Status.EXPERIMENTAL)
public final class SizeDirective implements PathDirective {
	private final PathExpression path;
	private final int sequence;
	private final ArbitraryContainerInfo containerInfo;

	public SizeDirective(PathExpression path, int sequence, ArbitraryContainerInfo containerInfo) {
		this.path = path;
		this.sequence = sequence;
		this.containerInfo = containerInfo;
	}

	public SizeDirective(PathExpression path, int sequence, int min, int max) {
		this(path, sequence, new ArbitraryContainerInfo(min, max));
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
		return -1;
	}

	@Override
	public boolean strict() {
		return false;
	}

	@Override
	public boolean registered() {
		return false;
	}

	public ArbitraryContainerInfo containerInfo() {
		return containerInfo;
	}

	public int min() {
		return containerInfo.getElementMinSize();
	}

	public int max() {
		return containerInfo.getElementMaxSize();
	}

	/**
	 * Returns a copy locked to a single random size drawn from this directive's current range.
	 * Used by {@code ArbitraryBuilder.fixed()} to make the size deterministic on subsequent samples.
	 */
	public SizeDirective fix() {
		int fixedSize = containerInfo.getRandomSize();
		return new SizeDirective(path, sequence, new ArbitraryContainerInfo(fixedSize, fixedSize));
	}
}
