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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * provides a DSL for a way of specifying the instantiation of types.
 */
@API(since = "0.6.12", status = Status.EXPERIMENTAL)
public interface Instantiator {
	/**
	 * Creates a DSL object for specifying a given type of constructor.
	 *
	 * @param <T> a type to instantiate.
	 * @return an {@link ConstructorInstantiator} DSL object.
	 */
	static <T> JavaConstructorInstantiator<T> constructor() {
		return new JavaConstructorInstantiator<>();
	}

	/**
	 * Creates a DSL object for specifying a given type of factory method for instantiating objects of type T.
	 *
	 * @param <T> The type to instantiate.
	 * @return A {@link FactoryMethodInstantiatorJava} DSL object for configuring factory method instantiation.
	 */
	static <T> FactoryMethodInstantiatorJava<T> factoryMethod() {
		return new FactoryMethodInstantiatorJava<>();
	}

	/**
	 * Creates a DSL object for specifying a given type which creating an object by field.
	 *
	 * @param <T> a type to instantiate.
	 * @return an {@link JavaFieldPropertyInstantiator} DSL object.
	 */
	static <T> JavaFieldPropertyInstantiator<T> field() {
		return new JavaFieldPropertyInstantiator<>();
	}

	/**
	 * Creates a DSL object for specifying a given type which creating an object by Java Beans Property.
	 *
	 * @param <T> a type to instantiate.
	 * @return an {@link JavaBeansPropertyInstantiator} DSL object.
	 */
	static <T> JavaBeansPropertyInstantiator<T> javaBeansProperty() {
		return new JavaBeansPropertyInstantiator<>();
	}
}
