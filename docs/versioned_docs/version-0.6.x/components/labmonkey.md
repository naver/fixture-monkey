---
title: "FixtureMonkey"
sidebar_position: 41
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
### Alter option:

| Option                           | Description                                                                                              | Link                                              |
|----------------------------------|----------------------------------------------------------------------------------------------------------|---------------------------------------------------|
| arbitraryValidator               | Arbitrary sampled value is validated by `arbitraryValidator`. The default validator is `new DefaultArbitraryValidator()`. | [ArbitraryValidator](./arbitraryvalidator)       |
| defaultArbitraryContainerInfo    | Default size for a randomly generated instance of container type. The default value is `new ArbitraryContainerInfo(0, defaultArbitraryContainerMaxSize, false)`. |                                                   |
| defaultArbitraryContainerMaxSize | Default max size for a randomly generated instance of container type. The default value is `3`.         |                                                   |
| defaultNullInjectGenerator       | Determines when a null instance is created. The default value is `new DefaultNullInjectGenerator(...)`. |                                                   |
| nullableContainer                | Determines whether to generate a randomly generated container instance. The default value is `false`.   |                                                   |
| nullableElement                  | Determines whether to generate a randomly generated element instance. The default value is `false`.     |                                                   |
| defaultNotNull                   | Determines whether to generate a null instance. The default value is `false`.                             |                                                   |
| defaultObjectPropertyGenerator   | Determines how to generate `ObjectProperty`. The default value is `DEFAULT_OBJECT_PROPERTY_GENERATOR`.   | [ObjectProperty](./objectproperty)               |
| defaultPropertyGenerator         | Determines how to generate child properties. The default value is `new DefaultPropertyGenerator()`.      |                                                   |
| defaultPropertyNameResolver      | Determines how property resolves name. The default value is `DEFAULT_PROPERTY_NAME_RESOLVER`.             |                                                   |
| javaArbitraryResolver            | Resolves how annotations apply to default Java classes. The default value is `new JavaArbitraryResolver() {}`. |                                                   |
| javaTimeArbitraryResolver        | Resolves how annotations apply to default Java Time/Date classes. The default value is `new JavaTimeArbitraryResolver() {}`. |                                                   |
| javaTypeArbitraryGenerator       | Determines a default value for default Java classes. The default value is `new JavaTypeArbitraryGenerator() {}`. |                                                   |
| javaTimeTypeArbitraryGenerator   | Determines a default value for default Java Time/Date classes. The default value is `new JavaTimeTypeArbitraryGenerator() {}`. |                                                   |
| manipulatorOptimizer             | Determines how manipulators are optimized. The default value is `new NoneManipulatorOptimizer()`.         |                                                   |
| monkeyExpressionFactory          | Determines general expression. The default value is `new ArbitraryExpressionFactory()`.                   |                                                   |
| objectIntrospector               | Determines how to create an instance. The default value is `BeanArbitraryIntrospector.INSTANCE`.          | [ArbitraryIntrospector](./arbitraryintrospector) |
| useExpressionStrictMode          | Determines whether to throw an exception if a property referenced by a given expression does not exist. The default value is `false`. |                                                   |

### Add option
| Option                              | Description                                                                        | Link                                              |
|-------------------------------------|------------------------------------------------------------------------------------|---------------------------------------------------|
| addContainerType                    | Add user-defined container type                                                    | [ArbitraryIntrospector](./arbitraryintrospector) |
| addExceptGenerateClass              | Do not create an instance of the given class                                       |                                                   |
| addExceptGenerateClasses            | Do not create an instance of the given classes                                     |                                                   |
| addExceptGeneratePackage            | Do not create an instance of any classes which package starts with given package   |                                                   |
| addExceptGeneratePackages           | Do not create an instance of any classes which package starts with given packages  |                                                   |
| pushExceptGenerateType              | Do not create an instance of any classes which property is matched by matcher      |                                                   |
| pushContainerIntrospector           | Determines how to create an instance of given container type                       | [ArbitraryIntrospector](./arbitraryintrospector) |
| plugin                              | Add new plugin                                                                     |                                                   |
| pushArbitraryContainerInfoGenerator | Determines size for an instance of given container type                            |                                                   |
| pushFixtureCustomizer               | Determines how to customize composing properties and an instance of given property | [FixtureCustomizer](./fixturecustomizer)         |
| pushArbitraryIntrospector           | Determines how to create an instance of given property                             | [ArbitraryIntrospector](./arbitraryintrospector) |
| pushObjectPropertyGenerator         | Determines how to generate `ObjectProperty` for given property                     | [ObjectProperty](./objectproperty)               |
| pushContainerPropertyGenerator      | Determines how to generate `ContainerProperty` for given property                  | [ContainerProperty](./containerproperty)         |
| pushPropertyGenerator               | Determines how to generate child properties for given property                     |                                                   |
| pushPropertyNameResolver            | Determines how property resolves name for given property                           |                                                   |
| pushNullInjectGenerator             | Determines when a null instance is created for given property                      |                                                   |
| register                            | Determines default `ArbitraryBuilder` for given property                           |                                                   |
| registerGroup                       | Determines default `ArbitraryBuilder` for given properties in a group class        |        

