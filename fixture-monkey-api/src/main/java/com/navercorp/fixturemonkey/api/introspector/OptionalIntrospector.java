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
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class OptionalIntrospector implements ArbitraryIntrospector, Matcher {
	@Override
	public boolean match(Property property) {
		Class<?> type = Types.getActualType(property.getType());
		return type == Optional.class
			|| type == OptionalInt.class
			|| type == OptionalLong.class
			|| type == OptionalDouble.class;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		if (!property.isContainer()) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}
		List<ArbitraryProperty> children = context.getChildren();

		if (children.isEmpty()) {
			return new ArbitraryIntrospectorResult(
				CombinableArbitrary.from(Optional.empty())
			);
		}

		ArbitraryProperty valueProperty = children.get(0);
		double presenceProbability = 1 - valueProperty.getObjectProperty().getNullInject();
		if (children.size() == 1) {
			presenceProbability = 1.0d;
		}

		Class<?> type = Types.getActualType(property.getObjectProperty().getProperty().getType());
		double optionalProbability = presenceProbability;

		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.containerBuilder()
				.elements(context.getElementCombinableArbitraryList())
				.build(
					elements -> {
						Object element = elements.get(0);

						return Arbitraries.just(element)
							.optional(optionalProbability)
							.map(it -> {
								if (type == OptionalInt.class) {
									return it.map(o -> OptionalInt.of((Integer)o))
										.orElseGet(OptionalInt::empty);
								} else if (type == OptionalLong.class) {
									return it.map(o -> OptionalLong.of((Long)o))
										.orElseGet(OptionalLong::empty);
								} else if (type == OptionalDouble.class) {
									return it.map(o -> OptionalDouble.of((Double)o))
										.orElseGet(OptionalDouble::empty);
								} else {
									return it;
								}
							})
							.sample();
					}
				)
		);
	}
}
