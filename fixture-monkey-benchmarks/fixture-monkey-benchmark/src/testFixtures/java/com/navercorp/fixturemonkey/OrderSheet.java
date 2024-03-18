package com.navercorp.fixturemonkey;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString
public class OrderSheet {
	private String id;

	private String backUrl;

	private Long userNo;

	@Size(min = 1, max = 1)
	private List<OrderSheetProduct> products;
	private Map.Entry<Long, OrderSheetMerchant> merchantsByMerchantNo;

	private Instant registeredDateTime;

	@Size(min = 1, max = 1)
	private Map<String, OrderSheetBundleDeliveryFee> bundleDeliveryFeesByDeliveryGroupKey;

	@Data
	@EqualsAndHashCode(of = "id", callSuper = false)
	@ToString
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

	@Data
	@EqualsAndHashCode(of = "id", callSuper = false)
	@ToString
	// @Builder
	public static class OrderSheetItem {

		String id;

		String itemNo;

		BigDecimal price;

		Long quantity;

		BigDecimal orderAmount;

		@Size(min = 1, max = 1)
		List<OrderSheetElement> elements;
	}

	@Data
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

	@Data
	public static class OrderSheetMerchant {
		String talkInterlockAccountId;

		String logeyeRequestId;

		String logeyeInflowPathName;

		Boolean logeyePayAccumulation;
	}

	@Data
	@ToString
	public static class OrderSheetDeliveryPolicy {
		DeliveryMethodType deliveryMethodType;

		DeliveryFeeClassType deliveryFeeClassType;

		DeliveryFeePayType deliveryFeePayType;

		BigDecimal baseFee;

		OrderSheetDeliveryBundlePolicy bundlePolicy;

		BigDecimal freeConditionalAmount;
	}

	@Data
	public static class OrderSheetDeliveryBundlePolicy {
		String bundleGroupId;
	}

	@Data
	@ToString
	public static class OrderSheetDeliveryFee {
		BigDecimal deliveryFee;

		String deliveryGroupKey;
	}

	@Data
	public static class OrderSheetBundleDeliveryFee {
		BigDecimal deliveryFee;

		BundleType type;
	}

	@Data
	@ToString
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

