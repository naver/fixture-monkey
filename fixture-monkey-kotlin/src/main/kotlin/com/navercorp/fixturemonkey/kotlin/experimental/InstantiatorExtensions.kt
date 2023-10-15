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

package com.navercorp.fixturemonkey.kotlin.experimental

import com.navercorp.fixturemonkey.api.type.TypeReference
import com.navercorp.fixturemonkey.experimental.ConstructorInstantiator
import com.navercorp.fixturemonkey.experimental.Instantiator

inline fun <reified T> ConstructorInstantiator.parameter(): ConstructorInstantiator =
    this.parameter(object : TypeReference<T>() {})

inline fun <reified T> ConstructorInstantiator.parameter(parameterName: String?): ConstructorInstantiator =
    this.parameter(object : TypeReference<T>() {}, parameterName)

fun constructor(dsl: ConstructorInstantiator.() -> ConstructorInstantiator): ConstructorInstantiator =
    dsl(Instantiator.constructor())

inline fun <reified T1> constructor(name1: String? = null): ConstructorInstantiator =
    Instantiator.constructor()
        .parameter<T1>(name1)

inline fun <reified T1, reified T2> constructor(name1: String? = null, name2: String? = null): ConstructorInstantiator =
    Instantiator.constructor()
        .parameter<T1>(name1)
        .parameter<T2>(name2)

inline fun <reified T1, reified T2, reified T3> constructor(
    name1: String? = null,
    name2: String? = null,
    name3: String? = null
): ConstructorInstantiator =
    Instantiator.constructor()
        .parameter<T1>(name1)
        .parameter<T2>(name2)
        .parameter<T3>(name3)

inline fun <reified T1, reified T2, reified T3, reified T4> constructor(
    name1: String? = null,
    name2: String? = null,
    name3: String? = null,
    name4: String? = null,
): ConstructorInstantiator =
    Instantiator.constructor()
        .parameter<T1>(name1)
        .parameter<T2>(name2)
        .parameter<T3>(name3)
        .parameter<T4>(name4)

inline fun <reified T1, reified T2, reified T3, reified T4, reified T5> constructor(
    name1: String? = null,
    name2: String? = null,
    name3: String? = null,
    name4: String? = null,
    name5: String? = null,
): ConstructorInstantiator =
    Instantiator.constructor()
        .parameter<T1>(name1)
        .parameter<T2>(name2)
        .parameter<T3>(name3)
        .parameter<T4>(name4)
        .parameter<T5>(name5)
