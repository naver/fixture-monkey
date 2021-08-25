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
  - Example [here]({{< relref "/docs/examples/arbitrarygenerator#add-constructorproperties-value-when-field-name-is-different" >}})
- `ArbitraryCustomizer` could add extra parameter in `@ConstructorProperties` which is not in constructor
  - Example [here]({{< relref "/docs/examples/arbitrarygenerator#add-constructorproperties-value-without-corresponding-field" >}})

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

- Instantiating by [Jackson](https://github.com/FasterXML/jackson) Map Deserializer
- Does not generate `@JsonIgnore` field
- `@JsonProperty` would change field name, when apply manipulator `Expression` is same as `@JsonProperty` value
  - Example [here]({{< relref "/docs/examples/arbitrarygenerator#set-field-in-jacksonarbitrarygenerator-with-jsonproperty" >}})
- Could inject `ObjectMapper` in `JacksonArbitraryGenerator`
- Could not generate `interface`, you should set default implementation by [InterfaceSupplier]({{< relref "/docs/features/interfacesupplier" >}})

```java

@Data
public class Product {
	private Long id;
	private String name;
}

	ObjectMapper objectMapper = ...;
	JacksonArbitraryGenerator jacksonArbitraryGenerator=new JacksonArbitraryGenerator(objectMapper);

```

## NullArbitraryGenerator
- Always generate `null`

## How to Add New ArbitraryGenerator

[Example]({{< relref "/docs/examples/arbitrarygenerator" >}})
