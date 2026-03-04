# Adapter 클래스 구조

## fixture-monkey 모듈

```
fixture-monkey/src/main/java/com/navercorp/fixturemonkey/adapter/
```

### Root (진입점 + 오케스트레이터)

| 클래스 | 역할 |
|--------|------|
| `NodeTreeAdapter` | adapt 인터페이스 |
| `DefaultNodeTreeAdapter` | 메인 구현체 - adapt 로직, 캐싱(promoted subtree + type metadata), resolver 생성 |
| `JavaNodeTreeAdapterPlugin` | 플러그인 설정 진입점 |
| `ContainerValuePruner` | 컨테이너 크기 초과 값 pruning (인스턴스 클래스, ContainerDetector 필드 주입, DefaultNodeTreeAdapter에서 분리) |
| `ContainerSizeResolverFactory` | ContainerSizeResolver 생성 팩토리 (lazy/manipulator/typed 크기 resolver 생성, DefaultNodeTreeAdapter에서 분리) |

### tracing/ (디버깅 + 진단)

| 클래스 | 역할 |
|--------|------|
| `AdapterTracer` | 디버깅용 트레이서 인터페이스 |
| `TraceContext` | 트레이스 컨텍스트 인터페이스 (NoOp 패턴) |
| `ActiveTraceContext` | 활성 트레이스 구현 |
| `NoOpTraceContext` | NoOp 트레이스 구현 |
| `ResolutionTrace` | 불변 트레이스 결과 데이터 |
| `ResolutionTraceFormatter` | ResolutionTrace 포매팅 (tree format, JSON format, assembly tree) |
| `TraceContextResolutionListener` | ResolutionListener → TraceContext 브릿지 |
| `TimingSample` | 타이밍 측정 인터페이스 |
| `AdapterTraceBuilder` | 트레이스 데이터 수집 + 트레이서 호출 (stateless) |

### projection/ (assembly 엔진)

| 클래스 | 역할 |
|--------|------|
| `ValueProjection` | 경로-값 매핑 저장 (데이터 구조) |
| `ValueProjectionAssembler` | 트리 순회 + 값 생성 어셈블리 엔진 |
| `ValueProjectionAssembler.CachedTypeMetadata` | JvmType별 PropertyNameResolver/NullInjectGenerator/isContainerType 캐시 (cross-call) |
| `ValueCandidate` | 값 + 순서 + 출처(USER_SET/REGISTER/DECOMPOSED) 통합 메타데이터 |
| `ValueOrder` | 타입 기반 우선순위 인터페이스 (UserOrder: direct, RegisterOrder: register) |
| `ValueDecomposer` | exact value를 하위 필드로 분해, ObjectValueExtractor로 값 추출 후 order 비교 필터링, DecomposeResult 반환 |
| `DecomposeResult` | ValueDecomposer의 불변 결과 객체 (ValueCandidate 맵 반환) |
| `PathValueResolver` | 경로 매칭 + thenApply lazy 해석 |
| `AssembleContext` | assemble 시 필요한 컨텍스트 |
| `LazyValueHolder` | LazyArbitrary 지연 평가 + root-level만 재귀 방지 (ThreadLocal 기반, field-level lazy는 guard 없이 직접 평가, RECURSION_BLOCKED sentinel로 supplier null과 guard null 구분) |

### analysis/ (manipulator 분석)

| 클래스 | 역할 |
|--------|------|
| `ManipulatorAnalyzer` | Manipulator 분석 → AnalysisResult |
| `AnalysisResult` | 분석 결과 불변 객체 (resolvers, values, filters, customizers 등), inner class: LazyManipulatorDescriptor, PostConditionFilter, PropertyCustomizer |
| `AdaptationResult` | adapt 결과 (JvmNodeTree + ValueProjection + AnalysisResult) |
| `TypedValueExtractor` | Manipulator에서 typed value 추출 + PathExpression 변환 (stateless) |

### converter/ (stateless 변환기)

| 클래스 | 역할 |
|--------|------|
| `ContainerInfoResolverConverter` | ContainerInfoManipulator → PathResolver 변환 |
| `PredicatePathConverter` | NextNodePredicate → PathExpression 변환, NodeResolver → Predicate 추출 |

### nodecandidate/ (NodeCandidate 생성)

| 클래스 | 역할 |
|--------|------|
| `PropertyGeneratorNodeCandidateGenerator` | PropertyGenerator로 NodeCandidate 생성 |
| `InterfaceMethodNodeCandidateGenerator` | 인터페이스 no-arg 메서드 → NodeCandidate |
| `NameResolvingNodeCandidateGenerator` | 이름 해석 래퍼 |
| `ContainerPropertyGeneratorNodeGenerator` | ContainerPropertyGenerator → JvmContainerNodeGenerator 어댑터 |

### property/ (JvmNode ↔ Property 변환)

| 클래스 | 역할 |
|--------|------|
| `JvmNodePropertyFactory` | JvmNode/CreationMethod → Property 팩토리 |

## object-farm-api 모듈

> 전체 모듈 구조(expression, type, output 등 포함)는 [object-farm-api.md](./object-farm-api.md) 참조.
> 아래는 adapter와 직접 연관된 패키지만 기술.

### 트리 구조

```
object-farm-api/src/main/java/com/navercorp/objectfarm/api/tree/
```

| 클래스 | 역할 |
|--------|------|
| `JvmNodeCandidateTree` | 타입 구조 트리 (캐시됨, 값 생성 전 구조) |
| `JvmNodeCandidateTreeContext` | CandidateTree 생성 및 서브트리 캐싱 컨텍스트 |
| `JvmNodeTree` | 실제 노드 트리 (값 생성에 사용) |
| `JvmNodeTreeTransformer` | CandidateTree → NodeTree 변환, 컨테이너 확장 |
| `ExpansionContext` | 재귀 노드 확장 제어 (사용자 정의 경로 기반 순환 확장 결정) |
| `JvmNodeSubtreeContext` | JvmNode 서브트리 스냅샷 캐시 (타입별, ConcurrentHashMap) + Snapshot inner class |
| `PathResolver` | PathExpression 패턴 + NodeCustomizer를 결합하는 경로 기반 해석 제네릭 인터페이스 |
| `PathResolverContext` | 경로 기반 resolver들 (컨테이너 크기, 인터페이스, 제네릭 타입) + ResolutionListener 통합 관리 |
| `PathContainerSizeResolver` | 특정 경로의 컨테이너 크기를 결정하는 PathResolver 구현체 |
| `PathInterfaceResolver` | 특정 경로의 인터페이스/추상 타입을 구체 구현체로 결정하는 PathResolver 구현체 |
| `PathGenericTypeResolver` | 특정 경로의 제네릭 타입을 구체 타입으로 해석하는 PathResolver 구현체 |
| `ResolutionListener` | 트리 변환 중 인터페이스/컨테이너 크기 해석 결정사항을 추적하는 리스너 인터페이스 |
| `NoOpResolutionListener` | 추적 비활성화 시 오버헤드 없이 동작하는 ResolutionListener no-op 구현체 |

### NodeCandidate

```
object-farm-api/src/main/java/com/navercorp/objectfarm/api/nodecandidate/
```

#### Core 인터페이스 + 기반 클래스

| 클래스 | 역할 |
|--------|------|
| `JvmNodeCandidate` | promote 전 타입 메타데이터를 보유하는 중간 표현 인터페이스 |
| `JvmNodeCandidateGenerator` | JVM 타입으로부터 NodeCandidate를 생성하는 루트 인터페이스 |
| `CreationMethod` | 프로퍼티 생성/설정 방식 메타데이터 인터페이스 |

#### NodeCandidate 구현체

| 클래스 | 역할 |
|--------|------|
| `JavaNodeCandidate` | POJO 필드용 표준 JvmNodeCandidate 구현 |
| `JavaMapNodeCandidate` | Map 타입 전용 JvmNodeCandidate (별도 key/value NodeCandidate 보유) |
| `JvmMapNodeCandidate` | Map.Entry를 독립 필드 타입으로 사용할 때의 마커 인터페이스 |
| `JvmMapEntryNodeCandidate` | 독립 필드로서의 Map.Entry NodeCandidate (Map 컨테이너 외부) |

#### CreationMethod 구현체

| 클래스 | 역할 |
|--------|------|
| `ConstructorParamCreationMethod` | 생성자 파라미터로 전달되는 프로퍼티 메타데이터 (인덱스 포함) |
| `FieldAccessCreationMethod` | 필드 reflection 직접 접근 프로퍼티 메타데이터 |
| `MethodInvocationCreationMethod` | 메서드 호출 (setter, builder, factory) 프로퍼티 메타데이터 |
| `ContainerElementCreationMethod` | 컨테이너 요소 인덱스 접근 프로퍼티 메타데이터 |

#### Generator 인터페이스 + 특수 생성기

| 클래스 | 역할 |
|--------|------|
| `JvmGenericNodeCandidateGenerator` | 제네릭 타입 파라미터 커스터마이징 가능한 생성기 인터페이스 |
| `JvmInterfaceNodeCandidateGenerator` | 인터페이스 타입 전용 생성기 인터페이스 |
| `JavaFieldNodeCandidateGenerator` | POJO 필드 NodeCandidate 표준 생성기 |
| `DefaultGenericNodeCandidateGenerator` | GenericTypeResolver로 제네릭 타입 파라미터 해석 후 위임하는 기본 구현 |
| `DefaultInterfaceNodeCandidateGenerator` | InterfaceResolver로 인터페이스를 구체 구현체로 해석 후 위임하는 기본 구현 |

#### Factory + 옵션

| 클래스 | 역할 |
|--------|------|
| `JvmNodeCandidateFactory` | JvmType → JvmNodeCandidate 변환 인터페이스 |
| `JavaNodeCandidateFactory` | 기본 구현 (Map.Entry → JavaMapEntryNodeCandidate 처리 포함) |
| `JavaRecordNodeCandidateGenerator` | Record 타입 전용 NodeCandidate 생성 (Java 17 MR JAR, canonical constructor 기반) |
| `ObjectFarmJdkVariantOptions` | JDK 버전별 옵션 (Java 8 no-op / Java 17 record 지원 등록) |

### Node (JvmNode 구현 + Promoter)

```
object-farm-api/src/main/java/com/navercorp/objectfarm/api/node/
```

#### Core 노드 인터페이스 + 구현체

| 클래스 | 역할 |
|--------|------|
| `JvmNode` | 모든 JVM 타입을 나타내는 통합 노드 인터페이스 (계층적 부모-자식 구조) |
| `JavaNode` | 기본 JvmNode 구현체 (인덱스, CreationMethod 메타데이터 포함) |
| `JvmMapNode` | Map entry의 key/value 노드 접근자를 가진 특수 JvmNode 인터페이스 |
| `JavaMapNode` | JvmMapNode 구현체 (JvmMapNodeCandidate → 1:1 매핑) |
| `JvmMapEntryNode` | 독립 Map.Entry의 key/value 노드 접근자를 가진 특수 JvmNode 인터페이스 |
| `JavaMapEntryNode` | JvmMapEntryNode 구현체 (1:1 토폴로지 매핑) |

#### 컨텍스트 + 시드 관리

| 클래스 | 역할 |
|--------|------|
| `JvmNodeContext` | 시드, promoter, candidate/container 생성기, resolver를 번들링하는 컨텍스트 인터페이스 |
| `JavaNodeContext` | JvmNodeContext 기본 구현 (Builder 패턴) |
| `SeedState` | 기본 시드 + 증가 시퀀스 카운터 관리 (스레드 안전 스냅샷, 결정적 랜덤 생성) |
| `SeedSnapshot` | 기본 시드 + 시퀀스 번호의 불변 스냅샷 (재현 가능한 랜덤 값 보장) |

#### Node Promoter

| 클래스 | 역할 |
|--------|------|
| `JvmNodePromoter` | NodeCandidate → JvmNode 변환 인터페이스 |
| `JavaDefaultNodePromoter` | 여러 JvmNodePromoter를 조합하는 기본 promoter (위임 기반) |
| `JavaObjectNodePromoter` | 일반 객체 노드 promote |
| `JavaMapNodePromoter` | Map 노드 promote |
| `JavaInterfaceNodePromoter` | 인터페이스 노드 promote |
| `JavaMapEntryNodePromoter` | Map.Entry 노드 promote (key/value 1:1 래핑) |
| `AbstractTypeNodePromoter` | 인터페이스/추상 타입 NodeCandidate promote |

#### 컨테이너 요소 노드 생성기

| 클래스 | 역할 |
|--------|------|
| `JvmContainerNodeGenerator` | 컨테이너 요소 JvmNode 생성 인터페이스 (런타임 동적 생성) |
| `JavaArrayElementNodeGenerator` | 배열 요소 노드 생성 (ContainerSizeResolver 기반) |
| `JavaLinearContainerElementNodeGenerator` | 선형 컨테이너 (List, Set) 요소 노드 생성 |
| `JavaMapElementNodeGenerator` | Map entry (key/value 쌍) 노드 생성 |
| `JavaSingleElementContainerNodeGenerator` | 투명 래퍼 (Supplier, Optional) 노드 생성 (단일 무명 내부 요소) |

#### 타입 해석기

| 클래스 | 역할 |
|--------|------|
| `NodeCustomizer` | 노드 커스터마이저 마커 인터페이스 (GenericTypeResolver, InterfaceResolver, ContainerSizeResolver, LeafTypeResolver) |
| `ContainerSizeResolver` | 컨테이너 크기 결정 함수형 인터페이스 (결정적 랜덤 + 고정 크기 구현) |
| `RandomContainerSizeResolver` | 지정 범위 내 시드 기반 랜덤 크기 resolver |
| `FixedContainerSizeResolver` | 미리 결정된 고정 크기 resolver |
| `GenericTypeResolver` | 제네릭 타입 파라미터를 구체 타입으로 해석하는 함수형 인터페이스 |
| `InterfaceResolver` | 인터페이스를 구체 구현체로 해석하는 함수형 인터페이스 |
| `LeafTypeResolver` | 커스텀 leaf 타입 판별 인터페이스 (kotlin.Unit 등) |
| `JavaLeafTypeResolver` | Java 표준 타입 (primitives, java.*, sun.*) leaf 판별 구현 |

### Input (값 분석 + 타입 파싱)

```
object-farm-api/src/main/java/com/navercorp/objectfarm/api/input/
```

#### 값 추출 + 분석

| 클래스 | 역할 |
|--------|------|
| `ObjectValueExtractor` | 재귀적 값 분해기 — FieldExtractor + ContainerDetector 조합으로 객체 그래프에서 path→value flat map 생성 |
| `FieldExtractor` | 1-level POJO 필드 추출 인터페이스 (reflection 구현 제공) |
| `ContainerDetector` | 컨테이너 타입 판별 + 크기 조회 인터페이스 (standard 구현 제공) |
| `ValueAnalyzer` | 객체 그래프를 분석하여 경로별 값, 타입 해석기, 컨테이너 크기 정보를 추출하는 재귀적 값 분해기 |
| `ValueAnalysisResult` | 값 분석 결과 불변 객체 (인터페이스/제네릭/컨테이너 해석기 + 분해된 경로별 값) |

#### Resolver 변환기

| 클래스 | 역할 |
|--------|------|
| `InterfaceResolverConverter` | 값 정보를 분석하여 인터페이스 타입을 구체 타입으로 해석하는 경로 기반 해석기로 변환 |
| `GenericTypeResolverConverter` | 값/명시적 타입 정의에서 제네릭 타입 정보를 추출하여 제네릭 타입 해석기로 변환 |

#### 타입 파싱 (스키마 기반 타입 정의)

| 클래스 | 역할 |
|--------|------|
| `TypeInputParser` | 다양한 입력 형식 (Java, JSON Schema, TypeScript)을 JvmType으로 파싱하는 인터페이스 |
| `TypeInputParserRegistry` | TypeInputParser 구현체 등록/관리, 입력 타입에 따라 적절한 파서 자동 선택 |
| `TypeParseContext` | 타입 별칭, ClassLoader, strict 모드 등 파싱 동작 커스터마이징 컨텍스트 |
| `TypeParseException` | TypeInputParser 파싱 실패 시 발생하는 예외 |
| `JavaTypeInputParser` | JvmType, Class, Type, AnnotatedType, ObjectTypeReference 등 네이티브 Java 타입 파싱 |
| `JsonSchemaInputParser` | JSON Schema 형식 문자열을 JvmType으로 파싱 |
| `TypeScriptInputParser` | TypeScript 유사 문법 문자열 (예: `{ name: string }`)을 JvmType으로 파싱 |

#### Synthetic 타입 (스키마 정의 타입 통합)

| 클래스 | 역할 |
|--------|------|
| `SyntheticJvmType` | JSON Schema/TypeScript 등 스키마로 정의된 타입을 기존 JvmType 인프라에 통합하는 합성 타입 |
| `SyntheticMember` | SyntheticJvmType의 멤버(필드) 표현 |
| `SyntheticMemberCreationMethod` | 스키마 정의 타입 멤버의 생성 방식 메타데이터 CreationMethod |
| `SyntheticNodeCandidateGenerator` | SyntheticJvmType 멤버들을 JvmNodeCandidate로 변환하는 생성기 |

## 자주 수정하는 파일

| 파일 | 수정 이유 |
|------|----------|
| `DefaultNodeTreeAdapter.java` | adapt 로직, 캐싱, resolver 생성 |
| `ContainerValuePruner.java` | 컨테이너 크기 초과 값 pruning |
| `ContainerSizeResolverFactory.java` | ContainerSizeResolver 생성 로직 |
| `projection/ValueProjection.java` | 경로-값 매핑, 값 병합 |
| `projection/ValueProjectionAssembler.java` | assemble 로직 (트리 순회, 값 생성) |
| `projection/PathValueResolver.java` | 경로 매칭, thenApply lazy 해석 |
| `projection/ValueDecomposer.java` | exact value 분해 (객체 필드, 컨테이너 요소) |
| `nodecandidate/PropertyGeneratorNodeCandidateGenerator.java` | NodeCandidate 생성 로직 |
| `JavaObjectNodePromoter.java` (object-farm-api) | 일반 객체 Node promote 로직 |
| `JvmNodeTreeTransformer.java` (object-farm-api) | 트리 변환, 컨테이너 확장 |

## 테스트 파일

### FixtureMonkeyAdapterTest 계열 (FixtureMonkeyTestSpecs 기반)

카테고리별로 분리된 파일들 (`com.navercorp.fixturemonkey.adapter` 패키지):
- `ApplyAdapterTest` - apply/acceptIf
- `BasicGenerationAdapterTest` - 기본 타입/객체 생성
- `ContainerAdapterTest` - 컨테이너 타입
- `CustomizationAdapterTest` - set/size/apply/postCondition 등
- `DoubleThenApplyAdapterTest` - double/triple thenApply 패턴
- `FixedSampleAdapterTest` - fixed() 관련
- `GenericTypeAdapterTest` - 제네릭 타입
- `InstantiatorAdapterTest` - 생성자/팩토리 메서드
- `IntrospectorAdapterTest` - ArbitraryIntrospector 관련
- `MiscAdapterTest` - 기타 (leaf type inference 포함)
- `PluginAdapterTest` - 플러그인
- `RecursiveTypeAdapterTest` - 재귀/순환 참조
- `RegisterAdapterTest` - register/registerGroup
- `RegisterComplexAdapterTest` - register 복합 시나리오
- `SizeAdapterTest` - size/minSize/maxSize
- `StrictModeAdapterTest` - strict mode
- `ThenApplyOrderingAdapterTest` - thenApply/size/set 순서

### ValueProjection 계열 (ValueProjectionAssembleSpecs 기반)

카테고리별로 분리된 파일들 (`com.navercorp.fixturemonkey.adapter` 패키지):
- `ValueProjectionBasicGenerationTest` - 기본 타입/객체 생성
- `ValueProjectionContainerTest` - 컨테이너 타입
- `ValueProjectionCustomizationTest` - set/size/apply/postCondition 등
- `ValueProjectionGenericTest` - 제네릭 타입
- `ValueProjectionInstantiatorTest` - 생성자/팩토리 메서드
- `ValueProjectionInterfaceTest` - 인터페이스/추상/sealed class
- `ValueProjectionIntrospectorTest` - ArbitraryIntrospector 관련
- `ValueProjectionMiscTest` - 기타
- `ValueProjectionPluginTest` - 플러그인
- `ValueProjectionPropertySelectorTest` - PropertySelector/expression
- `ValueProjectionStrictModeTest` - strict mode
- `ValueProjectionThenApplyTest` - thenApply/size/set 순서

### 단위 테스트

- `PredicatePathConverterTest` - PredicatePathConverter 단위 테스트
- `ContainerInfoResolverConverterTest` - ContainerInfoResolverConverter 단위 테스트
- `InterfaceResolverConverterTest` - InterfaceResolverConverter 단위 테스트
- `DefaultNodeTreeAdapterTest` - DefaultNodeTreeAdapter 단위 테스트
- `ValueProjectionUnitTest` - ValueProjection 빌더/데이터 구조 단위 테스트

### 기타 테스트 (`com.navercorp.fixturemonkey.test` 패키지)

- `FixtureMonkeyTest` - FixtureMonkey 통합 테스트
- `FixtureMonkeyOptionsTest` / `FixtureMonkeyOptionsAdditionalTestSpecs` - 옵션 테스트
- `InnerSpecTest` / `InnerSpecTestSpecs` - InnerSpec 테스트
- `StackOverflowReproTest` - StackOverflow 재현 테스트

### Spec 파일

| 파일 | 용도 |
|------|------|
| `FixtureMonkeyTestSpecs.java` (`test` 패키지) | FixtureMonkeyAdapterTest 계열 spec 클래스들 |
| `ValueProjectionAssembleSpecs.java` (`adapter` 패키지) | ValueProjection 계열 spec 클래스들 |

## 디버깅

테스트 실패 시 tracer 활성화:

```java
FixtureMonkey fm = FixtureMonkey.builder()
    .plugin(new JavaNodeTreeAdapterPlugin()
        .tracer(AdapterTracer.console()))
    .build();
```

### AdapterTracer 종류

| 트레이서 | 설명 |
|---------|------|
| `AdapterTracer.console()` | 콘솔에 텍스트 출력 |
| `AdapterTracer.consoleJson()` | 콘솔에 JSON 형식 출력 |
| `AdapterTracer.timing()` | 타이밍 정보 출력 |
| `AdapterTracer.file(Path)` | 파일에 트리 형식 출력 (append 모드) |
| `AdapterTracer.summary()` | 전체 trace 수집, 요약 테이블/타이밍 분석 출력 |

## 테스트 실행

```bash
# 특정 카테고리 테스트
./gradlew :fixture-monkey:test --tests "com.navercorp.fixturemonkey.test.CustomizationAdapterTest"
./gradlew :fixture-monkey:test --tests "com.navercorp.fixturemonkey.adapter.ValueProjectionCustomizationTest"

# Adapter 회귀 테스트 (전체)
./gradlew :fixture-monkey:test --tests "com.navercorp.fixturemonkey.test.*"
./gradlew :fixture-monkey:test --tests "com.navercorp.fixturemonkey.adapter.*"
```

## 작업 흐름

1. **테스트 작성** - 최소 단위 테스트 우선 작성
2. **기능 구현** - 기존 구조와 실행 흐름 유지하며 구현
3. **단위 테스트 실행**
4. **회귀 테스트 실행**
5. **실패 시** - tracer 활성화하여 원인 파악
