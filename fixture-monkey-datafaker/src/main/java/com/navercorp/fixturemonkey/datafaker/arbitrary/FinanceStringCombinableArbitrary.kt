package com.navercorp.fixturemonkey.datafaker.arbitrary

import com.navercorp.fixturemonkey.api.arbitrary.StringCombinableArbitrary

interface FinanceStringCombinableArbitrary : StringCombinableArbitrary {
    fun creditCard(): String
}
