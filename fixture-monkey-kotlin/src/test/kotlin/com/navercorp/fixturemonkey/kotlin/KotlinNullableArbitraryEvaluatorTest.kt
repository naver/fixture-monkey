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
import net.jqwik.api.Property
import org.assertj.core.api.BDDAssertions.then
import javax.validation.constraints.NotNull

class KotlinNullableArbitraryEvaluatorTest {
    private val fixture: FixtureMonkey = KFixtureMonkeyBuilder().build()

    @Property
    fun generate() {
        val actual = this.fixture.giveMe(KotlinDataObject::class.java, 10)
        actual.forEach {
            then(it.name).isNotNull
            then(it.address).isNotNull
        }
    }
}

class KotlinDataObject {
    @field:NotNull var name: String? = null
    var description: String? = null
    @field:NotNull var address: String? = null
}
