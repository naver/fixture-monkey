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

package com.navercorp.fixturemonkey.kotlin

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.util.function.Consumer

class FixtureMonkeyBuilderExtensionsTest {

    @Test
    fun addCustomizer() {
        // when
        val sut = KFixtureMonkeyBuilder()
            .addCustomizer<IntegerStringWrapperClass> {
                it?.copy(
                    intValue = -1,
                    stringValue = "test_value",
                )
            }.build()
        val actual = sut.giveMeOne<IntegerStringWrapperClass>()

        then(actual).satisfies(
            Consumer {
                with(it) {
                    then(intValue).isEqualTo(-1)
                    then(stringValue).isEqualTo("test_value")
                }
            }
        )
    }

    data class IntegerStringWrapperClass(
        val intValue: Int,
        val stringValue: String,
    )
}
