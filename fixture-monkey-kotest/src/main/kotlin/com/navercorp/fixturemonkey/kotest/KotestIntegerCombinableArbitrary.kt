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

import com.navercorp.fixturemonkey.api.arbitrary.IntegerCombinableArbitrary
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.negativeInt
import io.kotest.property.arbitrary.positiveInt
import io.kotest.property.arbitrary.single
import java.util.function.Predicate

class KotestIntegerCombinableArbitrary(private val arb: Arb<Int> = Arb.int()) : IntegerCombinableArbitrary {
    override fun combined(): Int = arb.single()

    override fun rawValue(): IntegerCombinableArbitrary = KotestIntegerCombinableArbitrary(arb)

    override fun withRange(min: Int, max: Int): IntegerCombinableArbitrary =
        KotestIntegerCombinableArbitrary(Arb.int(min..max))

    override fun positive(): IntegerCombinableArbitrary =
        KotestIntegerCombinableArbitrary(Arb.positiveInt())

    override fun negative(): IntegerCombinableArbitrary =
        KotestIntegerCombinableArbitrary(Arb.negativeInt())

    override fun even(): IntegerCombinableArbitrary =
        KotestIntegerCombinableArbitrary(Arb.int().filter { it % 2 == 0 })

    override fun odd(): IntegerCombinableArbitrary =
        KotestIntegerCombinableArbitrary(Arb.int().filter { it % 2 != 0 })

    override fun filter(tries: Int, predicate: Predicate<Int>): IntegerCombinableArbitrary =
        KotestIntegerCombinableArbitrary(Arb.int().filter { predicate.test(it) })

    override fun clear() {
    }

    override fun fixed(): Boolean = false
}
