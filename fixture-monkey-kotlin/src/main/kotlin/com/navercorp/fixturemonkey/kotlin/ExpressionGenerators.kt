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
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.util.function.Predicate
import net.jqwik.api.Arbitrary
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinProperty

class Exp<T> internal constructor(private val delegate: ExpressionGenerator) : ExpressionGenerator by delegate {
    constructor() : this(EmptyExpressionGenerator())

    infix fun <R> into(property: KProperty1<T, R?>): Exp<R> =
        Exp(ParsedExpressionGenerator(listOf(delegate, PropertyExpressionGenerator(KotlinProperty(property)))))

    @JvmName("getterBoolean")
    infix fun into(function: KFunction1<T, Boolean>): Exp<Boolean> =
        Exp(ParsedExpressionGenerator(listOf(delegate, PropertyExpressionGenerator(KotlinGetterProperty(function)))))

    infix fun <R> into(function: KFunction1<T, R?>): Exp<R> =
        Exp(ParsedExpressionGenerator(listOf(delegate, PropertyExpressionGenerator(KotlinGetterProperty(function)))))

    infix fun <R> into(exp: ExpList<T, R>): Exp<R> = Exp(ParsedExpressionGenerator(listOf(delegate, exp.delegate)))

    @JvmName("intoList")
    infix fun <R : Collection<E>, E : Any> into(property: KProperty1<T, R?>): ExpList<T, E> = ExpList(
        ParsedExpressionGenerator(
            listOf(
                delegate,
                PropertyExpressionGenerator(KotlinProperty(property))
            )
        )
    )

    @JvmName("getterIntoList")
    infix fun <R : Collection<E>, E : Any> into(getter: KFunction1<T, R?>): ExpList<T, E> = ExpList(
        ParsedExpressionGenerator(
            listOf(
                delegate,
                PropertyExpressionGenerator(KotlinGetterProperty(getter))
            )
        )
    )
}

fun <T> Exp(exp: Exp<T>) = Exp<T>(ParsedExpressionGenerator(listOf(exp)))

fun <T, R> Exp(expList: ExpList<T, R>) = Exp<R>(ParsedExpressionGenerator(listOf(expList)))

fun <T, R> Exp(property: KProperty1<T, R?>) = Exp<R>(
    ParsedExpressionGenerator(
        listOf(
            PropertyExpressionGenerator(
                KotlinProperty(property)
            )
        )
    )
)

fun <T, R> Exp(property: KFunction1<T, R?>) = Exp<R>(
    ParsedExpressionGenerator(
        listOf(
            PropertyExpressionGenerator(
                KotlinGetterProperty(property)
            )
        )
    )
)

@JvmName("getterBoolean")
fun Exp(property: KFunction1<*, Boolean>) = Exp<Boolean>(
    ParsedExpressionGenerator(
        listOf(
            PropertyExpressionGenerator(
                KotlinGetterProperty(property)
            )
        )
    )
)

fun <T> ArbitraryBuilder<T>.set(property: KProperty1<T, *>, value: Any): ArbitraryBuilder<T> =
    this.set(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))), value)

fun <T> ArbitraryBuilder<T>.set(property: KProperty1<T, *>, value: Any, limit: Long): ArbitraryBuilder<T> =
    this.set(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))), value, limit)

fun <T> ArbitraryBuilder<T>.set(property: KFunction1<T, *>, value: Any): ArbitraryBuilder<T> =
    this.set(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))), value)

fun <T> ArbitraryBuilder<T>.set(property: KFunction1<T, *>, value: Any, limit: Long): ArbitraryBuilder<T> =
    this.set(
        ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))),
        value,
        limit
    )

fun <T> ArbitraryBuilder<T>.set(property: KProperty1<T, *>, value: Arbitrary<*>?): ArbitraryBuilder<T> =
    this.set(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))), value)

fun <T> ArbitraryBuilder<T>.set(property: KFunction1<T, *>, value: Arbitrary<*>?): ArbitraryBuilder<T> =
    this.set(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))), value)

fun <T> ArbitraryBuilder<T>.setBuilder(property: KProperty1<T, *>, builder: ArbitraryBuilder<*>): ArbitraryBuilder<T> =
    this.setBuilder(
        ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))),
        builder
    )

fun <T> ArbitraryBuilder<T>.setBuilder(
    property: KProperty1<T, *>,
    builder: ArbitraryBuilder<*>,
    limit: Long,
): ArbitraryBuilder<T> =
    this.setBuilder(
        ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))),
        builder,
        limit
    )

fun <T> ArbitraryBuilder<T>.setBuilder(property: KFunction1<T, *>, builder: ArbitraryBuilder<*>): ArbitraryBuilder<T> =
    this.setBuilder(
        ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))),
        builder
    )

fun <T> ArbitraryBuilder<T>.setBuilder(
    property: KFunction1<T, *>,
    builder: ArbitraryBuilder<*>,
    limit: Long,
): ArbitraryBuilder<T> =
    this.setBuilder(
        ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))),
        builder,
        limit
    )

fun <T> ArbitraryBuilder<T>.setNull(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNull(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))))

fun <T> ArbitraryBuilder<T>.setNull(property: KFunction1<T, *>): ArbitraryBuilder<T> =
    this.setNull(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))))

fun <T> ArbitraryBuilder<T>.setNotNull(property: KProperty1<T, *>): ArbitraryBuilder<T> =
    this.setNotNull(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))))

fun <T> ArbitraryBuilder<T>.setNotNull(property: KFunction1<T, *>): ArbitraryBuilder<T> =
    this.setNotNull(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))))

fun <T, U> ArbitraryBuilder<T>.setPostCondition(
    property: KProperty1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>,
): ArbitraryBuilder<T> =
    this.setPostCondition(
        ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))),
        clazz,
        filter
    )

fun <T, U> ArbitraryBuilder<T>.setPostCondition(
    property: KFunction1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>,
    limit: Long,
): ArbitraryBuilder<T> =
    this.setPostCondition(
        ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))),
        clazz,
        filter,
        limit
    )

fun <T, U> ArbitraryBuilder<T>.setPostCondition(
    property: KProperty1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>,
    limit: Long,
): ArbitraryBuilder<T> =
    this.setPostCondition(
        ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))),
        clazz,
        filter,
        limit
    )

fun <T, U> ArbitraryBuilder<T>.setPostCondition(
    property: KFunction1<T, *>,
    clazz: Class<U>,
    filter: Predicate<U>,
): ArbitraryBuilder<T> =
    this.setPostCondition(
        ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))),
        clazz,
        filter
    )

fun <T> ArbitraryBuilder<T>.size(property: KProperty1<T, *>, size: Int): ArbitraryBuilder<T> =
    this.size(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))), size)

fun <T> ArbitraryBuilder<T>.size(property: KFunction1<T, *>, size: Int): ArbitraryBuilder<T> =
    this.size(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))), size)

fun <T> ArbitraryBuilder<T>.size(property: KProperty1<T, *>, min: Int, max: Int): ArbitraryBuilder<T> =
    this.size(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))), min, max)

fun <T> ArbitraryBuilder<T>.size(property: KFunction1<T, *>, min: Int, max: Int): ArbitraryBuilder<T> =
    this.size(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))), min, max)

fun <T> ArbitraryBuilder<T>.minSize(property: KProperty1<T, *>, min: Int): ArbitraryBuilder<T> =
    this.minSize(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))), min)

fun <T> ArbitraryBuilder<T>.minSize(property: KFunction1<T, *>, min: Int): ArbitraryBuilder<T> =
    this.minSize(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))), min)

fun <T> ArbitraryBuilder<T>.maxSize(property: KProperty1<T, *>, max: Int): ArbitraryBuilder<T> =
    this.maxSize(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinProperty(property)))), max)

fun <T> ArbitraryBuilder<T>.maxSize(property: KFunction1<T, *>, max: Int): ArbitraryBuilder<T> =
    this.maxSize(ParsedExpressionGenerator(listOf(PropertyExpressionGenerator(KotlinGetterProperty(property)))), max)

infix fun <T, R, E> KProperty1<T, R?>.into(property: KProperty1<R, E?>): Exp<E> =
    Exp(
        ParsedExpressionGenerator(
            listOf(
                PropertyExpressionGenerator(KotlinProperty(this)),
                PropertyExpressionGenerator(KotlinProperty(property))
            )
        )
    )

infix fun <T, R, E> KProperty1<T, R?>.into(expList: ExpList<R, E>): Exp<E> =
    Exp(
        ParsedExpressionGenerator(
            listOf(
                PropertyExpressionGenerator(KotlinProperty(this)),
                expList
            )
        )
    )

infix fun <T, R, E> KFunction1<T, R?>.into(property: KFunction1<R, E?>): Exp<E> =
    Exp(
        ParsedExpressionGenerator(
            listOf(
                PropertyExpressionGenerator(KotlinGetterProperty(this)),
                PropertyExpressionGenerator(KotlinGetterProperty(property))
            )
        )
    )

infix fun <T, R, E> KFunction1<T, R?>.into(expList: ExpList<R, E>): Exp<E> =
    Exp(
        ParsedExpressionGenerator(
            listOf(
                PropertyExpressionGenerator(KotlinGetterProperty(this)),
                expList
            )
        )
    )

infix operator
fun <T, R : Collection<E>, E : Any> KProperty1<T, R?>.get(index: Int): ExpList<T, E> =
    ExpList(ArrayExpressionGenerator(KotlinProperty(this), index))

infix operator
fun <T, R : Collection<E>, E : Any> KProperty1<T, R?>.get(key: String): ExpList<T, E> =
    ExpList(MapExpressionGenerator(KotlinProperty(this), key))

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, R?>.get(index: Int): ExpList<T, N?> =
    ExpList(ArrayExpressionGenerator(KotlinProperty(this), index))

@JvmName("getNestedMap")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, R?>.get(key: String): ExpList<T, N?> =
    ExpList(MapExpressionGenerator(KotlinProperty(this), key))

infix operator
fun <T, R : Collection<E>, E : Any> KFunction1<T, R?>.get(index: Int): ExpList<T, E> =
    ExpList(ArrayExpressionGenerator(KotlinGetterProperty(this), index))

infix operator
fun <T, R : Collection<E>, E : Any> KFunction1<T, R?>.get(key: String): ExpList<T, E> =
    ExpList(MapExpressionGenerator(KotlinGetterProperty(this), key))

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KFunction1<T, R?>.get(index: Int): ExpList<T, N?> =
    ExpList(ArrayExpressionGenerator(KotlinGetterProperty(this), index))

@JvmName("getNestedMap")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KFunction1<T, R?>.get(key: String): ExpList<T, N?> =
    ExpList(MapExpressionGenerator(KotlinGetterProperty(this), key))

@Suppress("unused")
class ExpList<E, L> internal constructor(val delegate: ExpressionGenerator) : ExpressionGenerator by delegate {
    infix fun <R> into(property: KProperty1<L, R?>): Exp<R> =
        Exp(
            ParsedExpressionGenerator(
                listOf(
                    this,
                    PropertyExpressionGenerator(KotlinProperty(property))
                )
            )
        )

    infix fun <R> into(property: KFunction1<L, R?>): Exp<R> =
        Exp(
            ParsedExpressionGenerator(
                listOf(
                    this,
                    PropertyExpressionGenerator(KotlinGetterProperty(property))
                )
            )
        )
}

infix operator fun <T, R : Collection<E>, E : Any> ExpList<T, R?>.get(index: Int): ExpList<T, E> =
    ExpList(ParsedExpressionGenerator(listOf(delegate, IndexExpressionGenerator(index))))

infix operator fun <T, R : Collection<E>, E : Any> ExpList<T, R?>.get(key: String): ExpList<T, E> =
    ExpList(ParsedExpressionGenerator(listOf(delegate, KeyExpressionGenerator(key))))

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> ExpList<T, R?>.get(index: Int): ExpList<T, N?> =
    ExpList(ParsedExpressionGenerator(listOf(delegate, IndexExpressionGenerator(index))))

@JvmName("getNestedMap")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> ExpList<T, R?>.get(key: String): ExpList<T, N?> =
    ExpList(ParsedExpressionGenerator(listOf(delegate, KeyExpressionGenerator(key))))

private class ParsedExpressionGenerator(private val expressionGenerators: List<ExpressionGenerator>) :
    ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        expressionGenerators.joinToString(separator = "") { expressionGenerator ->
            expressionGenerator.generate(propertyNameResolver)
        }.removePrefix(".")
}

private class PropertyExpressionGenerator(private val property: Property) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        ".${propertyNameResolver.resolve(property)}"
}

private class IndexExpressionGenerator(val index: Int) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = "[$index]"
}

private class ArrayExpressionGenerator(private val property: Property, val index: Int) :
    ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        ".${propertyNameResolver.resolve(property)}[$index]"
}

private class MapExpressionGenerator(private val property: Property, val key: String) :
    ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        ".${propertyNameResolver.resolve(property)}[$key]"
}

private class KeyExpressionGenerator(private val key: String) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = "[$key]"
}

private class EmptyExpressionGenerator : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = ""
}

private class KotlinProperty<V, R>(private val property: KProperty1<V, R>) : Property {
    override fun getType(): Class<*> = property.javaField!!.type

    override fun getAnnotatedType(): AnnotatedType = property.javaField!!.annotatedType

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
    private val propertyName: String =
        getter.name.substringAfter("get", getter.name.substringAfter("is"))
            .replaceFirstChar { it.lowercaseChar() }

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
    private val propertyAnnotation: List<Annotation> = property?.annotations ?: listOf()
    private val getterAnnotation: List<Annotation> = getter.annotations
    private val javaFieldAnnotations: List<Annotation> = javaField?.annotations?.toList() ?: listOf()

    override fun getType(): Class<*> = type

    override fun getAnnotatedType(): AnnotatedType? =
        property?.javaField?.annotatedType ?: javaField?.annotatedType

    override fun getName(): String = propertyName

    override fun getAnnotations(): List<Annotation> =
        (propertyAnnotation + getterAnnotation + javaFieldAnnotations).distinct()

    @Suppress("UNCHECKED_CAST")
    override fun getValue(obj: Any?): Any? = getter.invoke(obj as V)
}
