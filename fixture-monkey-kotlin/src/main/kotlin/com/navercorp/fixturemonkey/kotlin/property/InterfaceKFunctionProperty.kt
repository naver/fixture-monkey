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
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.api.type.Types.generateAnnotatedTypeWithoutAnnotation
import com.navercorp.fixturemonkey.kotlin.type.toTypeReference
import com.navercorp.objectfarm.api.type.JvmType
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import java.util.Optional
import kotlin.reflect.KType

/**
 * An interface method property for kotlin.
 */
@API(since = "0.5.5", status = Status.MAINTAINED)
data class InterfaceKFunctionProperty(
    private val type: KType,
    private val propertyName: String,
    private val methodName: String,
    private val annotations: List<Annotation>,
) : MethodProperty {
    private val cachedJvmType: JvmType = Types.toJvmType(
        generateAnnotatedTypeWithoutAnnotation(type.toTypeReference().type),
        annotations,
        type.isMarkedNullable,
    )

    override fun getJvmType(): JvmType = cachedJvmType

    override fun getName(): String = propertyName

    override fun getMethodName(): String = methodName

    override fun getAnnotations(): List<Annotation> = annotations

    override fun <T : Annotation?> getAnnotation(annotationClass: Class<T>?): Optional<T> {
        return super.getAnnotation(annotationClass)
    }

    override fun isNullable(): Boolean = type.isMarkedNullable
}
