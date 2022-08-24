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

package com.navercorp.fixturemonkey.arbitrary;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.OldArbitraryBuilderImpl;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class PriorityManipulatorDelegator implements MetadataManipulator {
	private final ArbitraryExpression arbitraryExpression = ArbitraryExpression.from("$");
	private final BuilderManipulator builderManipulator;

	public PriorityManipulatorDelegator(BuilderManipulator builderManipulator) {
		this.builderManipulator = builderManipulator;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void accept(OldArbitraryBuilderImpl arbitraryBuilder) {
		builderManipulator.accept(arbitraryBuilder);
	}

	@Override
	public BuilderManipulator copy() {
		return this;
	}

	@Override
	public Priority getPriority() {
		return Priority.HIGH;
	}

	@Override
	public ArbitraryExpression getArbitraryExpression() {
		return arbitraryExpression;
	}

	@Override
	public void addPrefix(String expression) {
		arbitraryExpression.addFirst(expression);
	}
}
