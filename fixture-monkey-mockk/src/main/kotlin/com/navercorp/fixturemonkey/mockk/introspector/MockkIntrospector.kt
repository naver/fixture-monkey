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

package com.navercorp.fixturemonkey.mockk.introspector

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.property.Property
import io.mockk.mockkClass
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import java.lang.reflect.Modifier
import java.util.function.Supplier
import kotlin.reflect.KClass

/**
 * Generates a fresh MockK mock for every sample.
 *
 * As a [Matcher] it matches abstract classes and interfaces, so it can be used as a fallback for types
 * Fixture Monkey cannot instantiate. Stubbing registered for a type in [stubbings] is applied to each new mock.
 */
@API(since = "1.2.4", status = EXPERIMENTAL)
class MockkIntrospector(
    private val relaxed: Boolean = true,
    private val relaxUnitFun: Boolean = false,
    private val stubbings: Map<Class<*>, Any.() -> Unit> = emptyMap(),
) : ArbitraryIntrospector, Matcher {
    override fun match(property: Property): Boolean {
        val type = property.jvmType.rawType
        return !type.isPrimitive && !type.isArray && Modifier.isAbstract(type.modifiers)
    }

    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val type = context.resolvedType
        return ArbitraryIntrospectorResult(CombinableArbitrary.from(Supplier { newMock(type) }))
    }

    @Suppress("UNCHECKED_CAST")
    private fun newMock(type: Class<*>): Any {
        val stubbing = stubbings[type]
        return mockkClass(
            type = type.kotlin as KClass<Any>,
            relaxed = relaxed,
            relaxUnitFun = relaxUnitFun,
        ) {
            stubbing?.invoke(this)
        }
    }
}
