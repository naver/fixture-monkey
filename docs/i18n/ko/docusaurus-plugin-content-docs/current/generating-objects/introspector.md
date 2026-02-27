---
title: "인트로스펙터"
sidebar_position: 35
---


## 인트로스펙터란 무엇인가요?

Fixture Monkey의 `인트로스펙터(Introspector)`는 테스트 객체를 생성하는 방법을 결정하는 도구입니다. 테스트 객체를 생성하는 "공장"이라고 생각하면 됩니다.

인트로스펙터는 다음과 같은 역할을 합니다:
- 생성자나 빌더 중 어떤 방식으로 객체를 생성할지 결정
- 필드 값을 어떻게 설정할지 결정
- 코드베이스의 다양한 클래스 타입을 어떻게 처리할지 결정

## 빠른 시작: 대부분의 프로젝트에 권장되는 설정

Fixture Monkey를 처음 사용하고 빠르게 시작하고 싶다면, 다음과 같은 설정을 사용하세요. 이 설정은 대부분의 프로젝트에서 잘 작동합니다:

```java
// 대부분의 클래스 타입을 처리할 수 있는 권장 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(new FailoverIntrospector(
        Arrays.asList(
            ConstructorPropertiesArbitraryIntrospector.INSTANCE,
            BuilderArbitraryIntrospector.INSTANCE,
            FieldReflectionArbitraryIntrospector.INSTANCE,
            BeanArbitraryIntrospector.INSTANCE
        ),
        false // 더 깔끔한 테스트 출력을 위해 로깅 비활성화
    ))
    .build();

// 테스트에서 사용하기
@Test
void 테스트_예제() {
    // 테스트 객체 생성
    MyClass myObject = fixtureMonkey.giveMeOne(MyClass.class);
    
    // 생성된 객체를 테스트에 사용
    assertThat(myObject).isNotNull();
    // 더 많은 검증...
}
```

이 설정은 여러 전략을 조합하여 다양한 클래스 타입을 처리하므로, 대부분의 실제 프로젝트에서 추가 설정 없이 잘 작동합니다.

## 가장 간단한 접근법 (기본 설정만 원할 경우)

가장 간단하게 설정하려면 다음과 같이 기본 설정을 사용할 수 있습니다:

```java
// 기본 설정으로 가장 간단한 접근법
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .build();

// 테스트 객체 생성
MyClass myObject = fixtureMonkey.giveMeOne(MyClass.class);
```

그러나 이 기본 접근법은 기본 생성자와 setter 메서드가 있는 간단한 JavaBean 클래스에서만 잘 작동합니다.

## 클래스에 맞는 인트로스펙터 선택하기

다양한 클래스 타입에 따라 객체 생성 방식이 달라야 합니다. 다음은 선택에 도움이 되는 간단한 가이드입니다:

| 클래스 타입 | 권장 인트로스펙터 | 예시 |
|------------|--------------------------|---------|
| **Setter가 있는 클래스 (JavaBeans)** | `BeanArbitraryIntrospector` | getter/setter가 있는 클래스 |
| **생성자가 있는 불변 클래스** | `ConstructorPropertiesArbitraryIntrospector` | 레코드, 어노테이션된 생성자가 있는 클래스 |
| **혼합 필드 접근 방식의 클래스** | `FieldReflectionArbitraryIntrospector` | public 필드, 기본 생성자가 있는 클래스 |
| **빌더 패턴을 사용하는 클래스** | `BuilderArbitraryIntrospector` | `.builder()` 메서드가 있는 클래스 |
| **다양한 패턴이 섞인 코드베이스** | `FailoverArbitraryIntrospector` | 다양한 클래스 타입이 있는 프로젝트 |

## 일반적인 클래스 타입 예제

### 예제 1: 표준 JavaBean 클래스 (getter/setter 있음)

```java
// 클래스 정의
public class Customer {
    private String name;
    private int age;
    
    // 기본 생성자
    public Customer() {}
    
    // Setter 메서드
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    
    // Getter 메서드
    public String getName() { return name; }
    public int getAge() { return age; }
}

// 테스트 코드
@Test
void customer_테스트() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE) // 기본값이므로 선택사항
        .build();
    
    Customer customer = fixtureMonkey.giveMeOne(Customer.class);
    
    assertThat(customer.getName()).isNotNull();
    assertThat(customer.getAge()).isGreaterThanOrEqualTo(0);
}
```

### 예제 2: 생성자가 있는 불변 클래스

```java
// 클래스 정의 (@ConstructorProperties 포함)
public class Product {
    private final String name;
    private final double price;
    
    @ConstructorProperties({"name", "price"})
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
}

// 테스트 코드
@Test
void product_테스트() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();
    
    Product product = fixtureMonkey.giveMeOne(Product.class);
    
    assertThat(product.getName()).isNotNull();
    assertThat(product.getPrice()).isGreaterThanOrEqualTo(0.0);
}

// Java 레코드에서도 잘 작동합니다
public record OrderItem(String productId, int quantity, double price) {}
```

### 예제 3: 빌더 패턴을 사용하는 클래스

```java
// 빌더가 있는 클래스 정의
public class User {
    private final String username;
    private final String email;
    
    private User(Builder builder) {
        this.username = builder.username;
        this.email = builder.email;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String username;
        private String email;
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public User build() {
            return new User(this);
        }
    }
    
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}

// 테스트 코드
@Test
void user_테스트() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
        .build();
    
    User user = fixtureMonkey.giveMeOne(User.class);
    
    assertThat(user.getUsername()).isNotNull();
    assertThat(user.getEmail()).isNotNull();
}
```

## 인트로스펙터가 중요한 이유

다양한 프로젝트는 객체 생성을 위한 다양한 패턴을 사용합니다:

- 일부는 getter/setter가 있는 간단한 클래스를 사용
- 일부는 생성자를 사용하는 불변 객체를 사용
- 일부는 빌더 패턴을 따름
- Lombok과 같은 프레임워크는 특정 방식으로 코드를 생성

적절한 인트로스펙터를 선택하면 기존 코드를 수정하지 않고도 Fixture Monkey를 활용할 수 있어 시간과 노력을 절약할 수 있습니다.

## 자주 묻는 질문 (FAQ)

### Q: 어떤 인트로스펙터를 사용해야 할지 모르겠어요. 어떻게 해야 하나요?
**A**: 권장 설정(여러 인트로스펙터를 포함한 `FailoverIntrospector` 사용)으로 시작하세요. 대부분의 프로젝트에서 잘 작동하며 자동으로 다양한 전략을 시도합니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(new FailoverIntrospector(
        Arrays.asList(
            ConstructorPropertiesArbitraryIntrospector.INSTANCE,
            BuilderArbitraryIntrospector.INSTANCE,
            FieldReflectionArbitraryIntrospector.INSTANCE,
            BeanArbitraryIntrospector.INSTANCE
        ),
        false // 더 깔끔한 테스트 출력을 위해 로깅 비활성화
    ))
    .build();
```

### Q: 객체가 생성되지 않아요. 무엇을 확인해야 하나요?
**A**: 클래스가 다음 중 하나를 가지고 있는지 확인하세요:
- 기본 생성자와 setter (`BeanArbitraryIntrospector`용)
- `@ConstructorProperties`가 있는 생성자 (`ConstructorPropertiesArbitraryIntrospector`용)
- 빌더 메서드 (`BuilderArbitraryIntrospector`용)

### Q: Lombok을 사용 중인데 객체가 제대로 생성되지 않아요. 어떻게 해야 하나요?
**A**: lombok.config 파일에 `lombok.anyConstructor.addConstructorProperties=true`를 추가하고 `ConstructorPropertiesArbitraryIntrospector`를 사용하세요.

### Q: 특정 클래스에 대해 사용자 정의 생성 로직이 필요하면 어떻게 하나요?
**A**: 특정 케이스의 경우 `instantiate` 메서드를 사용하여 인스턴스 생성 방법을 지정할 수 있습니다:

```java
MySpecialClass object = fixtureMonkey.giveMeBuilder(MySpecialClass.class)
    .instantiate(() -> new MySpecialClass(specialParam1, specialParam2))
    .sample();
```

더 고급 사용자 정의 로직은 [사용자 정의 인트로스펙터](./custom-introspector) 가이드를 참조하세요. 그러나 대부분의 사용자는 이 기능이 필요하지 않을 것입니다.

## 사용 가능한 인트로스펙터 (상세 정보)

### BeanArbitraryIntrospector (기본값)
적합한 대상: setter가 있는 표준 JavaBean 클래스

요구사항:
- 클래스에 기본 생성자가 있어야 함
- 속성에 대한 setter 메서드가 있어야 함

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(BeanArbitraryIntrospector.INSTANCE) // 이것이 기본값입니다
    .build();
```

### ConstructorPropertiesArbitraryIntrospector
적합한 대상: 생성자가 있는 불변 객체

요구사항:
- 클래스에 `@ConstructorProperties`가 있는 생성자가 있거나 레코드 타입이어야 함
- Lombok의 경우 lombok.config에 `lombok.anyConstructor.addConstructorProperties=true` 추가

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
```

### FieldReflectionArbitraryIntrospector
적합한 대상: 필드 접근 방식의 클래스

요구사항:
- 클래스에 기본 생성자가 있어야 함
- 필드는 리플렉션을 통해 접근 가능해야 함

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
    .build();
```

### BuilderArbitraryIntrospector
적합한 대상: 빌더 패턴을 사용하는 클래스

요구사항:
- 클래스에 설정 메서드와 build 메서드가 있는 빌더가 있어야 함

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
    .build();
```

### FailoverArbitraryIntrospector (혼합 코드베이스에 권장)
적합한 대상: 다양한 클래스 타입이 섞인 프로젝트

장점:
- 여러 인트로스펙터를 순차적으로 시도
- 다양한 클래스 패턴에 작동
- 가장 다용도로 사용 가능한 옵션

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(new FailoverIntrospector(
        Arrays.asList(
            ConstructorPropertiesArbitraryIntrospector.INSTANCE,
            BuilderArbitraryIntrospector.INSTANCE,
            FieldReflectionArbitraryIntrospector.INSTANCE,
            BeanArbitraryIntrospector.INSTANCE
        ),
        false // 더 깔끔한 테스트 출력을 위해 로깅 비활성화
    ))
    .build();
```

실패 로그를 비활성화하려면 위와 같이 생성자 인수 `enableLoggingFail`을 false로 설정하세요.

:::warning
성능 참고: `FailoverArbitraryIntrospector`는 각 등록된 인트로스펙터를 순차적으로 사용해 객체 생성을 시도하므로 생성 비용이 증가할 수 있습니다. 성능이 중요한 경우 클래스 패턴을 알고 있다면 특정 인트로스펙터를 사용하세요.
:::

### PriorityConstructorArbitraryIntrospector
적합한 대상: 다른 인트로스펙터가 작동하지 않는 특별한 경우

장점:
- `@ConstructorProperties` 없이도 사용 가능한 생성자 사용
- 수정할 수 없는 라이브러리 클래스에 유용

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(PriorityConstructorArbitraryIntrospector.INSTANCE)
    .build();
```

## 플러그인의 추가 인트로스펙터

플러그인은 특정 필요에 맞는 추가 인트로스펙터를 제공합니다:
- [`JacksonObjectArbitraryIntrospector`](../plugins/jackson-plugin/jackson-object-arbitrary-introspector): Jackson JSON 객체용
- [`PrimaryConstructorArbitraryIntrospector`](../plugins/kotlin-plugin/introspectors-for-kotlin): Kotlin 클래스용

## 인트로스펙터 작동 방식 (기술적 세부 사항)

```mermaid
graph TD
    A[객체 생성 요청] --> B{인트로스펙터 선택}
    B -- BeanArbitraryIntrospector --> C[기본 생성자 + Setter 사용]
    B -- ConstructorProperties --> D[어노테이션된 생성자 사용]
    B -- FieldReflection --> E[리플렉션으로 필드 설정]
    B -- Builder --> F[빌더 패턴 사용]
    B -- Failover --> G[여러 인트로스펙터 순차 시도]
    C --> H[객체 인스턴스]
    D --> H
    E --> H
    F --> H
    G --> H
```

## 더 고급 사용자 정의가 필요하신가요?

내장 인트로스펙터로 해결되지 않는 특별한 객체 생성 요구 사항이 있다면 사용자 정의 인트로스펙터를 만들어야 할 수도 있습니다.

이는 고급 주제이며 대부분의 사용자에게는 필요하지 않습니다. 관심이 있으시다면 [사용자 정의 인트로스펙터](./custom-introspector) 가이드를 참조하세요.

