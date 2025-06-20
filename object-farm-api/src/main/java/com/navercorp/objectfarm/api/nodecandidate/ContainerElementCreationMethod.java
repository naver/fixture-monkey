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

package com.navercorp.objectfarm.api.nodecandidate;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Element is accessed via container index (List[i], Map entry, Array[i]).
 * <p>
 * This creation method is used for elements within container types like:
 * <ul>
 *   <li>Arrays - {@code array[index]}</li>
 *   <li>Lists - {@code list.get(index)} / {@code list.set(index, value)}</li>
 *   <li>Sets - indexed iteration</li>
 *   <li>Maps - entry at index</li>
 * </ul>
 * <p>
 * Example:
 * <pre>{@code
 * CreationMethod method = new ContainerElementCreationMethod(0);
 * int index = ((ContainerElementCreationMethod) method).getIndex();
 * list.set(index, value);
 * }</pre>
 */
public final class ContainerElementCreationMethod implements CreationMethod {
	private final int index;

	/**
	 * Creates a new ContainerElementCreationMethod.
	 *
	 * @param index the index of the element within the container
	 */
	public ContainerElementCreationMethod(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("Index must be non-negative");
		}
		this.index = index;
	}

	@Override
	public CreationMethodType getType() {
		return CreationMethodType.CONTAINER_ELEMENT;
	}

	/**
	 * Returns the index of the element within the container.
	 *
	 * @return the index (0-based)
	 */
	public int getIndex() {
		return index;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ContainerElementCreationMethod that = (ContainerElementCreationMethod)obj;
		return index == that.index;
	}

	@Override
	public int hashCode() {
		return Objects.hash(index);
	}

	@Override
	public String toString() {
		return "ContainerElementCreationMethod{index=" + index + "}";
	}
}
