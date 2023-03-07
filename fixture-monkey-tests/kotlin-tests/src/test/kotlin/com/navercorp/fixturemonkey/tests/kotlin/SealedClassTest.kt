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

package com.navercorp.fixturemonkey.tests.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.generator.InterfaceObjectPropertyGenerator
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest

class SealedClassTest {
    @RepeatedTest(TEST_COUNT)
    fun sampleSealedClass() {
        val actual = SUT.giveMeOne<SealedClass>()

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleImplementedSealedClass() {
        val actual = SUT.giveMeOne<ImplementedSealedClass>()

        then(actual).isNotNull
    }

    sealed class SealedClass

    class ImplementedSealedClass(
        val string: String,
        val integer: Int,
        val float: Float,
        val long: Long,
        val double: Double,
        val byte: Byte,
        val char: Char,
        val short: Short,
        val boolean: Boolean,
        val enum: Enum
    ) : SealedClass()

    object ObjectSealedClass : SealedClass()

    enum class Enum { ONE, TWO, THREE }

    companion object {
        val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .pushObjectPropertyGenerator( // TODO: move to kotlin module
                MatcherOperator(
                    { (it.type as Class<*>).kotlin.isSealed },
                    ObjectPropertyGenerator { context ->
                        InterfaceObjectPropertyGenerator(
                            (context.property.type as Class<*>).kotlin.sealedSubclasses
                                .filter { it.objectInstance == null }
                                .map { it.java }
                        )
                            .generate(context)
                    }
                )
            )
            .build()
    }
}
