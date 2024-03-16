package com.navercorp.fixturemonkey.kotlin

import java.math.BigDecimal
import java.time.Instant
import javax.validation.constraints.Size

data class KotlinOrderSheet(
    val id: String,
    val backUrl: String?,
    val userNo: Long?,
    @field:Size(min = 1, max = 1)
    val products: List<OrderSheetProduct>,
    val merchantsByMerchantNo: Map.Entry<Long, OrderSheetMerchant>?,
    val registeredDateTime: Instant?,
    @field:Size(min = 1, max = 1)
    val bundleDeliveryFeesByDeliveryGroupKey: Map<String, OrderSheetBundleDeliveryFee>?
) {
    data class OrderSheetProduct(
        val id: String,
        val productName: String?,
        val salePrice: BigDecimal?,
        val ecMallProductId: String?,
        @field:Size(min = 1, max = 1)
        val items: List<OrderSheetItem>,
        val deliveryPolicy: OrderSheetDeliveryPolicy?,
        val merchantNo: Long,
        val merchantCategoryItemTypeName: String?,
        val wholeCategoryId: String?,
        val naverBenefit: Boolean,
        val deliveryFee: OrderSheetDeliveryFee?,
        val rewardPointPolicy: OrderSheetRewardPointPolicy?
    )

    data class OrderSheetItem(
        val id: String,
        val itemNo: String?,
        val price: BigDecimal?,
        val quantity: Long?,
        val orderAmount: BigDecimal?,
        @field:Size(min = 1, max = 1)
        val elements: List<OrderSheetElement>
    )

    data class OrderSheetElement(
        val id: String,
        val elementType: ElementType,
        @field:Size(min = 1, max = 1)
        val names: List<String>,
        @field:Size(min = 1, max = 1)
        val valueIds: List<String>,
        @field:Size(min = 1, max = 1)
        val texts: List<String>
    )

    data class OrderSheetMerchant(
        val talkInterlockAccountId: String?,
        val logeyeRequestId: String?,
        val logeyeInflowPathName: String?,
        val logeyePayAccumulation: Boolean?
    )

    data class OrderSheetDeliveryPolicy(
        val deliveryMethodType: DeliveryMethodType,
        val deliveryFeeClassType: DeliveryFeeClassType,
        val deliveryFeePayType: DeliveryFeePayType,
        val baseFee: BigDecimal?,
        val bundlePolicy: OrderSheetDeliveryBundlePolicy?,
        val freeConditionalAmount: BigDecimal?
    )

    data class OrderSheetDeliveryBundlePolicy(
        val bundleGroupId: String?
    )

    data class OrderSheetDeliveryFee(
        val deliveryFee: BigDecimal?,
        val deliveryGroupKey: String?
    )

    data class OrderSheetBundleDeliveryFee(
        val deliveryFee: BigDecimal?,
        val type: BundleType
    )

    data class OrderSheetRewardPointPolicy(
        val purchasePolicyNo: Long?,
        val purchaseAccumulateAmount: BigDecimal?
    )

    enum class DeliveryMethodType {
        DELIVERY, VISIT_RECEIPT, DIRECT_DELIVERY, QUICK_SVC, NOTHING
    }

    enum class ElementType {
        OPTION, CUSTOM
    }

    enum class DeliveryFeeClassType {
        CHARGE, FREE
    }

    enum class DeliveryFeePayType {
        PRE_PAY, AFTER_PAY, FREE
    }

    enum class BundleType {
        MANUALLY, IDENTICAL_PRODUCT
    }
}
