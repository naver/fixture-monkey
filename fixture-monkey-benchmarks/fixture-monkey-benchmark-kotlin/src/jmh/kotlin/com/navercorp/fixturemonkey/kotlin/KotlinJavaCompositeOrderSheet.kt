package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.OrderSheet
import java.math.BigDecimal
import java.time.Instant
import javax.validation.constraints.Size

data class KotlinJavaCompositeOrderSheet(
    val id: String,
    val backUrl: String?,
    val userNo: Long?,
    @field:Size(min = 1, max = 1)
    val products: List<OrderSheet.OrderSheetProduct>,
    val merchantsByMerchantNo: Map.Entry<Long, OrderSheetMerchant>?,
    val registeredDateTime: Instant?,
    @field:Size(min = 1, max = 1)
    val bundleDeliveryFeesByDeliveryGroupKey: Map<String, OrderSheetBundleDeliveryFee>?
) {
    data class OrderSheetMerchant(
        val talkInterlockAccountId: String?,
        val logeyeRequestId: String?,
        val logeyeInflowPathName: String?,
        val logeyePayAccumulation: Boolean?
    )

    data class OrderSheetBundleDeliveryFee(
        val deliveryFee: BigDecimal?,
        val type: BundleType
    )

    enum class BundleType {
        MANUALLY, IDENTICAL_PRODUCT
    }
}
