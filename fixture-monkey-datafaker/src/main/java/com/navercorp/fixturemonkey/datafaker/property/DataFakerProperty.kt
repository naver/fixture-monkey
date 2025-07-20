package com.navercorp.fixturemonkey.datafaker.property

import com.navercorp.fixturemonkey.api.property.Property

class DataFakerProperty(private val originalProperty: Property) : Property {
    private val uniqueId = System.nanoTime().toString() + Math.random().toString()

    override fun getType() = originalProperty.type
    override fun getAnnotatedType() = originalProperty.annotatedType
    override fun getName() = originalProperty.name
    override fun getAnnotations() = originalProperty.annotations
    override fun getValue(instance: Any?) = originalProperty.getValue(instance)
    override fun isNullable() = originalProperty.isNullable

    override fun hashCode(): Int = uniqueId.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataFakerProperty) return false
        return uniqueId == other.uniqueId
    }

    override fun toString(): String = "DataFakerNonCacheable(${originalProperty.name}:$uniqueId)"
}
