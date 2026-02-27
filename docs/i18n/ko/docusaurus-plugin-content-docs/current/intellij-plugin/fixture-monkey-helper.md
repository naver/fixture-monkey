---
title: "Fixture Monkey Helper"
sidebar_position: 91
---


[Fixture Monkey Helper](https://plugins.jetbrains.com/plugin/19589-fixture-monkey-helper)는 IntelliJ IDE에서 Fixture Monkey를 사용할 수 있도록 도와주는 IntelliJ 플러그인입니다.

문자열 표현식 및 Kotlin DSL 표현식을 더 쉽게 사용할 수 있는 몇 가지 기능을 제공하며, 어색한 코드를 감지하고 수정하기 위한 일부 IntelliJ 검사 기능도 제공합니다.

:::danger
이 플러그인은 현재 Java 소스 코드 및 Kotlin 테스트 코드 내에서만 작동합니다. 기능 확장이 진행 중입니다.
:::

### 기능

- **Fixture Monkey 표현식 지원**
    - 원활한 변환: 문자열 표현식을 Fixture Monkey가 제공하는 Kotlin DSL로 변환하여 ArbitraryBuilder에 사용할 수 있습니다.
    - 표현식 유효성 검사: 실행 전에 문자열 표현식의 정확성을 확인합니다.
    - 직관적인 자동 완성: 입력시 관련된 자동 완성 제안을 통해 코딩 속도를 높입니다.
    - 간편한 탐색: 코드베이스 내의 필드 참조로 바로 이동합니다.

- **Fixture Monkey Kotlin DSL 기능 향상**
    - 양방향 변환: Kotlin DSL과 Fixture Monkey 문자열 표현식 간에 쉽게 전환합니다.
        - 실시간 양방향 변환 지원 (Beta)
    - 코드 접기: DSL 표현식을 한 줄로 축소하여 뷰를 간소화합니다.
    - 람다 표현식 생성기: 생성된 람다 표현식을 사용하여 fixture 명세를 쉽게 작성합니다.
    - 람다에서 DSL 변환: 복잡한 람다 표현식을 읽기 쉽고 유지보수 가능한 Fixture Monkey Kotlin DSL로 변환합니다.

- **검사**
    - Fixture Monkey 팩토리 매소드에서 매소드 인자로 전달된 타입 정보를 제네릭 타입 인자로 변경합니다.
    - 가능한 경우 Fixture Monkey 팩토리 메소드에서 제네릭 타입 인자를 변수 타입으로 변경합니다.

- **Fixture Monkey 프로퍼티 개요 도구 창 (Alpha)**
    - 이 도구 창을 통해 ArbitraryBuilder에 등록된 모든 프로퍼티들을 한눈에 볼 수 있습니다. 이는 트리 형식으로 제공됩니다.

