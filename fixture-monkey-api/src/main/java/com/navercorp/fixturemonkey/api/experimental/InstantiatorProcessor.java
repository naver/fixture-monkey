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

import com.navercorp.fixturemonkey.api.type.TypeReference;

/**
 * The {@link InstantiatorProcessor} interface represents a component responsible for processing an
 * {@link Instantiator} DSL.
 */
@API(since = "0.6.12", status = Status.EXPERIMENTAL)
public interface InstantiatorProcessor {
	/**
	 * Processes an {@link Instantiator} DSL to resolve a way to instantiate to a given type.
	 *
	 * @param typeReference the type to be instantiated.
	 * @param instantiator a way to creating instances of the specified type.
	 * @return An {@link InstantiatorProcessResult} containing the result of the processing.
	 */
	InstantiatorProcessResult process(TypeReference<?> typeReference, Instantiator instantiator);
}
