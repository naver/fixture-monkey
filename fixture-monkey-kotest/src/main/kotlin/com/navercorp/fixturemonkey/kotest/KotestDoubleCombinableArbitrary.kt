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

import com.navercorp.fixturemonkey.api.arbitrary.DoubleCombinableArbitrary
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.single
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import java.math.BigDecimal
import java.math.RoundingMode

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
class KotestDoubleCombinableArbitrary(
    private val doubleArb: Arb<Double> = Arb.double()
) : DoubleCombinableArbitrary {

    override fun combined(): Double = doubleArb.single()

    override fun rawValue(): Double = doubleArb.single()

    override fun withRange(min: Double, max: Double): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(Arb.double(min, max))
    }

    override fun positive(): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(Arb.double(Double.MIN_VALUE, Double.MAX_VALUE))
    }

    override fun negative(): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(Arb.double(-Double.MAX_VALUE, -Double.MIN_VALUE))
    }

    override fun nonZero(): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(doubleArb.filter { it != 0.0 })
    }

    override fun withPrecision(scale: Int): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(
            doubleArb.map { value ->
                BigDecimal.valueOf(value)
                    .setScale(scale, RoundingMode.HALF_UP)
                    .toDouble()
            }
        )
    }

    override fun finite(): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(doubleArb.filter { it.isFinite() })
    }

    override fun infinite(): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(
            Arb.choice(
                Arb.constant(Double.POSITIVE_INFINITY),
                Arb.constant(Double.NEGATIVE_INFINITY)
            )
        )
    }

    override fun normalized(): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(Arb.double(0.0, 1.0))
    }

    override fun nan(): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(Arb.constant(Double.NaN))
    }

    override fun percentage(): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(Arb.double(0.0, 100.0))
    }

    override fun score(): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(Arb.double(0.0, 100.0))
    }

    override fun withSpecialValue(special: Double): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(
            Arb.choice(
                doubleArb,
                Arb.constant(special)
            )
        )
    }

    override fun withStandardSpecialValues(): DoubleCombinableArbitrary {
        return KotestDoubleCombinableArbitrary(
            Arb.choice(
                doubleArb,
                Arb.constant(Double.NaN),
                Arb.constant(Double.MIN_VALUE),
                Arb.constant(Double.MIN_VALUE), // MIN_NORMAL doesn't exist in Kotlin
                Arb.constant(Double.POSITIVE_INFINITY),
                Arb.constant(Double.NEGATIVE_INFINITY)
            )
        )
    }

    override fun clear() {
        // No-op for Kotest
    }

    override fun fixed(): Boolean = false
}
