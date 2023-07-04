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

package com.navercorp.fixturemonkey.api.context;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache;
import com.navercorp.fixturemonkey.api.generator.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.RootProperty;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class MonkeyContextBuilder {
	private ConcurrentLruCache<Property, CombinableArbitrary> arbitrariesByProperty;
	private ConcurrentLruCache<RootProperty, MonkeyGeneratorContext> generatorContextByRootProperty;
	private int cacheSize = 2048;
	private int generatorContextSize = 1000;

	public MonkeyContextBuilder arbitrariesByProperty(
		ConcurrentLruCache<Property, CombinableArbitrary> arbitrariesByProperty
	) {
		this.arbitrariesByProperty = arbitrariesByProperty;
		return this;
	}

	public MonkeyContextBuilder generatorContextByRootProperty(
		ConcurrentLruCache<RootProperty, MonkeyGeneratorContext> generatorContextByRootProperty
	) {
		this.generatorContextByRootProperty = generatorContextByRootProperty;
		return this;
	}

	public MonkeyContextBuilder cacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
		return this;
	}

	public MonkeyContextBuilder generatorContextSize(int generatorContextSize) {
		this.generatorContextSize = generatorContextSize;
		return this;
	}

	public MonkeyContext build() {
		if (arbitrariesByProperty == null) {
			arbitrariesByProperty = new ConcurrentLruCache<>(cacheSize);
		}

		if (generatorContextByRootProperty == null) {
			generatorContextByRootProperty = new ConcurrentLruCache<>(generatorContextSize);
		}

		return new MonkeyContext(arbitrariesByProperty, generatorContextByRootProperty);
	}
}
