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

package com.navercorp.fixturemonkey.expression;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Default implementation of {@link DeclarativeExpression}. Each builder call returns a new
 * instance carrying the accumulated {@link PathExpression}.
 */
public final class DefaultDeclarativeExpression implements DeclarativeExpression {
	private final PathExpression path;

	/**
	 * It is for internal use only. May be removed or changed in a future release.
	 */
	@Deprecated
	public DefaultDeclarativeExpression() {
		this(PathExpression.root());
	}

	DefaultDeclarativeExpression(PathExpression path) {
		this.path = path;
	}

	@Override
	public DefaultDeclarativeExpression property(String propertyName) {
		return new DefaultDeclarativeExpression(path.child(propertyName));
	}

	@Override
	public DefaultDeclarativeExpression element(int sequence) {
		return new DefaultDeclarativeExpression(path.index(sequence));
	}

	@Override
	public DefaultDeclarativeExpression allElement() {
		return new DefaultDeclarativeExpression(path.wildcard());
	}

	@Override
	public DefaultDeclarativeExpression key() {
		return new DefaultDeclarativeExpression(path.key());
	}

	@Override
	public DefaultDeclarativeExpression value() {
		return new DefaultDeclarativeExpression(path.value());
	}

	@API(since = "1.1.10", status = Status.INTERNAL)
	public DefaultDeclarativeExpression prepend(DefaultDeclarativeExpression parentDeclarativeExpression) {
		return new DefaultDeclarativeExpression(parentDeclarativeExpression.path.append(this.path));
	}

	@API(since = "1.1.10", status = Status.INTERNAL)
	public PathExpression getPath() {
		return path;
	}
}
