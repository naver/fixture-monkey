package com.navercorp.fixturemonkey.datafaker.introspector

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.datafaker.arbitrary.DataFakerStringArbitrary

class DataFakerArbitraryIntrospector : ArbitraryIntrospector, Matcher {

    override fun match(property: Property): Boolean {
        val fieldName = property.name ?: return false
        val fieldType = Types.getActualType(property.type) as Class<*>? ?: return false

        if (fieldType != String::class.java) return false

        return isDataFakerTargetField(fieldName)
    }

    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val property = context.arbitraryProperty.objectProperty.property

        if (!match(property)) {
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }

        val fieldName = property.name!!

        return try {
            val combinableArbitrary = resolveDataFakerArbitrary(fieldName)
            ArbitraryIntrospectorResult(combinableArbitrary)
        } catch (e: Exception) {
            ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }
    }

    private fun isDataFakerTargetField(fieldName: String): Boolean {
        val keywords = listOf(
            "name", "firstName", "lastName", "fullName",
            "address", "city", "email",
            "phone", "phoneNumber",
            "creditCard"
        )
        return keywords.any { fieldName.contains(it, ignoreCase = true) }
    }

    private fun resolveDataFakerArbitrary(fieldName: String): CombinableArbitrary<*> = when {
        fieldName.contains("name", ignoreCase = true) ||
                fieldName.contains("firstName", ignoreCase = true) ||
                fieldName.contains("lastName", ignoreCase = true) ||
                fieldName.contains(
                    "fullName",
                    ignoreCase = true
                ) -> DataFakerStringArbitrary.name() as CombinableArbitrary<*>

        fieldName.contains("address", ignoreCase = true) ||
                fieldName.contains(
                    "city",
                    ignoreCase = true
                ) -> DataFakerStringArbitrary.address() as CombinableArbitrary<*>

        fieldName.contains("email", ignoreCase = true) -> DataFakerStringArbitrary.internet() as CombinableArbitrary<*>

        fieldName.contains(
            "phone",
            ignoreCase = true
        ) -> DataFakerStringArbitrary.phoneNumber() as CombinableArbitrary<*>

        fieldName.contains(
            "creditCard",
            ignoreCase = true
        ) -> DataFakerStringArbitrary.finance() as CombinableArbitrary<*>

        else -> error("No DataFaker arbitrary found for field: $fieldName")
    }
}
