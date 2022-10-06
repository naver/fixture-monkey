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
import com.navercorp.fixturemonkey.api.type.Types
import net.jqwik.api.Builders
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class PrimaryConstructorArbitraryIntrospector : ArbitraryIntrospector {
    companion object {
        val INSTANCE = PrimaryConstructorArbitraryIntrospector()
    }

    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val type = Types.getActualType(context.property.type)
        if (type.isInterface) {
            return ArbitraryIntrospectorResult.EMPTY
        }

        val arbitrariesByResolvedName = context.childrenArbitraryContexts.arbitrariesByResolvedName

        val kotlinClass = Reflection.createKotlinClass(type) as KClass<*>
        val constructor =
            requireNotNull(kotlinClass.primaryConstructor) { "No primary constructor provided for $kotlinClass" }

        var builderCombinator = Builders.withBuilder { mutableMapOf<KParameter, Any?>() }
        for (parameter in constructor.parameters) {
            val parameterArbitrary = arbitrariesByResolvedName[parameter.name]
            builderCombinator = builderCombinator.use(parameterArbitrary).`in` { map, value ->
                map.apply {
                    this[parameter] = value
                }
            }
        }
        return ArbitraryIntrospectorResult(
            builderCombinator.build {
                constructor.callBy(it)
            }
        )
    }
}
