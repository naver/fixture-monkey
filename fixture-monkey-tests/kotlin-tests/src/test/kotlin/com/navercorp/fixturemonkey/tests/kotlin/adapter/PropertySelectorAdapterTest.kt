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

package com.navercorp.fixturemonkey.tests.kotlin.adapter

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.KotlinNodeTreeAdapterPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.expression.root
import com.navercorp.fixturemonkey.kotlin.get
import com.navercorp.fixturemonkey.kotlin.giveMeExperimentalBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.instantiator.instantiateBy
import com.navercorp.fixturemonkey.kotlin.into
import com.navercorp.fixturemonkey.kotlin.intoGetter
import com.navercorp.fixturemonkey.kotlin.pushExactTypeArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.setExp
import com.navercorp.fixturemonkey.kotlin.setExpGetter
import com.navercorp.fixturemonkey.kotlin.setNotNull
import com.navercorp.fixturemonkey.kotlin.setNull
import com.navercorp.fixturemonkey.kotlin.sizeExp
import com.navercorp.fixturemonkey.kotlin.sizeExpGetter
import com.navercorp.fixturemonkey.tests.TestEnvironment.ADAPTER_TEST_COUNT
import com.navercorp.fixturemonkey.tests.kotlin.ImmutableJavaTestSpecs.ArrayObject
import com.navercorp.fixturemonkey.tests.kotlin.ImmutableJavaTestSpecs.JavaStringObject
import com.navercorp.fixturemonkey.tests.kotlin.ImmutableJavaTestSpecs.NestedArrayObject
import com.navercorp.fixturemonkey.tests.kotlin.ImmutableJavaTestSpecs.RootJavaStringObject
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.ComplexObject
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.ComplexObjectObject
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.ListStringObject
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.SimpleObject
import com.navercorp.fixturemonkey.tests.kotlin.JavaConstructorTestSpecs.JavaTypeObject
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class PropertySelectorAdapterTest {
    @Test
    fun kotlinObjectUseJavaGetterThrows() {
        class KotlinObject(val value: String)

        thenThrownBy {
            SUT.giveMeKotlinBuilder<KotlinObject>()
                .set(javaGetter(KotlinObject::value), "test")
                .sample()
                .value
        }.cause()
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
        val actual = SUT.giveMeKotlinBuilder<ArrayObject>()
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
        val actual = SUT.giveMeKotlinBuilder<NestedArrayObject>()
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
        val actual = SUT.giveMeKotlinBuilder<ArrayObject>()
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
        val actual = SUT.giveMeKotlinBuilder<NestedArrayObject>()
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
    fun setRootExp() {
        val expected = "test"

        val actual = SUT.giveMeKotlinBuilder<String>()
            .set(String::root, expected)
            .sample()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setLazyRootExp() {
        val expected = "test"

        val actual = SUT.giveMeKotlinBuilder<String>()
            .setLazy(String::root) { expected }
            .sample()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setNullRootExp() {
        val actual = SUT.giveMeKotlinBuilder<String?>()
            .setNull(String::root)
            .sample()

        then(actual).isNull()
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setNotNullRootExp() {
        val actual = SUT.giveMeKotlinBuilder<String?>()
            .setNotNull(String::root)
            .sample()

        then(actual).isNotNull()
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun sizeRootExp() {
        val actual = SUT.giveMeKotlinBuilder<List<String>>()
            .size(List<String>::root, 1)
            .sample()

        then(actual).hasSize(1)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun sizeRangeRootExp() {
        val actual = SUT.giveMeKotlinBuilder<List<String>>()
            .size(List<String>::root, 1, 3)
            .sample()

        then(actual).hasSizeBetween(1, 3)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun minSizeRootExp() {
        val actual = SUT.giveMeKotlinBuilder<List<String>>()
            .minSize(List<String>::root, 1)
            .sample()

        then(actual).hasSizeGreaterThanOrEqualTo(1)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun maxSizeRootExp() {
        val actual = SUT.giveMeKotlinBuilder<List<String>>()
            .maxSize(List<String>::root, 1)
            .sample()

        then(actual).hasSizeLessThanOrEqualTo(1)
    }

    @Test
    fun setPostConditionRootExp() {
        val actual = SUT.giveMeKotlinBuilder<String>()
            .setPostCondition(String::root) { it.length < 5 }
            .sample()

        then(actual).hasSizeLessThan(5)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setRootElementExp() {
        val expected = "test"

        val actual = SUT.giveMeKotlinBuilder<List<String>>()
            .size(List<String>::root, 1)
            .set(List<String>::root[0], expected)
            .sample()

        then(actual[0]).isEqualTo(expected)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setRootArrayElementExp() {
        val expected = "test"

        val actual = SUT.giveMeKotlinBuilder<Array<String>>()
            .size(Array<String>::root, 1)
            .set(Array<String>::root[0], expected)
            .sample()

        then(actual[0]).isEqualTo(expected)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setRootAllElementExp() {
        val expected = "test"

        val actual = SUT.giveMeKotlinBuilder<List<String>>()
            .size(List<String>::root, 3)
            .set(List<String>::root["*"], expected)
            .sample()

        then(actual).allMatch { it == expected }
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setRootArrayAllElementExp() {
        val expected = "test"

        val actual = SUT.giveMeKotlinBuilder<Array<String>>()
            .size(Array<String>::root, 3)
            .set(Array<String>::root["*"], expected)
            .sample()

        then(actual).allMatch { it == expected }
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setRootNestedListElementExp() {
        val expected = "test"

        val actual = SUT.giveMeKotlinBuilder<List<List<String>>>()
            .size(List<List<String>>::root, 1)
            .size(List<List<String>>::root[0], 1)
            .set(List<List<String>>::root[0][0], expected)
            .sample()[0][0]

        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setRootNestedListAllElementExp() {
        val expected = "test"

        val actual = SUT.giveMeKotlinBuilder<List<List<String>>>()
            .size(List<List<String>>::root, 3)
            .size(List<List<String>>::root["*"], 3)
            .set(List<List<String>>::root["*"]["*"], expected)
            .sample()

        then(actual).allMatch { list -> list.all { it == expected } }
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setRootNestedArrayElementExp() {
        val expected = "test"

        val actual = SUT.giveMeKotlinBuilder<Array<Array<String>>>()
            .size(Array<Array<String>>::root, 1)
            .size(Array<Array<String>>::root[0], 1)
            .set(Array<Array<String>>::root[0][0], expected)
            .sample()[0][0]

        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setRootNestedArrayAllElementExp() {
        val expected = "test"

        val actual = SUT.giveMeKotlinBuilder<Array<Array<String>>>()
            .size(Array<Array<String>>::root, 3)
            .size(Array<Array<String>>::root["*"], 3)
            .set(Array<Array<String>>::root["*"]["*"], expected)
            .sample()
            .flatten()

        then(actual).allMatch { it == expected }
    }

    @Test
    fun setRootPropertyExp() {
        val expected = "expected"

        class StringObject(val string: String)

        val actual = SUT.giveMeKotlinBuilder<StringObject>()
            .set(StringObject::root into StringObject::string, expected)
            .sample()
            .string

        then(actual).isEqualTo(expected)
    }

    @Test
    fun typedKotlinPropertySelector() {
        // given
        class StringObject(val string: String)

        val expected = "test"

        // when
        val actual = SUT.giveMeKotlinBuilder<StringObject>()
            .customizeProperty(StringObject::string) {
                it.map { _ -> expected }
            }
            .sample()
            .string

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun typedKotlinPropertySelectorRecursively() {
        // given
        class StringObject(val string: String)

        val expected = "test"

        // when
        val actual = SUT.giveMeKotlinBuilder<StringObject>()
            .customizeProperty(StringObject::string) {
                it.map { _ -> expected }
            }.customizeProperty(StringObject::string) {
                it.map { _ -> expected }
            }
            .sample()

        // then
        then(actual.string).isEqualTo(expected)
    }

    @Test
    fun typedNestedKotlinPropertySelector() {
        // given
        class StringObject(val string: String)

        class NestedStringObject(val obj: StringObject)

        val expected = "test"

        // when
        val actual = SUT.giveMeKotlinBuilder<NestedStringObject>()
            .customizeProperty(NestedStringObject::obj into StringObject::string) {
                it.map { _ -> expected }
            }
            .sample()
            .obj
            .string

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun typedJavaPropertySelector() {
        // given
        val expected = "test"

        val sut = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .plugin(KotlinNodeTreeAdapterPlugin())
            .build();

        // when
        val actual = sut.giveMeKotlinBuilder<JavaStringObject>()
            .customizeProperty(JavaStringObject::getString) {
                it.map { _ -> expected }
            }
            .sample()
            .string

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun typedJavaPropertySelectorRecursively() {
        // given
        val expected = "test"

        val sut = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .plugin(KotlinNodeTreeAdapterPlugin())
            .build();

        // when
        val actual = sut.giveMeKotlinBuilder<JavaStringObject>()
            .customizeProperty(JavaStringObject::getString) {
                it.map { _ -> expected }
            }.customizeProperty(JavaStringObject::getString) {
                it.map { _ -> expected }
            }
            .sample()
            .string

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun typedRootIsKotlinNestedJavaPropertySelector() {
        // given
        class RootJavaStringObject(val obj: JavaStringObject)

        val expected = "test"

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .pushExactTypeArbitraryIntrospector<JavaStringObject>(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .plugin(KotlinNodeTreeAdapterPlugin())
            .build();

        // when
        val actual = sut.giveMeKotlinBuilder<RootJavaStringObject>()
            .customizeProperty(RootJavaStringObject::obj intoGetter JavaStringObject::getString) {
                it.map { _ -> expected }
            }
            .sample()
            .obj
            .string

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun typedRootIsJavaNestedJavaPropertySelector() {
        // given
        val expected = "test"

        val sut = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .plugin(KotlinNodeTreeAdapterPlugin())
            .build();

        // when
        val actual = sut.giveMeKotlinBuilder<RootJavaStringObject>()
            .customizeProperty(RootJavaStringObject::getObj intoGetter JavaStringObject::getString) {
                it.map { _ -> expected }
            }
            .sample()
            .obj
            .string

        // then
        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun threeLevelDeepSetShouldPreventAllAncestorNull() {
        // given
        val expected = "test"

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(KotlinNodeTreeAdapterPlugin())
            .defaultNullInjectGenerator { 1.0 }
            .build()

        // when
        val result = sut.giveMeKotlinBuilder<ComplexObjectObject>()
            .setExp(ComplexObjectObject::value into ComplexObject::value into SimpleObject::str, expected)
            .sample()

        // then
        then(result).isNotNull
        then(result.value).isNotNull
        then(result.value.value).isNotNull
        then(result.value.value.str).isEqualTo(expected)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setContainerElementShouldPreventParentNull() {
        // given
        val expected = "test"

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(KotlinNodeTreeAdapterPlugin())
            .defaultNullInjectGenerator { 1.0 }
            .build()

        // when
        val result = sut.giveMeKotlinBuilder<ListStringObject>()
            .sizeExp(ListStringObject::values, 1)
            .setExp(ListStringObject::values[0], expected)
            .sample()

        // then
        then(result).isNotNull
        then(result.values).isNotNull
        then(result.values[0]).isEqualTo(expected)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setMultipleSiblingPathsShouldPreventParentNull() {
        // given
        val expectedStr = "test"
        val expectedInt = 42

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(KotlinNodeTreeAdapterPlugin())
            .defaultNullInjectGenerator { 1.0 }
            .build()

        // when
        val result = sut.giveMeKotlinBuilder<ComplexObjectObject>()
            .setExp(ComplexObjectObject::value into ComplexObject::value into SimpleObject::str, expectedStr)
            .setExp(ComplexObjectObject::value into ComplexObject::value into SimpleObject::integer, expectedInt)
            .sample()

        // then
        then(result).isNotNull
        then(result.value).isNotNull
        then(result.value.value).isNotNull
        then(result.value.value.str).isEqualTo(expectedStr)
        then(result.value.value.integer).isEqualTo(expectedInt)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun sizeOnlyContainerShouldPreventParentNull() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(KotlinNodeTreeAdapterPlugin())
            .defaultNullInjectGenerator { 1.0 }
            .build()

        // when
        val result = sut.giveMeKotlinBuilder<ListStringObject>()
            .sizeExp(ListStringObject::values, 2)
            .sample()

        // then
        then(result).isNotNull
        then(result.values).isNotNull
        then(result.values).hasSize(2)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setNotNullNestedPathShouldPreventAncestorNull() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(KotlinNodeTreeAdapterPlugin())
            .defaultNullInjectGenerator { 1.0 }
            .build()

        // when
        val result = sut.giveMeKotlinBuilder<ComplexObjectObject>()
            .setNotNullExp(ComplexObjectObject::value into ComplexObject::value)
            .sample()

        // then
        then(result).isNotNull
        then(result.value).isNotNull
        then(result.value.value).isNotNull
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun customizePropertyNestedPathShouldPreventParentNull() {
        // given
        val expected = "test"

        val sut = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .plugin(KotlinPlugin())
            .plugin(KotlinNodeTreeAdapterPlugin())
            .defaultNullInjectGenerator { 1.0 }
            .build()

        // when
        val result = sut.giveMeKotlinBuilder<RootJavaStringObject>()
            .customizeProperty(RootJavaStringObject::getObj intoGetter JavaStringObject::getString) {
                it.map { _ -> expected }
            }
            .sample()

        // then
        then(result).isNotNull
        then(result.obj).isNotNull
        then(result.obj.string).isEqualTo(expected)
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun setExpChildPathShouldPreventParentNull() {
        // given
        val expected = "test"

        val sut = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .plugin(KotlinPlugin())
            .plugin(KotlinNodeTreeAdapterPlugin())
            .defaultNullInjectGenerator { 1.0 }
            .build()

        // when
        val result = sut.giveMeKotlinBuilder<RootJavaStringObject>()
            .setExp(RootJavaStringObject::getObj intoGetter JavaStringObject::getString, expected)
            .sample()

        // then
        then(result).isNotNull
        then(result.obj).isNotNull
        then(result.obj.string).isEqualTo(expected)
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(KotlinNodeTreeAdapterPlugin())
            .build()
    }
}
