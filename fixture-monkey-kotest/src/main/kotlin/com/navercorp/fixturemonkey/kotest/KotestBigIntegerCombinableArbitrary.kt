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

import com.navercorp.fixturemonkey.api.arbitrary.BigIntegerCombinableArbitrary
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigInt
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.single
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import java.math.BigInteger
import java.util.function.Predicate

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
class KotestBigIntegerCombinableArbitrary(
    private val arb: Arb<BigInteger> = Arb.bigInt(maxNumBits = DEFAULT_MAX_NUM_BITS)
) : BigIntegerCombinableArbitrary {

    override fun combined(): BigInteger = arb.single()

    override fun rawValue(): BigInteger = this.combined()

    override fun withRange(min: BigInteger, max: BigInteger): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(bigIntegersInRange(min, max))

    override fun positive(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(maxNumBits = DEFAULT_MAX_NUM_BITS).map(::toPositive)
        )

    override fun negative(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(maxNumBits = DEFAULT_MAX_NUM_BITS).map { toPositive(it).negate() }
        )

    override fun nonZero(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(maxNumBits = DEFAULT_MAX_NUM_BITS).map(::ensureNonZero)
        )

    override fun percentage(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.int(0..100).map { BigInteger.valueOf(it.toLong()) }
        )

    override fun score(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.int(0..100).map { BigInteger.valueOf(it.toLong()) }
        )

    override fun score(min: BigInteger, max: BigInteger): BigIntegerCombinableArbitrary =
        withRange(min, max)

    override fun even(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(maxNumBits = EVEN_MAX_NUM_BITS).map { it.shiftLeft(1) }
        )

    override fun odd(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(maxNumBits = EVEN_MAX_NUM_BITS).map { it.shiftLeft(1).add(BigInteger.ONE) }
        )

    override fun multipleOf(divisor: BigInteger): BigIntegerCombinableArbitrary =
        when {
            divisor == BigInteger.ZERO -> throw ArithmeticException("Division by zero")
            else -> KotestBigIntegerCombinableArbitrary(
                Arb.bigInt(maxNumBits = DEFAULT_MAX_NUM_BITS).map { it.multiply(divisor) }
            )
        }

    override fun prime(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.int(0..PRIME_CANDIDATES.lastIndex).map { PRIME_CANDIDATES[it] }
        )

    override fun filter(tries: Int, predicate: Predicate<BigInteger>): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(arb.filter(predicate::test))

    override fun clear() {
    }

    override fun fixed(): Boolean = false

    companion object {
        private const val DEFAULT_MAX_NUM_BITS = 32
        private const val EVEN_MAX_NUM_BITS = DEFAULT_MAX_NUM_BITS - 1

        private val PRIME_CANDIDATES: List<BigInteger> = buildList {
            val limit = 1000
            val isComposite = BooleanArray(limit + 1)
            for (candidate in 2..limit) {
                if (!isComposite[candidate]) {
                    add(BigInteger.valueOf(candidate.toLong()))
                    var multiple = candidate * 2
                    while (multiple <= limit) {
                        isComposite[multiple] = true
                        multiple += candidate
                    }
                }
            }
        }

        private fun toPositive(candidate: BigInteger): BigInteger {
            val absValue = candidate.abs()
            return if (absValue == BigInteger.ZERO) BigInteger.ONE else absValue
        }

        private fun ensureNonZero(candidate: BigInteger): BigInteger =
            if (candidate == BigInteger.ZERO) BigInteger.ONE else candidate

        private fun bigIntegersInRange(min: BigInteger, max: BigInteger): Arb<BigInteger> {
            if (min == max) {
                return Arb.constant(min)
            }

            val (lower, upper) = if (min <= max) min to max else max to min
            val span = upper.subtract(lower)
            val spanPlusOne = span.add(BigInteger.ONE)
            val bitLength = spanPlusOne.bitLength().coerceAtLeast(1)

            return Arb.bigInt(maxNumBits = bitLength).map { candidate ->
                val offset = candidate.abs().mod(spanPlusOne)
                lower.add(offset)
            }
        }
    }
}
