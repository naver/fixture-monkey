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

import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Field
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

infix operator fun <T, R : Collection<E>, E : Any> KProperty1<T, R?>.get(index: Int): ExpList<T, E> =
    ExpList(ArrayExpressionGenerator(KotlinProperty(this), index))

infix operator fun <T, R : Collection<E>, E : Any> KProperty1<T, R?>.get(key: String): ExpList<T, E> =
    ExpList(MapExpressionGenerator(KotlinProperty(this), key))

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, R?>.get(index: Int): ExpList<T, N?> =
    ExpList(ArrayExpressionGenerator(KotlinProperty(this), index))

@JvmName("getNestedMap")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, R?>.get(key: String): ExpList<T, N?> =
    ExpList(MapExpressionGenerator(KotlinProperty(this), key))

infix operator fun <T, R : Collection<E>, E : Any> KFunction1<T, R?>.get(index: Int): ExpList<T, E> =
    ExpList(ArrayExpressionGenerator(KotlinGetterProperty(this), index))

infix operator fun <T, R : Collection<E>, E : Any> KFunction1<T, R?>.get(key: String): ExpList<T, E> =
    ExpList(MapExpressionGenerator(KotlinGetterProperty(this), key))

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KFunction1<T, R?>.get(index: Int): ExpList<T, N?> =
    ExpList(ArrayExpressionGenerator(KotlinGetterProperty(this), index))

@JvmName("getNestedMap")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KFunction1<T, R?>.get(key: String): ExpList<T, N?> =
    ExpList(MapExpressionGenerator(KotlinGetterProperty(this), key))

@Suppress("unused")
class ExpList<E, L> internal constructor(val delegate: ExpressionGenerator) : ExpressionGenerator by delegate

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

private class ArrayExpressionGenerator(private val property: Property, val index: Int) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        ".${propertyNameResolver.resolve(property)}[$index]"
}

private class MapExpressionGenerator(private val property: Property, val key: String) : ExpressionGenerator {
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
    private val type: Class<*> = getter.returnType.javaType as Class<*>
    private val propertyName: String =
        getter.name.substringAfter("get", getter.name.substringAfter("is")).replaceFirstChar { it.lowercaseChar() }

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

    override fun getAnnotatedType(): AnnotatedType = property?.javaField?.annotatedType ?: javaField?.annotatedType!!

    override fun getName(): String = propertyName

    override fun getAnnotations(): List<Annotation> =
        (propertyAnnotation + getterAnnotation + javaFieldAnnotations).distinct()

    @Suppress("UNCHECKED_CAST")
    override fun getValue(obj: Any?): Any? = getter.invoke(obj as V)
}
