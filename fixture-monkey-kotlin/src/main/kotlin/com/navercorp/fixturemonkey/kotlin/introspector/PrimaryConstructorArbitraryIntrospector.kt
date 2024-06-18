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

package com.navercorp.fixturemonkey.kotlin.introspector

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyGenerator
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.kotlin.property.KotlinPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.type.actualType
import com.navercorp.fixturemonkey.kotlin.type.isKotlinType
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.MAINTAINED
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

@API(since = "0.4.0", status = MAINTAINED)
class PrimaryConstructorArbitraryIntrospector : ArbitraryIntrospector, Matcher {
    override fun match(property: Property): Boolean = property.type.actualType().isKotlinType()

    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val type = Types.getActualType(context.resolvedType)
        if (Modifier.isAbstract(type.modifiers)) {
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }

        val constructor = try {
            CONSTRUCTOR_CACHE.computeIfAbsent(type) {
                val kotlinClass = Reflection.createKotlinClass(type) as KClass<*>
                requireNotNull(kotlinClass.primaryConstructor) { "No kotlin primary constructor provided for $kotlinClass" }
            }.apply { isAccessible = true }
        } catch (ex: Exception) {
            LOGGER.warn("Given type $type is failed to generated due to the exception. It may be null.", ex)
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }

        return ArbitraryIntrospectorResult(
            CombinableArbitrary.objectBuilder()
                .properties(context.combinableArbitrariesByArbitraryProperty)
                .build {
                    val arbitrariesByPropertyName: Map<String?, Any?> =
                        it.mapKeys { map -> map.key.objectProperty.property.name }
                    val generatedByParameters = mutableMapOf<KParameter, Any?>()

                    for (parameter in constructor.parameters) {
                        val resolvedArbitrary = arbitrariesByPropertyName[parameter.name]
                        generatedByParameters[parameter] = resolvedArbitrary
                    }

                    constructor.callBy(generatedByParameters)
                },
        )
    }

    override fun getRequiredPropertyGenerator(property: Property): PropertyGenerator = KOTLIN_PROPERTY_GENERATOR

    companion object {
        val INSTANCE = PrimaryConstructorArbitraryIntrospector()
        private val LOGGER = LoggerFactory.getLogger(PrimaryConstructorArbitraryIntrospector::class.java)
        private val KOTLIN_PROPERTY_GENERATOR = KotlinPropertyGenerator()
        private val CONSTRUCTOR_CACHE = ConcurrentLruCache<Class<*>, KFunction<*>>(2048)
    }
}
