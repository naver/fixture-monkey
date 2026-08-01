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
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin
import com.navercorp.fixturemonkey.api.plugin.Plugin
import com.navercorp.fixturemonkey.api.property.ConcreteTypeCandidateConcretePropertyResolver
import com.navercorp.fixturemonkey.api.property.PropertyUtils
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.plugin.JvmTypeSystem
import com.navercorp.fixturemonkey.plugin.JvmTypeSystemPlugin
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import com.navercorp.objectfarm.api.node.JvmNodeContext
import com.navercorp.objectfarm.api.node.LeafTypeResolver
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator
import com.navercorp.objectfarm.api.type.JvmType
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.LinkedList
import java.util.TreeSet
import java.util.concurrent.atomic.AtomicBoolean

class PluginTest {
    @Test
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

    @Test
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

    @Test
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

    @Test
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
                        when (property.jvmType.rawType) {
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
                        when (property.jvmType.rawType) {
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

    @Test
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

    @Test
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

    @Test
    fun inlinedValueResolverOfLaterPluginKeepsKotlinResolver() {
        val laterResolverApplied = AtomicBoolean(false)

        class LaterJvmTypeSystemPlugin : Plugin, JvmTypeSystemPlugin {
            override fun accept(optionsBuilder: FixtureMonkeyOptionsBuilder) = Unit

            override fun configure(typeSystem: JvmTypeSystem) {
                typeSystem.inlinedValueResolver { _, _, extracted ->
                    laterResolverApplied.set(true)
                    extracted
                }
            }
        }

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(LaterJvmTypeSystemPlugin())
            .build()

        val expected = ValueClassObject(Foo("hello"))

        val actual = sut.giveMeBuilder<ValueClassObject>()
            .set(expected)
            .sample()

        then(laterResolverApplied).isTrue
        then(actual).isEqualTo(expected)
    }

    @Test
    fun jvmTypeSystemPartsOfLaterPluginMergeWithKotlinPlugin() {
        val leafTypeResolverConsulted = AtomicBoolean(false)
        val candidateGeneratorWrapped = AtomicBoolean(false)

        class LaterJvmTypeSystemPlugin : Plugin, JvmTypeSystemPlugin {
            override fun accept(optionsBuilder: FixtureMonkeyOptionsBuilder) = Unit

            override fun configure(typeSystem: JvmTypeSystem) {
                typeSystem.leafTypeResolvers(
                    listOf(
                        LeafTypeResolver {
                            leafTypeResolverConsulted.set(true)
                            false
                        }
                    )
                )
                typeSystem.candidateGeneratorWrapper { delegate ->
                    object : JvmNodeCandidateGenerator {
                        override fun generateNextNodeCandidates(jvmType: JvmType): List<JvmNodeCandidate> {
                            candidateGeneratorWrapped.set(true)
                            return delegate.generateNextNodeCandidates(jvmType)
                        }

                        override fun isSupported(jvmType: JvmType): Boolean = delegate.isSupported(jvmType)

                        override fun isSupported(jvmType: JvmType, context: JvmNodeContext): Boolean =
                            delegate.isSupported(jvmType, context)
                    }
                }
            }
        }

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(LaterJvmTypeSystemPlugin())
            .build()

        val expected = ValueClassObject(Foo("hello"))

        val actual = sut.giveMeBuilder<ValueClassObject>()
            .set(expected)
            .sample()
        val leafHolder = sut.giveMeOne<NoPropertyObject>()

        then(candidateGeneratorWrapped).isTrue
        then(leafTypeResolverConsulted).isTrue
        then(actual).isEqualTo(expected)
        then(leafHolder.noProperty).isNotNull
    }

    @JvmInline
    value class Foo(val bar: String)

    data class ValueClassObject(val foo: Foo)

    class NoProperty

    data class NoPropertyObject(val noProperty: NoProperty)
} 
