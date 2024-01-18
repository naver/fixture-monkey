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
import com.navercorp.fixturemonkey.kotlin.type.cachedKotlin
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties

class KotlinPropertyArbitraryIntrospector : ArbitraryIntrospector {
    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val type = Types.getActualType(context.resolvedType)
        val kotlinType = type.cachedKotlin()

        val generated = if (context.generated != CombinableArbitrary.NOT_GENERATED) {
            context.generated
        } else {
            CombinableArbitrary.from { kotlinType.createInstance() }
        }

        return ArbitraryIntrospectorResult(
            CombinableArbitrary.objectBuilder()
                .properties(context.combinableArbitrariesByArbitraryProperty)
                .build { arbitraryListByArbitraryProperty ->
                    val instance = generated.combined()
                    val valuesByPropertyName =
                        arbitraryListByArbitraryProperty.mapKeys { it.key.objectProperty.property.name }

                    kotlinType.declaredMemberProperties
                        .filter { it is KMutableProperty<*> }
                        .map { it as KMutableProperty<*> }
                        .filter { it.setter.visibility == KVisibility.INTERNAL || it.setter.visibility == KVisibility.PUBLIC }
                        .forEach {
                            if (valuesByPropertyName.containsKey(it.name)) {
                                it.setter.call(instance, valuesByPropertyName[it.name])
                            }
                        }

                    instance
                },
        )
    }

    companion object {
        val INSTANCE = KotlinPropertyArbitraryIntrospector()
    }
}
