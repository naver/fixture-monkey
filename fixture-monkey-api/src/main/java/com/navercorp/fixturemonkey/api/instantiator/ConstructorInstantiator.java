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

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.TypeReference;

/**
 * The {@link ConstructorInstantiator} interface represents a DSL for constructing objects of type T
 * using a constructor. It extends the {@link Instantiator} interface, providing methods to retrieve information
 * about the constructor's parameter types and names.
 *
 * @param <T> The type of objects that can be instantiated using the constructor.
 *
 * @see Instantiator
 */
@API(since = "0.6.12", status = Status.MAINTAINED)
public interface ConstructorInstantiator<T> extends Instantiator {
	/**
	 * Gets a list of types representing the input parameter types of the constructor.
	 *
	 * @return A list of types representing the input parameter types of the constructor.
	 */
	List<TypeReference<?>> getInputParameterTypes();

	/**
	 * Gets a list of strings representing the input parameter names of the constructor.
	 *
	 * @return A list of strings representing the input parameter names of the constructor.
	 */
	List<String> getInputParameterNames();

	@Nullable
	PropertyInstantiator<T> getPropertyInstantiator();
}
