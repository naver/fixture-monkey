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

import com.navercorp.objectfarm.api.type.JvmType
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

/**
 * JvmType implementation for Kotlin types with additional Kotlin-specific metadata.
 *
 * This class extends the basic JvmType interface to include:
 * - Nullability information from Kotlin's type system
 * - Value class detection
 *
 * @property rawType the raw Java class type
 * @property typeVariables the list of generic type parameters
 * @property annotations the list of annotations on this type
 * @property isNullable whether this type is marked as nullable in Kotlin (e.g., String?)
 * @property isValueClass whether this type is a Kotlin value class (inline class)
 * @property componentType the component type if this is an array type
 */
@API(since = "1.1.0", status = EXPERIMENTAL)
class KotlinType(
    private val rawType: Class<*>,
    private val typeVariables: List<JvmType> = emptyList(),
    private val annotations: List<Annotation> = emptyList(),
    val isNullable: Boolean = false,
    val isValueClass: Boolean = false,
    private val componentType: JvmType? = null
) : JvmType {

    override fun getRawType(): Class<*> = rawType

    override fun getTypeVariables(): List<JvmType> = typeVariables

    override fun getAnnotations(): List<Annotation> = annotations

    override fun getComponentType(): JvmType? = componentType

    override fun getNullable(): Boolean = isNullable

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JvmType) return false

        if (rawType != other.rawType) return false
        if (typeVariables != other.typeVariables) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rawType.hashCode()
        result = 31 * result + typeVariables.hashCode()
        return result
    }

    override fun toString(): String {
        val nullableSuffix = if (isNullable) "?" else ""
        val valueSuffix = if (isValueClass) " (value)" else ""
        return if (typeVariables.isEmpty()) {
            "${rawType.simpleName}$nullableSuffix$valueSuffix"
        } else {
            "${rawType.simpleName}<${typeVariables.joinToString(", ")}>${nullableSuffix}$valueSuffix"
        }
    }

    companion object {
        /**
         * Creates a KotlinType from a Kotlin KType.
         *
         * @param kType the Kotlin type to convert
         * @return a new KotlinType with nullability and value class information
         */
        @JvmStatic
        fun fromKType(kType: KType): KotlinType {
            val rawClass = kType.jvmErasure.java
            val typeVariables = kType.arguments.mapNotNull { arg ->
                arg.type?.let { fromKType(it) }
            }
            return KotlinType(
                rawType = rawClass,
                typeVariables = typeVariables,
                isNullable = kType.isMarkedNullable,
                isValueClass = kType.jvmErasure.isValue
            )
        }

        /**
         * Wraps an existing JvmType as a KotlinType with additional Kotlin metadata.
         *
         * If the input is already a KotlinType, returns it as-is.
         *
         * @param jvmType the JvmType to wrap
         * @param nullable whether this type should be marked as nullable
         * @return a KotlinType with the same type information plus Kotlin metadata
         */
        @JvmStatic
        fun fromJvmType(jvmType: JvmType, nullable: Boolean = false): KotlinType {
            if (jvmType is KotlinType) return jvmType

            val rawClass = jvmType.rawType
            return KotlinType(
                rawType = rawClass,
                typeVariables = jvmType.typeVariables.toList(),
                annotations = jvmType.annotations,
                isNullable = nullable,
                isValueClass = rawClass.cachedKotlin().isValue,
                componentType = jvmType.componentType
            )
        }
    }
}
