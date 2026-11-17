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

interface PaymentGateway {
    val name: String

    fun charge(amount: Long): String

    fun refund(paymentId: String)
}

abstract class Notifier {
    abstract fun send(message: String): Boolean
}

class PricingClient(private val baseUrl: String) {
    fun price(sku: String): Long = throw IllegalStateException("no network in tests: $baseUrl/$sku")
}

data class Order(
    val id: String,
    val gateway: PaymentGateway,
    val notifier: Notifier,
    val pricing: PricingClient,
)

data class Checkout(
    val gateways: List<PaymentGateway>,
)

class MutableOrder {
    var gateway: PaymentGateway? = null
    var notifier: Notifier? = null
}

class FixedGateway : PaymentGateway {
    override val name: String = "fixed"

    override fun charge(amount: Long): String = "fixed-$amount"

    override fun refund(paymentId: String) {
    }
}

sealed class Discount {
    data class Fixed(val amount: Long) : Discount()

    data class Rate(val percent: Int) : Discount()
}

data class Cart(
    val discount: Discount,
    val gateway: PaymentGateway,
)
