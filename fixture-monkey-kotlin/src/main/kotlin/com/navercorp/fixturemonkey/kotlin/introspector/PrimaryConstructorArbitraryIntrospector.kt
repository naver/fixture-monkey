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

import com.navercorp.fixturemonkey.api.collection.LruCache
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.arbitrary.ObjectCombinableArbitrary
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.type.Types
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.MAINTAINED
import java.lang.reflect.Modifier
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

@API(since = "0.4.0", status = MAINTAINED)
class PrimaryConstructorArbitraryIntrospector : ArbitraryIntrospector {
    companion object {
        val INSTANCE = PrimaryConstructorArbitraryIntrospector()
        private val CONSTRUCTOR_CACHE = LruCache<Class<*>, KFunction<*>>(2048)
    }

    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val type = Types.getActualType(context.resolvedType)
        if (Modifier.isAbstract(type.modifiers)) {
            return ArbitraryIntrospectorResult.EMPTY
        }

        val kotlinClass = Reflection.createKotlinClass(type) as KClass<*>
        val constructor = CONSTRUCTOR_CACHE.computeIfAbsent(type) {
            requireNotNull(kotlinClass.primaryConstructor) { "No kotlin primary constructor provided for $kotlinClass" }
        }

        return ArbitraryIntrospectorResult(
            ObjectCombinableArbitrary(context.combinableArbitrariesByArbitraryProperty) {
                val arbitrariesByPropertyName = it.mapKeys { map -> map.key.objectProperty.property.name }

                val map = mutableMapOf<KParameter, Any?>()
                for (parameter in constructor.parameters) {
                    map[parameter] = arbitrariesByPropertyName[parameter.name]
                }
                constructor.callBy(map)
            },
        )
    }
}
