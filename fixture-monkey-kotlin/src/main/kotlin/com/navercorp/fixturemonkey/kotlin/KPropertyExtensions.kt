@file:Suppress("unused", "UNUSED_PARAMETER")

package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.Constants
import java.util.function.Predicate
import java.util.function.Supplier
import kotlin.reflect.KProperty1

fun <T> ArbitraryBuilder<T>.set(property: KProperty1<T, Any?>, value: Any?): ArbitraryBuilder<T> =
    this.set(propertyExpressionGenerator(property), value)

fun <T> ArbitraryBuilder<T>.set(property: KProperty1<T, Any?>, value: Any?, limit: Long): ArbitraryBuilder<T> =
    this.set(propertyExpressionGenerator(property), value, limit.toInt())

fun <T> ArbitraryBuilder<T>.setNull(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNull(propertyExpressionGenerator(property))

fun <T> ArbitraryBuilder<T>.setNotNull(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNotNull(propertyExpressionGenerator(property))

fun <T, U> ArbitraryBuilder<T>.setPostCondition(
    property: KProperty1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>
): ArbitraryBuilder<T> =
    this.setPostCondition(
        propertyExpressionGenerator(property),
        clazz,
        filter
    )

fun <T, U> ArbitraryBuilder<T>.setPostCondition(
    property: KProperty1<T, *>,
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

fun <T> ArbitraryBuilder<T>.size(
    property: KProperty1<T, *>,
    size: Int
): ArbitraryBuilder<T> = this.size(propertyExpressionGenerator(property), size)

fun <T> ArbitraryBuilder<T>.size(
    property: KProperty1<T, *>,
    min: Int,
    max: Int
): ArbitraryBuilder<T> = this.size(propertyExpressionGenerator(property), min, max)

fun <T> ArbitraryBuilder<T>.minSize(
    property: KProperty1<T, *>,
    min: Int
): ArbitraryBuilder<T> = this.minSize(propertyExpressionGenerator(property), min)

fun <T> ArbitraryBuilder<T>.maxSize(
    property: KProperty1<T, *>,
    max: Int
): ArbitraryBuilder<T> = this.maxSize(propertyExpressionGenerator(property), max)

fun <T> ArbitraryBuilder<T>.setLazy(
    property: KProperty1<T, Any?>,
    supplier: Supplier<Any?>
): ArbitraryBuilder<T> = this.setLazy(propertyExpressionGenerator(property), supplier)

fun <T> ArbitraryBuilder<T>.setLazy(
    property: KProperty1<T, Any?>,
    supplier: Supplier<Any?>,
    limit: Long
): ArbitraryBuilder<T> = this.setLazy(propertyExpressionGenerator(property), supplier, limit.toInt())

// Exp
fun <T> ArbitraryBuilder<T>.setExp(property: KProperty1<T, Any?>, value: Any?): ArbitraryBuilder<T> =
    this.set(propertyExpressionGenerator(property), value)

fun <T> ArbitraryBuilder<T>.setExp(property: KProperty1<T, Any?>, value: Any?, limit: Long): ArbitraryBuilder<T> =
    this.set(propertyExpressionGenerator(property), value, limit.toInt())

fun <T> ArbitraryBuilder<T>.setNullExp(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNull(propertyExpressionGenerator(property))

fun <T> ArbitraryBuilder<T>.setNotNullExp(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNotNull(propertyExpressionGenerator(property))

fun <T, U> ArbitraryBuilder<T>.setPostConditionExp(
    property: KProperty1<T, *>,
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

fun <T> ArbitraryBuilder<T>.sizeExp(
    property: KProperty1<T, *>,
    size: Int
): ArbitraryBuilder<T> = this.size(propertyExpressionGenerator(property), size)

fun <T> ArbitraryBuilder<T>.sizeExp(
    property: KProperty1<T, *>,
    min: Int,
    max: Int
): ArbitraryBuilder<T> = this.size(propertyExpressionGenerator(property), min, max)

fun <T> ArbitraryBuilder<T>.minSizeExp(
    property: KProperty1<T, *>,
    min: Int
): ArbitraryBuilder<T> = this.minSize(propertyExpressionGenerator(property), min)

fun <T> ArbitraryBuilder<T>.maxSizeExp(
    property: KProperty1<T, *>,
    max: Int
): ArbitraryBuilder<T> = this.maxSize(propertyExpressionGenerator(property), max)

fun <T> ArbitraryBuilder<T>.setLazyExp(
    property: KProperty1<T, Any?>,
    supplier: Supplier<Any?>
): ArbitraryBuilder<T> = this.setLazy(propertyExpressionGenerator(property), supplier)

fun <T> ArbitraryBuilder<T>.setLazyExp(
    property: KProperty1<T, Any?>,
    supplier: Supplier<Any?>,
    limit: Long
): ArbitraryBuilder<T> = this.setLazy(propertyExpressionGenerator(property), supplier, limit.toInt())

// root
@JvmName("setRoot")
fun <T> ArbitraryBuilder<T>.set(property: KProperty1<T, Class<T>>, value: Any?): ArbitraryBuilder<T> =
    this.set("$", value)

@JvmName("setNullRoot")
fun <T> ArbitraryBuilder<T?>.setNull(property: KProperty1<T, Class<T>>): ArbitraryBuilder<T?> =
    this.setNull("$")

@JvmName("setNotNullRoot")
fun <T> ArbitraryBuilder<T?>.setNotNull(property: KProperty1<T, Class<T>>): ArbitraryBuilder<T?> =
    this.setNotNull("$")

inline fun <reified T> ArbitraryBuilder<T>.setPostCondition(
    property: KProperty1<T, Class<T>>,
    filter: Predicate<T>
): ArbitraryBuilder<T> =
    this.setPostCondition(
        "$",
        T::class.java,
        filter
    )

inline fun <reified T> ArbitraryBuilder<T>.setPostCondition(
    property: KProperty1<T, Class<T>>,
    filter: Predicate<T>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setPostCondition(
        "$",
        T::class.java,
        filter,
        limit.toInt()
    )

@JvmName("sizeRoot")
fun <T> ArbitraryBuilder<T>.size(
    property: KProperty1<T, Class<T>>,
    size: Int
): ArbitraryBuilder<T> = this.size("$", size)

@JvmName("sizeRoot")
fun <T> ArbitraryBuilder<T>.size(
    property: KProperty1<T, Class<T>>,
    min: Int,
    max: Int
): ArbitraryBuilder<T> = this.size("$", min, max)

@JvmName("minSizeRoot")
fun <T> ArbitraryBuilder<T>.minSize(
    property: KProperty1<T, Class<T>>,
    min: Int
): ArbitraryBuilder<T> = this.minSize("$", min)

@JvmName("maxSizeRoot")
fun <T> ArbitraryBuilder<T>.maxSize(
    property: KProperty1<T, Class<T>>,
    min: Int
): ArbitraryBuilder<T> = this.maxSize("$", min)

@JvmName("setLazyRoot")
fun <T> ArbitraryBuilder<T>.setLazy(
    property: KProperty1<T, Class<T>>,
    supplier: Supplier<Any?>
): ArbitraryBuilder<T> = this.setLazy("$", supplier)

@JvmName("setLazyRoot")
fun <T> ArbitraryBuilder<T>.setLazy(
    property: KProperty1<T, Class<T>>,
    supplier: Supplier<Any?>,
    limit: Long
): ArbitraryBuilder<T> = this.setLazy("$", supplier, limit.toInt())

// rootExp
@JvmName("setRootExp")
fun <T> ArbitraryBuilder<T>.setExp(
    property: KProperty1<T, Class<T>>,
    value: Any?,
    limit: Long = Constants.MAX_MANIPULATION_COUNT.toLong()
): ArbitraryBuilder<T> =
    this.set("$", value, limit.toInt())

@JvmName("setNullRootExp")
fun <T> ArbitraryBuilder<T>.setNullExp(property: KProperty1<T, Class<T>>): ArbitraryBuilder<T> =
    this.setNull("$")

@JvmName("setNotNullRootExp")
fun <T> ArbitraryBuilder<T>.setNotNullExp(property: KProperty1<T, Class<T>>): ArbitraryBuilder<T> =
    this.setNotNull("$")

inline fun <reified T> ArbitraryBuilder<T>.setPostConditionExp(
    property: KProperty1<T, Class<T>>,
    filter: Predicate<T>
): ArbitraryBuilder<T> =
    this.setPostCondition(
        "$",
        T::class.java,
        filter
    )

@JvmName("sizeRootExp")
fun <T> ArbitraryBuilder<T>.sizeExp(
    property: KProperty1<T, Class<T>>,
    size: Int
): ArbitraryBuilder<T> =
    this.size("$", size)

@JvmName("sizeRootExp")
fun <T> ArbitraryBuilder<T>.sizeExp(
    property: KProperty1<T, Class<T>>,
    min: Int,
    max: Int
): ArbitraryBuilder<T> = this.size("$", min, max)

inline fun <reified T> ArbitraryBuilder<T>.setPostConditionExp(
    property: KProperty1<T, Class<T>>,
    filter: Predicate<T>,
    limit: Long = Constants.MAX_MANIPULATION_COUNT.toLong()
): ArbitraryBuilder<T> =
    this.setPostCondition(
        "$",
        T::class.java,
        filter,
        limit.toInt()
    )

@JvmName("minSizeRootExp")
fun <T> ArbitraryBuilder<T>.minSizeExp(
    property: KProperty1<T, Class<T>>,
    min: Int
): ArbitraryBuilder<T> = this.minSize("$", min)

@JvmName("maxSizeRootExp")
fun <T> ArbitraryBuilder<T>.maxSizeExp(
    property: KProperty1<T, Class<T>>,
    max: Int
): ArbitraryBuilder<T> = this.maxSize("$", max)

@JvmName("setLazyRootExp")
fun <T> ArbitraryBuilder<T>.setLazyExp(
    property: KProperty1<T, Class<T>>,
    supplier: Supplier<Any?>,
    limit: Long = Constants.MAX_MANIPULATION_COUNT.toLong()
): ArbitraryBuilder<T> = this.setLazy("$", supplier, limit.toInt())
