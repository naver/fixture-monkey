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

import com.navercorp.fixturemonkey.api.property.Property
import org.apiguardian.api.API
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type
import kotlin.reflect.KProperty

@API(since = "0.4.0", status = API.Status.EXPERIMENTAL)
data class KPropertyProperty(
    private val annotatedType: AnnotatedType,
    val kProperty: KProperty<*>
) : Property {

    override fun getType(): Type = this.annotatedType.type

    override fun getAnnotatedType(): AnnotatedType = this.annotatedType

    override fun getName(): String = this.kProperty.name

    override fun getAnnotations(): List<Annotation> = this.annotatedType.annotations.toList()

    override fun getValue(obj: Any): Any? = this.kProperty.getter.call(obj)

    override fun isNullable(): Boolean = this.kProperty.returnType.isMarkedNullable
}
