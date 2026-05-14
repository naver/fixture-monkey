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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.MapKeyElementProperty;
import com.navercorp.fixturemonkey.api.property.MapValueElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class MapContainerPropertyGenerator implements ContainerPropertyGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(MapContainerPropertyGenerator.class);
	private static final JvmType DEFAULT_ELEMENT_JVM_TYPE = new JavaType(String.class);

	public static final MapContainerPropertyGenerator INSTANCE = new MapContainerPropertyGenerator();

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		List<? extends JvmType> typeVariables = property.getJvmType().getTypeVariables();
		JvmType keyType = !typeVariables.isEmpty() ? typeVariables.get(0) : DEFAULT_ELEMENT_JVM_TYPE;
		JvmType valueType = typeVariables.size() > 1 ? typeVariables.get(1) : DEFAULT_ELEMENT_JVM_TYPE;
		Class<?> actualKeyType = keyType.getRawType();

		ArbitraryContainerInfo containerInfo = context.getContainerInfo();
		if (actualKeyType.isEnum()) {
			int enumSize = EnumSet.allOf((Class<? extends Enum>)actualKeyType).size();
			if (containerInfo.getElementMaxSize() > enumSize) {
				LOGGER.warn("Map key enum should not be bigger than enum size. enum size : " + enumSize);
			}
			containerInfo = new ArbitraryContainerInfo(
				Math.min(containerInfo.getElementMinSize(), enumSize),
				Math.min(containerInfo.getElementMaxSize(), enumSize)
			);
		}

		int size = containerInfo.getRandomSize();

		List<Property> childProperties = new ArrayList<>();
		for (int sequence = 0; sequence < size; sequence++) {
			childProperties.add(
				new MapEntryElementProperty(
					property,
					new MapKeyElementProperty(
						property,
						new TypeParameterProperty(keyType),
						sequence
					),
					new MapValueElementProperty(
						property,
						new TypeParameterProperty(valueType),
						sequence
					)
				)
			);
		}

		return new ContainerProperty(
			Collections.unmodifiableList(childProperties),
			containerInfo
		);
	}
}
