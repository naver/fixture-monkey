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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;

@API(since = "0.4.0", status = Status.MAINTAINED)
public class DefaultArbitraryGenerator implements ArbitraryGenerator {
	private final ArbitraryIntrospector arbitraryIntrospector;

	public DefaultArbitraryGenerator(ArbitraryIntrospector arbitraryIntrospector) {
		this.arbitraryIntrospector = arbitraryIntrospector;
	}

	public static JavaDefaultArbitraryGeneratorBuilder javaBuilder() {
		return new JavaDefaultArbitraryGeneratorBuilder();
	}

	@Override
	public Arbitrary<?> generate(ArbitraryGeneratorContext context) {
		ArbitraryIntrospectorResult result = this.arbitraryIntrospector.introspect(context);
		if (result.getValue() != null) {
			double nullInject = context.getArbitraryProperty().getObjectProperty().getNullInject();
			return result.getValue()
				.injectNull(nullInject);
		}

		return Arbitraries.just(null);
	}
}
