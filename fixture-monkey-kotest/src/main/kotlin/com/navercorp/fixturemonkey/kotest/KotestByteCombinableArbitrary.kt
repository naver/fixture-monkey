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

import com.navercorp.fixturemonkey.api.arbitrary.ByteCombinableArbitrary
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.single
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import java.util.function.Predicate

@API(since = "1.1.15", status = Status.EXPERIMENTAL)
class KotestByteCombinableArbitrary(private val arb: Arb<Byte> = Arb.byte()) : ByteCombinableArbitrary {
    override fun combined(): Byte = arb.single()

    override fun rawValue(): Byte = this.combined()

    override fun withRange(min: Byte, max: Byte): ByteCombinableArbitrary =
        KotestByteCombinableArbitrary(Arb.byte().filter { it in min..max })

    override fun positive(): ByteCombinableArbitrary =
        KotestByteCombinableArbitrary(Arb.byte().filter { it > 0 })

    override fun negative(): ByteCombinableArbitrary =
        KotestByteCombinableArbitrary(Arb.byte().filter { it < 0 })

    override fun even(): ByteCombinableArbitrary =
        KotestByteCombinableArbitrary(Arb.byte().filter { it % 2 == 0 })

    override fun odd(): ByteCombinableArbitrary =
        KotestByteCombinableArbitrary(Arb.byte().filter { it % 2 != 0 })

    override fun ascii(): ByteCombinableArbitrary =
        KotestByteCombinableArbitrary(Arb.byte().filter { it in 0..127 })

    override fun filter(tries: Int, predicate: Predicate<Byte>): ByteCombinableArbitrary =
        KotestByteCombinableArbitrary(Arb.byte().filter(predicate::test))

    override fun clear() {
    }

    override fun fixed(): Boolean = false
}
