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

import static java.util.stream.Collectors.toMap;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import com.navercorp.fixturemonkey.api.property.InterfaceJavaMethodProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.5.3", status = API.Status.EXPERIMENTAL)
public final class InterfaceJavaMethodPropertyGenerator implements PropertyGenerator {
	@Override
	public Property generateRootProperty(
		AnnotatedType annotatedType
	) {
		return new RootProperty(annotatedType);
	}

	@Override
	public List<Property> generateObjectChildProperties(
		AnnotatedType annotatedType
	) {
		Class<?> actualType = Types.getActualType(annotatedType.getType());
		try {
			Map<Method, String> propertyNamesByGetter =
				Arrays.stream(Introspector.getBeanInfo(actualType).getPropertyDescriptors())
					.collect(
						toMap(
							PropertyDescriptor::getReadMethod,
							PropertyDescriptor::getName
						)
					);

			return Arrays.stream(actualType.getMethods())
				.filter(it -> it.getParameters().length == 0)
				.map(it -> new InterfaceJavaMethodProperty(
						it.getAnnotatedReturnType(),
						propertyNamesByGetter.getOrDefault(it, it.getName()),
						it.getName(),
						Arrays.asList(it.getAnnotations()),
						Arrays.stream(it.getAnnotations())
							.collect(Collectors.toMap(Annotation::annotationType, Function.identity(), (a1, a2) -> a1))
					)
				)
				.collect(Collectors.toList());

		} catch (IntrospectionException e) {
			throw new IllegalArgumentException("This type could not be introspected." + actualType.getTypeName(), e);
		}
	}

	@Override
	public Property generateElementProperty(
		Property containerProperty,
		AnnotatedType elementType,
		@Nullable Integer index,
		int sequence
	) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property generateMapEntryElementProperty(
		Property containerProperty,
		Property keyProperty,
		Property valueProperty
	) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property generateTupleLikeElementsProperty(
		Property containerProperty,
		List<Property> childProperties, @Nullable Integer index
	) {
		throw new UnsupportedOperationException();
	}
}
