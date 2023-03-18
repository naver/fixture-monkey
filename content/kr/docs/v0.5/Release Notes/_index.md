
---
title: "Release Notes"
weight: 6
---
### 0.5.2
* 0.5.0부터 발생한 성능 저하 이슈를 해결합니다.
* 인터페이스를 생성할 때 실제 생성하는 구현체의 옵션을 적용합니다.

### 0.5.1
- 값을 분해하지 않고 `set`하는 연산 추가. 
  - `set(표현식, Values.Just(값))`
- Primitive type을 `setPostCondition` 했을 때 발생하는 타입 불일치 예외를 해결합니다.
- Generic을 가지는 `ConstructorProperty`에서 타입이 지워지는 문제를 해결합니다.

### 0.5.0
#### Breaking changes
- register 동작 방식 변경
  - As-is
    - `register한 ArbitrayBuilder`를 `sample`한 결과 값을 `set`함
  - To-be
    - `register한 ArbitraryBuilder`의 모든 연산을 기본적으로 가지고 있음
- 연산 우선순위 제거
  - As-is
    - `size` 연산이 최우선순위를 가진다
  - To-be
    - 연산 선언 순서대로 실행한다
- `defaultArbitraryContainerInfo`, `defaultArbitrayContainerSize` 옵션 제거하고 `defaultArbitraryContainerInfoGenerator` 옵션 추가

#### New Features
- `NOT_NULL` 정적 변수 추가
  - `set("expression", NOT_NULL)` == `setNotNull("expression")`
- 타입이 다른 값으로 `set`하여 실패했을 때 출력하는 로그 정보를 보충합니다.
  - 상위 프로퍼티 타입을 출력합니다. 상위 프로퍼티가 존재하지 않으면 출력하지 않습니다.
