---
title: "ArbitraryCustomizer"
weight: 7
---

## customizerFields

`ArbitraryGenerator` 에서 객체를 생성하기 전에 객체를 구성하는 `FieldArbitraies` 안에 존재하는 `Arbitrary`를 제어합니다. 

## customizeFixture

`ArbitraryGenerator`에서 객체를 생성한 후 반환할 객체를 제어합니다.

### 생명주기

1. customizerFields
2. customizeFixture

## BuilderArbitraryCustomizer

오타 같은 휴먼 에러를 막기 위해 Builder 형태로 customizer를 사용할 수 있습니다.

### 생명주기

1. customizeFields
2. customizeBuilderFields
3. customizeBuilder
4. customizeFixture

## 예제

자세한 내용은 [여기]({{< relref "/docs/v0.3/examples/arbitrarycustomizer" >}})에서 확인하세요.
