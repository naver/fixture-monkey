---
title: "개요"
sidebar_position: 11
---


## Fixture Monkey

Fixture Monkey는 테스트 객체를 쉽게 생성하고 조작할 수 있도록 고안된 Java 및 Kotlin 라이브러리입니다.

이 라이브러리는 테스트 작성을 간편하게 하기 위해 필요한 테스트 픽스처를 손쉽게 생성하는 데 중점을 두고 있습니다.
기본적이거나 복잡한 테스트 픽스처를 다루고 있더라도, Fixture Monkey는 필요한 테스트 객체를 쉽게 생성하고 원하는 구성에 맞게 손쉽게 수정할 수 있도록 도와줍니다.

Fixture Monkey를 활용하여 JVM 테스트를 간결하고 안전하게 수행하세요.

---------

## Fixture Monkey를 왜 사용해야 하나요?
### 1. 간결함
```java
Product actual = fixtureMonkey.giveMeOne(Product.class);
```
Fixture Monkey를 사용하면 테스트 객체 생성이 놀랍게 간단해집니다. 한 줄의 코드로 어떤 종류의 테스트 객체든 손쉽게 생성할 수 있습니다.
이는 테스트의 준비 단계를 간소화하여 테스트를 빠르고 쉽게 작성할 수 있도록 해줍니다. 뿐만 아니라, 프로덕션 코드나 테스트 환경을 변경할 필요도 없습니다.

### 2. 재사용성
```java
ArbitraryBuilder<Product> actual = fixtureMonkey.giveMeBuilder(Product.class)
    .set("id", 1000L)
    .set("productName", "Book");
```
Fixture Monkey를 활용하면 여러 테스트에서 인스턴스 명세를 재사용할 수 있어 시간과 노력을 절약할 수 있습니다.
복잡한 명세는 빌더에서 한 번 정의된 후, 이후에 해당 인스턴스를 얻기 위해 재사용될 수 있습니다.

더불어, 재사용성을 높이는 추가 기능들이 있습니다. 이러한 기능에 대한 자세한 내용은 ['기본 ArbitraryBuilder 등록'](../fixture-monkey-options/customization-options#특정-타입에-기본-arbitrarybuilder-등록하기) 및 ['InnerSpec'](../customizing-objects/innerspec) 섹션을 참조하세요.

### 3. 랜덤성
```java
ArbitraryBuilder<Product> actual = fixtureMonkey.giveMeBuilder(Product.class);

then(actual.sample()).isNotEqualTo(actual.sample());
```
Fixture Monkey는 무작위 값으로 테스트 객체를 생성하여 테스트를 보다 동적으로 만들어줍니다.
이로써 정적 데이터를 사용할 때 감춰진 엣지 케이스를 발견할 수 있습니다.

### 4. 다용도성
```java
// 상속
class Foo {
  String foo;
}

class Bar extends Foo {
    String bar;
}

Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);
Bar bar = FixtureMonkey.create().giveMeOne(Bar.class);

// 순환 참조
class Foo {
    String value;

    Foo foo;
}

Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);

// 익명 객체
interface Foo {
    Bar getBar();
}

class Bar {
    String value;
}

Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);
```

Fixture Monkey는 상상할 수 있는 모든 종류의 객체를 생성할 수 있습니다. 리스트, 중첩된 컬렉션, 열거형 및 제네릭 타입과 같은 기본 객체의 생성이 가능합니다.
뿐만 아니라 상속 관계, 순환 참조 객체, 인터페이스를 구현하는 익명 객체와 같은 더 복잡한 시나리오도 처리할 수 있습니다.

---------

## 검증된 효과
Fixture Monkey는 [Naver](https://www.navercorp.com/) 내부 라이브러리로 처음 개발되었으며 Plasma 프로젝트에서 테스트 객체 생성을 간소화하는 데 핵심적인 역할을 했습니다.
Plasma 프로젝트는 대한민국에서 가장 많이 사용되는 모바일 결제 서비스인 Naver Pay의 아키텍처를 개선하기 위한 프로젝트입니다.

이 프로젝트는 복잡한 비즈니스 요구사항에 대한 철저한 테스트가 필요했으며, Fixture Monkey의 지원을 받아 팀에서 10,000개가 넘는 테스트를 효율적으로 작성하여 중요한 엣지 케이스를 찾아내고 시스템의 신뢰성을 보장했습니다.
Fixture Monkey는 현재 오픈 소스 라이브러리로 제공되어 전 세계의 개발자들이 Fixture Monkey를 활용하여 테스트 코드를 간소화하고 자신감을 가지고 견고한 애플리케이션을 구축할 수 있습니다.


