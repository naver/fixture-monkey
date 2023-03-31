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

import com.navercorp.fixturemonkey.api.property.MethodProperty
import org.apiguardian.api.API
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type
import java.util.Optional
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

/**
 * An interface method property for kotlin.
 */
@API(since = "0.5.3", status = API.Status.EXPERIMENTAL)
data class InterfaceKFunctionProperty(
    private val type: KType,
    private val propertyName: String,
    private val methodName: String,
    private val annotations: List<Annotation>,
) : MethodProperty {
    override fun getType(): Type = type.javaType

    override fun getAnnotatedType(): AnnotatedType = object : AnnotatedType {
        override fun getType(): Type = this@InterfaceKFunctionProperty.getType()

        override fun <T : Annotation?> getAnnotation(annotationClass: Class<T>): T =
            this@InterfaceKFunctionProperty.getAnnotation(annotationClass).orElse(null)

        override fun getAnnotations(): Array<Annotation> = this@InterfaceKFunctionProperty.annotations.toTypedArray()

        override fun getDeclaredAnnotations(): Array<Annotation> = annotations
    }

    override fun getName(): String = propertyName

    override fun getMethodName(): String = methodName

    override fun getAnnotations(): List<Annotation> = annotations

    override fun getValue(obj: Any?): Any? {
        throw UnsupportedOperationException("Interface method should not be called.")
    }

    override fun <T : Annotation?> getAnnotation(annotationClass: Class<T>?): Optional<T> {
        return super.getAnnotation(annotationClass)
    }

    override fun isNullable(): Boolean = type.isMarkedNullable
}
