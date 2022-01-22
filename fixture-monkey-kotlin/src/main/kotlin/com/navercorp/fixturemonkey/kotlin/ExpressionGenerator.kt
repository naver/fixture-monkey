package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver
import java.lang.reflect.AnnotatedType
import kotlin.reflect.KFunction2
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

class ParsedExpressionGenerator(private val expressionGenerators: List<ExpressionGenerator>) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        expressionGenerators.joinToString(separator = "") { expressionGenerator ->
            expressionGenerator.generate(
                propertyNameResolver
            )
        }.removePrefix(".")
}

class PropertyExpressionGenerator(private val property: Property) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String =
        ".${propertyNameResolver.resolve(property)}"
}

class IndexExpressionGenerator(val index: Long) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = "[$index]"
}

class AllIndexExpressionGenerator : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = "[*]"
}

class EmptyExpressionGenerator : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = ""
}

class DslBuilder<T>(private val expressionGenerator: ExpressionGenerator) {
    operator fun <R> rangeTo(property: KProperty1<T, R>): DslBuilder<R> = property(property)

    operator fun <R> rangeTo(indexWrapper: Index<T, R>): DslBuilder<R> = method(indexWrapper)

    operator fun <R> rangeTo(allIndexWrapper: AllIndex<T, R>): DslBuilder<R> = method(allIndexWrapper)

    infix fun <R> property(property: KProperty1<T, R>): DslBuilder<R> = DslBuilder(
        ParsedExpressionGenerator(
            listOf(
                this.expressionGenerator,
                PropertyExpressionGenerator(toProperty(property))
            )
        )
    )

    infix fun <R> method(indexWrapper: Index<T, R>): DslBuilder<R> = DslBuilder(
        ParsedExpressionGenerator(
            listOf(
                this.expressionGenerator,
                IndexExpressionGenerator(indexWrapper.index)
            )
        )
    )

    @Suppress("UNUSED_PARAMETER")
    infix fun <R> method(allIndexWrapper: AllIndex<T, R>): DslBuilder<R> = DslBuilder(
        ParsedExpressionGenerator(
            listOf(
                this.expressionGenerator,
                AllIndexExpressionGenerator()
            )
        )
    )

    private fun <T, R> toProperty(property: KProperty1<T, R>): Property = KotlinProperty(property)

    fun build() = expressionGenerator
}

@Suppress("UNUSED_PARAMETER")
fun <T, R> from(clazz: Class<T>, setup: DslBuilder<T>.() -> DslBuilder<R>): ExpressionGenerator =
    DslBuilder<T>(expressionGenerator = EmptyExpressionGenerator()).setup().build()

data class Index<L, R>(private val getter: KFunction2<L, Int, R>, val index: Long)

data class AllIndex<L, R>(private val getter: KFunction2<L, Int, R>)

class KotlinProperty<V, R>(val property: KProperty1<V, R>) : Property {
    override fun getType(): Class<*> = property.javaField!!.type

    override fun getAnnotatedType(): AnnotatedType = property.javaField!!.annotatedType

    override fun getName(): String = property.name

    override fun getAnnotations(): List<Annotation> = property.annotations

    @Suppress("UNCHECKED_CAST")
    override fun getValue(obj: Any): Any? = property.get(obj as V)
}
