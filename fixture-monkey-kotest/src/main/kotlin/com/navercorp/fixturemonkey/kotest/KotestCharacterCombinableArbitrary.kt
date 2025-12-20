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
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.single
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import java.util.function.Predicate

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
class KotestCharacterCombinableArbitrary(
    private val arb: Arb<Char> = DEFAULT_CHAR_ARB
) : CharacterCombinableArbitrary {
    override fun combined(): Char = arb.single()

    override fun rawValue(): Char = this.combined()

    override fun withRange(min: Char, max: Char): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(charRange(min, max))

    override fun alphabetic(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.choice(UPPERCASE_RANGE_ARB, LOWERCASE_RANGE_ARB))

    override fun numeric(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(NUMERIC_RANGE_ARB)

    override fun alphaNumeric(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.choice(UPPERCASE_RANGE_ARB, LOWERCASE_RANGE_ARB, NUMERIC_RANGE_ARB))

    override fun ascii(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(charRange('\u0000', '\u007F'))

    override fun uppercase(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(UPPERCASE_RANGE_ARB)

    override fun lowercase(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(LOWERCASE_RANGE_ARB)

    override fun korean(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(charRange('가', '힣'))

    override fun emoji(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.of(EMOJI_CHAR_CANDIDATES))

    override fun whitespace(): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(Arb.of(WHITESPACE_CHAR_CANDIDATES))

    override fun filter(tries: Int, predicate: Predicate<Char>): CharacterCombinableArbitrary =
        KotestCharacterCombinableArbitrary(arb.filter { predicate.test(it) })

    override fun clear() {
    }

    override fun fixed(): Boolean = false

    companion object {
        private val DEFAULT_CHAR_ARB: Arb<Char> = charRange(Char.MIN_VALUE, Char.MAX_VALUE)
        private val UPPERCASE_RANGE_ARB: Arb<Char> = charRange('A', 'Z')
        private val LOWERCASE_RANGE_ARB: Arb<Char> = charRange('a', 'z')
        private val NUMERIC_RANGE_ARB: Arb<Char> = charRange('0', '9')
        private val EMOJI_CHAR_CANDIDATES: List<Char> = listOf(
            '\u263A', // ☺
            '\u263B', // ☻
            '\u2661', // ♡
            '\u2665', // ♥
            '\u2660', // ♠
            '\u2663', // ♣
            '\u2666', // ♦
            '\u2600', // ☀
            '\u2601', // ☁
            '\u2602', // ☂
            '\u2603', // ☃
            '\u2604', // ☄
            '\u2614', // ☔
            '\u2615', // ☕
            '\u260E', // ☎
            '\u2618', // ☘
            '\u2728', // ✨
            '\u2764', // ❤
            '\u2708', // ✈
            '\u270C' // ✌
        )
        private val WHITESPACE_CHAR_CANDIDATES: List<Char> = listOf(
            ' ',
            '\t',
            '\n',
            '\r',
            '\u000B', // vertical tab
            '\u000C' // form feed
        )

        private fun charRange(startInclusive: Char, endInclusive: Char): Arb<Char> {
            if (startInclusive == endInclusive) {
                return Arb.of(startInclusive)
            }
            val (min, max) = if (startInclusive <= endInclusive) {
                startInclusive to endInclusive
            } else {
                endInclusive to startInclusive
            }
            return Arb.int(min.code..max.code).map(Int::toChar)
        }
    }
}
