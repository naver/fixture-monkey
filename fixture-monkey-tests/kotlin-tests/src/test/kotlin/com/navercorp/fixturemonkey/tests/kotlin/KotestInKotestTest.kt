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

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotest.KotestPlugin
import com.navercorp.fixturemonkey.kotest.checkAll
import com.navercorp.fixturemonkey.kotest.giveMeArb
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.kotest.property.forAll

class KotestInKotestTest : StringSpec({
    "checkAll" {
        SUT.checkAll { string: String, int: Int ->
            string shouldNotBeSameInstanceAs int
            string shouldBe string
        }
    }
    "checkAllObject" {
        SUT.checkAll { stringObject: StringObject ->
            stringObject.value shouldNotBe null
        }
    }
    "checkAllArbitraryBuilder" {
        SUT.checkAll { string: ArbitraryBuilder<List<String>> ->
            string
                .size("$", 3)
                .sample() shouldHaveSize 3
        }
    }
    "forAll" {
        forAll(SUT.giveMeArb<String> { it.set("test") }) { a ->
            a == "test"
        }
    }
}) {
    companion object {
        val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotestPlugin())
            .plugin(KotlinPlugin())
            .build()
    }

    data class StringObject(val value: String)
}
