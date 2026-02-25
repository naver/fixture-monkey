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

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin
import net.jqwik.api.Example
import org.assertj.core.api.BDDAssertions.then
import java.util.UUID

class FixtureMonkeyKotlinJacksonTest {
    private val sut: FixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JacksonPlugin())
        .build()

    @Example
    fun setParentPrivateValue() {
        val actual = this.sut.giveMeBuilder<SampleKotlin>()
            .set("id", "id")
            .sample()

        then(actual.getId()).isEqualTo("id")
    }

    data class SampleKotlin(
        val intValue: Int,
        val stringValue: String
    ) : Parent()

    abstract class Parent {
        private var id: String = UUID.randomUUID().toString()

        fun getId(): String {
            return id
        }
    }
}
