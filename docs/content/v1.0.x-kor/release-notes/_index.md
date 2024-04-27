---
title: "Release Notes"
images: []
menu:
docs:
weight: 100
---

sectionStart
### v.1.0.16
Add resolve the candidate concrete type of container type.

Fix register working on assignable type.

sectionEnd

sectionStart
### v.1.0.15
Add `ConcreteTypeDefinition` in `ArbitraryProperty`, deprecate `getChildPropertiesByResolvedProperty` and `getChildPropertyListsByCandidateProperty` which is added in 1.0.14.

Add "KotlinDurationIntrospector" supporting generating a Duration type in Kotlin.

Fix setting a child of a concrete type to an abstract type.

sectionEnd

sectionStart
### v.1.0.14
Add supporting value class with the private constructor.

Add supporting for sealed class and sealed interface.

Deprecate `nullInject` and `childPropertyListsByCandidateProperty` properties in `ObjectNode`. They would be moved to `ArbitraryProperty`.

sectionEnd

sectionStart
### v.1.0.13
Add InterfacePlugin supports abstract classes through `abstractClassExtends` option.

Fix setLazy with value wrapped by Just would not be manipulated.

Fix missing required PropertyGenerator within introspectors.

sectionEnd

sectionStart
### v.1.0.12
Fix generating an object with the value class property.

sectionEnd

sectionStart
### v.1.0.11
Fix mutation of a generated object by FieldReflection, BeanArbitraryIntrospector

sectionEnd

sectionStart
### v.1.0.10
Fix setting object field by any other type.

Refactor does not throw exception when it cannot generate, the next ArbitraryIntrospector will be used.

Add handling when using an ArbitraryIntrospector that does not match the property type. Add a log entry if the property is of a container type, and throw an exception if the property is of a concrete type.

Add better support Kotest by modifying `giveMeArb` more Kotlin-like, by adding `setArb` API.

sectionEnd

sectionStart
### v1.0.9
Fix the `addExceptGenerateClass` to be unaffected by the specific `ArbitraryIntrospector` used

Add customizing Wildcard type in an option

experimentalStart
##### Experimental Feature
Add `customizeProperty` API in `ArbitraryBuilder` to customize generated value

experimentalEnd

sectionEnd


sectionStart
### v1.0.8
Improve the error message with specific details about the failed type when generation fails

Fix setting child type when generating parent type

sectionEnd

sectionStart
### v1.0.7
Fix the fixture-monkey-kotlin module to be compatible with JDK 8

sectionEnd


sectionStart
### v1.0.6
Fixture Monkey now infers properties when generating a type using a specific `ArbitraryIntrospector` that requires certain types of properties
(Refer to the comment in `ArbitraryGenerator#getRequiredPropertyGenerator`)

Support generating an instance of a Kotlin type with a private constructor

Introduce `InterfacePlugin` featuring detailed interface options (`interfaceImplements`, `useAnonymousArbitraryIntrospector` option)

The `interfaceImplements` option in `FixtureMonkeyBuilder` is now deprecated. It will be moved in `InterfacePlugin`

sectionEnd

sectionStart
### v1.0.5
Fix `ConstructorPropertiesArbitraryIntrospector` to be able to generate a type that does not use Lombok

Fix `FailoverIntrospector` catching an exception thrown by declaring a `CombinableArbitrary`

sectionEnd

sectionStart
### v1.0.4
Record types are generated using the canonical constructor by default

sectionEnd

sectionStart
### v1.0.3
Introduce a new option `pushJavaConstraintGeneratorCustomizer` to customize the `JavaConstraintGenerator` option

Parallel execution is now supported with the `jqwik` engine

Fix the generation of decimal values, ensuring a minimum of 0, with the kotest-property engine

sectionEnd

sectionStart
### v1.0.2
Fix generating a record instance with 2 or more constructors

sectionEnd

sectionStart
### v1.0.1
Add Exp DSL resolving an array element at a specific index

sectionEnd

sectionStart
### v1.0.x
Add instantiate as stable API

Kotlin object generation with instantiateBy constructor, now utilizes the provided Kotlin constructor

experimentalStart
##### Experimental Feature

- Add a new property selector javaGetter replacing String expression with type-safe method reference.

experimentalEnd

sectionEnd
## v0.6.x

sectionStart
### v0.6.12
Fix setting Just in setLazy.

Fix a bug in validOnly operation in ArbitraryBuilder.

Fix a bug in addContainerType, addDecomposedContainerValueFactory option, which is not working for an implementation of option type.

Remove jqwik-kotlin dependency in fixture-monkey-kotlin module.

Deprecate FixtureMonkeyOptions dependency in ObjectPropertyGeneratorContext, ContainerPropertyGeneratorContext.

Add addDecomposedContainerValueFactory option.

Add giveMeExperimentalBuilder for experimental features.

experimentalStart
##### Experimental Feature
- Add a new ArbitraryBuilder operation instantiate, instantiateBy for Kotlin, which specifies how to instantiate a given type.

- Providing a static method constructor() for specifying that it instantiates a given type by constructor.

- Providing a static method factoryMethod() for specifying that it instantiates a given type by factory method.

- Providing a static method field(), javaBeansProperty() that subsequently sets a property for both constructor(), factoryMethod()

experimentalEnd

sectionEnd

sectionStart
### v0.6.11
Add kotest module. Using it as a runtime of generating primitive types.

Add supporting a custom validator in Javax, Jakarta Bean Validation.

Add PropertySelecotr as a super type of ExpressionGenerator to abstract how to reference a property.

Fix a bug in size Map in thenApply operation.

Add option to resolve a seed for deterministic re-runs.

Fix set a nested self reference object.

sectionEnd

sectionStart
### v0.6.10
An anonymous object generated by Fixture Monkey could invoke a default method instead of invoking an arbitrary method.

sectionEnd

sectionStart
### v0.6.9
Fix sampleList always returns a same element with AnonymousArbitraryIntrospector.

Remove CombinableArbitrary.from(Arbitrary). Use ArbitraryUtils.toCombinableArbitrary instead.

Refactor CombinableArbitrary.from has a type parameter instead of wildcard type.

Fix generating self reference map type with different key type.

Refactor setPostCondition does not cause any performance issue.

Fix generating a unique key of Map.

Add new option javaConstraintGenerator which defines a constraint of String, Decimal type, Integer type, DateTime type, Container type.

sectionEnd

sectionStart
### v0.6.8
`DefaultDecomposedContainerValueFactory` supports `Map.Entry`

A concurrency bug fixed in `PrimaryConstructorArbitraryIntrospector`

sectionEnd

sectionStart
### v0.6.7
Deprecate `List<PropertygenerateChildProperties(AnnotatedType annotatedType)` in `PropertyGenerator` interface, it will be removed in 0.7.0

Add `List<PropertygenerateChildProperties(Property property)` in `PropertyGenerator` interface

sectionEnd

sectionStart
### v0.6.6
Log the error instead of throwing an exception if setting field is failed with `FieldReflectionArbitraryIntrospector`

Add more support for kotlin extensions in FixtureMonkeyBuilder

sectionEnd
