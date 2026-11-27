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

package com.navercorp.objectfarm.api.input;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.Types;

/**
 * Detects whether a value is a container type and extracts its size.
 * <p>
 * This interface abstracts container detection logic, allowing different implementations
 * for different contexts (e.g., standard Java containers vs. custom containers).
 * <p>
 * Example usage:
 * <pre>
 * ContainerDetector detector = ContainerDetector.standard();
 * OptionalInt size = detector.getContainerSize(myList);
 * if (size.isPresent()) {
 *     // It's a container with size.getAsInt() elements
 * }
 * </pre>
 */
@FunctionalInterface
public interface ContainerDetector {

	/**
	 * Returns the size of the container if the value is a container type.
	 *
	 * @param value the value to check
	 * @return the container size if it's a container, empty otherwise
	 */
	OptionalInt getContainerSize(@Nullable Object value);

	/**
	 * Checks if the value is a container type.
	 *
	 * @param value the value to check
	 * @return true if it's a container type
	 */
	default boolean isContainer(@Nullable Object value) {
		return getContainerSize(value).isPresent();
	}

	/**
	 * Returns the size of the resizable container the given value puts at its path.
	 * <p>
	 * A present {@link Optional} shares its path with its content, so the container is its content.
	 * A container whose children do not depend on its value, such as a single-element wrapper or a
	 * {@link Map.Entry}, is not resizable.
	 *
	 * @param value the value set at a path
	 * @return the size of the resizable container at the value's path, empty if there is none
	 */
	default OptionalInt getResizableContainerSizeAtPath(@Nullable Object value) {
		Object valueAtPath = value;
		// The content of an Optional shares the Optional's path, so the size at this path sizes the content.
		// The Optional's own size, always 1, would cut a container inside it down to one element.
		while (valueAtPath instanceof Optional) {
			Optional<?> optional = (Optional<?>)valueAtPath;
			if (!optional.isPresent()) {
				return OptionalInt.empty();
			}
			valueAtPath = optional.get();
		}
		if (valueAtPath == null || Types.hasFixedChildren(valueAtPath.getClass())) {
			return OptionalInt.empty();
		}
		return getContainerSize(valueAtPath);
	}

	/**
	 * Returns a detector that recognizes standard Java containers
	 * (Collection, Map, arrays).
	 *
	 * @return the standard container detector
	 */
	static ContainerDetector standard() {
		return StandardContainerDetector.INSTANCE;
	}

	/**
	 * Combines this detector with another, returning the first successful detection.
	 *
	 * @param other the other detector to combine with
	 * @return a combined detector
	 */
	default ContainerDetector or(ContainerDetector other) {
		return value -> {
			OptionalInt result = this.getContainerSize(value);
			if (result.isPresent()) {
				return result;
			}
			return other.getContainerSize(value);
		};
	}

	/**
	 * Standard container detector for Java built-in container types.
	 */
	final class StandardContainerDetector implements ContainerDetector {
		static final StandardContainerDetector INSTANCE = new StandardContainerDetector();

		private StandardContainerDetector() {
		}

		@Override
		public OptionalInt getContainerSize(@Nullable Object value) {
			if (value == null) {
				return OptionalInt.empty();
			}

			if (value instanceof Collection) {
				return OptionalInt.of(((Collection<?>)value).size());
			}

			if (value instanceof Map) {
				return OptionalInt.of(((Map<?, ?>)value).size());
			}

			if (value.getClass().isArray()) {
				return OptionalInt.of(Array.getLength(value));
			}

			return OptionalInt.empty();
		}
	}
}
