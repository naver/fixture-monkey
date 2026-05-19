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
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.DefaultContainerElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArrayContainerPropertyGenerator implements ContainerPropertyGenerator {
	public static final ArrayContainerPropertyGenerator INSTANCE =
		new ArrayContainerPropertyGenerator();

	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		ArbitraryContainerInfo containerInfo = context.getContainerInfo();

		int size = containerInfo.getRandomSize();
		JvmType elementType = resolveArrayElementType(property.getJvmType());
		List<Property> childProperties = new ArrayList<>();
		for (int sequence = 0; sequence < size; sequence++) {
			childProperties.add(
				new DefaultContainerElementProperty(
					property,
					new TypeParameterProperty(elementType),
					sequence,
					sequence
				)
			);
		}

		return new ContainerProperty(
			childProperties,
			containerInfo
		);
	}

	private static JvmType resolveArrayElementType(JvmType arrayType) {
		JvmType componentType = arrayType.getComponentType();
		if (componentType != null) {
			return componentType;
		}
		Class<?> rawType = arrayType.getRawType();
		if (rawType.isArray()) {
			return new ReflectiveJvmType(rawType.getComponentType());
		}
		throw new IllegalArgumentException("given type is not an array type: " + arrayType);
	}
}
