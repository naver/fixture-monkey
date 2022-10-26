
---
title: "FAQ"
linkTitle: "FAQ"
weight: 6
---

## 생성한 문자열이 이상합니다
기본으로 생성하는 문자열은 아스키 코드에 해당하는 문자입니다. 아스키 코드에는 `control block`이라는 출력할 수 없는 문자를 포함하고 있습니다. 

유효한 문자만 생성하고 싶다면 `AnnotatedArbitraryGenerator`를 재정의해야 합니다.
자세한 방법은 [예제]({{< relref "/docs/v0.3/examples/annotatedarbitrarygenerator#override-string-to-generate-printable-characters" >}})를 참고해주세요.

## 객체 생성을 실패합니다
객체 생성은 `ArbitraryGenerator`에서 하고 있습니다. 초기 설정으로 `BeanArbitraryGenerator`가 설정돼있습니다.
생성하고자 하는 객체가 지정한 `ArbitraryGenerator`에 적합하지 않습니다.
[ArbitraryGenerator]({{< relref "/docs/v0.3/features/arbitrarygenerator">}})를 한 번 확인해주세요.
만약 원인을 모르겠다면 이슈에 실행 환경과 실행 결과를 올려주세요.
