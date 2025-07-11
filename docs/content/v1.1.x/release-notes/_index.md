---
title: "Release Notes"
images: []
menu:
docs:
weight: 100
---

## v1.1.x

### v1.1.14
Fix the property instantiation within the register option

Add a new 'register' option with the priority

### v1.1.13
Fix the issue where the instantiate API is not working within the register

Fix BeanArbitraryIntrospector throw NPE when retrieving setter

### v1.1.12
Fix size API within register that applies to root type

Add compatibility with TypedPropertySelector and path expression DSL

Fix bugs in SimpleValueJqwikPlugin if minSize is greater than default max size

Implement InnerSpec Kotlin DSL to resolve parameter shadowing

Add StringCombinableArbitrary, IntegerCombinableArbitrary for easy efficient customisation

### v1.1.11
Add generating Kotlin sealed object type

### v1.1.10
Add support for deterministic testing with JUnit, the tests annotated by `@Seed` would be deterministic.

Fix the functional interface instance supports `equals`, `hashCode`, `toString`.

Fix instantiate API with Kotlin value class.

Add support generating Kotlin constant object.

Refactor simplifying numeric validation.

Refactor addressing Java expGetter record.

Refactor resolving expressions as programmatic primarily.

### v1.1.9
Fix `abstractExtends` in InterfacePlugin does not support interface, please use `interafaceImplements` instead.

Fix a record instance generates canonical constructor properties.

Deprecate `InterfaceCandidateConcretePropertyResolver`, please use `ConcreteTypeCandidateConcretePropertyResolver` instead.

Fix modifying the number option in `SimpleValueJqwikPlugin`.

### v1.1.8
Fix not applying registered child manipulations if parent uses `thenApply`.

Fix InnerSpec `inner` API.

### v1.1.7
Remove default FixtureMonkeyOptions, use `FixtureMonkeyOptions.builder().build()` instead.

Refactor setting size variants of elements in container.

### v1.1.6
Deprecate `Randoms.create(String)`, use `Randoms.setSeed(long)` instead.

Fix an instance generated by `ConstructorPropertiesArbitraryIntrospector` that was not validated by validation annotations.

Fix using `KotlinArbitraryBuilder` when generating an `Arb` in kotest module.

### v1.1.5
Fix keep equivalence with `ElemenetProperty` and `MapKeyElementProperty` and `MapValueElementProperty`.

### v1.1.4
Fix not registering size API if decomposing.

Add enrich the failed test log with seed.

### v1.1.3
Fix generating empty String with @Size annotation.

### v1.1.2
Fix setting recursive implementations of self reference object.

Fix registering JavaBuilder and KotlinBuilder.

### v1.1.1
Fix set a recursive object.

Add a missing `giveMeJavaBuilder` with an object parameter.

## v1.0.x

### v1.0.29
Fix generating empty String with @Size annotation.

### v1.0.28
Add support for `hashCode`, `equals`, `toString` in anonymous object

Add `enableLoggingFail` option to dismiss the failed logging in `FixtureMonkeyBuilder`.

Add supporting for the `is` prefix boolean within `javaGetter`.

### v1.0.27
Add `enableLoggingFail` option as a constructor argument in `FailoverIntrospector`

### v1.0.26
Add `PriorityConstructorArbitraryIntrospector`

Add `korean` method in MonkeyStringArbitrary

### v1.0.25
Fix concurrency issue with string generation

Fix seed setting as annotated

### v1.0.24
Deprecate `ElementJsonSubTypesObjectPropertyGenerator`, `PropertyJsonSubTypesObjectPropertyGenerator` in `fixture-monkey-jackson` module.
Please use the `ElementJsonSubTypesConcreteTypeResolver`, `PropertyJsonSubTypesConcreteTypeResolver` instead.

Add new APIs that generates the unique value by `Values.unique(Supplier)` or `CombinableArbitrary.unique()`.

Check out the examples below. 
```java
.set("$[*]", Values.unique(() -> Arbitraries.integers().between(0, 3).sample()))`, 
```

```java
.<List<Integer>>customizeProperty(typedRoot(), CombinableArbitrary::unique)
```

Add `@Seed` to reproduce the randomly populated object in `fixture-monkey-junit-jupiter` module.

### v1.0.23
Add the flexible option for complex usage in InterfacePlugin.

Fix for generating Kotlin self-reference with default arguments.

### v1.0.22
Add compatibility with ObjectPropertyGenerator and CandidateConcretePropertyResolver.

Add regenerate when container is filtered.

Deprecate the pushExactTypePropertyCandidateResolver option. Use `InterfacePlugin` instead.

### v1.0.21
Deprecate the ObjectPropertyGenerator that modify child properties listed below.
For example, `InterfaceObjectPropertyGenerator`, `SealedTypeObjectPropertyGenerator`, `SingleValueObjectPropertyGenerator`
Use `ConcreteTypeCandidateConcretePropertyResolver`, `SealedTypeCandidateConcretePropertyResolver` instead.

Fix set `ZoneId` in Kotlin JDK21.

Fix collection generation not throw exception.

Fix decompose Java Kotlin functional interface.

### v1.0.20
Fix generation of enum implementations as a sealed class in JDK17.

Add support for multi-level inheritance of sealed class and sealed interface.

Fix JdkVariantOptions having higher priority than custom options.

### v1.0.19
Fix a SimpleValuePlugin "out of byte range" error when generate Byte.

### v1.0.18
Fix SimpleValueJqwikPlugin mismatching order with constructor and field.

### v1.0.17
Modify the way a value class is used to output arbitrary value.

Fix `sealedInterface` set not working after thenApply.

Add supporting `Supplier` type.

Add a new Plugin `SimpleValueJqwikPlugin` for beginners, it provides a readable String, limited scope of Number and
Date. It can customize them as well.

### v1.0.16
Add resolve the candidate concrete type of container type.

Fix register working on assignable type.

### v1.0.15
Add `ConcreteTypeDefinition` in `ArbitraryProperty`, deprecate `getChildPropertiesByResolvedProperty` and `getChildPropertyListsByCandidateProperty` which is added in 1.0.14.

Add "KotlinDurationIntrospector" supporting generating a Duration type in Kotlin.

Fix setting a child of a concrete type to an abstract type.

Add a new Kotlin Exp expression for referencing root. ex. `set(String::root, "expected")`

### v1.0.14
Add supporting value class with the private constructor.

Add supporting for sealed class and sealed interface.

Deprecate `nullInject` and `childPropertyListsByCandidateProperty` properties in `ObjectNode`. They would be moved to `ArbitraryProperty`. 

### v1.0.13
Add InterfacePlugin supports abstract classes through `abstractClassExtends` option.

Fix setLazy with value wrapped by Just would not be manipulated.

Fix missing required PropertyGenerator within introspectors.

### v1.0.12
Fix generating an object with the value class property.

### v1.0.11
Fix mutation of a generated object by FieldReflection, BeanArbitraryIntrospector

### v1.0.10
Fix setting object field by any other type.

Refactor does not throw exception when it cannot generate, the next ArbitraryIntrospector will be used.

Add handling when using an ArbitraryIntrospector that does not match the property type. Add a log entry if the property is of a container type, and throw an exception if the property is of a concrete type.

Add better support Kotest by modifying `giveMeArb` more Kotlin-like, by adding `setArb` API.

### v1.0.9
Fix the `addExceptGenerateClass` to be unaffected by the specific `ArbitraryIntrospector` used

Add customizing Wildcard type in an option

#### Experimental Feature
Add `customizeProperty` API in `ArbitraryBuilder` to customize generated value

### v1.0.8
Improve the error message with specific details about the failed type when generation fails

Fix setting child type when generating parent type

### v1.0.7
Fix the fixture-monkey-kotlin module to be compatible with JDK 8

### v1.0.6
Fixture Monkey now infers properties when generating a type using a specific `ArbitraryIntrospector` that requires certain types of properties
(Refer to the comment in `ArbitraryGenerator#getRequiredPropertyGenerator`)

Support generating an instance of a Kotlin type with a private constructor

Introduce `InterfacePlugin` featuring detailed interface options (`interfaceImplements`, `useAnonymousArbitraryIntrospector` option)

The `interfaceImplements` option in `FixtureMonkeyBuilder` is now deprecated. It will be moved in `InterfacePlugin`

### v1.0.5
Fix `ConstructorPropertiesArbitraryIntrospector` to be able to generate a type that does not use Lombok

Fix `FailoverIntrospector` catching an exception thrown by declaring a `CombinableArbitrary`

### v1.0.4
Record types are generated using the canonical constructor by default

### v1.0.3
Introduce a new option `pushJavaConstraintGeneratorCustomizer` to customize the `JavaConstraintGenerator` option

Parallel execution is now supported with the `jqwik` engine

Fix the generation of decimal values, ensuring a minimum of 0, with the kotest-property engine

### v1.0.2
Fix generating a record instance with 2 or more constructors

### v1.0.1
Add Exp DSL resolving an array element at a specific index

### v1.0.0
Add instantiate as stable API

Kotlin object generation with instantiateBy constructor, now utilizes the provided Kotlin constructor

#### Experimental Feature
- Add a new property selector javaGetter replacing String expression with type-safe method reference.

## v0.6.x

### v0.6.12
Fix setting Just in setLazy.

Fix a bug in validOnly operation in ArbitraryBuilder.

Fix a bug in addContainerType, addDecomposedContainerValueFactory option, which is not working for an implementation of option type.

Remove jqwik-kotlin dependency in fixture-monkey-kotlin module.

Deprecate FixtureMonkeyOptions dependency in ObjectPropertyGeneratorContext, ContainerPropertyGeneratorContext.

Add addDecomposedContainerValueFactory option.

Add giveMeExperimentalBuilder for experimental features.

#### Experimental Feature
- Add a new ArbitraryBuilder operation instantiate, instantiateBy for Kotlin, which specifies how to instantiate a given type.

- Providing a static method constructor() for specifying that it instantiates a given type by constructor.

- Providing a static method factoryMethod() for specifying that it instantiates a given type by factory method.

- Providing a static method field(), javaBeansProperty() that subsequently sets a property for both constructor(), factoryMethod()

### v0.6.11
Add kotest module. Using it as a runtime of generating primitive types.

Add supporting a custom validator in Javax, Jakarta Bean Validation.

Add PropertySelecotr as a super type of ExpressionGenerator to abstract how to reference a property.

Fix a bug in size Map in thenApply operation.

Add option to resolve a seed for deterministic re-runs.

Fix set a nested self reference object.

### v0.6.10
An anonymous object generated by Fixture Monkey could invoke a default method instead of invoking an arbitrary method.

### v0.6.9
Fix sampleList always returns a same element with AnonymousArbitraryIntrospector.

Remove CombinableArbitrary.from(Arbitrary). Use ArbitraryUtils.toCombinableArbitrary instead.

Refactor CombinableArbitrary.from has a type parameter instead of wildcard type.

Fix generating self reference map type with different key type.

Refactor setPostCondition does not cause any performance issue.

Fix generating a unique key of Map.

Add new option javaConstraintGenerator which defines a constraint of String, Decimal type, Integer type, DateTime type, Container type.

### v0.6.8
`DefaultDecomposedContainerValueFactory` supports `Map.Entry`

A concurrency bug fixed in `PrimaryConstructorArbitraryIntrospector`

### v0.6.7
Deprecate `List<PropertygenerateChildProperties(AnnotatedType annotatedType)` in `PropertyGenerator` interface, it will be removed in 0.7.0

Add `List<PropertygenerateChildProperties(Property property)` in `PropertyGenerator` interface

### v0.6.6
Log the error instead of throwing an exception if setting field is failed with `FieldReflectionArbitraryIntrospector`

Add more support for kotlin extensions in FixtureMonkeyBuilder
