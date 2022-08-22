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
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.util.function.Predicate
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinProperty

class Exp<T> internal constructor(private val delegate: ExpressionGenerator) : ExpressionGenerator by delegate {
    constructor() : this(EmptyExpressionGenerator())

    constructor(exp: Exp<T>) : this(exp.delegate)

    infix fun <R> into(property: KProperty1<T, R?>): Exp<R> =
        Exp(delegate.join(PropertyExpressionGenerator(KotlinProperty(property))))

    @JvmName("getterBoolean")
    infix fun intoGetter(getter: KFunction1<T, Boolean>): Exp<Boolean> =
        Exp(delegate.join(PropertyExpressionGenerator(KotlinGetterProperty(getter))))

    infix fun <R> intoGetter(getter: KFunction1<T, R?>): Exp<R> =
        Exp(delegate.join(PropertyExpressionGenerator(KotlinGetterProperty(getter))))

    infix fun <R> into(exp: ExpList<T, R>): Exp<R> = Exp(delegate.join(exp))

    infix fun <R> intoGetter(exp: ExpList<T, R>): Exp<R> = Exp(delegate.join(exp))

    @JvmName("intoList")
    infix fun <R : Collection<E>, E : Any> into(property: KProperty1<T, R?>): ExpList<T, E> =
        ExpList(delegate.join(PropertyExpressionGenerator(KotlinProperty(property))))

    @JvmName("getterIntoList")
    infix fun <R : Collection<E>, E : Any> intoGetter(getter: KFunction1<T, R?>): ExpList<T, E> =
        ExpList(delegate.join(PropertyExpressionGenerator(KotlinGetterProperty(getter))))
}

fun <T, R> Exp(expList: ExpList<T, R>) = Exp<R>(expList)

fun <T, R> Exp(property: KProperty1<T, R?>) = Exp<R>(PropertyExpressionGenerator(KotlinProperty(property)))

fun <T, R> ExpGetter(property: KFunction1<T, R?>) = Exp<R>(PropertyExpressionGenerator(KotlinGetterProperty(property)))

fun <T, R> ExpGetter(expList: ExpList<T, R>) = Exp<R>(expList)

@JvmName("getterBoolean")
fun ExpGetter(property: KFunction1<*, Boolean>) =
    Exp<Boolean>(PropertyExpressionGenerator(KotlinGetterProperty(property)))

fun <T> ArbitraryBuilder<T>.set(property: KProperty1<T, Any?>, value: Any?): ArbitraryBuilder<T> =
    this.set(PropertyExpressionGenerator(KotlinProperty(property)), value)

fun <T> ArbitraryBuilder<T>.set(property: KProperty1<T, Any?>, value: Any?, limit: Long): ArbitraryBuilder<T> =
    this.set(PropertyExpressionGenerator(KotlinProperty(property)), value, limit.toInt())

fun <T> ArbitraryBuilder<T>.setExp(property: KProperty1<T, Any?>, value: Any?): ArbitraryBuilder<T> =
    this.set(PropertyExpressionGenerator(KotlinProperty(property)), value)

fun <T> ArbitraryBuilder<T>.setExp(property: KProperty1<T, Any?>, value: Any?, limit: Long): ArbitraryBuilder<T> =
    this.set(PropertyExpressionGenerator(KotlinProperty(property)), value, limit.toInt())

fun <T> ArbitraryBuilder<T>.setExpGetter(property: KFunction1<T, Any?>, value: Any?): ArbitraryBuilder<T> =
    this.set(PropertyExpressionGenerator(KotlinGetterProperty(property)), value)

fun <T> ArbitraryBuilder<T>.setExpGetter(property: KFunction1<T, Any?>, value: Any?, limit: Long): ArbitraryBuilder<T> =
    this.set(
        PropertyExpressionGenerator(KotlinGetterProperty(property)),
        value,
        limit.toInt()
    )

fun <T> ArbitraryBuilder<T>.setExp(expressionGenerator: ExpressionGenerator, value: Any?): ArbitraryBuilder<T> =
    this.set(expressionGenerator, value)

fun <T> ArbitraryBuilder<T>.setExp(
    expressionGenerator: ExpressionGenerator,
    value: Any?,
    limit: Long
): ArbitraryBuilder<T> =
    this.set(expressionGenerator, value, limit.toInt())

fun <T> ArbitraryBuilder<T>.setExpGetter(expressionGenerator: ExpressionGenerator, value: Any?): ArbitraryBuilder<T> =
    this.set(expressionGenerator, value)

fun <T> ArbitraryBuilder<T>.setExpGetter(
    expressionGenerator: ExpressionGenerator,
    value: Any?,
    limit: Long
): ArbitraryBuilder<T> =
    this.set(expressionGenerator, value, limit.toInt())

fun <T> ArbitraryBuilder<T>.setNull(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNull(PropertyExpressionGenerator(KotlinProperty(property)))

fun <T> ArbitraryBuilder<T>.setNullExp(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNull(PropertyExpressionGenerator(KotlinProperty(property)))

fun <T> ArbitraryBuilder<T>.setNullExpGetter(property: KFunction1<T, *>): ArbitraryBuilder<T> =
    this.setNull(PropertyExpressionGenerator(KotlinGetterProperty(property)))

fun <T> ArbitraryBuilder<T>.setNullExp(expressionGenerator: ExpressionGenerator): ArbitraryBuilder<T> =
    this.setNull(expressionGenerator)

fun <T> ArbitraryBuilder<T>.setNullExpGetter(expressionGenerator: ExpressionGenerator): ArbitraryBuilder<T> =
    this.setNull(expressionGenerator)

fun <T> ArbitraryBuilder<T>.setNotNull(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNotNull(PropertyExpressionGenerator(KotlinProperty(property)))

fun <T> ArbitraryBuilder<T>.setNotNullExp(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNotNull(PropertyExpressionGenerator(KotlinProperty(property)))

fun <T> ArbitraryBuilder<T>.setNotNullExpGetter(property: KFunction1<T, *>): ArbitraryBuilder<T> =
    this.setNotNull(PropertyExpressionGenerator(KotlinGetterProperty(property)))

fun <T> ArbitraryBuilder<T>.setNotNullExp(expressionGenerator: ExpressionGenerator): ArbitraryBuilder<T> =
    this.setNotNull(expressionGenerator)

fun <T> ArbitraryBuilder<T>.setNotNullExpGetter(expressionGenerator: ExpressionGenerator): ArbitraryBuilder<T> =
    this.setNotNull(expressionGenerator)

fun <T, U> ArbitraryBuilder<T>.setPostCondition(
    property: KProperty1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>
): ArbitraryBuilder<T> =
    this.setPostCondition(
        PropertyExpressionGenerator(KotlinProperty(property)),
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
        PropertyExpressionGenerator(KotlinProperty(property)),
        clazz,
        filter,
        limit.toInt()
    )

fun <T, U> ArbitraryBuilder<T>.setPostConditionExp(
    property: KProperty1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>
): ArbitraryBuilder<T> =
    this.setPostCondition(
        PropertyExpressionGenerator(KotlinProperty(property)),
        clazz,
        filter
    )

fun <T, U> ArbitraryBuilder<T>.setPostConditionExpGetter(
    property: KFunction1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setPostCondition(
        PropertyExpressionGenerator(KotlinGetterProperty(property)),
        clazz,
        filter,
        limit.toInt()
    )

fun <T, U> ArbitraryBuilder<T>.setPostConditionExp(
    property: KProperty1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>,
    limit: Long
): ArbitraryBuilder<T> =
    this.setPostCondition(
        PropertyExpressionGenerator(KotlinProperty(property)),
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
        PropertyExpressionGenerator(KotlinGetterProperty(property)),
        clazz,
        filter
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

fun <T> ArbitraryBuilder<T>.size(
    property: KProperty1<T, *>,
    size: Int
): ArbitraryBuilder<T> =
    this.size(PropertyExpressionGenerator(KotlinProperty(property)), size)

fun <T> ArbitraryBuilder<T>.size(
    property: KProperty1<T, *>,
    min: Int,
    max: Int
): ArbitraryBuilder<T> =
    this.size(PropertyExpressionGenerator(KotlinProperty(property)), min, max)

fun <T> ArbitraryBuilder<T>.sizeExp(
    property: KProperty1<T, *>,
    size: Int
): ArbitraryBuilder<T> =
    this.size(PropertyExpressionGenerator(KotlinProperty(property)), size)

fun <T> ArbitraryBuilder<T>.sizeExpGetter(
    property: KFunction1<T, *>,
    size: Int
): ArbitraryBuilder<T> =
    this.size(PropertyExpressionGenerator(KotlinGetterProperty(property)), size)

fun <T> ArbitraryBuilder<T>.sizeExp(
    expressionGenerator: ExpressionGenerator,
    size: Int
): ArbitraryBuilder<T> =
    this.size(expressionGenerator, size)

fun <T> ArbitraryBuilder<T>.sizeExpGetter(
    expressionGenerator: ExpressionGenerator,
    size: Int
): ArbitraryBuilder<T> =
    this.size(expressionGenerator, size)

fun <T> ArbitraryBuilder<T>.sizeExp(
    property: KProperty1<T, *>,
    min: Int,
    max: Int
): ArbitraryBuilder<T> =
    this.size(PropertyExpressionGenerator(KotlinProperty(property)), min, max)

fun <T> ArbitraryBuilder<T>.sizeExpGetter(
    property: KFunction1<T, *>,
    min: Int,
    max: Int
): ArbitraryBuilder<T> =
    this.size(PropertyExpressionGenerator(KotlinGetterProperty(property)), min, max)

fun <T> ArbitraryBuilder<T>.sizeExp(
    expressionGenerator: ExpressionGenerator,
    min: Int,
    max: Int
): ArbitraryBuilder<T> =
    this.size(expressionGenerator, min, max)

fun <T> ArbitraryBuilder<T>.sizeExpGetter(
    expressionGenerator: ExpressionGenerator,
    min: Int,
    max: Int
): ArbitraryBuilder<T> =
    this.size(expressionGenerator, min, max)

fun <T> ArbitraryBuilder<T>.minSize(
    property: KProperty1<T, *>,
    min: Int
): ArbitraryBuilder<T> =
    this.minSize(PropertyExpressionGenerator(KotlinProperty(property)), min)

fun <T> ArbitraryBuilder<T>.minSizeExp(
    property: KProperty1<T, *>,
    min: Int
): ArbitraryBuilder<T> =
    this.minSize(PropertyExpressionGenerator(KotlinProperty(property)), min)

fun <T> ArbitraryBuilder<T>.minSizeExpGetter(
    property: KFunction1<T, *>,
    min: Int
): ArbitraryBuilder<T> =
    this.minSize(PropertyExpressionGenerator(KotlinGetterProperty(property)), min)

fun <T> ArbitraryBuilder<T>.minSizeExp(
    expressionGenerator: ExpressionGenerator,
    min: Int
): ArbitraryBuilder<T> =
    this.minSize(expressionGenerator, min)

fun <T> ArbitraryBuilder<T>.minSizeExpGetter(
    expressionGenerator: ExpressionGenerator,
    min: Int
): ArbitraryBuilder<T> =
    this.minSize(expressionGenerator, min)

fun <T> ArbitraryBuilder<T>.maxSize(
    property: KProperty1<T, *>,
    max: Int
): ArbitraryBuilder<T> =
    this.maxSize(PropertyExpressionGenerator(KotlinProperty(property)), max)

fun <T> ArbitraryBuilder<T>.maxSizeExp(
    property: KProperty1<T, *>,
    max: Int
): ArbitraryBuilder<T> =
    this.maxSize(PropertyExpressionGenerator(KotlinProperty(property)), max)

fun <T> ArbitraryBuilder<T>.maxSizeExpGetter(
    property: KFunction1<T, *>,
    max: Int
): ArbitraryBuilder<T> =
    this.maxSize(PropertyExpressionGenerator(KotlinGetterProperty(property)), max)

fun <T> ArbitraryBuilder<T>.maxSizeExp(
    expressionGenerator: ExpressionGenerator,
    max: Int
): ArbitraryBuilder<T> =
    this.maxSize(expressionGenerator, max)

fun <T> ArbitraryBuilder<T>.maxSizeExpGetter(
    expressionGenerator: ExpressionGenerator,
    max: Int
): ArbitraryBuilder<T> =
    this.maxSize(expressionGenerator, max)

infix fun <T, R, E> KProperty1<T, R?>.into(property: KProperty1<R, E?>): Exp<E> =
    Exp(
        JoinExpressionGenerator(
            listOf(
                PropertyExpressionGenerator(KotlinProperty(this)),
                DotExpressionGenerator(),
                PropertyExpressionGenerator(KotlinProperty(property))
            )
        )
    )

infix fun <T, R, E> KProperty1<T, R?>.intoGetter(property: KFunction1<R, E?>): Exp<E> =
    Exp(
        JoinExpressionGenerator(
            listOf(
                PropertyExpressionGenerator(KotlinProperty(this)),
                DotExpressionGenerator(),
                PropertyExpressionGenerator(KotlinGetterProperty(property))
            )
        )
    )

infix fun <T, R, E> KProperty1<T, R?>.into(expList: ExpList<R, E>): Exp<E> =
    Exp(
        JoinExpressionGenerator(
            listOf(
                PropertyExpressionGenerator(KotlinProperty(this)),
                DotExpressionGenerator(),
                expList
            )
        )
    )

infix fun <T, R, E> KProperty1<T, R?>.intoGetter(expList: ExpList<R, E>): Exp<E> =
    Exp(
        JoinExpressionGenerator(
            listOf(
                PropertyExpressionGenerator(KotlinProperty(this)),
                DotExpressionGenerator(),
                expList
            )
        )
    )

infix fun <T, R, E> KFunction1<T, R?>.intoGetter(property: KFunction1<R, E?>): Exp<E> =
    Exp(
        JoinExpressionGenerator(
            listOf(
                PropertyExpressionGenerator(KotlinGetterProperty(this)),
                DotExpressionGenerator(),
                PropertyExpressionGenerator(KotlinGetterProperty(property))
            )
        )
    )

infix fun <T, R, E> KFunction1<T, R?>.into(property: KProperty1<R, E?>): Exp<E> =
    Exp(
        JoinExpressionGenerator(
            listOf(
                PropertyExpressionGenerator(KotlinGetterProperty(this)),
                DotExpressionGenerator(),
                PropertyExpressionGenerator(KotlinProperty(property))
            )
        )
    )

infix fun <T, R, E> KFunction1<T, R?>.into(expList: ExpList<R, E>): Exp<E> =
    Exp(
        JoinExpressionGenerator(
            listOf(
                PropertyExpressionGenerator(KotlinGetterProperty(this)),
                DotExpressionGenerator(),
                expList
            )
        )
    )

infix fun <T, R, E> KFunction1<T, R?>.intoGetter(expList: ExpList<R, E>): Exp<E> =
    Exp(
        JoinExpressionGenerator(
            listOf(
                PropertyExpressionGenerator(KotlinGetterProperty(this)),
                DotExpressionGenerator(),
                expList
            )
        )
    )

infix operator fun <T, R : Collection<E>, E : Any> KProperty1<T, R?>.get(index: Int): ExpList<T, E> =
    ExpList(ArrayExpressionGenerator(KotlinProperty(this), index))

infix operator fun <T, R : Collection<E>, E : Any> KProperty1<T, R?>.get(key: String): ExpList<T, E> =
    ExpList(MapExpressionGenerator(KotlinProperty(this), key))

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, R?>.get(
    index: Int
): ExpList<T, N?> = ExpList(ArrayExpressionGenerator(KotlinProperty(this), index))

@JvmName("getNestedMap")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, R?>.get(
    key: String
): ExpList<T, N?> = ExpList(MapExpressionGenerator(KotlinProperty(this), key))

infix operator
fun <T, R : Collection<E>, E : Any> KFunction1<T, R?>.get(index: Int): ExpList<T, E> =
    ExpList(ArrayExpressionGenerator(KotlinGetterProperty(this), index))

infix operator
fun <T, R : Collection<E>, E : Any> KFunction1<T, R?>.get(key: String): ExpList<T, E> =
    ExpList(MapExpressionGenerator(KotlinGetterProperty(this), key))

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KFunction1<T, R?>.get(
    index: Int
): ExpList<T, N?> = ExpList(ArrayExpressionGenerator(KotlinGetterProperty(this), index))

@JvmName("getNestedMap")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KFunction1<T, R?>.get(
    key: String
): ExpList<T, N?> = ExpList(MapExpressionGenerator(KotlinGetterProperty(this), key))

@Suppress("unused")
class ExpList<E, L> internal constructor(val delegate: ExpressionGenerator) : ExpressionGenerator by delegate {
    infix fun <R> into(expList: ExpList<L, R>) =
        Exp<R>(
            JoinExpressionGenerator(
                listOf(
                    this.delegate,
                    DotExpressionGenerator(),
                    expList.delegate
                )
            )
        )

    infix fun <R> intoGetter(expList: ExpList<L, R>) =
        Exp<R>(
            JoinExpressionGenerator(
                listOf(
                    this.delegate,
                    DotExpressionGenerator(),
                    expList.delegate
                )
            )
        )

    infix fun <R> into(property: KProperty1<L, R?>): Exp<R> =
        Exp(
            JoinExpressionGenerator(
                listOf(
                    this,
                    DotExpressionGenerator(),
                    PropertyExpressionGenerator(KotlinProperty(property))
                )
            )
        )

    infix fun <R> intoGetter(property: KFunction1<L, R?>): Exp<R> =
        Exp(
            JoinExpressionGenerator(
                listOf(
                    this,
                    DotExpressionGenerator(),
                    PropertyExpressionGenerator(KotlinGetterProperty(property))
                )
            )
        )
}

infix operator
fun <T, R : Collection<E>, E : Any> ExpList<T, R?>.get(index: Int): ExpList<T, E> =
    ExpList(
        JoinExpressionGenerator(
            listOf(
                delegate,
                IndexExpressionGenerator(index)
            )
        )
    )

infix operator
fun <T, R : Collection<E>, E : Any> ExpList<T, R?>.get(key: String): ExpList<T, E> =
    ExpList(JoinExpressionGenerator(listOf(delegate, KeyExpressionGenerator(key))))

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> ExpList<T, R?>.get(
    index: Int
): ExpList<T, N?> =
    ExpList(
        JoinExpressionGenerator(
            listOf(
                delegate,
                IndexExpressionGenerator(index)
            )
        )
    )

@JvmName("getNestedMap")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> ExpList<T, R?>.get(
    key: String
): ExpList<T, N?> =
    ExpList(JoinExpressionGenerator(listOf(delegate, KeyExpressionGenerator(key))))

private class JoinExpressionGenerator(private val expressionGenerators: List<ExpressionGenerator>) :
    ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        expressionGenerators.joinToString(separator = "") { expressionGenerator ->
            expressionGenerator.generate(propertyNameResolver)
        }
}

private class PropertyExpressionGenerator(private val property: Property) :
    ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        propertyNameResolver.resolve(property)
}

private class IndexExpressionGenerator(val index: Int) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        "[$index]"
}

private class ArrayExpressionGenerator(
    private val property: Property,
    val index: Int
) :
    ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        "${propertyNameResolver.resolve(property)}[$index]"
}

private class MapExpressionGenerator(
    private val property: Property,
    val key: String
) :
    ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        "${propertyNameResolver.resolve(property)}[$key]"
}

private class KeyExpressionGenerator(private val key: String) :
    ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        "[$key]"
}

private class DotExpressionGenerator : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = "."
}

private class EmptyExpressionGenerator : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = ""
}

fun ExpressionGenerator.join(joinerExpressionGenerator: ExpressionGenerator): ExpressionGenerator =
    if (this is EmptyExpressionGenerator) {
        joinerExpressionGenerator
    } else {
        JoinExpressionGenerator(
            listOf(
                this,
                DotExpressionGenerator(),
                joinerExpressionGenerator
            )
        )
    }

private class KotlinProperty<V, R>(private val property: KProperty1<V, R>) :
    Property {
    override fun getType(): Class<*> = property.javaField!!.type

    override fun getAnnotatedType(): AnnotatedType =
        property.javaField!!.annotatedType

    override fun getName(): String = property.name

    override fun getAnnotations(): List<Annotation> = property.annotations

    @Suppress("UNCHECKED_CAST")
    override fun getValue(obj: Any): Any? = property.get(obj as V)
}

private class KotlinGetterProperty<V, R>(private val getter: KFunction1<V, R>) : Property {
    private val callerType = getter.parameters[0].type.javaType as Class<*>
    private val returnJavaType = getter.returnType.javaType
    private val type: Class<*> = if (returnJavaType is ParameterizedType) {
        returnJavaType.rawType as Class<*>
    } else {
        returnJavaType as Class<*>
    }
    private val propertyName: String = resolvePropertyName()

    private fun resolvePropertyName(): String =
        if (getter.name.startsWith("get")) {
            getter.name.substringAfter("get")
                .replaceFirstChar { it.lowercaseChar() }
        } else if (getter.returnType == Boolean::class.java && getter.name.startsWith("is")) {
            getter.name.substringAfter("is")
                .replaceFirstChar { it.lowercaseChar() }
        } else {
            getter.name
        }

    private val property: KProperty<*>? = try {
        callerType.getDeclaredField(name).kotlinProperty
    } catch (ex: Exception) {
        null
    }
    private val javaField: Field? = try {
        callerType.getDeclaredField(name)
    } catch (ex: Exception) {
        null
    }
    private val propertyAnnotation: List<Annotation> =
        property?.annotations ?: listOf()
    private val getterAnnotation: List<Annotation> = getter.annotations
    private val javaFieldAnnotations: List<Annotation> =
        javaField?.annotations?.toList() ?: listOf()

    override fun getType(): Class<*> = type

    override fun getAnnotatedType(): AnnotatedType? =
        property?.javaField?.annotatedType ?: javaField?.annotatedType

    override fun getName(): String = propertyName

    override fun getAnnotations(): List<Annotation> =
        (propertyAnnotation + getterAnnotation + javaFieldAnnotations).distinct()

    @Suppress("UNCHECKED_CAST")
    override fun getValue(obj: Any?): Any? = getter.invoke(obj as V)
}
