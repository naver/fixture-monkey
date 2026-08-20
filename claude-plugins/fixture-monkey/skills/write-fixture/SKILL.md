---
name: write-fixture
description: 'Write Java/Kotlin tests with Fixture Monkey — enumerate the cases a method can produce, pick the ones worth testing, and build each fixture pinning only the properties that force the expected outcome.'
when_to_use: 'Use when writing or adding tests for a Java or Kotlin method; when a test needs an object to test with; when replacing hand-built test objects, `new` calls, or test builders with Fixture Monkey; when a Fixture Monkey object fails to generate or its properties come back null; or when reviewing a test whose fixture sets more than the scenario needs. Example requests — "write tests for OrderService.calculate", "add test cases for this method", "why is Fixture Monkey not generating this record", "this test sets way too many fields".'
allowed-tools: WebFetch(domain:naver.github.io), Bash(jshell *), Bash(./gradlew *), Bash(mvn *)
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
| Remove or weaken a `@Nullable` so `defaultNotNull(true)` reaches the field | Leave the annotation alone: `setNotNull` the property, or empty the null-inject generator's nullable-annotation set — see *Nullability comes first* |
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
| That *this exact instance* survives, undecomposed | `set(selector, Values.just(value))` — only when fixing it is genuinely required |
| A cross-field constraint nothing above expresses | `setPostCondition` — last resort, rejection sampling |

A value that merely has to be *legal* is not a pin — it is a domain constraint, and it belongs somewhere the next test does not have to repeat it. See *Constrain the values you did not pin* below.

**Two ordering rules, both silent when broken.**

`size` before element writes, or the elements land on a collection that may be too short and are dropped.

And on **overlapping paths the last call wins** — including across a parent and its child, where it decides whether the parent exists at all:

```java
.setNull(javaGetter(Order::getCustomer))                                  // parent nulled
.set(javaGetter(Order::getCustomer).into(Customer::getName), "Kim")       // parent revived, name set

.set(javaGetter(Order::getCustomer).into(Customer::getName), "Kim")
.setNull(javaGetter(Order::getCustomer))                                  // parent null; the name pin is gone
```

The first order is what makes a shared base fixture work — null a subtree there, revive just the part a case needs. The second loses the pin with no exception. A helper that returns a builder gives the right order for free, since the case's own calls come after.

**`Values.just` is for when the value must not be rebuilt — nothing else.** Plain `set` decomposes the object and regenerates its properties, so a pre-built list comes back with different element values and no exception is raised. That silence is a symptom to recognise, not a reason to reach for `Values.just` by default: if the assertion does not depend on those inner values, plain `set` is correct and leaves the rest random, which is the point. Reach for `Values.just` only when the exact instance is what the case is about — the same restraint that applies to `fixed()`.

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

`javaGetter` resolves the property name by stripping a `get`/`is` prefix, or by using the method name as-is when it already matches a field. **Record accessors and other prefix-less getters work unchanged** — `javaGetter(Metric::totalChangeCount)` is as valid as `javaGetter(Order::getStatus)`, including for primitive components. A fluent accessor whose name does not match any field is the one case that fails to resolve.

Java imports `javaGetter` from `com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector` — the identically named one in `...api.experimental` is deprecated. Kotlin needs `KotlinPlugin` and takes the `Exp` suffix on every builder method (`setExp`, `sizeExp`, `setNullExp`, `setNotNullExp`, `setPostConditionExp`), with an `ExpGetter` variant for Java-style getter references.

### Nullability comes first

**Every nullable property is null 20% of the time by default** (`DEFAULT_NULL_INJECT = 0.2`). That single fact causes more confusing failures than anything else here: production code does `projects.stream().collect(toMap(Project::getId, ...))`, `getId()` comes back null, and the NPE looks like a service bug.

So before pinning anything else: **any field the code under test dereferences must be non-null.** Decide it explicitly rather than hoping.

| Scope | How |
| :--- | :--- |
| The whole instance | `.defaultNotNull(true)` on the builder |
| One type, everywhere | `.pushAssignableTypeNullInjectGenerator(Project.class, context -> 0.0d)` |
| One property, one test | `setNotNull(selector)` |

**`defaultNotNull(true)` only decides what the type left unsaid.** Declared nullability outranks it, and nothing warns you when it loses:

| The property is | Under `defaultNotNull(true)` |
| :--- | :--- |
| a Java field with no nullability annotation | never null |
| a Java field annotated `@Nullable` | **null 20% of the time, unchanged** |
| a Java field annotated `@NotNull` / `@NonNull` | never null — a not-null annotation also outranks a `@Nullable` on the same property |
| Kotlin `String?` | **null 20% of the time, unchanged** |
| Kotlin `String`, or a primitive | never null |

Highest wins: `@NotNull` / `@NonNull` → `@Nullable` or Kotlin `?` → `defaultNotNull(true)` → the 0.2 default. So the annotation that documents "this may be absent" is precisely what keeps the flakiness, and `@Nullable` here means any name in the default list — `javax.annotation`, `jakarta.annotation`, `org.springframework.lang`, `org.checkerframework.checker.nullness.qual`, `org.jspecify.annotations`, `org.eclipse.jgit.annotations`, `org.jmlspecs.annotation`.

The annotation is production code, so it stays. Fix it on the fixture side:

| Reach | How |
| :--- | :--- |
| One property, one test | `setNotNull(selector)` — outranks the annotation |
| Every `@Nullable` in the instance, still honouring `@NotNull` | a `DefaultNullInjectGenerator` whose nullable-annotation set is empty, so nothing flips a property back to nullable and `defaultNotNull` decides |

```java
// DEFAULT_NULL_INJECT, DEFAULT_NOTNULL_ANNOTATION_TYPES: com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator
.defaultNullInjectGenerator(new DefaultNullInjectGenerator(
    DEFAULT_NULL_INJECT, false, true, false,
    new HashSet<>(),                                     // no @Nullable is recognised
    new HashSet<>(DEFAULT_NOTNULL_ANNOTATION_TYPES)))    // @NotNull still forces non-null
```

`defaultNullInjectGenerator(context -> 0.0d)` is the blunt version: no property is ever null, including one a case wants absent.

**This changed inside 1.1.x.** A `TYPE_USE`-only annotation — `org.jspecify.annotations.Nullable`, `org.checkerframework.checker.nullness.qual.Nullable` — was invisible to generation before 1.1.15, and jspecify joined the default list only in 1.1.16. Null counts per 1000 samples for a `@Nullable`-annotated field under `defaultNotNull(true)`: on 1.1.11 jspecify 0 and checker 0, on 1.1.15 jspecify 0 and checker 187, from 1.1.16 both about 200. A declaration annotation such as `javax.annotation.Nullable` was already 20% on 1.1.11. So an upgrade across that boundary makes a fixture start nulling a field with no change on either side — check the version before concluding the fixture is wrong.

`defaultNotNull(true)` has a cost: **the entire object graph gets built**, including nested composites the test never touches. That is generation time, and it is a hard failure if any of those types cannot be constructed. Exclude them rather than giving up on the option:

```java
.addExceptGenerateClass(JiraConfig.class)      // or addExceptGeneratePackage(...)
```

Do not rely on repeated runs to catch a null. At 20% per field, three runs find it 49% of the time and nine runs 86% — you need about 21 to reach 99% on a *single* field. Repetition is for the randomness you accepted deliberately, not for nullability you left undecided.

**First, confirm the pins actually landed.** Before writing assertions against a newly configured `FixtureMonkey`, sample one instance of each type and check that what you pinned is what you got:

```java
Organization probe = organization(1L, "DEPT");
assertThat(probe.getId()).isEqualTo(1L);   // fails here, not 200 lines away
```

A dropped pin produces no exception and no warning — it surfaces later as an NPE inside production code, on a field the test never mentions. This throwaway check turns that into a five-minute fix.

**Then verify against randomness.** A fixture test that passes once proves nothing — the unpinned properties took one arbitrary set of values. **Run it at least ten times** with fresh generation:

```bash
for i in $(seq 10); do ./gradlew test --tests '*OrderServiceTest*' --rerun-tasks || break; done
```

Ten is a floor, not a guarantee: it catches a 20%-likely value about 89% of the time and a 5%-likely one only 40%. Use it to find *unexpected* sensitivity, and handle nullability by configuration instead, as above.

An intermittent failure has two causes, and they are handled differently:

- **A property that forces the outcome was left random** → pin it, back to step 5.
- **The production code has a real bug on inputs nobody considered** → **report it; do not fix it.** Leave the failing test in place and state what input triggers it. Do not pin the property to silence it, and do not reach for `fixed()` — a test adjusted to pass over a real defect is worse than no test.

### Constrain the values you did not pin

Leaving a property random does not mean leaving it *arbitrary*. A code that must be twelve numeric digits, an amount that cannot be negative, a date the code requires to be in the past — a fully random value violates the domain, and the test fails on data no caller could ever produce. That is not a defect the test found; it is setup that is missing.

The question is rarely *how* to constrain a value. It is **where the constraint belongs.** Take the highest row that applies — the higher it sits, the fewer places restate it:

| The constraint is | Put it | How |
| :--- | :--- | :--- |
| **Already declared in production code** as an annotation | A plugin, once | `new JakartaValidationPlugin()` (or `JavaxValidationPlugin`) — generation then honours `@Size`, `@Min`, `@Digits`, `@Pattern`, `@Email`, `@NotBlank`, `@Past` |
| True of a domain type everywhere | The instance | `register(TrackingCode.class, ...)`, `registerExactType`, `registerGroup(...)` to collect many |
| The project's default for a built-in type | A plugin, once | `new JqwikPlugin().javaTypeArbitraryGenerator(...)` overriding `strings()` / `integers()`; `.javaTimeTypeArbitraryGenerator(...)` for date and time ranges |
| Realistic-looking values — names, addresses | A plugin, once | `new DataFakerPlugin()` |
| True only for this case | The builder | `set(selector, Arbitraries.strings().numeric().ofLength(12))` |
| A relationship between two properties | The builder | `thenApply`; `setPostCondition` only when nothing else expresses it |
| That the value must not repeat | The builder | `set(selector, Values.unique(supplier))` |

**Prefer the annotation plugin to everything below it.** The constraint is then stated once, where it already lives, and every test tracks it when it changes. Restating `@Size(min = 5, max = 10)` as `ofMinLength(5).ofMaxLength(10)` in a test creates a second source of truth that nothing keeps in sync — the same mistake as retyping a fixture's literal into a stub.

`register` takes a builder for the whole type, so the argument-less `set` overload constrains it without naming a property:

```java
.register(TrackingCode.class, fixture -> fixture.giveMeBuilder(TrackingCode.class)
    .set(Arbitraries.strings().numeric().ofLength(12)))
```

Note where this lands against the placement rule under *Creating the object*: a tracking-code format the assertion never mentions is configuration the outcome does **not** depend on, so it is the kind that may be shared — unlike the introspector and the null policy.

An unsatisfiable constraint does not fail silently. Generation retries, then throws `RetryableFilterMissException` naming the property — which is also what a test gets for pinning against the domain, such as `setNull` on a `@NotBlank` field.

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

**`ArbitraryBuilder` is mutable.** `set`, `size`, and the rest add to the builder and return `this` — they do not return a new instance, and `sample()` does not reset it. So the shape above matters: it must be a **method that builds a fresh one per call**, never a shared field.

```java
// Leaks. Every test's pins accumulate on the one instance, in execution order.
private static final ArbitraryBuilder<Order> PAID_ORDER = fixtureMonkey.giveMeBuilder(Order.class)...;

// Safe. A new builder per call.
private ArbitraryBuilder<Order> paidOrder() { ... }
```

If you do hold a builder and need to branch from it, call `copy()` first.

**Extract a helper only when it carries a decision.** `paidOrder()` earns its place — a default shape several cases reuse and each can extend. A helper that merely renames a one-liner does not:

```java
// Don't — four lines to save twenty characters, and `set(quantity(), 10)`
// no longer shows which type is being selected
private static JavaGetterMethodPropertySelector<Order, Integer> quantity() {
    return javaGetter(Order::getQuantity);
}
```

The test is not "is it inline" but **does the call site still show which property is being selected.** `set(quantity(), 10)` fails it — a name was swapped for a name and nothing was gained. Factoring out a **path prefix that many pins repeat** passes it, because the leaf and the intermediate types stay visible at the call site:

```java
private static final JavaGetterMethodPropertySelector<JiraIssueResponse, JiraFields> FIELDS =
    javaGetter(JiraIssueResponse::fields);

.set(FIELDS.into(JiraFields::status).into(JiraIssueStatus::name), "Open")
```

That constant is safe to share: `into` returns a **new** selector wrapping the receiver rather than mutating it, so nothing accumulates across tests the way it would on an `ArbitraryBuilder`. Both types you need are public — `JavaGetterMethodPropertySelector<T, U>` for a root and `JoinJavaGetterPropertySelector<T, U>` for a chained one — even though `into` itself is inherited from a non-public interface.

**Above the builder there is one more layer: the `FixtureMonkey` instance itself.** Introspector choice, plugins, and null policy decide what a generated object actually looks like — and whether a pin lands at all. That makes them part of the test's context, not infrastructure to be tucked away.

**Keep the instance in the test class**, so the file answers "why does this object look like this" on its own. A `FixtureMonkey` is immutable configuration and is safe in a `static final` field, unlike `ArbitraryBuilder`:

```java
class OrderServiceTest {
    private static final FixtureMonkey FIXTURE = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .defaultNotNull(true)
        .build();
```

The rule that settles it: **the test file shows every configuration choice its outcome depends on.** A reader who must open `TestFixtures` to discover that `defaultNotNull(true)` is on cannot tell why the test passes. Extracting a shared holder is fine for configuration the outcome does not depend on — a uniform plugin set, a project-wide `javaTimeTypeArbitraryGenerator` — and wrong for the introspector and the null policy.

Four duplicated lines per test class is the price, and it is the cheaper side of the trade: that duplication is read far more often than it is edited.

### When the object will not build

`FixtureMonkey.create()` defaults to `BeanArbitraryIntrospector`, which needs a no-arg constructor and setters — which is why records and immutable classes fail out of the box. **Match the project's existing `FixtureMonkey` setup first**: if tests elsewhere generate the same class fine, reuse their instance rather than configuring a new one.

Past that, construction is a subject of its own — an introspector per class shape, three scopes to apply one at, and a trap that costs hours. It sits in a companion file rather than here, because it is only needed when something is already wrong.

**Read `${CLAUDE_SKILL_DIR}/references/object-construction.md` when any of these happens.** Do not reconstruct the fix from memory.

| Symptom | What it means |
| :--- | :--- |
| All properties null or default, or generation fails on a record | The introspector does not fit the class shape |
| **A `set` is silently ignored** — some fields populated, others null, or an NPE in production code on a field the test pinned | The introspector never writes that property. There is no exception to hunt for |
| Works everywhere except one class | Needs a per-type override, or `instantiate` on the one builder |
| Wrong constructor picked, or a `set` on a constructor parameter ignored | Needs `instantiate(constructor().parameter(...))`, or `-parameters` |
| Having to choose between introspectors, or order a `FailoverIntrospector` | That file measures it instead of guessing |

The last row is worth knowing about before you need it: the skill bundles `${CLAUDE_SKILL_DIR}/scripts/introspector-probe.jsh`, which runs every candidate introspector against the real class and reports, per type, which ones build it and which properties each leaves unwritten.

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

## When not to use Fixture Monkey

Setup is a real cost, and on a small type `new Money(1_000L, KRW)` can be shorter. But size today is the wrong test: a constructor names **every** component, so adding one puts every test that used `new` into a diff — the exact coupling this guide exists to avoid. Ask **"is this type closed?"**, not "how big is it?".

Use a constructor directly only when all of these hold:

- the type is **closed by nature** — a value object whose shape is the point, like a money amount or a coordinate pair, not an entity that accumulates fields,
- every component matters to the assertion, so there is nothing incidental to leave random,
- and it is built in few enough places that a signature change is a small edit.

Entities, DTOs, and request or response payloads fail the first condition however few fields they have today — generate those. When in doubt, generate: over-using Fixture Monkey costs a few lines, under-using it costs an edit to every call site the day the type grows.

The judgement is per type, not per file — a test can construct a small value object directly and still generate the aggregate it goes into.

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
| A helper that just renames a selector | Inline `javaGetter(Type::prop)`. A shared path *prefix* is fine — the call site still shows the leaf |
| Wrapping every pre-built value in `Values.just` | Only when that exact instance is what the case is about |
| Configuration the outcome depends on hidden in a shared holder | Declare the `FixtureMonkey` in the test class |
| Restating a production validation annotation as an `Arbitrary` in the test | Add the validation plugin and let the annotation drive generation |
| `setPostCondition` to filter a domain rule the type always obeys | `register` the type, so no test repeats it |
| Trusting `defaultNotNull(true)` to cover a `@Nullable` or Kotlin `?` property | It does not — `setNotNull` it, or empty the generator's nullable-annotation set |
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

This file carries the procedure and the syntax most fixtures need. Two kinds of reference sit outside it.

**Bundled with the skill**, read with `Read` — no network needed. `${CLAUDE_SKILL_DIR}` expands to an absolute path, so use it rather than a relative path; the working directory is the project, not the skill:

| Path | Contents |
| :--- | :--- |
| `${CLAUDE_SKILL_DIR}/references/object-construction.md` | Introspector per class shape, the three scopes, the failover trap, the diagnosing table. Read it on the symptoms listed under *When the object will not build* |
| `${CLAUDE_SKILL_DIR}/scripts/introspector-probe.jsh` | Measures which introspectors build a type and what each leaves unwritten |
| `${CLAUDE_SKILL_DIR}/scripts/print-test-classpath.gradle` | Prints a module's test runtime classpath for the probe, without editing the build |

**Online**, for anything beyond both — the single source these rules are maintained in. **Use the `.md` URLs**, which serve the source text; dropping the suffix gives the rendered page, wrapped in site navigation you do not need:

- https://naver.github.io/fixture-monkey/docs/agent-guide/api-reference.md
- https://naver.github.io/fixture-monkey/docs/agent-guide/writing-tests.md

WebFetch summarises rather than returning the page verbatim, so **ask for the specific thing you need** rather than fetching the page generically. For example: "list every `instantiate` overload with its exact signature", or "give the complete `InnerSpec` syntax for maps, verbatim".

If the fetch fails — offline, or `WebFetch` unavailable — do not guess at API surface. Proceed with the tables in this file and the bundled references above, and tell the user which detail you could not look up.
