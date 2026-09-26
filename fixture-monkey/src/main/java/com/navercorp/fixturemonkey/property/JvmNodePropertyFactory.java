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

package com.navercorp.fixturemonkey.property;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
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
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.nodecandidate.ConstructorParamCreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.CreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.FieldAccessCreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.MethodInvocationCreationMethod;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

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
	private final boolean declaredTypes;

	public JvmNodePropertyFactory(Function<JvmNode, @Nullable JvmNode> parentLookup) {
		this(parentLookup, false);
	}

	private JvmNodePropertyFactory(Function<JvmNode, @Nullable JvmNode> parentLookup, boolean declaredTypes) {
		this.parentLookup = parentLookup;
		this.declaredTypes = declaredTypes;
	}

	/**
	 * Creates a factory whose properties have the types nodes are declared with rather than the implementations
	 * chosen for them, for matching what a node is: a registered matcher or a scope sees a field declared
	 * {@code List<String>} as a list even when an {@code ArrayList} is built for it.
	 *
	 * @param parentLookup the parent of a node
	 * @return the factory
	 */
	public static JvmNodePropertyFactory forMatching(Function<JvmNode, @Nullable JvmNode> parentLookup) {
		return new JvmNodePropertyFactory(parentLookup, true);
	}

	/**
	 * Creates a factory for matching the nodes of a chain, where each node's parent is the node before it. Used
	 * while a tree is being built, when only the chain above a node is known.
	 *
	 * @param chain the nodes from the outermost ancestor down to the node, the parent right before each node
	 * @return the factory
	 * @see #forMatching(Function)
	 */
	public static JvmNodePropertyFactory ofChain(List<JvmNode> chain) {
		return forMatching(node -> {
			for (int i = chain.size() - 1; i > 0; i--) {
				if (chain.get(i) == node) {
					return chain.get(i - 1);
				}
			}
			return null;
		});
	}

	private JvmType typeOf(JvmNode node) {
		return declaredTypes ? node.getDeclaredType() : node.getConcreteType();
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
		return new TypeParameterProperty(jvmType);
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
		JvmType jvmType = typeOf(node);
		CreationMethod creationMethod = node.getCreationMethod();
		Boolean nullable = jvmType.getNullable();

		if (creationMethod == null) {
			return new TypeNameProperty(jvmType, node.getNodeName(), nullable);
		}

		switch (creationMethod.getType()) {
			case FIELD:
				return new FieldProperty(jvmType, ((FieldAccessCreationMethod)creationMethod).getField(), nullable);
			case CONSTRUCTOR:
				ConstructorParamCreationMethod ctorMethod = (ConstructorParamCreationMethod)creationMethod;
				Constructor<?> ctor = ctorMethod.getConstructor();

				// Check if the actual constructor parameter is primitive.
				// TypeReference<Int> boxes int to Integer, losing primitive info and
				// causing incorrect null injection for primitive parameters.
				// Match by name since parameterIndex in ConstructorParamCreationMethod
				// may not correspond to the actual constructor parameter position.
				Class<?> actualParamType = findConstructorParamType(ctor, node.getNodeName());
				JvmType paramJvmType = jvmType;
				if (actualParamType != null && actualParamType.isPrimitive()) {
					paramJvmType = new ReflectiveJvmType(
						actualParamType,
						Collections.emptyList(),
						Collections.emptyList(),
						false
					);
					nullable = false;
				}

				Property paramProperty = new TypeNameProperty(paramJvmType, node.getNodeName(), nullable);
				Property fieldProperty = findFieldProperty(ctor.getDeclaringClass(), node.getNodeName());
				return new ConstructorProperty(paramProperty, ctor, fieldProperty, nullable);
			case METHOD:
				return fromMethodCreation(
					jvmType,
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
					Property containerProperty = fromType(typeOf(annotationSource));
					Property elementProperty = new TypeNameProperty(jvmType, node.getNodeName(), null);
					return new DefaultContainerElementProperty(
						containerProperty,
						elementProperty,
						node.getIndex(),
						node.getIndex() != null ? node.getIndex() : 0
					);
				}
				return new TypeNameProperty(jvmType, node.getNodeName(), null);
			default:
				return new TypeNameProperty(jvmType, node.getNodeName(), null);
		}
	}

	private Property fromMethodCreation(
		JvmType jvmType,
		String name,
		MethodInvocationCreationMethod method
	) {
		Method invokedMethod = method.getMethod();
		Class<?> declaringClass = invokedMethod.getDeclaringClass();

		// For interface methods, return InterfaceJavaMethodProperty (implements MethodProperty)
		// so that AnonymousArbitraryIntrospector can recognize them
		if (declaringClass.isInterface()) {
			return new InterfaceJavaMethodProperty(
				jvmType,
				name,
				invokedMethod.getName(),
				Arrays.asList(invokedMethod.getAnnotations())
			);
		}

		PropertyDescriptor descriptor = TypeCache.getPropertyDescriptorsByPropertyName(declaringClass).get(name);
		if (descriptor != null) {
			return new PropertyDescriptorProperty(jvmType, descriptor);
		}
		return new TypeNameProperty(jvmType, name, null);
	}

	private static @Nullable Class<?> findConstructorParamType(
		Constructor<?> constructor,
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
		Field field = TypeCache.getFieldsByName(declaringClass).get(fieldName);
		if (field != null) {
			return new FieldProperty(field);
		}
		return null;
	}

}
