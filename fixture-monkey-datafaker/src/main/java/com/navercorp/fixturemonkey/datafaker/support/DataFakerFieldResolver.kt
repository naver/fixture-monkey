package com.navercorp.fixturemonkey.datafaker.support

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.datafaker.arbitrary.DataFakerStringArbitrary

object DataFakerFieldResolver {

    private val fieldKeywordMap = mapOf(
        String::class.java to listOf(
            "name",
            "firstName",
            "lastName",
            "fullName",
            "address",
            "city",
            "email",
            "phone",
            "phoneNumber",
            "creditCard"
        )
    )

    private val arbitraryGenerators = mapOf<Pair<Class<*>, String>, () -> CombinableArbitrary<*>>(
        Pair(String::class.java, "name") to { DataFakerStringArbitrary.name() as CombinableArbitrary<*> },
        Pair(String::class.java, "firstName") to { DataFakerStringArbitrary.name() as CombinableArbitrary<*> },
        Pair(String::class.java, "lastName") to { DataFakerStringArbitrary.name() as CombinableArbitrary<*> },
        Pair(String::class.java, "fullName") to { DataFakerStringArbitrary.name() as CombinableArbitrary<*> },
        Pair(String::class.java, "address") to { DataFakerStringArbitrary.address() as CombinableArbitrary<*> },
        Pair(String::class.java, "city") to { DataFakerStringArbitrary.address() as CombinableArbitrary<*> },
        Pair(String::class.java, "email") to { DataFakerStringArbitrary.internet() as CombinableArbitrary<*> },
        Pair(String::class.java, "phone") to { DataFakerStringArbitrary.phoneNumber() as CombinableArbitrary<*> },
        Pair(String::class.java, "phoneNumber") to { DataFakerStringArbitrary.phoneNumber() as CombinableArbitrary<*> },
        Pair(String::class.java, "creditCard") to { DataFakerStringArbitrary.finance() as CombinableArbitrary<*> }
    )

    fun isFakerTargetField(type: Class<*>, fieldName: String?): Boolean {
        if (fieldName == null) return false
        val keywords = fieldKeywordMap[type] ?: return false
        return keywords.any { fieldName.contains(it, ignoreCase = true) }
    }

    fun resolveFakerArbitrary(type: Class<*>, fieldName: String): CombinableArbitrary<*> {
        val matchedKeys = arbitraryGenerators.keys.filter { (clazz, keyword) ->
            clazz == type && fieldName.contains(keyword, ignoreCase = true)
        }

        val bestMatch = matchedKeys.maxByOrNull { (_, keyword) -> keyword.length }

        return bestMatch?.let { arbitraryGenerators[it]?.invoke() }
            ?: error("No faker arbitrary found for type=$type, field=$fieldName")
    }
}
