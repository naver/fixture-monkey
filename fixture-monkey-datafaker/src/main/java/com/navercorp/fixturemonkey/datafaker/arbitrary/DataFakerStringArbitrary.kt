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
    fun address(locale: Locale = Locale.ENGLISH): AddressStringCombinableArbitrary =
        object : BaseStringCombinableArbitrary(), AddressStringCombinableArbitrary {
            private val faker = Faker(locale)

            override fun city(): String = faker.address().city()
            override fun streetName(): String = faker.address().streetName()
            override fun streetAddress(): String = faker.address().streetAddress()
            override fun zipCode(): String = faker.address().zipCode()
            override fun state(): String = faker.address().state()
            override fun country(): String = faker.address().country()
            override fun fullAddress(): String = faker.address().fullAddress()

            override fun combined(): String = fullAddress()
        }

    @JvmStatic
    fun address(): AddressStringCombinableArbitrary = address(Locale.ENGLISH)

    @JvmStatic
    fun internet(locale: Locale = Locale.ENGLISH): InternetStringCombinableArbitrary =
        object : BaseStringCombinableArbitrary(), InternetStringCombinableArbitrary {
            private val faker = Faker(locale)

            override fun emailAddress(): String = faker.internet().emailAddress()
            override fun domainName(): String = faker.internet().domainName()
            override fun url(): String = faker.internet().url()
            override fun password(): String = faker.internet().password()
            override fun ipV4Address(): String = faker.internet().ipV4Address()
            override fun ipV6Address(): String = faker.internet().ipV6Address()

            override fun combined(): String = emailAddress()
        }

    @JvmStatic
    fun internet(): InternetStringCombinableArbitrary = internet(Locale.ENGLISH)

    @JvmStatic
    fun phoneNumber(locale: Locale = Locale.ENGLISH): PhoneStringCombinableArbitrary =
        object : BaseStringCombinableArbitrary(), PhoneStringCombinableArbitrary {
            private val faker = Faker(locale)

            override fun phoneNumber(): String = faker.phoneNumber().phoneNumber()
            override fun cellPhone(): String = faker.phoneNumber().cellPhone()
            override fun extension(): String = faker.phoneNumber().extension()

            override fun combined(): String = phoneNumber()
        }

    @JvmStatic
    fun phoneNumber(): PhoneStringCombinableArbitrary = phoneNumber(Locale.ENGLISH)

    @JvmStatic
    fun finance(locale: Locale = Locale.ENGLISH): FinanceStringCombinableArbitrary =
        object : BaseStringCombinableArbitrary(), FinanceStringCombinableArbitrary {
            private val faker = Faker(locale)

            override fun creditCard(): String = faker.finance().creditCard()
            override fun iban(): String = faker.finance().iban()
            override fun bic(): String = faker.finance().bic()

            override fun combined(): String = creditCard()
        }

    @JvmStatic
    fun finance(): FinanceStringCombinableArbitrary = finance(Locale.ENGLISH)
}
