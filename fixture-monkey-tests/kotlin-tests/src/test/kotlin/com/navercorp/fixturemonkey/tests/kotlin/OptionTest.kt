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
import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator
import com.navercorp.fixturemonkey.api.constraint.JavaDateTimeConstraint
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetTime
import javax.validation.constraints.FutureOrPresent

class OptionTest {
    @RepeatedTest(TEST_COUNT)
    fun customizeJavaConstraintGenerators() {
        // given
        class InstantObject(@field:FutureOrPresent val value: Instant)

        val offset = OffsetTime.now().offset
        val thisYearInstant = Instant.now()

        val nextYear = LocalDateTime.now().plusYears(1L)
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(JavaxValidationPlugin())
            .pushJavaConstraintGeneratorCustomizer {
                object : JavaConstraintGenerator by it {
                    override fun generateDateTimeConstraint(context: ArbitraryGeneratorContext?): JavaDateTimeConstraint? {
                        val constraint = it.generateDateTimeConstraint(context)!!

                        return JavaDateTimeConstraint(
                            { constraint.min },
                            { nextYear }
                        )
                    }
                }
            }
            .build()

        // when
        val actual = sut.giveMeOne<InstantObject>().value

        // then
        val nextYearInstant = nextYear.toInstant(offset)
        then(actual).isBetween(thisYearInstant, nextYearInstant)
    }

    @RepeatedTest(TEST_COUNT)
    fun customizeJavaConstraintGeneratorsTwice() {
        // given
        class InstantObject(@field:FutureOrPresent val value: Instant)

        val offset = OffsetTime.now().offset
        val thisYear = LocalDateTime.now()
        val thisYearInstant = Instant.now()

        val nextYear = LocalDateTime.now().plusYears(1L)
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .pushJavaConstraintGeneratorCustomizer {
                object : JavaConstraintGenerator by it {
                    override fun generateDateTimeConstraint(context: ArbitraryGeneratorContext?): JavaDateTimeConstraint {
                        return JavaDateTimeConstraint(
                            null
                        ) { nextYear }
                    }
                }
            }
            .pushJavaConstraintGeneratorCustomizer {
                object : JavaConstraintGenerator by it {
                    override fun generateDateTimeConstraint(context: ArbitraryGeneratorContext?): JavaDateTimeConstraint {
                        val constraint = it.generateDateTimeConstraint(context)!!
                        return JavaDateTimeConstraint(
                            { thisYear },
                            { constraint.max }
                        )
                    }
                }
            }
            .build()

        // when
        val actual = sut.giveMeOne<InstantObject>().value

        // then
        val nextYearInstant = nextYear.toInstant(offset)
        then(actual).isBetween(thisYearInstant, nextYearInstant)
    }
}
