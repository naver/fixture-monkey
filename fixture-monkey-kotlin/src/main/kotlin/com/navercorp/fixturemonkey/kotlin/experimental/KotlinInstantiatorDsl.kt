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

import com.navercorp.fixturemonkey.api.experimental.Instantiator
import com.navercorp.fixturemonkey.api.type.TypeReference
import com.navercorp.fixturemonkey.experimental.ExperimentalArbitraryBuilder

@DslMarker
annotation class InstantiatorDsl

@InstantiatorDsl
class InstantiatorDslSpec<T>(
    val rootTypeReference: TypeReference<T>,
) {
    val instantiators = mutableMapOf<TypeReference<*>, Instantiator>()

    @JvmName("rootConstructor")
    fun constructor(): InstantiatorDslSpec<T> {
        instantiators[rootTypeReference] = KotlinConstructorInstantiator<T>()
        return this
    }

    @JvmName("rootConstructor")
    fun constructor(dsl: KotlinConstructorInstantiator<T>.() -> KotlinConstructorInstantiator<T>): InstantiatorDslSpec<T> {
        dsl(KotlinConstructorInstantiator())
            .also {
                instantiators[rootTypeReference] = it
            }
        return this
    }

    inline fun <reified U> constructor(): InstantiatorDslSpec<T> {
        instantiators[object : TypeReference<U>() {}] = KotlinConstructorInstantiator<U>()
        return this
    }

    inline fun <reified U> constructor(dsl: KotlinConstructorInstantiator<U>.() -> KotlinConstructorInstantiator<U>): InstantiatorDslSpec<T> {
        dsl(KotlinConstructorInstantiator())
            .also {
                instantiators[object : TypeReference<U>() {}] = it
            }
        return this
    }

    inline fun <reified U> factory(dsl: KotlinFactoryMethodInstantiator<U>.() -> KotlinFactoryMethodInstantiator<U>): InstantiatorDslSpec<T> {
        dsl(KotlinFactoryMethodInstantiator())
            .also {
                instantiators[object : TypeReference<U>() {}] = it
            }
        return this
    }
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
