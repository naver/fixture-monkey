---
title: "Getting Started"
sidebar_position: 10
---

## Prerequisites
* JDK 8+
* JUnit 5 platform
* jqwik 1.7.0

## Installation
### Gradle 
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:0.6.12")
```


### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-starter</artifactId>
  <version>0.6.12</version>
  <scope>test</scope>
</dependency>
```


## Try Fixture Monkey
### Class
```java
@Data
@EqualsAndHashCode(of = "id", callSuper = false)
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
```

### Usage
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.plugin(new JavaxValidationPlugin())
    .build();

OrderSheet orderSheet = fixtureMonkey.giveMeOne(OrderSheet.class);
```
