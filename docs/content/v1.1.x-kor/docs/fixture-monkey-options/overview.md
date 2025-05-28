---
title: "옵션 개요"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "overview"
weight: 50
---

Fixture Monkey를 처음 사용할 때는 다양한 옵션들이 복잡하게 느껴질 수 있습니다. 이 가이드는 옵션들을 이해하고 어디서부터 시작해야 할지 파악하는 데 도움을 줄 것입니다.

## 옵션 vs ArbitraryBuilder API

Fixture Monkey에서 테스트 데이터를 설정하는 방법은 크게 두 가지가 있습니다:

1. **옵션 (Options)**
   - FixtureMonkey 인스턴스 생성 시 설정
   - 모든 테스트 데이터 생성에 적용되는 전역 규칙 정의
   - 재사용 가능한 설정

2. **ArbitraryBuilder API**
   - 개별 테스트 데이터 생성 시 설정
   - 특정 테스트 케이스에 필요한 일회성 설정
   - 더 세밀한 제어 가능

예시:

```java
// 옵션 사용 - 모든 Product 인스턴스에 적용
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultNotNull(true)  // 모든 필드를 non-null로 설정
    .register(Product.class, fm -> fm.giveMeBuilder(Product.class)
        .size("items", 3))  // items는 항상 3개
    .build();

// ArbitraryBuilder API 사용 - 특정 테스트에만 적용
Product specificProduct = fixtureMonkey.giveMeBuilder(Product.class)
    .set("name", "테스트 상품")  // 이 테스트에서만 이름 설정
    .set("price", 1000)      // 이 테스트에서만 가격 설정
    .sample();
```

## 왜 옵션을 사용해야 하나요?

옵션을 사용해야 하는 중요한 이유들이 있습니다:

### 1. 테스트 데이터의 일관성
- **문제**: 여러 테스트에서 동일한 규칙을 적용해야 함
  ```java
  // 옵션 없이 - 매 테스트마다 반복 설정 필요
  Product product1 = fixtureMonkey.giveMeBuilder(Product.class)
      .set("price", Arbitraries.longs().greaterThan(0))
      .sample();
  
  Product product2 = fixtureMonkey.giveMeBuilder(Product.class)
      .set("price", Arbitraries.longs().greaterThan(0))
      .sample();
  ```
  ```java
  // 옵션 사용 - 한 번 설정으로 모든 테스트에 적용
  FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
      .register(Product.class, fm -> fm.giveMeBuilder(Product.class)
          .set("price", Arbitraries.longs().greaterThan(0)))
      .build();
  
  Product product1 = fixtureMonkey.giveMeOne(Product.class);  // 자동으로 양수 가격
  Product product2 = fixtureMonkey.giveMeOne(Product.class);  // 자동으로 양수 가격
  ```

### 2. 테스트 유지보수성
- **문제**: 규칙이 변경될 때 모든 테스트를 수정해야 함
  ```java
  // 옵션 사용 - 한 곳에서 규칙 관리
  public class TestConfig {
      public static FixtureMonkey createFixtureMonkey() {
          return FixtureMonkey.builder()
              .defaultNotNull(true)
              .register(Product.class, fm -> fm.giveMeBuilder(Product.class)
                  .set("price", Arbitraries.longs().greaterThan(0))
                  .set("stock", Arbitraries.integers().greaterThan(0)))
              .register(Order.class, fm -> fm.giveMeBuilder(Order.class)
                  // 여기에 orderRules()에 해당하는 빌더 체이닝 추가
              )
              .build();
      }
  }
  ```

### 3. 도메인 규칙 적용
- **문제**: 비즈니스 규칙을 테스트 데이터에도 적용해야 함
  ```java
  // 옵션을 통한 도메인 규칙 적용
  FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
      .register(Order.class, fm -> fm.giveMeBuilder(Order.class)
          .thenApply((order, b) -> {
              b.set("totalAmount", order.getItems().stream()
                  .mapToInt(Item::getPrice)
                  .sum()
              );
          })
      )
      .build();
  ```

## 초보자를 위한 가장 일반적인 옵션

다음은 처음에 가장 필요할 것 같은 필수적인 옵션들입니다:

### 1. defaultNotNull 옵션 - null 값 방지하기

`defaultNotNull` 옵션은 명시적으로 null 가능으로 표시되지 않은 프로퍼티가 null이 아니도록 보장합니다(Java의 `@Nullable` 어노테이션이나 Kotlin의 `?`와 같은 표시가 없는 경우). 이 옵션은 테스트에서 null 관련 문제를 피하고 싶을 때 유용합니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void testDefaultNotNullOption() {
    // defaultNotNull 옵션이 적용된 FixtureMonkey 인스턴스 생성
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .defaultNotNull(true)  // 이 옵션은 @Nullable 어노테이션이 없는 프로퍼티가 null이 아니도록 보장합니다
        .build();
    
    // Product 생성 - 어노테이션이 없는 모든 프로퍼티가 null이 아님
    Product product = fixtureMonkey.giveMeOne(Product.class);
    
    assertThat(product.getProductName()).isNotNull();
    assertThat(product.getPrice()).isNotNull();
    assertThat(product.getCategory()).isNotNull();
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun testDefaultNotNullOption() {
    // defaultNotNull 옵션이 적용된 FixtureMonkey 인스턴스 생성
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .defaultNotNull(true)  // 이 옵션은 ? 표시가 없는 프로퍼티가 null이 아니도록 보장합니다
        .build()
    
    // Product 생성 - 모든 non-nullable 프로퍼티가 null이 아님
    val product = fixtureMonkey.giveMeOne<Product>()
    
    assertThat(product.productName).isNotNull()
    assertThat(product.price).isNotNull()
    assertThat(product.category).isNotNull()
}
{{< /tab >}}
{{< /tabpane>}}

### 2. javaTypeArbitraryGenerator 옵션 - 기본 타입 생성 제어하기

`javaTypeArbitraryGenerator` 옵션을 사용하면 기본 Java 타입(String, Integer 등)이 생성되는 방식을 사용자 정의할 수 있습니다. 이 옵션은 Fixture Monkey가 Jqwik 속성 기반 테스팅 라이브러리와 통합되는 JqwikPlugin을 통해 적용됩니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void testJavaTypeArbitraryGeneratorOption() {
    // 사용자 정의 문자열 생성이 적용된 FixtureMonkey 생성
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .plugin(
            new JqwikPlugin()  // JqwikPlugin은 Jqwik 라이브러리와의 통합을 제공합니다
                .javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
                    @Override
                    public StringArbitrary strings() {
                        // 알파벳 문자로만 구성된 문자열 생성하도록 사용자 정의
                        return Arbitraries.strings().alpha().ofLength(10);
                    }
                })
        )
        .build();
    
    // 생성된 모든 문자열은 길이가 10인 알파벳 문자열
    String generatedString = fixtureMonkey.giveMeOne(String.class);
    
    assertThat(generatedString).hasSize(10);
    assertThat(generatedString).matches("[a-zA-Z]+");
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun testJavaTypeArbitraryGeneratorOption() {
    // 사용자 정의 문자열 생성이 적용된 FixtureMonkey 생성
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(
            JqwikPlugin()  // JqwikPlugin은 Jqwik 라이브러리와의 통합을 제공합니다
                .javaTypeArbitraryGenerator(object : JavaTypeArbitraryGenerator {
                    override fun strings(): StringArbitrary {
                        // 알파벳 문자로만 구성된 문자열 생성하도록 사용자 정의
                        return Arbitraries.strings().alpha().ofLength(10)
                    }
                })
        )
        .build()
    
    // 생성된 모든 문자열은 길이가 10인 알파벳 문자열
    val generatedString = fixtureMonkey.giveMeOne<String>()
    
    assertThat(generatedString).hasSize(10)
    assertThat(generatedString).matches("[a-zA-Z]+")
}
{{< /tab >}}
{{< /tabpane>}}

### 3. register 옵션 - 타입별 기본 규칙 설정하기

`register` 옵션을 사용하면 특정 타입에 대한 기본 설정을 구성할 수 있습니다. 이는 여러 테스트에서 클래스에 일관된 요구 사항이 있을 때 유용합니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void testRegisterOption() {
    // Product 클래스에 대한 기본값이 설정된 FixtureMonkey 생성
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .register(
            Product.class,
            fm -> fm.giveMeBuilder(Product.class)
                .set("price", Arbitraries.longs().greaterThan(0))  // 양수 가격만 허용
                .set("category", "전자제품")  // 고정 카테고리
        )
        .build();
    
    // 모든 Product는 양수 가격과 '전자제품' 카테고리를 가짐
    Product product = fixtureMonkey.giveMeOne(Product.class);
    
    assertThat(product.getPrice()).isPositive();
    assertThat(product.getCategory()).isEqualTo("전자제품");
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun testRegisterOption() {
    // Product 클래스에 대한 기본값이 설정된 FixtureMonkey 생성
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .register(Product::class.java) { builder ->
            builder.giveMeBuilder<Product>()
                .set("price", Arbitraries.longs().greaterThan(0))  // 양수 가격만 허용
                .set("category", "전자제품")  // 고정 카테고리
        }
        .build()
    
    // 모든 Product는 양수 가격과 '전자제품' 카테고리를 가짐
    val product = fixtureMonkey.giveMeOne<Product>()
    
    assertThat(product.price).isPositive()
    assertThat(product.category).isEqualTo("전자제품")
}
{{< /tab >}}
{{< /tabpane>}}

### 4. plugin 옵션 - 기능 확장 추가하기

`plugin` 옵션을 사용하면 Fixture Monkey가 제공하는 다양한 플러그인의 추가 기능을 통합할 수 있습니다. 이 옵션은 특정 프레임워크나 라이브러리로 작업할 때 필수적입니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void testPluginOption() {
    // JSON 지원을 위한 Jackson 플러그인이 적용된 FixtureMonkey 생성
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .plugin(new JacksonPlugin())  // Jackson 어노테이션 지원 추가
        .build();
    
    // 이제 Jackson 어노테이션이 제대로 지원되는 객체를 생성할 수 있음
    JsonProduct product = fixtureMonkey.giveMeOne(JsonProduct.class);
    
    // JsonProduct의 Jackson 어노테이션이 존중됨
    // (예: @JsonProperty, @JsonIgnore 등)
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun testPluginOption() {
    // JSON 지원을 위한 Jackson 플러그인이 적용된 FixtureMonkey 생성
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())  // Kotlin 기능 지원
        .plugin(JacksonPlugin())  // Jackson 어노테이션 지원 추가
        .build()
    
    // 이제 Jackson 어노테이션이 제대로 지원되는 객체를 생성할 수 있음
    val product = fixtureMonkey.giveMeOne<JsonProduct>()
    
    // JsonProduct의 Jackson 어노테이션이 존중됨
    // (예: @JsonProperty, @JsonIgnore 등)
}
{{< /tab >}}
{{< /tabpane>}}

### 5. defaultArbitraryContainerInfoGenerator 옵션 - 컨테이너 크기 제어하기

`defaultArbitraryContainerInfoGenerator` 옵션을 사용하면 리스트, 세트, 맵과 같은 생성된 컨테이너 타입의 크기를 제어할 수 있습니다. 이 옵션은 테스트에서 특정 크기의 컨테이너가 필요할 때 유용합니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void testContainerSizeOption() {
    // 고정 컨테이너 크기가 적용된 FixtureMonkey 생성
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 3))  // 모든 컨테이너는 정확히 3개의 요소를 가짐
        .build();
    
    // 정확히 3개의 요소를 가진 리스트 생성
    List<String> stringList = fixtureMonkey.giveMeOne(new TypeReference<List<String>>() {});
    
    assertThat(stringList).hasSize(3);
    
    // 정확히 3개의 항목을 가진 맵 생성
    Map<Integer, String> map = fixtureMonkey.giveMeOne(new TypeReference<Map<Integer, String>>() {});
    
    assertThat(map).hasSize(3);
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun testContainerSizeOption() {
    // 고정 컨테이너 크기가 적용된 FixtureMonkey 생성
    val fixtureMonkey = FixtureMonkey.builder()
        .defaultArbitraryContainerInfoGenerator { context -> ArbitraryContainerInfo(3, 3) }  // 모든 컨테이너는 정확히 3개의 요소를 가짐
        .build()
    
    // 정확히 3개의 요소를 가진 리스트 생성
    val stringList: List<String> = fixtureMonkey.giveMeOne()
    
    assertThat(stringList).hasSize(3)
    
    // 정확히 3개의 항목을 가진 맵 생성
    val map: Map<Int, String> = fixtureMonkey.giveMeOne()
    
    assertThat(map).hasSize(3)
}
{{< /tab >}}
{{< /tabpane>}}

## 어떤 상황에 어떤 옵션을 사용해야 할까요?

다음은 어떤 옵션을 사용해야 할지 선택하는 데 도움이 되는 간단한 가이드입니다:

| 옵션 | 사용 시점 |
|--------|-------------|
| `defaultNotNull(true)` | 테스트 객체에 null 값이 없도록 보장하고 싶을 때 (명시적으로 null 가능한 프로퍼티 제외) |
| `javaTypeArbitraryGenerator` | 문자열이나 숫자와 같은 기본 타입이 생성되는 방식을 사용자 정의해야 할 때 |
| `register(Class, function)` | 특정 클래스에 일관된 기본값이나 제약 조건이 필요할 때 |
| `plugin(Plugin)` | 프레임워크(Jackson, Kotlin 등)에 대한 지원과 같은 추가 기능이 필요할 때 |
| `defaultArbitraryContainerInfoGenerator` | 생성된 컨테이너(리스트, 세트, 맵 등)의 크기를 제어해야 할 때 |

## 옵션의 적용 범위 이해하기

옵션을 사용할 때 알아야 할 중요한 점들이 있습니다:

1. **인스턴스 범위**
   - 옵션은 해당 FixtureMonkey 인스턴스에서만 적용됩니다
   - 여러 인스턴스를 만들어 다른 설정을 적용할 수 있습니다

```java
// 테스트용 설정
FixtureMonkey testFixture = FixtureMonkey.builder()
    .defaultNotNull(true)
    .build();

// 개발용 설정
FixtureMonkey devFixture = FixtureMonkey.builder()
    .defaultNotNull(false)
    .build();
```

2. **옵션 우선순위**
   - 구체적인 옵션이 일반적인 옵션보다 우선합니다
   - 나중에 설정된 옵션이 이전 옵션을 덮어씁니다

```java
FixtureMonkey fixture = FixtureMonkey.builder()
    .defaultNotNull(true)            // 모든 필드 non-null
    .register(Product.class, fm -> fm.giveMeBuilder(Product.class)
        .setNull("description"))     // Product의 description만 null 허용
    .build();
```

## 다음 단계

Fixture Monkey를 최대한 활용하기 위해 다음 학습 경로를 권장합니다:

1. **여기서 시작**: 개요(현재 페이지) - 옵션이 무엇이며 어떻게 구성되는지 이해하기
2. **초보자를 위한 다음 단계**: [초보자를 위한 필수 옵션](../essential-options-for-beginners) - 일상적인 테스트에 필요한 가장 일반적인 옵션 배우기
3. **개념 이해하기**: [옵션 개념](../concepts) - 옵션이 내부적으로 어떻게 작동하는지 더 깊이 이해하고 주요 용어 학습하기
4. **고급 기능**: [전문가를 위한 고급 옵션](../advanced-options-for-experts) - 복잡한 테스트 시나리오를 위한 옵션 탐색하기

초보자라면 우선 [초보자를 위한 필수 옵션](../essential-options-for-beginners) 문서부터 확인하시고, 필요에 따라 [숙련자를 위한 고급 옵션](../advanced-options-for-experts) 문서로 진행하세요.
