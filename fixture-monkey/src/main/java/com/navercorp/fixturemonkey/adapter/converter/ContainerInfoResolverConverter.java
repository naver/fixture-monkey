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

package com.navercorp.fixturemonkey.adapter.converter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.tree.PathContainerSizeResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Converts ContainerInfoManipulator to TransformPathResolver for container size resolution.
 * <p>
 * This converter analyzes ContainerInfoManipulator instances and creates corresponding
 * TransformPathResolver instances that can be used during tree transformation to determine
 * container sizes at specific paths.
 * <p>
 * The converter:
 * <ul>
 *   <li>Sorts manipulators by manipulatingSequence to ensure correct order of application</li>
 *   <li>Calls {@link ContainerInfoManipulator#fixed()} to determine the container size</li>
 *   <li>Creates ContainerSizeResolver instances with the fixed size</li>
 * </ul>
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
public final class ContainerInfoResolverConverter {
	private ContainerInfoResolverConverter() {
	}

	/**
	 * Converts a list of ContainerInfoManipulators to TransformPathResolvers.
	 * <p>
	 * The manipulators are sorted by manipulatingSequence before conversion.
	 * When multiple manipulators target the same path, only the last one is kept
	 * (the one with the highest manipulatingSequence).
	 *
	 * @param manipulators the list of ContainerInfoManipulators to convert
	 * @return a list of TransformPathResolvers for container size resolution
	 */
	public static List<PathResolver<ContainerSizeResolver>> convert(List<ContainerInfoManipulator> manipulators) {
		// Sort by manipulatingSequence to ensure correct override order
		List<ContainerInfoManipulator> sorted = new ArrayList<>(manipulators);
		sorted.sort(Comparator.comparingInt(ContainerInfoManipulator::getManipulatingSequence));

		// Keep only the last resolver for each path (last one wins)
		Map<String, PathResolver<ContainerSizeResolver>> resolverByPath = new LinkedHashMap<>();

		for (ContainerInfoManipulator manipulator : sorted) {
			String pathExpression = PredicatePathConverter.toExpression(manipulator.getNextNodePredicates());
			PathResolver<ContainerSizeResolver> resolver = convertSingle(manipulator);
			resolverByPath.put(pathExpression, resolver); // Later entries override earlier ones
		}

		return new ArrayList<>(resolverByPath.values());
	}

	/**
	 * Converts a list of ContainerInfoManipulators to TransformPathResolvers,
	 * using pre-computed PathExpression cache to avoid redundant predicate conversions.
	 *
	 * @param manipulators the list of ContainerInfoManipulators to convert
	 * @param pathCache pre-computed PathExpression for each manipulator
	 * @return a list of TransformPathResolvers for container size resolution
	 */
	public static List<PathResolver<ContainerSizeResolver>> convert(
		List<ContainerInfoManipulator> manipulators,
		Map<ContainerInfoManipulator, PathExpression> pathCache
	) {
		List<ContainerInfoManipulator> sorted = new ArrayList<>(manipulators);
		sorted.sort(Comparator.comparingInt(ContainerInfoManipulator::getManipulatingSequence));

		Map<String, PathResolver<ContainerSizeResolver>> resolverByPath = new LinkedHashMap<>();

		for (ContainerInfoManipulator manipulator : sorted) {
			PathExpression cachedPath = pathCache.get(manipulator);
			String pathString =
				cachedPath != null
					? cachedPath.toExpression()
					: PredicatePathConverter.toExpression(manipulator.getNextNodePredicates());
			PathResolver<ContainerSizeResolver> resolver = convertSingle(manipulator, cachedPath);
			resolverByPath.put(pathString, resolver);
		}

		return new ArrayList<>(resolverByPath.values());
	}

	/**
	 * Converts a single ContainerInfoManipulator to a TransformPathResolver.
	 *
	 * @param manipulator the ContainerInfoManipulator to convert
	 * @return a TransformPathResolver for this manipulator
	 */
	public static PathResolver<ContainerSizeResolver> convertSingle(ContainerInfoManipulator manipulator) {
		return convertSingle(manipulator, null);
	}

	private static PathResolver<ContainerSizeResolver> convertSingle(
		ContainerInfoManipulator manipulator,
		@Nullable PathExpression cachedPattern
	) {
		// Create a copy and fix the size
		ContainerInfoManipulator copy = manipulator.copy();
		copy.fixed();

		int fixedSize = copy.getContainerInfo().getElementMinSize();

		PathExpression pattern =
			cachedPattern != null ? cachedPattern : PredicatePathConverter.convert(manipulator.getNextNodePredicates());

		ContainerSizeResolver sizeResolver = containerType -> {
			int requestedSize = fixedSize;

			// For Set<Enum> or Map<Enum, V>, clamp to enum cardinality
			if (containerType != null) {
				Class<?> rawType = containerType.getRawType();
				Class<?> enumType = getEnumElementType(rawType, containerType);
				if (enumType != null) {
					Object[] enumConstants = enumType.getEnumConstants();
					if (enumConstants != null) {
						int enumSize = enumConstants.length;
						requestedSize = Math.min(requestedSize, enumSize);
					}
				}
			}

			return requestedSize;
		};

		return new PathContainerSizeResolver(pattern, sizeResolver);
	}

	/**
	 * Gets the enum element type from a container type if applicable.
	 * For Set, returns the element type if it's an enum.
	 * For Map, returns the key type if it's an enum.
	 *
	 * @param rawType the raw container type
	 * @param containerType the full container type with generics
	 * @return the enum class if found, null otherwise
	 */
	@Nullable
	private static Class<?> getEnumElementType(Class<?> rawType, JvmType containerType) {
		List<? extends JvmType> typeVariables = containerType.getTypeVariables();
		if (typeVariables.isEmpty()) {
			return null;
		}

		// For Set<E>, check if E is an enum
		if (Set.class.isAssignableFrom(rawType)) {
			Class<?> elementType = typeVariables.get(0).getRawType();
			if (elementType.isEnum()) {
				return elementType;
			}
		}

		// For Map<K, V>, check if K is an enum
		if (Map.class.isAssignableFrom(rawType)) {
			Class<?> keyType = typeVariables.get(0).getRawType();
			if (keyType.isEnum()) {
				return keyType;
			}
		}

		return null;
	}
}
