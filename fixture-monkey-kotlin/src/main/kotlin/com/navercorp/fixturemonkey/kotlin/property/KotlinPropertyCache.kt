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
 */ // ktlint-disable filename

package com.navercorp.fixturemonkey.kotlin.property

import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.kotlin.type.cachedKotlin
import com.navercorp.fixturemonkey.kotlin.type.getAnnotatedType
import org.apiguardian.api.API
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

private val KPROPERTY_ANNOTATED_TYPE_MAP = ConcurrentLruCache<Class<*>, Collection<KProperty<*>>>(2048)

@API(since = "0.4.0", status = API.Status.INTERNAL)
fun getMemberProperties(
    property: Property,
    propertyFilter: (KProperty<*>) -> Boolean = { true },
): List<Property> {
    val actualType = Types.getActualType(property.type)
    return getKotlinMemberProperties(actualType)
        .filter(propertyFilter)
        .map {
            val propertyAnnotatedType = getAnnotatedType(property.annotatedType, it)
            KPropertyProperty(propertyAnnotatedType, it)
        }
}

internal fun getKotlinMemberProperties(type: Class<*>): Collection<KProperty<*>> =
    KPROPERTY_ANNOTATED_TYPE_MAP.computeIfAbsent(type) { it.cachedKotlin().memberProperties }
