@file:Suppress("unused", "UNUSED_PARAMETER")

package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.Constants
import java.util.function.Predicate
import java.util.function.Supplier
import kotlin.reflect.KFunction1

fun <T> ArbitraryBuilder<T>.setExpGetter(property: KFunction1<T, Any?>, value: Any?): ArbitraryBuilder<T> =
    this.set(propertyExpressionGenerator(property), value)

fun <T> ArbitraryBuilder<T>.setExpGetter(property: KFunction1<T, Any?>, value: Any?, limit: Long): ArbitraryBuilder<T> =
    this.set(
        propertyExpressionGenerator(property),
        value,
        limit.toInt()
    )

fun <T> ArbitraryBuilder<T>.setNullExpGetter(property: KFunction1<T, *>): ArbitraryBuilder<T> =
    this.setNull(propertyExpressionGenerator(property))

fun <T> ArbitraryBuilder<T>.setNotNullExpGetter(property: KFunction1<T, *>): ArbitraryBuilder<T> =
    this.setNotNull(propertyExpressionGenerator(property))

fun <T, U> ArbitraryBuilder<T>.setPostConditionExpGetter(
    property: KFunction1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setPostCondition(
        propertyExpressionGenerator(property),
        clazz,
        filter,
        limit.toInt()
    )

fun <T, U> ArbitraryBuilder<T>.setPostConditionExpGetter(
    property: KFunction1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>
): ArbitraryBuilder<T> =
    this.setPostCondition(
        propertyExpressionGenerator(property),
        clazz,
        filter
    )

fun <T> ArbitraryBuilder<T>.sizeExpGetter(
    property: KFunction1<T, *>,
    size: Int
): ArbitraryBuilder<T> =
    this.size(propertyExpressionGenerator(property), size)

fun <T> ArbitraryBuilder<T>.sizeExpGetter(
    property: KFunction1<T, *>,
    min: Int,
    max: Int
): ArbitraryBuilder<T> =
    this.size(propertyExpressionGenerator(property), min, max)

fun <T> ArbitraryBuilder<T>.minSizeExpGetter(
    property: KFunction1<T, *>,
    min: Int
): ArbitraryBuilder<T> =
    this.minSize(propertyExpressionGenerator(property), min)

fun <T> ArbitraryBuilder<T>.maxSizeExpGetter(
    property: KFunction1<T, *>,
    max: Int
): ArbitraryBuilder<T> =
    this.maxSize(propertyExpressionGenerator(property), max)

fun <T> ArbitraryBuilder<T>.setLazyExpGetter(
    property: KFunction1<T, Any?>,
    supplier: Supplier<Any?>
): ArbitraryBuilder<T> =
    this.setLazy(propertyExpressionGenerator(property), supplier)

fun <T> ArbitraryBuilder<T>.setLazyExpGetter(
    property: KFunction1<T, Any?>,
    supplier: Supplier<Any?>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setLazy(propertyExpressionGenerator(property), supplier, limit.toInt())

// root
@JvmName("setRootExpGetter")
fun <T> ArbitraryBuilder<T>.setExpGetter(
    property: KFunction1<T, Class<T>>,
    value: Any?,
    limit: Long = Constants.MAX_MANIPULATION_COUNT.toLong()
): ArbitraryBuilder<T> =
    this.set("$", value, limit.toInt())

@JvmName("setNullRootExpGetter")
fun <T> ArbitraryBuilder<T>.setNullExpGetter(property: KFunction1<T, Class<T>>): ArbitraryBuilder<T> =
    this.setNull("$")

@JvmName("setNotNullRootExpGetter")
fun <T> ArbitraryBuilder<T>.setNotNullExpGetter(property: KFunction1<T, Class<T>>): ArbitraryBuilder<T> =
    this.setNotNull("$")

@JvmName("sizeRootExpGetter")
fun <T> ArbitraryBuilder<T>.sizeExpGetter(
    property: KFunction1<T, Class<T>>,
    size: Int
): ArbitraryBuilder<T> =
    this.size("$", size)

@JvmName("sizeRootExpGetter")
fun <T> ArbitraryBuilder<T>.sizeExpGetter(
    property: KFunction1<T, Class<T>>,
    min: Int,
    max: Int
): ArbitraryBuilder<T> =
    this.size("$", min, max)

@JvmName("minSizeRootExpGetter")
fun <T> ArbitraryBuilder<T>.minSizeExpGetter(
    property: KFunction1<T, Class<T>>,
    min: Int
): ArbitraryBuilder<T> = this.minSize("$", min)

@JvmName("maxSizeRootExpGetter")
fun <T> ArbitraryBuilder<T>.maxSizeExpGetter(
    property: KFunction1<T, Class<T>>,
    max: Int
): ArbitraryBuilder<T> = this.maxSize("$", max)

inline fun <reified T> ArbitraryBuilder<T>.setPostConditionExpGetter(
    property: KFunction1<T, Class<T>>,
    filter: Predicate<T>,
    limit: Long = Constants.MAX_MANIPULATION_COUNT.toLong()
): ArbitraryBuilder<T> =
    this.setPostCondition(
        "$",
        T::class.java,
        filter,
        limit.toInt()
    )

@JvmName("setLazyRootExpGetter")
fun <T> ArbitraryBuilder<T>.setLazyExpGetter(
    property: KFunction1<T, Class<T>>,
    supplier: Supplier<Any?>,
    limit: Long = Constants.MAX_MANIPULATION_COUNT.toLong()
): ArbitraryBuilder<T> = this.setLazy("$", supplier, limit.toInt())
