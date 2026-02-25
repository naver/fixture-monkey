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

package com.navercorp.fixturemonkey.kotlin.test

import com.fasterxml.jackson.annotation.JsonProperty
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setExp
import com.navercorp.fixturemonkey.kotlin.setExpGetter
import net.jqwik.api.Example
import org.assertj.core.api.BDDAssertions.then

class KotlinExpJacksonPropertyTest {
    private val sut: FixtureMonkey = FixtureMonkey.builder()
        .plugin(JacksonPlugin())
        .plugin(KotlinPlugin())
        .build()

    @Example
    fun setExpJsonPropertyName() {
        val stringValue: String = sut.giveMeOne()
        val actual = sut.giveMeBuilder<JsonPropertyDataValue>()
            .setExp(JsonPropertyDataValue::stringValue, stringValue)
            .sample()
        then(actual.stringValue).isEqualTo(stringValue)
    }

    @Example
    fun setExpGetterJsonPropertyName() {
        val intValue: Int = sut.giveMeOne()
        val actual = sut.giveMeBuilder<JsonPropertyDataValue>()
            .setExpGetter(JsonPropertyDataValue::getInt, intValue)
            .sample()
        then(actual.intValue).isEqualTo(intValue)
    }
}

data class JsonPropertyDataValue(
    var intValue: Int,

    @field:JsonProperty("string")
    val stringValue: String
) {
    @JsonProperty("intValue")
    fun getInt() = this.intValue
}
