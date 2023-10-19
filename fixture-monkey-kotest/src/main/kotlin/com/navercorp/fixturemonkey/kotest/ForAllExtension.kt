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

@file:Suppress("TooManyFunctions", "unused")

package com.navercorp.fixturemonkey.kotest

import com.navercorp.fixturemonkey.FixtureMonkey
import io.kotest.property.PropTestConfig
import io.kotest.property.PropertyContext
import io.kotest.property.forAll

suspend inline fun <reified A> FixtureMonkey.forAll(
    noinline function: suspend PropertyContext.(a: A) -> Boolean,
) = forAll(giveMeArb(), function)

suspend inline fun <reified A> FixtureMonkey.forAll(
    iterations: Int,
    noinline function: suspend PropertyContext.(a: A) -> Boolean,
) = forAll(iterations, giveMeArb(), function)

suspend inline fun <reified A> FixtureMonkey.forAll(
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A) -> Boolean,
) = forAll(config, giveMeArb(), function)

suspend inline fun <reified A> FixtureMonkey.forAll(
    iterations: Int,
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A) -> Boolean,
) = forAll(iterations, config, giveMeArb(), function)

suspend inline fun <reified A, reified B> FixtureMonkey.forAll(
    noinline function: suspend PropertyContext.(a: A, b: B) -> Boolean,
) = forAll(giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B> FixtureMonkey.forAll(
    iterations: Int,
    noinline function: suspend PropertyContext.(a: A, b: B) -> Boolean,
) = forAll(iterations, giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B> FixtureMonkey.forAll(
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B) -> Boolean,
) = forAll(config, giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B> FixtureMonkey.forAll(
    iterations: Int,
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B) -> Boolean,
) = forAll(iterations, config, giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C> FixtureMonkey.forAll(
    noinline function: suspend PropertyContext.(a: A, b: B, c: C) -> Boolean,
) = forAll(giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C> FixtureMonkey.forAll(
    iterations: Int,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C) -> Boolean,
) = forAll(iterations, giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C> FixtureMonkey.forAll(
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C) -> Boolean,
) = forAll(config, giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C> FixtureMonkey.forAll(
    iterations: Int,
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C) -> Boolean,
) = forAll(iterations, config, giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D> FixtureMonkey.forAll(
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D) -> Boolean,
) = forAll(giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D> FixtureMonkey.forAll(
    iterations: Int,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D) -> Boolean,
) = forAll(iterations, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D> FixtureMonkey.forAll(
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D) -> Boolean,
) = forAll(config, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D> FixtureMonkey.forAll(
    iterations: Int,
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D) -> Boolean,
) = forAll(iterations, config, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E> FixtureMonkey.forAll(
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E) -> Boolean,
) = forAll(giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E> FixtureMonkey.forAll(
    iterations: Int,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E) -> Boolean,
) = forAll(iterations, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E> FixtureMonkey.forAll(
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E) -> Boolean,
) = forAll(config, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E> FixtureMonkey.forAll(
    iterations: Int,
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E) -> Boolean,
) = forAll(iterations, config, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E, reified F> FixtureMonkey.forAll(
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E, f: F) -> Boolean,
) = forAll(giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E, reified F> FixtureMonkey.forAll(
    iterations: Int,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E, f: F) -> Boolean,
) = forAll(iterations, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E, reified F> FixtureMonkey.forAll(
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E, f: F) -> Boolean,
) = forAll(config, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E, reified F> FixtureMonkey.forAll(
    iterations: Int,
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E, f: F) -> Boolean,
) = forAll(iterations, config, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)
