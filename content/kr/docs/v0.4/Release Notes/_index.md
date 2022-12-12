
---
title: "Release Notes"
weight: 6
---
### 0.4.9
* 순환 참조인 객체를 생성할 수 있습니다.

### 0.4.8
* `ExpressionSpec`을 `set`할 수 있습니다.
* `ConstructorPropertiesIntrospector`에서 파라미터가 없는 생성자도 생성합니다.
* `FactoryMethodArbitraryIntrospector`를 deprecate 합니다.
* `PropertyDescriptorProperty`는 getter와 setter 모두 있는 경우에만 생성합니다.
* `InnerSpec`에서 맵을 제어하는 다양한 기능을 추가합니다.

### 0.4.7
* 인터페이스를 입력했을 때 생성할 구현체를 간단하게 설정하는 옵션을 추가합니다.

### 0.4.6
* 인터페이스를 set하면 입력한 값 그대로 적용하도록 수정합니다.
* Set과 Map의 요소를 생성할 때 중첩이 되도 유일하게 생성하도록 수정합니다.
* 맵을 제어하는 InnerSpec 일부 연산이 entry를 생성하는 문제를 수정합니다.

### 0.4.5
* Map이 중첩된 경우에 entry를 유니크하게 생성하지 않는 문제를 해결합니다.

### 0.4.4
* EnumSet, EnumMap을 생성할 때 크기가 size 연산을 적용하지 않는다면 enum 크기를 넘지 않습니다.

### 0.4.3
#### LabMonkey
* Javax.validation에서 숫자 타입을 생성할 때 min, max값을 타입에 맞게 제한합니다.
* generic이 없는 컨테이너를 생성할 수 있습니다.
* `JsonNode`를 생성할 수 있습니다.
* `@Pattern`과 `@NotBlank`를 설정한 경우 적용이 안되는 문제를 해결합니다.
* 코틀린 사용자를 위해 `fixture-monkey-starter-kotlin` 모듈을 추가합니다.
* Map과 Set에서 효율적으로 유일한 요소를 생성하게 수정합니다.
* 코틀린 플러그인을 추가했을 때 부모 객체의 필드를 못 받아오는 문제를 해결합니다.

#### FixtureMonkey
* generator를 변경해서 생성했을 때 generator에 해당하는 propertyNameResolver가 동작안하는 문제를 해결합니다.

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
