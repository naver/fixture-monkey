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
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.kotlin.type.actualType
import com.navercorp.fixturemonkey.kotlin.type.toTypeReference
import com.navercorp.objectfarm.api.type.JvmType
import java.lang.reflect.AnnotatedType
import java.util.Optional
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

internal data class KotlinConstructorParameterProperty(
    private val annotatedType: AnnotatedType,
    val kParameter: KParameter,
    private val parameterName: String?,
    private val constructor: KFunction<*>,
) : Property {
    private val cachedJvmType: JvmType = Types.toJvmType(
        annotatedType,
        kParameter.annotations,
        kParameter.type.isMarkedNullable,
    )

    override fun getJvmType(): JvmType = cachedJvmType

    override fun getName(): String? = parameterName

    override fun getAnnotations(): List<Annotation> = kParameter.annotations

    @Suppress("UNCHECKED_CAST")
    override fun <T : Annotation> getAnnotation(annotationClass: Class<T>): Optional<T> = annotations
        .find { it.annotationClass.java == annotationClass }
        .let { Optional.ofNullable(it as T?) }

    override fun isNullable(): Boolean = kParameter.type.isMarkedNullable
}
