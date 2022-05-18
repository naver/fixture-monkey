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

import java.util.Arrays;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class MapEntryElementArbitraryPropertyGenerator implements ArbitraryPropertyGenerator {
	public static final MapEntryElementArbitraryPropertyGenerator INSTANCE =
		new MapEntryElementArbitraryPropertyGenerator();

	private static final ArbitraryContainerInfo CONTAINER_INFO = new ArbitraryContainerInfo(1, 1);
	private static final double NULL_INJECT = 0.0d;

	@Override
	public ArbitraryProperty generate(ArbitraryPropertyGeneratorContext context) {
		Property property = context.getProperty();
		if (property.getClass() != MapEntryElementProperty.class) {
			throw new IllegalArgumentException(
				"property should be MapEntryElementProperty. property: " + property.getClass()
			);
		}

		MapEntryElementProperty mapEntryElementProperty = (MapEntryElementProperty)property;

		return new ArbitraryProperty(
			property,
			context.getPropertyNameResolver(),
			NULL_INJECT,
			null,
			Arrays.asList(mapEntryElementProperty.getKeyProperty(), mapEntryElementProperty.getValueProperty()),
			CONTAINER_INFO
		);
	}
}
