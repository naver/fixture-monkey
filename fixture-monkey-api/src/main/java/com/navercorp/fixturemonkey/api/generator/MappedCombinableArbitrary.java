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

package com.navercorp.fixturemonkey.api.generator;

import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It would transform a generated object into a new object.
 */
@API(since = "0.5.0", status = Status.MAINTAINED)
public final class MappedCombinableArbitrary implements CombinableArbitrary {
	private final CombinableArbitrary combinableArbitrary;
	private final Function<Object, Object> mapper;

	public MappedCombinableArbitrary(CombinableArbitrary combinableArbitrary, Function<Object, Object> mapper) {
		this.combinableArbitrary = combinableArbitrary;
		this.mapper = mapper;
	}

	@Override
	public Object combined() {
		return mapper.apply(combinableArbitrary.combined());
	}

	@Override
	public Object rawValue() {
		return mapper.apply(combinableArbitrary.rawValue());
	}

	@Override
	public void clear() {
		combinableArbitrary.clear();
	}

	@Override
	public boolean fixed() {
		return combinableArbitrary.fixed();
	}
}
