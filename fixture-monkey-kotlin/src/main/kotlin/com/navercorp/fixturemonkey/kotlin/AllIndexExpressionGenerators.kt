package com.navercorp.fixturemonkey.kotlin

import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1

// property
infix operator fun <T, R : Collection<E>, E : Any> KProperty1<T, R?>.get(allIndex: String): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))

@JvmName("getNestedAllIndex")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, R?>.get(
    allIndex: String,
): JoinableExpressionGenerator<T, N?> = DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))

@JvmName("arrayAllIndex")
infix operator fun <T, E : Any> KProperty1<T, Array<E>>.get(allIndex: String): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))

@JvmName("nestedArrayAllIndex")
infix operator fun <T, E : Any> KProperty1<T, Array<Array<E>>?>.get(allIndex: String): JoinableExpressionGenerator<T, Array<E>> =
    DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))

// rootProperty
@JvmName("getRootListAllIndex")
infix operator fun <T : Collection<E>, E : Any> KProperty1<T, Class<T>>.get(allIndex: String): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))

@JvmName("getRootNestedListAllIndex")
infix operator fun <T : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, Class<T>>.get(
    allIndex: String,
): JoinableExpressionGenerator<T, N?> = DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))

@JvmName("getRootArrayAllIndex")
infix operator fun <E : Any> KProperty1<Array<E>, Class<Array<E>>>.get(allIndex: String): JoinableExpressionGenerator<Array<E>, E> =
    DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))

@JvmName("getRootNestedArrayAllIndex")
infix operator fun <E : Any> KProperty1<Array<Array<E>>, Class<Array<Array<E>>>>.get(
    allIndex: String,
): JoinableExpressionGenerator<Array<Array<E>>, Array<E>> =
    DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))

// function
infix operator fun <T, R : Collection<E>, E : Any> KFunction1<T, R?>.get(allIndex: String): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))

@JvmName("getNestedAllIndex")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KFunction1<T, R?>.get(
    allIndex: String,
): JoinableExpressionGenerator<T, N?> = DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))

@JvmName("array")
infix operator fun <T, E : Any> KFunction1<T, Array<E>?>.get(allIndex: String): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))

// rootFunction
@JvmName("getRootListAllIndex")
infix operator fun <T : Collection<E>, E : Any> KFunction1<T, Class<T>>.get(allIndex: String): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))

@JvmName("getRootNestedListAllIndex")
infix operator fun <T : Collection<N>, N : Collection<E>, E : Any> KFunction1<T, Class<T>>.get(
    allIndex: String,
): JoinableExpressionGenerator<T, N?> = DefaultJoinableExpressionGenerator(allIndexExpressionGenerator(this, allIndex))
