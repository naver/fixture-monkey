package com.navercorp.fixturemonkey.kotlin

import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1

// property
infix operator fun <T, R : Collection<E>, E : Any> KProperty1<T, R?>.get(index: Int): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, R?>.get(
    index: Int,
): JoinableExpressionGenerator<T, N?> = DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

@JvmName("arrayIndex")
infix operator fun <T, E : Any> KProperty1<T, Array<E>>.get(index: Int): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

@JvmName("nestedArrayIndex")
infix operator fun <T, E : Any> KProperty1<T, Array<Array<E>>?>.get(index: Int): JoinableExpressionGenerator<T, Array<E>> =
    DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

// rootProperty
@JvmName("getRootList")
infix operator fun <T : Collection<E>, E : Any> KProperty1<T, Class<T>>.get(index: Int): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

@JvmName("getRootArray")
infix operator fun <E : Any> KProperty1<Array<E>, Class<Array<E>>>.get(index: Int): JoinableExpressionGenerator<Array<E>, E> =
    DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

@JvmName("getRootNestedList")
infix operator fun <T : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, Class<T>>.get(
    index: Int,
): JoinableExpressionGenerator<T, N?> = DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

@JvmName("getRootNestedArray")
infix operator fun <E : Any> KProperty1<Array<Array<E>>, Class<Array<Array<E>>>>.get(
    index: Int,
): JoinableExpressionGenerator<Array<Array<E>>, Array<E>> =
    DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

// function
infix operator fun <T, R : Collection<E>, E : Any> KFunction1<T, R?>.get(index: Int): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

@JvmName("array")
infix operator fun <T, E : Any> KFunction1<T, Array<E>?>.get(index: Int): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KFunction1<T, R?>.get(
    index: Int,
): JoinableExpressionGenerator<T, N?> = DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

// rootFunction
@JvmName("getRootList")
infix operator fun <T : Collection<E>, E : Any> KFunction1<T, Class<T>>.get(index: Int): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

@JvmName("getRootArray")
infix operator fun <E : Any> KFunction1<Array<E>, Class<Array<E>>>.get(index: Int): JoinableExpressionGenerator<Array<E>, E> =
    DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

@JvmName("getRootNestedList")
infix operator fun <T : Collection<N>, N : Collection<E>, E : Any> KFunction1<T, Class<T>>.get(
    index: Int,
): JoinableExpressionGenerator<T, N?> = DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))

@JvmName("getRootNestedArray")
infix operator fun <E : Any> KFunction1<Array<Array<E>>, Class<Array<Array<E>>>>.get(
    index: Int,
): JoinableExpressionGenerator<Array<Array<E>>, Array<E>> =
    DefaultJoinableExpressionGenerator(indexExpressionGenerator(this, index))
