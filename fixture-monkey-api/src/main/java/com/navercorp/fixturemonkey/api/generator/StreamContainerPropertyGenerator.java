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

import static com.navercorp.fixturemonkey.api.type.Types.generateAnnotatedTypeWithoutAnnotation;

import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class StreamContainerPropertyGenerator implements ContainerPropertyGenerator {
	public static final StreamContainerPropertyGenerator INSTANCE = new StreamContainerPropertyGenerator();

	private static final AnnotatedType INTEGER_TYPE = generateAnnotatedTypeWithoutAnnotation(Integer.class);
	private static final AnnotatedType LONG_TYPE = generateAnnotatedTypeWithoutAnnotation(Long.class);
	private static final AnnotatedType DOUBLE_TYPE = generateAnnotatedTypeWithoutAnnotation(Double.class);

	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		AnnotatedType elementAnnotatedType = getElementAnnotatedType(property);

		ArbitraryContainerInfo containerInfo = context.getContainerInfo();
		if (containerInfo == null) {
			containerInfo = context.getGenerateOptions()
				.getArbitraryContainerInfoGenerator(property)
				.generate(context);
		}

		int size = containerInfo.getRandomSize();
		List<Property> childProperties = new ArrayList<>();
		for (int sequence = 0; sequence < size; sequence++) {
			childProperties.add(
				new ElementProperty(
					property,
					elementAnnotatedType,
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

	private AnnotatedType getElementAnnotatedType(Property streamProperty) {
		Class<?> type = Types.getActualType(streamProperty.getType());
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

		List<AnnotatedType> genericsTypes = Types.getGenericsTypes(streamProperty.getAnnotatedType());
		if (genericsTypes.size() != 1) {
			throw new IllegalArgumentException(
				"Stream genericTypes must be have 1 generics type for value. "
					+ "propertyType: " + streamProperty.getType()
					+ ", genericsTypes: " + genericsTypes
			);
		}

		return genericsTypes.get(0);
	}
}
