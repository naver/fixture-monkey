package com.navercorp.fixturemonkey.datafaker.support

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.datafaker.arbitrary.DataFakerStringArbitrary

object DataFakerFieldResolver {
    private val fieldKeywordMap = mapOf(
        String::class.java to listOf("name", "address", "email", "phone", "creditCard"),
    )

    private val arbitraryGenerators = mapOf<Pair<Class<*>, String>, () -> CombinableArbitrary<*>>(
        Pair(String::class.java, "name") to { DataFakerStringArbitrary.name() },
        Pair(String::class.java, "address") to { DataFakerStringArbitrary.address() },
        Pair(String::class.java, "email") to { DataFakerStringArbitrary.internet() },
        Pair(String::class.java, "phone") to { DataFakerStringArbitrary.phoneNumber() },
        Pair(String::class.java, "creditCard") to { DataFakerStringArbitrary.finance() }
    )

    fun isFakerTargetField(type: Class<*>, fieldName: String?): Boolean {
        if (fieldName == null) return false
        val keywords = fieldKeywordMap[type] ?: return false
        return keywords.any { fieldName.contains(it, ignoreCase = true) }
    }

    fun resolveFakerArbitrary(type: Class<*>, fieldName: String): CombinableArbitrary<*> {
        val key = arbitraryGenerators.keys.find { (clazz, keyword) ->
            clazz == type && fieldName.contains(keyword, ignoreCase = true)
        }

        return key?.let { arbitraryGenerators[it]?.invoke() }
            ?: error("No faker arbitrary found for type=$type, field=$fieldName")
    }
}
