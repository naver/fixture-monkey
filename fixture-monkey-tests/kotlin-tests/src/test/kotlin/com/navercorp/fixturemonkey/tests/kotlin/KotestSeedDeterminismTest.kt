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

package com.navercorp.fixturemonkey.tests.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotest.KotestPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class KotestSeedDeterminismTest {
    @Test
    fun sameSeedGivesSameValuesAcrossInstances() {
        // given
        val first = newFixtureMonkey().giveMeKotlinBuilder<Profile>().sampleList(SAMPLE_SIZE)

        // when
        val second = newFixtureMonkey().giveMeKotlinBuilder<Profile>().sampleList(SAMPLE_SIZE)

        // then
        then(second).isEqualTo(first)
    }

    @Test
    fun valuesDoNotDependOnOtherBuildersSampling() {
        // given
        val alone = profilesOfBuilderB(0)

        // when
        val afterOtherBuilderSampled = profilesOfBuilderB(5)

        // then
        then(afterOtherBuilderSampled).isEqualTo(alone)
    }

    @Test
    fun valuesDoNotDependOnOtherFieldsBeingSet() {
        // given
        val plain = newFixtureMonkey().giveMeKotlinBuilder<Profile>()
            .sampleList(SAMPLE_SIZE)
            .map { it.name to it.tags }

        // when
        val countSet = newFixtureMonkey().giveMeKotlinBuilder<Profile>()
            .set("count", 7)
            .sampleList(SAMPLE_SIZE)
            .map { it.name to it.tags }

        // then
        then(countSet).isEqualTo(plain)
    }

    private fun profilesOfBuilderB(builderASampleCount: Int): List<Profile> {
        val fixtureMonkey = newFixtureMonkey()
        val builderA = fixtureMonkey.giveMeKotlinBuilder<Profile>()
        val builderB = fixtureMonkey.giveMeKotlinBuilder<Profile>()
        builderA.sampleList(builderASampleCount)

        return builderB.sampleList(SAMPLE_SIZE)
    }

    private fun newFixtureMonkey(): FixtureMonkey = FixtureMonkey.builder()
        .seed(SEED)
        .plugin(KotestPlugin())
        .plugin(KotlinPlugin())
        .build()

    data class Profile(
        val name: String,
        val count: Int,
        val amount: Long,
        val rate: Double,
        val tags: List<String>,
    )

    companion object {
        private const val SEED = 20260926L
        private const val SAMPLE_SIZE = 20
    }
}
