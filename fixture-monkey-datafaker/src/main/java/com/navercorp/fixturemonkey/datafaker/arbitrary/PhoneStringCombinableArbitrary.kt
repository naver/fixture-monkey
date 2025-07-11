package com.navercorp.fixturemonkey.datafaker.arbitrary

import com.navercorp.fixturemonkey.api.arbitrary.StringCombinableArbitrary

interface PhoneStringCombinableArbitrary : StringCombinableArbitrary {
    fun cellPhone(): String
    fun phoneNumber(): String
}
