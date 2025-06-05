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
import com.navercorp.fixturemonkey.api.expression.TypedExpressionGenerator
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin
import com.navercorp.fixturemonkey.javax.validation.validator.JavaxArbitraryValidator
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import javax.validation.Valid
import javax.validation.constraints.Size

class ValidationTest {
    @Test
    fun sizeZero() {
        // given
        class StringWithSizeZero(
            @field:Size(min = 0, max = 0)
            val value: String,
        )

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(JavaxValidationPlugin())
            .build()

        // when
        val actual = sut.giveMeOne<StringWithSizeZero>().value

        // then
        then(actual).isEqualTo("")
    }

    @Test
    fun customizerValidOnly() {
        // given
        class StringWithSizeZero(
            @field:Size(min = 0, max = 0)
            val value: String,
        )

        class Wrapper(@field:Valid val value: StringWithSizeZero)

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin { it.defaultArbitraryValidator(JavaxArbitraryValidator()) }
            .pushCustomizeValidOnly(
                { it.annotations.any { annotation -> annotation.annotationClass == Size::class } },
                false
            )
            .build()

        // when
        val actual = sut.giveMeBuilder<Wrapper>()
            .customizeProperty(TypedExpressionGenerator.typedString<String>("value.value")) { it.filter { str -> str.length > 1 } }
            .sample()

        // then
        then(actual).isNotNull
    }

    @Test
    fun customizerValidOnlyBuilderValidOnlyFirst() {
        // given
        class StringWithSizeZero(
            @field:Size(min = 0, max = 0)
            val value: String,
        )

        class Wrapper(@field:Valid val value: StringWithSizeZero)

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin { it.defaultArbitraryValidator(JavaxArbitraryValidator()) }
            .pushCustomizeValidOnly(
                { it.annotations.any { annotation -> annotation.annotationClass == Size::class } },
                true
            )
            .build()

        // when
        val actual = sut.giveMeBuilder<Wrapper>()
            .customizeProperty(TypedExpressionGenerator.typedString<String>("value.value")) { it.filter { str -> str.length > 1 } }
            .validOnly(false)
            .sample()

        // then
        then(actual).isNotNull
    }
} 