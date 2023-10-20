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

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;

/**
 * Introspects by one or more {@link ArbitraryIntrospector}.
 * <p>
 * All {@link ArbitraryIntrospector} are used in the declared order.
 * A particular {@link ArbitraryIntrospector} will use the result of
 * a previously declared {@link ArbitraryIntrospector}.
 */
@API(since = "0.6.12", status = Status.EXPERIMENTAL)
public class CompositeArbitraryIntrospector implements ArbitraryIntrospector {
	private final List<ArbitraryIntrospector> introspectors;

	public CompositeArbitraryIntrospector(List<ArbitraryIntrospector> introspectors) {
		this.introspectors = introspectors;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		for (ArbitraryIntrospector introspector : introspectors) {
			CombinableArbitrary<?> introspected = introspector.introspect(context).getValue();
			context.setGenerated(introspected);
		}
		return new ArbitraryIntrospectorResult(context.getGenerated());
	}
}
