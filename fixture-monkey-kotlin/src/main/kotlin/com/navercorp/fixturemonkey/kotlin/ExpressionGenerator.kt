package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver
import java.lang.reflect.AnnotatedType
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

class Exp<T> internal constructor(val delegate: ExpressionGenerator) : ExpressionGenerator by delegate {
    constructor() : this(EmptyExpressionGenerator())

    infix fun <R> dot(property: KProperty1<T, R>): Exp<R> =
        Exp(ParsedExpressionGenerator(listOf(delegate, PropertyExpressionGenerator(KotlinProperty(property)))))

    @JvmName("dotList")
    infix fun <R : Collection<E>, E : Any> dot(property: KProperty1<T, R>): ExpList<E> = ExpList(
        ParsedExpressionGenerator(
            listOf(
                delegate,
                PropertyExpressionGenerator(KotlinProperty(property))
            )
        )
    )

    @JvmName("dotNestedList")
    infix fun <R : Collection<N>, N : Collection<E>, E : Any> dot(property: KProperty1<T, R>): ExpNestedList<E> =
        ExpNestedList(
            ParsedExpressionGenerator(
                listOf(
                    delegate,
                    PropertyExpressionGenerator(KotlinProperty(property))
                )
            )
        )

    infix fun <R> dot(expList: ExpList<R>): Exp<R> = Exp(ParsedExpressionGenerator(listOf(delegate, expList.delegate)))

    infix operator fun <R> div(property: KProperty1<T, R>): Exp<R> = dot(property)

    infix operator fun <R : Collection<E>, E : Any> div(property: KProperty1<T, R>) = dot(property)

    infix operator fun <R : Collection<N>, N : Collection<E>, E : Any> div(property: KProperty1<T, R>) = dot(property)

    infix operator fun <R> div(expList: ExpList<R>): Exp<R> = dot(expList)
}

operator fun <T, R : Collection<E>, E : Any> KProperty1<T, R>.get(index: Int): ExpList<E> =
    ExpList(ArrayExpressionGenerator(KotlinProperty(this), index))

@JvmName("getNestedList")
operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, R>.get(index: Int): ExpNestedList<E> =
    ExpNestedList(ArrayExpressionGenerator(KotlinProperty(this), index))

@JvmName("getNestedList")
operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, R>.get(key: String): ExpNestedList<E> =
    ExpNestedList(ArrayWithKeyExpressionGenerator(KotlinProperty(this), key))

class ExpList<T> internal constructor(val delegate: ExpressionGenerator) : ExpressionGenerator by delegate {
    infix operator fun get(index: Int): Exp<T> {
        return Exp(ParsedExpressionGenerator(listOf(delegate, IndexExpressionGenerator(index))))
    }

    infix operator fun get(key: String): Exp<T> {
        return Exp(ParsedExpressionGenerator(listOf(delegate, KeyExpressionGenerator(key))))
    }
}

class ExpNestedList<T> internal constructor(private val delegate: ExpressionGenerator) :
    ExpressionGenerator by delegate {
    infix operator fun get(index: Int): ExpList<T> {
        return ExpList(ParsedExpressionGenerator(listOf(delegate, IndexExpressionGenerator(index))))
    }

    infix operator fun get(key: String): ExpList<T> {
        return ExpList(ParsedExpressionGenerator(listOf(delegate, KeyExpressionGenerator(key))))
    }
}

private class ParsedExpressionGenerator(private val expressionGenerators: List<ExpressionGenerator>) :
    ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        expressionGenerators.joinToString(separator = "") { expressionGenerator ->
            expressionGenerator.generate(
                propertyNameResolver
            )
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

private class ArrayWithKeyExpressionGenerator(private val property: Property, val key: String) : ExpressionGenerator {
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
