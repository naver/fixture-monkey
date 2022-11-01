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

import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

/**
 * Deprecated. use DefaultSingleContainerPropertyGenerator instead.
 */
@API(since = "0.4.0", status = Status.DEPRECATED)
public final class DefaultContainerPropertyGenerator implements ContainerPropertyGenerator {
	public static final DefaultContainerPropertyGenerator INSTANCE =
		new DefaultContainerPropertyGenerator();

	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		List<AnnotatedType> elementTypes = Types.getGenericsTypes(property.getAnnotatedType());
		if (elementTypes.size() != 1) {
			throw new IllegalArgumentException(
				"Container elementsTypes must be have 1 generics type for element. "
					+ "propertyType: " + property.getType()
					+ ", elementTypes: " + elementTypes
			);
		}

		ArbitraryContainerInfo containerInfo = context.getContainerInfo();
		if (containerInfo == null) {
			containerInfo = context.getGenerateOptions()
				.getArbitraryContainerInfoGenerator(property)
				.generate(context);
		}

		int size = containerInfo.getRandomSize();
		AnnotatedType elementType = elementTypes.get(0);
		List<Property> childProperties = new ArrayList<>();
		for (int sequence = 0; sequence < size; sequence++) {
			Integer elementIndex = sequence;

			childProperties.add(
				new ElementProperty(
					property,
					elementType,
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
