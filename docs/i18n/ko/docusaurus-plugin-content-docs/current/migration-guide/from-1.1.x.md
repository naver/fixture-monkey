---
title: "1.1.x"
sidebar_position: 111
---

# 1.1.x에서 1.2.x로 마이그레이션하기

1.2.0에서 레거시 object tree 엔진(0.4.0부터 기본이었던)이 **제거**되고, 1.1.17에서 실험적으로 도입되었던 adapter 엔진이 **유일한 기본** 객체 생성 엔진이 되었습니다. `FixtureMonkeyBuilder`가 자동으로 연결합니다.

## 무언가 바꿔야 하나요?

**대부분의 사용자는 그렇지 않습니다.** `FixtureMonkey.builder()`, `giveMe*`, `.set()` / `.size()` / `.thenApply()`를 사용한다면 코드는 그대로 동작합니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder().build(); // adapter 엔진 자동 적용
```

다음에 해당할 때만 수정이 필요합니다:
- `JavaNodeTreeAdapterPlugin` / `KotlinNodeTreeAdapterPlugin`을 명시적으로 등록한 경우,
- tracer를 설정한 경우,
- 커스텀 `Property`, `PropertyGenerator`, `ArbitraryIntrospector`, `object-farm-api` 타입을 구현한 경우.

## adapter 플러그인 제거

### 무엇이 바뀌었나
- **이전 (1.1.x)**: node tree adapter는 opt-in 플러그인이었습니다.
- **현재 (1.2.x)**: 기본 엔진이 되었고, 플러그인 클래스는 제거되었습니다.

### 해야 할 일
플러그인 등록을 삭제하세요. 대체할 것은 없습니다.

```java
// 이전 (1.1.x)
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JavaNodeTreeAdapterPlugin())
    .build();

// 현재 (1.2.x)
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .build();
```

Kotlin에서는 `KotlinNodeTreeAdapterPlugin()`을 제거하세요(`KotlinPlugin()`은 유지).

## 트레이싱이 빌더로 이동

### 무엇이 바뀌었나
- **이전 (1.1.x)**: `AdapterTracer`를 플러그인에 설정했습니다.
- **현재 (1.2.x)**: `FixtureMonkeyBuilder#tracer(...)`로 빌더에 `AssemblyTracer`를 설정합니다.

### 해야 할 일
```java
// 이전 (1.1.x)
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JavaNodeTreeAdapterPlugin()
        .tracer(AdapterTracer.console()))
    .build();

// 현재 (1.2.x)
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .tracer(AssemblyTracer.console())
    .build();
```

`FixtureMonkeyOptionsBuilder#adapterTracer(...)` / `#nodeTreeAdapter(...)`는 제거되었습니다. 전체 tracer 레퍼런스는 [트레이싱 가이드](../debugging/tracing)를 참고하세요.

## seed 동작

### 무엇이 바뀌었나
- `@Seed`가 `FixtureMonkeyBuilder#seed(long)`보다 우선합니다. `@Seed`(또는 `FixtureMonkeySeedExtension`)가 활성인 동안에는 빌더의 seed가 무시됩니다.
- seed는 테스트 범위로 스코프되고 이후 해제됩니다. 테스트나 다른 extension이 예외를 던져도 해제되어 이후 테스트로 누수되지 않습니다.
- 같은 seed로 재실행 시 컨테이너 크기까지 재현됩니다.

### 해야 할 일
바꿀 것은 없지만, iteration 순서와 seed 처리가 바뀌었으므로 같은 seed에서도 random stream이 1.1.x와 달라질 수 있습니다. 특정 seed 값을 고정한 테스트는 재기준화(re-baseline)가 필요할 수 있습니다. [재현 가능한 생성](../debugging/reproducible-generation)을 참고하세요.

## 커스텀 확장 지점

`Property`(또는 커스텀 `PropertyGenerator` / `ArbitraryIntrospector`)를 구현한다면 타입 시스템 표면이 바뀌었습니다. 다음 매핑을 사용하세요:

| 이전 (1.1.x) | 현재 (1.2.x) |
|--------------|--------------|
| `Property#getType()` | `Property#getJvmType()` |
| `Property#getAnnotatedType()` | `Types.toAnnotatedType(property.getJvmType())` |
| `Property#getValue(Object)` | 제거됨 — 값 추출은 더 이상 `Property`의 일부가 아님 |
| `ArbitraryGeneratorContext#getResolvedType()` (`Type`) | `getResolvedJvmType()` (반환 타입 `Class<?>`로 변경, deprecated) |
| `ArbitraryGeneratorContext#getResolvedAnnotatedType()` | `getResolvedJvmType()` (deprecated) |
| `PropertyUtils#toProperty(AnnotatedType)` | `PropertyUtils#toProperty(JvmType)` |
| `ContainerPropertyGenerator.DEFAULT_ELEMENT_RAW_TYPE` | `DEFAULT_ELEMENT_JVM_TYPE` |
| `JavaType(...)` (object-farm-api) | `ReflectiveJvmType(...)` |
| `JvmType#getAnnotatedType()` | `getRawType()` + `getAnnotations()` |

내장 `Property` 구현체(`FieldProperty`, `ConstructorProperty`, `RootProperty`, `MapEntryElementProperty` 등)의 생성자 시그니처도 이에 맞춰 변경되었습니다.

JVM 언어용 커스텀 엔진을 설치하려면 새 `JvmTypeSystemPlugin` SPI를 사용하세요:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new MyJvmTypeSystemPlugin()) // 언어별 JVM 타입 시스템 부품 등록
    .build();
```

## 요약

- 대부분의 사용자: 변경 불필요.
- `JavaNodeTreeAdapterPlugin` / `KotlinNodeTreeAdapterPlugin` 제거 — 이제 기본 엔진.
- 트레이싱은 `FixtureMonkeyBuilder#tracer(AssemblyTracer...)`로 이동.
- 커스텀 `Property` / 플러그인 작성자: `JvmType`과 새 `JvmTypeSystemPlugin` SPI로 마이그레이션.
