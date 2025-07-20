package com.navercorp.fixturemonkey.datafaker.arbitrary

interface PhoneStringCombinableArbitrary {
    fun phoneNumber(): String
    fun cellPhone(): String
    fun extension(): String
}