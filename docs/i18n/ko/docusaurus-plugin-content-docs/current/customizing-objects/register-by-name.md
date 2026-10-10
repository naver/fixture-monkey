---
title: "이름으로 빌더 등록하기"
sidebar_position: 46
---

## 이 문서에서 배울 내용
- 같은 타입에 여러 빌더를 이름으로 등록하는 방법
- 이름을 선택하지 않았을 때 우선순위와 가중치로 빌더를 고르는 방식
- 이름으로 빌더를 선택하는 방법과, 여러 빌더가 만날 때 어느 쪽이 이기는지

:::caution
`registerByName`과 `selectName`은 실험적인 API입니다. 이후 버전에서 바뀔 수 있습니다.
:::

## 이름으로 빌더 등록하기

`register`는 한 타입에 기본 빌더 하나를 정합니다. `registerByName`을 쓰면 같은 타입에 여러 빌더를 각자의 이름으로 등록할 수 있습니다. 예를 들어 배송 완료된 주문과 취소된 주문을 따로 등록할 수 있습니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", "DELIVERED"))
	.registerByName("cancelled", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", "CANCELLED"))
	.build();
```

```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .registerByName<Order>("delivered") { it.giveMeKotlinBuilder<Order>().setExp(Order::status, "DELIVERED") }
    .registerByName<Order>("cancelled") { it.giveMeKotlinBuilder<Order>().setExp(Order::status, "CANCELLED") }
    .build()
```

이름은 빌더 하나를 가리킵니다. 같은 이름을 두 번 등록하면 `build()`에서 `IllegalArgumentException`이 발생합니다.

## 이름을 선택하지 않았을 때

같은 타입에 이름으로 등록한 빌더들은 서로 대안입니다. 샘플이 그중 아무것도 선택하지 않으면 샘플마다 하나가 골라지고, `register`로 등록한 빌더처럼 적용됩니다.

```java
List<Order> orders = fixtureMonkey.giveMe(Order.class, 10);
// 각 주문은 DELIVERED 또는 CANCELLED입니다
```

- 우선순위 숫자가 가장 낮은 대안들만 경쟁합니다. 더 낮은 숫자의 대안이 있으면 높은 숫자의 빌더는 골라지지 않습니다.
- 경쟁하는 대안 중 하나가 골라질 확률은 (자신의 가중치) / (가중치의 합)입니다. 기본 가중치는 `1`이라서, 가중치를 주지 않으면 모두 같은 확률입니다.
- 같은 seed는 같은 빌더를 고릅니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	// 우선순위, 가중치 순서: CANCELLED가 열 번 중 아홉 번 정도 골라집니다
	.registerByName("delivered", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", "DELIVERED"),
		Integer.MAX_VALUE, 1)
	.registerByName("cancelled", Order.class, fm -> fm.giveMeBuilder(Order.class).set("status", "CANCELLED"),
		Integer.MAX_VALUE, 9)
	.build();
```

```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .registerByName<Order>("delivered", weight = 1) { it.giveMeKotlinBuilder<Order>().setExp(Order::status, "DELIVERED") }
    .registerByName<Order>("cancelled", weight = 9) { it.giveMeKotlinBuilder<Order>().setExp(Order::status, "CANCELLED") }
    .build()
```

가중치는 양수여야 합니다. 그렇지 않으면 `registerByName`에서 `IllegalArgumentException`이 발생합니다.

## `selectName`으로 빌더 선택하기

`selectName`은 빌더가 만드는 모든 샘플에 적용할 빌더를 고릅니다. 선택한 빌더는 샘플 안에서 그 타입이 나오는 모든 곳에 적용되고, 같은 타입의 다른 대안은 빠집니다.

```java
Order order = fixtureMonkey.giveMeExperimentalBuilder(Order.class)
	.selectName("cancelled")
	.sample();
// order.getStatus()는 "CANCELLED"입니다

Customer customer = fixtureMonkey.giveMeExperimentalBuilder(Customer.class)
	.selectName("cancelled")
	.sample();
// customer 안의 모든 Order가 CANCELLED입니다
```

```kotlin
val order = fixtureMonkey.giveMeKotlinBuilder<Order>()
    .selectName("cancelled")
    .sample()
```

이름을 한 번에 여러 개 선택하거나 `selectName`을 다시 호출할 수 있습니다. `selectName("a", "b")`는 `selectName("a").selectName("b")`와 같고, 이미 선택한 이름을 다시 선택하면 그 이름이 가장 나중에 선택한 것이 됩니다.

## 어느 빌더가 이기는지

여러 빌더가 같은 속성에 닿으면 다음 순서로 앞섭니다.

1. 샘플링하는 빌더가 직접 선언한 것(`set` 등). `selectName`보다 먼저 호출했든 나중에 호출했든 같습니다.
2. 선택한 빌더. 나중에 선택한 것이 먼저이고, 서로의 타입이 안쪽에 있든 바깥쪽에 있든 같습니다.
3. 선택하지 않은 등록 빌더. `register` 규칙을 따릅니다: 바깥 타입의 빌더가 먼저, 그다음 우선순위 숫자가 낮은 것, 그다음 나중에 등록한 것.

이 순서는 값, `null`, 컨테이너 크기, customizer, `instantiate`에 모두 같게 적용됩니다. 예외가 하나 있는데, `register`에서 온 규칙입니다. 샘플링하는 빌더가 와일드카드 경로(`items[*].tags` 등)로 선언한 컨테이너 크기는, 선택 여부와 상관없이 안쪽 타입에 등록된 빌더가 선언한 크기에 집니다. 정확한 경로(`items[0].tags` 등)로 선언한 크기는 이깁니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.registerByName("simpleObject", SimpleObject.class, fm -> fm.giveMeBuilder(SimpleObject.class).set("str", "simpleObject"))
	.registerByName("string", String.class, fm -> fm.giveMeBuilder("string"))
	.build();

fixtureMonkey.giveMeExperimentalBuilder(SimpleObject.class)
	.selectName("string", "simpleObject")
	.sample()
	.getStr(); // "simpleObject": 나중에 선택했습니다

fixtureMonkey.giveMeExperimentalBuilder(SimpleObject.class)
	.selectName("simpleObject", "string")
	.sample()
	.getStr(); // "string": String이 SimpleObject 안에 있어도 나중에 선택했습니다
```

빌더는 여기저기 전달되며 다시 선택될 수 있으므로, 앞선 선택이 팩토리 메서드 안에 숨어 있어도 마지막에 선택한 이름만 보면 무엇이 적용되는지 알 수 있습니다.

## 제약 사항

- 등록하지 않은 이름을 선택하면 샘플링할 때 `IllegalArgumentException`이 발생합니다. 등록된 이름이라도 그 타입이 샘플에 나오지 않으면 아무 효과가 없습니다.
- 객체 전체에 설정한 값은 모든 선택보다 앞섭니다. 빌더가 `set("$", value)`, `setNull("$")`, 샘플링한 `fixed()`, `thenApply` 등으로 객체 전체 값을 설정한 뒤에는 `selectName`이 `IllegalStateException`을 던집니다. 이름은 그보다 먼저 선택하세요.
- `register`나 `registerByName`에 전달하는 빌더 안에서는 `selectName`을 쓸 수 없습니다. `build()`에서 `IllegalArgumentException`이 발생합니다.

## 그룹의 우선순위

`registerGroup`은 그룹의 빌더를 기본 우선순위 `Integer.MAX_VALUE`로 등록합니다. 다른 우선순위를 주려면 그룹 클래스에 `@Order`를 붙이세요. 빌더 메서드를 가진 클래스와 `ArbitraryBuilderGroup` 구현체 모두에 적용됩니다.

```java
import com.navercorp.fixturemonkey.annotation.Order;

@Order(1)
public class CouponGroup {
	public ArbitraryBuilder<Coupon> coupon(FixtureMonkey fixtureMonkey) {
		return fixtureMonkey.giveMeBuilder(Coupon.class).set("code", "WELCOME");
	}
}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.registerGroup(CouponGroup.class)
	.register(Coupon.class, fm -> fm.giveMeBuilder(Coupon.class).set("code", "SUMMER"))
	.build();
// 우선순위 1이 기본 우선순위보다 낮으므로 그룹의 빌더가 이깁니다
```
