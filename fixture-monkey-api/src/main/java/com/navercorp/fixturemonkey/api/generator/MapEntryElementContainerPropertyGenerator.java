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
public final class MapEntryElementContainerPropertyGenerator implements ContainerPropertyGenerator {
	public static final MapEntryElementContainerPropertyGenerator INSTANCE =
		new MapEntryElementContainerPropertyGenerator();

	private static final ArbitraryContainerInfo CONTAINER_INFO = new ArbitraryContainerInfo(1, 1, false);

	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();
		if (property.getClass() != MapEntryElementProperty.class) {
			throw new IllegalArgumentException(
				"property should be MapEntryElementProperty. property: " + property.getClass()
			);
		}

		MapEntryElementProperty mapEntryElementProperty = (MapEntryElementProperty)property;

		return new ContainerProperty(
			Arrays.asList(mapEntryElementProperty.getKeyProperty(), mapEntryElementProperty.getValueProperty()),
			CONTAINER_INFO
		);
	}
}
