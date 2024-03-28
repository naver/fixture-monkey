@file:Suppress("unused")

package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver
import com.navercorp.fixturemonkey.kotlin.type.getPropertyName
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinProperty

// property
infix fun <F, T : Any, R> KProperty1<F, T?>.into(property: KProperty1<T, R?>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                propertyExpressionGenerator(this),
                DotExpressionGenerator(),
                propertyExpressionGenerator(property),
            ),
        ),
    )

infix fun <F, T : Any, R> KProperty1<F, T?>.intoGetter(getter: KFunction1<T, R?>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                propertyExpressionGenerator(this),
                DotExpressionGenerator(),
                propertyExpressionGenerator(getter),
            ),
        ),
    )

infix fun <F, T : Any, R> KProperty1<F, T?>.into(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                propertyExpressionGenerator(this),
                DotExpressionGenerator(),
                expressionGenerator,
            ),
        ),
    )

infix fun <F, T : Any, R> KProperty1<F, T?>.intoGetter(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                propertyExpressionGenerator(this),
                DotExpressionGenerator(),
                expressionGenerator,
            ),
        ),
    )

// rootProperty
@JvmName("rootInto")
infix fun <F, T : Any, R> KProperty1<F, Class<T>>.into(property: KProperty1<T, R?>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(propertyExpressionGenerator(property))

@JvmName("rootIntoGetter")
infix fun <F, T : Any, R> KProperty1<F, Class<T>>.intoGetter(getter: KFunction1<T, R?>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(propertyExpressionGenerator(getter))

@JvmName("rootInto")
infix fun <F, T : Any, R> KProperty1<F, Class<T>>.into(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                propertyExpressionGenerator(this),
                DotExpressionGenerator(),
                expressionGenerator,
            ),
        ),
    )

@JvmName("rootIntoGetter")
infix fun <F, T : Any, R> KProperty1<F, Class<T>>.intoGetter(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                propertyExpressionGenerator(this),
                DotExpressionGenerator(),
                expressionGenerator,
            ),
        ),
    )

// function
infix fun <F, T : Any, R> KFunction1<F, T?>.into(property: KProperty1<T, R?>): JoinableExpressionGenerator<T, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                propertyExpressionGenerator(this),
                DotExpressionGenerator(),
                propertyExpressionGenerator(property),
            ),
        ),
    )

infix fun <F, T : Any, R : Any> KFunction1<F, T?>.intoGetter(getter: KFunction1<T, R?>): JoinableExpressionGenerator<T, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                propertyExpressionGenerator(this),
                DotExpressionGenerator(),
                propertyExpressionGenerator(getter),
            ),
        ),
    )

infix fun <F, T : Any, R> KFunction1<F, T?>.into(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                propertyExpressionGenerator(this),
                DotExpressionGenerator(),
                expressionGenerator,
            ),
        ),
    )

infix fun <F, T : Any, R> KFunction1<F, T?>.intoGetter(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                propertyExpressionGenerator(this),
                DotExpressionGenerator(),
                expressionGenerator,
            ),
        ),
    )

// rootFunction
@JvmName("root")
infix fun <F, T : Any, R> KFunction1<F, Class<T>>.into(property: KProperty1<T, R?>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(propertyExpressionGenerator(property))

@JvmName("root")
infix fun <F, T : Any, R> KFunction1<F, Class<T>>.intoGetter(getter: KFunction1<T, R?>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(propertyExpressionGenerator(getter))

@JvmName("rootInto")
infix fun <F, T : Any, R> KFunction1<F, Class<T>>.into(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                propertyExpressionGenerator(this),
                DotExpressionGenerator(),
                expressionGenerator,
            ),
        ),
    )

@JvmName("rootIntoGetter")
infix fun <F, T : Any, R> KFunction1<F, Class<T>>.intoGetter(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                propertyExpressionGenerator(this),
                DotExpressionGenerator(),
                expressionGenerator,
            ),
        ),
    )

// ExpressionGenerator Index
@JvmName("index")
infix operator fun <T, R : Collection<E>, E : Any> JoinableExpressionGenerator<T, R?>.get(index: Int): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                this,
                IndexExpressionGenerator(index),
            ),
        ),
    )

@JvmName("nestedIndex")
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

@JvmName("allIndex")
infix operator fun <T, R : Collection<E>, E : Any> JoinableExpressionGenerator<T, R?>.get(allIndex: String): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                this,
                AllIndexExpressionGenerator(allIndex),
            ),
        ),
    )

@JvmName("nestedAllIndex")
infix operator fun <T, R : Collection<N>, N : Collection<E>, E : Any> JoinableExpressionGenerator<T, R?>.get(
    allIndex: String,
): JoinableExpressionGenerator<T, N?> =
    DefaultJoinableExpressionGenerator(JoinExpressionGenerator(listOf(this, AllIndexExpressionGenerator(allIndex))))

@JvmName("array")
infix operator fun <T, E : Any> JoinableExpressionGenerator<T, Array<E>>.get(index: Int): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                this,
                IndexExpressionGenerator(index),
            ),
        ),
    )

@JvmName("nestedArray")
infix operator fun <T, E : Any> JoinableExpressionGenerator<T, Array<Array<E>>>.get(index: Int): JoinableExpressionGenerator<T, Array<E>> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                this,
                IndexExpressionGenerator(index),
            ),
        ),
    )

@JvmName("arrayAllIndex")
infix operator fun <T, E : Any> JoinableExpressionGenerator<T, Array<E>>.get(key: String): JoinableExpressionGenerator<T, E> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                this,
                AllIndexExpressionGenerator(key),
            ),
        ),
    )

@JvmName("nestedArrayAllIndex")
infix operator fun <T, E : Any> JoinableExpressionGenerator<T, Array<Array<E>>>.get(key: String): JoinableExpressionGenerator<T, Array<E>> =
    DefaultJoinableExpressionGenerator(
        JoinExpressionGenerator(
            listOf(
                this,
                AllIndexExpressionGenerator(key),
            ),
        ),
    )

// ExpressionGenerator factory
fun <R, E> propertyExpressionGenerator(property: KProperty1<R, E?>): ExpressionGenerator =
    PropertyExpressionGenerator(KotlinProperty(property))

fun <R, E> propertyExpressionGenerator(function: KFunction1<R, E?>): ExpressionGenerator =
    PropertyExpressionGenerator(KotlinGetterProperty(function))

@JvmName("propertyIndexExpressionGenerator")
internal fun <T, R : Collection<E>, E : Any> indexExpressionGenerator(
    property: KProperty1<T, R?>,
    index: Int
): ExpressionGenerator =
    ElementIndexExpressionGenerator(KotlinProperty(property), index)

@JvmName("getterIndexExpressionGenerator")
internal fun <T, R : Collection<E>, E : Any> indexExpressionGenerator(
    function: KFunction1<T, R?>,
    index: Int
): ExpressionGenerator =
    ElementIndexExpressionGenerator(KotlinGetterProperty(function), index)

internal fun <T, E : Any> indexExpressionGenerator(
    function: KFunction1<T, Array<E>?>,
    index: Int
): ExpressionGenerator =
    ElementIndexExpressionGenerator(KotlinGetterProperty(function), index)

@JvmName("rootIndexExpressionGenerator")
internal fun <T> indexExpressionGenerator(property: KProperty1<T, Class<T>>, index: Int): ExpressionGenerator =
    ElementIndexExpressionGenerator(
        object : Property by KotlinProperty(property) {
            override fun getName(): String = "$"
        },
        index
    )

@JvmName("rootArrayExpressionGenerator")
internal fun <T> indexExpressionGenerator(property: KFunction1<T, Class<T>>, index: Int): ExpressionGenerator =
    ElementIndexExpressionGenerator(
        object : Property by KotlinGetterProperty(property) {
            override fun getName(): String = "$"
        },
        index
    )

internal fun <T, E : Any> indexExpressionGenerator(
    property: KProperty1<T, Array<E>?>,
    index: Int
): ExpressionGenerator =
    ElementIndexExpressionGenerator(KotlinProperty(property), index)

@JvmName("propertyAllIndexExpressionGenerator")
internal fun <T, R : Collection<E>, E : Any> allIndexExpressionGenerator(
    property: KProperty1<T, R?>,
    key: String
): ExpressionGenerator =
    MapExpressionGenerator(KotlinProperty(property), key)

@JvmName("propertyArrayAllIndexExpressionGenerator")
internal fun <T, E : Any> allIndexExpressionGenerator(
    property: KProperty1<T, Array<E>?>,
    key: String
): ExpressionGenerator =
    MapExpressionGenerator(KotlinProperty(property), key)

@JvmName("getterAllIndexExpressionGenerator")
internal fun <T, R : Collection<E>, E : Any> allIndexExpressionGenerator(
    function: KFunction1<T, R?>,
    key: String
): ExpressionGenerator =
    MapExpressionGenerator(KotlinGetterProperty(function), key)

@JvmName("getterArrayAllIndexExpressionGenerator")
internal fun <T, E : Any> allIndexExpressionGenerator(
    property: KFunction1<T, Array<E>?>,
    key: String
): ExpressionGenerator =
    MapExpressionGenerator(KotlinGetterProperty(property), key)

@JvmName("rootAllIndexExpressionGenerator")
internal fun <T> allIndexExpressionGenerator(property: KProperty1<T, Class<T>>, key: String): ExpressionGenerator =
    MapExpressionGenerator(
        object : Property by KotlinProperty(property) {
            override fun getName(): String = "$"
        },
        key
    )

@JvmName("rootAllIndexExpressionGenerator")
internal fun <T> allIndexExpressionGenerator(property: KFunction1<T, Class<T>>, key: String): ExpressionGenerator =
    MapExpressionGenerator(
        object : Property by KotlinGetterProperty(property) {
            override fun getName(): String = "$"
        },
        key
    )

private class PropertyExpressionGenerator(private val property: Property) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        propertyNameResolver.resolve(property)
}

// ExpressionGenerator
interface JoinableExpressionGenerator<F, T> : ExpressionGenerator {
    infix fun <R> into(property: KProperty1<T, R?>): JoinableExpressionGenerator<F, R>

    infix fun <R> intoGetter(getter: KFunction1<T, R?>): JoinableExpressionGenerator<F, R>

    infix fun <R> into(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R>

    infix fun <R> intoGetter(expressionGenerator: JoinableExpressionGenerator<T, R>): JoinableExpressionGenerator<F, R>
}

class DefaultJoinableExpressionGenerator<F, T>(
    private val delegate: ExpressionGenerator,
) : JoinableExpressionGenerator<F, T> {
    override fun <R> into(property: KProperty1<T, R?>): JoinableExpressionGenerator<F, R> =
        DefaultJoinableExpressionGenerator(
            JoinExpressionGenerator(
                listOf(
                    delegate,
                    DotExpressionGenerator(),
                    propertyExpressionGenerator(property),
                ),
            ),
        )

    override fun <R> intoGetter(getter: KFunction1<T, R?>): JoinableExpressionGenerator<F, R> =
        DefaultJoinableExpressionGenerator(
            JoinExpressionGenerator(
                listOf(
                    delegate,
                    DotExpressionGenerator(),
                    propertyExpressionGenerator(getter),
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

private class ElementIndexExpressionGenerator(
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

private class AllIndexExpressionGenerator(private val key: String) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        "[$key]"
}

private class DotExpressionGenerator : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = "."
}

private class EmptyExpressionGenerator : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = ""
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

    private fun resolvePropertyName(): String = getter.getPropertyName()

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
