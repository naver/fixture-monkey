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

package com.navercorp.fixturemonkey.api.property;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.TypeReference;

@API(since = "0.6.12", status = Status.MAINTAINED)
public final class ConstructorPropertyGeneratorContext {
	private final Property property;
	private final Constructor<?> constructor;
	private final List<TypeReference<?>> inputParameterTypes;
	private final List<@Nullable String> inputParameterNames;

	public ConstructorPropertyGeneratorContext(
		Property property,
		Constructor<?> constructor
	) {
		this(property, constructor, Collections.emptyList(), Collections.emptyList());
	}

	public ConstructorPropertyGeneratorContext(
		Property property,
		Constructor<?> constructor,
		List<TypeReference<?>> inputParameterTypes,
		List<@Nullable String> inputParameterNames
	) {
		this.property = property;
		this.constructor = constructor;
		this.inputParameterTypes = inputParameterTypes;
		this.inputParameterNames = inputParameterNames;
	}

	public Property getProperty() {
		return property;
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	public List<TypeReference<?>> getInputParameterTypes() {
		return inputParameterTypes;
	}

	public List<@Nullable String> getInputParameterNames() {
		return inputParameterNames;
	}
}
