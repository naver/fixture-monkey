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

package com.navercorp.fixturemonkey.kotlin.test

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.introspector.KotlinAndJavaCompositeArbitraryIntrospector
import net.jqwik.api.Example
import org.assertj.core.api.BDDAssertions.then

class KotlinAndJavaCompositeArbitraryIntrospectorTest {
    @Example
    fun kotlinClassWithJavaClass() {
        // given
        val sut: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .objectIntrospector(KotlinAndJavaCompositeArbitraryIntrospector())
            .build()

        // when
        val actual = sut.giveMeOne<KotlinClassWithJavaClass>()

        then(actual).isNotNull
        then(actual.javaObject).isNotNull
    }

    @Example
    fun kotlinClassWithJavaClassUsingOtherIntrospector() {
        // given
        val sut: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .objectIntrospector(
                KotlinAndJavaCompositeArbitraryIntrospector(
                    javaArbitraryIntrospector = ConstructorPropertiesArbitraryIntrospector.INSTANCE
                )
            )
            .build()

        // when
        val actual = sut.giveMeOne<KotlinClassWithJavaClass>()

        then(actual).isNotNull
        then(actual.javaObject).isNotNull
    }

    @Example
    fun sampleMapValue() {
        // given
        val sut: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .objectIntrospector(KotlinAndJavaCompositeArbitraryIntrospector())
            .build()

        // when
        val actual = sut.giveMeOne<MapValue>()

        then(actual).isNotNull
        then(actual.map).isNotNull
    }
}
