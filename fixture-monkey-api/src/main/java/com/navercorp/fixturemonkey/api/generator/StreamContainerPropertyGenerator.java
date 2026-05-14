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
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.DefaultContainerElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class StreamContainerPropertyGenerator implements ContainerPropertyGenerator {
	public static final StreamContainerPropertyGenerator INSTANCE = new StreamContainerPropertyGenerator();

	private static final JvmType INTEGER_TYPE = new JavaType(Integer.class);
	private static final JvmType LONG_TYPE = new JavaType(Long.class);
	private static final JvmType DOUBLE_TYPE = new JavaType(Double.class);

	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		JvmType elementJvmType = getElementJvmType(property);

		ArbitraryContainerInfo containerInfo = context.getContainerInfo();

		int size = containerInfo.getRandomSize();
		List<Property> childProperties = new ArrayList<>();
		for (int sequence = 0; sequence < size; sequence++) {
			childProperties.add(
				new DefaultContainerElementProperty(
					property,
					new TypeParameterProperty(elementJvmType),
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

	private JvmType getElementJvmType(Property streamProperty) {
		Class<?> type = streamProperty.getJvmType().getRawType();
		if (IntStream.class.isAssignableFrom(type)) {
			return INTEGER_TYPE;
		}

		if (LongStream.class.isAssignableFrom(type)) {
			return LONG_TYPE;
		}

		if (DoubleStream.class.isAssignableFrom(type)) {
			return DOUBLE_TYPE;
		}

		if (!(Stream.class.isAssignableFrom(type))) {
			throw new IllegalArgumentException(
				"type is not Stream type. propertyType: " + type
			);
		}

		List<? extends JvmType> typeVariables = streamProperty.getJvmType().getTypeVariables();
		if (typeVariables.size() != 1) {
			throw new IllegalArgumentException(
				"Stream typeVariables must be have 1 generics type for value. "
					+ "propertyType: " + type
					+ ", typeVariables: " + typeVariables
			);
		}

		return typeVariables.get(0);
	}
}
