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
import com.navercorp.fixturemonkey.experimental.ExperimentalArbitraryBuilder
import com.navercorp.fixturemonkey.experimental.Instantiator

@DslMarker
annotation class InstantiatorDsl

@InstantiatorDsl
class InstantiatorDslSpec<T>(
    val rootTypeReference: TypeReference<T>,
) {
    val instantiators = mutableMapOf<TypeReference<*>, Instantiator>()

    @JvmName("rootConstructor")
    inline fun constructor(dsl: ConstructorInstantiatorKt<T>.() -> ConstructorInstantiatorKt<T>): ConstructorInstantiatorKt<T> =
        dsl(ConstructorInstantiatorKt())
            .also {
                instantiators[rootTypeReference] = it
            }

    inline fun <reified U> constructor(dsl: ConstructorInstantiatorKt<U>.() -> ConstructorInstantiatorKt<U>): ConstructorInstantiatorKt<U> =
        dsl(ConstructorInstantiatorKt())
            .also {
                instantiators[object : TypeReference<U>() {}] = it
            }
}

class ConstructorInstantiatorKt<T> : ConstructorInstantiator<T> {
    val _types: MutableList<TypeReference<*>> = ArrayList()
    val _parameterNames: MutableList<String?> = ArrayList()

    inline fun <reified U> parameter(parameterName: String? = null): ConstructorInstantiatorKt<T> =
        this.apply {
            _types.add(object : TypeReference<U>() {})
            _parameterNames.add(parameterName)
        }

    override fun getTypes(): List<TypeReference<*>> = _types
    override fun getParameterNames(): List<String?> = _parameterNames
}

inline fun <reified T> ExperimentalArbitraryBuilder<T>.instantiateBy(
    instantiatorDsl: InstantiatorDslSpec<T>.() -> Unit,
): ExperimentalArbitraryBuilder<T> {
    val spec = InstantiatorDslSpec(object : TypeReference<T>() {}).apply(instantiatorDsl)
    spec.instantiators.forEach { (type, instantiator) ->
        this.instantiate(type, instantiator)
    }
    return this
}
