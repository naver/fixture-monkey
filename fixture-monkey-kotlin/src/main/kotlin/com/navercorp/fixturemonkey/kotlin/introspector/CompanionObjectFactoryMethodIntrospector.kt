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
import com.navercorp.fixturemonkey.api.type.Types
import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObjectInstance

class CompanionObjectFactoryMethodIntrospector(
    private val companionObjectMethod: KFunction<*>
) : ArbitraryIntrospector {
    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val type = Types.getActualType(context.resolvedType)
        val kotlinType = type.kotlin
        val propertyNames = context.children.map { it.objectProperty.property.name }

        return ArbitraryIntrospectorResult(
            CombinableArbitrary.objectBuilder()
                .properties(context.combinableArbitrariesByArbitraryProperty)
                .build {
                    val valuesByMethodName = it.mapKeys { map -> map.key.objectProperty.property.name }
                    companionObjectMethod.call(
                        kotlinType.companionObjectInstance,
                        *propertyNames.map { name -> valuesByMethodName[name] }.toTypedArray()
                    )
                }
        )
    }
}
