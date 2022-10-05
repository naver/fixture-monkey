package com.navercorp.fixturemonkey;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import lombok.Builder;

@Builder
public class BuilderOrderSheet {
	private String id;

	private String backUrl;

	private Long userNo;

	@Size(min = 1, max = 1)
	private List<OrderSheetProduct> products;
	private Map.Entry<Long, OrderSheetMerchant> merchantsByMerchantNo;

	private Instant registeredDateTime;

	@Size(min = 1, max = 1)
	private Map<String, OrderSheetBundleDeliveryFee> bundleDeliveryFeesByDeliveryGroupKey;

	@Builder
	public static class OrderSheetProduct {
		String id;

		String productName;

		BigDecimal salePrice;

		String ecMallProductId;

		@Size(min = 1, max = 1)
		List<OrderSheetItem> items;

		OrderSheetDeliveryPolicy deliveryPolicy;

		long merchantNo;

		String merchantCategoryItemTypeName;

		String wholeCategoryId;

		boolean naverBenefit;

		OrderSheetDeliveryFee deliveryFee;

		OrderSheetRewardPointPolicy rewardPointPolicy;
	}

	@Builder
	public static class OrderSheetItem {

		String id;

		String itemNo;

		BigDecimal price;

		Long quantity;

		BigDecimal orderAmount;

		@Size(min = 1, max = 1)
		List<OrderSheetElement> elements;
	}

	@Builder
	public static class OrderSheetElement {

		String id;

		ElementType elementType;

		@Size(min = 1, max = 1)
		List<String> names;

		@Size(min = 1, max = 1)
		List<String> valueIds;

		@Size(min = 1, max = 1)
		List<String> texts;
	}

	@Builder
	public static class OrderSheetMerchant {

		String talkInterlockAccountId;

		String logeyeRequestId;

		String logeyeInflowPathName;

		Boolean logeyePayAccumulation;
	}

	@Builder
	public static class OrderSheetDeliveryPolicy {

		DeliveryMethodType deliveryMethodType;

		DeliveryFeeClassType deliveryFeeClassType;

		DeliveryFeePayType deliveryFeePayType;

		BigDecimal baseFee;

		OrderSheetDeliveryBundlePolicy bundlePolicy;

		BigDecimal freeConditionalAmount;
	}

	@Builder
	public static class OrderSheetDeliveryBundlePolicy {

		String bundleGroupId;
	}

	@Builder
	public static class OrderSheetDeliveryFee {

		BigDecimal deliveryFee;

		String deliveryGroupKey;
	}

	@Builder
	public static class OrderSheetBundleDeliveryFee {

		BigDecimal deliveryFee;

		BundleType type;
	}

	@Builder
	public static class OrderSheetRewardPointPolicy {
		Long purchasePolicyNo;

		BigDecimal purchaseAccumulateAmount;
	}

	public enum DeliveryMethodType {
		DELIVERY,
		VISIT_RECEIPT,
		DIRECT_DELIVERY,
		QUICK_SVC,
		NOTHING
	}

	public enum ElementType {
		OPTION,
		CUSTOM
	}

	public enum DeliveryFeeClassType {
		CHARGE,
		FREE
	}

	public enum DeliveryFeePayType {
		PRE_PAY,
		AFTER_PAY,
		FREE
	}

	public enum BundleType {
		MANUALLY,
		IDENTICAL_PRODUCT
	}
}

