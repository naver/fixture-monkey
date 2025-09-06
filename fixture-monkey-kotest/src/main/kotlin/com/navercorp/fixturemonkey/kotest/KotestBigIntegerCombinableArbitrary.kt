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
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.single
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import java.math.BigInteger
import java.util.function.Predicate

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
class KotestBigIntegerCombinableArbitrary(
    private val arb: Arb<BigInteger> = Arb.bigInt(32)
) : BigIntegerCombinableArbitrary {

    override fun combined(): BigInteger = arb.single()

    override fun rawValue(): BigInteger = this.combined()

    override fun withRange(min: BigInteger, max: BigInteger): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(64).filter { it in min..max }
        )

    override fun positive(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(64).filter { it > BigInteger.ZERO }
        )

    override fun negative(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(64).filter { it < BigInteger.ZERO }
        )

    override fun nonZero(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(64).filter { it != BigInteger.ZERO }
        )

    override fun percentage(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(7).filter { it >= BigInteger.ZERO && it <= BigInteger.valueOf(100) }
        )

    override fun score(min: BigInteger, max: BigInteger): BigIntegerCombinableArbitrary =
        withRange(min, max)

    override fun even(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(64).filter { it % BigInteger.valueOf(2) == BigInteger.ZERO }
        )

    override fun odd(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(64).filter { it % BigInteger.valueOf(2) != BigInteger.ZERO }
        )

    override fun multipleOf(divisor: BigInteger): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(64).filter { it % divisor == BigInteger.ZERO }
        )

    override fun prime(): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(
            Arb.bigInt(32).filter { it > BigInteger.ONE && it.isProbablePrime(100) }
        )

    override fun filter(tries: Int, predicate: Predicate<BigInteger>): BigIntegerCombinableArbitrary =
        KotestBigIntegerCombinableArbitrary(arb.filter(predicate::test))

    override fun clear() {
    }

    override fun fixed(): Boolean = false
}
