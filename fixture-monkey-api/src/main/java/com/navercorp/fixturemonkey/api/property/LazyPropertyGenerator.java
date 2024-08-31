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

package com.navercorp.fixturemonkey.api.property;

import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;

@API(since = "1.1.0", status = Status.EXPERIMENTAL)
public final class LazyPropertyGenerator implements PropertyGenerator {
	private final PropertyGenerator delegate;
	private final Map<Property, LazyArbitrary<List<Property>>> childPropertyListsByProperty
		= new ConcurrentLruCache<>(32);

	public LazyPropertyGenerator(PropertyGenerator delegate) {
		this.delegate = delegate;
	}

	/**
	 * The generated properties determined when calling the method, they are cached.
	 *
	 * @param property the property need the child properties
	 * @return the child properties
	 */
	@Override
	public List<Property> generateChildProperties(Property property) {
		return childPropertyListsByProperty.computeIfAbsent(
			property,
			p -> LazyArbitrary.lazy(() -> delegate.generateChildProperties(p))
		).getValue();
	}

	/**
	 * It is used when the delegate is needed.
	 *
	 * @return the delegate
	 */
	public PropertyGenerator getDelegate() {
		return delegate;
	}
}
