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

package com.navercorp.fixturemonkey.kotlin.generator

import com.navercorp.fixturemonkey.api.property.FieldProperty
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers
import com.navercorp.fixturemonkey.customizer.WithFixtureCustomizer
import com.navercorp.fixturemonkey.generator.AbstractArbitraryGenerator
import com.navercorp.fixturemonkey.generator.ArbitraryGenerator
import com.navercorp.fixturemonkey.generator.FieldArbitraries
import com.navercorp.fixturemonkey.kotlin.customizer.customizeFields
import net.jqwik.api.Arbitrary
import net.jqwik.api.Builders
import java.lang.reflect.Field
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class PrimaryConstructorArbitraryGenerator(
    private val arbitraryCustomizers: ArbitraryCustomizers = ArbitraryCustomizers()
) : AbstractArbitraryGenerator(), WithFixtureCustomizer {

    companion object {
        val INSTANCE = PrimaryConstructorArbitraryGenerator()
    }

    private val propertyNameResolver: PropertyNameResolver = PropertyNameResolver.IDENTITY

    override fun <T : Any> generateObject(
        arbitraryType: ArbitraryType<*>,
        nodes: List<ArbitraryNode<*>>
    ): Arbitrary<T> {
        @Suppress("UNCHECKED_CAST")
        val clazz = Reflection.createKotlinClass(arbitraryType.type) as KClass<T>

        val fieldArbitraries = FieldArbitraries(
            toArbitrariesByFieldName(nodes, { it.propertyName }) { _, arbitrary -> arbitrary }
        )

        arbitraryCustomizers.customizeFields(clazz, fieldArbitraries)

        val constructor = requireNotNull(clazz.primaryConstructor) { "No primary constructor provided for $clazz" }

        var builderCombinator = Builders.withBuilder { mutableMapOf<KParameter, Any?>() }
        for (parameter in constructor.parameters) {
            val parameterArbitrary = fieldArbitraries.getArbitrary(parameter.name)
            builderCombinator = builderCombinator.use(parameterArbitrary).`in` { map, value ->
                map.apply {
                    this[parameter] = value
                }
            }
        }

        return builderCombinator.build { map ->
            constructor.callBy(map).let {
                arbitraryCustomizers.customizeFixture(clazz.java, it)
            }
        }
    }

    override fun resolveFieldName(field: Field?): String? {
        return this.propertyNameResolver.resolve(FieldProperty(field))
    }

    override fun withFixtureCustomizers(arbitraryCustomizers: ArbitraryCustomizers): ArbitraryGenerator {
        return if (this.arbitraryCustomizers === arbitraryCustomizers) {
            this
        } else {
            PrimaryConstructorArbitraryGenerator(arbitraryCustomizers)
        }
    }
}
