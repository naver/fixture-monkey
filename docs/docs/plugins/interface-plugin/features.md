---
title: "Features"
sidebar_position: 41
---


The Interface Plugin is a powerful tool that enables Fixture Monkey to dynamically handle implementations of interfaces and abstract classes during object generation. It is particularly useful when you need to specify concrete implementations for interfaces or abstract classes in your test fixtures.

## Abstract

- Register concrete implementations for interfaces
- Register concrete implementations for abstract classes
- Anonymous arbitrary introspector usage option (default: enabled)
- Support for dynamic implementation resolution based on property characteristics using CandidateConcretePropertyResolver

## Basic Usage

```java
FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(MyInterface.class, Arrays.asList(MyInterfaceImpl1.class, MyInterfaceImpl2.class))
        .abstractClassExtends(MyAbstractClass.class, Arrays.asList(MyConcreteClass1.class, MyConcreteClass2.class))
    )
    .build();
```

### Disabling Anonymous Arbitrary Introspector

```java
FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(MyInterface.class, Arrays.asList(MyInterfaceImpl.class))
        .useAnonymousArbitraryIntrospector(false)
    )
    .build();
```

## API Reference

### interfaceImplements

Registers implementations for a given interface.

```java
public <T> InterfacePlugin interfaceImplements(
    Class<T> interfaceType,
    List<Class<? extends T>> implementations
)
```

Parameters:
- `interfaceType`: The interface class to be implemented
- `implementations`: List of classes implementing the interface

### abstractClassExtends

Registers implementations for a given abstract class.

```java
public <T> InterfacePlugin abstractClassExtends(
    Class<T> abstractClassType,
    List<Class<? extends T>> implementations
)
```

Parameters:
- `abstractClassType`: The abstract class type to be implemented
- `implementations`: List of classes implementing the abstract class

### useAnonymousArbitraryIntrospector

Configures the use of an anonymous arbitrary introspector. By default, this option is enabled (default value: true).
When enabled, it uses an instance of `AnonymousArbitraryIntrospector` as the fallback introspector.

```java
public InterfacePlugin useAnonymousArbitraryIntrospector(boolean useAnonymousArbitraryIntrospector)
```

Parameters:
- `useAnonymousArbitraryIntrospector`: Whether to use the anonymous arbitrary introspector (default: true)

Example:
```java
// Default behavior (anonymous introspector enabled)
FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(MyInterface.class, Arrays.asList(MyInterfaceImpl.class))
        .useAnonymousArbitraryIntrospector(true)
    )
    .build();

// Anonymous introspector disabled
FixtureMonkey sut2 = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(MyInterface.class, Arrays.asList(MyInterfaceImpl.class))
        .useAnonymousArbitraryIntrospector(false)
    )
    .build();
```

## Examples

### Basic Interface Implementation

```java
interface Animal {
    String sound();
}

class Dog implements Animal {
    @Override
    public String sound() {
        return "Woof";
    }
}

class Cat implements Animal {
    @Override
    public String sound() {
        return "Meow";
    }
}

FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(Animal.class, Arrays.asList(Dog.class, Cat.class))
    )
    .build();

// Returns either a Dog or Cat instance
Animal animal = sut.giveMeOne(Animal.class);
```

### Abstract Class Implementation

```java
abstract class Vehicle {
    abstract int getWheels();
}

class Car extends Vehicle {
    @Override
    int getWheels() {
        return 4;
    }
}

class Bike extends Vehicle {
    @Override
    int getWheels() {
        return 2;
    }
}

FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .abstractClassExtends(Vehicle.class, Arrays.asList(Car.class, Bike.class))
    )
    .build();

// Returns either a Car or Bike instance
Vehicle vehicle = sut.giveMeOne(Vehicle.class);
```

### Anonymous Object Generation

When `useAnonymousArbitraryIntrospector` is enabled, you can generate anonymous implementations for interfaces that don't have registered implementations. The plugin uses JDK Dynamic Proxy to create these implementations, allowing you to customize the generated values. Here's an example:

```java
interface UserService {
    String getUserName();
    int getUserAge();
    List<String> getUserRoles();
    
    // Default method - behavior varies by JDK version
    default String getFullInfo() {
        return getUserName() + " (" + getUserAge() + ")";
    }
}

FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .useAnonymousArbitraryIntrospector(true)
    )
    .build();

// Generates an anonymous implementation with random values using JDK Dynamic Proxy
UserService anonymousUserService = sut.giveMeOne(UserService.class);

// Customizes the generated values
UserService customAnonymousUserService = sut.giveMeBuilder(UserService.class)
    .set("userName", "John Doe")
    .set("userAge", 30)
    .set("userRoles", Arrays.asList("ADMIN", "USER"))
    .sample();
```

The generated anonymous implementation:
- Is created using JDK Dynamic Proxy
- Returns random values for all interface methods by default
- Can be customized using `giveMeBuilder`
- Maintains consistent values across multiple method calls
- Supports all primitive types, objects, and collections

Note: Default method behavior varies by JDK version:
- In JDK 17, default methods maintain their original implementation
- In JDK versions prior to 17, default methods are proxied and return random values
  - This means the original implementation is ignored, and default methods return random values like other methods
  - For example, the `getFullInfo()` method will return a random string instead of using its original implementation

## Advanced Usage

### Using Custom Matchers

```java
FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(
            new ExactTypeMatcher(MyInterface.class),
            Arrays.asList(MyInterfaceImpl1.class, MyInterfaceImpl2.class)
        )
    )
    .build();
```

### Using CandidateConcretePropertyResolver

`CandidateConcretePropertyResolver` provides a flexible way to dynamically determine concrete implementations for interfaces or abstract classes. It can make decisions at runtime based on various factors:

- Property name
- Property type
- Property annotations
- Property metadata
- Other property characteristics

The resolver's `resolve` method is called for each property that requires a concrete implementation, enabling you to:
1. Return a single implementation for specific cases
2. Return multiple implementations for random selection
3. Return different implementations based on property characteristics
4. Apply complex business logic to determine the appropriate implementation

Here's an example:

```java
interface Animal {
    String sound();
    String getName();
}

class Dog implements Animal {
    @Override
    public String sound() {
        return "Woof";
    }

    @Override
    public String getName() {
        return "Dog";
    }
}

class Cat implements Animal {
    @Override
    public String sound() {
        return "Meow";
    }

    @Override
    public String getName() {
        return "Cat";
    }
}

// Custom resolver that returns implementations only when the property name is "animal"
class AnimalResolver implements CandidateConcretePropertyResolver {
    @Override
    public List<Class<?>> resolve(Property property) {
        if ("animal".equals(property.getName())) {
            return Arrays.asList(Dog.class, Cat.class);
        }
        return Collections.emptyList();
    }
}

FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(
            new ExactTypeMatcher(Animal.class),
            new AnimalResolver()
        )
    )
    .build();

// Randomly selects either Dog or Cat when the property name is "animal"
Animal animal = sut.giveMeOne(Animal.class);
```

