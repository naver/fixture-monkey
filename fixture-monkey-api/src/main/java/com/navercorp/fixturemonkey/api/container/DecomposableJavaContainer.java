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

package com.navercorp.fixturemonkey.api.container;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It is used for decomposing the custom container.
 */
@API(since = "0.6.0", status = Status.EXPERIMENTAL)
public class DecomposableJavaContainer {
	/**
	 * It should be one of these Java Container type.
	 * For example, the implementations of {@link Iterable}, {@link java.util.List}, {@link java.util.Set}, etc.
	 * {@link Iterable}, {@link java.util.Map}, {@link java.util.Map.Entry}, {@link java.util.Optional}, Array as well.
	 */
	private final Object javaContainer;
	/**
	 * It is a size of {@link #javaContainer}.
	 */
	private final int size;

	public DecomposableJavaContainer(Object javaContainer, int size) {
		this.javaContainer = javaContainer;
		this.size = size;
	}

	public Object getJavaContainer() {
		return javaContainer;
	}

	public int getSize() {
		return size;
	}
}
