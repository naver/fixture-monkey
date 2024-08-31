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

package com.navercorp.fixturemonkey.kotlin.type

import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

private val CONSTRUCTORS = ConcurrentLruCache<KClass<*>, Collection<KFunction<*>>>(2048)
private val KOTLIN_TYPES = ConcurrentLruCache<Class<*>, KClass<*>>(2048)
private val MEMBER_FUNCTIONS = ConcurrentLruCache<KClass<*>, Collection<KFunction<*>>>(2048)
private val CONSTRUCTOR_CACHE = ConcurrentLruCache<Class<*>, KFunction<*>>(2048)

fun Class<*>.declaredKotlinConstructors(): Collection<KFunction<*>> =
    CONSTRUCTORS.computeIfAbsent(this.cachedKotlin()) { it.constructors }

fun Class<*>.cachedKotlin(): KClass<*> = KOTLIN_TYPES.computeIfAbsent(this) { it.kotlin }

fun Class<*>.kotlinMemberFunctions(): Collection<KFunction<*>> =
    MEMBER_FUNCTIONS.computeIfAbsent(this.cachedKotlin()) { cachedKotlin().memberFunctions }

fun KClass<*>.cachedMemberFunctions(): Collection<KFunction<*>> =
    MEMBER_FUNCTIONS.computeIfAbsent(this) { this.memberFunctions }

fun KClass<*>.isKotlinLambda(): Boolean = this.isSubclassOf(Function::class)

internal fun Class<*>.kotlinPrimaryConstructor(): KFunction<*> =
    CONSTRUCTOR_CACHE.computeIfAbsent(this) {
        val kotlinClass = Reflection.createKotlinClass(this) as KClass<*>
        requireNotNull(kotlinClass.primaryConstructor) { "No kotlin primary constructor provided for $kotlinClass" }
    }.apply { isAccessible = true }
