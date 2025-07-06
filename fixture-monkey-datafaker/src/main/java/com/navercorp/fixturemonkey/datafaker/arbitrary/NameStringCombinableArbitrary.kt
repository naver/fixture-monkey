package com.navercorp.fixturemonkey.datafaker.arbitrary

import com.navercorp.fixturemonkey.api.arbitrary.StringCombinableArbitrary

interface NameStringCombinableArbitrary : StringCombinableArbitrary {
    fun fullName(): String
    fun firstName(): String
    fun lastName(): String
}
