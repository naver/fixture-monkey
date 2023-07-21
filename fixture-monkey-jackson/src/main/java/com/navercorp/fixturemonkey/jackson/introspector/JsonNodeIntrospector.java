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

package com.navercorp.fixturemonkey.jackson.introspector;

import java.util.HashMap;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty.MapEntryElementType;
import com.navercorp.fixturemonkey.jackson.FixtureMonkeyJackson;

@API(since = "0.4.3", status = Status.MAINTAINED)
public final class JsonNodeIntrospector implements ArbitraryIntrospector {
	public static final JsonNodeIntrospector INSTANCE = new JsonNodeIntrospector(
		FixtureMonkeyJackson.defaultObjectMapper()
	);

	private final ObjectMapper objectMapper;

	public JsonNodeIntrospector(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		ContainerProperty containerProperty = property.getContainerProperty();
		if (containerProperty == null || containerProperty.getContainerInfo() == null) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.containerBuilder()
				.elements(context.getElementCombinableArbitraryList())
				.build(
					elements -> {
						Map<Object, Object> map = new HashMap<>();
						for (Object element : elements) {
							MapEntryElementType mapEntryElementType = (MapEntryElementType)element;
							if (mapEntryElementType.getKey() == null) {
								throw new IllegalArgumentException("Map key cannot be null.");
							}
							map.put(mapEntryElementType.getKey(), mapEntryElementType.getValue());
						}

						return objectMapper.convertValue(map, JsonNode.class);
					}
				)
		);
	}
}
