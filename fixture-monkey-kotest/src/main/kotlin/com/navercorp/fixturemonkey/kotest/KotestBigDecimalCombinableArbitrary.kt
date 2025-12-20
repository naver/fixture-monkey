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

import com.navercorp.fixturemonkey.api.arbitrary.BigDecimalCombinableArbitrary
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.single
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.function.Predicate

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
class KotestBigDecimalCombinableArbitrary(
    private val arb: Arb<BigDecimal> = Arb.bigDecimal()
) : BigDecimalCombinableArbitrary {

    override fun combined(): BigDecimal = arb.single()

    override fun rawValue(): BigDecimal = this.combined()

    override fun withRange(min: BigDecimal, max: BigDecimal): BigDecimalCombinableArbitrary =
        KotestBigDecimalCombinableArbitrary(
            Arb.bigDecimal().filter { it >= min && it <= max }
        )

    override fun positive(): BigDecimalCombinableArbitrary =
        KotestBigDecimalCombinableArbitrary(
            Arb.bigDecimal().filter { it > BigDecimal.ZERO }
        )

    override fun negative(): BigDecimalCombinableArbitrary =
        KotestBigDecimalCombinableArbitrary(
            Arb.bigDecimal().filter { it < BigDecimal.ZERO }
        )

    override fun nonZero(): BigDecimalCombinableArbitrary =
        KotestBigDecimalCombinableArbitrary(
            Arb.bigDecimal().filter { it.compareTo(BigDecimal.ZERO) != 0 }
        )

    override fun percentage(): BigDecimalCombinableArbitrary =
        KotestBigDecimalCombinableArbitrary(
            Arb.bigDecimal().filter { it >= BigDecimal.ZERO && it <= BigDecimal.valueOf(100) }
        )

    override fun score(): BigDecimalCombinableArbitrary =
        KotestBigDecimalCombinableArbitrary(
            Arb.bigDecimal().filter { it >= BigDecimal.ZERO && it <= BigDecimal.valueOf(100) }
        )

    override fun score(min: BigDecimal, max: BigDecimal): BigDecimalCombinableArbitrary =
        withRange(min, max)

    override fun withPrecision(precision: Int): BigDecimalCombinableArbitrary =
        KotestBigDecimalCombinableArbitrary(
            Arb.bigDecimal().map { it.round(MathContext(precision)) }
        )

    override fun withScale(scale: Int): BigDecimalCombinableArbitrary =
        KotestBigDecimalCombinableArbitrary(
            Arb.bigDecimal().map { it.setScale(scale, RoundingMode.HALF_UP) }
        )

    override fun normalized(): BigDecimalCombinableArbitrary =
        KotestBigDecimalCombinableArbitrary(
            Arb.bigDecimal().filter { it >= BigDecimal.ZERO && it <= BigDecimal.ONE }
        )

    override fun stripTrailingZeros(): BigDecimalCombinableArbitrary =
        KotestBigDecimalCombinableArbitrary(
            Arb.bigDecimal().map { it.stripTrailingZeros() }
        )

    override fun filter(tries: Int, predicate: Predicate<BigDecimal>): BigDecimalCombinableArbitrary =
        KotestBigDecimalCombinableArbitrary(arb.filter(predicate::test))

    override fun clear() {
    }

    override fun fixed(): Boolean = false
}
