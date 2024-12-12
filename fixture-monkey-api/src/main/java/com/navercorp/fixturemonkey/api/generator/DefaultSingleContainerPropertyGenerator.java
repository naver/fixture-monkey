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

import com.navercorp.fixturemonkey.api.property.DefaultContainerElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.3", status = Status.MAINTAINED)
public final class DefaultSingleContainerPropertyGenerator implements ContainerPropertyGenerator {
	private static final TypeReference<String> DEFAULT_ELEMENT_TYPE =
		new TypeReference<String>() {
		};

	public static final DefaultSingleContainerPropertyGenerator INSTANCE =
		new DefaultSingleContainerPropertyGenerator();

	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		List<AnnotatedType> elementTypes = Types.getGenericsTypes(property.getAnnotatedType());
		if (elementTypes.size() > 1) {
			throw new IllegalArgumentException(
				"Container elementsTypes support 1 generics type. "
					+ "You should be custom ContainerPropertyGenerator for N generics container type. "
					+ "propertyType: " + property.getType()
					+ ", elementTypes: " + elementTypes
			);
		}

		ArbitraryContainerInfo containerInfo = context.getContainerInfo();

		int size = containerInfo.getRandomSize();
		List<Property> childProperties = new ArrayList<>();
		for (int sequence = 0; sequence < size; sequence++) {
			Integer elementIndex = sequence;
			AnnotatedType elementType = elementTypes.isEmpty()
				? DEFAULT_ELEMENT_TYPE.getAnnotatedType()
				: elementTypes.get(0);

			childProperties.add(
				new DefaultContainerElementProperty(
					property,
					new TypeParameterProperty(elementType),
					elementIndex,
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
