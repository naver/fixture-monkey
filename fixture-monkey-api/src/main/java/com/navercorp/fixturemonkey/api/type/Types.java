package com.navercorp.fixturemonkey.api.type;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class Types {
	public static Class<?> getActualType(Type type) {
		if (type.getClass() == Class.class) {
			return (Class<?>)type;
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
}
