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

package com.navercorp.fixturemonkey.api.expression;

import java.io.Serializable;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It represents a functional interface for referencing getter methods in Java.
 *
 * @param <T> The type of the input parameter (typically the object containing the property).
 * @param <R> The return type of the getter method (the type of the property being retrieved).
 */
@API(since = "1.0.0", status = Status.EXPERIMENTAL)
public interface JavaGetterMethodReference<T, R> extends Function<T, R>, Serializable {
}
