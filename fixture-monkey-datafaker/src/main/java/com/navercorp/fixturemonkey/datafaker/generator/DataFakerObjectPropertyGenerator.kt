package com.navercorp.fixturemonkey.datafaker.generator

import com.navercorp.fixturemonkey.api.generator.ObjectProperty
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext
import com.navercorp.fixturemonkey.datafaker.property.DataFakerProperty

class DataFakerObjectPropertyGenerator : ObjectPropertyGenerator {
    override fun generate(context: ObjectPropertyGeneratorContext): ObjectProperty {
        val property = context.property

        val dataFakerProperty = DataFakerProperty(property)

        return ObjectProperty(
            dataFakerProperty,
            context.propertyNameResolver,
            context.elementIndex
        )
    }
}
