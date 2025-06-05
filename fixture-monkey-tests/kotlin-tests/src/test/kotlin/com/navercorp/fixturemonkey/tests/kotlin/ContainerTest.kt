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
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class ContainerTest {
    @Test
    fun setShrinkContainerNode() {
        val expected = listOf("a")

        val actual = SUT.giveMeKotlinBuilder<List<String>>()
            .size("$", 3)
            .set("$[0]", "a1")
            .set("$[1]", "b")
            .set("$[2]", "c")
            .set(expected)
            .sample()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setExpandContainerNode() {
        val expected = listOf("a", "b", "c")

        val actual = SUT.giveMeKotlinBuilder<List<String>>()
            .size("$", 1)
            .set(expected)
            .sample()

        then(actual).isEqualTo(expected)
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
} 
