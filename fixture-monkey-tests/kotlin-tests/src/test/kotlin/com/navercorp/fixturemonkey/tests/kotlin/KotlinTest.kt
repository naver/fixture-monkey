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
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter
import com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedRoot
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.FactoryMethodArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector
import com.navercorp.fixturemonkey.api.type.Types.GeneratingWildcardType
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.get
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeExperimentalBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.instantiator.instantiateBy
import com.navercorp.fixturemonkey.kotlin.into
import com.navercorp.fixturemonkey.kotlin.intoGetter
import com.navercorp.fixturemonkey.kotlin.pushExactTypeArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.setExp
import com.navercorp.fixturemonkey.kotlin.setExpGetter
import com.navercorp.fixturemonkey.kotlin.sizeExp
import com.navercorp.fixturemonkey.kotlin.sizeExpGetter
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import com.navercorp.fixturemonkey.tests.kotlin.ImmutableJavaTestSpecs.ArrayObject
import com.navercorp.fixturemonkey.tests.kotlin.ImmutableJavaTestSpecs.NestedArrayObject
import com.navercorp.fixturemonkey.tests.kotlin.JavaConstructorTestSpecs.JavaTypeObject
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.reflect.jvm.javaMethod

class KotlinTest {
    @Test
    fun kotlinObjectUseJavaGetterThrows() {
        class KotlinObject(val value: String)

        thenThrownBy {
            SUT.giveMeBuilder<KotlinObject>()
                .set(javaGetter(KotlinObject::value), "test")
                .sample()
                .value
        }.cause
            .isExactlyInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Kotlin type could not resolve property name.")
    }

    @Test
    fun javaGetter() {
        val expected = "test"

        val actual = SUT.giveMeExperimentalBuilder<JavaTypeObject>()
            .instantiateBy {
                constructor {
                    parameter<String>("string")
                    parameter<Int>()
                }
            }
            .set(javaGetter(JavaTypeObject::getString), expected)
            .sample()
            .string

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setExpArrayElement() {
        // given
        class ArrayObject(val array: Array<String>)

        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<ArrayObject>()
            .sizeExp(ArrayObject::array, 1)
            .setExp(ArrayObject::array[0], expected)
            .sample()
            .array[0]

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun setExpNestedArrayElement() {
        // given
        class ArrayObject(val array: Array<String>)
        class NestedArrayObject(val obj: ArrayObject)

        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<NestedArrayObject>()
            .sizeExp(NestedArrayObject::obj into ArrayObject::array, 1)
            .setExp(NestedArrayObject::obj into ArrayObject::array[0], expected)
            .sample()
            .obj
            .array[0]

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun setExpGetterArrayElement() {
        // given
        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<ArrayObject>()
            .instantiateBy {
                constructor<ArrayObject> {
                    parameter<Array<String>>(parameterName = "array")
                }
            }
            .sizeExpGetter(ArrayObject::getArray, 1)
            .setExpGetter(ArrayObject::getArray[0], expected)
            .sample()
            .array[0]

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun setExpGetterNestedArrayElement() {
        // given
        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<NestedArrayObject>()
            .instantiateBy {
                constructor<NestedArrayObject> {
                    parameter<ArrayObject>(parameterName = "obj")
                }
                constructor<ArrayObject> {
                    parameter<Array<String>>(parameterName = "array")
                }
            }
            .sizeExp(NestedArrayObject::getObj intoGetter ArrayObject::getArray, 1)
            .setExp(NestedArrayObject::getObj intoGetter ArrayObject::getArray[0], expected)
            .sample()
            .obj
            .array[0]

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun constructorArbitraryIntrospectorWithoutPrimaryConstructor() {
        // given
        class ConstructorWithoutAnyAnnotations(val string: String)

        val sut = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build()

        // when, then
        thenThrownBy { sut.giveMeOne<ConstructorWithoutAnyAnnotations>() }
            .hasMessageContaining("Primary Constructor does not exist.")
    }

    @Test
    fun failoverIntrospectorHandlingExceptionWhenDeclaring() {
        val sut = FixtureMonkey.builder()
            .pushExactTypeArbitraryIntrospector<UUID>(
                FailoverIntrospector(
                    listOf(
                        ConstructorPropertiesArbitraryIntrospector.INSTANCE,
                        FactoryMethodArbitraryIntrospector(
                            FactoryMethodArbitraryIntrospector.FactoryMethodWithParameterNames(
                                UUID::randomUUID.javaMethod,
                                listOf(),
                            ),
                        ),
                    ),
                ),
            )
            .build()

        val actual = sut.giveMeOne<UUID>()

        then(actual).isNotNull
    }

    @Test
    fun setChild() {
        open class Parent(val parent: String)
        class Child : Parent("parent")

        val actual = SUT.giveMeBuilder<Parent>()
            .set(Child())
            .sample()
            .parent

        then(actual).isEqualTo("parent")
    }

    @RepeatedTest(TEST_COUNT)
    fun customizePropertySet() {
        val actual = SUT.giveMeExperimentalBuilder<String>()
            .customizeProperty(typedRoot<String>()) {
                it.filter { str -> str.length > 5 }
                    .map { str -> str.substring(0..3) }
            }
            .sample()

        then(actual).hasSizeLessThanOrEqualTo(4)
    }

    @RepeatedTest(TEST_COUNT)
    fun customizePropertyFilter() {
        val now = Instant.now()
        val min = now.minus(365, ChronoUnit.DAYS)
        val max = now.plus(365, ChronoUnit.DAYS)

        val actual = SUT.giveMeExperimentalBuilder<Instant>()
            .customizeProperty(typedRoot<Instant>()) {
                it.filter { instant -> instant.isAfter(min) && instant.isBefore(max) }
            }
            .sample()

        then(actual).isBetween(min, max)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleCustomizedWildcardType() {
        // given
        class MapObject(val map: Map<String, *>)

        val expected = "test"

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .pushExactTypeArbitraryIntrospector<GeneratingWildcardType> {
                ArbitraryIntrospectorResult(
                    CombinableArbitrary.from(expected)
                )
            }
            .build()

        // when
        val actual = sut.giveMeBuilder<MapObject>()
            .sizeExp(MapObject::map, 1)
            .sample()
            .map
            .values
            .first()

        // then
        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun overwriteExistingType() {
        val expected = "string"
        val sut = FixtureMonkey.builder()
            .pushExactTypeArbitraryIntrospector<String> {
                ArbitraryIntrospectorResult(
                    CombinableArbitrary.from(expected)
                )
            }
            .build()

        val actual: String = sut.giveMeOne()

        then(actual).isEqualTo(expected)
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
}
