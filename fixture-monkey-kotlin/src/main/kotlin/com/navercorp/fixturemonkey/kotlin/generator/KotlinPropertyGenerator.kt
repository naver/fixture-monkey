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

import com.navercorp.fixturemonkey.api.generator.PropertyGenerator
import com.navercorp.fixturemonkey.api.property.ElementProperty
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.RootProperty
import com.navercorp.fixturemonkey.api.property.TupleLikeElementsProperty
import com.navercorp.fixturemonkey.kotlin.property.getMemberProperties
import org.apiguardian.api.API
import java.lang.reflect.AnnotatedType

@API(since = "0.4.0", status = API.Status.EXPERIMENTAL)
class KotlinPropertyGenerator : PropertyGenerator {
    override fun generateRootProperty(annotatedType: AnnotatedType): Property = RootProperty(annotatedType)

    override fun generateObjectChildProperties(annotatedType: AnnotatedType): List<Property> =
        getMemberProperties(annotatedType)

    override fun generateElementProperty(
        containerProperty: Property,
        elementType: AnnotatedType,
        index: Int?,
        sequence: Int
    ): Property = ElementProperty(containerProperty, elementType, index, sequence)

    override fun generateMapEntryElementProperty(
        containerProperty: Property,
        keyProperty: Property,
        valueProperty: Property
    ): Property = MapEntryElementProperty(containerProperty, keyProperty, valueProperty)

    override fun generateTupleLikeElementsProperty(
        containerProperty: Property,
        childProperties: MutableList<Property>,
        index: Int?
    ): Property = TupleLikeElementsProperty(containerProperty, childProperties, index)
}
