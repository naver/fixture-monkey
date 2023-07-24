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

package com.navercorp.fixturemonkey.api.arbitrary;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.6.0", status = Status.EXPERIMENTAL)
public final class ContainerCombineArbitraryBuilder {
	/**
	 * Deprecated.
	 * Use {@code FixtureMonkeyOptions.DEFAULT_MAX_UNIQUE_GENERATION_COUNT} instead.
	 */
	@Deprecated
	public static final int DEFAULT_MAX_UNIQUE_GENERATION_COUNT = 1_000;

	private final List<CombinableArbitrary<?>> elementArbitraryList;
	private Runnable postBuild = () -> {
	};

	ContainerCombineArbitraryBuilder() {
		elementArbitraryList = new ArrayList<>();
	}

	public ContainerCombineArbitraryBuilder element(CombinableArbitrary<?> arbitrary) {
		elementArbitraryList.add(arbitrary);
		return this;
	}

	public ContainerCombineArbitraryBuilder elements(List<CombinableArbitrary<?>> arbitraryList) {
		elementArbitraryList.addAll(arbitraryList);
		return this;
	}

	public ContainerCombineArbitraryBuilder postBuild(Runnable postBuilderAction) {
		this.postBuild = postBuilderAction;
		return this;
	}

	public <T> CombinableArbitrary<T> build(Function<List<Object>, T> combinator) {
		return new ContainerCombinableArbitrary<>(
			elementArbitraryList,
			elements -> {
				T container = combinator.apply(elements);
				postBuild.run();
				return container;
			}
		);
	}
}
