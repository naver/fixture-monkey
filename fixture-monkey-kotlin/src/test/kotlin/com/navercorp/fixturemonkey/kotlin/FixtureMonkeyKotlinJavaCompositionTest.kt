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
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.spec.JavaObject
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class FixtureMonkeyKotlinJavaCompositionTest {

    @Test
    fun KotlinObjectWithJavaObject() {
        // given
        val sut: FixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
            .build()

        // when
        class KotlinObjectWithJavaObject(val javaObject: JavaObject)
        val actual = sut.giveMeOne<KotlinObjectWithJavaObject>()

        then(actual).isNotNull
        then(actual.javaObject).isNotNull
        then(actual.javaObject.value).isNotNull
    }
}
