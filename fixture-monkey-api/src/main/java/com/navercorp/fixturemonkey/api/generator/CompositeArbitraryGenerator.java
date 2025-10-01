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

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.property.CompositePropertyGenerator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;

/**
 * Generates a {@link CombinableArbitrary} by one or more {@link ArbitraryGenerator}.
 * <p>
 * All {@link ArbitraryGenerator} are used in the declared order.
 * A particular {@link ArbitraryGenerator} will use the result of a previously declared {@link ArbitraryGenerator}.
 */
@API(since = "0.6.2", status = Status.MAINTAINED)
public final class CompositeArbitraryGenerator implements ArbitraryGenerator {
	private final List<ArbitraryGenerator> arbitraryGenerators;

	public CompositeArbitraryGenerator(List<ArbitraryGenerator> arbitraryGenerators) {
		this.arbitraryGenerators = arbitraryGenerators;
	}

	@Override
	public CombinableArbitrary<?> generate(ArbitraryGeneratorContext context) {
		CombinableArbitrary<?> generated = context.getGenerated();
		for (ArbitraryGenerator arbitraryGenerator : arbitraryGenerators) {
			generated = arbitraryGenerator.generate(context);
			context.setGenerated(generated);
		}
		return generated;
	}

	@Nullable
	@Override
	public PropertyGenerator getRequiredPropertyGenerator(Property property) {
		List<PropertyGenerator> propertyGenerators = new ArrayList<>();
		for (ArbitraryGenerator arbitraryGenerator : arbitraryGenerators) {
			PropertyGenerator propertyGenerator = arbitraryGenerator.getRequiredPropertyGenerator(property);
			if (propertyGenerator != null) {
				propertyGenerators.add(propertyGenerator);
			}
		}

		if (propertyGenerators.isEmpty()) {
			return null;
		}

		return new CompositePropertyGenerator(propertyGenerators);
	}
}
