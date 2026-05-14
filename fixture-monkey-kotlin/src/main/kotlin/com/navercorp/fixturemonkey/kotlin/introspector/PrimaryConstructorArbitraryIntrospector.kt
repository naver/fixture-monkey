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
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyGenerator
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.kotlin.property.KotlinConstructorParameterPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.type.cachedKotlin
import com.navercorp.fixturemonkey.kotlin.type.isKotlinLambda
import com.navercorp.fixturemonkey.kotlin.type.isKotlinType
import com.navercorp.fixturemonkey.kotlin.type.kotlinPrimaryConstructor
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.MAINTAINED
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

@API(since = "0.4.0", status = MAINTAINED)
class PrimaryConstructorArbitraryIntrospector : ArbitraryIntrospector, Matcher {
    override fun match(property: Property): Boolean {
        val rawType = property.jvmType.rawType
        if (!rawType.isKotlinType()) {
            return false
        }

        val kotlinClass = rawType.cachedKotlin()

        return !kotlinClass.isKotlinLambda() &&
            kotlinClass != Unit::class &&
            kotlinClass.objectInstance == null
    }

    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val type = context.resolvedType
        if (Modifier.isAbstract(type.modifiers)) {
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }

        val constructor = try {
            type.kotlinPrimaryConstructor()
        } catch (ex: Exception) {
            LOGGER.warn("Given type $type is failed to generated due to the exception. It may be null.", ex)
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }

        return ArbitraryIntrospectorResult(
            CombinableArbitrary.objectBuilder()
                .properties(context.combinableArbitrariesByArbitraryProperty)
                .build {
                    val arbitrariesByPropertyName = HashMap<String?, Any?>(it.size)
                    it.forEach { (arbitraryProperty, value) ->
                        arbitrariesByPropertyName[arbitraryProperty.objectProperty.property.name] = value
                    }

                    callConstructor(constructor, arbitrariesByPropertyName)
                },
        )
    }

    private fun callConstructor(
        constructor: KFunction<*>,
        arbitrariesByPropertyName: Map<String?, Any?>
    ): Any? {
        val parameters = constructor.parameters
        val arguments = arrayOfNulls<Any>(parameters.size)
        val includedParameters = BooleanArray(parameters.size)
        var hasSkippedOptionalParameter = false

        for ((index, parameter) in parameters.withIndex()) {
            val resolvedArbitrary = arbitrariesByPropertyName[parameter.name]
            if (resolvedArbitrary != null || !parameter.isOptional || parameter.type.isMarkedNullable) {
                arguments[index] = resolvedArbitrary
                includedParameters[index] = true
            } else {
                hasSkippedOptionalParameter = true
            }
        }

        if (!hasSkippedOptionalParameter) {
            return constructor.call(*arguments)
        }

        val generatedByParameters = LinkedHashMap<KParameter, Any?>(parameters.size)
        for ((index, parameter) in parameters.withIndex()) {
            if (includedParameters[index]) {
                generatedByParameters[parameter] = arguments[index]
            }
        }
        return constructor.callBy(generatedByParameters)
    }

    override fun getRequiredPropertyGenerator(p: Property): PropertyGenerator = PROPERTY_GENERATOR

    companion object {
        val INSTANCE = PrimaryConstructorArbitraryIntrospector()
        private val LOGGER = LoggerFactory.getLogger(PrimaryConstructorArbitraryIntrospector::class.java)
        internal val PROPERTY_GENERATOR = KotlinConstructorParameterPropertyGenerator({ property ->
            property.jvmType.rawType.kotlinPrimaryConstructor()
        })
    }
}
