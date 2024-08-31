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

package com.navercorp.fixturemonkey.kotlin.property

import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.kotlin.type.actualType
import com.navercorp.fixturemonkey.kotlin.type.toTypeReference
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type
import java.util.Optional
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

internal data class KotlinConstructorParameterProperty(
    private val annotatedType: AnnotatedType,
    val kParameter: KParameter,
    private val parameterName: String?,
    private val constructor: KFunction<*>,
) : Property {
    override fun getType(): Type = annotatedType.type

    override fun getAnnotatedType(): AnnotatedType = annotatedType

    override fun getName(): String? = parameterName

    override fun getAnnotations(): List<Annotation> = kParameter.annotations

    override fun getValue(obj: Any?): Any? {
        return getKotlinMemberProperties(constructor.returnType.toTypeReference().type.actualType())
            .firstOrNull { it.name == parameterName }
            ?.call(obj)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Annotation> getAnnotation(annotationClass: Class<T>): Optional<T> = annotations
        .find { it.annotationClass.java == annotationClass }
        .let { Optional.of(it as T) }

    override fun isNullable(): Boolean = kParameter.type.isMarkedNullable
}
