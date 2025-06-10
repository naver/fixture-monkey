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

package com.navercorp.fixturemonkey.kotest

import com.navercorp.fixturemonkey.api.arbitrary.StringCombinableArbitrary
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.ascii
import io.kotest.property.arbitrary.codepoints
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.string
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import java.util.function.Predicate

@API(since = "1.1.12", status = EXPERIMENTAL)
class KotestStringCombinableArbitrary(private val arb: Arb<String> = Arb.string()) : StringCombinableArbitrary {
    override fun combined(): String = arb.single()

    override fun rawValue(): String = arb.single()

    override fun filter(tries: Int, predicate: Predicate<String>): StringCombinableArbitrary =
        KotestStringCombinableArbitrary(arb.filter { predicate.test(it) })

    override fun withLength(min: Int, max: Int): StringCombinableArbitrary =
        KotestStringCombinableArbitrary(Arb.string(min..max))

    override fun alphabetic(): StringCombinableArbitrary = KotestStringCombinableArbitrary(
        Arb.string(
            codepoints = Arb.codepoints().filter { it.value.toChar() in 'a'..'z' || it.value.toChar() in 'A'..'Z' }
        )
    )

    override fun ascii(): StringCombinableArbitrary = KotestStringCombinableArbitrary(
        Arb.string(codepoints = Codepoint.ascii())
    )

    override fun numeric(): StringCombinableArbitrary = KotestStringCombinableArbitrary(
        Arb.string(
            codepoints = Arb.codepoints().filter { it.value.toChar() in '0'..'9' }
        )
    )

    override fun korean(): StringCombinableArbitrary = KotestStringCombinableArbitrary(
        Arb.string(
            codepoints = Arb.codepoints().filter { it.value.toChar() in '가'..'힣' }
        )
    )

    override fun filterCharacter(tries: Int, predicate: Predicate<Char>): StringCombinableArbitrary =
        KotestStringCombinableArbitrary(
            Arb.string(
                codepoints = Arb.codepoints().filter { predicate.test(it.value.toChar()) }
            )
        )

    override fun clear() {
    }

    override fun fixed(): Boolean = false

    companion object {
        private const val MAX_ASCII_CODEPOINT: Int = 0x007F
    }
}
