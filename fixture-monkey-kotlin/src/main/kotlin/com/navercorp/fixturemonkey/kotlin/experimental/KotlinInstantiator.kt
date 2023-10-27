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

@file:Suppress("PropertyName")

package com.navercorp.fixturemonkey.kotlin.experimental

import com.navercorp.fixturemonkey.api.type.TypeReference
import com.navercorp.fixturemonkey.experimental.ConstructorInstantiator
import com.navercorp.fixturemonkey.experimental.InitializeArbitraryBuilder
import com.navercorp.fixturemonkey.experimental.Instantiator

class ConstructorInstantiatorKotlin<T>(
    val _types: MutableList<TypeReference<*>> = ArrayList(),
    val _parameterNames: MutableList<String?> = ArrayList(),
) : ConstructorInstantiator<T> {
    inline fun <reified U> parameter(parameterName: String? = null): ConstructorInstantiatorKotlin<T> {
        val type = object : TypeReference<U>() {}
        this._types.add(type)
        this._parameterNames.add(parameterName)
        return this
    }

    override fun getTypes(): List<TypeReference<*>> = _types
    override fun getParameterNames(): List<String?> = _parameterNames
}

inline fun <T, reified U> InitializeArbitraryBuilder<T>.instantiateBy(
    instantiator: () -> Instantiator<U>
) = this.instantiate(object : TypeReference<U>() {}, instantiator())

fun <T> constructor(dsl: ConstructorInstantiatorKotlin<T>.() -> ConstructorInstantiatorKotlin<T>): ConstructorInstantiatorKotlin<T> =
    dsl(ConstructorInstantiatorKotlin())
