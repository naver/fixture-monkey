package com.navercorp.fixturemonkey.datafaker.arbitrary

interface FinanceStringCombinableArbitrary {
    fun creditCard(): String
    fun iban(): String
    fun bic(): String
}