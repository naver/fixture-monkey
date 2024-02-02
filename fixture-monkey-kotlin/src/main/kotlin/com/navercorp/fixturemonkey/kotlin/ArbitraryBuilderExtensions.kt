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
import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator
import com.navercorp.fixturemonkey.customizer.InnerSpec

/**
 * Apply manipulation to [InnerSpec][com.navercorp.fixturemonkey.customizer.InnerSpec]
 * and pass it to [setInner][com.navercorp.fixturemonkey.ArbitraryBuilder.setInner].
 */
fun <T> ArbitraryBuilder<T>.setInner(innerSpecConfigurer: ((InnerSpec) -> Unit)): ArbitraryBuilder<T> {
    return this.setInner(InnerSpec().apply(innerSpecConfigurer))
}

@Deprecated(
    message = "Extension function is deprecated. Use giveMeKotlinBuilder and instance method instead.",
    replaceWith = ReplaceWith("setPostCondition(expression, predicate)")
)
inline fun <T, reified U> ArbitraryBuilder<T>.setPostCondition(expression: String, noinline predicate: (U) -> Boolean) =
    this.setPostCondition(expression, U::class.java, predicate)

@Deprecated(
    message = "Extension function is deprecated. Use giveMeKotlinBuilder and instance method instead.",
    replaceWith = ReplaceWith("setPostCondition(expressionGenerator, predicate)")
)
inline fun <T, reified U> ArbitraryBuilder<T>.setPostCondition(
    expressionGenerator: ExpressionGenerator,
    noinline predicate: (U) -> Boolean
) = this.setPostCondition(expressionGenerator, U::class.java, predicate)

@Deprecated(
    message = "Extension function is deprecated. Use giveMeKotlinBuilder and instance method instead.",
    replaceWith = ReplaceWith("setPostCondition(expression, predicate, limit)")
)
inline fun <T, reified U> ArbitraryBuilder<T>.setPostCondition(
    expression: String,
    noinline predicate: (U) -> Boolean,
    limit: Int
) = this.setPostCondition(expression, U::class.java, predicate, limit)

@Deprecated(
    message = "Extension function is deprecated. Use giveMeKotlinBuilder and instance method instead.",
    replaceWith = ReplaceWith("setPostCondition(expressionGenerator, predicate, limit)")
)
inline fun <T, reified U> ArbitraryBuilder<T>.setPostCondition(
    expressionGenerator: ExpressionGenerator,
    noinline predicate: (U) -> Boolean,
    limit: Int
) = this.setPostCondition(expressionGenerator, U::class.java, predicate, limit)
