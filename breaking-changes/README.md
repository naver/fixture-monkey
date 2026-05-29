# Breaking Changes — 1.1.21

브랜치 `sa/remove-node-tree`가 릴리스되는 버전 **1.1.21**부터 발생하는 사용자 영향 항목입니다. (브랜치 base: `main` @ `2ca0eff91`.)

대부분의 일반 사용자(`FixtureMonkey.builder()` + `giveMeBuilder()` + `.set(...)` / `.size(...)` / `.sample()` 패턴)는 영향을 받지 않습니다. 아래는 **확장 지점을 직접 구현**했거나 **내부 타입을 import**해서 쓰던 사용자에게 영향이 가는 것들입니다.

## 영향도 높음 — Custom Introspector 작성자

이 변경은 `ArbitraryIntrospector`(예: `BeanArbitraryIntrospector`, `ConstructorPropertiesArbitraryIntrospector` 등을 흉내 낸 사용자 구현)를 직접 작성한 모든 사람에게 영향을 줍니다. 가장 흔히 쓰는 확장 지점입니다.

### `ArbitraryGeneratorContext` 반환 타입 변경 — **1.1.21부터**

`com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext` (since `0.4.0`, MAINTAINED)

```diff
- public AnnotatedType getResolvedAnnotatedType()       // 삭제
- public Type          getResolvedType()                 // 반환 타입 변경
+ public Class<?>      getResolvedType()
+ public JvmType       getResolvedJvmType()              // 신규
```

- `getResolvedAnnotatedType()`는 삭제됐습니다. 대안: `context.getResolvedProperty().getAnnotatedType()`.
- `getResolvedType()`의 반환 타입이 `java.lang.reflect.Type` → `Class<?>`로 좁아졌습니다. `Type` 변수에 받던 코드는 컴파일 유지되지만, 제네릭/배열 등 `Class<?>`로 표현 불가한 경우를 다뤘다면 동작이 달라집니다.

### Map iteration 순서 변경 — **1.1.21부터**

`getCombinableArbitrariesByArbitraryProperty()` / `getCombinableArbitrariesByResolvedName()` / `getCombinableArbitrariesByPropertyName()` 가 `LinkedHashMap`을 반환하도록 바뀌었습니다(이전엔 `HashMap`). 선언 타입은 `Map`이라 컴파일 영향은 없지만, **iteration 순서에 의존하는 random stream**(예: 같은 seed에서 같은 결과를 기대하는 회귀 테스트)이 깨질 수 있습니다.

## 영향도 중간 — `JvmType` / `JavaType` 직접 사용자

이 변경은 object-farm-api의 타입 객체를 직접 만들거나 `getAnnotatedType()`을 호출하는 코드에 영향을 줍니다 (custom property/plugin 구현체).

### `JvmType#getAnnotatedType()` 삭제 — **1.1.21부터**

`com.navercorp.objectfarm.api.type.JvmType` 인터페이스

```diff
- @Deprecated
- default AnnotatedType getAnnotatedType()    // 삭제 (이전부터 @Deprecated)
```

대안: `jvmType.getRawType()` + `jvmType.getAnnotations()` 조합으로 대체. 이미 `@Deprecated`였으므로 미리 마이그레이션해둔 코드는 영향 없음.

### `JavaType` 생성자 변경 — **1.1.21부터**

`com.navercorp.objectfarm.api.type.JavaType`

```diff
- @Deprecated
- public JavaType(Class<?>, List<JvmType>, List<Annotation>, @Nullable AnnotatedType)
+ public JavaType(Class<?>, List<? extends JvmType>, List<Annotation>, @Nullable AnnotatedType, @Nullable Boolean nullable)
+ public JavaType(Class<?>, List<? extends JvmType>, List<Annotation>, @Nullable Boolean nullable)
- @Override public AnnotatedType getAnnotatedType()    // 삭제 (@Deprecated였음)
+ public Boolean getNullable()                          // 신규
```

- 4-arg 생성자(`@Deprecated`였음) 삭제 — `nullable` 인자를 추가해 새 5-arg 시그니처로 옮기거나, `AnnotatedType` 없이 4-arg 새 생성자를 사용해야 합니다.
- `typeVariables` 파라미터 타입이 `List<JvmType>` → `List<? extends JvmType>`로 완화됐습니다. 대부분 호환이지만 `List<? super JvmType>`를 넘기던 코드는 안 됩니다.

## 영향도 낮음 — Plugin / NodeTreeAdapter 직접 구현자

`@API(since = "1.1.17", status = EXPERIMENTAL)` 였던 어댑터 브릿지 SPI. **1.1.17 ~ 1.1.20**에 도입돼 잠깐 노출됐던 클래스로, 정식 사용을 안내한 적이 없습니다. 1.1.17~1.1.20 구간에 이 SPI를 따라 만든 코드가 있다면 1.1.21에서 깨집니다.

| 삭제된 클래스 | 도입 | 삭제 |
|--------------|------|------|
| `c.n.f.adapter.NodeTreeAdapter` | 1.1.17 | 1.1.21 |
| `c.n.f.adapter.DefaultNodeTreeAdapter` | 1.1.17 | 1.1.21 |
| `c.n.f.adapter.JavaNodeTreeAdapterPlugin` | 1.1.17 | 1.1.21 |
| `c.n.f.kotlin.KotlinNodeTreeAdapterPlugin` (Kotlin) | 1.1.17 | 1.1.21 |

이들은 `c.n.f.planner.AssemblyPlanner` + `c.n.f.plugin.JvmTypeSystemPlugin`으로 대체됐습니다. 일반 사용자는 `FixtureMonkeyBuilder`만 쓰면 자동으로 새 구조를 받습니다.

## 영향 없음 (참고)

다음은 코드상 사라졌지만 일반 사용자가 직접 쓸 일이 거의 없는 내부 머신러리라 사용자 영향이 사실상 없습니다. 자세한 내용이 필요하면 git diff로 확인하세요.

- **`c.n.f.tree.*` 전체 삭제** (`NodeResolver`, `StartNodePredicate`, `NodeKeyPredicate`, `ObjectNode` 등 — since 0.4.0 MAINTAINED): 모두 expression(`.set("a.b.c", ...)`) 내부 구현. 사용자는 문자열 path를 쓰지 `NodeResolver`를 직접 만들지 않습니다.
- **`c.n.f.customizer` 매니퓰레이터 패밀리 삭제** (`ArbitraryManipulator`, `NodeSetJustManipulator`, `ContainerInfoManipulator` 등 — since 0.4.0 MAINTAINED): `ArbitraryBuilder.set/setLazy/setNull/size`의 내부 표현. 사용자는 builder 메서드만 호출합니다.
- **`c.n.f.expression.MonkeyExpression` / `MonkeyExpressionFactory` 삭제** (MAINTAINED since 0.4.0): `useExpressionStrictMode()`로 strict 모드를 켜는 사용자는 그대로 동작합니다 (strict 검증이 `StrictModeSizeValidator`로 이동). `MonkeyExpressionFactory`를 **직접 구현해서 주입**한 사용자만 영향.
- **`c.n.f.api.tree.*` 전체 삭제** (모두 EXPERIMENTAL): API 모듈의 traverse 골격.

## Migration cheat sheet

확장 지점을 구현 중인 경우 자주 쓰는 매핑:

| 1.1.20 이하 | 1.1.21+ |
|------------|---------|
| `ArbitraryGeneratorContext.getResolvedAnnotatedType()` | `getResolvedProperty().getAnnotatedType()` |
| `ArbitraryGeneratorContext.getResolvedType()` (`Type`) | `getResolvedType()` (`Class<?>`) 또는 `getResolvedJvmType()` |
| `JvmType.getAnnotatedType()` | `getRawType()` + `getAnnotations()` |
| `c.n.f.adapter.tracing.AdapterTracer` | `c.n.f.tracing.AssemblyTracer` |
| `c.n.f.adapter.NodeTreeAdapter` | `c.n.f.planner.AssemblyPlanner` (자동 주입) |
