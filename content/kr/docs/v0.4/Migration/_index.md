---
title: "마이그레이션 가이드"
weight: 4
---
{{< alert color="primary" title="Abstract">}}
Fixture Monkey 0.3에서 0.4로 마이그레이션 하는 방법을 소개합니다.
{{< /alert >}}

## 옵션

### addAnnotatedArbitraryGenerator
```java
public FixtureOptionsBuilder addAnnotatedArbitraryGenerator(Class<?> clazz, AnnotatedArbitraryGenerator<?> generator) 
```

```java
public interface AnnotatedArbitraryGenerator<T> {
	Arbitrary<T> generate(AnnotationSource annotationSource);
}
```

0.3에서 어노테이션을 적용한 타입 생성을 추가하거나 수정할 때 `addAnnotatedArbitraryGenerator` 옵션을 사용했습니다.

객체 생성과 어노테이션 적용을 동시에 하였기 때문에 객체 생성 범위만 변경하거나 어노테이션 적용만 변경하기 쉽지 않았습니다.

#### 0.4
0.4에서는 객체 생성과 어노테이션 적용을 분리하였습니다,

자바에서 제공하는 기본 객체 생성은 `JavaTypeArbitraryGenerator`를 사용합니다.

필요한 타입을 override하여 사용할 수 있습니다.

```java
public LabMonkeyBuilder javaTypeArbitraryGenerator(
    JavaTypeArbitraryGenerator javaTypeArbitraryGenerator
)
```

```java
public interface JavaTypeArbitraryGenerator {
	default StringArbitrary strings() {
		return Arbitraries.strings();
	}
    ...
}
```

자바에서 제공하는 기본 객체에 어노테이션을 적용할 때는 `JavaArbitraryResolver`를 사용합니다.

필요한 타입을 override하여 사용할 수 있습니다.

```java
public LabMonkeyBuilder javaArbitraryResolver(JavaArbitraryResolver javaArbitraryResolver)
```

```java
public interface JavaArbitraryResolver {
	default Arbitrary<String> strings(StringArbitrary stringArbitrary, ArbitraryGeneratorContext context) {
		return stringArbitrary;
	}
    ...
}
```


자바에서 제공하는 기본 시간/날짜 객체에 어노테이션을 적용할 때는 `JavaTimeTypeArbitraryGenerator`를 사용합니다.

필요한 타입을 override하여 사용할 수 있습니다.

```java
public LabMonkeyBuilder javaTimeTypeArbitraryGenerator(
		JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator
	)
```

```java
public interface JavaTimeTypeArbitraryGenerator {
	default CalendarArbitrary calendars() {
		Instant now = Instant.now();
		Calendar min = Calendar.getInstance();
		min.setTimeInMillis(now.minus(365, ChronoUnit.DAYS).toEpochMilli());
		Calendar max = Calendar.getInstance();
		max.setTimeInMillis(now.plus(365, ChronoUnit.DAYS).toEpochMilli());
		return Dates.datesAsCalendar()
			.between(min, max);
	}
    ...
}
```

그 외 타입은 `pushAssignableTypeArbitraryIntrospector` 옵션을 사용하면 됩니다.
```java
public LabMonkeyBuilder pushAssignableTypeArbitraryIntrospector(
		Class<?> type,
		ArbitraryIntrospector arbitraryIntrospector
) 
```

### defaultGenerator
```java
public FixtureMonkeyBuilder defaultGenerator(ArbitraryGenerator defaultCombiner)
```

0.3에서는 객체 생성 방식을 변경할 때 `defaultGenerator` 옵션을 사용했습니다.

#### 0.4
0.4에서는 `ArbitraryGenerator` 대신 `ArbitraryIntrospector`를 사용합니다. `objectIntrospector` 옵션을 사용합니다.

`objectIntrospector`는 특정 타입에 의존적인 생성 방식이 아닌 일반적인 타입 생성 방식을 정의하는 `ArbitraryIntrospector`를 사용합니다.

ex. `ConstructorPropertiesArbitraryIntrospector`, `BeanArbitraryIntrospector` 

```java
public LabMonkeyBuilder objectIntrospector(ArbitraryIntrospector objectIntrospector)
```

### putGenerator
0.3에서는 어노테이션 적용이 필요없는 타입 생성할 때 `putGenerator` 옵션을 사용했습니다.

```java
public FixtureMonkeyBuilder putGenerator(Class<?> type, ArbitraryGenerator generator)
```

#### 0.4
0.4에서는 `pushAssignableTypeArbitraryIntrospector`를 사용하면 됩니다.

```java
public LabMonkeyBuilder pushAssignableTypeArbitraryIntrospector(
	Class<?> type,
	ArbitraryIntrospector arbitraryIntrospector
)
```

### addInterfaceSupplier
```java
public <T> FixtureMonkeyBuilder addInterfaceSupplier(Class<T> clazz, InterfaceSupplier<T> interfaceSupplier)
```

0.3에서는 인터페이스를 생성할 때 `addIntrefaceSupplier` 옵션을 사용했습니다.

#### 0.4
0.4에서는 `pushAssignableTypeArbitraryIntrospector`를 사용하면 됩니다.

```java
public LabMonkeyBuilder pushAssignableTypeArbitraryIntrospector(
	Class<?> type,
	ArbitraryIntrospector arbitraryIntrospector
)
```

### null 옵션
#### nullableArbitraryEvaluator
```java
public FixtureOptionsBuilder nullableArbitraryEvaluator(NullableArbitraryEvaluator nullableArbitraryEvaluator)
```

```java
public interface NullableArbitraryEvaluator {
	default boolean isNullable(Field field) {
		return true;
	}
}
```

Kotlin 같이 필드에서 별도로 nullable 여부를 결정하는 경우 사용하는 인터페이스입니다.

```kotlin
class KotlinNullableArbitraryEvaluator : NullableArbitraryEvaluator {
    override fun isNullable(field: Field): Boolean {
        return field.kotlinProperty?.returnType?.isMarkedNullable ?: true
    }
}
```

#### nullInject
```java
public FixtureOptionsBuilder nullInject(double nullInject)
```

생성한 객체가 null일 확률을 설정합니다. 

#### nullableContainer
```java
public FixtureOptionsBuilder nullableContainer(boolean nullableContainer)
```

컨테이너 타입을 생성할 때 null을 허용할 것인지 설정합니다.

#### defaultNotNull
```java
public FixtureMonkeyBuilder defaultNotNull(boolean defaultNotNull)
```

기본 생성하는 객체는 null이 되지 않도록 설정합니다.
`@Nullable`로 마킹해도 null이 되지 않습니다.

#### 0.4
0.4에서 null 설정은 `NullInjectGenerator` 인터페이스를 사용합니다.
defaultNullInjectGenerator 옵션을 통해 기본으로 사용할 `NullInjectGenerator`를 변경할 수 있습니다.

DefaultNullInjectGenerator 객체를 생성할 때 위 모든 0.3 옵션들을 설정할 수 있습니다.

```java
public GenerateOptionsBuilder defaultNullInjectGenerator(NullInjectGenerator defaultNullInjectGenerator)
```

```java
public final class DefaultNullInjectGenerator implements NullInjectGenerator {
	private final double defaultNullInject;
	private final boolean nullableContainer;
	private final boolean defaultNotNull;

	private final boolean nullableElement;
	private final Set<String> nullableAnnotationTypes;
	private final Set<String> notNullAnnotationTypes;
}
```


### 정리
#### 타입 생성
##### 0.3
* `어노테이션 적용이 필요한 타입`, `필요없는 타입`, `인터페이스`가 다른 옵션을 사용했습니다.
* `어노테이션 적용이 필요한 타입 생성`에서는 타입 생성과 어노테이션 적용이 한 곳에서 이루어졌습니다. 

##### 0.4
* `자바에서 기본 제공하는 타입`과 `그 외 타입`이 다른 옵션을 사용합니다.
* `자바에서 기본 제공하는 타입`에서 타입 생성과 어노테이션 적용이 다른 인터페이스를 사용합니다. 

#### Null 옵션
##### 0.3
* `nullableArbitraryEvaluator`, `nullInject`, `nullableContainer`, `defaultNotNull` 옵션으로 null 설정을 합니다.

##### 0.4
* `defaultNullInjectGenerator` 옵션으로 null 설정합니다.

## Plugin
```java
public LabMonkeyBuilder plugin(Plugin plugin) {
		generateOptionsBuilder.plugin(plugin);
		return this;
}
```

```java
public interface Plugin {
	void accept(GenerateOptionsBuilder optionsBuilder);
}
```

서드파티 라이브러리와 간편한 연동을 위해 `Plugin` 인터페이스를 추가합니다.

서드파티 모듈 의존성 추가하고 plugin 옵션을 추가하면 됩니다.

plugin에서 중복으로 설정한 옵션이 있을 경우 가장 마지막에 추가한 plugin을 적용합니다.




