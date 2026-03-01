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
import org.apiguardian.api.API
import org.slf4j.LoggerFactory
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

@API(since = "0.4.0", status = API.Status.MAINTAINED)
data class KPropertyProperty(
    private val annotatedType: AnnotatedType,
    val kProperty: KProperty<*>,
) : Property {
    override fun getType(): Type = this.annotatedType.type

    override fun getAnnotatedType(): AnnotatedType = this.annotatedType

    override fun getName(): String = this.kProperty.name

    override fun getAnnotations(): List<Annotation> = this.annotatedType.annotations.toList()

    override fun getValue(instance: Any): Any? {
        val javaMethod = this.kProperty.getter.javaMethod

        if (javaMethod != null) {
            javaMethod.isAccessible = true
            return javaMethod.invoke(instance)
        }

        val javaField = this.kProperty.javaField
        if (javaField != null) {
            javaField.isAccessible = true
            return javaField.get(instance)
        }

        LOGGER.warn("Failed to get value of property {} in instance {}.", this.kProperty.name, instance)
        return null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass == other.javaClass) {
            return false
        }

        val that = other as Property
        return type == that.type
    }

    override fun hashCode(): Int = type.hashCode()

    override fun isNullable(): Boolean = this.kProperty.returnType.isMarkedNullable

    companion object {
        private val LOGGER = LoggerFactory.getLogger(KPropertyProperty::class.java)
    }
}
