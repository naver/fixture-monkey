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

import static com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary.NOT_GENERATED;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.arbitrary.TraceableCombinableArbitrary;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;

/**
 * Generates a {@link CombinableArbitrary} by {@link ArbitraryIntrospector}.
 */
@API(since = "0.6.2", status = Status.MAINTAINED)
public final class IntrospectedArbitraryGenerator implements ArbitraryGenerator {
	private final ArbitraryIntrospector arbitraryIntrospector;

	public IntrospectedArbitraryGenerator(ArbitraryIntrospector arbitraryIntrospector) {
		this.arbitraryIntrospector = arbitraryIntrospector;
	}

	public static JavaDefaultArbitraryGeneratorBuilder javaBuilder() {
		return new JavaDefaultArbitraryGeneratorBuilder();
	}

	/**
	 * Generates a {@link CombinableArbitrary} by given {@link ArbitraryIntrospector}.
	 *
	 * @param context generator context
	 * @return generated {@link CombinableArbitrary}.
	 * Returns {@code DefaultArbitraryGenerator.NOT_GENERATED}
	 * if given {@link ArbitraryGenerator} could not generate a {@link CombinableArbitrary}
	 */
	@Override
	public CombinableArbitrary<?> generate(ArbitraryGeneratorContext context) {
		if (context.getGenerated() != NOT_GENERATED) {
			return context.getGenerated();
		}

		ArbitraryIntrospectorResult result = this.arbitraryIntrospector.introspect(context);
		if (result != ArbitraryIntrospectorResult.NOT_INTROSPECTED && result.getValue() != null) {
			double nullInject = context.getNullInject();
			return new TraceableCombinableArbitrary<>(
				result.getValue()
					.injectNull(nullInject),
				context.getPropertyPath()
			);
		}

		return NOT_GENERATED;
	}

	@Nullable
	@Override
	public PropertyGenerator getRequiredPropertyGenerator(Property property) {
		return arbitraryIntrospector.getRequiredPropertyGenerator(property);
	}
}
