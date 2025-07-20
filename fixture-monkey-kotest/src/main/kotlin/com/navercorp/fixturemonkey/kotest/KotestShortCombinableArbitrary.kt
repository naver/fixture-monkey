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

import com.navercorp.fixturemonkey.api.arbitrary.ShortCombinableArbitrary
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.short
import io.kotest.property.arbitrary.single
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import java.util.function.Predicate

@API(since = "1.1.15", status = Status.EXPERIMENTAL)
class KotestShortCombinableArbitrary(private val arb: Arb<Short> = Arb.short()) : ShortCombinableArbitrary {
    override fun combined(): Short = arb.single()

    override fun rawValue(): Short = arb.single()

    override fun withRange(min: Short, max: Short): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it in min..max })

    override fun positive(): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it > 0 })

    override fun negative(): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it < 0 })

    override fun even(): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it % 2 == 0 })

    override fun odd(): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it % 2 != 0 })

    override fun nonZero(): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it != 0.toShort() })

    override fun multipleOf(value: Short): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it % value == 0 })

    override fun percentage(): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it in 0..100 })

    override fun score(): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it in 0..100 })

    override fun year(): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it in 1900..2100 })

    override fun month(): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it in 1..12 })

    override fun day(): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it in 1..31 })

    override fun hour(): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it in 0..23 })

    override fun minute(): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(Arb.short().filter { it in 0..59 })

    override fun filter(tries: Int, predicate: Predicate<Short>): ShortCombinableArbitrary =
        KotestShortCombinableArbitrary(arb.filter(predicate::test))

    override fun clear() {
    }

    override fun fixed(): Boolean = false
}
