package com.navercorp.fixturemonkey.datafaker.arbitrary

interface AddressStringCombinableArbitrary {
    fun city(): String
    fun streetName(): String
    fun streetAddress(): String
    fun zipCode(): String
    fun state(): String
    fun country(): String
    fun fullAddress(): String
}