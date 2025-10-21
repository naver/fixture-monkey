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

package com.navercorp.fixturemonkey.api.instantiator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.TypeReference;

@API(since = "0.6.12", status = Status.MAINTAINED)
public final class FactoryMethodInstantiatorJava<T> implements FactoryMethodInstantiator<T> {
	private final String factoryMethodName;
	private final List<TypeReference<?>> types;
	private final List<String> parameterNames;
	@Nullable
	private PropertyInstantiator<T> propertyInstantiator = null;

	public FactoryMethodInstantiatorJava(String factoryMethodName) {
		this.factoryMethodName = factoryMethodName;
		this.types = new ArrayList<>();
		this.parameterNames = new ArrayList<>();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public FactoryMethodInstantiatorJava<T> parameter(Class<?> type) {
		this.types.add(new TypeReference(type) {
		});
		this.parameterNames.add(null);
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public FactoryMethodInstantiatorJava<T> parameter(Class<?> type, String parameterName) {
		this.types.add(new TypeReference(type) {
		});
		this.parameterNames.add(parameterName);
		return this;
	}

	public FactoryMethodInstantiatorJava<T> parameter(TypeReference<?> type) {
		this.types.add(type);
		this.parameterNames.add(null);
		return this;
	}

	public FactoryMethodInstantiatorJava<T> parameter(TypeReference<?> type, String parameterName) {
		this.types.add(type);
		this.parameterNames.add(parameterName);
		return this;
	}

	public FactoryMethodInstantiator<T> field() {
		this.propertyInstantiator = new JavaFieldPropertyInstantiator<>();
		return this;
	}


	public FactoryMethodInstantiator<T> field(Consumer<JavaFieldPropertyInstantiator<T>> consumer) {
		this.propertyInstantiator = new JavaFieldPropertyInstantiator<>();
		consumer.accept((JavaFieldPropertyInstantiator<T>)this.propertyInstantiator);
		return this;
	}

	public FactoryMethodInstantiator<T> javaBeansProperty() {
		this.propertyInstantiator = new JavaBeansPropertyInstantiator<>();
		return this;
	}

	public FactoryMethodInstantiator<T> javaBeansProperty(Consumer<JavaBeansPropertyInstantiator<T>> consumer) {
		this.propertyInstantiator = new JavaBeansPropertyInstantiator<>();
		consumer.accept((JavaBeansPropertyInstantiator<T>)this.propertyInstantiator);
		return this;
	}

	@Override
	public String getFactoryMethodName() {
		return factoryMethodName;
	}

	public List<TypeReference<?>> getInputParameterTypes() {
		return types;
	}

	public List<String> getInputParameterNames() {
		return parameterNames;
	}

	@Nullable
	@Override
	public PropertyInstantiator<T> getPropertyInstantiator() {
		return propertyInstantiator;
	}
}
