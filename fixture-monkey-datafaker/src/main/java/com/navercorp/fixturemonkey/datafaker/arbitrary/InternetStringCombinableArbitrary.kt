package com.navercorp.fixturemonkey.datafaker.arbitrary

interface InternetStringCombinableArbitrary {
    fun emailAddress(): String
    fun domainName(): String
    fun url(): String
    fun password(): String
    fun ipV4Address(): String
    fun ipV6Address(): String
}