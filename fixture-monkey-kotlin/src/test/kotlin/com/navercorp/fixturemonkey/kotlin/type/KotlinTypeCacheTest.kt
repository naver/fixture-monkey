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

package com.navercorp.fixturemonkey.kotlin.type

import com.navercorp.fixturemonkey.api.type.TypeReference
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.kotlin.property.GenericSample
import com.navercorp.fixturemonkey.kotlin.property.GenericSample2
import com.navercorp.fixturemonkey.kotlin.property.SampleValue
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType
import javax.annotation.Nullable
import kotlin.reflect.full.memberProperties

class KotlinTypeCacheTest {
    @Test
    fun getAnnotatedType() {
        // given
        val typeReference = object : TypeReference<SampleValue>() {}

        val actualType = Types.getActualType(typeReference.type)
        val property = actualType.kotlin.memberProperties
            .filter { it.name == "name" }[0]

        // when
        val actual = getAnnotatedType(typeReference.annotatedType, property)

        then(actual.type).isEqualTo(String::class.java)
        then(actual.annotations).hasSize(1)
        then(actual.annotations[0].annotationClass).isEqualTo(Nullable::class)
    }

    @Test
    fun getAnnotatedTypeGenerics() {
        // given
        val typeReference = object : TypeReference<GenericSample<String>>() {}

        val actualType = Types.getActualType(typeReference.type)
        val property = actualType.kotlin.memberProperties
            .filter { it.name == "name" }[0]

        // when
        val actual = getAnnotatedType(typeReference.annotatedType, property)

        then(actual.type).isEqualTo(String::class.java)
    }

    @Test
    fun getAnnotatedTypeNestedGenerics() {
        // given
        val typeReference = object : TypeReference<GenericSample<String>>() {}

        val actualType = Types.getActualType(typeReference.type)
        val property = actualType.kotlin.memberProperties
            .filter { it.name == "sample2" }[0]

        // when
        val actual = getAnnotatedType(typeReference.annotatedType, property)

        then(actual.type).isInstanceOf(ParameterizedType::class.java)
        with(actual.type as ParameterizedType) {
            then(rawType).isEqualTo(GenericSample2::class.java)
            then(actualTypeArguments).hasSize(1)
            then(actualTypeArguments[0]).isEqualTo(String::class.java)
        }
    }

    @Test
    fun getAnnotatedTypeContainerGenerics() {
        // given
        val typeReference = object : TypeReference<GenericSample<String>>() {}

        val actualType = Types.getActualType(typeReference.type)
        val property = actualType.kotlin.memberProperties
            .filter { it.name == "list" }[0]

        // when
        val actual = getAnnotatedType(typeReference.annotatedType, property)

        then(actual.type).isInstanceOf(ParameterizedType::class.java)
        with(actual.type as ParameterizedType) {
            then(rawType).isEqualTo(List::class.java)
            then(actualTypeArguments).hasSize(1)
            then(actualTypeArguments[0]).isEqualTo(String::class.java)
        }
    }

    @Test
    fun getAnnotatedTypeRefiedGenerics() {
        // given
        val typeReference = object : TypeReference<GenericSample<String>>() {}

        val actualType = Types.getActualType(typeReference.type)
        val property = actualType.kotlin.memberProperties
            .filter { it.name == "samples" }[0]

        // when
        val actual = getAnnotatedType(typeReference.annotatedType, property)

        then(actual.type).isInstanceOf(ParameterizedType::class.java)
        with(actual.type as ParameterizedType) {
            then(rawType).isEqualTo(List::class.java)
            then(actualTypeArguments).hasSize(1)
            then(actualTypeArguments[0]).isEqualTo(SampleValue::class.java)
        }
    }
}
