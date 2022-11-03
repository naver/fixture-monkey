
---
title: "Release Notes"
weight: 6
---
### 0.4.2
* 생성자로 객체를 생성하는 ConstructorPropertiesArbitraryIntrospector 추가
* 팩토리 메서드로 객체를 생성하는 FactoryMethodIntrospector 추가
* nullableContainer 옵션이 적용안되는 문제 해결
* setInner, setLazy Exp 추가

### 0.4.1
* 자식 객체를 register한 경우 부모 객체에서 register한 setNull이 적용안되는 문제 해결
* BigDecimal, UUID와 같이 자바 기본 패키지에 존재하는 객체 생성 못하는 문제 해결
* KotlinPlugin을 추가하면 자바 객체를 생성 못하는 문제 해결

### 0.4.0
* Exp 추가
* 맵 연산 추가 `setInner`
* 옵션 변경
