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

import com.navercorp.fixturemonkey.api.type.KotlinTypeDetector
import com.navercorp.fixturemonkey.api.type.TypeReference
import com.navercorp.fixturemonkey.api.type.Types
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

/**
 * Provides a collection of extension functions for Type,
 * facilitating the conversion and handling of types in Kotlin reflection.
 *
 * These utilities enhance the interoperability between Kotlin's type system and Java's
 *  type system, particularly focusing on [TypeReference].
 */
fun Type.actualType(): Class<*> = Types.getActualType(this)

fun <T> Class<T>.toAnnotatedType(): AnnotatedType = Types.generateAnnotatedTypeWithoutAnnotation(this)

fun Type.toAnnotatedType(): AnnotatedType = Types.generateAnnotatedTypeWithoutAnnotation(this)

fun <T> Class<T>.toTypeReference(): TypeReference<T> = object : TypeReference<T>() {
    override fun getType(): Type {
        return this@toTypeReference
    }

    override fun getAnnotatedType(): AnnotatedType {
        return Types.generateAnnotatedTypeWithoutAnnotation(this@toTypeReference)
    }
}

fun Type.toTypeReference(): TypeReference<*> = object : TypeReference<Any?>() {
    override fun getType(): Type {
        return this@toTypeReference
    }

    override fun getAnnotatedType(): AnnotatedType {
        return Types.generateAnnotatedTypeWithoutAnnotation(this@toTypeReference)
    }
}

fun KType.toTypeReference(): TypeReference<*> = object : TypeReference<Any?>() {
    override fun getType(): Type {
        return this@toTypeReference.javaType
    }

    override fun getAnnotatedType(): AnnotatedType {
        try {
            if (this@toTypeReference.classifier != null && this@toTypeReference.jvmErasure.isValue) { // for Kotlin value class
                return this@toTypeReference.classifier!!.createType().javaType.toAnnotatedType()
            }
        } catch (ex: KotlinReflectionInternalError) {
            // ignored
        }

        return this@toTypeReference.javaType.toAnnotatedType()
    }
}

fun Class<*>.isKotlinType(): Boolean = KotlinTypeDetector.isKotlinType(this)
