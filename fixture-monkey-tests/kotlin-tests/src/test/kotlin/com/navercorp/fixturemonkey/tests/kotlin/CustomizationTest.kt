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
import com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedRoot
import com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedString
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.type.Types.GeneratingWildcardType
import com.navercorp.fixturemonkey.customizer.InnerSpec
import com.navercorp.fixturemonkey.customizer.Values
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.get
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeExperimentalBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.into
import com.navercorp.fixturemonkey.kotlin.pushExactTypeArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.register
import com.navercorp.fixturemonkey.kotlin.setExp
import com.navercorp.fixturemonkey.kotlin.sizeExp
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import com.navercorp.fixturemonkey.tests.kotlin.specs.KotlinObject
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class CustomizationTest {
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

    @Test
    fun setToObject() {
        // given
        class Object(val obj: Any?)

        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<Object>()
            .setExp(Object::obj, expected)
            .sample()
            .obj

        // then
        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun setLazyJustNotChanged() {
        // given
        class StringObject(val string: String)

        val expected = StringObject("test")

        // when
        val actual = SUT.giveMeBuilder<StringObject>()
            .setLazy("$") { Values.just(expected) }
            .setExp(StringObject::string, "notTest")
            .sample()

        // then
        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun setPostCondition() {
        class StringObject(val string: String)

        val actual = SUT.giveMeKotlinBuilder<StringObject>()
            .setPostCondition<String>("string") { it.length < 5 }
            .sample()
            .string

        then(actual).hasSizeLessThan(5)
    }

    @RepeatedTest(TEST_COUNT)
    fun setPostConditionWithProperty() {
        class StringObject(val string: String)

        val actual = SUT.giveMeKotlinBuilder<StringObject>()
            .setPostCondition<String>(StringObject::string) { it.length < 5 }
            .sample()
            .string

        then(actual).hasSizeLessThan(5)
    }

    @RepeatedTest(TEST_COUNT)
    fun customizePropertyAfterSet() {
        // given
        class StringValue(val value: String)

        val expected = "abc"

        // when
        val actual = SUT.giveMeKotlinBuilder<StringValue>()
            .setExp(StringValue::value, "abcdef")
            .customizeProperty(typedString<String>("value")) {
                it.map { str -> str.substring(0..2) }
            }
            .sample()
            .value

        //then
        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun customizePropertyIgnoredIfSet() {
        // given
        class StringValue(val value: String)

        val expected = "fixed"

        // when
        val actual = SUT.giveMeKotlinBuilder<StringValue>()
            .customizeProperty(typedString<String>("value")) {
                it.filter { value -> value.length > 5 }
            }
            .setExp(StringValue::value, expected)
            .sample()
            .value

        //then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun registerAssignableType() {
        // given
        open class Parent(val parent: String)

        class Child(parent: String) : Parent(parent)

        val expected = "registered"
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .register {
                it.giveMeBuilder<Parent>()
                    .setExp(Parent::parent, expected)
            }
            .build()

        // when
        val actual = sut.giveMeOne<Child>().parent

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun registerKotlinTypeBuilder() {
        val expected = "test"
        val sut = FixtureMonkey.builder()
            .register(String::class.java) {
                it.giveMeKotlinBuilder(expected)
            }
            .build()
        val actual: String = sut.giveMeOne()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setAndRegister() {
        // given
        class ListStringObject(val list: List<String>)
        class ListStringObjectObject(val value: ListStringObject)

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .register(ListStringObjectObject::class.java) {
                it.giveMeBuilder<ListStringObjectObject>()
                    .size("value.list", 3)
            }
            .build()

        val set = ListStringObject(listOf("a", "b"))

        // when
        val actual = sut.giveMeBuilder<ListStringObjectObject>()
            .set("value", set)
            .sample()
            .value
            .list

        // then
        val expected = set.list
        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun register() {
        class NestedInnerObject(val list: List<String>)

        class InnerObject(val list: List<NestedInnerObject>)

        class WrapperObject(val obj: InnerObject)

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .register(WrapperObject::class.java) {
                it.giveMeKotlinBuilder<WrapperObject>()
                    .sizeExp(WrapperObject::obj into InnerObject::list, 1)
                    .thenApply { _, _ -> }
            }
            .build()

        val actual = sut.giveMeKotlinBuilder<WrapperObject>()
            .sizeExp(WrapperObject::obj into InnerObject::list[0] into NestedInnerObject::list, 1)
            .sample()
            .obj
            .list[0]
            .list[0]

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun innerSpecInner() {
        // given
        data class ChildObject(val values: List<String>)

        data class ParentObject(val list: List<ChildObject>)

        // when
        val actual = SUT.giveMeKotlinBuilder<ParentObject>()
            .setInner {
                property("list") { l ->
                    l.size(1)
                        .listElement(0) { e ->
                            e.inner(
                                InnerSpec()
                                    .property("values") { v -> v.size(5) }
                            )
                        }
                }
            }.sample().list[0].values

        // then
        then(actual).hasSize(5)
    }

    @Test
    fun parentRegisterThenApplyAndSizeReturnsChildRegister() {
        data class ChildObject(val value: String)

        data class ParentObject(val values: List<ChildObject>)

        val sut = FixtureMonkey.builder()
            .register { it.giveMeKotlinBuilder<ParentObject>().thenApply { _, _ -> } }
            .register { it.giveMeKotlinBuilder<ChildObject>().set(ChildObject::value, "1") }
            .plugin(KotlinPlugin())
            .build()

        val actual = sut.giveMeKotlinBuilder<ParentObject>()
            .size(ParentObject::values, 10)
            .sample()
            .values

        then(actual).allMatch { it.value == "1" }
    }

    @RepeatedTest(TEST_COUNT)
    fun generateObjectInstanceThrows() {
        // when
        val actual: KotlinObject = SUT.giveMeOne()

        then(actual).isInstanceOf(KotlinObject::class.java)
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
} 
