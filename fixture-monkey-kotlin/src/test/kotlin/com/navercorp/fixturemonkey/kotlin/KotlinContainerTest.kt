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

package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import net.jqwik.api.Property
import org.assertj.core.api.BDDAssertions.then
import java.util.function.Consumer

class KotlinContainerTest {
    @Property
    fun samplePair() {
        val actual = SUT.giveMeOne<Pair<String, Long>>()

        then(actual.first).isInstanceOf(String::class.java)
        then(actual.second).isInstanceOf(java.lang.Long::class.java)
    }

    @Property
    fun samplePairList() {
        val actual = SUT.giveMeOne<List<Pair<String, Long>>>()

        then(actual).allSatisfy(
            Consumer {
                then(it.first).isInstanceOf(String::class.java)
                then(it.second).isInstanceOf(java.lang.Long::class.java)
            }
        )
    }

    @Property
    fun decomposePair() {
        val builder = SUT.giveMeBuilder<Pair<String, Long>>()
            .fixed()

        val actual1 = builder.sample()
        val actual2 = builder.sample()

        then(actual1).isEqualTo(actual2)
    }

    @Property
    fun sampleTriple() {
        val actual = SUT.giveMeOne<Triple<String, String, Long>>()

        then(actual.first).isInstanceOf(String::class.java)
        then(actual.second).isInstanceOf(String::class.java)
        then(actual.third).isInstanceOf(java.lang.Long::class.java)
    }

    @Property
    fun sampleTripleList() {
        val actual = SUT.giveMeOne<List<Triple<String, String, Long>>>()

        then(actual).allSatisfy(
            Consumer {
                then(it.first).isInstanceOf(String::class.java)
                then(it.second).isInstanceOf(String::class.java)
                then(it.third).isInstanceOf(java.lang.Long::class.java)
            }
        )
    }

    @Property
    fun decomposeTriple() {
        val builder = SUT.giveMeBuilder<Triple<String, String, Long>>()
            .fixed()

        val actual1 = builder.sample()
        val actual2 = builder.sample()

        then(actual1).isEqualTo(actual2)
    }

    companion object {
        val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .defaultNotNull(true)
            .build()
    }
}
