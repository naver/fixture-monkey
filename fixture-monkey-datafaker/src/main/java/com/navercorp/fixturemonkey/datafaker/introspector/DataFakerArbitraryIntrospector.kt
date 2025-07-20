package com.navercorp.fixturemonkey.datafaker.introspector

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.datafaker.support.DataFakerFieldResolver

class DataFakerArbitraryIntrospector : ArbitraryIntrospector {
    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val property = context.arbitraryProperty.objectProperty.property
        val fieldName = property.name ?: return ArbitraryIntrospectorResult.NOT_INTROSPECTED
        val fieldType = Types.getActualType(property.type) as Class<*>

        if (!DataFakerFieldResolver.isFakerTargetField(fieldType, fieldName)) {
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }

        return try {
            val combinableArbitrary = DataFakerFieldResolver.resolveFakerArbitrary(fieldType, fieldName)
            ArbitraryIntrospectorResult(combinableArbitrary)
        } catch (e: Exception) {
            ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }
    }
}
