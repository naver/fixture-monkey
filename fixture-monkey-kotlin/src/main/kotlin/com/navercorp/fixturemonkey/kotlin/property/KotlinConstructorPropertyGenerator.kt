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

import com.navercorp.fixturemonkey.api.property.ConstructorProperty
import com.navercorp.fixturemonkey.api.property.ConstructorPropertyGenerator
import com.navercorp.fixturemonkey.api.property.ConstructorPropertyGeneratorContext
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.type.Types
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

@API(since = "0.6.12", status = Status.EXPERIMENTAL)
class KotlinConstructorPropertyGenerator : ConstructorPropertyGenerator {
    override fun generateParameterProperties(context: ConstructorPropertyGeneratorContext): List<Property> {
        val constructor = context.constructor
        val kotlinConstructor = constructor.kotlinFunction!!
        val parameters: List<KParameter> = kotlinConstructor.parameters

        val inputParameterTypes = context.inputParameterTypes
        val inputParameterNames = context.inputParameterNames

        return parameters.mapIndexed { index, kParameter ->
            val annotatedType = if (inputParameterTypes.size > index) {
                inputParameterTypes[index].annotatedType
            } else {
                Types.generateAnnotatedTypeWithoutAnnotation(Types.getActualType(kParameter.type.javaType))
            }

            val parameterName = inputParameterNames.getOrNull(index) ?: kParameter.name

            ConstructorProperty(
                annotatedType,
                constructor,
                parameterName,
                null,
                kParameter.type.isMarkedNullable,
            )
        }
    }

    companion object {
        val INSTANCE = KotlinConstructorPropertyGenerator()
    }
}
