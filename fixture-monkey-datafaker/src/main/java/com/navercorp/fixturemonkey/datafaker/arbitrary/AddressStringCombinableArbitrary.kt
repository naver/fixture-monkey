package com.navercorp.fixturemonkey.datafaker.arbitrary

import com.navercorp.fixturemonkey.api.arbitrary.StringCombinableArbitrary

interface AddressStringCombinableArbitrary : StringCombinableArbitrary {
    fun fullAddress(): String
    fun city(): String
    fun country(): String
    fun zipCode(): String
}
