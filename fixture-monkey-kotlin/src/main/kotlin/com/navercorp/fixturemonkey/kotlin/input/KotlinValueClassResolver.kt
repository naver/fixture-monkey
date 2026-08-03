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

package com.navercorp.fixturemonkey.kotlin.input

import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache
import com.navercorp.fixturemonkey.api.type.KotlinTypeDetector.isKotlinType
import com.navercorp.fixturemonkey.kotlin.type.cachedKotlin
import com.navercorp.objectfarm.api.input.InlinedValueResolver
import com.navercorp.objectfarm.api.input.ExtractedField
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

/**
 * Restores Kotlin value class properties that a raw field read flattens.
 *
 * A `@JvmInline value class` is inlined into its owner's field, so the field holds the underlying
 * value rather than the value class instance. Decomposing a set value through raw field reads
 * therefore yields the underlying value at the property's path, and the owner's constructor
 * rejects it because it expects the value class.
 *
 * Only properties whose declared Kotlin type is a value class are re-boxed, and only when the field
 * does not already hold the value class instance. Kotlin does not inline every position: a nullable
 * value class over a primitive underlying type is stored boxed, while a nullable one over a
 * reference type still inlines. A field that already holds the boxed instance is left as read.
 */
@API(since = "1.2.1", status = EXPERIMENTAL)
class KotlinValueClassResolver : InlinedValueResolver {
    override fun resolve(owner: Any, memberName: String, extracted: ExtractedField): ExtractedField {
        val valueClassProperties = VALUE_CLASS_PROPERTIES_CACHE.computeIfAbsent(owner.javaClass) {
            if (!isKotlinType(it)) {
                return@computeIfAbsent emptyMap()
            }

            try {
                it.cachedKotlin().memberProperties
                    .mapNotNull { property ->
                        val returnClass = property.returnType.classifier as? KClass<*>
                        if (returnClass != null && returnClass.isValue) property.name to returnClass else null
                    }
                    .toMap()
            } catch (_: Exception) {
                emptyMap()
            }
        }

        val valueClass = valueClassProperties[memberName] ?: return extracted
        val rawValue = extracted.value ?: return extracted

        if (valueClass.javaObjectType.isInstance(rawValue)) {
            return extracted
        }

        val boxed = boxValueClass(rawValue, valueClass) ?: return extracted
        return ExtractedField(boxed, valueClass.java)
    }

    /**
     * Boxes the inlined [rawValue] back into [valueClass], unwinding nested value classes from the
     * inside out. A value class over another value class is inlined down to the innermost
     * underlying type, so each level has to be boxed before the next one can accept it.
     */
    private fun boxValueClass(rawValue: Any, valueClass: KClass<*>): Any? {
        val constructor = valueClass.primaryConstructor ?: return null
        val parameterClass = constructor.parameters.singleOrNull()?.type?.classifier as? KClass<*> ?: return null

        val argument = if (parameterClass.isValue && !parameterClass.javaObjectType.isInstance(rawValue)) {
            boxValueClass(rawValue, parameterClass) ?: return null
        } else {
            rawValue
        }

        return try {
            constructor.isAccessible = true
            constructor.call(argument)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private val VALUE_CLASS_PROPERTIES_CACHE = ConcurrentLruCache<Class<*>, Map<String, KClass<*>>>(2048)
    }
}
