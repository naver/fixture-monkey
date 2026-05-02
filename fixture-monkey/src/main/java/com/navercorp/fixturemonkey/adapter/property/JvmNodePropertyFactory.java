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

package com.navercorp.fixturemonkey.adapter.property;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.property.ConstructorProperty;
import com.navercorp.fixturemonkey.api.property.DefaultContainerElementProperty;
import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.InterfaceJavaMethodProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyDescriptorProperty;
import com.navercorp.fixturemonkey.api.property.TypeNameProperty;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.nodecandidate.ConstructorParamCreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.CreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.FieldAccessCreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.MethodInvocationCreationMethod;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Factory for creating appropriate {@link Property} implementations from {@link JvmNode} and {@link JvmType}.
 * <p>
 * This factory uses {@link CreationMethod} metadata to restore the original Property type
 * that was produced by the corresponding {@link com.navercorp.fixturemonkey.api.property.PropertyGenerator}.
 * <p>
 * The factory implements {@link Function}{@code <JvmNode, Property>} so it can be used as a
 * method reference for property caching (e.g., {@code computeIfAbsent(node, factory)}).
 * Subclasses can override {@link #apply(JvmNode)} to customize Property creation.
 *
 * @since 1.1.17
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public class JvmNodePropertyFactory implements Function<JvmNode, Property> {
	private final Function<JvmNode, @Nullable JvmNode> parentLookup;

	public JvmNodePropertyFactory(Function<JvmNode, @Nullable JvmNode> parentLookup) {
		this.parentLookup = parentLookup;
	}

	/**
	 * Creates a {@link Property} from a {@link JvmType} for type-matching purposes.
	 * <p>
	 * Used when only type information is needed (e.g., for matching against
	 * {@link com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions} matchers).
	 *
	 * @param jvmType the JvmType to convert
	 * @return a {@link TypeParameterProperty} wrapping the type
	 */
	public static Property fromType(JvmType jvmType) {
		AnnotatedType annotatedType = buildAnnotatedType(jvmType);
		return new TypeParameterProperty(annotatedType);
	}

	/**
	 * Creates an appropriate {@link Property} from a {@link JvmNode} based on its {@link CreationMethod}.
	 * <p>
	 * The factory restores the original Property type that was produced by the corresponding
	 * {@link com.navercorp.fixturemonkey.api.property.PropertyGenerator}:
	 * <ul>
	 *   <li>{@code FIELD} → {@link FieldProperty}</li>
	 *   <li>{@code CONSTRUCTOR} → {@link ConstructorProperty}</li>
	 *   <li>{@code METHOD} → {@link PropertyDescriptorProperty}</li>
	 *   <li>others → {@link TypeNameProperty}</li>
	 * </ul>
	 *
	 * @param node the JvmNode to convert
	 * @return an appropriate Property implementation
	 */
	@SuppressWarnings({"argument", "dereference.of.nullable", "unboxing.of.nullable"})
	@Override
	public Property apply(JvmNode node) {
		JvmType jvmType = node.getConcreteType();
		AnnotatedType annotatedType = buildAnnotatedType(jvmType);
		CreationMethod creationMethod = node.getCreationMethod();
		Boolean nullable = jvmType.getNullable();

		if (creationMethod == null) {
			return new TypeNameProperty(annotatedType, node.getNodeName(), nullable);
		}

		switch (creationMethod.getType()) {
			case FIELD:
				return new FieldProperty(annotatedType, ((FieldAccessCreationMethod)creationMethod).getField());
			case CONSTRUCTOR:
				ConstructorParamCreationMethod ctorMethod = (ConstructorParamCreationMethod)creationMethod;
				java.lang.reflect.Constructor<?> ctor = ctorMethod.getConstructor();

				// Check if the actual constructor parameter is primitive.
				// TypeReference<Int> boxes int to Integer, losing primitive info and
				// causing incorrect null injection for primitive parameters.
				// Match by name since parameterIndex in ConstructorParamCreationMethod
				// may not correspond to the actual constructor parameter position.
				Class<?> actualParamType = findConstructorParamType(ctor, node.getNodeName());
				if (actualParamType != null && actualParamType.isPrimitive()) {
					annotatedType = Types.generateAnnotatedTypeWithoutAnnotation(actualParamType);
					nullable = false;
				}

				Property paramProperty = new TypeNameProperty(annotatedType, node.getNodeName(), nullable);
				Property fieldProperty = findFieldProperty(ctor.getDeclaringClass(), node.getNodeName());
				return new ConstructorProperty(paramProperty, ctor, fieldProperty, nullable);
			case METHOD:
				return fromMethodCreation(
					annotatedType,
					node.getNodeName(),
					(MethodInvocationCreationMethod)creationMethod
				);
			case CONTAINER_ELEMENT:
				JvmNode parentNode = parentLookup.apply(node);
				if (parentNode != null) {
					JvmNode annotationSource = parentNode;
					JvmNode grandParent = parentLookup.apply(parentNode);
					if (
						grandParent != null
							&& grandParent.getCreationMethod() != null
							&& grandParent.getCreationMethod().getType()
							!= CreationMethod.CreationMethodType.CONTAINER_ELEMENT
					) {
						annotationSource = grandParent;
					}
					Property containerProperty = fromTypePreservingAnnotations(annotationSource.getConcreteType());
					Property elementProperty = new TypeNameProperty(annotatedType, node.getNodeName(), null);
					return new DefaultContainerElementProperty(
						containerProperty,
						elementProperty,
						node.getIndex(),
						node.getIndex() != null ? node.getIndex() : 0
					);
				}
				return new TypeNameProperty(annotatedType, node.getNodeName(), null);
			default:
				return new TypeNameProperty(annotatedType, node.getNodeName(), null);
		}
	}

	/**
	 * Creates a {@link TypeParameterProperty} from a {@link JvmType}, ensuring all annotations
	 * stored in the JvmType are preserved. Unlike {@link #fromType(JvmType)}, this method
	 * bypasses the cached {@code AnnotatedType} which may not include annotations
	 * merged from multiple sources (e.g., field + setter annotations via {@code CompositeProperty}).
	 */
	private static Property fromTypePreservingAnnotations(JvmType jvmType) {
		List<Annotation> allAnnotations = jvmType.getAnnotations();
		AnnotatedType baseType = buildAnnotatedType(jvmType);
		Type type = baseType.getType();

		AnnotatedType annotatedType = new AnnotatedType() {
			@Override
			public Type getType() {
				return type;
			}

			@Override
			@SuppressWarnings({"unchecked", "cast.unsafe"})
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return (T)allAnnotations
					.stream()
					.filter(a -> a.annotationType().equals(annotationClass))
					.findFirst()
					.orElse(null);
			}

			@Override
			public Annotation[] getAnnotations() {
				return allAnnotations.toArray(new Annotation[0]);
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return getAnnotations();
			}
		};
		return new TypeParameterProperty(annotatedType);
	}

	private Property fromMethodCreation(
		AnnotatedType annotatedType,
		String name,
		MethodInvocationCreationMethod method
	) {
		Method invokedMethod = method.getMethod();
		Class<?> declaringClass = invokedMethod.getDeclaringClass();

		// For interface methods, return InterfaceJavaMethodProperty (implements MethodProperty)
		// so that AnonymousArbitraryIntrospector can recognize them
		if (declaringClass.isInterface()) {
			return new InterfaceJavaMethodProperty(
				annotatedType,
				name,
				invokedMethod.getName(),
				Arrays.asList(invokedMethod.getAnnotations())
			);
		}

		PropertyDescriptor descriptor = TypeCache.getPropertyDescriptorsByPropertyName(declaringClass).get(name);
		if (descriptor != null) {
			return new PropertyDescriptorProperty(annotatedType, descriptor);
		}
		return new TypeNameProperty(annotatedType, name, null);
	}

	private static @Nullable Class<?> findConstructorParamType(
		java.lang.reflect.Constructor<?> constructor,
		String paramName
	) {
		if (paramName == null) {
			return null;
		}

		String[] names = TypeCache.getParameterNames(constructor);
		Class<?>[] types = constructor.getParameterTypes();
		for (int i = 0; i < names.length && i < types.length; i++) {
			if (paramName.equals(names[i])) {
				return types[i];
			}
		}

		return null;
	}

	/**
	 * Finds the field matching the given name in the declaring class and creates a FieldProperty.
	 * Used to restore field-level annotations (e.g., {@code @NotNull}) that are not copied
	 * to constructor parameters by annotation processors like Lombok.
	 *
	 * @param declaringClass the class declaring the constructor
	 * @param fieldName the name of the field to find
	 * @return a FieldProperty wrapping the field, or null if the field is not found
	 */
	private static @Nullable Property findFieldProperty(Class<?> declaringClass, String fieldName) {
		if (fieldName == null) {
			return null;
		}
		java.lang.reflect.Field field = TypeCache.getFieldsByName(declaringClass).get(fieldName);
		if (field != null) {
			return new FieldProperty(field);
		}
		return null;
	}

	/**
	 * Builds an {@link AnnotatedType} from a {@link JvmType}.
	 * <p>
	 * If the JvmType provides an AnnotatedType directly (for backward compatibility),
	 * it will be used. Otherwise, a new AnnotatedType is constructed from the
	 * raw type and annotations.
	 *
	 * @param jvmType the JvmType to convert
	 * @return an AnnotatedType representing the JvmType
	 */
	static AnnotatedType buildAnnotatedType(JvmType jvmType) {
		try {
			AnnotatedType existing = jvmType.getAnnotatedType();
			if (existing != null) {
				return existing;
			}
		} catch (UnsupportedOperationException ignored) {
		}

		Class<?> rawType = jvmType.getRawType();
		List<Annotation> annotations = jvmType.getAnnotations();
		List<? extends JvmType> typeVariables = jvmType.getTypeVariables();

		final Type type;
		if (typeVariables != null && !typeVariables.isEmpty()) {
			Type[] typeArgs = typeVariables
				.stream()
				.map(tv -> buildAnnotatedType(tv).getType())
				.toArray(Type[]::new);
			type = new ParameterizedType() {
				@Override
				public Type[] getActualTypeArguments() {
					return typeArgs;
				}

				@Override
				public Type getRawType() {
					return rawType;
				}

				@Override
				public @Nullable Type getOwnerType() {
					return null;
				}

				@Override
				public String toString() {
					StringBuilder sb = new StringBuilder(rawType.getName());
					if (typeArgs.length > 0) {
						sb.append("<");
						for (int i = 0; i < typeArgs.length; i++) {
							if (i > 0) {
								sb.append(", ");
							}
							sb.append(typeArgs[i].getTypeName());
						}
						sb.append(">");
					}
					return sb.toString();
				}
			};
		} else {
			type = rawType;
		}

		return new AnnotatedType() {
			@Override
			public Type getType() {
				return type;
			}

			@Override
			@SuppressWarnings({"unchecked", "cast.unsafe"})
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return (T)annotations
					.stream()
					.filter(a -> a.annotationType().equals(annotationClass))
					.findFirst()
					.orElse(null);
			}

			@Override
			public Annotation[] getAnnotations() {
				return annotations.toArray(new Annotation[0]);
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return getAnnotations();
			}
		};
	}
}
