---
title: "FixtureMonkeyBuilder"
linkTitle: "FixtureMonkeyBuilder"
weight: 1
---

```java
FixtureMonkey fixture = FixtureMonkey.builder()
        .defaultGenerator(BuilderArbitraryGenerator.INSTANCE)    // default: BeanArbitraryGenerator.INSTANCE
        .putGenerator(CustomObject.class, FieldReflectionArbitraryGenerator.INSTANCE)
        .defaultContainerMaxSize(5)    // default: 3
        .nullInject(0.5)    // default: 0.2
        .nullableContainer(true)    // default: false
        .nullableContainerElement(true)    // default: false
        .nullGenerateTypes(Set.of(UnsupportedType.class, XXX.class))
        .exceptGeneratePackages(Set.of("com.naver.xxx", "com.naver.yyy"))    // default: "java.lang.reflect", "jdk.internal.reflect", "sun.reflect"
        .addCustomizer(CustomObject.class, new CustomObjectFixtureCustomizer())
        .fixtureValidator(new CustomFixtureValidator())    // default: CompositeFixtureValidator
        .addAnnotatedArbitraryGenerator(Tuple.class, TupleAnnotatedArbitraryGenerator())
        .defaultInterfaceSupplier(type -> null)
        .addInterfaceSupplier(SomeInterface.class, type -> ConcreateClass.INSTANCE)
        .defaultNotNull(true) // default: true
        .register(Order.class, defaultOrderBuilder())
        .registerGroup(FixtureMonkeyGroup.class)
        .build();

```


## Options
- defaultGenerator (default: BeanArbitraryGenerator.INSTANCE)
    - [ArbitraryGenerator]({{< relref "/docs/v0.3.x/features/arbitrarygenerator" >}})
- putGenerator
    - Use given generator if type is matched instead of defaultGenerator
    - [Example]({{< relref "/docs/v0.3.x/examples/arbitrarygenerator" >}})
- nullInject (default: 0.2)
    - Set probabilities to inject `null`.
    - If field has `@NotNull`, it would not inject `null`
- nullableContainer (default: false)
    - It decides whether containers like `Map`, `List`, `Set`, `Stream`, `Optional` allows to be `null` 
    - If `false`, set default Container (`new ArrayList()`) ,not `null`  ;
    - If `true`, set `null`, `null` probabilities depend on **nullInject**
    - Field with `@NotNull`, would not be `null`
- exceptGeneratePackages
    - Set packages always be `null`
    - Default packages are below, it always does not override
      > "java.lang.reflect"
      "jdk.internal.reflect"
      "sun.reflect"
      "com.naver.denma.domain.entity.AggregateMetaModel"
- addExceptGeneratePackage
    - Add package which class in given package always returns `null`
    - Appends to default exceptGeneratePackages
- customizers
    - Set ArbitraryCustomizers which is a collection of [ArbitraryCustomizer]({{< relref "/docs/v0.3.x/features/arbitrarycustomizer" >}})
- addCustomizer
    - Add specific type of [ArbitraryCustomizer]({{< relref "/docs/v0.3.x/features/arbitrarycustomizer" >}})
    - Appends to default ArbitraryCustomizers
- [arbitraryValidator]({{< relref "/docs/v0.3.x/features/arbitraryvalidator" >}}) (default: CompositeArbitraryValidator)
- addAnnotatedArbitraryGenerator
    - Add specified [type]({{< relref "/docs/v0.3.x/features/defaultsupportedtypes" >}})
    - Appends to default annotatedArbitraryGenerators. It overrides if given type already exists
    - [Example]({{< relref "/docs/v0.3.x/examples/annotatedarbitrarygenerator" >}})
- defaultInterfaceSupplier
  - Set default [InterfaceSupplier]({{< relref "/docs/v0.3.x/features/interfacesupplier" >}}) (default : `type -> null`) 
- addInterfaceSupplier
  - Determine returning implementation for `interface` or `abstract class`
  - Check out detail [here]({{< relref "/docs/v0.3.x/features/interfacesupplier" >}})
- defaultNotNull
  - If `true`, fields without `@Nullable` would not be null.
  - If `false`, fields without `@NotNull` would be null with `nullInject` probabilities.
- register
  - Register default ArbitraryBuilder for specific type
  - Check out detail [here]({{< relref "/docs/v0.3.x/features/registerarbitrarybuilder" >}})
- registerGroup
  - Register default ArbitraryBuilders by declaring methods which signatures are `ArbitraryBuilder<TYPE> NAME(FixtureMonkey fixture)`
  - Check out detail [here]({{< relref "/docs/v0.3.x/features/registerarbitrarybuilder" >}})
