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

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.kotlin.type.actualType
import com.navercorp.fixturemonkey.kotlin.type.isKotlinType
import org.slf4j.LoggerFactory

class KotlinAndJavaCompositeArbitraryIntrospector(
    private val kotlinArbitraryIntrospector: ArbitraryIntrospector,
    private val javaArbitraryIntrospector: ArbitraryIntrospector,
) : ArbitraryIntrospector {
    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val type = context.resolvedType.actualType()
        try {
            return if (type.isKotlinType()) {
                kotlinArbitraryIntrospector.introspect(context)
            } else {
                javaArbitraryIntrospector.introspect(context)
            }
        } catch (e: Exception) {
            LOGGER.warn("Given type $type is failed to generated due to the exception.", e)
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(KotlinAndJavaCompositeArbitraryIntrospector::class.java)
    }
}
