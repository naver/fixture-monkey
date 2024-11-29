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

package com.navercorp.fixturemonkey.junit.jupiter.extension

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.random.Randoms
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(FixtureMonkeySeedExtension::class)
class FixtureMonkeySeedTest {
    @Test
    fun withoutSeedAnnotationApplyNull() {
        then(Randoms.currentSeed()).isNull()
    }

    @Test
    @Seed(1000L)
    fun seedAnnotation() {
        then(Randoms.currentSeed()).isEqualTo(1000L)
    }

    companion object {
        private val FIXTURE_MONKEY = FixtureMonkey.builder()
            .seed(12345L)
            .build()
    }
}
