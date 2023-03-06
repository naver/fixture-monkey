---
title: "FixtureMonkey"
weight: 1
---

## Generating
### FixtureMonkey with default options
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.create();
```

### FixtureMonkey with custom options
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	+ options...
    .build();
```

## Option
### Alter option
| Option                           | Description                                                                                              | Default Value                                                          | Link                                                                                  |
|----------------------------------|----------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| arbitraryValidator               | Arbitrary sampled value is validated by `arbitraryValidator`                                             | new DefaultArbitraryValidator()                                        | [ArbitraryValidator]({{< relref "/docs/v0.4/components/arbitraryvalidator" >}})       |
 | defaultArbitraryContainerInfo    | Default size for a randomly generated instance of container type                                         | new ArbitraryContainerInfo(0, defaultArbitraryContainerMaxSize, false) |                                                                                       |
 | defaultArbitraryContainerMaxSize | Default max size for a randomly generated instance of container type                                     | 3                                                                      |                                                                                       |
| defaultNullInjectGenerator       | Determines when a null instance is created                                                               | new DefaultNullInjectGenerator(...)                                    |                                                                                       |
| nullableContainer                | Determines whether generate a randomly generated container instance                                      | false                                                                  |                                                                                       |
| nullableElement                  | Determines whether generate a randomly generated element instance                                        | false                                                                  |                                                                                       |
 | defaultNotNull                   | Determines whether generate a null instance                                                              | false                                                                  |                                                                                       |
 | defaultObjectPropertyGenerator   | Determines how to generate `ObjectProperty`                                                              | DEFAULT_OBJECT_PROPERTY_GENERATOR                                      | [ObjectProperty]({{< relref "/docs/v0.4/components/objectproperty" >}})               |
 | defaultPropertyGenerator         | Determines how to generate child properties                                                              | new DefaultPropertyGenerator()                                         |                                                                                       |
 | defaultPropertyNameResolver      | Determines how property resolves name                                                                    | DEFAULT_PROPERTY_NAME_RESOLVER                                         |                                                                                       |
 | javaArbitraryResolver            | Resolves how annotations apply to default Java classes                                                   | new JavaArbitraryResolver() {}                                         |                                                                                       |
 | javaTimeArbitraryResolver        | Resolves how annotations apply to default Java Time/Date classes                                         | new JavaTimeArbitraryResolver() {}                                     |                                                                                       |
 | javaTypeArbitraryGenerator       | Determines a default value for default Java classes                                                      | new JavaTypeArbitraryGenerator() {}                                    |                                                                                       |
 | javaTimeTypeArbitraryGenerator   | Determines a default value for default Java Time/Date classes                                            | new JavaTimeTypeArbitraryGenerator() {}                                |                                                                                       |
 | manipulatorOptimizer             | Determines how manipulators is optimized                                                                 | new NoneManipulatorOptimizer()                                         |                                                                                       |
 | monkeyExpressionFactory          | Determines general expression                                                                            | new ArbitraryExpressionFactory()                                       |                                                                                       |
 | objectIntrospector               | Determines how to create an instance                                                                     | BeanArbitraryIntrospector.INSTANCE                                     | [ArbitraryIntrospector]({{< relref "/docs/v0.4/components/arbitraryintrospector" >}}) |                                                                               |
 | useExpressionStrictMode          | Determines whether throws exception if property does not exist which is referenced by a given expression | false                                                                  |                                                                                       |

### Add option
| Option                              | Description                                                                        | Link                                                                                  |
|-------------------------------------|------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| addContainerType                    | Add user-defined container type                                                    | [ArbitraryIntrospector]({{< relref "/docs/v0.4/components/arbitraryintrospector" >}}) |
| addExceptGenerateClass              | Do not create an instance of the given class                                       |                                                                                       |
| addExceptGenerateClasses            | Do not create an instance of the given classes                                     |                                                                                       |
| addExceptGeneratePackage            | Do not create an instance of any classes which package starts with given package   |                                                                                       |
| addExceptGeneratePackages           | Do not create an instance of any classes which package starts with given packages  |                                                                                       |
| pushExceptGenerateType              | Do not create an instance of any classes which property is matched by matcher      |                                                                                       |
| pushContainerIntrospector           | Determines how to create an instance of given container type                       | [ArbitraryIntrospector]({{< relref "/docs/v0.4/components/arbitraryintrospector" >}}) |
| plugin                              | Add new plugin                                                                     |                                                                                       |
| pushArbitraryContainerInfoGenerator | Determines size for an instance of given container type                            |                                                                                       |
| pushFixtureCustomizer               | Determines how to customize composing properties and an instance of given property | [FixtureCustomizer]({{< relref "/docs/v0.4/components/fixturecustomizer" >}})         |
| pushArbitraryIntrospector           | Determines how to create an instance of given property                             | [ArbitraryIntrospector]({{< relref "/docs/v0.4/components/arbitraryintrospector" >}}) |
| pushObjectPropertyGenerator         | Determines how to generate `ObjectProperty` for given property                     | [ObjectProperty]({{< relref "/docs/v0.4/components/objectproperty" >}})               |
| pushContainerPropertyGenerator      | Determines how to generate `ContainerProperty` for given property                  | [ContainerProperty]({{< relref "/docs/v0.4/components/containerproperty" >}})         |
| pushPropertyGenerator               | Determines how to generate child properties for given property                     |                                                                                       |
| pushPropertyNameResolver            | Determines how property resolves name for given property                           |                                                                                       |
| pushNullInjectGenerator             | Determines when a null instance is created for given property                      |                                                                                       |
| register                            | Determines default `ArbitraryBuilder` for given property                           |                                                                                       |
| registerGroup                       | Determines default `ArbitraryBuilder` for given properties in a group class        |        
