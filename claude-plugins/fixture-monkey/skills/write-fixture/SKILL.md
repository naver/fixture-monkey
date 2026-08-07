---
name: write-fixture
description: 'Write Java/Kotlin tests with Fixture Monkey — enumerate the cases a method can produce, pick the ones worth testing, and build each fixture pinning only the properties that force the expected outcome.'
when_to_use: 'Use when writing or adding tests for a Java or Kotlin method; when a test needs an object to test with; when replacing hand-built test objects, `new` calls, or test builders with Fixture Monkey; when a Fixture Monkey object fails to generate or its properties come back null; or when reviewing a test whose fixture sets more than the scenario needs. Example requests — "write tests for OrderService.calculate", "add test cases for this method", "why is Fixture Monkey not generating this record", "this test sets way too many fields".'
allowed-tools: WebFetch(domain:naver.github.io)
---

# Writing tests with Fixture Monkey

Fixture Monkey generates test objects with random values. Two skills matter: choosing **which cases to test**, and knowing **what not to set** in each one.

> Pin the properties that force the expected outcome. Leave everything else random.

A fixture that sets every field is a hand-rolled builder with extra ceremony — it breaks when an unrelated field is added, and it drags the test file into diffs it has nothing to do with.

## Scope: tests only

**Never modify production code.** This work adds and edits tests. Production sources are read-only — read them to understand behaviour, never to change it.

That holds even when changing them would be easier:

| Temptation | Do instead |
| :--- | :--- |
| Add `@ConstructorProperties`, a no-arg constructor, or a setter so the type generates | Choose a different introspector, or `instantiate` a specific constructor |
| Relax a validation annotation that keeps rejecting samples | Pin the property to a valid value, or narrow the `Arbitrary` |
| Widen a field's visibility to reach it | Reach it through the constructor or an existing accessor |
| "Fix" a bug the new test just exposed | Report it and leave the failing test — see step 6 |

If a test genuinely cannot be written without a production change, stop and say so, naming the change and why it is needed. Let the user decide. A production edit smuggled in with a test is the one thing a reviewer will not be looking for.

## Procedure

Steps 1–4 decide *which tests to write*. Steps 5–6 write each one.

### 1. Identify the method under test

Name the exact method. Read its body and the types it takes and returns. If the request is vague ("write tests for the order service"), narrow it to specific methods and say which ones you picked.

Everything downstream depends on this being one concrete method, not a class or a feature.

### 2. Enumerate the cases the method can produce

Work through the body and list every distinct outcome. Look for:

- **Branches** — every `if` / `else` / `when` / `switch` arm, and every guard clause.
- **Boundaries** — for each comparison, the value below, at, and above the threshold. `amount >= 100_000` yields 99,999 / 100,000 / 100,001.
- **Exceptions** — every `throw`, and every call that can throw.
- **Empty and absent** — empty collection, `null`, `Optional.empty()`, zero, when the type permits them.
- **Enum and subtype fan-out** — each constant or implementation that reaches a different path.
- **Interactions** — combinations where two conditions are not independent, e.g. a member discount *and* a coupon that cannot stack.

List them plainly before writing code. This list is the deliverable of steps 2–3, and it is worth showing to the user.

### 3. Select the cases worth testing

Do not test everything you listed. Keep a case if it earns its place:

| Keep | Drop |
| :--- | :--- |
| A distinct branch or outcome | A case that reaches the same code path as one already kept |
| A boundary value, on both sides | Repeats of the same value class (three "large amounts") |
| An error path with distinct handling | A case only the framework can trigger |
| A regression the bug report describes | Combinations that no caller can construct |

Aim for the smallest set that covers every outcome once, plus the boundaries. Say which cases you dropped and why — that reasoning is what lets the user disagree.

### 4. Ask before going exhaustive

The step-3 list is the default: representative coverage. Exhaustive coverage — every combination, every boundary, every enum constant — costs real test-suite time and maintenance, so it is the user's call, not yours.

If the user has already asked for thorough or exhaustive tests, skip the question and cover the full step-2 list. Otherwise, present the selected cases, note roughly how many more a full sweep would add, and ask whether they want it. Then proceed with the answer — do not block on it if the user is not present; write the representative set and note that the exhaustive set is available.

When exhaustive coverage is wanted, prefer `@ParameterizedTest` over copy-pasted methods, so the case list stays readable.

### 5. Identify the properties that force the outcome

For each selected case, pin a property **only** if one of these holds:

| Pin when | Example |
| :--- | :--- |
| **Drives the expected value** — the assertion's number cannot be derived without it | discount test: `price` and `quantity` — pinning both makes the expected discount an exact, obvious number |
| **Selects the branch** — its value decides which path runs | the underage path needs `age` below the threshold |
| **Matched by a stub** | `given(repository.findById(id))` needs the same `id` |
| **Required for validity** — otherwise the object cannot be built, or the code throws before reaching the case | a non-null foreign key, an enum discriminant selecting a subtype |
| **Collides** — a random value would violate a real constraint | a uniquely-indexed column, a fixed-width parsed code |

Everything else stays random. Do not pin because a property is conceptually important, because production requires it, or because a random value looks odd in a debugger — none of that changes the outcome.

The test for a pin: *if this property were random, could the assertion still be written as an exact expected value?* If yes, leave it random.

Over-pinning is the common failure and no test run catches it — it surfaces later as a diff in an unrelated pull request. When unsure, leave it random; step 6's verification will tell you if you were wrong.

When two properties must agree with each other, do not pin both to hand-computed constants — pin the minimum set and derive the rest with `thenApply`.

### 6. Build the fixture with `set`, then verify

Pin with the narrowest API that expresses the constraint. Take the first row that fits:

| The case needs | Use |
| :--- | :--- |
| One exact value | `set` |
| Any value in a range | `set` with an `Arbitrary` — `Arbitraries.longs().greaterThan(100)` |
| Only that it is present / absent | `setNotNull` / `setNull` |
| A specific collection size | `size`, `minSize`, `maxSize` — **before** setting elements |
| A value derived from another | `thenApply` |
| A whole object placed as-is, not decomposed | `set(selector, Values.just(value))` |
| A cross-field constraint nothing above expresses | `setPostCondition` — last resort, rejection sampling |

Select properties type-safely. String paths break silently on rename, and an unmatched path is ignored rather than reported.

```java
.set(javaGetter(Order::getStatus), OrderStatus.PAID)     // Java
```
```kotlin
.setExp(Order::status, OrderStatus.PAID)                 // Kotlin
```

| Target | Java | Kotlin |
| :--- | :--- | :--- |
| Direct property | `javaGetter(Order::getStatus)` | `Order::status` |
| Nested | `javaGetter(Order::getCustomer).into(Customer::getName)` | `Order::customer into Customer::name` |
| One element | `javaGetter(Order::getItems).index(Item.class, 0)` | `Order::items[0]` |
| All elements | `javaGetter(Order::getItems).allIndex(Item.class)` | `Order::items["*"]` |

Java imports `javaGetter` from `com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector` — the identically named one in `...api.experimental` is deprecated. Kotlin needs `KotlinPlugin` and takes the `Exp` suffix on every builder method (`setExp`, `sizeExp`, `setNullExp`, `setNotNullExp`, `setPostConditionExp`), with an `ExpGetter` variant for Java-style getter references.

**First, confirm the pins actually landed.** Before writing assertions against a newly configured `FixtureMonkey`, sample one instance of each type and check that what you pinned is what you got:

```java
Organization probe = organization(1L, "DEPT");
assertThat(probe.getId()).isEqualTo(1L);   // fails here, not 200 lines away
```

A dropped pin produces no exception and no warning — it surfaces later as an NPE inside production code, on a field the test never mentions. This throwaway check turns that into a five-minute fix.

**Then verify against randomness.** A fixture test that passes once proves nothing — the unpinned properties took one arbitrary set of values. Run it several times with fresh generation:

```bash
./gradlew test --tests '*OrderServiceTest*' --rerun-tasks
```

An intermittent failure has two causes, and they are handled differently:

- **A property that forces the outcome was left random** → pin it, back to step 5.
- **The production code has a real bug on inputs nobody considered** → **report it; do not fix it.** Leave the failing test in place and state what input triggers it. Do not pin the property to silence it, and do not reach for `fixed()` — a test adjusted to pass over a real defect is worse than no test.

## Creating the object

Pick the entry point by what the case needs:

| Call | Returns | Use when |
| :--- | :--- | :--- |
| `giveMeOne(Type.class)` | one instance | nothing needs pinning |
| `giveMe(Type.class, n)` | `List` of n | several instances, none pinned |
| `giveMeBuilder(Type.class)` | `ArbitraryBuilder` | anything needs pinning — the usual choice from step 6 |
| `giveMeBuilder(value)` | `ArbitraryBuilder` | starting from an existing object |

Kotlin uses reified generics: `giveMeOne<Order>()`, `giveMe<Order>(3)`, `giveMeBuilder<Order>()`. `giveMe<T>()` with no size returns a `Sequence`, not a `List`.

Generic types need a `TypeReference`: `giveMeOne(new TypeReference<List<Order>>() {})`.

Terminal operations on a builder: `sample()`, `sampleList(n)`, `sampleStream()`.

Share setup across cases as an `ArbitraryBuilder`, never as a sampled object — each case can then add its own pins:

```java
private ArbitraryBuilder<Order> paidOrder() {
    return fixtureMonkey.giveMeBuilder(Order.class)
        .set(javaGetter(Order::getStatus), OrderStatus.PAID);
}
```

**Extract a helper only when it carries a decision.** `paidOrder()` earns its place — a default shape several cases reuse and each can extend. A helper that merely renames a one-liner does not:

```java
// Don't — four lines to save twenty characters, and `set(quantity(), 10)`
// no longer shows which type is being selected
private static JavaGetterMethodPropertySelector<Order, Integer> quantity() {
    return javaGetter(Order::getQuantity);
}
```

Selectors stay inline. Share at the `ArbitraryBuilder` level and stop there.

### How the object actually gets constructed

Different class shapes are built in completely different ways, and this is the most common reason Fixture Monkey appears not to work. **Match the project's existing `FixtureMonkey` setup first** — if tests elsewhere in the codebase generate the same class fine, reuse their instance rather than configuring a new one.

When construction does fail, fix it at the narrowest scope that works.

**Level 1 — the introspector, global.** `FixtureMonkey.create()` defaults to `BeanArbitraryIntrospector`, which needs a no-arg constructor and setters. That is why records and immutable classes fail out of the box.

| Class shape | Introspector | Requirement |
| :--- | :--- | :--- |
| JavaBeans | `BeanArbitraryIntrospector.INSTANCE` | No-arg constructor plus setters. The default |
| Records, immutable classes | `ConstructorPropertiesArbitraryIntrospector.INSTANCE` | A record, `@ConstructorProperties`, **any constructor whose parameter names survive compilation (`-parameters`)**, or a no-arg constructor. Lombok needs `lombok.anyConstructor.addConstructorProperties=true` in `lombok.config` |
| Accessible fields, no setters | `FieldReflectionArbitraryIntrospector.INSTANCE` | No-arg constructor |
| Builder pattern | `BuilderArbitraryIntrospector.INSTANCE` | A static `builder()` method |
| Any constructor, no annotations — a library class you cannot modify | `PriorityConstructorArbitraryIntrospector.INSTANCE` | Uses whatever constructor exists |
| Genuinely mixed shapes | `new FailoverIntrospector(List.of(...))` | Tries each in order, **first success wins**. Last resort — see below |

Kotlin, where `KotlinPlugin` already defaults to `PrimaryConstructorArbitraryIntrospector`:

| Situation | Introspector |
| :--- | :--- |
| Default | `PrimaryConstructorArbitraryIntrospector` — **primary-constructor parameters only**, so a property declared in the class body or inherited is not populated |
| Kotlin classes referencing Java classes in the same graph | `KotlinAndJavaCompositeArbitraryIntrospector()` — Kotlin introspector for Kotlin types, Java one for Java types |
| Properties beyond the primary constructor | `KotlinPropertyArbitraryIntrospector` |

From plugins: `JacksonObjectArbitraryIntrospector` (`JacksonPlugin`), `MockitoIntrospector.INSTANCE` (`fixture-monkey-mockito`), `DataFakerArbitraryIntrospector` (`DataFakerPlugin`). Interfaces, abstract classes, and sealed types need `InterfacePlugin` rather than an introspector.

These are the only introspectors you select. `objectIntrospector(...)` replaces **only** the one that builds ordinary objects — collections, maps, enums, `java.time`, and primitives have their own introspectors wired in by default and are unaffected. If one of those generates incorrectly, the object introspector is not the cause. The [API reference](https://naver.github.io/fixture-monkey/docs/agent-guide/api-reference) has the complete inventory.

#### `FailoverIntrospector` is a last resort

Classify the types the test touches and pick the **one** introspector that matches — it either works or fails loudly, with no ordering to get wrong. When a few types do not fit, override those individually with `pushAssignableTypeArbitraryIntrospector` (level 2). Ten records and two JavaBeans is not a mixed codebase; it is `ConstructorProperties` globally plus two overrides.

Use a chain only when shapes are genuinely mixed and too numerous to enumerate, because it carries this trap:

**Failover stops at the first introspector that *succeeds*, and "succeeds" means "produced an object" — not "populated everything you pinned".** `ConstructorProperties` writes only the chosen constructor's parameters; a JPA `id`, `createdAt`, or audit column outside that list stays null, and a `set` targeting it is dropped with no exception and no warning. The failure then surfaces as an NPE inside production code, on a getter the test never mentioned.

It qualifies far more often than the annotation requirement suggests: a constructor also counts when its parameter names merely survive compilation, and Spring Boot's build plugins add `-parameters` by default.

```java
// Organization has a public 4-arg constructor (name, description, code, userId),
// so ConstructorProperties succeeds and failover never reaches FieldReflection.
new FailoverIntrospector(List.of(
    ConstructorPropertiesArbitraryIntrospector.INSTANCE,   // wins; id stays null
    FieldReflectionArbitraryIntrospector.INSTANCE))
builder.set(javaGetter(Organization::getId), 1L)           // silently ignored

// Fix: strictest first. FieldReflection needs a no-arg constructor, so records
// fall through correctly, and classes that have one get every field written.
new FailoverIntrospector(List.of(
    FieldReflectionArbitraryIntrospector.INSTANCE,
    ConstructorPropertiesArbitraryIntrospector.INSTANCE))
```

`setExpressionStrictMode(true)` does **not** catch this — the path resolves to a real property; the introspector simply never writes it.

**Level 2 — one type.** Do not change the global introspector for a single class: `.pushAssignableTypeArbitraryIntrospector(Order.class, BuilderArbitraryIntrospector.INSTANCE)`, or `register` for broader per-type rules.

**Level 3 — one builder, with `instantiate`.** For several constructors, a factory-method entry point, or one test needing a different path:

```java
import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.constructor;
import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.factoryMethod;

.instantiate(constructor())                                          // use a constructor
.instantiate(constructor().parameter(String.class, "name"))          // pick an overload, name the parameter
.instantiate(factoryMethod("create"))                                // static factory method
.instantiate(constructor().javaBeansProperty())                      // constructor, then setters for the rest
.instantiate(Address.class, constructor())                           // apply to a nested type
```

```kotlin
import com.navercorp.fixturemonkey.kotlin.instantiator.instantiateBy

.instantiateBy { constructor() }
.instantiateBy { factory("create") }
```

Naming a parameter — `.parameter(String.class, "name")` — is what makes `set` able to reach a constructor parameter whose name is not available at runtime.

**Diagnosing:**

| Symptom | Fix |
| :--- | :--- |
| **A `set` is silently ignored — some fields populated, others null, or an NPE in production code on a field the test pinned** | The introspector never writes that property. `ConstructorProperties` writes only constructor parameters. Do not hunt for an exception; suspect the introspector |
| All properties null or default, or fails on a record | Introspector does not fit the class shape — see level 1 |
| Works everywhere except one class | Override that type (level 2) or `instantiate` (level 3) |
| Wrong constructor picked | `constructor().parameter(...)` naming the signature |
| `set` on a constructor parameter ignored | `.parameter(Type.class, "name")`, or compile with `-parameters` |

## Stubs follow the same rule

For a service-level test, most of the code is stubbing collaborators, not building fixtures. The pin/leave-random rule applies there unchanged:

- **Argument the case does not depend on → a matcher.** `any()`, `anyString()`, `anyCollection()`. Pinning a stub argument the case does not care about couples the test to a call signature it is not testing.
- **Argument the case does depend on → derive it from the fixture, never retype the literal.**

```java
TtsDeployment deployment = deployment().set(javaGetter(TtsDeployment::getId), 100L).sample();

// Don't — 100L now lives in two places; changing one silently breaks the test
when(reader.findServiceCodes(anyCollection())).thenReturn(Map.of(100L, List.of("SVC-1")));

// Do — one source of truth
when(reader.findServiceCodes(anyCollection())).thenReturn(Map.of(deployment.getId(), List.of("SVC-1")));
```

Nothing fails at compile time when the two literals drift apart, which is what makes this silent.

Stub only the collaborators the selected case actually reaches — a stub for a call it never makes is dead setup that still has to be maintained.

## Diff stability

These are what make narrow pinning pay off:

- **Type-safe selectors, never strings.** A rename becomes a compile error at the production site; the test needs no edit.
- **Never assert a value you did not pin.** Read it back: `assertThat(saved.getName()).isEqualTo(user.getName())`, not a hardcoded `"John"`.
- **Never compare a whole object to a fully constructed expected value.** That breaks the moment anyone adds a field. Assert the properties the case is about.
- **Derive indexes and sizes, never hardcode them.** `getLast()` or `size() - 1`, not `get(35)` — a hardcoded index couples every case to a production constant, so changing it breaks several tests at once with no clue why.
- **Do not pin "just in case."** Every pin is a line a future refactor may have to touch.

## Anti-patterns

| Anti-pattern | Instead |
| :--- | :--- |
| Setting every field | Pin only what step 5 selects |
| One test per input value instead of per outcome | Group by the case list from step 3 |
| `giveMeBuilder(...).sample()` with no pins | `giveMeOne(Type.class)` |
| Mutating the object after `sample()` | Pin on the builder |
| `fixed()` to stabilise a flaky test | Pin the property that matters, or report the bug |
| `setPostCondition` where `set` would do | `set` with an `Arbitrary` |
| Hand-constructing objects alongside Fixture Monkey | Generate both |
| Editing production code to make a fixture work | Change the introspector or `instantiate`; if truly blocked, ask |
| Fixing a bug the new test exposed | Report it and leave the test failing |
| Reaching for `FailoverIntrospector` by default | Pick the one matching introspector; override odd types individually |
| A helper that just renames a selector | Inline `javaGetter(Type::prop)` — share at the `ArbitraryBuilder` level only |
| Retyping a fixture's literal into a stub or assertion | Read it off the sampled object |
| `get(35)` against a fixed-size result | `getLast()` or `size() - 1` |

## Example

Method: `DiscountPolicy.calculate(Order)` — 10% off when quantity is 10 or more.

Step 2 lists: below threshold, at threshold, above threshold, empty order, null price. Step 3 keeps the three boundary cases and the empty order; "above threshold" and "well above threshold" collapse into one. Step 5, for the at-threshold case: `price` and `quantity` force the expected number, so both are pinned — nothing else does.

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

Not pinned: id, customer, address, timestamps, status — the policy does not read them, and the expected 1,000 is derivable without them.

## Reference

This skill carries the procedure and the syntax most fixtures need. For anything beyond it, fetch the reference — the single source these rules are maintained in:

- https://naver.github.io/fixture-monkey/docs/agent-guide/api-reference
- https://naver.github.io/fixture-monkey/docs/agent-guide/writing-tests

WebFetch summarises rather than returning the page verbatim, so **ask for the specific thing you need** rather than fetching the page generically. For example: "list every `instantiate` overload with its exact signature", or "give the complete `InnerSpec` syntax for maps, verbatim".

If the fetch fails — offline, or `WebFetch` unavailable — do not guess at API surface. Proceed with the tables in this skill, and tell the user which detail you could not look up.
