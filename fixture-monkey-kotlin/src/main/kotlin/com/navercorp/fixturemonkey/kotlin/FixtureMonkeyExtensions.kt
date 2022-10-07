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

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.ArbitraryOption
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.type.TypeReference
import com.navercorp.fixturemonkey.kotlin.customizer.KArbitraryCustomizer
import com.navercorp.fixturemonkey.kotlin.customizer.toArbitraryCustomizer
import net.jqwik.api.Arbitrary
import kotlin.streams.asSequence

inline fun <reified T : Any?> FixtureMonkey.giveMe(): Sequence<T> =
    this.giveMe(object : TypeReference<T>() {}).asSequence()

inline fun <reified T : Any> FixtureMonkey.giveMe(
    customizer: KArbitraryCustomizer<T>
): Sequence<T> = this.giveMe(T::class.java, customizer.toArbitraryCustomizer()).asSequence()

inline fun <reified T : Any?> FixtureMonkey.giveMe(size: Int): List<T> =
    this.giveMe(object : TypeReference<T>() {}, size)

inline fun <reified T : Any> FixtureMonkey.giveMe(
    size: Int,
    customizer: KArbitraryCustomizer<T>
): List<T> = this.giveMe(T::class.java, size, customizer.toArbitraryCustomizer())

inline fun <reified T : Any?> FixtureMonkey.giveMeOne(): T = this.giveMeOne(object : TypeReference<T>() {})

inline fun <reified T : Any> FixtureMonkey.giveMeOne(
    customizer: KArbitraryCustomizer<T>
): T = this.giveMeOne(T::class.java, customizer.toArbitraryCustomizer())

inline fun <reified T : Any?> FixtureMonkey.giveMeArbitrary(): Arbitrary<T> =
    this.giveMeArbitrary(object : TypeReference<T>() {})

inline fun <reified T : Any?> FixtureMonkey.giveMeBuilder(): ArbitraryBuilder<T> =
    this.giveMeBuilder(object : TypeReference<T>() {})

inline fun <reified T : Any> FixtureMonkey.giveMeBuilder(
    options: ArbitraryOption
): ArbitraryBuilder<T> = this.giveMeBuilder(object : TypeReference<T>() {}, options)
