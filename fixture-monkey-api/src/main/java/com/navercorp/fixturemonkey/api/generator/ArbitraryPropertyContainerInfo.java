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

import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryPropertyContainerInfo {
	private final Integer elementMinSize;
	private final Integer elementMaxSize;
	private final List<AnnotatedType> elementTypes;

	public ArbitraryPropertyContainerInfo(
		Integer elementMinSize,
		Integer elementMaxSize,
		List<AnnotatedType> elementTypes
	) {
		this.elementMinSize = elementMinSize;
		this.elementMaxSize = elementMaxSize;
		this.elementTypes = new ArrayList<>(elementTypes);
	}

	public Integer getElementMinSize() {
		return this.elementMinSize;
	}

	public Integer getElementMaxSize() {
		return this.elementMaxSize;
	}

	public List<AnnotatedType> getElementTypes() {
		return Collections.unmodifiableList(this.elementTypes);
	}

	@Override
	public String toString() {
		return "ArbitraryPropertyContainerInfo{"
			+ "elementMinSize=" + elementMinSize
			+ ", elementMaxSize=" + elementMaxSize
			+ ", elementTypes=" + elementTypes
			+ '}';
	}
}
