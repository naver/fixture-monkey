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
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.functions
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

@API(since = "1.1.0", status = EXPERIMENTAL)
object KotlinNullabilityUtils {

    fun isNullableConstructorParam(constructor: Constructor<*>, parameterIndex: Int): Boolean {
        val declaringClass = constructor.declaringClass

        if (!KotlinTypeDetector.isKotlinType(declaringClass)) {
            return false
        }

        return try {
            val kotlinClass = declaringClass.cachedKotlin()
            val primaryConstructor = kotlinClass.primaryConstructor ?: return false
            primaryConstructor.isAccessible = true

            val params = primaryConstructor.parameters.filter { it.kind == KParameter.Kind.VALUE }
            if (parameterIndex >= params.size) {
                return false
            }

            params[parameterIndex].type.isMarkedNullable
        } catch (e: Throwable) {
            false
        }
    }

    fun isNullableConstructorParamByName(constructor: Constructor<*>, paramName: String?): Boolean {
        if (paramName == null) {
            return false
        }

        val declaringClass = constructor.declaringClass

        if (!KotlinTypeDetector.isKotlinType(declaringClass)) {
            return false
        }

        return try {
            val kotlinClass = declaringClass.cachedKotlin()
            val primaryConstructor = kotlinClass.primaryConstructor ?: return false
            primaryConstructor.isAccessible = true

            val param = primaryConstructor.parameters
                .filter { it.kind == KParameter.Kind.VALUE }
                .find { it.name == paramName }
                ?: return false

            param.type.isMarkedNullable
        } catch (e: Throwable) {
            false
        }
    }

    fun isNullableByOwnerType(ownerType: Class<*>, propertyName: String?): Boolean? {
        if (propertyName == null) {
            return null
        }

        if (!KotlinTypeDetector.isKotlinType(ownerType)) {
            return null
        }

        if (Function::class.java.isAssignableFrom(ownerType)) {
            return null
        }

        return try {
            val kotlinClass = ownerType.cachedKotlin()

            // Check primary constructor parameters first
            val primaryConstructor = kotlinClass.primaryConstructor
            if (primaryConstructor != null) {
                primaryConstructor.isAccessible = true
                val param = primaryConstructor.parameters
                    .filter { it.kind == KParameter.Kind.VALUE }
                    .find { it.name == propertyName }
                if (param != null) {
                    return param.type.isMarkedNullable
                }
            }

            // Check secondary constructors
            for (constructor in kotlinClass.constructors) {
                if (constructor == primaryConstructor) continue
                val param = constructor.parameters
                    .filter { it.kind == KParameter.Kind.VALUE }
                    .find { it.name == propertyName }
                if (param != null) {
                    return param.type.isMarkedNullable
                }
            }

            // Check companion object functions (for factory method patterns)
            val companion = kotlinClass.companionObject
            if (companion != null) {
                for (func in companion.functions) {
                    val param = func.parameters
                        .filter { it.kind == KParameter.Kind.VALUE }
                        .find { it.name == propertyName }
                    if (param != null) {
                        return param.type.isMarkedNullable
                    }
                }
            }

            // Fallback to member properties
            val member = kotlinClass.members.find { it.name == propertyName }
            member?.returnType?.isMarkedNullable
        } catch (e: Throwable) {
            null
        }
    }

    fun isNullableField(field: Field): Boolean {
        val declaringClass = field.declaringClass
        val fieldName = field.name

        if (!KotlinTypeDetector.isKotlinType(declaringClass)) {
            return false
        }

        if (Function::class.java.isAssignableFrom(declaringClass)) {
            return false
        }

        return try {
            val kotlinClass = declaringClass.cachedKotlin()
            val property = kotlinClass.members.find { it.name == fieldName }
            property?.returnType?.isMarkedNullable ?: false
        } catch (e: Throwable) {
            false
        }
    }
}
