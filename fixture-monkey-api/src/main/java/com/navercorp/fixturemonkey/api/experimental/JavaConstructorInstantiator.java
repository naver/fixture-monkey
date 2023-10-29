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

package com.navercorp.fixturemonkey.api.experimental;

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.TypeReference;

@API(since = "0.6.12", status = Status.EXPERIMENTAL)
public final class JavaConstructorInstantiator<T> implements ConstructorInstantiator<T> {
	private final List<TypeReference<?>> inputTypes;
	private final List<String> inputParameterNames;

	public JavaConstructorInstantiator() {
		this.inputTypes = new ArrayList<>();
		this.inputParameterNames = new ArrayList<>();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public JavaConstructorInstantiator<T> parameter(Class<?> type) {
		this.inputTypes.add(new TypeReference(type) {
		});
		this.inputParameterNames.add(null);
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public JavaConstructorInstantiator<T> parameter(Class<?> type, String parameterName) {
		this.inputTypes.add(new TypeReference(type) {
		});
		this.inputParameterNames.add(parameterName);
		return this;
	}

	public JavaConstructorInstantiator<T> parameter(TypeReference<?> type) {
		this.inputTypes.add(type);
		this.inputParameterNames.add(null);
		return this;
	}

	public JavaConstructorInstantiator<T> parameter(TypeReference<?> type, String parameterName) {
		this.inputTypes.add(type);
		this.inputParameterNames.add(parameterName);
		return this;
	}

	public List<TypeReference<?>> getInputParameterTypes() {
		return inputTypes;
	}

	public List<String> getInputParameterNames() {
		return inputParameterNames;
	}
}
