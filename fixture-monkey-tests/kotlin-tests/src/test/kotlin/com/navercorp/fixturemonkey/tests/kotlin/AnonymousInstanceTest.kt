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
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setExpGetter
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import com.navercorp.fixturemonkey.tests.kotlin.JavaAnonymousInstanceTestSpecs.InheritedJavaInterface
import com.navercorp.fixturemonkey.tests.kotlin.JavaAnonymousInstanceTestSpecs.InheritedJavaInterfaceWithSameNameMethod
import com.navercorp.fixturemonkey.tests.kotlin.JavaAnonymousInstanceTestSpecs.InheritedTwoJavaInterface
import com.navercorp.fixturemonkey.tests.kotlin.JavaAnonymousInstanceTestSpecs.JavaAnnotatedInterface
import com.navercorp.fixturemonkey.tests.kotlin.JavaAnonymousInstanceTestSpecs.JavaContainerInterface
import com.navercorp.fixturemonkey.tests.kotlin.JavaAnonymousInstanceTestSpecs.JavaGetterInterface
import com.navercorp.fixturemonkey.tests.kotlin.JavaAnonymousInstanceTestSpecs.JavaInterface
import com.navercorp.fixturemonkey.tests.kotlin.JavaAnonymousInstanceTestSpecs.JavaInterfaceWithConstant
import com.navercorp.fixturemonkey.tests.kotlin.JavaAnonymousInstanceTestSpecs.JavaInterfaceWithParams
import com.navercorp.fixturemonkey.tests.kotlin.JavaAnonymousInstanceTestSpecs.NestedInheritedJavaInterface
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import javax.validation.constraints.NotEmpty

class AnonymousInstanceTest {
    @Test
    fun sampleInterface() {
        val actual = SUT.giveMeOne<Interface>()

        then(actual.string()).isNotNull
        then(actual.integer()).isNotNull
    }

    @Test
    fun objectBaseMethods() {
        val actual = SUT.giveMeOne<Interface>()

        then(actual.hashCode()).isNotNull
        then(actual).isEqualTo(actual)
        then(actual.toString()).isNotNull()
    }

    @Test
    fun equalsOnSimilarInterface() {
        val one = SUT.giveMeBuilder<Interface>()
            .setExpGetter(Interface::string, "test")
            .setExpGetter(Interface::integer, 123)
            .sample()
        val another = SUT.giveMeBuilder<SimilarInterface>()
            .setExpGetter(SimilarInterface::string, "test")
            .setExpGetter(SimilarInterface::integer, 123)
            .sample()

        then(one).isNotEqualTo(another)
    }

    @Test
    fun setInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<Interface>()
            .setExpGetter(Interface::string, expected)
            .sample()
            .string()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun sampleInterfaceWithParam() {
        val actual = SUT.giveMeOne<InterfaceWithParams>()

        then(actual).isNull()
    }

    @Test
    fun sampleContainerInterface() {
        val actual = SUT.giveMeOne<ContainerInterface>()

        then(actual.list()).isNotNull
        then(actual.map()).isNotNull
    }

    @Test
    fun setContainerInterfaceList() {
        val actual: List<String> = SUT.giveMeBuilder<ContainerInterface>()
            .size("list", 3)
            .set("list[0]", "test")
            .sample()
            .list()

        then(actual).hasSize(3)
        then(actual[0]).isEqualTo("test")
    }

    @Test
    fun sampleAnnotatedInterface() {
        val actual = SUT.giveMeOne<AnnotatedInterface>().string()

        then(actual).isNotEmpty
    }

    @Test
    fun sampleGetterInterface() {
        val actual = SUT.giveMeOne<GetterInterface>().getString()

        then(actual).isNotNull
    }

    @Test
    fun setGetterInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<GetterInterface>()
            .set("string", expected)
            .sample()
            .getString()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun samplePropertyInterface() {
        val actual = SUT.giveMeOne<PropertyInterface>()

        then(actual.string).isNotNull
        then(actual.int).isNotNull
    }

    @Test
    fun setPropertyInterface() {
        val expected = "expected"

        val actual = SUT.giveMeBuilder<PropertyInterface>()
            .set("string", expected)
            .sample()
            .string

        then(actual).isEqualTo(expected)
    }

    @Test
    fun sampleInheritedInterface() {
        val actual = SUT.giveMeOne<InheritedInterface>()

        then(actual.value()).isNotNull
        then(actual.string()).isNotNull
        then(actual.integer()).isNotNull
    }

    @Test
    fun setInheritedInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<InheritedInterface>()
            .set("value", expected)
            .sample()
            .value()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun sampleInheritedInterfaceWithOverrideMethod() {
        val actual = SUT.giveMeOne<InheritedInterfaceWithOverrideMethod>().string()

        then(actual).isNotNull
    }

    @Test
    fun setInheritedInterfaceWithOverrideMethod() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<InheritedInterfaceWithOverrideMethod>()
            .set("string", expected)
            .sample()
            .string()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun sampleExtendsTwoInterface() {
        val actual = SUT.giveMeOne<InheritedTwoInterface>().string()

        then(actual).isNotNull
    }

    @Test
    fun sampleNestedInheritedInterface() {
        val actual = SUT.giveMeOne<NestedInheritedInterface>().string()

        then(actual).isNotNull
    }

    @Test
    fun setNestedInheritedInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<NestedInheritedInterface>()
            .set("string", expected)
            .sample()
            .string()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun sampleInheritedPropertyInterface() {
        val actual = SUT.giveMeOne<InheritedPropertyInterface>().string

        then(actual).isNotNull
    }

    @Test
    fun setInheritedPropertyInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<InheritedPropertyInterface>()
            .set("string", expected)
            .sample()
            .string

        then(actual).isEqualTo(expected)
    }

    @Test
    fun sampleNestedInheritedPropertyInterface() {
        val actual = SUT.giveMeOne<NestedInheritedPropertyInterface>().string

        then(actual).isNotNull
    }

    @Test
    fun setNestedInheritedPropertyInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<NestedInheritedPropertyInterface>()
            .set("string", expected)
            .sample()
            .string

        then(actual).isEqualTo(expected)
    }

    @Test
    fun sampleJavaInterface() {
        val actual = SUT.giveMeOne<JavaInterface>()

        then(actual).isNotNull
        then(actual.string()).isNotNull
        then(actual.integer()).isNotNull
    }

    @Test
    fun setJavaInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<JavaInterface>()
            .set("string", expected)
            .sample()
            .string()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun sampleJavaInterfaceWithParamReturnsNullProperties() {
        val actual = SUT.giveMeOne<JavaInterfaceWithParams>()

        then(actual).isNull()
    }

    @Test
    fun sampleJavaInterfaceWithConstantIsNull() {
        val actual = SUT.giveMeOne<JavaInterfaceWithConstant>()

        then(actual).isNull()
    }

    @Test
    fun sampleJavaContainerInterface() {
        val actual = SUT.giveMeOne<JavaContainerInterface>()

        then(actual.list()).isNotNull
        then(actual.map()).isNotNull
    }

    @Test
    fun setJavaContainerInterfaceList() {
        val actual = SUT.giveMeBuilder<JavaContainerInterface>()
            .size("list", 3)
            .set("list[0]", "test")
            .sample()
            .list()

        then(actual).hasSize(3)
        then(actual[0]).isEqualTo("test")
    }

    @Test
    fun sampleJavaAnnotatedInterface() {
        val actual = SUT.giveMeOne<JavaAnnotatedInterface>().string()

        then(actual).isNotEmpty
    }

    @Test
    fun setJavaGetterInterfacePropertyName() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<JavaGetterInterface>()
            .set("value", expected)
            .sample()
            .value

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setJavaGetterInterfaceMethodNameNotWorks() {
        val notExpected = "test"

        val actual = SUT.giveMeBuilder<JavaGetterInterface>()
            .set("getValue", notExpected)
            .sample()
            .value

        then(actual).isNotEqualTo(notExpected)
    }

    @Test
    fun sampleInheritedJavaInterface() {
        val actual = SUT.giveMeOne<InheritedJavaInterface>()

        then(actual.value()).isNotNull
        then(actual.string()).isNotNull
        then(actual.integer()).isNotNull
    }

    @Test
    fun setInheritedJavaInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<InheritedJavaInterface>()
            .set("value", expected)
            .sample()
            .value()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun sampleInheritedJavaInterfaceWithSameMethodName() {
        val actual = SUT.giveMeOne<InheritedJavaInterfaceWithSameNameMethod>().string()

        then(actual).isNotNull
    }

    @Test
    fun setInheritedJavaInterfaceWithSameMethodName() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<InheritedJavaInterfaceWithSameNameMethod>()
            .set("string", expected)
            .sample()
            .string()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun sampleInheritedTwoJavaInterface() {
        val actual = SUT.giveMeOne<InheritedTwoJavaInterface>()

        then(actual.integer()).isNotNull
        then(actual.string()).isNotNull
        then(actual.list()).isNotNull
        then(actual.map()).isNotNull
    }

    @Test
    fun sampleNestedInheritedJavaInterface() {
        val actual = SUT.giveMeOne<NestedInheritedJavaInterface>().string()

        then(actual).isNotNull
    }

    @Test
    fun setNestedInheritedJavaInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<NestedInheritedJavaInterface>()
            .set("string", expected)
            .sample()
            .string()

        then(actual).isEqualTo(expected)
    }

    interface Interface {
        fun string(): String
        fun integer(): Int
    }

    interface SimilarInterface {
        fun string(): String
        fun integer(): Int
    }

    interface InterfaceWithParams {
        fun string(str: String): String?
        fun integer(int: Int): Int?
    }

    interface ContainerInterface {
        fun list(): List<String>
        fun map(): Map<String, Int>
    }

    interface AnnotatedInterface {
        @NotEmpty
        fun string(): String
    }

    interface GetterInterface {
        fun getString(): String
    }

    interface PropertyInterface {
        val string: String

        val int: Int
    }

    interface InheritedInterface : Interface {
        fun value(): String
    }

    interface InheritedInterfaceWithOverrideMethod : Interface {
        override fun string(): String
    }

    interface InheritedTwoInterface : Interface, ContainerInterface

    interface NestedInheritedInterface : InheritedInterfaceWithOverrideMethod, Interface

    interface InheritedPropertyInterface : PropertyInterface {
        override val string: String
    }

    interface NestedInheritedPropertyInterface : InheritedPropertyInterface, PropertyInterface

    companion object {
        val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(JavaxValidationPlugin())
            .plugin(InterfacePlugin())
            .defaultNotNull(true)
            .build()
    }
}
