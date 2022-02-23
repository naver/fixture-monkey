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

package com.navercorp.fixturemonkey.kotlin.customizer

import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers
import com.navercorp.fixturemonkey.generator.FieldArbitraries
import kotlin.reflect.KClass

fun <T : Any> KArbitraryCustomizer<T>.toArbitraryCustomizer(): ArbitraryCustomizer<T> {
    return object : ArbitraryCustomizer<T> {
        private val delegate = this@toArbitraryCustomizer

        override fun customizeFields(type: Class<T>, fieldArbitraries: FieldArbitraries) {
            delegate.customizeFields(type.kotlin, fieldArbitraries)
        }

        override fun customizeFixture(target: T?): T? {
            return delegate.customizeFixture(target)
        }
    }
}

internal fun <T : Any> ArbitraryCustomizers.customizeFields(
    type: KClass<T>,
    fieldArbitraries: FieldArbitraries
): Unit = this.customizeFields(type.java, fieldArbitraries)
