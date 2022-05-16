
---
title: "Release Notes"
linkTitle: "Release Notes"
weight: 8
---
### 0.3.4
* 람다식을 사용하여 필드 값을 Lazy하게 설정할 수 있는 `setLazy`를 지원합니다.
* JDK 8에서 패키지를 명시하지 않은 클래스를 생성하지 못하는 버그를 수정합니다.

### 0.3.3
  * `addAnnotatedArbitraryGenerator`를 사용할 때 생기는 warning을 제거합니다. 
  * `@Size`의 max 값을 설정하지 않았을 경우의 OOM을 해결합니다.

### 0.3.2
* 연산 set에 파라미터로 ExpressionSpec을 지원합니다.
* QueueBuilder를 추가하여 큐 구현체를 생성할 수 있습니다.

### 0.3.1
* Add Fixture Monkey Starter module
* Fix ArbitraryBuilder generated Arbitrary not validated by validator
* Fix not copy valid only
* Remove useless testing validator debug log

check out details here in [Milestone](https://github.com/naver/fixture-monkey/pulls?q=is%3Apr+is%3Aclosed+milestone%3A0.3.1)
### 0.3.0
The first release published on maven central.
