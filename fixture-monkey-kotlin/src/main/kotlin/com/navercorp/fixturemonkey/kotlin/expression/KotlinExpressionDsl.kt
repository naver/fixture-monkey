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

package com.navercorp.fixturemonkey.kotlin.expression

import com.navercorp.fixturemonkey.expression.DeclarativeExpression
import com.navercorp.fixturemonkey.expression.DefaultDeclarativeExpression

@DslMarker
annotation class ExpressionDsl

@ExpressionDsl
class DeclarativeExpressionDsl(
    private val declarativeExpressions: MutableList<DefaultDeclarativeExpression> = mutableListOf()
) : DeclarativeExpression {
    override fun property(propertyName: String): DeclarativeExpressionDsl {
        this.declarativeExpressions.add(DefaultDeclarativeExpression().property(propertyName))
        return this
    }

    override fun element(sequence: Int): DeclarativeExpressionDsl {
        this.declarativeExpressions.add(DefaultDeclarativeExpression().element(sequence))
        return this
    }

    override fun allElement(): DeclarativeExpressionDsl {
        this.declarativeExpressions.add(DefaultDeclarativeExpression().allElement())
        return this
    }

    override fun key(): DeclarativeExpressionDsl {
        this.declarativeExpressions.add(DefaultDeclarativeExpression().key())
        return this
    }

    override fun value(): DeclarativeExpressionDsl {
        this.declarativeExpressions.add(DefaultDeclarativeExpression().value())
        return this
    }

    fun property(
        propertyName: String,
        dsl: DeclarativeExpressionDsl.() -> DeclarativeExpressionDsl
    ): DeclarativeExpressionDsl {
        this.declarativeExpressions.addAll(
            dsl(DeclarativeExpressionDsl()).prepend(
                DefaultDeclarativeExpression().property(
                    propertyName
                )
            )
        )
        return this
    }

    fun element(
        sequence: Int,
        dsl: DeclarativeExpressionDsl.() -> DeclarativeExpressionDsl
    ): DeclarativeExpressionDsl {
        this.declarativeExpressions.addAll(
            dsl(DeclarativeExpressionDsl())
                .prepend(DefaultDeclarativeExpression().element(sequence))
        )
        return this
    }

    fun allElement(dsl: DeclarativeExpressionDsl.() -> DeclarativeExpressionDsl): DeclarativeExpressionDsl {
        this.declarativeExpressions.addAll(
            dsl(DeclarativeExpressionDsl())
                .prepend(DefaultDeclarativeExpression().allElement())
        )
        return this
    }

    fun key(dsl: DeclarativeExpressionDsl.() -> DeclarativeExpressionDsl): DeclarativeExpressionDsl {
        this.declarativeExpressions.addAll(
            dsl(DeclarativeExpressionDsl())
                .prepend(DefaultDeclarativeExpression().key())
        )
        return this
    }

    fun value(dsl: DeclarativeExpressionDsl.() -> DeclarativeExpressionDsl): DeclarativeExpressionDsl {
        this.declarativeExpressions.addAll(
            dsl(DeclarativeExpressionDsl())
                .prepend(DefaultDeclarativeExpression().value())
        )
        return this
    }

    private fun prepend(parentDeclarativeExpression: DefaultDeclarativeExpression) =
        this.declarativeExpressions.map { it.prepend(parentDeclarativeExpression) }
}
