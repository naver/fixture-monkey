---
title: "Registering Builders by Name"
sidebar_position: 46
---

## What you will learn in this document
- How to register several builders for the same type under names
- How a builder is chosen when none is selected, by priority and weight
- How to select builders by name and which one wins where they meet

:::caution
`registerByName` and `selectName` are experimental APIs. They may change in later versions.
:::

## Registering builders by name

`register` gives one type its default builder. `registerByName` lets you register several builders for the same type,
each under its own name, for example an order that is delivered and one that is cancelled.

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

A name identifies one builder. Registering the same name twice throws an `IllegalArgumentException` from `build()`.

## When no name is selected

Builders registered by name for the same type are alternatives. When a sample selects none of them, one of them is
chosen for each sample and applies like a builder registered by `register`.

```java
List<Order> orders = fixtureMonkey.giveMe(Order.class, 10);
// each order is either DELIVERED or CANCELLED
```

- Only the alternatives with the lowest priority number compete. A builder with a higher priority number is never
  chosen while one with a lower number exists.
- Among them, each is chosen with a chance of its weight over the sum of their weights. The default weight is `1`, so
  they are equally likely unless you give weights.
- The same seed chooses the same builders.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	// priority, then weight: CANCELLED is chosen about nine times out of ten
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

A weight must be a positive number; otherwise `registerByName` throws an `IllegalArgumentException`.

## Selecting builders with `selectName`

`selectName` selects the builders to apply to every sample of a builder. The selected builder applies wherever its type
appears in the sample, and the other alternatives for the same type are left out.

```java
Order order = fixtureMonkey.giveMeExperimentalBuilder(Order.class)
	.selectName("cancelled")
	.sample();
// order.getStatus() is "CANCELLED"

Customer customer = fixtureMonkey.giveMeExperimentalBuilder(Customer.class)
	.selectName("cancelled")
	.sample();
// every Order inside the customer is CANCELLED
```

```kotlin
val order = fixtureMonkey.giveMeKotlinBuilder<Order>()
    .selectName("cancelled")
    .sample()
```

You can select several names at once, or call `selectName` again. `selectName("a", "b")` is the same as
`selectName("a").selectName("b")`, and selecting a name again makes it the latest one.

## Which builder wins

Where several builders reach the same property, they take precedence in this order:

1. What the sampled builder declares itself, such as `set`, whether it is called before or after `selectName`.
2. The selected builders, the one selected later first, whether its type sits inside or outside the others'.
3. The registered builders that are not selected, by the rules of `register`: the builder of the outer type first, then
   the lower priority number, then the one registered later.

The order applies to values, `null`, container sizes, customizers and `instantiate` alike. One exception comes from
`register` itself: a container size the sampled builder declares through a wildcard path, such as `items[*].tags`,
yields to a size a registered builder declares for the inner type, selected or not. A size declared at an exact path,
such as `items[0].tags`, wins.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.registerByName("simpleObject", SimpleObject.class, fm -> fm.giveMeBuilder(SimpleObject.class).set("str", "simpleObject"))
	.registerByName("string", String.class, fm -> fm.giveMeBuilder("string"))
	.build();

fixtureMonkey.giveMeExperimentalBuilder(SimpleObject.class)
	.selectName("string", "simpleObject")
	.sample()
	.getStr(); // "simpleObject", selected later

fixtureMonkey.giveMeExperimentalBuilder(SimpleObject.class)
	.selectName("simpleObject", "string")
	.sample()
	.getStr(); // "string", selected later even though String sits inside SimpleObject
```

Since a builder may be passed around and selected again, the name selected last tells you what applies, even when an
earlier selection is hidden in a factory method.

## Restrictions

- Selecting a name that is not registered throws an `IllegalArgumentException` when the builder samples. A
  registered name whose type does not appear in the sample has no effect on it.
- A value set to the whole object takes precedence over every selection. Once a builder sets one, for example with
  `set("$", value)`, `setNull("$")`, a sampled `fixed()` or `thenApply`, `selectName` throws an
  `IllegalStateException`, so select names before it.
- `selectName` is not supported in a builder passed to `register` or `registerByName`; `build()` throws an
  `IllegalArgumentException`.

## Priority of a group

`registerGroup` registers the builders of a group with the default priority, `Integer.MAX_VALUE`. Annotate the group's
class with `@Order` to give them another priority. It works for a class of builder methods and for an
`ArbitraryBuilderGroup` alike.

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
// the group's builder wins, because priority 1 is lower than the default priority
```
