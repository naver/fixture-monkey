---
title: "FixtureMonkey"
weight: 1
---

## 생성 방법
### 기본 옵션을 사용하는 FixtureMonkey 생성
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.create();
```

### 옵션을 추가하여 생성하는 FixtureMonkey 생성
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	+ 옵션들...
    .build();
```

## 옵션
### 옵션 변경
| 옵션                               | 설명                                                         | 기본 옵션                                                                  | 링크                                                                                    |
|----------------------------------|------------------------------------------------------------|------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| arbitraryValidator               | `arbitraryValidator`는 ArbitraryBuilder에서 sample한 값을 검증합니다. | new DefaultArbitraryValidator()                                        | [ArbitraryValidator]({{< relref "/docs/v0.4/components/arbitraryvalidator" >}})       |
| defaultArbitraryContainerInfo    | 임의로 생성하는 컨테이너 타입의 크기를 설정합니다.                               | new ArbitraryContainerInfo(0, defaultArbitraryContainerMaxSize, false) |                                                                                       |
| defaultArbitraryContainerMaxSize | 임의로 생성하는 컨테이너 타입의 최대 크기를 설정합니다.                            | 3                                                                      |                                                                                       |
| defaultNullInjectGenerator       | 기본적으로 null을 생성하는 방법을 설정합니다.                                | new DefaultNullInjectGenerator(...)                                    |                                                                                       |
| nullableContainer                | 컨테이너 타입을 생성할 때 null을 기본적으로 생성하도록 설정합니다.                    | false                                                                  |                                                                                       |
| nullableElement                  | 컨테이너 타입의 자식 요소에서 null을 생성하도록 설정합니다.                        | false                                                                  |                                                                                       |
| defaultNotNull                   | nullable한 타입이 항상 null을 생성하지 않도록 설정합니다.                     | false                                                                  |                                                                                       |
| defaultObjectPropertyGenerator   | `ObjectProperty`를 생성하는 방법을 설정합니다.                          | DEFAULT_OBJECT_PROPERTY_GENERATOR                                      | [ObjectProperty]({{< relref "/docs/v0.4/components/objectproperty" >}})               |
| defaultPropertyGenerator         | 자식 Property를 생성하는 방법을 설정합니다.                               | new DefaultPropertyGenerator()                                         |                                                                                       |
| defaultPropertyNameResolver      | Property에서 이름을 반환하는 방법을 설정합니다.                             | DEFAULT_PROPERTY_NAME_RESOLVER                                         |                                                                                       |
| javaArbitraryResolver            | 자바 기본 클래스가 어노테이션 적용하는 방법을 설정합니다.                           | new JavaArbitraryResolver() {}                                         |                                                                                       |
| javaTimeArbitraryResolver        | 자바 시간/날짜 클래스가 어노테이션 적용하는 방법을 설정합니다.                        | new JavaTimeArbitraryResolver() {}                                     |                                                                                       |
| javaTypeArbitraryGenerator       | 자바 기본 클래스를 생성할 때 생성하는 기본 값을 설정합니다.                         | new JavaTypeArbitraryGenerator() {}                                    |                                                                                       |
| javaTimeTypeArbitraryGenerator   | 자바 시간/날짜 클래스를 생성할 때 생성하는 기본 값을 설정합니다.                      | new JavaTimeTypeArbitraryGenerator() {}                                |                                                                                       |
| manipulatorOptimizer             | 연산들을 최적화하는 방법을 설정합니다.                                      | new NoneManipulatorOptimizer()                                         |                                                                                       |
| monkeyExpressionFactory          | 사용자가 정의한 표현식을 설정합니다.                                       | new ArbitraryExpressionFactory()                                       |                                                                                       |
| objectIntrospector               | 객체를 생성하는 방법을 설정합니다.                                        | BeanArbitraryIntrospector.INSTANCE                                     | [ArbitraryIntrospector]({{< relref "/docs/v0.4/components/arbitraryintrospector" >}}) |                                                                               |
| useExpressionStrictMode          | 유효하지 않은 표현식을 입력했을 때 예외를 발생시킬지 여부를 설정합니다.                   | false                                                                  |                                                                                       |

### 옵션 추가
| 옵션                                  | 설명                                                                    | 링크                                                                                    |
|-------------------------------------|-----------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| addContainerType                    | 사용자 정의 컨테이너 타입을 추가합니다.                                                | [ArbitraryIntrospector]({{< relref "/docs/v0.4/components/arbitraryintrospector" >}}) |
| addExceptGenerateClass              | 생성하지 않을 클래스를 추가합니다.                                                   |                                                                                       |
| addExceptGenerateClasses            | 생성하지 않을 클래스 여러 개를 추가합니다.                                              |                                                                                       |
| addExceptGeneratePackage            | 생성하지 않을 패키지를 추가합니다. 입력한 패키지의 하위 패키지도 생성하지 않습니다.                       |                                                                                       |
| addExceptGeneratePackages           | 생성하지 않을 패키지 여러 개를 추가합니다. 입력한 패키지의 하위 패키지도 생성하지 않습니다.                  |                                                                                       |
| pushExceptGenerateType              | 생성하지 않을 Property를 추가합니다.                                              |                                                                                       |
| pushContainerIntrospector           | 입력한 컨테이너 타입 객체를 생성하는 방법을 설정합니다.                                       | [ArbitraryIntrospector]({{< relref "/docs/v0.4/components/arbitraryintrospector" >}}) |
| plugin                              | 새로운 플러그인을 추가합니다.                                                      |                                                                                       |
| pushArbitraryContainerInfoGenerator | 입력한 컨테이너 타입의 크기를 설정합니다.                                               |                                                                                       |
| pushFixtureCustomizer               | 입력한 Property 객체를 생성할 때 사용하는 Property 객체와 생성한 값을 변경합니다.                | [FixtureCustomizer]({{< relref "/docs/v0.4/components/fixturecustomizer" >}})         |
| pushArbitraryIntrospector           | 입력한 Property 객체를 생성하는 방법을 설정합니다.                                      | [ArbitraryIntrospector]({{< relref "/docs/v0.4/components/arbitraryintrospector" >}}) |
| pushObjectPropertyGenerator         | 입력한 Property에서 `ObjectProperty`를 생성하는 방법을 설정합니다.                      | [ObjectProperty]({{< relref "/docs/v0.4/components/objectproperty" >}})               |
| pushContainerPropertyGenerator      | 입력한 Property에서 `ContainerProperty` 를 생성하는 방법을 설정합니다.                  | [ContainerProperty]({{< relref "/docs/v0.4/components/containerproperty" >}})         |
| pushPropertyGenerator               | 입력한 Property에서 자식 Property를 생성하는 방법을 설정합니다.                           |                                                                                       |
| pushPropertyNameResolver            | 입력한 Property에서 이름을 반환하는 방법을 설정합니다.                                    |                                                                                       |
| pushNullInjectGenerator             | 입력한 Property에서 null을 생성하는 방법을 설정합니다.                                  |                                                                                       |
| register                            | 입력한 Property에서 기본으로 반환할 `ArbitraryBuilder`을 설정합니다.                    |                                                                                       |
| registerGroup                       | 입력한 그룹 클래스에 존재하는 모든 `ArbitraryBuilder`를 기본 `ArbitraryBuilder`로 설정합니다. |        
