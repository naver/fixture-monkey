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

package com.navercorp.fixturemonkey.kotlin.generator

import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext
import com.navercorp.fixturemonkey.api.property.ConstructorProperty
import com.navercorp.fixturemonkey.api.property.FieldProperty
import com.navercorp.fixturemonkey.api.type.KotlinTypeDetector
import com.navercorp.fixturemonkey.kotlin.type.KotlinNullabilityUtils
import com.navercorp.fixturemonkey.kotlin.type.KotlinType
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL

/**
 * A [NullInjectGenerator] that respects Kotlin's nullability information.
 *
 * This generator checks if the property's type is a [KotlinType] and if it's non-nullable,
 * it returns 0.0 (no null injection). Otherwise, it delegates to the provided delegate generator.
 *
 * @property delegate the delegate generator to use when the type is not a non-nullable KotlinType
 * @since 1.1.0
 */
@API(since = "1.1.0", status = EXPERIMENTAL)
class KotlinNullInjectGenerator(
    private val delegate: NullInjectGenerator
) : NullInjectGenerator {

    override fun generate(context: ObjectPropertyGeneratorContext): Double {
        val property = context.property

        // Check ConstructorProperty for Kotlin nullability
        if (property is ConstructorProperty) {
            val constructor = property.constructor
            if (KotlinTypeDetector.isKotlinType(constructor.declaringClass)) {
                val isNullable = KotlinNullabilityUtils.isNullableConstructorParamByName(
                    constructor,
                    property.name
                )
                if (!isNullable) {
                    return NOT_NULL_INJECT
                }
            }
        }

        // Check FieldProperty for Kotlin nullability
        if (property is FieldProperty) {
            val field = property.field
            if (KotlinTypeDetector.isKotlinType(field.declaringClass)) {
                val isNullable = KotlinNullabilityUtils.isNullableField(field)
                if (!isNullable) {
                    return NOT_NULL_INJECT
                }
            }
        }

        // Check Property.isNullable() — covers KotlinConstructorParameterProperty
        // and properties with nullable info propagated from JvmType.getNullable()
        val nullable = property.isNullable
        if (nullable == false) {
            return NOT_NULL_INJECT
        }

        // Fallback: use owner (parent) type to determine nullability for properties
        // where nullable info is not directly available (e.g., TypeNameProperty with nullable=null
        // in the Instantiator path where PropertyGeneratorNodeCandidateGenerator loses KotlinType info)
        if (nullable == null) {
            val ownerProperty = context.ownerProperty
            if (ownerProperty != null) {
                val ownerType = ownerProperty.objectProperty?.property?.type
                if (ownerType is Class<*> && KotlinTypeDetector.isKotlinType(ownerType)) {
                    val ownerNullable = KotlinNullabilityUtils.isNullableByOwnerType(ownerType, property.name)
                    if (ownerNullable == false) {
                        return NOT_NULL_INJECT
                    }
                }
            }
        }

        return delegate.generate(context)
    }

    companion object {
        private const val NOT_NULL_INJECT = 0.0
    }
}
