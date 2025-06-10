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
import com.navercorp.fixturemonkey.api.introspector.AnonymousArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.FactoryMethodArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.introspector.PrimaryConstructorArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.pushExactTypeArbitraryIntrospector
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import com.navercorp.fixturemonkey.tests.kotlin.BuilderJavaTestSpecs.BuilderObjectCustomBuildName
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.sql.Timestamp
import java.util.UUID
import kotlin.reflect.jvm.javaMethod

class IntrospectorTest {
    @Test
    fun constructorArbitraryIntrospectorWithoutPrimaryConstructor() {
        // given
        class ConstructorWithoutAnyAnnotations(val string: String)

        val sut = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build()

        // when
        val actual: ConstructorWithoutAnyAnnotations = sut.giveMeOne()

        // then
        then(actual).isNull()
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
    fun primaryConstructorArbitraryIntrospectorNotThrows() {
        val actual: Timestamp = SUT.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun beanArbitraryIntrospectorNotThrows() {
        val sut = FixtureMonkey.builder()
            .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
            .build()

        val actual: Timestamp = sut.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun fieldReflectionArbitraryIntrospectorNotThrows() {
        val sut = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build()

        val actual: Timestamp = sut.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun constructorPropertiesArbitraryIntrospectorNotThrows() {
        val sut = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build()

        val actual: Timestamp = sut.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun anonymousArbitraryIntrospectorNotThrows() {
        val sut = FixtureMonkey.builder()
            .objectIntrospector(AnonymousArbitraryIntrospector.INSTANCE)
            .build()

        val actual: Timestamp = sut.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun builderArbitraryIntrospectorNotThrows() {
        val sut = FixtureMonkey.builder()
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .build()

        val actual: Timestamp = sut.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun builderArbitraryIntrospectorMissBuildMethodNotThrows() {
        val sut = FixtureMonkey.builder()
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .build()

        val actual: BuilderObjectCustomBuildName = sut.giveMeOne()

        then(actual).isNull()
    }

    @RepeatedTest(TEST_COUNT)
    fun pushPrimaryConstructorIntrospector() {
        // given
        class StringObject(val string: String)

        val sut = FixtureMonkey.builder()
            .pushExactTypeArbitraryIntrospector<StringObject>(PrimaryConstructorArbitraryIntrospector.INSTANCE)
            .build()

        // when
        val actual = sut.giveMeOne<StringObject>().string

        // then
        then(actual).isNotNull
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .build()
    }
} 