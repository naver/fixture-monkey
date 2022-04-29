package com.navercorp.fixturemonkey.kotlin.type

import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

private val KPROPERTY_ANNOTATED_TYPE_MAP = ConcurrentHashMap<KProperty<*>, AnnotatedType>()

@OptIn(ExperimentalStdlibApi::class)
fun getAnnotatedType(kProperty: KProperty<*>): AnnotatedType =
    KPROPERTY_ANNOTATED_TYPE_MAP.computeIfAbsent(kProperty) {
        val _type = kProperty.returnType.javaType
        val _annotations = mutableSetOf<Annotation>()
        _annotations.addAll(kProperty.findAnnotations())
        _annotations.addAll(kProperty.javaField?.annotations?.toList() ?: listOf())
        _annotations.addAll(kProperty.getter.annotations.toList())
        val _annotationArray = _annotations.toTypedArray()
        object : AnnotatedType {
            override fun getType(): Type = _type

            override fun getDeclaredAnnotations(): Array<Annotation> = _annotationArray

            override fun getAnnotations(): Array<Annotation> = declaredAnnotations

            override fun <T : Annotation?> getAnnotation(annotationClass: Class<T>): T =
                annotations
                    .find { it.annotationClass.java == annotationClass }
                    .let { it as T }
        }
    }
