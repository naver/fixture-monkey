---
title: "ArbitraryGenerator"
linkTitle: "ArbitraryGenerator"
weight: 6
---
`ArbitraryGenerator` would determine the way of instantiating of given types.

## BeanArbitraryGenerator (default)

- Generate Java Bean object
- `NoArgsConstructor` and `Setter` is required

```java

@Setter
@NoArgsConstructor
public class Person {
	private Long id;
	private String name;
}
```

## ConstructorPropertiesArbitraryGenerator
- Only one constructor and `@ConstructorProperties` is required
- All parameters in constructor should be in `@ConstructorProperties`, and should have same name
- Field name should be aligned with constructor parameter or `@ConstructorProperties` value name unless using `ArbitraryCustomizer`
  - Example [here]({{< relref "/docs/v0.3.x/examples/arbitrarygenerator#add-constructorproperties-value-when-field-name-is-different" >}})
- `ArbitraryCustomizer` could add extra parameter in `@ConstructorProperties` which is not in constructor
  - Example [here]({{< relref "/docs/v0.3.x/examples/arbitrarygenerator#add-constructorproperties-value-without-corresponding-field" >}})

```java
public class Product{ 
    private Long id;
    private String name;
    
    @ConstructorProperties({"id", "name"}) 
    public Person(Long id, String name) { 
        this.id = id;
        this.name = name; 
    }
}
```

## BuilderArbitraryGenerator

- Instantiating by [Lombok](https://projectlombok.org/) 's [@Builder](https://projectlombok.org/features/Builder)
- If you redefined `@Builder`'s `builderMethodName`, `buildMethodName`, you should redefine `builderMethodName`
  , `buildMethodName` in `BuilderArbitraryGenerator`
- Could generate immutable instance

```java

@Builder
public class Person {
	private Long id;
	private String name;
}

@Builder(builderMethodName = "builder2", buildMethodName = "build2")
public class Order {
	private Long id;
	private String name;
}
```

## FieldReflectionArbitraryGenerator

- Instantiating by `Field Reflection`
- `package-public` `NoArgsConstructor` is required, can not generate `final`, `transient` fields. (like `JPA` '
  s `@Entity`)

```java

@Entity
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
public class Person {
	@Id
	private Long id;
	private String name;
}
```

## JacksonArbitraryGenerator
- [Fixture Monkey Jackson module]({{< relref "/docs/v0.3.x/third-party modules/fixturemonkeyjackson" >}})

## NullArbitraryGenerator
- Always generate `null`

## How to Add New ArbitraryGenerator

[Example]({{< relref "/docs/v0.3.x/examples/arbitrarygenerator" >}})
