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

import com.navercorp.fixturemonkey.api.arbitrary.CharacterCombinableArbitrary
import io.kotest.property.Arb
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.single
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import java.util.function.Predicate

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
class KotestCharacterCombinableArbitrary(private val arb: Arb<Char> = Arb.char()) : CharacterCombinableArbitrary {
    override fun combined(): Char = arb.single()

    override fun rawValue(): Char = this.combined()

    override fun withRange(min: Char, max: Char): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.char().filter { it in min..max })

    override fun alphabetic(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.char().filter { it.isLetter() })

    override fun numeric(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.char().filter { it.isDigit() })

    override fun alphaNumeric(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.char().filter { it.isLetterOrDigit() })

    override fun ascii(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.char().filter { it.code <= 127 })

    override fun uppercase(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.char().filter { it.isUpperCase() })

    override fun lowercase(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.char().filter { it.isLowerCase() })

    override fun korean(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.char().filter { it in '가'..'힣' })

    private fun isEmojiCodePoint(codePoint: Int): Boolean {
        return codePoint in 0x1F600..0x1F64F || // Emoticons
            codePoint in 0x1F300..0x1F5FF || // Misc Symbols and Pictographs
            codePoint in 0x1F680..0x1F6FF || // Transport and Map
            codePoint in 0x2600..0x26FF || // Misc symbols
            codePoint in 0x2700..0x27BF // Dingbats
    }

    override fun emoji(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.char().filter { isEmojiCodePoint(it.code) })

    override fun whitespace(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.char().filter { it.isWhitespace() })

    override fun filter(tries: Int, predicate: Predicate<Char>): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.char().filter { predicate.test(it) })

    override fun clear() {
    }

    override fun fixed(): Boolean = false
}
