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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class OptionalArbitraryPropertyGenerator implements ArbitraryPropertyGenerator {
	public static final OptionalArbitraryPropertyGenerator INSTANCE = new OptionalArbitraryPropertyGenerator();

	private static final AnnotatedType INTEGER_TYPE = generateAnnotatedTypeWithoutAnnotation(Integer.class);
	private static final AnnotatedType LONG_TYPE = generateAnnotatedTypeWithoutAnnotation(Long.class);
	private static final AnnotatedType DOUBLE_TYPE = generateAnnotatedTypeWithoutAnnotation(Double.class);
	private static final ArbitraryContainerInfo CONTAINER_INFO = new ArbitraryContainerInfo(0, 1);

	@Override
	public ArbitraryProperty generate(ArbitraryPropertyGeneratorContext context) {
		Property property = context.getProperty();

		AnnotatedType valueAnnotatedType = getValueAnnotatedType(property);
		Property valueProperty = new ElementProperty(
			property,
			valueAnnotatedType,
			0,
			0,
			null
		);

		double nullInject = context.getGenerateOptions().getNullInjectGenerator(property)
			.generate(context, CONTAINER_INFO);

		return new ArbitraryProperty(
			property,
			context.getPropertyNameResolver(),
			nullInject,
			context.getElementIndex(),
			Collections.singletonList(valueProperty),
			CONTAINER_INFO
		);
	}

	private AnnotatedType getValueAnnotatedType(Property optionalProperty) {
		Class<?> type = Types.getActualType(optionalProperty.getType());
		if (type == OptionalInt.class) {
			return INTEGER_TYPE;
		}

		if (type == OptionalLong.class) {
			return LONG_TYPE;
		}

		if (type == OptionalDouble.class) {
			return DOUBLE_TYPE;
		}

		if (type != Optional.class) {
			throw new IllegalArgumentException(
				"type is not Optional type. propertyType: " + type
			);
		}

		List<AnnotatedType> genericsTypes = Types.getGenericsTypes(optionalProperty.getAnnotatedType());
		if (genericsTypes.size() != 1) {
			throw new IllegalArgumentException(
				"Optional genericTypes must be have 1 generics type for value. "
					+ "propertyType: " + optionalProperty.getType()
					+ ", genericsTypes: " + genericsTypes
			);
		}

		return genericsTypes.get(0);
	}
}
