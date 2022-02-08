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

package com.navercorp.fixturemonkey.api.introspector;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class CompositeArbitraryTypeIntrospector implements ArbitraryTypeIntrospector {
	private final List<ArbitraryTypeIntrospector> introspectors;

	public CompositeArbitraryTypeIntrospector(List<ArbitraryTypeIntrospector> introspectors) {
		this.introspectors = introspectors;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		for (ArbitraryTypeIntrospector introspector : this.introspectors) {
			ArbitraryIntrospectorResult result = introspector.introspect(context);
			if (!ArbitraryIntrospectorResult.EMPTY.equals(result)) {
				return result;
			}
		}

		return ArbitraryIntrospectorResult.EMPTY;
	}
}
