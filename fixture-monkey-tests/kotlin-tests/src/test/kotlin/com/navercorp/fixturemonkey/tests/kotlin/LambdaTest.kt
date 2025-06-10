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
import com.navercorp.fixturemonkey.customizer.Values
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setExp
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenNoException
import org.junit.jupiter.api.Test

class LambdaTest {
    @Test
    fun kotlinLambda() {
        // given
        class KotlinLambdaValue(val lambda: (String, String, String) -> Unit)

        // when
        val actual: KotlinLambdaValue = SUT.giveMeOne()

        then(actual.lambda).isNotNull
    }

    @Test
    fun kotlinLambdaOnlyReturnType() {
        // given
        class KotlinLambdaValue(val lambda: () -> String)

        // when
        val actual: KotlinLambdaValue = SUT.giveMeOne()

        then(actual.lambda.invoke()).isNotNull
    }

    @Test
    fun decomposeKotlinLambdaOnlyReturnType() {
        // given
        class KotlinLambdaValue(val lambda: () -> String)

        // when
        val actual = SUT.giveMeBuilder<KotlinLambdaValue>()
            .thenApply { _, _ -> }
            .sample()

        then(actual.lambda.invoke()).isNotNull
    }

    @Test
    fun kotlinLambdaReturnType() {
        // given
        class KotlinLambdaValue(val lambda: (String) -> String)

        // when
        val actual: KotlinLambdaValue = SUT.giveMeOne()

        then(actual.lambda.invoke("test")).isNotNull
    }

    @Test
    fun decomposeKotlinLambdaReturnType() {
        // given
        class KotlinLambdaValue(val lambda: (String) -> String)

        // when
        val actual = SUT.giveMeBuilder<KotlinLambdaValue>()
            .thenApply { _, _ -> }
            .sample()

        then(actual.lambda.invoke("test")).isNotNull
    }

    @Test
    fun setJustKotlinLambda() {
        // given
        class KotlinLambdaValue(val lambda: (String, String, String) -> Unit)

        val expected: (String, String, String) -> Unit = { _, _, _ -> }

        // when
        val actual = SUT.giveMeBuilder<KotlinLambdaValue>()
            .setExp(KotlinLambdaValue::lambda, Values.just(expected))
            .sample()

        then(actual.lambda).isNotNull
    }

    @Test
    fun sampleFunctionalObject() {
        data class FunctionObject(
            val value: () -> Int
        )

        val actual = SUT.giveMeOne<FunctionObject>().value()

        then(actual).isNotNull()
    }

    @Test
    fun toStringFunctionalObjectNotThrows() {
        data class FunctionObject(
            val value: () -> Int
        )

        thenNoException()
            .isThrownBy { SUT.giveMeOne<FunctionObject>().toString() }
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
} 