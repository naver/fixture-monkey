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

import java.util.Collection;
import java.util.function.Consumer;

import javax.validation.constraints.NotNull;

import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;

final class CallbackList<T extends BuilderManipulator> extends DecoratedList<T> {
	private final Consumer<T> callback;

	public CallbackList(DecoratedList<T> decoratedList, Consumer<T> callback) {
		super(decoratedList);
		this.callback = callback;
	}

	@Override
	public boolean add(T value) {
		boolean added = decoratedList.add(value);
		callback.accept(value);
		return added;
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends T> collection) {
		boolean addAll = decoratedList.addAll(collection);
		collection.forEach(callback);
		return addAll;
	}

	@Override
	public DecoratedList<T> copy() {
		return new CallbackList<>(this.decoratedList.copy(), callback);
	}
}
