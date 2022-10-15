---
title: "LabMonkey"
weight: 1
---
Fixure Monkey 0.4.x 의 새로운 기능은 `LabMonkey` 클래스에서 제공합니다.

## 생성 방법
### 기본 옵션을 사용하는 LabMonkey 생성
```java
LabMonkey labMonkey = LabMonkey.create();
```

### 옵션을 추가하여 생성하는 LabMonkey 생성
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
	+ 옵션들...
    .build();
```

## 옵션
### addContainerType
```java
public LabMonkeyBuilder addContainerType(
		Class<?> type,
		ContainerPropertyGenerator containerPropertyGenerator,
		ArbitraryIntrospector containerArbitraryIntrospector,
		DecomposedContainerValueFactory decomposedContainerValueFactory
)
```
#### type
컨테이너 타입입니다.

#### containerPropertyGenerator
컨테이너 타입에서 자식 요소(child element)를 생성하는 방법을 정의합니다. 

#### containerArbitraryIntrospector
Arbitrary로 컨테이너 타입을 만드는 방법을 정의합니다.

#### decomposedContainerValueFactory
컨테이너 값을 분해 가능한 값으로 변환하여 반환하고 컨테이너의 크기를 반환합니다.

### addExceptGenerateClass
```java
public LabMonkeyBuilder addExceptGenerateClass(Class<?> type)
```
생성하지 않을 타입을 추가합니다.
### addExceptGenerateClasses
```java
public LabMonkeyBuilder addExceptGenerateClasses(Class<?>... types)
```
생성하지 않을 복수 개 타입을 추가합니다.
### addExceptGeneratePackage
```java
public LabMonkeyBuilder addExceptGeneratePackage(String exceptGeneratePackage)
```
생성하지 않을 패키지 이름을 추가합니다.
### addExceptGeneratePackages
```java
public LabMonkeyBuilder addExceptGeneratePackages(String... exceptGeneratePackages)
```
생성하지 않을 복수 개 패키지 이름을 추가합니다.

### arbitraryValidator
```java
public LabMonkeyBuilder arbitraryValidator(ArbitraryValidator arbitraryValidator)
```
유효하지 않은 객체를 검증할 `ArbitraryValidator`를 변경합니다. **기본 값 `DefaultArbitraryValidator`**

### defaultArbitraryContainerInfo
```java
public LabMonkeyBuilder defaultArbitraryContainerInfo(ArbitraryContainerInfo defaultArbitraryContainerInfo)
```
기본 컨테이너 크기를 정의합니다. **기본 값 new ArbitraryContainerInfo(0, 3, false)**

#### minSize
랜덤하게 생성할 컨테이너 최소 크기

#### maxSize
랜덤하게 생성할 컨테이너 최대 크기

#### manipulated
연산 적용 여부. 

true일 경우 컨테이너를 set하더라도 크기를 유지합니다.

false일 경우 컨테이너를 set하면 컨테이너 크기와 동일하게 설정합니다.


### defaultArbitraryContainerMaxSize
랜덤하게 생성할 컨테이너 최대 크기 **기본 값 3**

### defaultDecomposedContainerValueFactory
기본으로 등록한 컨테이너 타입을 제외한 다른 컨테이너 타입을 분해할 때 사용합니다.

### defaultNotNull
true로 설정할 경우 `@Nullable`한 필드가 항상 null이 되지 않습니다. **기본 값 false**

### defaultNullInjectGenerator
```java
public LabMonkeyBuilder defaultNullInjectGenerator(NullInjectGenerator nullInjectGenerator)
```

등록한 객체 외에 모든 객체에 사용할 null을 설정할 확률을 반환하는 `NullInjectGenerator`를 설정합니다.

### defaultObjectPropertyGenerator
```java
public LabMonkeyBuilder defaultObjectPropertyGenerator(ObjectPropertyGenerator objectPropertyGenerator)
```
객체를 정의하는 방법을 추가합니다. 

**기본 값 DefaultObjectPropertyGenerator**

### defaultPropertyGenerator
```java
public interface PropertyGenerator {
	Property generateRootProperty(AnnotatedType annotatedType);

	List<Property> generateObjectChildProperties(AnnotatedType annotatedType);

	Property generateElementProperty(
		Property containerProperty,
		AnnotatedType elementType,
		@Nullable Integer index,
		int sequence
	);

	Property generateMapEntryElementProperty(
		Property containerProperty,
		Property keyProperty,
		Property valueProperty
	);

	Property generateTupleLikeElementsProperty(
		Property containerProperty,
		List<Property> childProperties,
		@Nullable Integer index
	);
}
```

#### generateRootProperty
생성하려고 하는 최상위 타입을 나타내는 RootProperty를 반환합니다.

#### generateObjectChildProperties
하위 타입들을 어떤 Property 형태로 반환할지 결정합니다.

#### generateElementProperty
컨테이너 타입에 포함하는 요소를 어떤 Property 형태로 반환할지 결정합니다.

#### generateMapEntryElementProperty
항상 Key, Value를 가지는 MapEntry를 어떤 Property 형태로 반환할지 결정합니다.

#### generateTupleLikeElementsProperty 
n개의 제네릭 값을 가지는 tuple을 어떤 Property 형태로 반환할지 결정합니다.

**기본 값 DefaultPropertyGenerator**

### defaultPropertyNameResolver
```java
public interface PropertyNameResolver {
	PropertyNameResolver IDENTITY = new IdentityPropertyNameResolver();

	String resolve(Property property);
}
```
property의 이름을 결정하는 기본 `PropertyNameResolver`을 변경합니다.

### javaArbitraryResolver
```java
public interface JavaArbitraryResolver {
	default Arbitrary<String> strings(StringArbitrary stringArbitrary, ArbitraryGeneratorContext context) {
		return stringArbitrary;
	}

	default Arbitrary<Character> characters(CharacterArbitrary characterArbitrary, ArbitraryGeneratorContext context) {
		return characterArbitrary;
	}

	default Arbitrary<Short> shorts(ShortArbitrary shortArbitrary, ArbitraryGeneratorContext context) {
		return shortArbitrary;
	}

	default Arbitrary<Byte> bytes(ByteArbitrary byteArbitrary, ArbitraryGeneratorContext context) {
		return byteArbitrary;
	}

	default Arbitrary<Double> doubles(DoubleArbitrary doubleArbitrary, ArbitraryGeneratorContext context) {
		return doubleArbitrary;
	}

	default Arbitrary<Float> floats(FloatArbitrary floatArbitrary, ArbitraryGeneratorContext context) {
		return floatArbitrary;
	}

	default Arbitrary<Integer> integers(IntegerArbitrary integerArbitrary, ArbitraryGeneratorContext context) {
		return integerArbitrary;
	}

	default Arbitrary<Long> longs(LongArbitrary longArbitrary, ArbitraryGeneratorContext context) {
		return longArbitrary;
	}

	default Arbitrary<BigInteger> bigIntegers(
		BigIntegerArbitrary bigIntegerArbitrary,
		ArbitraryGeneratorContext context
	) {
		return bigIntegerArbitrary;
	}

	default Arbitrary<BigDecimal> bigDecimals(
		BigDecimalArbitrary bigDecimalArbitrary,
		ArbitraryGeneratorContext context
	) {
		return bigDecimalArbitrary;
	}
}
```

Java에서 기본적으로 제공하는 타입들이 어노테이션을 처리하는 방식을 변경합니다.

### javaTimeArbitraryResolver
```java
public interface JavaTimeArbitraryResolver {
	default Arbitrary<Calendar> calendars(CalendarArbitrary calendarArbitrary, ArbitraryGeneratorContext context) {
		return calendarArbitrary;
	}

	default Arbitrary<Date> dates(DateArbitrary dateArbitrary, ArbitraryGeneratorContext context) {
		return dateArbitrary;
	}

	default Arbitrary<Instant> instants(InstantArbitrary instantArbitrary, ArbitraryGeneratorContext context) {
		return instantArbitrary;
	}

	default Arbitrary<LocalDate> localDates(LocalDateArbitrary localDateArbitrary, ArbitraryGeneratorContext context) {
		return localDateArbitrary;
	}

	default Arbitrary<LocalDateTime> localDateTimes(
		LocalDateTimeArbitrary localDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return localDateTimeArbitrary;
	}

	default Arbitrary<LocalTime> localTimes(
		LocalTimeArbitrary localTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return localTimeArbitrary;
	}

	default Arbitrary<ZonedDateTime> zonedDateTimes(
		ZonedDateTimeArbitrary zonedDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return zonedDateTimeArbitrary;
	}

	default Arbitrary<MonthDay> monthDays(
		MonthDayArbitrary monthDayArbitrary,
		ArbitraryGeneratorContext context
	) {
		return monthDayArbitrary;
	}

	default Arbitrary<OffsetDateTime> offsetDateTimes(
		OffsetDateTimeArbitrary offsetDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return offsetDateTimeArbitrary;
	}

	default Arbitrary<OffsetTime> offsetTimes(
		OffsetTimeArbitrary offsetTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return offsetTimeArbitrary;
	}

	default Arbitrary<Period> periods(PeriodArbitrary periodArbitrary, ArbitraryGeneratorContext context) {
		return periodArbitrary;
	}

	default Arbitrary<Duration> durations(DurationArbitrary durationArbitrary, ArbitraryGeneratorContext context) {
		return durationArbitrary;
	}

	default Arbitrary<Year> years(YearArbitrary yearArbitrary, ArbitraryGeneratorContext context) {
		return yearArbitrary;
	}

	default Arbitrary<YearMonth> yearMonths(
		YearMonthArbitrary yearMonthArbitrary,
		ArbitraryGeneratorContext context
	) {
		return yearMonthArbitrary;
	}

	default Arbitrary<ZoneOffset> zoneOffsets(
		ZoneOffsetArbitrary zoneOffsetArbitrary,
		ArbitraryGeneratorContext context
	) {
		return zoneOffsetArbitrary;
	}
}
```
Java에서 기본적으로 제공하는 시간 타입들이 어노테이션을 처리하는 방식을 변경합니다.


### javaTypeArbitraryGenerator
```java
public interface JavaTypeArbitraryGenerator {

	default StringArbitrary strings() {
		return Arbitraries.strings();
	}

	default CharacterArbitrary characters() {
		return Arbitraries.chars();
	}

	default ShortArbitrary shorts() {
		return Arbitraries.shorts();
	}

	default ByteArbitrary bytes() {
		return Arbitraries.bytes();
	}

	default DoubleArbitrary doubles() {
		return Arbitraries.doubles();
	}

	default FloatArbitrary floats() {
		return Arbitraries.floats();
	}

	default IntegerArbitrary integers() {
		return Arbitraries.integers();
	}

	default LongArbitrary longs() {
		return Arbitraries.longs();
	}

	default BigIntegerArbitrary bigIntegers() {
		return Arbitraries.bigIntegers();
	}

	default BigDecimalArbitrary bigDecimals() {
		return Arbitraries.bigDecimals();
	}
}
```
Java에서 기본적으로 제공하는 타입들이 기본적으로 반환하는 값의 범위를 변경합니다.
모든 값은 `Arbitrary` 형태로 반환합니다.

### javaTimeTypeArbitraryGenerator
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

	default DateArbitrary dates() {
		Instant now = Instant.now();
		Date min = new Date(now.minus(365, ChronoUnit.DAYS).toEpochMilli());
		Date max = new Date(now.plus(365, ChronoUnit.DAYS).toEpochMilli());
		return Dates.datesAsDate()
			.between(min, max);
	}

	default InstantArbitrary instants() {
		Instant now = Instant.now();
		return DateTimes.instants()
			.between(
				now.minus(365, ChronoUnit.DAYS),
				now.plus(365, ChronoUnit.DAYS)
			);
	}

	default LocalDateArbitrary localDates() {
		LocalDate now = LocalDate.now();
		return Dates.dates()
			.between(
				now.minusDays(365),
				now.plusDays(365)
			);
	}

	default LocalDateTimeArbitrary localDateTimes() {
		LocalDateTime now = LocalDateTime.now();
		return DateTimes.dateTimes()
			.between(
				now.minusDays(365),
				now.plusDays(365)
			);
	}

	default LocalTimeArbitrary localTimes() {
		return Times.times();
	}

	default ZonedDateTimeArbitrary zonedDateTimes() {
		LocalDateTime now = LocalDateTime.now();
		return DateTimes.zonedDateTimes()
			.between(
				now.minusDays(365),
				now.plusDays(365)
			);
	}

	default MonthDayArbitrary monthDays() {
		return Dates.monthDays();
	}

	default OffsetDateTimeArbitrary offsetDateTimes() {
		LocalDateTime now = LocalDateTime.now();
		return DateTimes.offsetDateTimes()
			.between(
				now.minusDays(365),
				now.plusDays(365)
			);
	}

	default OffsetTimeArbitrary offsetTimes() {
		return Times.offsetTimes();
	}

	default PeriodArbitrary periods() {
		return Dates.periods()
			.between(
				Period.ofDays(-365),
				Period.ofDays(365)
			);
	}

	default DurationArbitrary durations() {
		return Times.durations()
			.between(
				Duration.ofDays(-365),
				Duration.ofDays(365)
			);
	}

	default YearArbitrary years() {
		return Dates.years()
			.between(
				Year.now().minusYears(10),
				Year.now().plusYears(10)
			);
	}

	default YearMonthArbitrary yearMonths() {
		return Dates.yearMonths()
			.yearBetween(
				Year.now().minusYears(10),
				Year.now().plusYears(10)
			);
	}

	default ZoneOffsetArbitrary zoneOffsets() {
		return Times.zoneOffsets();
	}
}
```
Java에서 기본적으로 제공하는 시간 타입들이 반환하는 기본 값을 변경합니다.
모든 값은 `Arbitrary` 형태로 반환합니다.

### manipulatorOptimizer
```java
public interface ManipulatorOptimizer {
	OptimizedManipulatorResult optimize(List<ArbitraryManipulator> manipulators);
}
```

입력한 연산을 정책에 따라 최적화하여 실행합니다.

**기본 값 NoneManipulatorOptimizer**

### monkeyExpressionFactory
```java
public interface MonkeyExpressionFactory {
	MonkeyExpression from(String expression);
}
```
**기본 값 ArbitraryExpressionFactory**


```java
public interface MonkeyExpression {
	NodeResolver toNodeResolver();
}
```
**기본 값 ArbitraryExpression**

표현식을 변경합니다.

### nullableContainer
Container 타입을 생성할 때 null 허용 여부를 변경합니다.

**기본 값 false**

### nullableElement
요소를 생성할 때 null 허용 여부를 변경합니다.

**기본 값 false**

### objectIntrospector
```java
public LabMonkeyBuilder objectIntrospector(ArbitraryIntrospector objectIntrospector)
```
객체를 생성하는 방법을 변경합니다.

**기본 값 BuilderArbitraryIntrospector**

### pushContainerIntrospector
```java
public LabMonkeyBuilder pushContainerIntrospector(ArbitraryIntrospector containerIntrospector)
```
컨테이너를 생성하는 방법을 변경합니다.

### plugin
연관이 있는 옵션을 플러그인으로 묶어 확장합니다.

### pushArbitraryContainerInfoGenerator
```java
	public LabMonkeyBuilder pushArbitraryContainerInfoGenerator(
		MatcherOperator<ArbitraryContainerInfoGenerator> arbitraryContainerInfoGenerator
    ) 
```

```java
public interface ArbitraryContainerInfoGenerator {
	ArbitraryContainerInfo generate(ContainerPropertyGeneratorContext context);
}
```
조건을 만족하는 Property는 설정한 컨테이너 크기대로 생성합니다.

### pushArbitraryCustomizer
```java
public LabMonkeyBuilder pushArbitraryCustomizer(MatcherOperator<FixtureCustomizer> arbitraryCustomizer)
```

```java
public interface FixtureCustomizer<T> {
	default void customizeProperties(ChildArbitraryContext childArbitraryContext) {
	}

	@Nullable
	T customizeFixture(@Nullable T object);
}
```

생성하려는 객체의 property가 특정 조건을 만족할 경우 객체를 구성하는 property를 변경하거나 객체를 변환하여 반환합니다.

##### customizerProperties
객체를 구성하는 property를 변경합니다.

##### customizeFixture
객체를 변환하여 반환합니다.

#### pushAssignableTypeArbitraryCustomizer
```java
public <T> LabMonkeyBuilder pushAssignableTypeArbitraryCustomizer(
		Class<T> type,
		FixtureCustomizer<? extends T> fixtureCustomizer
)
```
입력한 타입과 일치하거나 타입의 구현체 혹은 자식 객체일 경우 객체를 구성하는 property를 변경하거나 객체를 변환하여 반환합니다.

##### customizerProperties
객체를 구성하는 property를 변경합니다.

##### customizeFixture
객체를 변환하여 반환합니다.

#### pushExactTypeArbitraryCustomizer
```java
public <T> LabMonkeyBuilder pushExactTypeArbitraryCustomizer(
		Class<T> type,
		FixtureCustomizer<T> fixtureCustomizer
) 
```
입력한 타입과 일치하면 객체를 구성하는 property를 변경하거나 객체를 변환하여 반환합니다.

##### customizerProperties
객체를 구성하는 property를 변경합니다.

##### customizeFixture
객체를 변환하여 반환합니다.

### pushArbitraryIntrospector
```java
public LabMonkeyBuilder pushArbitraryIntrospector(
		MatcherOperator<ArbitraryIntrospector> arbitraryIntrospector
)
```

```java
public interface ArbitraryIntrospector {
	ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context);
}
```

조건에 해당하는 property는 설정한 `ArbitraryIntrospector`로 객체를 생성합니다.

#### pushAssignableTypeArbitraryIntrospector
```java
public LabMonkeyBuilder pushAssignableTypeArbitraryIntrospector(
	Class<?> type,
	ArbitraryIntrospector arbitraryIntrospector
) 
```

```java
public interface ArbitraryIntrospector {
	ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context);
}
```
입력한 타입과 일치하거나 타입의 구현체 혹은 자식 객체일 경우 설정한 `ArbitraryIntrospector`로 객체를 생성합니다.

#### pushExactTypeArbitraryIntrospector
```java
public LabMonkeyBuilder pushExactTypeArbitraryIntrospector(
	Class<?> type,
	ArbitraryIntrospector arbitraryIntrospector
) 
```

```java
public interface ArbitraryIntrospector {
	ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context);
}
```

입력한 타입과 일치하면 설정한 `ArbitraryIntrospector`로 객체를 생성합니다.

### pushContainerPropertyGenerator
```java
public LabMonkeyBuilder pushContainerPropertyGenerator(
		MatcherOperator<ContainerPropertyGenerator> containerPropertyGenerator
)
```
```java
public interface ContainerPropertyGenerator {
	ContainerProperty generate(ContainerPropertyGeneratorContext context);
}
```

조건에 해당하는 property는 입력한 `ContainerPropertyGenerator`을 사용해 컨테이너 정보를 생성합니다.

#### pushAssignableTypeContainerPropertyGenerator
```java
public LabMonkeyBuilder pushAssignableTypeContainerPropertyGenerator(
		Class<?> type,
		ContainerPropertyGenerator containerPropertyGenerator
)
```
```java
public interface ContainerPropertyGenerator {
	ContainerProperty generate(ContainerPropertyGeneratorContext context);
}
```

입력한 타입과 일치하거나 타입의 구현체 혹은 자식 객체일 경우 입력한 `ContainerPropertyGenerator`을 사용해 컨테이너 정보를 생성합니다.
#### pushExactTypeContainerPropertyGenerator
```java
public LabMonkeyBuilder pushExactTypeContainerPropertyGenerator(
		Class<?> type,
		ContainerPropertyGenerator containerPropertyGenerator
) 
```
```java
public interface ContainerPropertyGenerator {
	ContainerProperty generate(ContainerPropertyGeneratorContext context);
}
```

입력한 타입과 일치하면 입력한 `ContainerPropertyGenerator`을 사용해 컨테이너 정보를 생성합니다.

### pushObjectPropertyGenerator
```java
public LabMonkeyBuilder pushObjectPropertyGenerator(
		MatcherOperator<ObjectPropertyGenerator> arbitraryObjectPropertyGenerator
)
```
```java
public interface ObjectPropertyGenerator {
	ObjectProperty generate(ObjectPropertyGeneratorContext context);
}
```
조건에 해당하는 property는 입력한 `ObjectPropertyGenerator`을 사용해 객체 정보를 생성합니다.

#### pushAssignableTypeObjectPropertyGenerator
```java
public LabMonkeyBuilder pushAssignableTypeObjectPropertyGenerator(
		Class<?> type,
		ObjectPropertyGenerator objectPropertyGenerator
)
```
```java
public interface ObjectPropertyGenerator {
	ObjectProperty generate(ObjectPropertyGeneratorContext context);
}
```

입력한 타입과 일치하거나 타입의 구현체 혹은 자식 객체일 경우 입력한 `ObjectPropertyGenerator`을 사용해 객체 정보를 생성합니다.

#### pushExactTypeObjectPropertyGenerator
```java
public LabMonkeyBuilder pushExactTypeObjectPropertyGenerator(
		Class<?> type,
		ObjectPropertyGenerator objectPropertyGenerator
)
```
```java
public interface ObjectPropertyGenerator {
	ObjectProperty generate(ObjectPropertyGeneratorContext context);
}
```
입력한 타입과 일치하면 입력한 `ObjectPropertyGenerator`을 사용해 객체 정보를 생성합니다.

### pushPropertyNameResolver
```java
public LabMonkeyBuilder pushPropertyNameResolver(
		MatcherOperator<PropertyNameResolver> propertyNameResolver
)
```
```java
public interface PropertyNameResolver {
	PropertyNameResolver IDENTITY = new IdentityPropertyNameResolver();

	String resolve(Property property);
}
```

조건에 해당하는 property는 입력한 `propertyNameResolver`를 통해 property의 이름을 반환합니다.

#### pushAssignableTypePropertyNameResolver
```java
public LabMonkeyBuilder pushAssignableTypePropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
)
```
```java
public interface PropertyNameResolver {
	PropertyNameResolver IDENTITY = new IdentityPropertyNameResolver();

	String resolve(Property property);
}
```

입력한 타입과 일치하거나 타입의 구현체 혹은 자식 객체일 경우 입력한 `propertyNameResolver`를 통해 property의 이름을 반환합니다.

#### pushExactTypePropertyNameResolver
```java
public LabMonkeyBuilder pushExactTypePropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
)
```
```java
public interface PropertyNameResolver {
	PropertyNameResolver IDENTITY = new IdentityPropertyNameResolver();

	String resolve(Property property);
}
```

입력한 타입과 일치하면 입력한 `propertyNameResolver`를 통해 property의 이름을 반환합니다.

### pushExceptGenerateType
```java
public LabMonkeyBuilder pushExceptGenerateType(Matcher matcher)
```
```java
boolean match(Property property)
```

matcher를 만족하는 property는 생성하지 않습니다.

### pushNullInjectGenerator
```java
public LabMonkeyBuilder pushNullInjectGenerator(MatcherOperator<NullInjectGenerator> nullInjectGenerator)
```
```java
public interface NullInjectGenerator {
	double generate(ObjectPropertyGeneratorContext context);
}
```

조건에 해당하는 property는 입력한 `nullInjectGenerator`를 통해 객체의 null 생성 확률을 설정합니다.

#### pushAssignableTypeNullInjectGenerator
```java
public LabMonkeyBuilder pushAssignableTypeNullInjectGenerator(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
)
```
```java
public interface NullInjectGenerator {
	double generate(ObjectPropertyGeneratorContext context);
}
```

입력한 타입과 일치하거나 타입의 구현체 혹은 자식 객체일 경우 입력한 `nullInjectGenerator`를 통해 객체의 null 생성 확률을 설정합니다.
#### pushExactTypeNullInjectGenerator
```java
public LabMonkeyBuilder pushExactTypeNullInjectGenerator(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
)
```
```java
public interface NullInjectGenerator {
	double generate(ObjectPropertyGeneratorContext context);
}
```

입력한 타입과 일치하면 입력한 `nullInjectGenerator`를 통해 객체의 null 생성 확률을 설정합니다.

### register
```java
public LabMonkeyBuilder register(
		Class<?> type,
		Function<LabMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
)
```

입력한 타입과 일치하거나 타입의 구현체 혹은 자식 객체일 경우 입력한 `registeredArbitraryBuilder`를 기본 ArbitraryBuilder로 반환합니다.

#### registerAssignableType
```java
public LabMonkeyBuilder registerAssignableType(
		Class<?> type,
		Function<LabMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
)
```

입력한 타입과 일치하거나 타입의 구현체 혹은 자식 객체일 경우 입력한 `registeredArbitraryBuilder`를 기본 ArbitraryBuilder로 반환합니다.

#### registerExactType
```java
public LabMonkeyBuilder registerExactType(
		Class<?> type,
		Function<LabMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
)
```

입력한 타입과 일치하면 입력한 `registeredArbitraryBuilder`를 기본 ArbitraryBuilder로 반환합니다.

#### registerGroup
```java
public LabMonkeyBuilder registerGroup(Class<?>... arbitraryBuilderGroups)
```

arbitraryBuilderGroups에서 정의한 `public ArbitraryBuilder<TYPE> type(FixtureMonkey fixtureMonkey)` 형태의 메서드를 `register` 옵션에 추가합니다.

추가한 ArbitrarBuilder들은 모두 타입과 일치하거나 타입의 구현체 혹은 자식 객체일 경우에 기본 ArbitraryBuilder로 사용합니다.

### useExpressionStrictMode
```java
public LabMonkeyBuilder useExpressionStrictMode()
```
표현식과 일치하는 property가 존재하지 않을 경우 예외 발생하도록 설정합니다.
