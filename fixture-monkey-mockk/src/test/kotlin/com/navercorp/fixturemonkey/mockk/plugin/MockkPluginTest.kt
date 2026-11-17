/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.mockk.plugin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.mockk.MockKException
import io.mockk.every
import io.mockk.isMockKMock
import io.mockk.verify
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class MockkPluginTest {
    @RepeatedTest(TEST_COUNT)
    fun interfaceAndAbstractClassAreRelaxedMocks() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(MockkPlugin())
            .build()

        // when
        val actual = sut.giveMeOne<Order>()

        // then
        then(isMockKMock(actual.gateway)).isTrue()
        then(isMockKMock(actual.notifier)).isTrue()
        then(actual.gateway.charge(100L)).isEqualTo("")
        then(actual.notifier.send("hello")).isFalse()
    }

    @Test
    fun generatedMockCanBeStubbedAndVerified() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(MockkPlugin())
            .build()
        val gateway = sut.giveMeOne<Order>().gateway

        // when
        every { gateway.charge(any()) } returns "approved"
        val actual = gateway.charge(500L)

        // then
        then(actual).isEqualTo("approved")
        verify(exactly = 1) { gateway.charge(500L) }
    }

    @Test
    fun everySampleGetsNewMock() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(MockkPlugin())
            .build()

        // when
        val actual = sut.giveMeBuilder<Order>().sampleList(3).map { it.gateway }

        // then
        then(actual).doesNotHaveDuplicates()
    }

    @RepeatedTest(TEST_COUNT)
    fun rootInterfaceIsMock() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(MockkPlugin())
            .defaultNullInjectGenerator { 0.0 }
            .build()

        // when
        val actual = sut.giveMeOne<PaymentGateway>()

        // then
        then(isMockKMock(actual)).isTrue()
    }

    @Test
    fun containerElementsAreMocks() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(MockkPlugin())
            .defaultNullInjectGenerator { 0.0 }
            .build()

        // when
        val actual = sut.giveMeBuilder<Checkout>()
            .size("gateways", 3)
            .sample()
            .gateways

        // then
        then(actual).hasSize(3)
        then(actual).allMatch { isMockKMock(it) }
    }

    @Test
    fun strictMockThrowsOnUnstubbedCall() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(MockkPlugin().relaxed(false))
            .build()
        val gateway = sut.giveMeOne<Order>().gateway

        // when, then
        thenThrownBy { gateway.charge(100L) }.isInstanceOf(MockKException::class.java)
    }

    @Test
    fun relaxUnitFunAllowsUnitCallsOnStrictMock() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(MockkPlugin().relaxed(false).relaxUnitFun(true))
            .build()
        val gateway = sut.giveMeOne<Order>().gateway

        // when
        gateway.refund("payment-1")

        // then
        verify { gateway.refund("payment-1") }
        thenThrownBy { gateway.charge(100L) }.isInstanceOf(MockKException::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun mockFinalConcreteClass() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(MockkPlugin().mock<PricingClient>())
            .build()

        // when
        val actual = sut.giveMeOne<Order>().pricing

        // then
        then(isMockKMock(actual)).isTrue()
        then(actual.price("sku-1")).isEqualTo(0L)
    }

    @RepeatedTest(TEST_COUNT)
    fun mockWithStubbingAppliesToEveryGeneratedMock() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                MockkPlugin()
                    .mock<PaymentGateway> {
                        every { name } returns "toss"
                        every { charge(any()) } answers { "approved-${firstArg<Long>()}" }
                    }
                    .mock<PricingClient> {
                        every { price(any()) } returns 1_000L
                    }
            )
            .build()

        // when
        val actual = sut.giveMeBuilder<Order>().sampleList(3)

        // then
        then(actual).allSatisfy {
            then(it.gateway.name).isEqualTo("toss")
            then(it.gateway.charge(7L)).isEqualTo("approved-7")
            then(it.pricing.price("sku-1")).isEqualTo(1_000L)
            then(isMockKMock(it.notifier)).isTrue()
        }
    }

    @Test
    fun mockWithKClass() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(MockkPlugin().mock(PricingClient::class) { every { price("sku-1") } returns 42L })
            .build()

        // when
        val actual = sut.giveMeOne<PricingClient>()

        // then
        then(actual.price("sku-1")).isEqualTo(42L)
    }

    @Test
    fun setOverridesMock() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(MockkPlugin().mock<PaymentGateway>())
            .build()
        val gateway = FixedGateway()

        // when
        val actual = sut.giveMeBuilder<Order>()
            .set("gateway", gateway)
            .sample()

        // then
        then(isMockKMock(actual.gateway)).isFalse()
        then(actual.gateway.charge(3L)).isEqualTo("fixed-3")
        then(isMockKMock(actual.notifier)).isTrue()
    }

    @Test
    fun worksWithoutKotlinPlugin() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(MockkPlugin())
            .defaultNullInjectGenerator { 0.0 }
            .build()

        // when
        val actual = sut.giveMeOne(MutableOrder::class.java)

        // then
        then(isMockKMock(actual.gateway!!)).isTrue()
        then(isMockKMock(actual.notifier!!)).isTrue()
    }

    @RepeatedTest(TEST_COUNT)
    fun registeredImplementationIsUsedBeforeMock() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(MockkPlugin())
            .plugin(InterfacePlugin().interfaceImplements(PaymentGateway::class.java, listOf(FixedGateway::class.java)))
            .build()

        // when
        val actual = sut.giveMeOne<Order>()

        // then
        then(actual.gateway).isInstanceOf(FixedGateway::class.java)
        then(isMockKMock(actual.notifier)).isTrue()
    }

    @RepeatedTest(TEST_COUNT)
    fun sealedClassUsesSubclassesBeforeMock() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(MockkPlugin())
            .build()

        // when
        val actual = sut.giveMeOne<Cart>()

        // then
        then(isMockKMock(actual.discount)).isFalse()
        then(actual.discount).isInstanceOfAny(Discount.Fixed::class.java, Discount.Rate::class.java)
        then(isMockKMock(actual.gateway)).isTrue()
    }

    companion object {
        private const val TEST_COUNT = 10
    }
}
