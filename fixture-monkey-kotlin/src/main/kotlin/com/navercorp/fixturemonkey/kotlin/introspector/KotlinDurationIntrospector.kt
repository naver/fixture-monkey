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
import com.navercorp.fixturemonkey.kotlin.matcher.Matchers.DURATION_TYPE_MATCHER
import kotlin.reflect.full.primaryConstructor
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val SEED = 234L

class KotlinDurationIntrospector : ArbitraryIntrospector, Matcher {
    override fun match(property: Property) = DURATION_TYPE_MATCHER.match(property)

    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val kClass = Duration::class
        val primaryConstructor = kClass.primaryConstructor

        require(match(context.resolvedProperty)) { "Given type is not Duration type: " + context.resolvedType }
        require(primaryConstructor != null) { "Duration class must have primary constructor" }
        require(primaryConstructor.parameters.size == 1) { "Duration class must have only one parameter" }

        val parameterName = primaryConstructor.parameters[0].name

        return ArbitraryIntrospectorResult(
            CombinableArbitrary.objectBuilder()
                .properties(context.combinableArbitrariesByArbitraryProperty)
                .build {
                    val parameterValue = it.mapKeys { map -> map.key.objectProperty.property.name }[parameterName]
                    if (parameterValue is Long) {
                        parameterValue.toDuration(DurationUnit.values().random())
                    } else {
                        Duration.ZERO
                    }
                }
        )
    }
}

