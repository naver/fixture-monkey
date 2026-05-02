# Adapter 실행 흐름

## 전체 흐름

```
┌────────────────────────────────────────────────────────────────────────────┐
│ ArbitraryBuilder.sample() / sampleList()                                   │
│ - ManipulatorSet 생성 (사용자가 set(), size() 등으로 설정한 조작들)              │
└────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ DefaultNodeTreeAdapter.adapt(rootType, manipulatorSet, options)            │
│ - 캐시 확인 후 buildAdaptationResult() 호출                                  │
└────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ Phase 1: ManipulatorAnalyzer.analyze()                                     │
│ - ArbitraryManipulator 목록 → AnalysisResult                               │
│ - 추출 항목:                                                                │
│   - valuesByPath: 경로별 설정된 값                                           │
│   - filtersByPath: 경로별 필터                                               │
│   - interfaceResolvers: 인터페이스 구현체 결정 정보                            │
│   - valueOrderByPath: 값 설정 순서                                           │
└────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ Phase 2: resolveRootTypeWithAnalysisResult()                               │
│ - 루트 타입이 인터페이스/추상 클래스면 구현체로 resolve                          │
│ - "$" 경로에 직접 set된 값이 있으면 그 타입 사용                                │
└────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ Phase 3: ContainerInfoResolverConverter.convert()                          │
│ - ContainerInfoManipulator → PathResolver<ContainerSizeResolver>           │
│ - 컨테이너 크기 정보를 경로 기반 resolver로 변환                                │
└────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ Phase 4: createResolverContext()                                           │
│ - PathResolverContext 생성                                                  │
│ - 컨테이너 크기, 인터페이스 구현체 등 resolver 통합                             │
└────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ Phase 5: getOrBuildCandidateTree()                                         │
│ - JvmNodeCandidateTree 생성 (타입 구조만, 캐시됨)                              │
│ - PropertyGenerator로 자식 프로퍼티 생성                                      │
└────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ Phase 6: JvmNodeTreeTransformer.transform()                                │
│ - JvmNodeCandidateTree → JvmNodeTree                                       │
│ - NodeCandidate를 JvmNode로 promote (JvmNodePromoter 사용)                  │
│ - 컨테이너 요소 확장 (List, Map 등의 실제 요소 노드 생성)                        │
│ - 인터페이스 구현체 결정                                                      │
└────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ Phase 7: ValueProjection.fromStringPathMap()                               │
│ - JvmNodeTree + prunedValuesByPath → ValueProjection                       │
│ - 트리 구조와 사용자 설정 값을 결합                                            │
└────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ Return: AdaptationResult                                                   │
│ - valueProjection: 경로-값 매핑                                              │
│ - analysisResult: 분석 결과 (필터, 순서 등)                                   │
│ - timing 정보                                                               │
└────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ ValueProjection.assemble(context)                                          │
│ - AssemblyState 생성                                                        │
│ - assembleNode()로 트리 순회하며 각 노드 값 생성                               │
│ - ArbitraryIntrospector 사용하여 실제 객체 생성                               │
└────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ 최종 객체 반환                                                               │
└────────────────────────────────────────────────────────────────────────────┘
```

## Phase별 핵심 클래스

| Phase | 클래스 | 입력 | 출력 |
|-------|--------|------|------|
| 1 | ManipulatorAnalyzer | List<ArbitraryManipulator> | AnalysisResult |
| 3 | ContainerInfoResolverConverter | List<ContainerInfoManipulator> | List<PathResolver> |
| 5 | JvmNodeCandidateTree.Builder | JvmType, JvmNodeContext | JvmNodeCandidateTree |
| 6 | JvmNodeTreeTransformer | JvmNodeCandidateTree, PathResolverContext | JvmNodeTree |
| 7 | ValueProjection | JvmNodeTree, Map<String, Object> | ValueProjection |
| 8 | ValueProjection.assemble | AssembleContext | CombinableArbitrary |

## 캐싱 포인트

```
adapt()
    │
    ├── manipulatorSet.isEmpty()? → buildDefaultAdaptationResult() (최적화 경로)
    │
    └── manipulatorSet 있음 → buildAdaptationResult()
            │
            └── buildAdaptationResult() 내부
                    │
                    ├── candidateTreeCache: JvmNodeCandidateTree (타입 구조)
                    │
                    ├── concreteTypeCandidateTreeCache: JvmNodeCandidateTree (구체 타입용)
                    │
                    ├── nodeContextCache: JvmNodeContext (노드 컨텍스트)
                    │
                    ├── subtreeContext: JvmNodeSubtreeContext (JvmNode 서브트리)
                    │       - JvmNodeTreeTransformer.expandObjectChildren()에서 사용
                    │       - 타입별 promoted 노드 구조 캐시
                    │       - 컨테이너 노드만 동적 확장 (사이즈 매번 다름)
                    │       - 조건: 순환참조 없음, path-specific resolver 없음
                    │       - ExpansionContext 존재 시: non-self-recursive 서브트리만 저장 허용
                    │
                    └── currentResolverContext: ThreadLocal<PathResolverContext>
                            - 현재 adapt 호출의 PathResolverContext 보관

ValueProjection.assemble()
    │
    └── nodeMetadataCache: ConcurrentHashMap<JvmType, CachedTypeMetadata>
            - DefaultNodeTreeAdapter에서 관리, AssemblyState로 전달
            - JvmType별 PropertyNameResolver/NullInjectGenerator/isContainerType 캐시
            - write-back on miss: assembleNode()에서 캐시 미스 시 기록
            - clearCache()에서 함께 clear
```

## Registered Builder 처리

Registered builder는 타입별로 등록된 빌더 설정이다.

```
ManipulatorSet.registeredBuilderInfos
    │
    └── RegisteredBuilderInfo
            ├── targetType (예: ListStringObject.class)
            ├── manipulators (ArbitraryManipulator 목록)
            └── containerInfoManipulators (ContainerInfoManipulator 목록)
                    │
                    ▼
            타입 기반 resolver로 변환
                    │
                    ▼
            JvmNodeTreeTransformer에서 해당 타입 만나면 적용
```

**주의**: Registered builder의 컨테이너 크기는 **타입 기반**으로 적용된다.
트리 변환 시점에 해당 타입의 노드를 만나면 등록된 크기가 적용됨.

자세한 아키텍처는 memory `adapter-register-builder-architecture` 참조.
