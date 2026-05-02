# object-farm-api 모듈 구조

JVM 타입 시스템, 노드 트리, 경로 표현식 등 fixture-monkey의 기반 인프라를 제공하는 독립 모듈.

## 패키지 구조

```
object-farm-api/src/main/java/com/navercorp/objectfarm/api/
├── expression/     경로 표현식 (PathExpression, Selector 계층)
├── type/           JVM 타입 추상화 (JvmType, JavaType, GenericType)
├── node/           노드 구현 (JvmNode, Promoter, Resolver)
├── nodecandidate/  NodeCandidate + Generator + CreationMethod
├── tree/           트리 구조 (CandidateTree, NodeTree, Transformer)
├── input/          값 분석 + 타입 파싱 (ValueAnalyzer, TypeInputParser)
├── output/         트리 출력 포맷터 (TreeOutputFormatter)
└── projection/     노드 프로젝션 인터페이스 (NodeProjection)
```

## expression/ (경로 표현식)

| 클래스 | 역할 |
|--------|------|
| `PathExpression` | `$`, `.name`, `[index]`, `[*]` 등으로 구성된 경로 표현식 핵심 클래스 |
| `Segment` | PathExpression의 개별 경로 조각 (root `$`, child `.field`, index `[0]` 등) |
| `Selector` | Segment가 노드를 선택하는 방식의 마커 인터페이스 |
| `NameSelector` | 이름으로 필드 선택 (`$.fieldName`) |
| `IndexSelector` | 인덱스로 요소 선택 (`$[0]`) |
| `WildcardSelector` | 모든 자식/요소 선택 (`$.*`, `$[*]`) |
| `KeySelector` | Map key로 선택 |
| `ValueSelector` | Map value로 선택 |
| `TypeSelector` | 타입 기반 선택 (`$[type:ClassName]`) — register 등에서 사용 |

## type/ (JVM 타입 추상화)

| 클래스 | 역할 |
|--------|------|
| `JvmType` | JVM 타입 최상위 인터페이스 (Class, ParameterizedType 등 통합) |
| `JavaType` | JvmType 기본 구현체 |
| `GenericType` | 제네릭 타입 파라미터 정보를 포함하는 JvmType |
| `ObjectTypeReference` | 런타임 타입 캡처 (`new ObjectTypeReference<List<String>>() {}`) |
| `JvmTypes` | JvmType 팩토리/유틸리티 |
| `Types` | java.lang.reflect.Type 유틸리티 |
| `AnnotatedTypes` | AnnotatedType 유틸리티 |
| `Reflections` | Reflection 유틸리티 |
| `WildcardRawType` | 와일드카드 타입의 raw type 표현 |

## node/ (노드 구현 + Promoter + Resolver)

상세 내용은 [adapter-classes.md](./adapter-classes.md#node-jvmnode-구현--promoter) 참조.

### Core 노드

| 클래스 | 역할 |
|--------|------|
| `JvmNode` | 모든 JVM 타입을 나타내는 통합 노드 인터페이스 (계층적 부모-자식 구조) |
| `JavaNode` | 기본 JvmNode 구현체 (인덱스, CreationMethod 메타데이터 포함) |
| `JvmMapNode` / `JavaMapNode` | Map entry의 key/value 노드 접근자를 가진 특수 노드 |
| `JvmMapEntryNode` / `JavaMapEntryNode` | 독립 Map.Entry 노드 |

### 컨텍스트 + 시드

| 클래스 | 역할 |
|--------|------|
| `JvmNodeContext` / `JavaNodeContext` | 시드, promoter, generator, resolver를 번들링하는 컨텍스트 |
| `SeedState` | 기본 시드 + 증가 시퀀스 카운터 관리 |
| `SeedSnapshot` | 기본 시드 + 시퀀스 번호의 불변 스냅샷 |

### Node Promoter

| 클래스 | 역할 |
|--------|------|
| `JvmNodePromoter` | NodeCandidate → JvmNode 변환 인터페이스 |
| `JavaDefaultNodePromoter` | 여러 JvmNodePromoter를 조합하는 기본 promoter |
| `JavaObjectNodePromoter` | 일반 객체 노드 promote |
| `JavaMapNodePromoter` | Map 노드 promote |
| `JavaInterfaceNodePromoter` | 인터페이스 노드 promote |
| `JavaMapEntryNodePromoter` | Map.Entry 노드 promote |
| `AbstractTypeNodePromoter` | 인터페이스/추상 타입 NodeCandidate promote |

### 타입 해석기 (NodeCustomizer)

| 클래스 | 역할 |
|--------|------|
| `NodeCustomizer` | 마커 인터페이스 |
| `ContainerSizeResolver` | 컨테이너 크기 결정 함수형 인터페이스 |
| `RandomContainerSizeResolver` | 시드 기반 랜덤 크기 |
| `FixedContainerSizeResolver` | 고정 크기 |
| `GenericTypeResolver` | 제네릭 타입 파라미터 해석 |
| `InterfaceResolver` | 인터페이스 → 구체 구현체 해석 |
| `LeafTypeResolver` / `JavaLeafTypeResolver` | leaf 타입 판별 |

### 컨테이너 요소 노드 생성기

| 클래스 | 역할 |
|--------|------|
| `JvmContainerNodeGenerator` | 컨테이너 요소 JvmNode 생성 인터페이스 |
| `JavaArrayElementNodeGenerator` | 배열 요소 |
| `JavaLinearContainerElementNodeGenerator` | List, Set 요소 |
| `JavaMapElementNodeGenerator` | Map entry |
| `JavaSingleElementContainerNodeGenerator` | Supplier, Optional (투명 래퍼) |

## nodecandidate/ (NodeCandidate 생성)

상세 내용은 [adapter-classes.md](./adapter-classes.md#nodecandidate) 참조.

### Core

| 클래스 | 역할 |
|--------|------|
| `JvmNodeCandidate` | promote 전 타입 메타데이터를 보유하는 중간 표현 인터페이스 |
| `JvmNodeCandidateGenerator` | JVM 타입으로부터 NodeCandidate를 생성하는 루트 인터페이스 |
| `CreationMethod` | 프로퍼티 생성/설정 방식 메타데이터 인터페이스 |

### NodeCandidate 구현체

`JavaNodeCandidate`, `JavaMapNodeCandidate`, `JvmMapNodeCandidate`, `JvmMapEntryNodeCandidate`

### CreationMethod 구현체

`ConstructorParamCreationMethod`, `FieldAccessCreationMethod`, `MethodInvocationCreationMethod`, `ContainerElementCreationMethod`

### Generator

`JvmGenericNodeCandidateGenerator`, `JvmInterfaceNodeCandidateGenerator`, `JavaFieldNodeCandidateGenerator`, `DefaultGenericNodeCandidateGenerator`, `DefaultInterfaceNodeCandidateGenerator`

### Factory + 옵션

`JvmNodeCandidateFactory`, `JavaNodeCandidateFactory`, `JavaRecordNodeCandidateGenerator` (Java 17 MR JAR), `ObjectFarmJdkVariantOptions`

## tree/ (트리 구조 + 변환)

| 클래스 | 역할 |
|--------|------|
| `JvmNodeCandidateTree` | 타입 구조 트리 (캐시됨, 값 생성 전 구조) |
| `JvmNodeCandidateTreeContext` | CandidateTree 생성 및 서브트리 캐싱 컨텍스트 |
| `JvmNodeTree` | 실제 노드 트리 (값 생성에 사용) |
| `JvmNodeTreeTransformer` | CandidateTree → NodeTree 변환, 컨테이너 확장 |
| `ExpansionContext` | 재귀 노드 확장 제어 (사용자 정의 경로 기반 순환 확장 결정) |
| `JvmNodeSubtreeContext` | JvmNode 서브트리 스냅샷 캐시 (타입별, ConcurrentHashMap) |
| `PathResolver` | PathExpression + NodeCustomizer를 결합하는 경로 기반 해석 인터페이스 |
| `PathResolverContext` | 경로 기반 resolver 통합 관리 (컨테이너 크기, 인터페이스, 제네릭) |
| `PathContainerSizeResolver` | 특정 경로의 컨테이너 크기 결정 |
| `PathInterfaceResolver` | 특정 경로의 인터페이스 → 구체 구현체 결정 |
| `PathGenericTypeResolver` | 특정 경로의 제네릭 타입 해석 |
| `ResolutionListener` / `NoOpResolutionListener` | 트리 변환 중 해석 결정사항 추적 리스너 |

## input/ (값 분석 + 타입 파싱)

### 값 추출 + 분석

| 클래스 | 역할 |
|--------|------|
| `ObjectValueExtractor` | 재귀적 객체 그래프 → path→value flat map 생성 |
| `FieldExtractor` | 1-level POJO 필드 추출 인터페이스 |
| `ContainerDetector` | 컨테이너 타입 판별 + 크기 조회 인터페이스 |
| `ValueAnalyzer` | 객체 그래프에서 경로별 값, 타입 해석기, 컨테이너 크기 정보 추출 |
| `ValueAnalysisResult` | 값 분석 결과 불변 객체 |

### Resolver 변환기

| 클래스 | 역할 |
|--------|------|
| `InterfaceResolverConverter` | 값 정보 → 인터페이스 경로 기반 해석기 변환 |
| `GenericTypeResolverConverter` | 값/타입 정보 → 제네릭 타입 해석기 변환 |

### 타입 파싱

| 클래스 | 역할 |
|--------|------|
| `TypeInputParser` | 다양한 입력 형식을 JvmType으로 파싱하는 인터페이스 |
| `TypeInputParserRegistry` | TypeInputParser 등록/관리, 입력 타입에 따라 파서 자동 선택 |
| `TypeParseContext` | 타입 별칭, ClassLoader, strict 모드 등 파싱 컨텍스트 |
| `TypeParseException` | 파싱 실패 예외 |
| `JavaTypeInputParser` | JvmType, Class, Type 등 네이티브 Java 타입 파싱 |
| `JsonSchemaInputParser` | JSON Schema → JvmType 파싱 |
| `TypeScriptInputParser` | TypeScript 유사 문법 → JvmType 파싱 |

### Synthetic 타입

| 클래스 | 역할 |
|--------|------|
| `SyntheticJvmType` | 스키마 정의 타입을 JvmType 인프라에 통합 |
| `SyntheticMember` | SyntheticJvmType의 멤버(필드) |
| `SyntheticMemberCreationMethod` | 스키마 정의 타입 멤버의 CreationMethod |
| `SyntheticNodeCandidateGenerator` | SyntheticJvmType → JvmNodeCandidate 변환 |

## output/ (트리 출력 포맷터)

| 클래스 | 역할 |
|--------|------|
| `TreeOutputFormatter` | 트리 출력 포맷터 인터페이스 |
| `TreeOutputFormatterRegistry` | 포맷터 등록/관리 |
| `OutputFormat` | 출력 형식 enum (MARKDOWN, JSON, PROMPT_OPTIMIZED) |
| `MarkdownTreeFormatter` | Markdown 형식 트리 출력 |
| `JsonTreeFormatter` | JSON 형식 트리 출력 |
| `PromptOptimizedFormatter` | LLM 프롬프트 최적화 형식 출력 |
| `FormatOptions` | 출력 옵션 (depth 제한 등) |

## projection/ (노드 프로젝션)

| 클래스 | 역할 |
|--------|------|
| `NodeProjection<T>` | 노드에 값을 매핑하는 읽기 전용 뷰 인터페이스 (get, getByPath, forEach, filter) |

## 테스트

```
object-farm-api/src/test/java/com/navercorp/objectfarm/api/
├── input/          ObjectValueExtractorTest, TypeInputParserRegistryTest, TypeScriptInputParserTest, ValueAnalyzerTest
├── node/           JavaNodePromoterTest, JavaNodeTest, specs/ (GenericObject, ImmutableObject, InterfaceSpecs)
├── tree/           JvmNodeCandidateTreeTest, JvmNodeTreeTest, JvmNodeTreeResolveTest,
│                   JvmNodeTreeTransformerTest, NestedContainerTreeTest, PathResolverContextTest,
│                   PathResolverIntegrationTest
└── output/         TreeOutputFormatterTest
```

## 빌드 및 테스트

```bash
# 전체 테스트
./gradlew clean :object-farm-api:test

# 특정 패키지 테스트
./gradlew clean :object-farm-api:test --tests "com.navercorp.objectfarm.api.tree.*"
./gradlew clean :object-farm-api:test --tests "com.navercorp.objectfarm.api.input.*"
./gradlew clean :object-farm-api:test --tests "com.navercorp.objectfarm.api.node.*"
```
