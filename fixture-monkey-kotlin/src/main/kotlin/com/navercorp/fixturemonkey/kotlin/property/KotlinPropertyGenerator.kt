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
import com.navercorp.fixturemonkey.api.property.DefaultPropertyGenerator
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyGenerator
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import java.lang.reflect.AnnotatedType

/**
 * Generates Kotlin and Java properties.
 * If both properties exist, it generates [CompositeProperty].
 * Kotlin property would be primary, Java property would be secondary.
 */
@API(since = "0.5.3", status = Status.MAINTAINED)
class KotlinPropertyGenerator(
    private val javaDelegatePropertyGenerator: PropertyGenerator = DefaultPropertyGenerator(),
) : PropertyGenerator {
    private val objectChildPropertiesCache = ConcurrentLruCache<AnnotatedType, List<Property>>(2048)

    override fun generateChildProperties(annotatedType: AnnotatedType): List<Property> =
        objectChildPropertiesCache.computeIfAbsent(annotatedType) {
            val javaProperties = javaDelegatePropertyGenerator.generateChildProperties(annotatedType)
                .filter { it.name != null }
                .associateBy { it.name!! }
            val kotlinProperties = getMemberProperties(annotatedType)
                .filter { it.name != null }
                .associateBy { it.name!! }

            val propertyNames = mutableListOf<String>()
            propertyNames.addAll(kotlinProperties.keys)
            javaProperties.keys.forEach {
                if (!propertyNames.contains(it)) {
                    propertyNames.add(it)
                }
            }

            propertyNames.mapNotNull {
                val kotlinProperty: Property? = kotlinProperties[it]
                val javaProperty: Property? = javaProperties[it]
                if (kotlinProperty != null && javaProperty != null) {
                    CompositeProperty(kotlinProperty, javaProperty)
                } else {
                    kotlinProperty ?: javaProperty
                }
            }
        }
}
