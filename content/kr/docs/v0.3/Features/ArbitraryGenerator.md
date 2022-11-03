---
title: "ArbitraryGenerator"
linkTitle: "ArbitraryGenerator"
weight: 6
---
`ArbitraryGenerator`는 객체를 생성하는 역할을 가지고 있습니다. 객체 생성 조건을 만족해야 생성할 수 있습니다.

## BeanArbitraryGenerator (default)

- JavaBeans 스펙에 따라 객체를 생성합니다.
- `NoArgsConstructor` 와 `Setter`가 있어야 합니다.

```java

@Setter
@NoArgsConstructor
public class Person {
	private Long id;
	private String name;
}
```

## ConstructorPropertiesArbitraryGenerator
- 하나의 생성자와 `@ConstructorProperties` 가 필요합니다.
- 생성자에 존재하는 모든 파라미터는 `@ConstructorProperties`에 있어야 합니다. 같은 이름을 가지고 있어야 합니다.
- 필드 이름은 생성자 이름과 일치해야 합니다. 
- 필드 이름과 생성자 이름이 다르지만 코드 베이스에서 수정할 수 없는 경우 `ArbitraryCustomizer`에서 수정할 수 있습니다. [예제]({{< relref "/docs/v0.3/examples/arbitrarygenerator#add-constructorproperties-value-when-field-name-is-different" >}})
- 생성자에 존재하지 않고 `@ConstructorProperties` 에서 새로 정의한 파라미터는 `ArbitraryCustomizer`에서 활용할 수 있습니다. [예제]({{< relref "/docs/v0.3/examples/arbitrarygenerator#add-constructorproperties-value-without-corresponding-field" >}})

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

- [Lombok](https://projectlombok.org/) 's [@Builder](https://projectlombok.org/features/Builder) 가 선언된 경우 생성할 수 있습니다.
- `@Builder`의 `builderMethodName`를 재정의한 경우 `BuilderArbitraryGenerator` 안에 있는 `builderMethodName`를 일치시켜 주어야 합니다. 
- 불변 객체를 생성할 수 있습니다.

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

- `Field Reflection`을 통해 객체를 생성합니다.
- `package-public` `NoArgsConstructor` 이 필요합니다. `final`, `transient`는 생성할 수 없습니다. (`JPA의 @Entity`와 유사합니다)

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
- [Fixture Monkey Jackson module]({{< relref "/docs/v0.3/third-party modules/fixturemonkeyjackson" >}})

## NullArbitraryGenerator
- 항상 `null`을 반환합니다.

## 새로운 ArbitraryGenerator 추가하기

[예제]({{< relref "/docs/v0.3/examples/arbitrarygenerator" >}})
