---
title: "컨셉"
sidebar_position: 51
---


Fixture Monkey가 제공하는 옵션에 대해 배우기 전에 몇 가지 알아둬야 할 사항이 있습니다.

## 프로퍼티

문서에서는 클래스 객체의 특성을 나타낼 때, `필드` 대신에 일관적으로 `프로퍼티` 라는 용어를 사용합니다.
이 용어는 Kotlin의 '프로퍼티'와 동일한 이름이지만, Fixture Monkey에서는 컨셉이 다릅니다.

Fixture Monkey의 초기 구조는 주로 필드에 기반하고 있었으며, 이는 메서드와 기타 메커니즘을 통한 구성 및 제어에 제약을 가했습니다.
예를 들어, 오직 필드에만 의존할 경우 Setter 메서드에 존재하는 어노테이션에 접근할 수 없습니다.
이러한 한계점을 다루기 위해 필드를 넘어 지원을 확장하는 `프로퍼티` 인터페이스가 도입되었습니다.

Fixture Monkey의 `프로퍼티`는 클래스 내에서 기본 컴포넌트로 작동하며 `필드`, `메서드` 또는 Kotlin `프로퍼티`를 나타낼 수 있습니다.
프로퍼티는 해당 `타입`, 명시된 `어노테이션` 그리고 `이름` 에 관한 정보가 포함되어 있습니다.

Fixture Monkey에서는 `객체`와 `컨테이너`의 특성도 `프로퍼티` 컨셉을 통해 표현합니다.

### 객체 프로퍼티

`객체 프로퍼티`는 불변 객체 정보를 나타내는 프로퍼티로 다음을 포함합니다:

- **property**: 객체 자체의 프로퍼티입니다.
- **propertyNameResolver**: 프로퍼티 명이 결정되는 방식을 정의합니다.
- **nullInject**: null 값을 주입할 확률.
- **elementIndex**: 객체가 컨테이너 요소인 경우, 해당 인덱스를 나타냅니다.
- **childPropertyListsByCandidateProperty**: 후보군 프로퍼티별로 그룹화된 자식 프로퍼티 정보를 담은 맵.

```java
public final class ObjectProperty {
private final Property property;

    private final PropertyNameResolver propertyNameResolver;

    private final double nullInject;

    @Nullable
    private final Integer elementIndex;

    private final Map<Property, List<Property>> childPropertyListsByCandidateProperty;
}
```

### 컨테이너 프로퍼티

컨테이너 타입의 프로퍼티는 불변 컨테이너 정보를 나타내는 `컨테이너 프로퍼티`로 다음을 포함합니다:

- **elementProperties**: 모든 요소 프로퍼티들을 담은 리스트.
- **containerInfo**: 컨테이너의 크기를 결정하는 `ArbitraryContainerInfo`.

```java
public final class ContainerProperty {
private final List<Property> elementProperties;

    private final ArbitraryContainerInfo containerInfo;
}
```

## 옵션 특성

Fixture Monkey의 여러 옵션들은 공통적인 특성을 공유합니다.
예를 들어, `ObjectPropertyGenerator` 를 수정하는 옵션들을 살펴보겠습니다.

> `defaultObjectPropertyGenerator`, `pushObjectPropertyGenerator`, `pushAssignableTypeObjectPropertyGenerator`, `pushExactTypeObjectPropertyGenerator`

접두사 default가 붙은 옵션은 Fixture Monkey에서 생성한 모든 프로퍼티에 기본값으로 적용됩니다.
이러한 기본값은 모든 프로퍼티 타입에 전역적으로 영향을 미치는 기본 동작을 설정합니다.

그러나 특정 유형에 대해 구체적인 옵션을 적용해야 하는 경우 push로 시작하는 옵션을 사용할 수 있습니다.
이러한 push 옵션에는 세 가지 바리에이션이 있습니다.

- push~ : MatcherOperator를 매개변수로 받습니다.
- pushAssignableType~: 이 옵션은 주어진 타입을 할당할 수 있는 모든 프로퍼티 타입에 명시된 설정을 적용합니다.
  이는 해당 옵션이 정확히 지정된 타입뿐만 아니라 부모클래스나 부모인터페이스를 포함한 프로퍼티 타입에 할당할 수 있는 모든 프로퍼티에 적용됩니다.
- pushExactType~: 이 옵션은 설정을 정확하게 동일한 타입의 프로퍼티들로 제한합니다.
  이는 자식타입 혹은 부모타입으로 연관되어있는 프로퍼티들에게 영향을 미치지 않습니다.

`push` 바리에이션을 사용하여 설정한 옵션이 `기본` 옵션보다 우선한다는 점에 유의해야합니다.
이는 특정 타입에 대해 push 옵션이 정의되면 해당 타입에 해당하는 모든 `기본` 옵션이 재정의된다는 것을 의미합니다.

