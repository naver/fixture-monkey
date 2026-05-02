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

package com.navercorp.objectfarm.api.node;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;

import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A {@link JvmContainerNodeGenerator} implementation for generating element nodes
 * for single-element wrapper containers such as Supplier and Optional.
 * <p>
 * These types are treated as "transparent" wrappers, meaning:
 * <ul>
 *   <li>The inner element has no index and no name</li>
 *   <li>Path expressions go directly to the inner type's fields</li>
 *   <li>For {@code Supplier<SimpleObject>}, {@code $.str} accesses the inner SimpleObject's str field</li>
 * </ul>
 */
public final class JavaSingleElementContainerNodeGenerator implements JvmContainerNodeGenerator {

	/**
	 * Creates a new generator for single-element container types.
	 */
	public JavaSingleElementContainerNodeGenerator() {
	}

	@Override
	public boolean isSupported(JvmType containerType) {
		Class<?> rawType = containerType.getRawType();
		return (Supplier.class.isAssignableFrom(rawType)
			|| Function.class.isAssignableFrom(rawType)
			|| Optional.class.isAssignableFrom(rawType)
			|| OptionalInt.class.isAssignableFrom(rawType)
			|| OptionalLong.class.isAssignableFrom(rawType)
			|| OptionalDouble.class.isAssignableFrom(rawType));
	}

	@Override
	public List<JvmNode> generateContainerElements(JvmNode containerNode, JvmNodeContext context) {
		return generateContainerElements(containerNode, context, context.getContainerSizeResolver());
	}

	@Override
	public List<JvmNode> generateContainerElements(
		JvmNode containerNode,
		JvmNodeContext context,
		ContainerSizeResolver sizeResolver
	) {
		JvmType containerType = containerNode.getConcreteType();
		Class<?> rawType = containerType.getRawType();

		JvmType elementType = getElementType(containerType, rawType);

		// Single-element wrappers generate exactly one child with null name and null index
		// This makes them "transparent" in path expressions
		JvmNode elementNode = new JavaNode(elementType, null, null);
		return Collections.singletonList(elementNode);
	}

	/**
	 * Gets the element type for the single-element container.
	 *
	 * @param containerType the container type
	 * @param rawType the raw type of the container
	 * @return the element type
	 */
	private JvmType getElementType(JvmType containerType, Class<?> rawType) {
		// OptionalInt, OptionalLong, OptionalDouble have fixed element types
		if (OptionalInt.class.isAssignableFrom(rawType)) {
			return new JavaType(Integer.class);
		}
		if (OptionalLong.class.isAssignableFrom(rawType)) {
			return new JavaType(Long.class);
		}
		if (OptionalDouble.class.isAssignableFrom(rawType)) {
			return new JavaType(Double.class);
		}

		// Supplier<T> and Optional<T> - extract T from type variables
		List<? extends JvmType> typeVariables = containerType.getTypeVariables();
		if (!typeVariables.isEmpty()) {
			return typeVariables.get(
				typeVariables.size() - 1); // Last type variable (for Supplier, it's the return type)
		}

		// Fallback to Object if no type variable is present
		return new JavaType(Object.class);
	}
}
