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
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin
import com.navercorp.fixturemonkey.api.property.ConcreteTypeCandidateConcretePropertyResolver
import com.navercorp.fixturemonkey.api.property.PropertyUtils
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.LinkedList
import java.util.TreeSet

class PluginTest {
    @RepeatedTest(TEST_COUNT)
    fun propertyCandidateResolverReturnsConcreteListType() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin {
                it.candidateConcretePropertyResolvers(
                    listOf(
                        MatcherOperator.exactTypeMatchOperator(
                            List::class.java,
                            ConcreteTypeCandidateConcretePropertyResolver(listOf(LinkedList::class.java))
                        )
                    )
                )
            }
            .build()

        val actual: List<String> = sut.giveMeOne()

        then(actual).isInstanceOf(LinkedList::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun nestedPropertyCandidateResolverReturnsConcreteListType() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin {
                it.candidateConcretePropertyResolvers(
                    listOf(
                        MatcherOperator.exactTypeMatchOperator(
                            Collection::class.java,
                            ConcreteTypeCandidateConcretePropertyResolver(listOf(List::class.java))
                        ),
                        MatcherOperator.exactTypeMatchOperator(
                            List::class.java,
                            ConcreteTypeCandidateConcretePropertyResolver(listOf(LinkedList::class.java))
                        )
                    )
                )
            }
            .build()

        val actual: Collection<String> = sut.giveMeOne()

        then(actual).isInstanceOf(LinkedList::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun propertyCandidateResolverReturnsConcreteSetType() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin {
                it.candidateConcretePropertyResolvers(
                    listOf(
                        MatcherOperator.exactTypeMatchOperator(
                            Set::class.java,
                            ConcreteTypeCandidateConcretePropertyResolver(listOf(TreeSet::class.java))
                        )
                    )
                )
            }
            .build()

        val actual: Set<String> = sut.giveMeOne()

        then(actual).isInstanceOf(TreeSet::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun nestedPropertyCandidateResolverReturnsConcreteSetType() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin {
                it.candidateConcretePropertyResolvers(
                    listOf(
                        MatcherOperator.exactTypeMatchOperator(
                            Collection::class.java,
                            ConcreteTypeCandidateConcretePropertyResolver(listOf(Set::class.java))
                        ),
                        MatcherOperator.exactTypeMatchOperator(
                            Set::class.java,
                            ConcreteTypeCandidateConcretePropertyResolver(listOf(TreeSet::class.java))
                        )
                    )
                )
            }
            .build()

        val actual: Collection<String> = sut.giveMeOne()

        then(actual).isInstanceOf(TreeSet::class.java)
    }


    @Test
    fun complexAbstractExtendsReturnsFirst() {
        abstract class ParentAbstractClass

        abstract class FirstAbstractClass : ParentAbstractClass()
        abstract class SecondAbstractClass : ParentAbstractClass()

        class FirstConcreteClass : FirstAbstractClass()
        class SecondConcreteClass : SecondAbstractClass()

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .abstractClassExtends(
                        AssignableTypeMatcher(ParentAbstractClass::class.java)
                    ) { property ->
                        when (property.type) {
                            FirstAbstractClass::class.java -> listOf(PropertyUtils.toProperty(FirstConcreteClass::class.java))
                            SecondAbstractClass::class.java -> listOf(PropertyUtils.toProperty(SecondConcreteClass::class.java))
                            else -> throw NotImplementedError()
                        }
                    }

            )
            .build()

        val actual: FirstAbstractClass = sut.giveMeOne()

        then(actual).isInstanceOf(FirstConcreteClass::class.java)
    }

    @Test
    fun complexAbstractExtendsReturnsSecond() {
        abstract class ParentAbstractClass

        abstract class FirstAbstractClass : ParentAbstractClass()
        abstract class SecondAbstractClass : ParentAbstractClass()

        class FirstConcreteClass : FirstAbstractClass()
        class SecondConcreteClass : SecondAbstractClass()

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .abstractClassExtends(
                        AssignableTypeMatcher(ParentAbstractClass::class.java)
                    ) { property ->
                        when (property.type) {
                            FirstAbstractClass::class.java -> listOf(PropertyUtils.toProperty(FirstConcreteClass::class.java))
                            SecondAbstractClass::class.java -> listOf(PropertyUtils.toProperty(SecondConcreteClass::class.java))
                            else -> throw NotImplementedError()
                        }
                    }

            )
            .build()

        val actual: SecondAbstractClass = sut.giveMeOne()

        then(actual).isInstanceOf(SecondConcreteClass::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun setConcreteTypeChildReturnsExactlyConcreteTypeChildType() {
        // given
        abstract class AbstractClass
        open class ConcreteType(val parentValue: String) : AbstractClass()
        class ConcreteTypeChild(val childValue: String) : ConcreteType("parent")

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .abstractClassExtends(
                        AbstractClass::class.java,
                        listOf(ConcreteType::class.java, ConcreteTypeChild::class.java)
                    )
            )
            .build()

        // when
        val actual = sut.giveMeBuilder<AbstractClass>()
            .set("$", ConcreteTypeChild(""))
            .sample()

        then(actual).isExactlyInstanceOf(ConcreteTypeChild::class.java)
    }

    @Test
    fun setReturnsImplementation() {
        // given
        abstract class ParentAbstractClass

        data class FirstConcreteClass(val string: String) : ParentAbstractClass()
        data class SecondConcreteClass(val string: String) : ParentAbstractClass()

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .abstractClassExtends(
                        ParentAbstractClass::class.java,
                        listOf(FirstConcreteClass::class.java, SecondConcreteClass::class.java)
                    )

            )
            .build()

        val expected = SecondConcreteClass("expected")

        // when
        val actual: SecondConcreteClass = sut.giveMeBuilder<ParentAbstractClass>()
            .set(expected)
            .sample() as SecondConcreteClass

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setReturnsLastImplementation() {
        // given
        abstract class ParentAbstractClass

        data class FirstConcreteClass(val string: String) : ParentAbstractClass()
        data class SecondConcreteClass(val string: String) : ParentAbstractClass()

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .abstractClassExtends(
                        ParentAbstractClass::class.java,
                        listOf(FirstConcreteClass::class.java, SecondConcreteClass::class.java)
                    )

            )
            .build()

        val notExpected = FirstConcreteClass("notExpected")
        val expected = SecondConcreteClass("expected")

        // when
        val actual: SecondConcreteClass = sut.giveMeBuilder<ParentAbstractClass>()
            .set(notExpected)
            .set(expected)
            .sample() as SecondConcreteClass

        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun setComplexReturnsSimple() {
        // given
        abstract class ParentAbstractClass

        data class FirstConcreteClass(val string: String, val int: Int, val instant: Instant) : ParentAbstractClass()
        data class SecondConcreteClass(val string: String, val long: Long) : ParentAbstractClass()

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .abstractClassExtends(
                        ParentAbstractClass::class.java,
                        listOf(FirstConcreteClass::class.java, SecondConcreteClass::class.java)
                    )

            )
            .build()

        val expected = FirstConcreteClass("expected", 1, Instant.now())
        val notExpected = SecondConcreteClass("notExpected", 2L)

        // when
        val actual = sut.giveMeBuilder<ParentAbstractClass>()
            .set(expected)
            .set(notExpected)
            .set(expected)
            .sample() as FirstConcreteClass

        then(actual).isEqualTo(expected)
    }
} 
