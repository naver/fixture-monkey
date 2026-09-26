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

package com.navercorp.fixturemonkey.decompose;

import java.util.OptionalInt;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.container.DecomposableJavaContainer;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.objectfarm.api.input.ContainerDetector;

/**
 * Tells container values apart and sizes them the way the options decompose containers, so a value expands the
 * same way during planning and during assembly.
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public final class DecomposedContainerDetector implements ContainerDetector {
	private final DecomposedContainerValueFactory factory;

	public DecomposedContainerDetector(DecomposedContainerValueFactory factory) {
		this.factory = factory;
	}

	@Override
	public OptionalInt getContainerSize(@Nullable Object value) {
		if (value == null) {
			return OptionalInt.empty();
		}
		try {
			DecomposableJavaContainer decomposed = factory.from(value);
			if (decomposed != null) {
				return OptionalInt.of(decomposed.getSize());
			}
			return OptionalInt.empty();
		} catch (IllegalArgumentException e) {
			return OptionalInt.empty();
		}
	}
}
