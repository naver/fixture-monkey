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

@file:Suppress("unused")

package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator
import java.util.function.Predicate
import java.util.function.Supplier
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setExp(expressionGenerator, value)")
)
fun <T> ArbitraryBuilder<T>.setExp(expressionGenerator: ExpressionGenerator, value: Any?): ArbitraryBuilder<T> =
    this.set(expressionGenerator, value)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setExp(expressionGenerator, value, limit)")
)
fun <T> ArbitraryBuilder<T>.setExp(
    expressionGenerator: ExpressionGenerator,
    value: Any?,
    limit: Long
): ArbitraryBuilder<T> =
    this.set(expressionGenerator, value, limit.toInt())

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setExpGetter(expressionGenerator, value, limit)")
)
fun <T> ArbitraryBuilder<T>.setExpGetter(
    expressionGenerator: ExpressionGenerator,
    value: Any?,
    limit: Long
): ArbitraryBuilder<T> =
    this.set(expressionGenerator, value, limit.toInt())

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setExpGetter(expressionGenerator, value)")
)
fun <T> ArbitraryBuilder<T>.setExpGetter(expressionGenerator: ExpressionGenerator, value: Any?): ArbitraryBuilder<T> =
    this.set(expressionGenerator, value)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setNullExp(expressionGenerator)")
)
fun <T> ArbitraryBuilder<T>.setNullExp(expressionGenerator: ExpressionGenerator): ArbitraryBuilder<T> =
    this.setNull(expressionGenerator)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setNullExpGetter(expressionGenerator)")
)
fun <T> ArbitraryBuilder<T>.setNullExpGetter(expressionGenerator: ExpressionGenerator): ArbitraryBuilder<T> =
    this.setNull(expressionGenerator)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setNotNullExp(expressionGenerator)")
)
fun <T> ArbitraryBuilder<T>.setNotNullExp(expressionGenerator: ExpressionGenerator): ArbitraryBuilder<T> =
    this.setNotNull(expressionGenerator)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setNotNullExpGetter(expressionGenerator)")
)
fun <T> ArbitraryBuilder<T>.setNotNullExpGetter(expressionGenerator: ExpressionGenerator): ArbitraryBuilder<T> =
    this.setNotNull(expressionGenerator)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setPostConditionExp(property, filter)")
)
fun <T, U> ArbitraryBuilder<T>.setPostConditionExp(
    property: KProperty1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>
): ArbitraryBuilder<T> =
    this.setPostCondition(
        property(property),
        clazz,
        filter
    )

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setPostConditionExp(expressionGenerator, filter, limit)")
)
fun <T, U> ArbitraryBuilder<T>.setPostConditionExp(
    expressionGenerator: ExpressionGenerator,
    clazz: Class<U>,
    filter: Predicate<U>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setPostCondition(
        expressionGenerator,
        clazz,
        filter,
        limit.toInt()
    )

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setPostConditionExp(expressionGenerator, filter)")
)
fun <T, U> ArbitraryBuilder<T>.setPostConditionExp(
    expressionGenerator: ExpressionGenerator,
    clazz: Class<U>,
    filter: Predicate<U>
): ArbitraryBuilder<T> =
    this.setPostCondition(
        expressionGenerator,
        clazz,
        filter
    )

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setPostConditionExpGetter(expressionGenerator, filter, limit)")
)
fun <T, U> ArbitraryBuilder<T>.setPostConditionExpGetter(
    expressionGenerator: ExpressionGenerator,
    clazz: Class<U>,
    filter: Predicate<U>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setPostCondition(
        expressionGenerator,
        clazz,
        filter,
        limit.toInt()
    )

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setPostConditionExp(expressionGenerator, filter)")
)
fun <T, U> ArbitraryBuilder<T>.setPostConditionExpGetter(
    expressionGenerator: ExpressionGenerator,
    clazz: Class<U>,
    filter: Predicate<U>
): ArbitraryBuilder<T> =
    this.setPostCondition(
        expressionGenerator,
        clazz,
        filter
    )

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("sizeExp(expressionGenerator, size)")
)
fun <T> ArbitraryBuilder<T>.sizeExp(
    expressionGenerator: ExpressionGenerator,
    size: Int
): ArbitraryBuilder<T> =
    this.size(expressionGenerator, size)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("sizeExpGetter(expressionGenerator, size)")
)
fun <T> ArbitraryBuilder<T>.sizeExpGetter(
    expressionGenerator: ExpressionGenerator,
    size: Int
): ArbitraryBuilder<T> =
    this.size(expressionGenerator, size)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("sizeExp(expressionGenerator, min, max)")
)
fun <T> ArbitraryBuilder<T>.sizeExp(
    expressionGenerator: ExpressionGenerator,
    min: Int,
    max: Int
): ArbitraryBuilder<T> =
    this.size(expressionGenerator, min, max)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("sizeExpGetter(expressionGenerator, min, max)")
)
fun <T> ArbitraryBuilder<T>.sizeExpGetter(
    expressionGenerator: ExpressionGenerator,
    min: Int,
    max: Int
): ArbitraryBuilder<T> =
    this.size(expressionGenerator, min, max)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("minSizeExp(expressionGenerator, min)")
)
fun <T> ArbitraryBuilder<T>.minSizeExp(
    expressionGenerator: ExpressionGenerator,
    min: Int
): ArbitraryBuilder<T> =
    this.minSize(expressionGenerator, min)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("minSizeExpGetter(expressionGenerator, min)")
)
fun <T> ArbitraryBuilder<T>.minSizeExpGetter(
    expressionGenerator: ExpressionGenerator,
    min: Int
): ArbitraryBuilder<T> =
    this.minSize(expressionGenerator, min)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("maxSizeExp(expressionGenerator, max)")
)
fun <T> ArbitraryBuilder<T>.maxSizeExp(
    expressionGenerator: ExpressionGenerator,
    max: Int
): ArbitraryBuilder<T> =
    this.maxSize(expressionGenerator, max)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("maxSizeExpGetter(expressionGenerator, max)")
)
fun <T> ArbitraryBuilder<T>.maxSizeExpGetter(
    expressionGenerator: ExpressionGenerator,
    max: Int
): ArbitraryBuilder<T> =
    this.maxSize(expressionGenerator, max)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setLazyExp(expressionGenerator, supplier)")
)
fun <T> ArbitraryBuilder<T>.setLazyExp(
    expressionGenerator: ExpressionGenerator,
    supplier: Supplier<Any?>
): ArbitraryBuilder<T> =
    this.setLazy(expressionGenerator, supplier)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setLazyExp(expressionGenerator, supplier, limit)")
)
fun <T> ArbitraryBuilder<T>.setLazyExp(
    expressionGenerator: ExpressionGenerator,
    supplier: Supplier<Any?>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setLazy(expressionGenerator, supplier, limit.toInt())

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setLazyExpGetter(expressionGenerator, supplier)")
)
fun <T> ArbitraryBuilder<T>.setLazyExpGetter(
    expressionGenerator: ExpressionGenerator,
    supplier: Supplier<Any?>
): ArbitraryBuilder<T> =
    this.setLazy(expressionGenerator, supplier)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setLazyExpGetter(expressionGenerator, supplier, limit)")
)
fun <T> ArbitraryBuilder<T>.setLazyExpGetter(
    expressionGenerator: ExpressionGenerator,
    supplier: Supplier<Any?>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setLazy(expressionGenerator, supplier, limit.toInt())

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("set(property, value)")
)
fun <T> ArbitraryBuilder<T>.set(property: KProperty1<T, Any?>, value: Any?): ArbitraryBuilder<T> =
    this.set(property(property), value)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("set(property, value, limit)")
)
fun <T> ArbitraryBuilder<T>.set(property: KProperty1<T, Any?>, value: Any?, limit: Long): ArbitraryBuilder<T> =
    this.set(property(property), value, limit.toInt())

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setExp(property, value)")
)
fun <T> ArbitraryBuilder<T>.setExp(property: KProperty1<T, Any?>, value: Any?): ArbitraryBuilder<T> =
    this.set(property(property), value)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setExp(property, value, limit)")
)
fun <T> ArbitraryBuilder<T>.setExp(property: KProperty1<T, Any?>, value: Any?, limit: Long): ArbitraryBuilder<T> =
    this.set(property(property), value, limit.toInt())

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setNull(property)")
)
fun <T> ArbitraryBuilder<T>.setNull(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNull(property(property))

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setNullExp(property)")
)
fun <T> ArbitraryBuilder<T>.setNullExp(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNull(property(property))

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setNotNull(property)")
)
fun <T> ArbitraryBuilder<T>.setNotNull(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNotNull(property(property))

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setNotNullExp(property)")
)
fun <T> ArbitraryBuilder<T>.setNotNullExp(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNotNull(property(property))

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setPostCondition(property, filter)")
)
fun <T, U> ArbitraryBuilder<T>.setPostCondition(
    property: KProperty1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>
): ArbitraryBuilder<T> =
    this.setPostCondition(
        property(property),
        clazz,
        filter
    )

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setPostCondition(property, filter, limit)")
)
fun <T, U> ArbitraryBuilder<T>.setPostCondition(
    property: KProperty1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setPostCondition(
        property(property),
        clazz,
        filter,
        limit.toInt()
    )

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setPostConditionExp(property, filter, limit)")
)
fun <T, U> ArbitraryBuilder<T>.setPostConditionExp(
    property: KProperty1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setPostCondition(
        property(property),
        clazz,
        filter,
        limit.toInt()
    )

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("size(property, size)")
)
fun <T> ArbitraryBuilder<T>.size(
    property: KProperty1<T, *>,
    size: Int
): ArbitraryBuilder<T> = this.size(property(property), size)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("size(property, min, max)")
)
fun <T> ArbitraryBuilder<T>.size(
    property: KProperty1<T, *>,
    min: Int,
    max: Int
): ArbitraryBuilder<T> = this.size(property(property), min, max)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("sizeExp(property, size)")
)
fun <T> ArbitraryBuilder<T>.sizeExp(
    property: KProperty1<T, *>,
    size: Int
): ArbitraryBuilder<T> = this.size(property(property), size)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("sizeExp(property, min, max)")
)
fun <T> ArbitraryBuilder<T>.sizeExp(
    property: KProperty1<T, *>,
    min: Int,
    max: Int
): ArbitraryBuilder<T> = this.size(property(property), min, max)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("minSize(property, min)")
)
fun <T> ArbitraryBuilder<T>.minSize(
    property: KProperty1<T, *>,
    min: Int
): ArbitraryBuilder<T> = this.minSize(property(property), min)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("minSizeExp(property, min)")
)
fun <T> ArbitraryBuilder<T>.minSizeExp(
    property: KProperty1<T, *>,
    min: Int
): ArbitraryBuilder<T> = this.minSize(property(property), min)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("maxSize(property, max)")
)
fun <T> ArbitraryBuilder<T>.maxSize(
    property: KProperty1<T, *>,
    max: Int
): ArbitraryBuilder<T> = this.maxSize(property(property), max)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("maxSizeExp(property, max)")
)
fun <T> ArbitraryBuilder<T>.maxSizeExp(
    property: KProperty1<T, *>,
    max: Int
): ArbitraryBuilder<T> = this.maxSize(property(property), max)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setLazyExp(property, supplier)")
)
fun <T> ArbitraryBuilder<T>.setLazyExp(
    property: KProperty1<T, Any?>,
    supplier: Supplier<Any?>
): ArbitraryBuilder<T> = this.setLazy(property(property), supplier)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setLazyExp(property, supplier, limit)")
)
fun <T> ArbitraryBuilder<T>.setLazyExp(
    property: KProperty1<T, Any?>,
    supplier: Supplier<Any?>,
    limit: Long
): ArbitraryBuilder<T> = this.setLazy(property(property), supplier, limit.toInt())

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setExpGetter(property, value)")
)
fun <T> ArbitraryBuilder<T>.setExpGetter(property: KFunction1<T, Any?>, value: Any?): ArbitraryBuilder<T> =
    this.set(property(property), value)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setExpGetter(property, value, limit)")
)
fun <T> ArbitraryBuilder<T>.setExpGetter(property: KFunction1<T, Any?>, value: Any?, limit: Long): ArbitraryBuilder<T> =
    this.set(
        property(property),
        value,
        limit.toInt()
    )

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setNullExpGetter(property)")
)
fun <T> ArbitraryBuilder<T>.setNullExpGetter(property: KFunction1<T, *>): ArbitraryBuilder<T> =
    this.setNull(property(property))

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setNotNullExpGetter(property)")
)
fun <T> ArbitraryBuilder<T>.setNotNullExpGetter(property: KFunction1<T, *>): ArbitraryBuilder<T> =
    this.setNotNull(property(property))

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setPostConditionExpGetter(property, filter, limit)")
)
fun <T, U> ArbitraryBuilder<T>.setPostConditionExpGetter(
    property: KFunction1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setPostCondition(
        property(property),
        clazz,
        filter,
        limit.toInt()
    )

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setPostConditionExpGetter(property, filter)")
)
fun <T, U> ArbitraryBuilder<T>.setPostConditionExpGetter(
    property: KFunction1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>
): ArbitraryBuilder<T> =
    this.setPostCondition(
        property(property),
        clazz,
        filter
    )

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("sizeExpGetter(property, size)")
)
fun <T> ArbitraryBuilder<T>.sizeExpGetter(
    property: KFunction1<T, *>,
    size: Int
): ArbitraryBuilder<T> =
    this.size(property(property), size)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("sizeExpGetter(property, min, max)")
)
fun <T> ArbitraryBuilder<T>.sizeExpGetter(
    property: KFunction1<T, *>,
    min: Int,
    max: Int
): ArbitraryBuilder<T> =
    this.size(property(property), min, max)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("minSizeExpGetter(property, min)")
)
fun <T> ArbitraryBuilder<T>.minSizeExpGetter(
    property: KFunction1<T, *>,
    min: Int
): ArbitraryBuilder<T> =
    this.minSize(property(property), min)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("maxSizeExpGetter(property, max)")
)
fun <T> ArbitraryBuilder<T>.maxSizeExpGetter(
    property: KFunction1<T, *>,
    max: Int
): ArbitraryBuilder<T> =
    this.maxSize(property(property), max)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setLazyExpGetter(property, supplier)")
)
fun <T> ArbitraryBuilder<T>.setLazyExpGetter(
    property: KFunction1<T, Any?>,
    supplier: Supplier<Any?>
): ArbitraryBuilder<T> =
    this.setLazy(property(property), supplier)

@Deprecated(
    message = "Extension function is deprecated. Use instance method instead.",
    replaceWith = ReplaceWith("setLazyExpGetter(property, supplier, limit)")
)
fun <T> ArbitraryBuilder<T>.setLazyExpGetter(
    property: KFunction1<T, Any?>,
    supplier: Supplier<Any?>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setLazy(property(property), supplier, limit.toInt())
