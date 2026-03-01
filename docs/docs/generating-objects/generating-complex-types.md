---
title: "Generating Complex Types"
sidebar_position: 43
---


## Why Complex Types Matter in Testing

When writing real-world tests, you'll often need to work with complex objects that include:
- Generic types with multiple type parameters
- Self-referencing structures (like trees or graphs)
- Complex interface hierarchies
- Sealed or abstract classes

Manually creating instances of these types for testing can be extremely tedious and error-prone. This is where Fixture Monkey shines - it can automatically generate valid instances of even the most complex types with minimal code.

## How Fixture Monkey Handles Complex Types

Fixture Monkey analyzes the structure of your classes and interfaces at runtime, understanding their relationships and constraints. It then generates valid instances with all the necessary fields populated, even for nested and recursive structures.

For interfaces, Fixture Monkey applies special handling. When an interface has multiple implementations, Fixture Monkey randomly selects one of the available implementations to generate. This is especially useful when testing interfaces with various implementations. Of course, you can also explicitly specify which implementations to use. This behavior can be controlled in detail through the `InterfacePlugin`.

```java
// Example of specifying multiple implementations for a UserService interface
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(
        new InterfacePlugin()
            .interfaceImplements(UserService.class, 
                List.of(BasicUserService.class, PremiumUserService.class))
    )
    .build();

// One of the specified implementations will be randomly selected
UserService userService = fixtureMonkey.giveMeOne(UserService.class);
```

Let's look at examples of complex types and how to generate them with Fixture Monkey.

## Java

### Generic Objects

Generic types with type parameters can be challenging to instantiate correctly in tests:

```java
@Value
public static class GenericObject<T> {
   T foo;
}

@Value
public static class GenericArrayObject<T> {
   GenericObject<T>[] foo;
}

@Value
public static class TwoGenericObject<T, U> {
   T foo;
   U bar;
}

@Value
public static class ThreeGenericObject<T, U, V> {
   T foo;
   U bar;
   V baz;
}
```

To generate instances of these generic types with Fixture Monkey:

```java
// Simple generic with String
GenericObject<String> stringGeneric = fixtureMonkey.giveMeOne(
    new TypeReference<GenericObject<String>>() {}
);

// Generic with array
GenericArrayObject<Integer> arrayGeneric = fixtureMonkey.giveMeOne(
    new TypeReference<GenericArrayObject<Integer>>() {}
);

// Multiple type parameters
TwoGenericObject<String, Integer> twoParamGeneric = fixtureMonkey.giveMeOne(
    new TypeReference<TwoGenericObject<String, Integer>>() {}
);
```

### Generic Interfaces
```java
public interface GenericInterface<T> {
}

@Value
public static class GenericInterfaceImpl<T> implements GenericInterface<T> {
   T foo;
}

public interface TwoGenericInterface<T, U> {
}

@Value
public static class TwoGenericImpl<T, U> implements TwoGenericInterface<T, U> {
   T foo;

   U bar;
}
```

To generate interface implementations:

```java
// Generate an implementation of GenericInterface<String>
GenericInterface<String> genericInterface = fixtureMonkey.giveMeOne(
    new TypeReference<GenericInterface<String>>() {}
);
```

For example, when you have multiple classes implementing the same interface:

```java
public interface PaymentProcessor {
    void processPayment(double amount);
}

public class CreditCardProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        // Credit card payment processing logic
    }
}

public class BankTransferProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        // Bank transfer payment processing logic
    }
}

// One of the implementations will be randomly selected
PaymentProcessor processor = fixtureMonkey.giveMeOne(PaymentProcessor.class);
```

### SelfReference

Self-referencing types are particularly challenging to create manually but easy with Fixture Monkey:

```java
@Value
public class SelfReference {
   String foo;
   SelfReference bar;
}

@Value
public class SelfReferenceList {
   String foo;
   List<SelfReferenceList> bar;
}
```

Generate self-referencing objects with depth control:

```java
// Default generation (limited nesting depth to avoid infinite recursion)
SelfReference selfRef = fixtureMonkey.giveMeOne(SelfReference.class);

// With custom configuration to control container size
FixtureMonkey customFixture = FixtureMonkey.builder()
    .defaultArbitraryContainerInfo(new ContainerInfo(2, 2)) // Controls lists size
    .build();
    
SelfReferenceList refList = customFixture.giveMeOne(SelfReferenceList.class);
```

### Interface
```java
public interface Interface {
   String foo();

   Integer bar();
}

public interface InheritedInterface extends Interface {
   String foo();
}

public interface InheritedInterfaceWithSameNameMethod extends Interface {
   String foo();
}

public interface ContainerInterface {
   List<String> baz();

   Map<String, Integer> qux();
}

public interface InheritedTwoInterface extends Interface, ContainerInterface {
}
```

## Kotlin
### Generic Objects
```kotlin
class Generic<T>(val foo: T)

class GenericImpl(val foo: Generic<String>)
```

Generating Kotlin generic objects:

```kotlin
// Generate a Generic<Int>
val genericInt: Generic<Int> = fixtureMonkey.giveMeOne()

// Generate a GenericImpl with nested Generic<String>
val genericImpl: GenericImpl = fixtureMonkey.giveMeOne()
```

### SelfReference
```kotlin
class SelfReference(val foo: String, val bar: SelfReference?)
```

### Sealed class, Value class
```kotlin
sealed class SealedClass

object ObjectSealedClass : SealedClass()

class SealedClassImpl(val foo: String) : SealedClass()

@JvmInline
value class ValueClass(val foo: String)
```

Generating sealed classes and value classes in Kotlin:

```kotlin
// Fixture Monkey will choose a concrete implementation of the sealed class
val sealedClass: SealedClass = fixtureMonkey.giveMeOne()

// Generate a value class
val valueClass: ValueClass = fixtureMonkey.giveMeOne()
```

Kotlin sealed classes are handled similarly to interfaces. Fixture Monkey randomly selects one of the subclasses of the sealed class to generate.

## Tips for Working with Complex Types

1. Use `TypeReference` for generic types to preserve type information
2. For complex interfaces, you may need to configure implementation classes using `InterfacePlugin`
3. If you want to use only specific implementations for interfaces or abstract classes, use `InterfacePlugin.interfaceImplements()`
4. For very complex structures, consider breaking them down and building them step by step

