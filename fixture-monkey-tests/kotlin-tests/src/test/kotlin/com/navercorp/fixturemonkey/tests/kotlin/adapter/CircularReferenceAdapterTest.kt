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
import com.navercorp.fixturemonkey.kotlin.KotlinNodeTreeAdapterPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.tests.TestEnvironment.ADAPTER_TEST_COUNT
import com.navercorp.fixturemonkey.tests.kotlin.specs.CircularReferenceDefaultArgument
import com.navercorp.fixturemonkey.tests.kotlin.specs.CircularReferenceValueNullable
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest

class CircularReferenceAdapterTest {
    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun circularReferenceDefaultArgument() {
        val actual = SUT.giveMeOne<CircularReferenceDefaultArgument>().value

        then(actual).isNotNull()
    }

    @RepeatedTest(ADAPTER_TEST_COUNT)
    fun circularReferenceNullable() {
        val actual: CircularReferenceValueNullable = SUT.giveMeOne()

        then(actual).isNotNull()
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(KotlinNodeTreeAdapterPlugin())
            .build()
    }
}
