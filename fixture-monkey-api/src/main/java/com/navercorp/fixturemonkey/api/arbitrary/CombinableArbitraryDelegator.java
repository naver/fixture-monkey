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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.6.12", status = Status.EXPERIMENTAL)
public final class CombinableArbitraryDelegator<T> implements CombinableArbitrary<T> {
	private final CombinableArbitrary<T> delegated;

	public CombinableArbitraryDelegator(CombinableArbitrary<T> delegated) {
		this.delegated = delegated;
	}

	@Override
	public T combined() {
		return delegated.combined();
	}

	@Override
	public Object rawValue() {
		return delegated.rawValue();
	}

	@Override
	public void clear() {
		delegated.clear();
	}

	@Override
	public boolean fixed() {
		return delegated.fixed();
	}
}
