package com.navercorp.objectfarm.api.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public abstract class JvmTypes {
	public static JvmType resolveJvmType(JvmType parentType, Type type, List<Annotation> annotations) {
		if (!Types.isGenericType(type)) {
			return new JavaType(Types.getActualType(type));
		}

		if (type instanceof TypeVariable) {
			return new JavaType(resolveGenericTypes(parentType, type).get(0));
		}

		List<Class<?>> actualGenericTypes = resolveGenericTypes(parentType, type);
		List<JavaType> actualTypeVariables = actualGenericTypes.stream()
			.map(JavaType::new)
			.collect(Collectors.toList());

		return new JavaType(
			Types.getActualType(type),
			actualTypeVariables,
			annotations
		);
	}

	private static List<Class<?>> resolveGenericTypes(JvmType parentType, Type fieldGenericType) {
		if (fieldGenericType instanceof ParameterizedType) {
			ParameterizedType genericType = (ParameterizedType)fieldGenericType;
			return Arrays.stream(genericType.getActualTypeArguments())
				.map(Types::getActualType)
				.collect(Collectors.toList());
		}

		if (fieldGenericType instanceof GenericArrayType) {
			Type genericComponentType = ((GenericArrayType)fieldGenericType).getGenericComponentType();
			if (genericComponentType instanceof ParameterizedType) {
				ParameterizedType genericType = (ParameterizedType)genericComponentType;

				List<? extends JvmType> parentTypeTypeVariables = parentType.getTypeVariables(); // It is not erased.
				List<Type> erasedTypeVariableCandidates = Arrays.stream(genericType.getActualTypeArguments())
					.collect(Collectors.toList());

				List<Class<?>> resolvedErasedTypeVariables = new ArrayList<>();
				for (int i = 0; i < erasedTypeVariableCandidates.size(); i++) {
					resolvedErasedTypeVariables.add(
						resolveErasedTypeVariableType(
							erasedTypeVariableCandidates.get(i),
							parentTypeTypeVariables.get(i)
						)
					);
				}

				return Collections.unmodifiableList(resolvedErasedTypeVariables);
			}
		}

		if (fieldGenericType instanceof TypeVariable) {
			return Collections.singletonList(
				resolveErasedTypeVariableType(
					fieldGenericType,
					parentType.getTypeVariables().get(0)
				)
			);
		}

		throw new IllegalArgumentException("Unsupported generic type: " + fieldGenericType);
	}

	private static Class<?> resolveErasedTypeVariableType(
		Type erasedTypeVariableCandidate,
		@Nullable JvmType parentTypeTypeVariable
	) {
		if (!(erasedTypeVariableCandidate instanceof TypeVariable)) {
			return Types.getActualType(erasedTypeVariableCandidate);
		}

		if (parentTypeTypeVariable == null) {
			return Types.getActualType(erasedTypeVariableCandidate); // It is erased. So it always returns Object.class
		}

		return parentTypeTypeVariable.getRawType();
	}
}
