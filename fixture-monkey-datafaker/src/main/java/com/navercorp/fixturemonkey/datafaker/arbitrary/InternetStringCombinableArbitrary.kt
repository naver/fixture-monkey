package com.navercorp.fixturemonkey.datafaker.arbitrary

import com.navercorp.fixturemonkey.api.arbitrary.StringCombinableArbitrary

interface InternetStringCombinableArbitrary : StringCombinableArbitrary {
    fun emailAddress(): String
    fun domainName(): String
    fun url(): String
    fun ipV4Address(): String
    fun ipV6Address(): String
    fun macAddress(): String
}
