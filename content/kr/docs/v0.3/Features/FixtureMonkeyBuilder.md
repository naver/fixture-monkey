---
title: "FixtureMonkeyBuilder"
linkTitle: "FixtureMonkeyBuilder"
weight: 1
---

```java
FixtureMonkey fixture = FixtureMonkey.builder()
        .defaultGenerator(BuilderArbitraryGenerator.INSTANCE)    // default: BeanArbitraryGenerator.INSTANCE
        .putGenerator(CustomObject.class, FieldReflectionArbitraryGenerator.INSTANCE)
        .defaultContainerMaxSize(5)    // default: 3
        .nullInject(0.5)    // default: 0.2
        .nullableContainer(true)    // default: false
        .nullableContainerElement(true)    // default: false
        .nullGenerateTypes(Set.of(UnsupportedType.class, XXX.class))
        .exceptGeneratePackages(Set.of("com.naver.xxx", "com.naver.yyy"))    // default: "java.lang.reflect", "jdk.internal.reflect", "sun.reflect"
        .addCustomizer(CustomObject.class, new CustomObjectFixtureCustomizer())
        .fixtureValidator(new CustomFixtureValidator())    // default: CompositeFixtureValidator
        .addAnnotatedArbitraryGenerator(Tuple.class, TupleAnnotatedArbitraryGenerator())
        .defaultInterfaceSupplier(type -> null)
        .addInterfaceSupplier(SomeInterface.class, type -> ConcreateClass.INSTANCE)
        .defaultNotNull(true) // default: true
        .register(Order.class, defaultOrderBuilder())
        .registerGroup(FixtureMonkeyGroup.class)
        .build();

```


## Options
- defaultGenerator (default: BeanArbitraryGenerator.INSTANCE)
    - [ArbitraryGenerator]({{< relref "/docs/v0.3/features/arbitrarygenerator" >}})
- putGenerator
    - 주어진 타입은 `defaultGenerator`가 아닌 입력한 generator로 생성합니다. 
    - [예제]({{< relref "/docs/v0.3/examples/arbitrarygenerator" >}})
- nullInject (default: 0.2)
    - 생성한 값이 `null`이 될 수 있는 확률을 설정합니다.
    - 필드에 `@NotNull`을 정의한 경우 `null`을 생성하지 않습니다.
- nullableContainer (default: false)
    - `Map`, `List`, `Set`, `Stream`, `Optional`와 같은 컨테이너를 생성할 때 빈 컨테이너가 아닌 `null`을 생성할지 여부를 결정합니다.  
    - 만약 `false`라면 null은 생성하지 않고 빈 컨테이너가 생성됩니다. (`new ArrayList()`)
    - 만약 `true`라면 null이 생성될 수 있다. `null`이 될 확률은 **nullInject**가 결정한다.
    - 필드에 `@NotNull`을 정의한 경우 `null`이 되지 않는다.
- exceptGeneratePackages
    - 항상 `null`을 생성할 패키지를 설정한다.
    - 기본적으로 지정돼있는 패키지는 아래와 같다. 설정하더라도 아래 기본 패키지는 유지한 상태에서 추가한다. 
      > "java.lang.reflect"
      "jdk.internal.reflect"
      "sun.reflect"
      "com.naver.denma.domain.entity.AggregateMetaModel"
- addExceptGeneratePackage
    - 항상 `null`을 생성할 패키지를 추가한다.
    - exceptGeneratePackages에서 설정한 패키지에 추가한다. 설정하지 않은 경우 기본 패키지에 추가한다.
- customizers
    - 하나 이상의 [ArbitraryCustomizer]({{< relref "/docs/v0.3/features/arbitrarycustomizer" >}})를 가지는 ArbitraryCustomizers를 설정합니다.
- addCustomizer
    - 특정 타입에 [ArbitraryCustomizer]({{< relref "/docs/v0.3/features/arbitrarycustomizer" >}})를 설정합니다.
    - customizers에 설정한 ArbitraryCustomizers에 추가합니다.
- [arbitraryValidator]({{< relref "/docs/v0.3/features/arbitraryvalidator" >}}) (default: CompositeArbitraryValidator)
- addAnnotatedArbitraryGenerator
    - 특정 [타입]({{< relref "/docs/v0.3/features/defaultsupportedtypes" >}})을 생성하는 방법을 설정합니다.
    - 기본으로 정의돼있는 AnnotatedArbitraryGenerator에 추가 됩니다. 타입에 이미 정의한 AnnotatedArbitraryGenerator가 존재하는 경우 덮어 씌웁니다.  
    - [Example]({{< relref "/docs/v0.3/examples/annotatedarbitrarygenerator" >}})
- defaultInterfaceSupplier
  - 기본 [InterfaceSupplier]({{< relref "/docs/v0.3/features/interfacesupplier" >}})를 설정합니다. (default : `type -> null`) 
- addInterfaceSupplier
  - 특정 `인터페이스`와 `추상 클래스`의 구현체를 설정합니다.
  - 자세한 내용은 [here]({{< relref "/docs/v0.3/features/interfacesupplier" >}})를 확인해주세요.
- defaultNotNull
  - 만약 `true`인 경우 `@Nullable`이 없는 필드는 항상 notNull이다.
  - 만약 `false`인 경우 `@NotNull`이 없는 필드는 항상 `nullInject`에서 설정한 확률에 따라 null이 된다.
- register
  - 타입에 기본으로 사용할 ArbitraryBuilder를 등록한다.
  - 자세한 내용은 [여기]({{< relref "/docs/v0.3/features/registerarbitrarybuilder" >}})를 확인해주세요.
- registerGroup
  - ArbitraryBuilder를 여러 개 등록할 때 사용한다.
  - 메소드 시그니처가 `ArbitraryBuilder<TYPE> NAME(FixtureMonkey fixture)`
  - 자세한 내용은 [여기]({{< relref "/docs/v0.3/features/registerarbitrarybuilder" >}})를 확인해주세요.
