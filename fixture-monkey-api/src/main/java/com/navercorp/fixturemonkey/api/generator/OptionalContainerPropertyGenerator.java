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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.SingleElementProperty;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class OptionalContainerPropertyGenerator implements ContainerPropertyGenerator {
	public static final OptionalContainerPropertyGenerator INSTANCE = new OptionalContainerPropertyGenerator();

	private static final AnnotatedType INTEGER_TYPE = generateAnnotatedTypeWithoutAnnotation(Integer.class);
	private static final AnnotatedType LONG_TYPE = generateAnnotatedTypeWithoutAnnotation(Long.class);
	private static final AnnotatedType DOUBLE_TYPE = generateAnnotatedTypeWithoutAnnotation(Double.class);
	private static final ArbitraryContainerInfo CONTAINER_INFO = new ArbitraryContainerInfo(0, 1);

	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		AnnotatedType valueAnnotatedType = getValueAnnotatedType(property);
		Type childType = valueAnnotatedType.getType();
		AnnotatedType childAnnotatedType = Types.generateAnnotatedTypeWithoutAnnotation(childType);

		Property childProperty = new Property() {
			@Override
			public Type getType() {
				return childType;
			}

			@Override
			public AnnotatedType getAnnotatedType() {
				return childAnnotatedType;
			}

			@Nullable
			@Override
			public String getName() {
				return null;
			}

			@Override
			public List<Annotation> getAnnotations() {
				return Arrays.asList(childAnnotatedType.getAnnotations());
			}

			@Nullable
			@Override
			public Object getValue(Object instance) {
				Class<?> actualType = Types.getActualType(instance.getClass());
				if (isOptional(actualType)) {
					return getOptionalValue(instance);
				}

				throw new IllegalArgumentException("given value has no match");
			}
		};

		SingleElementProperty singleElementProperty = new SingleElementProperty(childProperty, valueAnnotatedType);

		return new ContainerProperty(
			Collections.singletonList(singleElementProperty),
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

	private boolean isOptional(Class<?> type) {
		return Optional.class.isAssignableFrom(type)
			|| OptionalInt.class.isAssignableFrom(type)
			|| OptionalLong.class.isAssignableFrom(type)
			|| OptionalDouble.class.isAssignableFrom(type);
	}

	@Nullable
	private Object getOptionalValue(Object obj) {
		Class<?> actualType = Types.getActualType(obj.getClass());
		if (Optional.class.isAssignableFrom(actualType)) {
			return ((Optional<?>)obj).orElse(null);
		}

		if (OptionalInt.class.isAssignableFrom(actualType)) {
			return ((OptionalInt)obj).orElse(0);
		}

		if (OptionalLong.class.isAssignableFrom(actualType)) {
			return ((OptionalLong)obj).orElse(0L);
		}

		if (OptionalDouble.class.isAssignableFrom(actualType)) {
			return ((OptionalDouble)obj).orElse(Double.NaN);
		}

		throw new IllegalArgumentException("given value is not optional, actual type : " + actualType);
	}
}
