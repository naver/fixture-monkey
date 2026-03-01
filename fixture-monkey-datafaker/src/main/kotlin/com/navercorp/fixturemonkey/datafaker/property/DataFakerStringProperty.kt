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

package com.navercorp.fixturemonkey.datafaker.property

import com.navercorp.fixturemonkey.api.property.Property

class DataFakerStringProperty(private val originalProperty: Property) : Property {
    private val uniqueId = System.nanoTime().toString() + Math.random().toString()

    override fun getType() = originalProperty.type
    override fun getAnnotatedType() = originalProperty.annotatedType
    override fun getName() = originalProperty.name
    override fun getAnnotations() = originalProperty.annotations
    override fun getValue(instance: Any) = originalProperty.getValue(instance)
    override fun isNullable() = originalProperty.isNullable

    override fun hashCode(): Int = uniqueId.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataFakerStringProperty) return false
        return uniqueId == other.uniqueId
    }

    override fun toString(): String = "DataFakerNonCacheable(${originalProperty.name}:$uniqueId)"
}
