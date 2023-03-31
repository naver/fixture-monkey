package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinProperty

interface JoinableExpressionGenerator<F, T> : ExpressionGenerator {
    infix fun <R> into(property: KProperty1<T, R?>): JoinableExpressionGenerator<F, R>

    infix fun <R> intoGetter(getter: KFunction1<T, R?>): JoinableExpressionGenerator<F, R>

    infix fun <R> into(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R>

    infix fun <R> intoGetter(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R>
}

infix fun <F, T : Any, R : Any> KFunction1<F, T?>.intoGetter(getter: KFunction1<T, R?>): JoinableExpressionGenerator<T, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                property(this),
                DotExpressionGenerator(),
                property(getter),
            ),
        ),
    )

infix fun <F, T : Any, R> KFunction1<F, T?>.into(property: KProperty1<T, R?>): JoinableExpressionGenerator<T, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                property(this),
                DotExpressionGenerator(),
                property(property),
            ),
        ),
    )

infix fun <F, T : Any, R> KFunction1<F, T?>.into(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                property(this),
                DotExpressionGenerator(),
                expressionGenerator,
            ),
        ),
    )

infix fun <F, T : Any, R> KFunction1<F, T?>.intoGetter(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                property(this),
                DotExpressionGenerator(),
                expressionGenerator,
            ),
        ),
    )

infix fun <F, T : Any, R> KProperty1<F, T?>.into(property: KProperty1<T, R?>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                property(this),
                DotExpressionGenerator(),
                property(property),
            ),
        ),
    )

infix fun <F, T : Any, R> KProperty1<F, T?>.into(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                property(this),
                DotExpressionGenerator(),
                expressionGenerator,
            ),
        ),
    )

infix fun <F, T : Any, R> KProperty1<F, T?>.intoGetter(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                property(this),
                DotExpressionGenerator(),
                expressionGenerator,
            ),
        ),
    )

infix fun <F, T : Any, R> KProperty1<F, T?>.intoGetter(getter: KFunction1<T, R?>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                property(this),
                DotExpressionGenerator(),
                property(getter),
            ),
        ),
    )

infix operator fun <T, R : Collection<E>, E : Any> JoinableExpressionGenerator<T, R?>.get(index: Int): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                this,
                IndexExpressionGenerator(index),
            ),
        ),
    )

infix operator fun <T, R : Collection<E>, E : Any> JoinableExpressionGenerator<T, R?>.get(key: String): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                this,
                KeyExpressionGenerator(key),
            ),
        ),
    )

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> JoinableExpressionGenerator<T, R?>.get(
    index: Int,
): JoinableExpressionGenerator<T, N?> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                this,
                IndexExpressionGenerator(index),
            ),
        ),
    )

@JvmName("getNestedMap")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> JoinableExpressionGenerator<T, R?>.get(
    key: String,
): JoinableExpressionGenerator<T, N?> =
    DefaultJoinableExpressionGenerator(JoinExpressionGenerator(listOf(this, KeyExpressionGenerator(key))))

infix operator fun <T, R : Collection<E>, E : Any> KProperty1<T, R?>.get(index: Int): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(array(this, index))

infix operator fun <T, R : Collection<E>, E : Any> KProperty1<T, R?>.get(key: String): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(map(this, key))

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, R?>.get(
    index: Int,
): JoinableExpressionGenerator<T, N?> = DefaultJoinableExpressionGenerator(array(this, index))

@JvmName("getNestedMap")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KProperty1<T, R?>.get(
    key: String,
): JoinableExpressionGenerator<T, N?> = DefaultJoinableExpressionGenerator(map(this, key))

infix operator fun <T, R : Collection<E>, E : Any> KFunction1<T, R?>.get(index: Int): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(array(this, index))

infix operator fun <T, R : Collection<E>, E : Any> KFunction1<T, R?>.get(key: String): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(map(this, key))

@JvmName("getNestedList")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KFunction1<T, R?>.get(
    index: Int,
): JoinableExpressionGenerator<T, N?> = DefaultJoinableExpressionGenerator(array(this, index))

@JvmName("getNestedMap")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> KFunction1<T, R?>.get(
    key: String,
): JoinableExpressionGenerator<T, N?> = DefaultJoinableExpressionGenerator(map(this, key))

class DefaultJoinableExpressionGenerator<F, T>(
    private val delegate: ExpressionGenerator,
) : JoinableExpressionGenerator<F, T> {
    override fun <R> into(property: KProperty1<T, R?>): JoinableExpressionGenerator<F, R> =
        DefaultJoinableExpressionGenerator(
            JoinExpressionGenerator(
                listOf(
                    delegate,
                    DotExpressionGenerator(),
                    property(property),
                ),
            ),
        )

    override fun <R> intoGetter(getter: KFunction1<T, R?>): JoinableExpressionGenerator<F, R> =
        DefaultJoinableExpressionGenerator(
            JoinExpressionGenerator(
                listOf(
                    delegate,
                    DotExpressionGenerator(),
                    property(getter),
                ),
            ),
        )

    override fun <R> into(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
        DefaultJoinableExpressionGenerator(
            JoinExpressionGenerator(
                listOf(
                    delegate,
                    DotExpressionGenerator(),
                    expressionGenerator,
                ),
            ),
        )

    override fun <R> intoGetter(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
        DefaultJoinableExpressionGenerator(
            JoinExpressionGenerator(
                listOf(
                    delegate,
                    DotExpressionGenerator(),
                    expressionGenerator,
                ),
            ),
        )

    override fun generate(propertyNameResolver: PropertyNameResolver?): String = delegate.generate(propertyNameResolver)
}

private class JoinExpressionGenerator(
    private val expressionGenerators: List<ExpressionGenerator>,
) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        expressionGenerators.joinToString(separator = "") { expressionGenerator ->
            expressionGenerator.generate(propertyNameResolver)
        }
}

private class IndexExpressionGenerator(private val index: Int) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        "[$index]"
}

private class ArrayExpressionGenerator(
    private val property: Property,
    private val index: Int,
) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        "${propertyNameResolver.resolve(property)}[$index]"
}

private class MapExpressionGenerator(
    private val property: Property,
    private val key: String,
) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        "${propertyNameResolver.resolve(property)}[$key]"
}

private class KeyExpressionGenerator(private val key: String) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        "[$key]"
}

private class DotExpressionGenerator : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = "."
}

private class EmptyExpressionGenerator : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = ""
}

internal fun <R, E> property(property: KProperty1<R, E?>): ExpressionGenerator =
    PropertyExpressionGenerator(KotlinProperty(property))

internal fun <R, E> property(function: KFunction1<R, E?>): ExpressionGenerator =
    PropertyExpressionGenerator(KotlinGetterProperty(function))

internal fun <T, R : Collection<E>, E : Any> array(function: KFunction1<T, R?>, index: Int): ExpressionGenerator =
    ArrayExpressionGenerator(KotlinGetterProperty(function), index)

internal fun <T, R : Collection<E>, E : Any> array(property: KProperty1<T, R?>, index: Int): ExpressionGenerator =
    ArrayExpressionGenerator(KotlinProperty(property), index)

internal fun <T, R : Collection<E>, E : Any> map(function: KFunction1<T, R?>, key: String): ExpressionGenerator =
    MapExpressionGenerator(KotlinGetterProperty(function), key)

internal fun <T, R : Collection<E>, E : Any> map(property: KProperty1<T, R?>, key: String): ExpressionGenerator =
    MapExpressionGenerator(KotlinProperty(property), key)

private class PropertyExpressionGenerator(private val property: Property) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        propertyNameResolver.resolve(property)
}

private class KotlinProperty<V, R>(private val property: KProperty1<V, R>) : Property {
    private val propertyAnnotations: List<Annotation> = property.annotations
    private val getterAnnotations: List<Annotation> =
        property.getter.annotations.toList()
    private val fieldAnnotations: List<Annotation> =
        property.javaField?.annotations?.toList() ?: listOf()

    override fun getType(): Class<*> = property.javaField!!.type

    override fun getAnnotatedType(): AnnotatedType =
        property.javaField!!.annotatedType

    override fun getName(): String = property.name

    override fun getAnnotations(): List<Annotation> =
        (propertyAnnotations + getterAnnotations + fieldAnnotations).distinct()

    @Suppress("UNCHECKED_CAST")
    override fun getValue(instance: Any): Any? = property.get(instance as V)

    override fun isNullable(): Boolean = property.returnType.isMarkedNullable
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
        } else if (getter.returnType.javaType == Boolean::class.java && getter.name.startsWith("is")) {
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
    private val propertyAnnotations: List<Annotation> =
        property?.annotations ?: listOf()
    private val getterAnnotations: List<Annotation> = getter.annotations
    private val fieldAnnotations: List<Annotation> =
        javaField?.annotations?.toList() ?: listOf()

    override fun getType(): Class<*> = type

    override fun getAnnotatedType(): AnnotatedType? =
        property?.javaField?.annotatedType ?: javaField?.annotatedType

    override fun getName(): String = propertyName

    override fun getAnnotations(): List<Annotation> =
        (propertyAnnotations + getterAnnotations + fieldAnnotations).distinct()

    @Suppress("UNCHECKED_CAST")
    override fun getValue(instance: Any?): Any? = getter.invoke(instance as V)

    override fun isNullable(): Boolean = getter.returnType.isMarkedNullable
}
