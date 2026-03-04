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
import com.navercorp.fixturemonkey.kotlin.type.KotlinType
import com.navercorp.fixturemonkey.kotlin.type.cachedKotlin
import com.navercorp.objectfarm.api.node.InterfaceResolver
import com.navercorp.objectfarm.api.node.JavaNode
import com.navercorp.objectfarm.api.node.JvmNode
import com.navercorp.objectfarm.api.node.JvmNodeContext
import com.navercorp.objectfarm.api.node.JvmNodePromoter
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate
import com.navercorp.objectfarm.api.type.JavaType
import com.navercorp.objectfarm.api.type.JvmType
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import java.lang.reflect.Modifier
import java.util.Random

/**
 * Promotes Kotlin sealed class and interface node candidates.
 *
 * This promoter handles:
 * - Kotlin sealed classes: resolves to one of the sealed subclasses
 * - Kotlin interfaces: delegates to context's InterfaceResolver
 *
 * For sealed classes, this promoter picks a random sealed subclass
 * using the context's seed for deterministic selection.
 */
@API(since = "1.1.0", status = EXPERIMENTAL)
class KotlinInterfaceNodePromoter : JvmNodePromoter {

    override fun canPromote(node: JvmNodeCandidate): Boolean {
        val rawType = node.type.rawType
        if (!KotlinTypeDetector.isKotlinType(rawType)) {
            return false
        }

        val kotlinClass = rawType.cachedKotlin()

        // Handle sealed classes
        if (kotlinClass.isSealed) {
            return true
        }

        // Handle Kotlin interfaces and abstract classes
        val modifiers = rawType.modifiers
        return Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers)
    }

    override fun promote(node: JvmNodeCandidate, context: JvmNodeContext): List<JvmNode> {
        val rawType = node.type.rawType
        val kotlinClass = rawType.cachedKotlin()

        // Handle sealed classes
        if (kotlinClass.isSealed) {
            val sealedSubclasses = kotlinClass.sealedSubclasses
            if (sealedSubclasses.isNotEmpty()) {
                // Select one subclass deterministically using seed
                val random = Random(context.seedState.initialSeed xor rawType.hashCode().toLong())
                val selectedSubclass = sealedSubclasses[random.nextInt(sealedSubclasses.size)]
                val selectedType = selectedSubclass.java

                // Create JvmType for the selected subclass
                val resolvedType = createResolvedType(selectedType, node.type)

                // Check if the selected type is also a Kotlin type
                if (KotlinTypeDetector.isKotlinType(selectedType)) {
                    val kotlinType = KotlinType(
                        rawType = selectedType,
                        typeVariables = resolvedType.typeVariables.toList(),
                        annotations = node.type.annotations,
                        isNullable = false,
                        isValueClass = selectedSubclass.isValue
                    )

                    return listOf(
                        JavaNode(kotlinType, node.name, null, node.creationMethod)
                    )
                }

                return listOf(
                    JavaNode(resolvedType, node.name, null, node.creationMethod)
                )
            }
        }

        // Handle interfaces and abstract classes using context's resolver
        val interfaceResolver = context.interfaceResolver
        if (interfaceResolver != null) {
            // Recursively resolve type until we get a concrete type
            val resolvedType = resolveRecursively(node.type, interfaceResolver, context.maxRecursionDepth)
            if (resolvedType != null && resolvedType.rawType != rawType) {
                // Check if resolved type is a Kotlin type
                if (KotlinTypeDetector.isKotlinType(resolvedType.rawType)) {
                    val kotlinType = KotlinType.fromJvmType(resolvedType, false)
                    return listOf(
                        JavaNode(kotlinType, node.name, null, node.creationMethod)
                    )
                }

                return listOf(
                    JavaNode(resolvedType, node.name, null, node.creationMethod)
                )
            }
        }

        // Fallback: keep the abstract type as-is (may fail at instantiation)
        // This matches the behavior of AbstractTypeNodePromoter
        return listOf(
            JavaNode(
                node.type,
                node.name,
                null,
                node.creationMethod
            )
        )
    }

    /**
     * Creates a JvmType for the resolved concrete class, preserving type variables if applicable.
     */
    private fun createResolvedType(concreteClass: Class<*>, originalType: JvmType): JvmType {
        // For now, create a simple type. Type variable mapping could be enhanced.
        val typeVariables = originalType.typeVariables
        return if (typeVariables.isEmpty()) {
            JavaType(concreteClass, emptyList(), originalType.annotations)
        } else {
            JavaType(concreteClass, typeVariables.toList(), originalType.annotations)
        }
    }

    /**
     * Recursively resolves a type using the interface resolver until a concrete type is found.
     * This handles chained type resolution (e.g., Collection -> List -> LinkedList).
     *
     * @param type the type to resolve
     * @param resolver the interface resolver
     * @param maxDepth maximum number of resolution iterations to prevent infinite loops
     * @return the resolved concrete type, or null if resolution fails
     */
    private fun resolveRecursively(
        type: JvmType,
        resolver: InterfaceResolver,
        maxDepth: Int
    ): JvmType? {
        var currentType = type
        val visited = mutableSetOf<Class<*>>()

        for (depth in 0 until maxDepth) {
            val rawType = currentType.rawType

            // Cycle detection
            if (!visited.add(rawType)) {
                return currentType
            }

            val resolved = resolver.resolve(currentType) ?: return currentType

            // Check if the resolved type is concrete (not interface/abstract)
            val modifiers = resolved.rawType.modifiers
            if (!Modifier.isInterface(modifiers) && !Modifier.isAbstract(modifiers)) {
                return resolved
            }

            currentType = resolved
        }

        return currentType
    }
}
