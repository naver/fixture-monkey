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

import com.navercorp.fixturemonkey.api.arbitrary.LongCombinableArbitrary
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.positiveLong
import io.kotest.property.arbitrary.negativeLong
import io.kotest.property.arbitrary.single
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import java.util.function.Predicate

@API(since = "1.1.15", status = Status.EXPERIMENTAL)
class KotestLongCombinableArbitrary(private val arb: Arb<Long> = Arb.long()) : LongCombinableArbitrary {
    override fun combined(): Long = arb.single()

    override fun rawValue(): Long = this.combined()

    override fun withRange(min: Long, max: Long): LongCombinableArbitrary =
        KotestLongCombinableArbitrary(Arb.long(min..max))

    override fun positive(): LongCombinableArbitrary = KotestLongCombinableArbitrary(Arb.positiveLong())

    override fun negative(): LongCombinableArbitrary = KotestLongCombinableArbitrary(Arb.negativeLong())

    override fun even(): LongCombinableArbitrary =
        KotestLongCombinableArbitrary(Arb.long().filter { it % 2 == 0.toLong() })

    override fun odd(): LongCombinableArbitrary =
        KotestLongCombinableArbitrary(Arb.long().filter { it % 2 != 0.toLong() })

    override fun filter(tries: Int, predicate: Predicate<Long>): LongCombinableArbitrary =
        KotestLongCombinableArbitrary(Arb.long().filter(predicate::test))

    override fun clear() {
    }

    override fun fixed(): Boolean = false
}
