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

package com.navercorp.fixturemonkey.kotlin.generator

import com.navercorp.fixturemonkey.api.generator.NoArgumentInterfaceJavaMethodPropertyGenerator
import com.navercorp.fixturemonkey.api.property.InterfaceJavaMethodProperty
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyGenerator
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.kotlin.property.InterfaceKFunctionProperty
import com.navercorp.fixturemonkey.kotlin.type.getPropertyName
import com.navercorp.fixturemonkey.kotlin.type.isKotlinType
import com.navercorp.fixturemonkey.kotlin.type.kotlinMemberFunctions
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import kotlin.reflect.KParameter.Kind.INSTANCE
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType

/**
 * A property generator for generating no-argument Kotlin interface method.
 * It generates [InterfaceKFunctionProperty] if Kotlin and [InterfaceJavaMethodProperty] if Java.
 */
@API(since = "0.5.5", status = Status.MAINTAINED)
class InterfaceKFunctionPropertyGenerator : PropertyGenerator {
    override fun generateChildProperties(property: Property): List<Property> {
        val type = Types.getActualType(property.type)

        if (type.isKotlinType()) {
            val methods = type.kotlinMemberFunctions()
                .filter { it.parameters.none { parameter -> parameter.kind != INSTANCE } }
                .filter { !DATA_CLASS_METHOD_NAMES.contains(it.name) }
                .filter { it.returnType.javaType != Void.TYPE }
                .map {
                    InterfaceKFunctionProperty(
                        it.returnType,
                        it.getPropertyName(),
                        it.name,
                        it.annotations,
                    )
                }

            val properties = type.kotlin.memberProperties
                .map {
                    InterfaceKFunctionProperty(
                        type = it.returnType,
                        propertyName = it.name,
                        methodName = it.getter.javaMethod!!.name,
                        annotations = it.annotations,
                    )
                }
            return methods + properties
        }

        return JAVA_METHOD_PROPERTY_GENERATOR.generateChildProperties(property)
    }

    companion object {
        private val JAVA_METHOD_PROPERTY_GENERATOR =
            NoArgumentInterfaceJavaMethodPropertyGenerator()

        private val DATA_CLASS_METHOD_NAMES = setOf("toString", "hashCode")
    }
}
