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
import java.util.EnumSet;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class SetContainerPropertyGenerator implements ContainerPropertyGenerator {
	public static final SetContainerPropertyGenerator INSTANCE = new SetContainerPropertyGenerator();

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		List<AnnotatedType> elementTypes = Types.getGenericsTypes(property.getAnnotatedType());
		if (elementTypes.size() != 1) {
			throw new IllegalArgumentException(
				"Set elementsTypes must be have 1 generics type for element. "
					+ "propertyType: " + property.getType()
					+ ", elementTypes: " + elementTypes
			);
		}

		AnnotatedType elementType = elementTypes.get(0);
		ArbitraryContainerInfo containerInfo = context.getContainerInfo();
		Class<?> actualElementType = Types.getActualType(elementType.getType());

		if (containerInfo == null) {
			containerInfo = context.getGenerateOptions()
				.getArbitraryContainerInfoGenerator(property)
				.generate(context);

			if (actualElementType.isEnum()) {
				int enumSize = EnumSet.allOf((Class<? extends Enum>)actualElementType).size();
				containerInfo = new ArbitraryContainerInfo(
					Math.min(containerInfo.getElementMinSize(), enumSize),
					Math.min(containerInfo.getElementMaxSize(), enumSize)
				);
			}
		} else {
			if (actualElementType.isEnum()) {
				int enumSize = EnumSet.allOf((Class<? extends Enum>)actualElementType).size();
				if (containerInfo.getElementMaxSize() > enumSize) {
					throw new IllegalArgumentException(
						"Set of enum should not be bigger than enum size."
					);
				}
			}
		}

		int size = containerInfo.getRandomSize();
		List<Property> childProperties = new ArrayList<>();
		for (int sequence = 0; sequence < size; sequence++) {
			childProperties.add(
				new ElementProperty(
					property,
					elementType,
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
