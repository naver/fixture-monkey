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
import com.navercorp.fixturemonkey.kotlin.type.getAnnotatedType
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.time.Instant
import javax.annotation.Nonnull
import javax.annotation.Nullable
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Positive
import kotlin.reflect.full.memberProperties

class KPropertyPropertyTest {
    @Test
    fun kPropertyPropertyValue() {
        // given
        val typeReference = object : TypeReference<PropertySample>() {}
        val properties = PropertySample::class.memberProperties.toList()

        // when
        val actual = properties
            .map { KPropertyProperty(getAnnotatedType(typeReference.annotatedType, it), it) }
            .associateBy { it.name }

        then(actual["str"]!!.type).isEqualTo(String::class.java)
        then(actual["str"]!!.isNullable).isFalse
        then(actual["str"]!!.annotations).hasSize(1)
        then(actual["str"]!!.annotations[0].annotationClass).isEqualTo(Nonnull::class)
        then(actual["nullable"]!!.type).isEqualTo(String::class.java)
        then(actual["nullable"]!!.isNullable).isTrue
        then(actual["nullable"]!!.annotations).hasSize(1)
        then(actual["nullable"]!!.annotations[0].annotationClass).isEqualTo(Nullable::class)
        then(actual["integer"]!!.type).isEqualTo(Int::class.java)
        then(actual["integer"]!!.isNullable).isFalse
        then(actual["integer"]!!.annotations).hasSize(1)
        then(actual["integer"]!!.annotations[0].annotationClass).isEqualTo(Positive::class)
        then(actual["list"]!!.type.typeName).isEqualTo("java.util.List<java.lang.String>")
        then(actual["list"]!!.isNullable).isFalse
        then(actual["list"]!!.annotations).isEmpty()
        then(actual["instant"]!!.type).isEqualTo(Instant::class.java)
        then(actual["instant"]!!.isNullable).isFalse
        then(actual["instant"]!!.annotations).hasSize(1)
        then(actual["instant"]!!.annotations[0].annotationClass).isEqualTo(PastOrPresent::class)
    }
}

data class PropertySample(
    @property:Nonnull
    val str: String,

    @field:Nullable
    val nullable: String?,

    @get:Positive
    val integer: Int,

    @param:NotEmpty
    val list: List<String>
) {
    @PastOrPresent
    val instant: Instant = Instant.now()
}
