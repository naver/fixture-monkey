---
title: "ObjectProperty"
weight: 3
---
생성하려는 객체 정보를 가지고 있습니다. 불변합니다.

### property
생성하려는 Property입니다. 필요한 경우 생성하려는 Property를 확장할 수 있습니다.

### propertyNameResolver
Property에서 이름을 설정하는 방법입니다.

### nullInject
생성하려는 객체가 null이 될 확률입니다.

### elementIndex
생성하려는 객체가 컨테이너 타입의 요소일 경우 몇 번째 요소인지를 나타냅니다.

컨테이너 타입의 요소가 아닌 경우 null이 됩니다.

### childProperties
생성하려는 Property를 구성하는 자식 Property입니다. 
