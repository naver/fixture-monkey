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

package com.navercorp.fixturemonkey.datafaker.generator

import com.navercorp.fixturemonkey.api.generator.ObjectProperty
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext
import com.navercorp.fixturemonkey.datafaker.property.DataFakerStringProperty

class DataFakerObjectPropertyGenerator : ObjectPropertyGenerator {
    override fun generate(context: ObjectPropertyGeneratorContext): ObjectProperty {
        val property = context.property

        val dataFakerProperty = DataFakerStringProperty(property)

        return ObjectProperty(
            dataFakerProperty,
            context.propertyNameResolver,
            context.elementIndex
        )
    }
}
