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

/**
 * The {@link PropertyInstantiator} interface represents a DSL for constructing objects of type T
 * using a no-args constructor and properties. It extends the {@link Instantiator} interface.
 *
 * @param <T> The type of objects that can be instantiated using the no-args constructor and properties.
 * @see Instantiator
 */
public interface PropertyInstantiator<T> extends Instantiator {
}
