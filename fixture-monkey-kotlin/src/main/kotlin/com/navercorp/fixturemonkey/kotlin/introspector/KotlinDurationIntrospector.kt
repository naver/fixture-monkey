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
import com.navercorp.fixturemonkey.kotlin.matcher.Matchers.DURATION_TYPE_MATCHER
import com.navercorp.fixturemonkey.kotlin.property.KotlinConstructorParameterPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.type.kotlinPrimaryConstructor
import org.apiguardian.api.API
import kotlin.time.Duration

@API(since = "1.0.15", status = API.Status.EXPERIMENTAL)
class KotlinDurationIntrospector : ArbitraryIntrospector, Matcher {
    override fun match(property: Property) = DURATION_TYPE_MATCHER.match(property)

    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val rawValueArbitraryByArbitraryProperty = context.combinableArbitrariesByArbitraryProperty
            .mapValues { entry -> entry.value.filter(::isValidDurationRawValue) }

        return ArbitraryIntrospectorResult(
            CombinableArbitrary.objectBuilder()
                .properties(rawValueArbitraryByArbitraryProperty)
                .build {
                    val rawValue = it.values.first() as Long

                    PRIMARY_CONSTRUCTOR.callBy(mapOf(RAW_VALUE_PARAMETER to rawValue))
                }
        )
    }

    private fun isValidDurationRawValue(it: Any?): Boolean {
        val rawValue = it as Long
        val value = rawValue shr 1
        val isInNanos = (rawValue.toInt() and 1) == 0

        return if (isInNanos) {
            value in -MAX_NANOS..MAX_NANOS
        } else {
            (value in -MAX_MILLIS..MAX_MILLIS) &&
                (value !in -MAX_NANOS_IN_MILLIS..MAX_NANOS_IN_MILLIS)
        }
    }

    override fun getRequiredPropertyGenerator(property: Property): PropertyGenerator = PROPERTY_GENERATOR

    companion object {
        internal val PRIMARY_CONSTRUCTOR = Duration::class.java.kotlinPrimaryConstructor()
        internal val PROPERTY_GENERATOR = KotlinConstructorParameterPropertyGenerator({ PRIMARY_CONSTRUCTOR })
        internal val RAW_VALUE_PARAMETER =
            PRIMARY_CONSTRUCTOR.parameters.first { parameter -> parameter.name == "rawValue" }
        private const val NANOS_IN_MILLIS = 1_000_000
        private const val MAX_NANOS = Long.MAX_VALUE / 2 / NANOS_IN_MILLIS * NANOS_IN_MILLIS - 1 // ends in ..._999_999

        // maximum number duration can store in millisecond range, also encodes an infinite value
        private const val MAX_MILLIS = Long.MAX_VALUE / 2

        // MAX_NANOS expressed in milliseconds
        private const val MAX_NANOS_IN_MILLIS = MAX_NANOS / NANOS_IN_MILLIS
    }
}
