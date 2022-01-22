package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver
import java.lang.reflect.AnnotatedType
import kotlin.reflect.KFunction2
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

class DslBuilder<T>(private val expressionGenerator: ExpressionGenerator) {
    fun build() = expressionGenerator

    private fun <T, R> toProperty(property: KProperty1<T, R>): Property = KotlinProperty(property)

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

    operator fun <R> rangeTo(property: KProperty1<T, R>): DslBuilder<R> = property(property)

    operator fun <R> rangeTo(indexWrapper: Index<T, R>): DslBuilder<R> = method(indexWrapper)

    operator fun <R> rangeTo(allIndexWrapper: AllIndex<T, R>): DslBuilder<R> = method(allIndexWrapper)
}

data class Index<L, R>(private val getter: KFunction2<L, Int, R>, val index: Long)

data class AllIndex<L, R>(private val getter: KFunction2<L, Int, R>)

@Suppress("UNUSED_PARAMETER")
fun <T, R> from(clazz: Class<T>, setup: DslBuilder<T>.() -> DslBuilder<R>): ExpressionGenerator =
    DslBuilder<T>(expressionGenerator = EmptyExpressionGenerator()).setup().build()

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

private class IndexExpressionGenerator(val index: Long) : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = "[$index]"
}

private class AllIndexExpressionGenerator : ExpressionGenerator {
    override fun generate(propertyNameResolver: PropertyNameResolver): String = "[*]"
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
