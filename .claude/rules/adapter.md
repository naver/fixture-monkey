# Adapter 개요

Adapter는 `ManipulatorSet`(사용자 조작 명령)을 `ValueProjection`(경로-값 매핑)으로 변환하는 핵심 컴포넌트다.

## 핵심 흐름 요약

```
ArbitraryBuilder.sample()
    ↓
ManipulatorSet (사용자가 설정한 값, 크기, 필터 등)
    ↓
DefaultNodeTreeAdapter.adaptWithValues()
    ↓
AdaptationResult (JvmNodeTree + ValueProjection)
    ↓
ValueProjection.assemble()
    ↓
최종 객체
```

## 서브패키지 구조

```
adapter/
├── tracing/        디버깅 + 진단 (AdapterTracer, TraceContext 등)
├── projection/     assembly 엔진 (ValueProjection, ValueProjectionAssembler 등)
├── analysis/       manipulator 분석 (ManipulatorAnalyzer, AdaptationResult)
├── converter/      stateless 변환기 (PredicatePathConverter 등)
├── nodecandidate/  NodeCandidate 생성 브릿지
├── property/       JvmNode ↔ Property 변환
└── (root)          진입점 (NodeTreeAdapter, DefaultNodeTreeAdapter, JavaNodeTreeAdapterPlugin)
```

## 관련 문서

- [adapter-execution-flow.md](./adapter-execution-flow.md) - 상세 실행 흐름
- [adapter-classes.md](./adapter-classes.md) - 클래스 구조 및 역할

## 중요 규칙

### 실행 흐름 유지

**실행 흐름은 절대 변경하지 않는다.** `adapter-execution-flow.md`에 정의된 Phase 순서와 흐름을 반드시 유지해야 한다.

불가피하게 변경이 필요한 경우 반드시 사용자에게 먼저 확인을 받아야 한다.

### 문서 유지

클래스 변경/삭제/추가 시 반드시 이 rules 문서들을 업데이트해야 한다.

- 클래스 삭제: 해당 클래스를 문서에서 제거
- 클래스 추가: 역할에 맞는 문서에 추가
- 클래스 대체: 기존 클래스 제거 + 새 클래스 추가
