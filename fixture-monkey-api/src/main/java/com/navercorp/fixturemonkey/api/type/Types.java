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

package com.navercorp.fixturemonkey.api.type;

import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class Types {
	public static Class<?> getActualType(Type type) {
		if (type.getClass() == Class.class) {
			return (Class<?>)type;
		}

		if (WildcardType.class.isAssignableFrom(type.getClass())) {
			WildcardType wildcardType = (WildcardType)type;
			Type upperBound = wildcardType.getUpperBounds()[0];
			return getActualType(upperBound);
		}

		if (TypeVariable.class.isAssignableFrom(type.getClass())) {
			GenericDeclaration genericDeclaration = ((TypeVariable<?>)type).getGenericDeclaration();
			if (genericDeclaration.getClass() == Class.class) {
				return (Class<?>)genericDeclaration;
			} else {
				// Method? Constructor?
				throw new UnsupportedOperationException(
					"Unsupported TypeVariable's generationDeclaration type. type: " + genericDeclaration.getClass()
				);
			}
		}

		if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Type rawType = parameterizedType.getRawType();
			return getActualType(rawType);
		}

		throw new UnsupportedOperationException(
			"Unsupported Type to get actualType. type: " + type.getClass()
		);
	}

	public static List<Type> getGenericsTypes(Type type) {
		if (type.getClass() == Class.class) {
			return Collections.emptyList();
		}

		if (WildcardType.class.isAssignableFrom(type.getClass())) {
			WildcardType wildcardType = (WildcardType)type;
			return getGenericsTypes(wildcardType.getUpperBounds()[0]);
		}

		if (TypeVariable.class.isAssignableFrom(type.getClass())) {
			GenericDeclaration genericDeclaration = ((TypeVariable<?>)type).getGenericDeclaration();
			if (genericDeclaration.getClass() == Class.class) {
				return Collections.emptyList();
			} else {
				// Method? Constructor?
				throw new UnsupportedOperationException(
					"Unsupported TypeVariable's generationDeclaration type. type: " + genericDeclaration.getClass()
				);
			}
		}

		if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Type[] rawTypes = parameterizedType.getActualTypeArguments();
			if (rawTypes == null) {
				return Collections.emptyList();
			}

			return Arrays.asList(rawTypes);
		}

		throw new UnsupportedOperationException(
			"Unsupported Type to get genericsTypes. type: " + type.getClass()
		);
	}

	public static Type resolveWithTypeReferenceGenerics(TypeReference<?> ownerTypeReference, Field field) {
		Type ownerType = ownerTypeReference.getType();
		if (!(ownerType instanceof ParameterizedType)) {
			return field.getType();
		}

		ParameterizedType ownerParameterizedType = (ParameterizedType)ownerType;
		Type[] ownerGenericsTypes = ownerParameterizedType.getActualTypeArguments();
		if (ownerGenericsTypes == null || ownerGenericsTypes.length == 0) {
			return field.getType();
		}

		Class<?> ownerActualType = Types.getActualType(ownerParameterizedType.getRawType());
		List<Type> ownerTypeVariableParameters = Arrays.asList(ownerActualType.getTypeParameters());

		Type fieldGenericsType = field.getGenericType();
		if (TypeVariable.class.isAssignableFrom(fieldGenericsType.getClass())) {
			int index = ownerTypeVariableParameters.indexOf(fieldGenericsType);
			return ownerGenericsTypes[index];
		}

		if (!(fieldGenericsType instanceof ParameterizedType)) {
			return field.getType();
		}

		ParameterizedType fieldParameterizedType = (ParameterizedType)fieldGenericsType;
		Type[] fieldGenericsTypes = fieldParameterizedType.getActualTypeArguments();
		if (fieldGenericsTypes == null || fieldGenericsTypes.length == 0) {
			return field.getType();
		}

		Type[] resolvedGenericsTypes = new Type[fieldGenericsTypes.length];
		for (int i = 0; i < fieldGenericsTypes.length; i++) {
			Type generics = fieldGenericsTypes[i];
			if (generics instanceof ParameterizedType || generics.getClass() == Class.class) {
				resolvedGenericsTypes[i] = generics;
				continue;
			}

			if (TypeVariable.class.isAssignableFrom(generics.getClass())) {
				TypeVariable<?> typeVariable = (TypeVariable<?>)generics;
				int index = ownerTypeVariableParameters.indexOf(typeVariable);
				resolvedGenericsTypes[i] = ownerGenericsTypes[index];
			}
		}

		Type resultRawType = fieldParameterizedType.getRawType();
		Type resultOwnerType = fieldParameterizedType.getOwnerType();
		return new ParameterizedType() {
			@Override
			public Type[] getActualTypeArguments() {
				return resolvedGenericsTypes;
			}

			@Override
			public Type getRawType() {
				return resultRawType;
			}

			@Override
			public Type getOwnerType() {
				return resultOwnerType;
			}
		};
	}
}
