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
import java.util.EnumSet;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.property.DefaultContainerElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.objectfarm.api.type.JvmType;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class SetContainerPropertyGenerator implements ContainerPropertyGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(SetContainerPropertyGenerator.class);

	public static final SetContainerPropertyGenerator INSTANCE = new SetContainerPropertyGenerator();

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		List<? extends JvmType> elementTypes = property.getJvmType().getTypeVariables();
		JvmType elementType = elementTypes.size() == 1 ? elementTypes.get(0) : DEFAULT_ELEMENT_JVM_TYPE;

		ArbitraryContainerInfo containerInfo = context.getContainerInfo();
		Class<?> actualElementType = elementType.getRawType();

		if (actualElementType.isEnum()) {
			int enumSize = EnumSet.allOf((Class<? extends Enum>)actualElementType).size();
			if (containerInfo.getElementMaxSize() > enumSize) {
				LOGGER.warn("Set of enum should not be bigger than enum size. enum size : " + enumSize);
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
				new DefaultContainerElementProperty(
					property,
					new TypeParameterProperty(elementType),
					null,
					sequence
				)
			);
		}

		return new ContainerProperty(
			childProperties,
			containerInfo
		);
	}
}
