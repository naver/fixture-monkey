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

package com.navercorp.fixturemonkey.api.type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Optional;

public abstract class Constructors {
	public static Optional<Constructor<?>> findPrimaryConstructor(Class<?> type, Constructor<?>[] constructors) {
		if (!type.isRecord()) {
			return Arrays.stream(constructors)
				.findFirst();
		}

		Optional<Constructor<?>> primaryConstructor = Arrays.stream(constructors)
			.filter(it -> isCanonicalConstructor(type, it))
			.findFirst();

		if (primaryConstructor.isEmpty()) {
			throw new IllegalArgumentException("Given record has no canonical constructor. type: " + type);
		}

		return primaryConstructor;
	}

	private static boolean isCanonicalConstructor(Class<?> type, Constructor<?> constructor) {
		Class<?>[] parameterTypes = Arrays.stream(constructor.getParameters())
			.map(Parameter::getType)
			.toArray(Class[]::new);
		Class<?>[] recordComponentTypes = Arrays.stream(type.getRecordComponents())
			.map(RecordComponent::getType)
			.toArray(Class[]::new);

		return Types.isAssignableTypes(parameterTypes, recordComponentTypes)
			&& Types.isAssignableTypes(recordComponentTypes, parameterTypes);
	}
}
