package com.navercorp.fixturemonkey.datafaker.arbitrary

import net.datafaker.Faker
import java.util.*

object DataFakerStringArbitrary {

    @JvmStatic
    fun name(locale: Locale = Locale.ENGLISH): NameStringCombinableArbitrary =
        object : BaseStringCombinableArbitrary(), NameStringCombinableArbitrary {
            private val faker = Faker(locale)

            override fun fullName(): String = faker.name().fullName()
            override fun firstName(): String = faker.name().firstName()
            override fun lastName(): String = faker.name().lastName()

            override fun combined(): String = fullName()
        }

    @JvmStatic
    fun name(): NameStringCombinableArbitrary = name(Locale.ENGLISH)

    @JvmStatic
    fun address(
        locale: Locale = Locale.ENGLISH
    ): AddressStringCombinableArbitrary =
        object : BaseStringCombinableArbitrary(), AddressStringCombinableArbitrary {

            private val faker = Faker(locale)

            override fun fullAddress(): String = faker.address().fullAddress()
            override fun city(): String = faker.address().city()
            override fun country(): String = faker.address().country()
            override fun zipCode(): String = faker.address().zipCode()

            override fun combined(): String = fullAddress()
        }

    @JvmStatic
    fun address(): AddressStringCombinableArbitrary = address(Locale.ENGLISH)

    @JvmStatic
    fun internet(locale: Locale = Locale.ENGLISH): InternetStringCombinableArbitrary =
        object : BaseStringCombinableArbitrary(), InternetStringCombinableArbitrary {
            private val innerFaker = Faker(locale)

            override fun emailAddress(): String = innerFaker.internet().emailAddress()
            override fun domainName(): String = innerFaker.internet().domainName()
            override fun url(): String = innerFaker.internet().url()
            override fun ipV4Address(): String = innerFaker.internet().ipV4Address()
            override fun ipV6Address(): String = innerFaker.internet().ipV6Address()
            override fun macAddress(): String = innerFaker.internet().macAddress()

            override fun combined(): String = emailAddress()
        }

    @JvmStatic
    fun internet(): InternetStringCombinableArbitrary = internet(Locale.ENGLISH)

    @JvmStatic
    fun phoneNumber(locale: Locale = Locale.ENGLISH): PhoneStringCombinableArbitrary =
        object : BaseStringCombinableArbitrary(), PhoneStringCombinableArbitrary {
            private val innerFaker = Faker(locale)

            override fun cellPhone(): String = innerFaker.phoneNumber().cellPhone()
            override fun phoneNumber(): String = innerFaker.phoneNumber().phoneNumber()
            override fun combined(): String = cellPhone()
        }

    @JvmStatic
    fun phoneNumber(): PhoneStringCombinableArbitrary = phoneNumber(Locale.ENGLISH)

    @JvmStatic
    fun finance(locale: Locale = Locale.ENGLISH): FinanceStringCombinableArbitrary =
        object : BaseStringCombinableArbitrary(), FinanceStringCombinableArbitrary {
            private val innerFaker = Faker(locale)

            override fun creditCard(): String = innerFaker.finance().creditCard()
            override fun combined(): String = creditCard()
        }

    @JvmStatic
    fun finance(): FinanceStringCombinableArbitrary = finance(Locale.ENGLISH)
}
