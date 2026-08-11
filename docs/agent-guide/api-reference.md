# API reference

Condensed API surface for agents. For the reasoning about *which* of these to use, see [Writing tests](https://naver.github.io/fixture-monkey/docs/agent-guide/writing-tests).

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

Selectors are values, and `into` returns a **new** selector wrapping its receiver rather than mutating it. A path prefix that many pins share can therefore be held in a constant and reused without the accumulation hazard that makes a shared `ArbitraryBuilder` unsafe:

```java
private static final JavaGetterMethodPropertySelector<JiraIssueResponse, JiraFields> FIELDS =
    javaGetter(JiraIssueResponse::fields);

.set(FIELDS.into(JiraFields::status).into(JiraIssueStatus::name), "Open")
```

Both types are public — `JavaGetterMethodPropertySelector<T, U>` for a root selector, `JoinJavaGetterPropertySelector<T, U>` for the result of `into` — so either can be named in a field or a return type. `into` itself is a default method inherited from a non-public interface, which does not prevent calling it or declaring those types from another package.

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

Avoid them in agent-written code. They break silently on rename, and an unmatched path is ignored rather than reported unless `useExpressionStrictMode()` is set on the `FixtureMonkey` instance.

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
| `setInner(innerSpec)` | Customize maps and other structures path expressions cannot reach. See [InnerSpec](https://naver.github.io/fixture-monkey/docs/customizing-objects/innerspec) |
| `instantiate(instantiator)` | Choose a constructor or factory method. See [Instantiate methods](https://naver.github.io/fixture-monkey/docs/generating-objects/instantiate-methods) |
| `validOnly(boolean)` | When false, allow samples that violate Bean Validation constraints |
| `customizeProperty(selector, customizer)` | Transform the `CombinableArbitrary` behind a property, for control `set` cannot express |
| `map(mapper)` | Returns a **new** builder producing `mapper` applied to each sample |
| `zipWith(other, combinator)` | Returns a **new** builder combining two builders' samples |
| `build()` | Returns the underlying `Arbitrary` instead of sampling |
| `copy()` | Returns an independent copy — the way to branch without affecting the original |

### Ordering: the last call on an overlapping path wins

Manipulators apply in call order, and two of the resulting rules break silently.

`size` must precede element writes, or the elements land on a collection that may be too short and are dropped.

And where two calls overlap, the later one decides — including across a parent and its child, where it determines whether the parent exists at all:

```java
.setNull(javaGetter(Order::getCustomer))
.set(javaGetter(Order::getCustomer).into(Customer::getName), "Kim")   // parent revived, name set

.set(javaGetter(Order::getCustomer).into(Customer::getName), "Kim")
.setNull(javaGetter(Order::getCustomer))                             // parent null, the name pin is gone
```

The first order is what lets a shared base fixture null a subtree and have each case revive only the part it needs. The second discards the pin without an exception. Sharing setup as a method that returns a builder gives the correct order for free, because the case's own calls always come after.

### `ArbitraryBuilder` is mutable

`set`, `size`, `setNull`, `thenApply`, `fixed` and the rest **mutate the builder and return `this`**, and `sample()` does not reset it. Only `copy()`, `map()`, and `zipWith()` hand back a new instance.

That makes the sharing style load-bearing. A helper *method* is safe because it constructs a fresh builder on every call:

```java
private ArbitraryBuilder<Order> paidOrder() {
    return fixtureMonkey.giveMeBuilder(Order.class)
        .set(javaGetter(Order::getStatus), OrderStatus.PAID);
}
```

A shared *field* is not. Every pin a test adds sticks to the one instance and leaks into whatever runs next, in execution order:

```java
private static final ArbitraryBuilder<Order> PAID_ORDER = ...;   // leaks between tests
```

If a builder must be held, call `copy()` before customizing it.

### Values helpers

From `com.navercorp.fixturemonkey.customizer.Values`:

- `Values.just(value)` — place the value as-is, blocking decomposition. **Use it only when fixing that exact instance is genuinely required.**
- `Values.unique(supplier)` — draw a distinct value per generation, for uniquely-constrained columns.

Plain `set` decomposes the value and regenerates its properties, so a pre-built list or nested object comes back with different inner values and nothing is raised to say so. That silence is worth recognising, but it is not by itself a reason to wrap: if the assertion does not depend on those inner values, plain `set` is the correct call and leaves them random. `Values.just` freezes a subtree, which carries the same cost as `fixed()` — the frozen part stops exploring — so reserve it for the case where the exact instance is the thing under test.

## Constraining generated values

Random does not have to mean arbitrary. These are the APIs that narrow what generation produces, in ascending order of scope. For *which* one to reach for, see [Constrain the values you did not pin](https://naver.github.io/fixture-monkey/docs/agent-guide/writing-tests#constrain-the-values-you-did-not-pin) — the short version is to take the widest scope that fits, so the fewest places restate the rule.

**Per property, on the builder**

| Call | Effect |
| :--- | :--- |
| `set(selector, arbitrary)` | Any jqwik `Arbitrary`: `Arbitraries.strings().numeric().ofLength(12)`, `.alpha()`, `.ascii()`, `ofMinLength`/`ofMaxLength`, `Arbitraries.longs().greaterThan(100)` |
| `set(value)` | No selector — constrains the root of the builder. This is the form `register` uses |
| `set(selector, Values.unique(supplier))` | A distinct value per generation |
| `thenApply((sample, builder) -> ...)` | Derive one property from another after generation |
| `setPostCondition(selector, type, predicate)` | Rejection sampling. Last resort: it regenerates until the predicate passes, so it is slow and can fail to converge |

**Per type, on the instance**

| Call | Effect |
| :--- | :--- |
| `register(Class, fixture -> builder)` | A default builder for the type and its subtypes, applied wherever the type appears |
| `registerExactType(Class, ...)` / `registerAssignableType(Class, ...)` | The same, with the matcher stated explicitly |
| `register(MatcherOperator, priority)` | Arbitrary matching, with priority — lower number wins, and equal priorities are picked randomly |
| `registerGroup(Class...)` | Collect many registrations in one class: every method that takes a single `FixtureMonkey` parameter and returns an `ArbitraryBuilder<T>` is registered for `T` |

**Project-wide, through a plugin**

| Plugin | Effect |
| :--- | :--- |
| `new JakartaValidationPlugin()`, `new JavaxValidationPlugin()` | Generation honours Bean Validation annotations already on the production fields — `@Size`, `@Min`, `@Max`, `@Digits`, `@Pattern`, `@Email`, `@NotBlank`, `@NotEmpty`, `@Past`, `@Future`. Prefer this to restating the same rule in test code |
| `new JqwikPlugin().javaTypeArbitraryGenerator(...)` | Override the default generator for built-in types by implementing `strings()`, `integers()`, `longs()`, `characters()` and so on. `monkeyStrings()` returns a `MonkeyStringArbitrary`, whose `filterCharacter(predicate)` restricts the character set; the default already excludes ISO control characters |
| `new JqwikPlugin().javaTimeTypeArbitraryGenerator(...)` | The same for `java.time` and `Date` — override `localDates()`, `instants()`, `zonedDateTimes()` and the rest. Defaults span one year either side of now |
| `new JqwikPlugin().javaArbitraryResolver(...)` / `.javaTimeArbitraryResolver(...)` | Change how constraints are *applied* to those arbitraries, rather than which arbitrary is used |
| `javaConstraintGenerator(...)`, `pushJavaConstraintGeneratorCustomizer(...)` | Teach Fixture Monkey a project's own constraint annotations |
| `new DataFakerPlugin()` | Realistic values — names, addresses, and similar |

An unsatisfiable constraint is not silent: generation retries and then throws `RetryableFilterMissException`, naming the property that could not be produced.

## How objects get constructed

This is the most common source of "Fixture Monkey does not work on my class". Different class shapes are constructed in completely different ways, and the mechanism is chosen at three levels of scope. Work from the narrowest scope that solves the problem.

### Measure the candidates before classifying

The tables in this section classify a class by its shape, and that classification is a guess. It is right most of the time and silently wrong on precisely the cases that cost the most — a type that *is* built, by an introspector that never writes half of it.

The guess is avoidable. Every introspector can be run against the real class in about two seconds, using nothing but `jshell` and the module's test runtime classpath. Three outcomes matter per candidate: it cannot build the type at all, it builds it but leaves some properties unwritten, or it writes everything.

```java
FixtureMonkey sut = FixtureMonkey.builder()
    .objectIntrospector(candidate)
    .defaultNotNull(true)
    .build();

Object sample = sut.giveMeOne(Order.class);   // null means this candidate cannot build it
```

Then read every declared field back by reflection. A field still holding its JVM default was never written — **and a `set` targeting it would be dropped with no exception and no warning.** Two details keep that inference honest:

- **Numbers and booleans need sentinels.** `0` and `false` are legitimate random values, so an unwritten `int` is indistinguishable from a written one. Push a constant for each primitive and wrapper first, and the default becomes proof of absence: `pushExactTypeArbitraryIntrospector(int.class, context -> new ArbitraryIntrospectorResult(CombinableArbitrary.from(7)))`.
- **Reference types need repetition.** Some values generate as `null` some of the time regardless of the introspector, and `defaultNotNull(true)` does not always override it — `ZoneId` is one. Sample about five times and separate *always* default, which is the introspector, from *sometimes* default, which is nullability and needs a pin instead.

The [Claude Code plugin](https://github.com/naver/fixture-monkey/tree/main/claude-plugins/fixture-monkey/skills/write-fixture/scripts) bundles this as a ready `jshell` script plus a Gradle init script that prints a module's test runtime classpath without touching the build files. Where `jshell` is unavailable it is a JDK 9+ tool, and the classification tables below remain the fallback.

#### Choosing when several candidates work

More than one introspector usually writes everything, and that is not a reason to combine them:

1. **One candidate is complete for every type under test** — set it globally with `objectIntrospector(...)`. This is the common outcome.
2. **Several cover every type** — take the one that names how the class is actually built: `ConstructorProperties` for records and immutables, `Bean` for JavaBeans, `PrimaryConstructor` for Kotlin. Rank `PriorityConstructor` and `Jackson` last, since they can succeed for reasons unrelated to the class's intended shape.
3. **Different types need different introspectors** — the one covering the most types globally, plus a [Level 2](#level-2--per-type) override per exception. Two or three overrides beat a chain.
4. **The exceptions are too numerous to enumerate** — only then a `FailoverIntrospector`, measured rather than assumed. Probing the chain as a single candidate turns [the ordering trap](#the-failover-ordering-trap) into an observation: the same two introspectors leave a field unwritten in one order and write it in the other.

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
| Interfaces, abstract classes, sealed types | — | `new InterfacePlugin()`; see [Interface plugin](https://naver.github.io/fixture-monkey/docs/plugins/interface-plugin/features) |
| Mockito mocks | `MockitoIntrospector.INSTANCE` | `fixture-monkey-mockito` |
| Realistic names, addresses, and similar values | `DataFakerArbitraryIntrospector` | `new DataFakerPlugin()` |

When no introspector matches, interfaces with no-argument methods fall back to `AnonymousArbitraryIntrospector`, which proxies them.

Set the one you picked on the builder:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
```

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
| **Building blocks for a custom introspector** | `CompositeArbitraryIntrospector`, `MatchArbitraryIntrospector`, `TypedArbitraryIntrospector` — see [Custom introspector](https://naver.github.io/fixture-monkey/docs/generating-objects/custom-introspector) |

If a collection, enum, or `java.time` value is generating incorrectly, the object introspector is not the cause. Look at the plugin set or the relevant option instead.

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

`useExpressionStrictMode()` does **not** catch this. The path resolves to a real property; the introspector simply never writes it.

Which order is correct is measurable rather than arguable — probe the chain as a single candidate, as described [above](#measure-the-candidates-before-classifying), and read off which fields each ordering leaves unwritten.

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

See [Introspector](https://naver.github.io/fixture-monkey/docs/generating-objects/introspector) and [Instantiate methods](https://naver.github.io/fixture-monkey/docs/generating-objects/instantiate-methods) for the full treatment.

## Other setup

| Situation | Option |
| :--- | :--- |
| Kotlin | `.plugin(KotlinPlugin())` |
| Jackson-annotated types | `.plugin(new JacksonPlugin())` |
| Bean Validation annotations should be honoured | `.plugin(new JakartaValidationPlugin())` |
| Nulls are getting in the way | `.defaultNotNull(true)`, or `defaultNullInjectGenerator` for a rate other than the 0.2 default |
| Interfaces and sealed types | `.plugin(new InterfacePlugin())` |

See [Fixture Monkey options](https://naver.github.io/fixture-monkey/docs/fixture-monkey-options/overview) for the full option set, and the Plugins section for each plugin — [Kotlin](https://naver.github.io/fixture-monkey/docs/plugins/kotlin-plugin/features) is the one most agents need.

## Reproducibility

`fixture-monkey-junit-jupiter` provides `@Seed(1234L)`. It fixes the seed for one test method and logs the seed of any failing test so a CI failure can be replayed. Use it to diagnose an intermittent failure, not to make one go away.
