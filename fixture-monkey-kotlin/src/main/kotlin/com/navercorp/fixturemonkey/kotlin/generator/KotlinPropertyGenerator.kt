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

import com.navercorp.fixturemonkey.api.collection.LruCache
import com.navercorp.fixturemonkey.api.generator.PropertyGenerator
import com.navercorp.fixturemonkey.api.property.CompositeProperty
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyCache
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.kotlin.property.getMemberProperties
import org.apiguardian.api.API
import java.lang.reflect.AnnotatedType

@API(since = "0.4.0", status = API.Status.EXPERIMENTAL)
class KotlinPropertyGenerator : PropertyGenerator {
    private val objectChildPropertiesCache = LruCache<Class<*>, List<Property>>(2000)

    override fun generateProperties(annotatedType: AnnotatedType): List<Property> =
        objectChildPropertiesCache.computeIfAbsent(Types.getActualType(annotatedType.type)) {
            val javaProperties = PropertyCache.getProperties(annotatedType)
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
