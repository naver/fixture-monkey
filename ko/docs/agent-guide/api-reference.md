# API reference

Condensed API surface for agents. For the reasoning about *which* of these to use, see [Writing tests](https://naver.github.io/fixture-monkey/ko/docs/agent-guide/writing-tests).

## Entry points

| Call | Returns | Use when |
| :--- | :--- | :--- |
| `giveMeOne(Type.class)` | one instance | nothing needs pinning |
| `giveMe(Type.class, n)` | `List` of n | several instances, none pinned |
| `giveMeBuilder(Type.class)` | `ArbitraryBuilder` | anything needs pinning |
| `giveMeBuilder(value)` | `ArbitraryBuilder` seeded from an existing object | starting from a real instance |

Generic types need a `TypeReference`:

```java
fixtureMonkey.giveMeOne(new TypeReference<List<Order>>() {});
```

Terminal operations on a builder: `sample()`, `sampleList(n)`, `sampleStream()`.

Kotlin uses reified generics throughout — `giveMeOne<Order>()`, `giveMe<Order>(3)`, `giveMeBuilder<Order>()`. Note `giveMe<T>()` with no size returns a `Sequence`, not a `List`.

## Selectors

### Java

Static import from `com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector`:

```java
import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;
```

| Target | Expression |
| :--- | :--- |
| Direct property | `javaGetter(Order::getStatus)` |
| Nested property | `javaGetter(Order::getCustomer).into(Customer::getName)` |
| Collection element | `javaGetter(Order::getItems).index(Item.class, 0)` |
| All elements | `javaGetter(Order::getItems).allIndex(Item.class)` |

`.index` and `.allIndex` take the **element type** as their first argument. Chain `.into(...)` after them to reach into the element.

There is a deprecated `javaGetter` in the `...api.experimental` package. Import from `...api.expression`.

### Kotlin

Requires `KotlinPlugin`. Import from `com.navercorp.fixturemonkey.kotlin`.

| Target | Expression |
| :--- | :--- |
| Direct property | `setExp(Order::status, ...)` |
| Java-style getter | `setExpGetter(Order::getStatus, ...)` |
| Nested property | `setExp(Order::customer into Customer::name, ...)` |
| Collection element | `setExp(Order::items[0] into Item::name, ...)` |
| All elements | `setExp(Order::items["*"] into Item::name, ...)` |

The index operator is overloaded: an `Int` selects one element, the `String` `"*"` selects all.

Every builder method has an `Exp` form for `KProperty` references and an `ExpGetter` form for `KFunction` getter references: `setExp` / `setExpGetter`, `setNullExp`, `setNotNullExp`, `sizeExp`, `minSizeExp`, `maxSizeExp`, `setPostConditionExp`.

### String expressions

`"status"`, `"customer.name"`, `"items[0].name"`, `"items[*].name"`, `"$"` for the root. Every builder method accepts them.

Avoid them in agent-written code. They break silently on rename, and an unmatched path is ignored rather than reported unless `setExpressionStrictMode(true)` is set on the `FixtureMonkey` instance.

## Builder methods

| Method | Effect |
| :--- | :--- |
| `set(selector, value)` | Pin to a value. An `Arbitrary` pins to a constrained range. A third `int` argument limits how many matches are affected |
| `set(value)` | Pin the root object |
| `setLazy(selector, supplier)` | Pin to a value resolved at sample time — a fresh value per `sample()` |
| `setNull(selector)` / `setNotNull(selector)` | Force absence / presence |
| `size(selector, n)` / `size(selector, min, max)` | Fix container size. **Call before setting elements** |
| `minSize(selector, n)` / `maxSize(selector, n)` | Bound container size on one side |
| `setPostCondition(predicate)` | Reject samples failing the predicate. Rejection sampling — prefer `set` |
| `thenApply(biConsumer)` | Sample, then pin further properties from the sampled value. For derived and cross-field values |
| `acceptIf(predicate, consumer)` | Apply customizations only when the sampled value matches |
| `fixed()` | Freeze the whole graph to one value across samples. Rarely correct in a test |
| `setInner(innerSpec)` | Customize maps and other structures path expressions cannot reach. See [InnerSpec](https://naver.github.io/fixture-monkey/ko/docs/customizing-objects/innerspec) |
| `instantiate(instantiator)` | Choose a constructor or factory method. See [Instantiate methods](https://naver.github.io/fixture-monkey/ko/docs/generating-objects/instantiate-methods) |
| `validOnly(boolean)` | When false, allow samples that violate Bean Validation constraints |
| `copy()` | Branch a builder without mutating the original |

### Values helpers

From `com.navercorp.fixturemonkey.customizer.Values`:

- `Values.just(value)` — set the value as-is. Without it, `set` decomposes the object and regenerates its properties, which matters for immutable and inlined types.
- `Values.unique(supplier)` — draw a distinct value per generation, for uniquely-constrained columns.

## How objects get constructed

This is the most common source of "Fixture Monkey does not work on my class". Different class shapes are constructed in completely different ways, and the mechanism is chosen at three levels of scope. Work from the narrowest scope that solves the problem.

### Level 1 — the introspector (global default)

The introspector decides how *every* class is built. `FixtureMonkey.create()` uses `BeanArbitraryIntrospector`, which needs a no-arg constructor and setters — that is why records and immutable classes fail out of the box.

**Java**

| Class shape | Introspector | Requirement |
| :--- | :--- | :--- |
| JavaBeans | `BeanArbitraryIntrospector.INSTANCE` | No-arg constructor plus setters. This is the default |
| Records; immutable classes with a constructor | `ConstructorPropertiesArbitraryIntrospector.INSTANCE` | A record, a constructor annotated `@ConstructorProperties`, **any constructor whose parameter names survive compilation (`-parameters`)**, or a no-arg constructor. For Lombok, set `lombok.anyConstructor.addConstructorProperties=true` in `lombok.config` |
| Accessible fields, no setters | `FieldReflectionArbitraryIntrospector.INSTANCE` | No-arg constructor |
| Builder pattern | `BuilderArbitraryIntrospector.INSTANCE` | A static `builder()` method |
| Any constructor, no annotations — typically a library class you cannot modify | `PriorityConstructorArbitraryIntrospector.INSTANCE` | Uses whatever constructor is available |
| Genuinely mixed shapes | `new FailoverIntrospector(List.of(a, b, c))` | Tries each in order, first success wins. A last resort — see below |

**Kotlin** — `KotlinPlugin` sets `PrimaryConstructorArbitraryIntrospector` as the default, so most projects need nothing further.

| Situation | Introspector |
| :--- | :--- |
| Kotlin classes, default | `PrimaryConstructorArbitraryIntrospector` — populates primary-constructor parameters only |
| Kotlin classes that reference Java classes in the same graph | `KotlinAndJavaCompositeArbitraryIntrospector()` — applies a Kotlin introspector to Kotlin types and a Java one to Java types. Both are configurable; it defaults to `PrimaryConstructor` and `Bean` |
| Kotlin properties beyond the primary constructor (fields, getters, inherited members) | `KotlinPropertyArbitraryIntrospector` |

Note the trade-off in `PrimaryConstructorArbitraryIntrospector`: it sees only primary-constructor parameters. A property declared in the class body or inherited from a parent is not populated — the same shape of problem as the failover trap below.

**From plugins**

| Types | Introspector | Enabled by |
| :--- | :--- | :--- |
| Jackson-annotated types | `JacksonObjectArbitraryIntrospector` | `new JacksonPlugin()` |
| Interfaces, abstract classes, sealed types | — | `new InterfacePlugin()`; see [Interface plugin](https://naver.github.io/fixture-monkey/ko/docs/plugins/interface-plugin/features) |
| Mockito mocks | `MockitoIntrospector.INSTANCE` | `fixture-monkey-mockito` |
| Realistic names, addresses, and similar values | `DataFakerArbitraryIntrospector` | `new DataFakerPlugin()` |

Interfaces with no-argument methods fall back to `AnonymousArbitraryIntrospector`, which proxies them, when no introspector matches.

#### Complete inventory

`objectIntrospector(...)` replaces **only** the introspector that builds ordinary objects. Everything below the first group is wired in by the default generator or a plugin and keeps working unchanged — collections, maps, enums, `java.time`, and primitives are never affected by that setting, and are not something to select.

| Group | Introspectors |
| :--- | :--- |
| **Selectable — object shape** | `BeanArbitraryIntrospector` (default), `ConstructorPropertiesArbitraryIntrospector`, `FieldReflectionArbitraryIntrospector`, `BuilderArbitraryIntrospector`, `PriorityConstructorArbitraryIntrospector`, `FailoverIntrospector` |
| **Selectable — Kotlin** | `PrimaryConstructorArbitraryIntrospector` (KotlinPlugin default), `KotlinAndJavaCompositeArbitraryIntrospector`, `KotlinPropertyArbitraryIntrospector` |
| **Selectable — from plugins** | `JacksonObjectArbitraryIntrospector`, `Jackson3ObjectArbitraryIntrospector`, `DataFakerArbitraryIntrospector`, `MockitoIntrospector` |
| **Automatic — containers and built-ins** | `ArrayIntrospector`, `IterableIntrospector`, `IteratorIntrospector`, `SetIntrospector`, `QueueIntrospector`, `StreamIntrospector`, `OptionalIntrospector`, `SingleGenericCollectionIntrospector`, `MapIntrospector`, `MapEntryIntrospector`, `MapEntryElementIntrospector`, `EnumIntrospector`, `BooleanIntrospector`, `UuidIntrospector`, `JavaArbitraryIntrospector` (primitives, `String`), `JavaTimeArbitraryIntrospector`, `FunctionalInterfaceArbitraryIntrospector` |
| **Automatic — Kotlin built-ins** (KotlinPlugin) | `PairIntrospector`, `TripleIntrospector`, `KotlinDurationIntrospector`, `CompanionObjectFactoryMethodIntrospector` |
| **Automatic — Jackson containers** | `JacksonArrayArbitraryIntrospector`, `JacksonCollectionArbitraryIntrospector`, `JacksonMapArbitraryIntrospector`, `JsonNodeIntrospector`, and the Jackson 3 equivalents `Jackson3ArrayArbitraryIntrospector`, `Jackson3CollectionArbitraryIntrospector`, `Jackson3MapArbitraryIntrospector`, `Jackson3JsonNodeIntrospector` |
| **Automatic — validation plugins** | `JakartaValidationBooleanIntrospector`, `JavaxValidationBooleanIntrospector` |
| **Internal — driven by other APIs** | `ConstructorArbitraryIntrospector` and `FactoryMethodArbitraryIntrospector` (used by `instantiate`), `AnonymousArbitraryIntrospector` (interface fallback), `ConstantIntrospector`, `NullArbitraryIntrospector` |
| **Building blocks for a custom introspector** | `CompositeArbitraryIntrospector`, `MatchArbitraryIntrospector`, `TypedArbitraryIntrospector` — see [Custom introspector](https://naver.github.io/fixture-monkey/ko/docs/generating-objects/custom-introspector) |

If a collection, enum, or `java.time` value is generating incorrectly, the object introspector is not the cause. Look at the plugin set or the relevant option instead.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
```

#### Prefer one introspector over a failover chain

Classify the types under test and pick the single introspector that matches. One correct introspector either works or fails loudly; a chain has an ordering that can be wrong in a way nothing reports.

When a few types do not fit, scope those individually with `pushAssignableTypeArbitraryIntrospector` (see [Level 2](#level-2--per-type)) instead of adding a chain. Ten records and two JavaBeans is not a mixed codebase — it is `ConstructorProperties` globally plus two overrides.

Reserve `FailoverIntrospector` for shapes that are genuinely mixed and too numerous to enumerate.

#### The failover ordering trap

`FailoverIntrospector` stops at the first introspector that *succeeds*, and "succeeds" means "produced an object" — not "populated everything you pinned".

`ConstructorPropertiesArbitraryIntrospector` writes only what the chosen constructor takes as parameters. A field outside that list — a JPA `id`, `createdAt`, an audit column — is never written. It stays null, **and a `set` targeting it is dropped with no exception and no warning.**

This fires more often than the annotation requirement suggests, because a constructor also qualifies when its parameter names merely survive compilation. Spring Boot's build plugins add `-parameters` by default, so an ordinary public constructor is enough:

```java
// Organization has a public 4-arg constructor (name, description, code, userId).
// ConstructorProperties builds from it and reports success, so failover never
// reaches FieldReflection — and `id` is not a constructor parameter.
new FailoverIntrospector(List.of(
    ConstructorPropertiesArbitraryIntrospector.INSTANCE,   // wins; id stays null
    FieldReflectionArbitraryIntrospector.INSTANCE))

builder.set(javaGetter(Organization::getId), 1L)          // silently ignored
```

The failure then surfaces nowhere near its cause — as an NPE inside the production code under test, on a getter the test never mentioned. The stack trace looks like a service bug.

**Order the strictest introspector first**, so the permissive one only sees what the strict one rejected:

```java
// FieldReflection needs a no-arg constructor, so records fail here and fall
// through correctly. Classes that have one get every field written, including id.
new FailoverIntrospector(List.of(
    FieldReflectionArbitraryIntrospector.INSTANCE,
    ConstructorPropertiesArbitraryIntrospector.INSTANCE))
```

`setExpressionStrictMode(true)` does **not** catch this. The path resolves to a real property; the introspector simply never writes it.

After configuring a `FixtureMonkey`, sample one instance of each type and assert that the pins landed. A dropped pin is silent, so this throwaway check is the cheapest way to find it:

```java
Organization probe = organization(1L, "DEPT");
assertThat(probe.getId()).isEqualTo(1L);   // fails here, not 200 lines away
```

### Level 2 — per type

When one class does not fit the global introspector, do not change the global one. Override that class only:

```java
FixtureMonkey.builder()
    .pushAssignableTypeArbitraryIntrospector(Order.class, BuilderArbitraryIntrospector.INSTANCE)
    .build();
```

`register` sets generation rules for a type — including an `instantiate` — across every test using that instance. `registerGroup` does the same property by property. Reach for either only when the rule genuinely belongs everywhere; a rule that exists for one scenario belongs in that test.

### Level 3 — per builder, with `instantiate`

The narrowest scope: this builder, this test. Use it when a class has several constructors, when a factory method is the right entry point, or when one test needs a different construction path than the rest.

**Java** — static imports from `com.navercorp.fixturemonkey.api.instantiator.Instantiator`:

```java
import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.constructor;
import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.factoryMethod;
```

| Goal | Call |
| :--- | :--- |
| Use a constructor | `.instantiate(constructor())` |
| Pick one overload by signature | `.instantiate(constructor().parameter(String.class).parameter(int.class))` |
| Name a parameter so `set` can reach it | `.instantiate(constructor().parameter(String.class, "name"))` |
| Use a static factory method | `.instantiate(factoryMethod("create"))` |
| Factory method with a chosen signature | `.instantiate(factoryMethod("create").parameter(String.class))` |
| Constructor, then fill remaining state via fields | `.instantiate(constructor().field())` |
| Constructor, then fill remaining state via setters | `.instantiate(constructor().javaBeansProperty())` |
| Apply to a nested type rather than the root | `.instantiate(Address.class, constructor())` |

Parameter names matter: `.parameter(String.class, "name")` is what makes `set("name", ...)` resolve for a constructor parameter that carries no name at runtime.

**Kotlin** — `instantiateBy`, imported from `com.navercorp.fixturemonkey.kotlin.instantiator.instantiateBy` (requires `KotlinPlugin`):

```kotlin
.instantiateBy { constructor() }
.instantiateBy { constructor<Product> { property() } }
.instantiateBy { factory("create") }
```

### Diagnosing a construction failure

| Symptom | Likely cause | Fix |
| :--- | :--- | :--- |
| A `set` is ignored with no error and the value stays null | The introspector never writes that property | Do not hunt for an exception — suspect the introspector. With `ConstructorProperties`, only constructor parameters are written |
| NPE in production code on a field the test pinned | Same cause; the pin was dropped and the symptom is far from it | Sample a probe instance and assert the pin landed |
| Some fields populated, others null | A constructor-based introspector wrote only its parameter list | Use an introspector that writes fields, or override that type |
| All properties null or default | Introspector cannot write to the class | Match the introspector to the class shape (level 1) |
| Fails on a record or immutable class | Default `BeanArbitraryIntrospector` needs setters | `ConstructorPropertiesArbitraryIntrospector` |
| Works for most classes, fails for one | Global introspector is right, one class differs | Override that type (level 2) or `instantiate` (level 3) |
| Wrong constructor picked | Multiple overloads | `constructor().parameter(...)` naming the signature |
| `set` on a constructor parameter is ignored | Parameter name not available at runtime | `.parameter(Type.class, "name")`, or compile with `-parameters` |
| Lombok class not generating | No `@ConstructorProperties` | `lombok.anyConstructor.addConstructorProperties=true` |

See [Introspector](https://naver.github.io/fixture-monkey/ko/docs/generating-objects/introspector) and [Instantiate methods](https://naver.github.io/fixture-monkey/ko/docs/generating-objects/instantiate-methods) for the full treatment.

## Other setup

| Situation | Option |
| :--- | :--- |
| Kotlin | `.plugin(KotlinPlugin())` |
| Jackson-annotated types | `.plugin(new JacksonPlugin())` |
| Bean Validation annotations should be honoured | `.plugin(new JakartaValidationPlugin())` |
| Nulls are getting in the way | `.defaultNotNull(true)` or `.nullInject(0.0)` |
| Interfaces and sealed types | `.plugin(new InterfacePlugin())` |

See [Fixture Monkey options](https://naver.github.io/fixture-monkey/ko/docs/fixture-monkey-options/overview) for the full option set, and the Plugins section for each plugin — [Kotlin](https://naver.github.io/fixture-monkey/ko/docs/plugins/kotlin-plugin/features) is the one most agents need.

## Reproducibility

`fixture-monkey-junit-jupiter` provides `@Seed(1234L)`. It fixes the seed for one test method and logs the seed of any failing test so a CI failure can be replayed. Use it to diagnose an intermittent failure, not to make one go away.
