package com.navercorp.fixturemonkey.kotlin.property

import com.navercorp.fixturemonkey.api.property.Property
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type
import kotlin.reflect.KProperty

data class KPropertyProperty(
    val kProperty: KProperty<*>
) : Property {

    override fun getType(): Type = this.annotatedType.type

    override fun getAnnotatedType(): AnnotatedType =
        com.navercorp.fixturemonkey.kotlin.type.getAnnotatedType(this.kProperty)

    override fun getName(): String = this.kProperty.name

    override fun getAnnotations(): List<Annotation> = this.annotatedType.annotations.toList()

    override fun getValue(obj: Any): Any? = this.kProperty.getter.call(obj)

    override fun isNullable(): Boolean = this.kProperty.returnType.isMarkedNullable
}
