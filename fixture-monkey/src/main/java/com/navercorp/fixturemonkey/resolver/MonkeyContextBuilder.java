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

package com.navercorp.fixturemonkey.resolver;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.collection.LruCache;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class MonkeyContextBuilder {
	private LruCache<Property, Arbitrary<?>> arbitrariesByProperty;
	private int cacheSize = 2000;

	public MonkeyContextBuilder arbitrariesByProperty(LruCache<Property, Arbitrary<?>> arbitrariesByProperty) {
		this.arbitrariesByProperty = arbitrariesByProperty;
		return this;
	}

	public MonkeyContextBuilder cacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
		return this;
	}

	public MonkeyContext build() {
		if (arbitrariesByProperty == null) {
			arbitrariesByProperty = new LruCache<>(cacheSize);
		}

		return new MonkeyContext(arbitrariesByProperty);
	}
}
