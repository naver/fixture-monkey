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

import com.navercorp.fixturemonkey.api.arbitrary.FloatCombinableArbitrary
import io.kotest.property.Arb
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.single
import java.math.BigDecimal
import java.math.RoundingMode

class KotestFloatCombinableArbitrary(
    private val floatArb: Arb<Float> = Arb.float()
) : FloatCombinableArbitrary {

    override fun combined(): Float = floatArb.single()

    override fun rawValue(): Float = floatArb.single()

    override fun withRange(min: Float, max: Float): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(Arb.float(min, max))
    }

    override fun positive(): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(Arb.float(Float.MIN_VALUE, Float.MAX_VALUE))
    }

    override fun negative(): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(Arb.float(-Float.MAX_VALUE, -Float.MIN_VALUE))
    }

    override fun nonZero(): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(floatArb.filter { it != 0.0f })
    }

    override fun withPrecision(scale: Int): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(
            floatArb.map { value ->
                BigDecimal.valueOf(value.toDouble())
                    .setScale(scale, RoundingMode.HALF_UP)
                    .toFloat()
            }
        )
    }

    override fun finite(): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(floatArb.filter { it.isFinite() })
    }

    override fun infinite(): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(
            Arb.choice(
                Arb.constant(Float.POSITIVE_INFINITY),
                Arb.constant(Float.NEGATIVE_INFINITY)
            )
        )
    }

    override fun normalized(): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(Arb.float(0.0f, 1.0f))
    }

    override fun nan(): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(Arb.constant(Float.NaN))
    }

    override fun percentage(): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(Arb.float(0.0f, 100.0f))
    }

    override fun score(): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(Arb.float(0.0f, 100.0f))
    }

    override fun withSpecialValue(special: Float): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(
            Arb.choice(
                floatArb,
                Arb.constant(special)
            )
        )
    }

    override fun withStandardSpecialValues(): FloatCombinableArbitrary {
        return KotestFloatCombinableArbitrary(
            Arb.choice(
                floatArb,
                Arb.constant(Float.NaN),
                Arb.constant(Float.MIN_VALUE),
                Arb.constant(Float.MIN_VALUE), // MIN_NORMAL doesn't exist in Kotlin
                Arb.constant(Float.POSITIVE_INFINITY),
                Arb.constant(Float.NEGATIVE_INFINITY)
            )
        )
    }

    override fun clear() {
        // No-op for Kotest
    }

    override fun fixed(): Boolean = false
}
