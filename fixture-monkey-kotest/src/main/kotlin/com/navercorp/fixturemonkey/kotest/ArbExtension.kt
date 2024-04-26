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

@file:Suppress("TooManyFunctions")

package com.navercorp.fixturemonkey.kotest

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.property.PropertySelector
import com.navercorp.fixturemonkey.api.type.TypeReference
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.propertyExpressionGenerator
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.single
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type
import java.util.function.Supplier
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

@Suppress("UNCHECKED_CAST")
inline fun <reified T> FixtureMonkey.giveMeArb(): Arb<T> {
    val type = typeOf<T>()

    return if (type.isSubtypeOf(typeOf<ArbitraryBuilder<*>>())) {
        val typeParameter = type.arguments[0]

        arbitrary {
            val javaType = typeParameter.type!!.javaType
            giveMeBuilder(
                object : TypeReference<T>() {
                    override fun getType(): Type {
                        return javaType
                    }

                    override fun getAnnotatedType(): AnnotatedType {
                        return Types.generateAnnotatedTypeWithoutAnnotation(javaType)
                    }
                },
            )
        } as Arb<T>
    } else {
        arbitrary {
            this@giveMeArb.giveMeOne()
        }
    }
}

inline fun <reified T> FixtureMonkey.giveMeArb(crossinline applyBuilder: ArbitraryBuilder<T>.() -> ArbitraryBuilder<T>): Arb<T> =
    arbitrary {
        applyBuilder.invoke(this@giveMeArb.giveMeBuilder()).sample()
    }

fun <T : Any?> ArbitraryBuilder<T>.setArb(expression: String, arb: Arb<Any>): ArbitraryBuilder<T> = this.set(
    expression,
    Supplier { arb.single() }
)

fun <T : Any?> ArbitraryBuilder<T>.setArb(propertySelector: PropertySelector, arb: Arb<Any>): ArbitraryBuilder<T> =
    this.set(
        propertySelector,
        Supplier { arb.single() }
    )

fun <T : Any?> ArbitraryBuilder<T>.setArb(p: KProperty1<T, Any?>, arb: Arb<Any>): ArbitraryBuilder<T> =
    this.set(propertyExpressionGenerator(p), Supplier { arb.single() })
