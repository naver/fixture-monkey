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

import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.MapKeyElementProperty;
import com.navercorp.fixturemonkey.api.property.MapValueElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class EntryArbitraryPropertyGenerator implements ArbitraryPropertyGenerator {
	public static final EntryArbitraryPropertyGenerator INSTANCE = new EntryArbitraryPropertyGenerator();
	private static final ArbitraryContainerInfo CONTAINER_INFO = new ArbitraryContainerInfo(0, 1);

	@Override
	public ArbitraryProperty generate(ArbitraryPropertyGeneratorContext context) {
		Property property = context.getProperty();

		List<AnnotatedType> genericsTypes = Types.getGenericsTypes(property.getAnnotatedType());
		if (genericsTypes.size() != 2) {
			throw new IllegalArgumentException(
				"Entry genericsTypes must be have 2 generics type for key and value. "
					+ "propertyType: " + property.getType()
					+ ", genericsTypes: " + genericsTypes
			);
		}

		ArbitraryContainerInfo containerInfo = context.getContainerInfo();
		if (containerInfo == null) {
			containerInfo = CONTAINER_INFO;
		}

		int size = containerInfo.getRandomSize();

		AnnotatedType keyType = genericsTypes.get(0);
		AnnotatedType valueType = genericsTypes.get(1);

		List<Property> childProperties = new ArrayList<>();
		for (int sequence = 0; sequence < size; sequence++) {
			childProperties.add(
				new MapEntryElementProperty(
					property,
					new MapKeyElementProperty(
						property,
						keyType,
						sequence
					),
					new MapValueElementProperty(
						property,
						valueType,
						sequence
					)
				)
			);
		}

		double nullInject = context.getGenerateOptions().getNullInjectGenerator(property)
			.generate(context, CONTAINER_INFO);

		return new ArbitraryProperty(
			property,
			context.getPropertyNameResolver(),
			nullInject,
			context.getElementIndex(),
			childProperties,
			containerInfo
		);
	}
}
