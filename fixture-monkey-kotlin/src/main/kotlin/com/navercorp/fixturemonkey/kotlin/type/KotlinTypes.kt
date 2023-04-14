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

package com.navercorp.fixturemonkey.kotlin.type

import com.navercorp.fixturemonkey.api.type.Types
import org.apiguardian.api.API
import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

@API(since = "0.4.0", status = API.Status.EXPERIMENTAL)
@OptIn(ExperimentalStdlibApi::class)
fun getAnnotatedType(ownerType: AnnotatedType, kProperty: KProperty<*>): AnnotatedType {
    val type = kProperty.returnType.javaType
    val annotations = mutableSetOf<Annotation>()
    annotations.addAll(kProperty.findAnnotations())
    annotations.addAll(kProperty.javaField?.annotations?.toList() ?: listOf())
    annotations.addAll(kProperty.getter.annotations.toList())
    val annotationArray = annotations.toTypedArray()

    if (ownerType !is AnnotatedParameterizedType) {
        return KotlinAnnotatedType(type, annotationArray)
    }

    val ownerGenericsTypes = ownerType.annotatedActualTypeArguments
    if (ownerGenericsTypes == null || ownerGenericsTypes.isEmpty()) {
        return KotlinAnnotatedType(type, annotationArray)
    }

    val parameterizedType = ownerType.type as ParameterizedType
    val ownerActualType = Types.getActualType(parameterizedType.rawType)
    val ownerTypeVariableParameters = ownerActualType.typeParameters.toList()

    if (TypeVariable::class.java.isAssignableFrom(type::class.java)) {
        val index = ownerTypeVariableParameters.indexOf(type)
        return ownerGenericsTypes[index]
    }

    if (type !is ParameterizedType) {
        return KotlinAnnotatedType(type, annotationArray)
    }

    val propertyGenericsTypes = type.actualTypeArguments
    if (propertyGenericsTypes.isEmpty()) {
        return KotlinAnnotatedType(type, annotationArray)
    }

    val resolvedGenericsTypes = mutableListOf<AnnotatedType>()
    val resolvedTypes = mutableListOf<Type>()
    for (i in propertyGenericsTypes.indices) {
        val generics = propertyGenericsTypes[i]
        if (generics is ParameterizedType || generics::class.java == Class::class.java) {
            resolvedGenericsTypes.add(i, KotlinAnnotatedType(type, arrayOf()))
            resolvedTypes.add(i, generics)
            continue
        }

        if (TypeVariable::class.java.isAssignableFrom(generics::class.java)) {
            val index = ownerTypeVariableParameters.indexOf(generics)
            val typeVariableGenerics = ownerGenericsTypes[index]
            resolvedGenericsTypes.add(i, typeVariableGenerics)
            resolvedTypes.add(i, typeVariableGenerics.type)
        }
    }

    val resolvedTypesArray = resolvedTypes.toTypedArray()
    val resolveType = object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = resolvedTypesArray

        override fun getRawType(): Type = type.rawType

        override fun getOwnerType(): Type? = type.ownerType
    }

    val resolvedGenericsTypesArray = resolvedGenericsTypes.toTypedArray()
    return object : AnnotatedParameterizedType {
        override fun getAnnotatedActualTypeArguments(): Array<AnnotatedType> = resolvedGenericsTypesArray

        override fun getType(): Type = resolveType

        override fun getAnnotations(): Array<Annotation> = annotationArray

        override fun getDeclaredAnnotations(): Array<Annotation> = annotationArray

        override fun <T : Annotation?> getAnnotation(annotationClass: Class<T>): T? =
            @Suppress("UNCHECKED_CAST")
            annotations
                .find { it.annotationClass.java == annotationClass }
                .let { it as T }
    }
}

internal class KotlinAnnotatedType(
    private val _type: Type,
    private val _annotations: Array<Annotation>,
) : AnnotatedType {
    override fun getType(): Type = this._type

    override fun <T : Annotation?> getAnnotation(annotationClass: Class<T>): T? =
        @Suppress("UNCHECKED_CAST")
        annotations
            .find { it.annotationClass.java == annotationClass }
            .let { it as T }

    override fun getAnnotations(): Array<Annotation> = this._annotations

    override fun getDeclaredAnnotations(): Array<Annotation> = this._annotations
}

fun KFunction<*>.getPropertyName(): String {
    return if (this.name.startsWith("get")) {
        this.name.substringAfter("get")
            .replaceFirstChar { it.lowercaseChar() }
    } else if (this.returnType.javaType == kotlin.Boolean::class.java && this.name.startsWith("is")) {
        this.name.substringAfter("is")
            .replaceFirstChar { it.lowercaseChar() }
    } else {
        this.name
    }
}
