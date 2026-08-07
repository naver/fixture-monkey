---
title: "Writing tests"
sidebar_position: 2
---

The procedure for going from a method to the tests that cover it, with Fixture Monkey supplying the data. Follow it in order.

Steps 1–4 decide *which tests to write*. Steps 5–6 write each one.

## Scope: tests only

**Production code is read-only.** This procedure adds and edits tests. Read production sources to understand behaviour, never to change them — even when changing them would be easier:

| Temptation | Do instead |
| :--- | :--- |
| Add `@ConstructorProperties`, a no-arg constructor, or a setter so the type generates | Choose a different introspector, or `instantiate` a specific constructor |
| Relax a validation annotation that keeps rejecting samples | Pin the property to a valid value, or narrow the `Arbitrary` |
| Widen a field's visibility to reach it | Reach it through the constructor or an existing accessor |
| "Fix" a bug the new test just exposed | Report it and leave the failing test — see step 6 |

If a test genuinely cannot be written without a production change, stop and say so, naming the change and why it is needed. A production edit smuggled in alongside a test is the one thing a reviewer will not be looking for.

## Step 1 — Identify the method under test

Name the exact method. Read its body, and the types it takes and returns.

If the request is broader than one method — "write tests for the order service" — narrow it to specific methods first and say which ones you picked. Everything downstream assumes a single concrete method; a fixture designed for a whole class is a fixture designed for nothing in particular.

## Step 2 — Enumerate the cases the method can produce

Work through the body and list every distinct outcome:

- **Branches** — every `if` / `else` / `when` / `switch` arm, and every guard clause.
- **Boundaries** — for each comparison, the value below, at, and above the threshold. `amount >= 100_000` yields 99,999 / 100,000 / 100,001.
- **Exceptions** — every `throw`, and every call that can throw.
- **Empty and absent** — empty collection, `null`, `Optional.empty()`, zero, wherever the type permits them.
- **Enum and subtype fan-out** — each constant or implementation that reaches a different path.
- **Interactions** — combinations where two conditions are not independent, such as a member discount and a coupon that cannot stack.

Write the list down before writing any code. Enumerating first is what separates tests that cover the method from tests that cover the happy path plus whatever came to mind.

## Step 3 — Select the cases worth testing

Do not test everything enumerated. A case earns its place if it exercises something no kept case already does:

| Keep | Drop |
| :--- | :--- |
| A distinct branch or outcome | A case reaching the same path as one already kept |
| A boundary value, on both sides | Repeats of one value class — three different "large amounts" |
| An error path with distinct handling | A case only the framework can trigger |
| A regression a bug report describes | Combinations no caller can construct |

Aim for the smallest set covering every outcome once, plus the boundaries. State which cases were dropped and why: that reasoning is what lets a reviewer disagree with the selection rather than guess at it.

## Step 4 — Decide how exhaustive to be

Step 3's list is the default: representative coverage. Exhaustive coverage — every combination, every boundary, every enum constant — costs test-suite time and maintenance, so it is a decision to be made deliberately rather than assumed.

An agent working from this guide should present the selected cases, note roughly how many more a full sweep would add, and ask. If the user has already asked for thorough or exhaustive tests, cover the full step-2 list without asking.

When going exhaustive, use `@ParameterizedTest` rather than copy-pasted methods, so the case list stays readable as it grows.

## Step 5 — Identify the properties that force the outcome

For each selected case, pin a property **only** if one of these holds:

| Pin when | Why | Example |
| :--- | :--- | :--- |
| **Drives the expected value** | The assertion's number cannot be derived without it | A discount test pins `price` and `quantity` — together they make the expected discount an exact, obvious number |
| **Selects the branch** | Its value decides which code path runs | Testing the underage path needs `age` below the threshold |
| **Matched by a stub** | A stub or fake matches on it | `given(repository.findById(id))` needs the same `id` |
| **Required for validity** | Otherwise the object cannot be built, or the code throws before reaching the case | A non-null foreign key, an enum discriminant selecting a subtype |
| **Collides** | A random value would violate a real constraint | A uniquely-indexed column, a code parsed by a fixed-width reader |

Everything else stays random. Do not pin a property because it is conceptually important, because production requires it, or because a random value looks strange in a debugger. None of those change the outcome.

The test for a pin: *if this property were random, could the assertion still be written as an exact expected value?* If yes, leave it random.

Two failure modes, and the second is the common one:

- **Under-pinned** — the test passes or fails depending on the seed. Caught by step 6.
- **Over-pinned** — the test passes reliably but spells out data it does not depend on. No test run catches it; it surfaces later as a diff in an unrelated pull request.

### Properties that must agree with each other

When two properties are constrained relative to each other — `totalAmount` must equal the sum of `items` — do not pin both to hand-computed constants. Pin the minimum set and derive the rest with `thenApply`, so the invariant survives a change to either side:

```java
fixtureMonkey.giveMeBuilder(Order.class)
    .size(javaGetter(Order::getItems), 3)
    .thenApply((order, builder) -> builder.set(
        javaGetter(Order::getTotalAmount),
        order.getItems().stream().mapToLong(Item::getAmount).sum()
    ))
    .sample();
```

## Step 6 — Pin with `set`, then verify

Reach for the first row that fits. Rows lower down cost more, either in performance or in test-to-production coupling.

| The case needs | Use | Note |
| :--- | :--- | :--- |
| One exact value | `set` | |
| Any value in a range or shape | `set` with an `Arbitrary` | `Arbitraries.integers().greaterThan(100)` — still random, still constrained |
| Only that it is present | `setNotNull` | |
| Explicitly absent | `setNull` | |
| A specific collection size | `size` / `minSize` / `maxSize` | **Set size before setting elements**, or element writes land on a collection that may be too short and are silently dropped |
| A value derived from another | `thenApply` | See above |
| A whole object placed as-is | `set(selector, Values.just(value))` | Plain `set` decomposes the value and regenerates its fields; `Values.just` blocks that |
| A cross-field constraint no other row expresses | `setPostCondition` | Last resort — rejection sampling, so it re-generates until the predicate passes and can be slow or fail to converge |

Select properties **type-safely**, never with string paths. Nothing updates a string expression when the property is renamed, and outside [expression strict mode](../fixture-monkey-options/advanced-options-for-experts) a path that no longer matches is ignored rather than reported — the property stays random and the test fails somewhere unrelated.

```java
// Java
.set(javaGetter(Order::getStatus), OrderStatus.PAID)

// Kotlin
.setExp(Order::status, OrderStatus.PAID)
```

See the [API reference](./api-reference) for the full selector syntax and the object-creation entry points.

### Verify against randomness, not once

A fixture test that passes once proves nothing: the unpinned properties took one arbitrary set of values. Run it repeatedly, and always with fresh generation rather than a cached task result.

```bash
./gradlew test --tests '*OrderServiceTest*' --rerun-tasks
```

If it fails intermittently, the cause is one of two things, handled differently:

- **A property that forces the outcome was left random.** Pin it — back to step 5.
- **The production code has a genuine bug on inputs nobody considered.** Report it and leave the failing test in place; do not fix the code as part of writing the test. This is Fixture Monkey finding a real defect, not test flakiness. Do not pin the property to make it go away.

Never tune a test into passing when the defect it found is real. That is worse than having no test.

To reproduce a specific failure while diagnosing it, fix the seed with `@Seed(1234L)` from `fixture-monkey-junit-jupiter`. The extension logs the seed of any failing test, so a CI failure can be replayed locally. Remove or keep the annotation deliberately — a permanently seeded test no longer explores anything.

## Stubs follow the same rule

For a service-level test, most of the code is stubbing collaborators rather than building fixtures. The pin/leave-random rule applies there unchanged:

- **An argument the case does not depend on gets a matcher** — `any()`, `anyString()`, `anyCollection()`. Pinning a stub argument the case does not care about couples the test to a call signature it is not testing.
- **An argument the case does depend on is derived from the fixture**, never retyped as a literal.

```java
TtsDeployment deployment = deployment().set(javaGetter(TtsDeployment::getId), 100L).sample();

// Wrong — 100L now lives in two places, and nothing fails at compile time when they drift
when(reader.findServiceCodes(anyCollection())).thenReturn(Map.of(100L, List.of("SVC-1")));

// Right — one source of truth
when(reader.findServiceCodes(anyCollection())).thenReturn(Map.of(deployment.getId(), List.of("SVC-1")));
```

This is "never assert a value you did not pin" applied to the arrange step. A literal repeated between a fixture and a stub is silent breakage waiting to happen.

Stub only the collaborators the selected case actually reaches. A stub for a call the case never makes is dead setup that still has to be maintained.

## Extract helpers for decisions, not for renames

Sharing setup as an `ArbitraryBuilder` pays off when the helper encodes a default shape that several cases reuse and each can extend. It does not pay off when the helper merely renames a one-line expression:

```java
// Worth it — carries a decision, and callers can add their own pins
private ArbitraryBuilder<Order> paidOrder() {
    return fixtureMonkey.giveMeBuilder(Order.class)
        .set(javaGetter(Order::getStatus), OrderStatus.PAID);
}

// Not worth it — four lines to save twenty characters, and the call site
// `set(quantity(), 10)` no longer shows which type is being selected
private static JavaGetterMethodPropertySelector<Order, Integer> quantity() {
    return javaGetter(Order::getQuantity);
}
```

Selectors are one-liners and stay inline.

### The layer above: the `FixtureMonkey` instance

Introspector choice, plugins, and null policy are project-wide decisions, not per-test ones, and `ArbitraryBuilder` sharing cannot deduplicate them — the duplication lives in the instance. When a second test class needs the same configuration, extract it:

```java
public final class TestFixtures {
    public static final FixtureMonkey MONKEY = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .defaultNotNull(true)
        .build();
}
```

Before adding a `FixtureMonkey.builder()` block to a test, look for an existing one in the module and reuse it. A ten-line setup copied into a third test file is the signal it should have been extracted. Unlike `ArbitraryBuilder`, a `FixtureMonkey` is immutable configuration and is safe to hold in a static field.

## When not to use Fixture Monkey

Fixture Monkey is not free: a `FixtureMonkey` instance and an introspector choice are fixed setup, and on a small type with few cases they may not be repaid. A direct `new Money(1_000L, KRW)` can be shorter and more direct.

But size today is the wrong test, because a constructor call names **every** component. Add one component and every `new` in the suite stops compiling; the edit is mechanical but it puts every one of those test files into a diff that has nothing to do with them. That is the exact coupling the rest of this guide exists to avoid, so the question is not "how many components now" but **"is this type closed?"**

Use a constructor directly only when all of these hold:

- the type is **closed by nature** — a value object whose shape is the point, like a money amount or a coordinate pair, not a domain entity that accumulates fields,
- every component matters to the assertion, so there is no incidental data to leave random,
- and it is built in few enough places that a signature change is a small edit.

Anything that looks like an entity, a DTO, or a request or response payload fails the first condition, however few fields it has today. Generate those.

Two notes on the cost side. The setup is a per-module cost, not a per-test one — once it lives in a shared `TestFixtures`, comparing a generated test against a hand-written one by line count overstates the difference. And when in doubt, generate: over-using Fixture Monkey costs a few lines, while under-using it costs an edit to every call site the day the type grows.

The judgement is per type, not per file. A test can construct a small value object directly and still generate the aggregate it goes into.

## Diff stability

The point of pinning narrowly is that the test file stops appearing in unrelated diffs. These rules preserve that; breaking one gives the coupling back.

- **Type-safe selectors over strings.** A rename becomes a compile error at the production site, and the test needs no edit.
- **Never assert on a value you did not pin.** Read it from the generated object instead: `assertThat(saved.getName()).isEqualTo(user.getName())`, not a hardcoded `"John"`.
- **Never compare whole objects to a fully constructed expected value.** `assertThat(actual).isEqualTo(expected)` breaks the moment anyone adds a field. Assert the properties the case is about.
- **Derive indexes and sizes, never hardcode them.** If a result holds 36 months, assert with `getLast()` or `size() - 1`, not `get(35)`. A hardcoded index couples every case to a constant that lives in production code, so changing it breaks several tests at once with no clue why.
- **Share setup as an `ArbitraryBuilder`, not as a sampled object.** Returning a builder lets each case add its own pins; returning a sampled instance forces every caller to accept one fixed shape.

```java
private ArbitraryBuilder<Order> paidOrder() {
    return fixtureMonkey.giveMeBuilder(Order.class)
        .set(javaGetter(Order::getStatus), OrderStatus.PAID);
}
```

- **Do not pin "just in case".** Every pin is a line that a future refactor may have to touch.

## Anti-patterns

| Anti-pattern | Why it is wrong | Instead |
| :--- | :--- | :--- |
| Setting every field of the object | Reproduces the brittleness of a hand-written builder | Pin only what step 5 selects |
| One test per input value rather than per outcome | Test count grows without coverage growing | Group by the case list from step 3 |
| `giveMeBuilder(...).sample()` with no pins | Extra ceremony for nothing | `giveMeOne(Type.class)` |
| Mutating the object after `sample()` | Bypasses generation; breaks for immutable types and records | Pin on the builder |
| `fixed()` to "make the test stable" | Freezes the whole graph, so the test stops exploring — and hides the real problem, which is a missing pin | Pin the property that matters |
| `setPostCondition` where `set` would do | Rejection sampling; slow, and can fail to converge | `set` with an `Arbitrary` |
| Constructing objects by hand alongside Fixture Monkey | The hand-built one must be updated on every field change | Generate both |
| A helper that just renames a selector | Costs more lines than it saves and hides the type at the call site | Inline `javaGetter(Type::prop)` |
| Retyping a fixture's literal into a stub or assertion | Two sources of truth, and nothing catches the drift | Read it off the sampled object |
| `get(35)` against a fixed-size result | Couples the case to a production constant | `getLast()` or `size() - 1` |
| Adding a plugin or introspector to fix one test | Usually a signal the type needs an `instantiate` or a `register` instead | See [How objects get constructed](./api-reference#how-objects-get-constructed) |
| Editing production code so a fixture generates | Ships an unreviewed source change inside a test commit | Change the introspector or `instantiate`; if truly blocked, ask |
| Fixing a bug the new test exposed | Conflates two changes and hides the defect | Report it and leave the test failing |
| Reaching for `FailoverIntrospector` by default | Ordering silently decides which pins land | Pick the one matching introspector; override odd types individually |

## Worked example

The method: `DiscountPolicy.calculate(Order)` — 10% off when quantity reaches 10.

**Step 2** enumerates: below threshold, at threshold, above threshold, empty order, null price, quantity zero.
**Step 3** keeps the three boundary cases and the empty order; "above threshold" and "far above threshold" collapse into one, since they reach the same branch.
**Step 5**, for the at-threshold case: `price` and `quantity` together force the expected number. Nothing else does.

```java
@Test
void tenItemsGetTenPercentOff() {
    Order order = fixtureMonkey.giveMeBuilder(Order.class)
        .set(javaGetter(Order::getPrice), 1_000L)
        .set(javaGetter(Order::getQuantity), 10)
        .sample();

    Discount discount = discountPolicy.calculate(order);

    assertThat(discount.getAmount()).isEqualTo(1_000L);
}
```

What is *not* pinned, and why: id, customer, address, timestamps, status. The policy reads none of them, and the expected 1,000 is derivable without them. Should someone add a `couponCode` field to `Order` next week, this test does not change.
