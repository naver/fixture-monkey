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
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest

class InnerSpecTest {

    @RepeatedTest(TEST_COUNT)
    fun setInnerSpecByTrailingLambda() {
        val actual = SUT.giveMeKotlinBuilder<Map<String, String>>()
            .setInner {
                keys("key1", "key2")
                    .minSize(3)
            }
            .sample()

        then(actual).containsKeys("key1", "key2")
    }

    @RepeatedTest(TEST_COUNT)
    fun setKotlinInnerByTrailingLambda() {
        val actual = SUT.giveMeKotlinBuilder<Map<String, String>>()
            .setKotlinInner {
                keys("key1", "key2")
                minSize(3)
            }
            .sample()

        then(actual).containsKeys("key1", "key2")
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
}
