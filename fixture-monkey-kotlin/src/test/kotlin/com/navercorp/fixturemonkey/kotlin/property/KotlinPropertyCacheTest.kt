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

import com.navercorp.fixturemonkey.api.type.TypeReference
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType
import java.time.Instant
import javax.annotation.Nullable

class KotlinPropertyCacheTest {
    @Test
    fun getMemberProperties() {
        // given
        val typeReference = object : TypeReference<SampleValue>() {}

        // when
        val actual = getMemberProperties(typeReference.annotatedType)

        then(actual).hasSize(1)
        then(actual[0].type).isEqualTo(String::class.java)
        then(actual[0].name).isEqualTo("name")
        then(actual[0].annotations).hasSize(1)
        then(actual[0].annotations[0].annotationClass).isEqualTo(Nullable::class)
        then(actual[0].isNullable).isTrue
    }

    @Test
    fun getMemberPropertiesGenerics() {
        // given
        val typeReference = object : TypeReference<GenericSample<String>>() {}

        // when
        val actual = getMemberProperties(typeReference.annotatedType)

        then(actual).hasSize(6)
        val sorted = actual.sortedBy { it.name }

        then(sorted[0].name).isEqualTo("list")
        then(sorted[0].isNullable).isFalse
        then(sorted[0].type).isInstanceOf(ParameterizedType::class.java)
        with(sorted[0].type as ParameterizedType) {
            then(rawType).isEqualTo(List::class.java)
            then(actualTypeArguments).hasSize(1)
            then(actualTypeArguments[0]).isEqualTo(String::class.java)
        }

        then(sorted[1].name).isEqualTo("name")
        then(sorted[1].isNullable).isFalse
        then(sorted[1].type).isEqualTo(String::class.java)

        then(sorted[2].name).isEqualTo("property")
        then(sorted[2].isNullable).isTrue
        then(sorted[2].type).isEqualTo(Instant::class.java)

        then(sorted[3].name).isEqualTo("sample2")
        then(sorted[3].isNullable).isTrue
        then(sorted[3].type).isInstanceOf(ParameterizedType::class.java)
        with(sorted[3].type as ParameterizedType) {
            then(rawType).isEqualTo(GenericSample2::class.java)
            then(actualTypeArguments).hasSize(1)
            then(actualTypeArguments[0]).isEqualTo(String::class.java)
        }

        then(sorted[4].name).isEqualTo("samples")
        then(sorted[4].isNullable).isFalse
        then(sorted[4].type).isInstanceOf(ParameterizedType::class.java)
        with(sorted[4].type as ParameterizedType) {
            then(rawType).isEqualTo(List::class.java)
            then(actualTypeArguments).hasSize(1)
            then(actualTypeArguments[0]).isEqualTo(SampleValue::class.java)
        }

        then(sorted[5].name).isEqualTo("test")
        then(sorted[5].isNullable).isFalse
        then(sorted[5].type).isEqualTo(SampleValue::class.java)
    }
}

data class GenericSample<T>(
    val sample2: GenericSample2<T>?,
    val name: T,
    val test: SampleValue,
    val list: List<T>,
    val samples: List<SampleValue>
) {
    var property: Instant? = null
}

data class GenericSample2<T>(
    val name: T
)

data class SampleValue(
    @field:Nullable
    val name: String?
)
