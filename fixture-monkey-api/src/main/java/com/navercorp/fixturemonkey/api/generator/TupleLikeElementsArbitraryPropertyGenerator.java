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

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TupleLikeElementsProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class TupleLikeElementsArbitraryPropertyGenerator implements ArbitraryPropertyGenerator {
	public static TupleLikeElementsArbitraryPropertyGenerator INSTANCE =
		new TupleLikeElementsArbitraryPropertyGenerator();

	private static final ArbitraryContainerInfo CONTAINER_INFO = new ArbitraryContainerInfo(1, 1);
	private static final double NULL_INJECT = 0.0d;

	@Override
	public ArbitraryProperty generate(ArbitraryPropertyGeneratorContext context) {
		Property property = context.getProperty();
		if (property.getClass() != TupleLikeElementsProperty.class) {
			throw new IllegalArgumentException(
				"property should be TupleLikeElementsProperty. property: " + property.getClass()
			);
		}

		TupleLikeElementsProperty tupleLikeElementsProperty = (TupleLikeElementsProperty)property;

		return new ArbitraryProperty(
			property,
			context.getPropertyNameResolver(),
			NULL_INJECT,
			context.getElementIndex(),
			tupleLikeElementsProperty.getElementsProperties(),
			CONTAINER_INFO
		);
	}
}
