# Fixture Monkey - Claude Code 가이드

## 절대 규칙

- 기존 테스트는 **삭제/수정/주석 처리 금지**
- 테스트 실패 시 바로 롤백 (개선 가능하다고 판단되면 사용자에게 롤백 여부 확인)
- Adapter 작업 시 `fixture-monkey`, `fixture-monkey-api` 모듈 코드 수정 금지 (필요 시 사용자에게 확인)
- 커밋은 사용자가 명시적으로 요청할 때만 (메시지는 영어)
- 데드 코드는 제거한다
- [작업 흐름] 을 보고 작업을 한다

## 프로젝트 구조

| 모듈/폴더 | 역할 |
|------|------|
| `fixture-monkey/` | 메인 모듈 (ArbitraryBuilder, FixtureMonkey 등) |
| `fixture-monkey-api/` | 공용 API 인터페이스 |
| `object-farm-api/` | JvmNodeTree, PathExpression 등 구조 관련 API |
| `fixture-monkey-tests/` | 통합 테스트 |
| `history/` | 벤치마크 결과 + 작업 히스토리 |
| `guide/` | 프로파일링 가이드 등 참고 문서 |

## 빌드 및 테스트

```bash
./gradlew build                        # 전체 빌드
./gradlew :fixture-monkey:test         # 특정 모듈 테스트
./gradlew :fixture-monkey:test --tests "com.navercorp.fixturemonkey.test.FixtureMonkeyAdapterTest"
```

**Adapter 작업 시 회귀 테스트**:
```bash
./gradlew clean \
  :fixture-monkey:test --tests "com.navercorp.fixturemonkey.adapter.*" \
  :fixture-monkey-tests:java-tests:test --tests "com.navercorp.fixturemonkey.tests.java.adapter.*" \
  :fixture-monkey-tests:java-17-tests:test --tests "com.navercorp.fixturemonkey.tests.java17.adapter.*" \
  :fixture-monkey-tests:kotlin-tests:test --tests "com.navercorp.fixturemonkey.tests.kotlin.adapter.*"
```

## 코드 스타일

- 탭 들여쓰기 (스페이스 아님)
- 주석은 `// given`, `// when`, `// then`만 허용 (예외 시 사용자 확인)
- Javadoc은 public API에만 작성
- `@API` 어노테이션으로 API 안정성 표시 (`EXPERIMENTAL`, `MAINTAINED` 등)

## 디버깅 절차

테스트 실패 시 아래 순서를 따른다:

1. **코드 분석 우선** - 코드를 읽고 원인 추론
2. **런타임 확인** - 분석만으로 부족하면 디버그 로그 추가 후 `--info` 플래그로 실행
3. **문제 격리** - 반복 실패 시 최소 재현 테스트 작성 (sample() → adapt → 특정 Phase 순으로 좁힘)
4. **수정 및 검증** - 원인 부분만 수정, 기존 테스트 전체 통과 확인

tracer 활성화:
```java
FixtureMonkey fm = FixtureMonkey.builder()
    .plugin(new JavaNodeTreeAdapterPlugin()
        .tracer(AdapterTracer.console()))
    .build();
```

## 테스트 케이스 관리

테스트 작성/발견 시 해당 경우의 수가 **기존 테스트에서 커버하지 못하는 새로운 경우의 수**라고 판단되면:

1. `test-case/` 폴더의 해당 카테고리 마크다운 파일에 경우의 수를 추가
2. 카테고리가 없으면 새 마크다운 파일 생성
3. 각 경우의 수는 **원자적 단위**(하나의 독립된 동작/조건)로 기록
4. 타입 변형(String→Int, List→Set→Array)은 같은 경우의 수로 취급 — 동작이 다른 경우만 별도

### 파일 형식

- `[x]` 이미 테스트됨 (`테스트파일:메서드명`)
- `[ ]` 미구현

### 카테고리

| 파일 | 카테고리 |
|------|---------|
| `basic-generation.md` | 기본 타입/객체 생성 |
| `container.md` | 컨테이너 타입 |
| `customization.md` | set/size/apply/postCondition 등 |
| `generic.md` | 제네릭 타입 |
| `interface-abstract.md` | 인터페이스/추상/sealed class |
| `recursive.md` | 재귀/순환 참조 |
| `instantiator.md` | 생성자/팩토리 메서드 |
| `introspector.md` | ArbitraryIntrospector 관련 |
| `register.md` | register/registerGroup |
| `strict-mode.md` | strict mode |
| `plugin.md` | 플러그인 |
| `property-selector.md` | PropertySelector/expression |
| `kotlin.md` | Kotlin 전용 |
| `annotation-validation.md` | 어노테이션/validation |
| `then-apply-ordering.md` | thenApply/size/set 순서 상호작용 |

## 문서 관리 규칙

- 작업 중 생성한 문서(가이드, 분석 등)는 `guide/`에 저장한다
- 벤치마크 실행 시 결과를 `history/benchmark-history.md`에 추가한다
- 작업 완료 시 `history/work-log.md`에 날짜와 한 줄 요약을 추가한다
  - 형식: `- YYYY-MM-DD: 작업 내용 한 줄 요약`

## Plan 작성 규칙

1. Claude Code가 자동 생성하는 plan 파일에 작성
2. 파일명: 작업 내용을 요약하는 제목 (예: `add-map-entry-support.md`)
3. 문서 내용: 작업 목표, 변경 대상 파일, 구현 계획
4. 구현 완료 후 `plans/` 디렉토리에 최종 plan 복사

## 문서 동기화

코드 변경 시 관련 문서도 함께 업데이트:
- `AdapterTracer`, `ResolutionTrace` 변경 시 → `architecture/adapter-tracer.md` 업데이트

## 작업 흐름
아래 흐름을 거이 무조건 지켜야 한다. 지킬 필요가 없거나 왜 지켜야할지 모르겠으면 사용자에게 묻는다

1. 재현 가능한 테스트를 작성한다
2. 기능을 구현한다
3. 재현 가능한 테스트를 실행한다. 실패할 경우 2번을 진행한다. 성공하면 4번으로 간다
4. 회귀 테스트를 실행한다
