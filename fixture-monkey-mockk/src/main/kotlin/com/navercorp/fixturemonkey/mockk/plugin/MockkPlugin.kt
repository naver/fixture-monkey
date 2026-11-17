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

package com.navercorp.fixturemonkey.mockk.plugin

import com.navercorp.fixturemonkey.api.introspector.MatchArbitraryIntrospector
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder
import com.navercorp.fixturemonkey.api.plugin.Plugin
import com.navercorp.fixturemonkey.mockk.introspector.MockkIntrospector
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import kotlin.reflect.KClass

/**
 * Generates MockK mocks inside Fixture Monkey fixtures.
 *
 * Interfaces and abstract classes that Fixture Monkey cannot otherwise generate become MockK mocks.
 * Types registered with [mock] are always generated as mocks, including final Kotlin classes,
 * and the optional stubbing block is applied to every generated mock of that type.
 *
 * Mocks are relaxed by default, so unstubbed calls return default values instead of throwing.
 */
@API(since = "1.2.4", status = EXPERIMENTAL)
class MockkPlugin : Plugin {
    private var relaxed: Boolean = true
    private var relaxUnitFun: Boolean = false
    private val mockTypes: MutableMap<Class<*>, Any.() -> Unit> = LinkedHashMap()

    /**
     * Sets whether generated mocks are relaxed. If false, calling an unstubbed function throws a MockKException.
     */
    fun relaxed(relaxed: Boolean): MockkPlugin = apply { this.relaxed = relaxed }

    /**
     * Sets whether functions returning Unit are relaxed. Only meaningful when [relaxed] is false.
     */
    fun relaxUnitFun(relaxUnitFun: Boolean): MockkPlugin = apply { this.relaxUnitFun = relaxUnitFun }

    /**
     * Always generates [type] as a MockK mock, even if it is a concrete or final class.
     * [stubbing] runs on each new mock, so it can declare `every { ... }` answers.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> mock(type: KClass<T>, stubbing: T.() -> Unit = {}): MockkPlugin = apply {
        mockTypes[type.java] = stubbing as Any.() -> Unit
    }

    /**
     * Always generates [T] as a MockK mock. See [mock].
     */
    inline fun <reified T : Any> mock(noinline stubbing: T.() -> Unit = {}): MockkPlugin = mock(T::class, stubbing)

    override fun accept(optionsBuilder: FixtureMonkeyOptionsBuilder) {
        val stubbings = LinkedHashMap(mockTypes)
        val introspector = MockkIntrospector(relaxed, relaxUnitFun, stubbings)
        if (stubbings.isNotEmpty()) {
            optionsBuilder.insertFirstArbitraryIntrospector(
                { property -> stubbings.containsKey(property.jvmType.rawType) },
                introspector,
            )
        }
        optionsBuilder.fallbackIntrospector {
            MatchArbitraryIntrospector(listOf(it, introspector))
        }
    }
}
