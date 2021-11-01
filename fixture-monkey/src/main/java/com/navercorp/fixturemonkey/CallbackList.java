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

package com.navercorp.fixturemonkey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;

final class CallbackList<T extends BuilderManipulator> extends DecoratedList<T> {
	private final CallbackOperation<T> callbackOperation;

	public CallbackList(DecoratedList<T> decoratedList, CallbackOperation<T> callback) {
		super(decoratedList);
		this.callbackOperation = callback;
	}

	@Override
	public boolean add(T value) {
		List<T> transformed = callbackOperation.apply(value);
		boolean added = decoratedList.addAll(transformed);
		callbackOperation.accept(value);
		return added;
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends T> collection) {
		List<T> transformed = collection.stream()
			.flatMap(it -> callbackOperation.apply(it).stream())
			.collect(Collectors.toList());
		boolean addAll = decoratedList.addAll(transformed);
		collection.forEach(callbackOperation::accept);
		return addAll;
	}

	@Override
	public DecoratedList<T> copy() {
		return new CallbackList<>(this.decoratedList.copy(), callbackOperation);
	}
}
