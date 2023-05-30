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

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo
import com.navercorp.fixturemonkey.api.generator.ContainerProperty
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGeneratorContext
import com.navercorp.fixturemonkey.api.property.ElementProperty
import com.navercorp.fixturemonkey.api.type.Types
import org.apiguardian.api.API

@API(since = "0.6.0", status = API.Status.EXPERIMENTAL)
class TripleContainerPropertyGenerator : ContainerPropertyGenerator {
    override fun generate(context: ContainerPropertyGeneratorContext): ContainerProperty {
        val property = context.property
        val genericsTypes = Types.getGenericsTypes(property.annotatedType)
        if (genericsTypes.size != 3) {
            throw IllegalArgumentException(
                """
                    Triple genericsTypes must have 3 generics types for the first, second, and third values.
                    "propertyType: ${property.type}, genericsTypes: $genericsTypes
                """.trimIndent()
            )
        }

        val (firstElementType, secondElementType, thirdElementType) = genericsTypes

        return ContainerProperty(
            listOf(
                ElementProperty(
                    property,
                    firstElementType,
                    0,
                    0
                ),
                ElementProperty(
                    property,
                    secondElementType,
                    1,
                    1
                ),
                ElementProperty(
                    property,
                    thirdElementType,
                    2,
                    2
                )
            ),
            CONTAINER_INFO
        )
    }

    companion object {
        private val CONTAINER_INFO = ArbitraryContainerInfo(1, 1)
    }
}
