---
title: "조건을 만족하는 랜덤 테스트 데이터 만들기"
weight: 43
menu:
docs:
parent: "customizing-objects"
identifier: "arbitrary"
---

## 이 문서에서 배우는 내용
- 랜덤하지만 규칙을 따르는 테스트 데이터 만들기
- 숫자 범위, 문자열 패턴, 값 목록 등의 제약조건 설정 방법
- 고정 값 대신 랜덤 값을 사용해야 하는 상황과 이유

## 랜덤 테스트 데이터 소개
테스트에서 항상 **고정된 값**만 사용하는 것은 충분하지 않을 수 있습니다. 다음과 같은 상황에서는 랜덤 값이 필요합니다:
- 단일 값이 아닌 유효한 입력값 범위로 테스트
- 테스트가 실행될 때마다 다른 테스트 데이터 사용
- 비즈니스 규칙을 따르는 현실적이지만 다양한 데이터

예를 들어, 다음과 같은 테스트 상황에서 유용합니다:
- 나이 검증: 18-65세 사이의 랜덤 나이 생성
- 사용자명 검증: 특정 패턴을 따르는 랜덤 문자열 생성
- 결제 처리: 특정 범위 내의 다양한 금액 생성

## Arbitrary 이해하기
Fixture Monkey에서는 규칙을 따르는 랜덤 값을 만들기 위해 `Arbitrary`를 사용합니다. `Arbitrary`는 **규칙이 있는 값 생성기**라고 생각하면 됩니다.

> **쉽게 말하면:** Arbitrary는 랜덤 값을 생성하는 기계와 같지만, 여러분이 정한 규칙을 따르는 값만 생성합니다.

## 단계별 랜덤 값 생성 가이드

### 1. 기본 사용법: 간단한 범위 설정

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 20-30세 사이의 회원 생성
Member member = fixtureMonkey.giveMeBuilder(Member.class)
    .set("age", Arbitraries.integers().between(20, 30))  // 20-30 사이 랜덤 나이
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 20-30세 사이의 회원 생성
val member = fixtureMonkey.giveMeBuilder<Member>()
    .setExp(Member::age, Arbitraries.integers().between(20, 30))  // 20-30 사이 랜덤 나이
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### 2. 텍스트 다루기: 문자열 패턴

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 유효한 사용자명을 가진 사용자 생성 (소문자, 5-10자)
User user = fixtureMonkey.giveMeBuilder(User.class)
    .set("username", Arbitraries.strings()
        .withCharRange('a', 'z')  // 소문자만 사용
        .ofMinLength(5)           // 최소 5자
        .ofMaxLength(10))         // 최대 10자
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 유효한 사용자명을 가진 사용자 생성 (소문자, 5-10자)
val user = fixtureMonkey.giveMeBuilder<User>()
    .setExp(User::username, Arbitraries.strings()
        .withCharRange('a', 'z')  // 소문자만 사용
        .ofMinLength(5)           // 최소 5자
        .ofMaxLength(10))         // 최대 10자
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### 3. 유효한 옵션에서 선택하기

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 유효한 상태를 가진 주문 생성
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .set("status", Arbitraries.of(  // 이 값들 중 하나를 랜덤하게 선택
        OrderStatus.PENDING,
        OrderStatus.PROCESSING,
        OrderStatus.SHIPPED))
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 유효한 상태를 가진 주문 생성
val order = fixtureMonkey.giveMeBuilder<Order>()
    .setExp(Order::status, Arbitraries.of(  // 이 값들 중 하나를 랜덤하게 선택
        OrderStatus.PENDING,
        OrderStatus.PROCESSING,
        OrderStatus.SHIPPED))
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### 4. 여러 제약조건 결합하기

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 다양한 제약조건을 가진 상품 생성
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("id", Arbitraries.longs().greaterOrEqual(1000))  // ID는 최소 1000 이상
    .set("name", Arbitraries.strings().withCharRange('a', 'z').ofMaxLength(10))  // 이름은 최대 10자
    .set("price", Arbitraries.bigDecimals()
        .between(BigDecimal.valueOf(10.0), BigDecimal.valueOf(1000.0)))  // 가격은 10-1000 사이
    .set("category", Arbitraries.of("전자제품", "의류", "도서"))  // 이 카테고리 중 하나
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 다양한 제약조건을 가진 상품 생성
val product = fixtureMonkey.giveMeBuilder<Product>()
    .setExp(Product::id, Arbitraries.longs().greaterOrEqual(1000))  // ID는 최소 1000 이상
    .setExp(Product::name, Arbitraries.strings().withCharRange('a', 'z').ofMaxLength(10))  // 이름은 최대 10자
    .setExp(Product::price, Arbitraries.bigDecimals()
        .between(BigDecimal.valueOf(10.0), BigDecimal.valueOf(1000.0)))  // 가격은 10-1000 사이
    .setExp(Product::category, Arbitraries.of("전자제품", "의류", "도서"))  // 이 카테고리 중 하나
    .sample()
{{< /tab >}}
{{< /tabpane>}}

## 실제 사례: 나이 검증 테스트

성인 회원(18세 이상)만 가입할 수 있고 노인(65세 이상)은 할인을 받는 서비스를 테스트한다고 가정해 봅시다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void 성인_회원_가입_테스트() {
    // 50명의 랜덤 성인 회원으로 테스트
    for (int i = 0; i < 50; i++) {
        Member member = fixtureMonkey.giveMeBuilder(Member.class)
            .set("age", Arbitraries.integers().between(18, 100))  // 성인만
            .sample();
            
        boolean isSenior = member.getAge() >= 65;
        
        // 다양한 나이로 가입 로직 테스트
        MembershipResponse response = membershipService.register(member);
        
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.hasDiscount()).isEqualTo(isSenior);  // 노인은 할인 받음
    }
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun 성인_회원_가입_테스트() {
    // 50명의 랜덤 성인 회원으로 테스트
    repeat(50) {
        val member = fixtureMonkey.giveMeBuilder<Member>()
            .setExp(Member::age, Arbitraries.integers().between(18, 100))  // 성인만
            .sample()
            
        val isSenior = member.age >= 65
        
        // 다양한 나이로 가입 로직 테스트
        val response = membershipService.register(member)
        
        assertThat(response.isSuccess).isTrue()
        assertThat(response.hasDiscount).isEqualTo(isSenior)  // 노인은 할인 받음
    }
}
{{< /tab >}}
{{< /tabpane>}}

## 자주 사용하는 Arbitrary 메서드

| 메서드 | 용도 | 예시 |
|--------|------|------|
| `between(min, max)` | 범위 내 값 | `Arbitraries.integers().between(1, 100)` |
| `greaterOrEqual(min)` | 최소값 이상 | `Arbitraries.longs().greaterOrEqual(1000)` |
| `lessOrEqual(max)` | 최대값 이하 | `Arbitraries.doubles().lessOrEqual(99.9)` |
| `ofMaxLength(max)` | 최대 길이 문자열 | `Arbitraries.strings().ofMaxLength(10)` |
| `withCharRange(from, to)` | 문자 범위 설정 | `Arbitraries.strings().withCharRange('a', 'z')` |
| `of(values...)` | 옵션 중 선택 | `Arbitraries.of("빨강", "초록", "파랑")` |

## 자주 묻는 질문

### 고정 값 대신 Arbitrary를 사용해야 하는 경우는 언제인가요?

다음과 같은 경우에 Arbitrary를 사용하세요:
- 단일 값이 아닌 다양한 입력으로 테스트하고 싶을 때
- 정확한 값보다는 규칙을 따르는 값이 필요할 때
- 자동으로 엣지 케이스를 발견하고 싶을 때
- 다양한 유효한 입력으로 테스트해야 할 때

### 랜덤 값을 사용하면 테스트가 불안정하지 않을까요?

값은 랜덤이지만 여러분이 정의한 규칙을 따르기 때문에 다음과 같은 이점이 있습니다:
- 특정 값에서만 나타나는 버그 발견 가능
- 유효한 입력 전체 범위에서 코드가 작동하는지 확인
- 예상치 못한 엣지 케이스 발견

테스트가 실패한 경우 Fixture Monkey의 `@Seed` 어노테이션을 사용하여 재현 가능하게 만들 수 있습니다:

```java
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed;
import com.navercorp.fixturemonkey.junit.jupiter.extension.FixtureMonkeySeedExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(FixtureMonkeySeedExtension.class)
class MembershipTest {
    @Test
    @Seed(123L)  // 예측 가능한 랜덤 값을 위한 특정 시드 사용
    void 성인_회원만_가능한_테스트() {
        Member member = fixtureMonkey.giveMeBuilder(Member.class)
            .set("age", Arbitraries.integers().between(18, 100))
            .sample();
            
        // 테스트 로직
        assertThat(membershipService.isEligible(member)).isTrue();
    }
}
```

`@Seed` 어노테이션을 사용하면 Fixture Monkey는 지정된 시드 값을 사용하여 테스트가 실행될 때마다 동일한 "랜덤" 값을 생성합니다. 이렇게 하면 랜덤 데이터를 사용하는 테스트를 완전히 재현 가능하게 만들 수 있습니다.

`FixtureMonkeySeedExtension`의 가장 유용한 기능 중 하나는 테스트가 실패할 때 자동으로 시드 값을 로그에 출력한다는 것입니다:

```
Test Method [MembershipTest#성인_회원만_가능한_테스트] failed with seed: 42
```

이렇게 출력된 시드 값을 `@Seed` 어노테이션에 추가하면 실패한 테스트 상황을 일관되게 재현할 수 있습니다.

### setPostCondition()과 어떻게 다른가요?

- `setPostCondition()`은 임의의 값을 생성한 후 조건에 맞는지 확인합니다
- `Arbitrary`는 조건을 만족하는 값을 직접 생성합니다

생성된 값에 대한 더 많은 제어가 필요하거나, `setPostCondition()`이 많은 유효하지 않은 값을 폐기해야 해서 너무 느릴 때는 `Arbitrary`를 사용하세요.

## 추가 자료

모든 Arbitrary 유형과 메서드에 대한 자세한 내용은 [Jqwik 사용자 가이드](https://jqwik.net/docs/current/user-guide.html#static-arbitraries-methods)를 참조하세요.
