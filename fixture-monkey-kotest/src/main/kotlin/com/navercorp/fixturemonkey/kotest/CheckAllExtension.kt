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
import io.kotest.property.checkAll

suspend inline fun <reified A> FixtureMonkey.checkAll(
    noinline function: suspend PropertyContext.(a: A) -> Unit,
) = checkAll(giveMeArb(), function)

suspend inline fun <reified A> FixtureMonkey.checkAll(
    iterations: Int,
    noinline function: suspend PropertyContext.(a: A) -> Unit,
) = checkAll(iterations, giveMeArb(), function)

suspend inline fun <reified A> FixtureMonkey.checkAll(
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A) -> Unit,
) = checkAll(config, giveMeArb(), function)

suspend inline fun <reified A> FixtureMonkey.checkAll(
    iterations: Int,
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A) -> Unit,
) = checkAll(iterations, config, giveMeArb(), function)

suspend inline fun <reified A, reified B> FixtureMonkey.checkAll(
    noinline function: suspend PropertyContext.(a: A, b: B) -> Unit,
) = checkAll(giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B> FixtureMonkey.checkAll(
    iterations: Int,
    noinline function: suspend PropertyContext.(a: A, b: B) -> Unit,
) = checkAll(iterations, giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B> FixtureMonkey.checkAll(
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B) -> Unit,
) = checkAll(config, giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B> FixtureMonkey.checkAll(
    iterations: Int,
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B) -> Unit,
) = checkAll(iterations, config, giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C> FixtureMonkey.checkAll(
    noinline function: suspend PropertyContext.(a: A, b: B, c: C) -> Unit,
) = checkAll(giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C> FixtureMonkey.checkAll(
    iterations: Int,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C) -> Unit,
) = checkAll(iterations, giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C> FixtureMonkey.checkAll(
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C) -> Unit,
) = checkAll(config, giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C> FixtureMonkey.checkAll(
    iterations: Int,
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C) -> Unit,
) = checkAll(iterations, config, giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D> FixtureMonkey.checkAll(
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D) -> Unit,
) = checkAll(giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D> FixtureMonkey.checkAll(
    iterations: Int,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D) -> Unit,
) = checkAll(iterations, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D> FixtureMonkey.checkAll(
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D) -> Unit,
) = checkAll(config, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D> FixtureMonkey.checkAll(
    iterations: Int,
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D) -> Unit,
) = checkAll(iterations, config, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E> FixtureMonkey.checkAll(
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E) -> Unit,
) = checkAll(giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E> FixtureMonkey.checkAll(
    iterations: Int,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E) -> Unit,
) = checkAll(iterations, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E> FixtureMonkey.checkAll(
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E) -> Unit,
) = checkAll(config, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E> FixtureMonkey.checkAll(
    iterations: Int,
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E) -> Unit,
) = checkAll(iterations, config, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E, reified F> FixtureMonkey.checkAll(
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E, f: F) -> Unit,
) = checkAll(giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E, reified F> FixtureMonkey.checkAll(
    iterations: Int,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E, f: F) -> Unit,
) = checkAll(iterations, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E, reified F> FixtureMonkey.checkAll(
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E, f: F) -> Unit,
) = checkAll(config, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)

suspend inline fun <reified A, reified B, reified C, reified D, reified E, reified F> FixtureMonkey.checkAll(
    iterations: Int,
    config: PropTestConfig,
    noinline function: suspend PropertyContext.(a: A, b: B, c: C, d: D, e: E, f: F) -> Unit,
) = checkAll(iterations, config, giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), giveMeArb(), function)
