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

import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache
import com.navercorp.fixturemonkey.api.property.CompositeProperty
import com.navercorp.fixturemonkey.api.property.DefaultPropertyGenerator.CACHED_DEFAULT_FIELD_PROPERTY_GENERATOR
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyGenerator
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 * Generates the parameter properties of the given Kotlin constructor.
 * The property is generated based on the Java field of the given type.
 * That is because the generic type is erased inside the constructor.
 */
@API(since = "1.1.0", status = Status.INTERNAL)
internal class KotlinConstructorParameterPropertyGenerator(
    private val constructorResolver: (Property) -> KFunction<*>,
    private val parameterFilter: (KProperty<*>) -> Boolean = { true }
) : PropertyGenerator {
    private val parameterPropertiesCache = ConcurrentLruCache<Property, List<Property>>(2048)

    override fun generateChildProperties(property: Property): List<Property> =
        parameterPropertiesCache.computeIfAbsent(property) { _ ->
            val constructorParameterNames = constructorResolver.invoke(property).parameters.map { it.name }

            val kotlinProperties = getMemberProperties(property) {
                parameterFilter.invoke(it).and(constructorParameterNames.contains(it.name))
            }
                .filter { it.name != null }
                .associateBy { it.name!! }

            val javaProperties = JAVA_FIELD_PROPERTY_GENERATOR.generateChildProperties(property)
                .filter { it.name != null && constructorParameterNames.contains(it.name) }
                .associateBy { it.name!! }

            kotlinProperties.keys.mapNotNull { propertyName ->
                val kotlinProperty: Property? = kotlinProperties[propertyName]
                val javaProperty: Property? = javaProperties[propertyName]
                if (kotlinProperty != null && javaProperty != null) {
                    CompositeProperty(kotlinProperty, javaProperty)
                } else {
                    kotlinProperty
                }
            }
        }

    companion object {
        private val JAVA_FIELD_PROPERTY_GENERATOR = CACHED_DEFAULT_FIELD_PROPERTY_GENERATOR
    }
}
