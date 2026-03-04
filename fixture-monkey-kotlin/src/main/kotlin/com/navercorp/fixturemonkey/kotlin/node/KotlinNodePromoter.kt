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

package com.navercorp.fixturemonkey.kotlin.node

import com.navercorp.fixturemonkey.api.type.KotlinTypeDetector
import com.navercorp.fixturemonkey.kotlin.type.KotlinNullabilityUtils
import com.navercorp.fixturemonkey.kotlin.type.KotlinType
import com.navercorp.fixturemonkey.kotlin.type.cachedKotlin
import com.navercorp.objectfarm.api.node.JavaNode
import com.navercorp.objectfarm.api.node.JvmNode
import com.navercorp.objectfarm.api.node.JvmNodeContext
import com.navercorp.objectfarm.api.node.JvmNodePromoter
import com.navercorp.objectfarm.api.nodecandidate.ConstructorParamCreationMethod
import com.navercorp.objectfarm.api.nodecandidate.FieldAccessCreationMethod
import com.navercorp.objectfarm.api.nodecandidate.JavaMapNodeCandidate
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import java.lang.reflect.Modifier

/**
 * Promotes Kotlin type node candidates to KotlinNode instances.
 *
 * This promoter handles Kotlin-specific type features:
 * - Detects Kotlin types using @Metadata annotation
 * - Preserves nullability information from Kotlin's type system
 * - Detects constructor parameters with default values
 * - Handles value classes
 * - Handles Java types used in Kotlin constructor parameters (with correct nullability)
 *
 * It only processes concrete Kotlin types (not interfaces or abstract classes).
 * Interface and abstract type handling should be done by other promoters.
 */
@API(since = "1.1.0", status = EXPERIMENTAL)
class KotlinNodePromoter : JvmNodePromoter {

    override fun canPromote(node: JvmNodeCandidate): Boolean {
        if (node is JavaMapNodeCandidate) {
            return false
        }

        val rawType = node.type.rawType
        val modifiers = rawType.modifiers

        // Don't handle interfaces or abstract classes
        if (Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers)) {
            return false
        }

        // Handle Kotlin types directly
        if (KotlinTypeDetector.isKotlinType(rawType)) {
            return true
        }

        // Also handle Java types that are used in Kotlin constructor parameters
        // This ensures correct nullability handling for types like String, Integer, etc.
        return isFromKotlinConstructorParam(node)
    }

    override fun promote(node: JvmNodeCandidate, context: JvmNodeContext): List<JvmNode> {
        val rawType = node.type.rawType

        // Determine nullability from constructor parameter if available
        val isNullable = findNullability(node)

        // Determine if it's a value class (only for Kotlin types)
        val isValueClass = if (KotlinTypeDetector.isKotlinType(rawType)) {
            rawType.cachedKotlin().isValue
        } else {
            false
        }

        // Create KotlinType with Kotlin metadata
        val kotlinType = KotlinType(
            rawType = rawType,
            typeVariables = node.type.typeVariables.toList(),
            annotations = node.type.annotations,
            isNullable = isNullable,
            isValueClass = isValueClass,
            componentType = node.type.componentType
        )

        return listOf(
            JavaNode(kotlinType, node.name, null, node.creationMethod)
        )
    }

    /**
     * Checks if this node is created from a Kotlin constructor parameter or field.
     * This is used to also handle Java types (like String, Integer) that are
     * used in Kotlin classes, ensuring correct nullability.
     */
    private fun isFromKotlinConstructorParam(node: JvmNodeCandidate): Boolean {
        val creationMethod = node.creationMethod

        // Check if created from Kotlin constructor parameter
        if (creationMethod is ConstructorParamCreationMethod) {
            val constructor = creationMethod.constructor
            val declaringClass = constructor.declaringClass
            return KotlinTypeDetector.isKotlinType(declaringClass)
        }

        // Check if created from a field in a Kotlin class
        if (creationMethod is FieldAccessCreationMethod) {
            val field = creationMethod.field
            val declaringClass = field.declaringClass
            return KotlinTypeDetector.isKotlinType(declaringClass)
        }

        return false
    }

    /**
     * Determines if this node's type is nullable based on constructor parameter or field info.
     */
    private fun findNullability(node: JvmNodeCandidate): Boolean {
        val creationMethod = node.creationMethod

        // Check constructor parameter nullability
        if (creationMethod is ConstructorParamCreationMethod) {
            return findNullabilityFromConstructor(creationMethod)
        }

        // Check field nullability
        if (creationMethod is FieldAccessCreationMethod) {
            return findNullabilityFromField(creationMethod)
        }

        return false
    }

    private fun findNullabilityFromConstructor(creationMethod: ConstructorParamCreationMethod): Boolean {
        return KotlinNullabilityUtils.isNullableConstructorParam(
            creationMethod.constructor,
            creationMethod.parameterIndex
        )
    }

    private fun findNullabilityFromField(creationMethod: FieldAccessCreationMethod): Boolean {
        return KotlinNullabilityUtils.isNullableField(creationMethod.field)
    }
}
