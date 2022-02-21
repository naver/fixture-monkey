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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryPropertyContainerInfo {
	private final Integer elementMinSize;
	private final Integer elementMaxSize;
	private final Integer elementIndex;
	private final List<Type> elementTypes;

	public ArbitraryPropertyContainerInfo(
		Integer elementMinSize,
		Integer elementMaxSize,
		Integer elementIndex,
		List<Type> elementTypes
	) {
		this.elementMinSize = elementMinSize;
		this.elementMaxSize = elementMaxSize;
		this.elementIndex = elementIndex;
		this.elementTypes = new ArrayList<>(elementTypes);
	}

	public Integer getElementMinSize() {
		return this.elementMinSize;
	}

	public Integer getElementMaxSize() {
		return this.elementMaxSize;
	}

	public Integer getElementIndex() {
		return this.elementIndex;
	}

	public List<Type> getElementTypes() {
		return Collections.unmodifiableList(this.elementTypes);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		ArbitraryPropertyContainerInfo that = (ArbitraryPropertyContainerInfo)obj;
		return Objects.equals(elementMinSize, that.elementMinSize)
			&& Objects.equals(elementMaxSize, that.elementMaxSize)
			&& Objects.equals(elementIndex, that.elementIndex)
			&& Objects.equals(elementTypes, that.elementTypes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(elementMinSize, elementMaxSize, elementIndex, elementTypes);
	}

	@Override
	public String toString() {
		return "ArbitraryPropertyContainerInfo{"
			+ "elementMinSize=" + elementMinSize
			+ ", elementMaxSize=" + elementMaxSize
			+ ", elementIndex=" + elementIndex
			+ ", elementTypes=" + elementTypes
			+ '}';
	}
}
