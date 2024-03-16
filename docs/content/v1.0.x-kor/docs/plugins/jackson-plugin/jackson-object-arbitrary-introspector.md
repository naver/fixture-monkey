---
title: "JacksonObjectArbitraryIntrospector"
images: []
menu:
docs:
parent: "jackson-plugin"
identifier: "jackson-object-arbitrary-introspector"
weight: 72
---

## JacksonObjectArbitraryIntrospector
Jackson 플러그인이 추가되면 JacksonObjectArbitraryIntrospector가 기본 introspector로 지정됩니다.
주어진 클래스의 프로퍼티들을 생성해 맵에 추가하고 Jackson의 객체 매퍼를 사용하여 역직렬화합니다.

**예제 자바 클래스 :**
```java
@Value
public class Product {
    long id;

    String productName;

    long price;

    List<String> options;

    Instant createdAt;
}
```

**JacksonObjectArbitraryIntrospector 사용 :**
{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .plugin(new JacksonPlugin())
        .build();

    Product product = fixtureMonkey.giveMeOne(Product.class);
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:{{< fixture-monkey-version >}}")
testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")

@Test
fun test() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JacksonPlugin())
        .build();

    val product: Product = fixtureMonkey.giveMeOne()
}

{{< /tab >}}
{{< /tabpane>}}

{{< alert icon="💡" text="Kotlin 클래스를 생성할 때 JacksonObjectArbitraryIntrospector를 활용하려면, Kotlin 플러그인과 Jackson 플러그인을 둘 다 추가해야 합니다. 추가적으로, Kotlin 클래스의 직렬화 및 역직렬화를 위해 fasterxml jackson-module-kotlin도 의존성에 추가되어야 합니다." />}}

`JacksonObjectArbitraryIntrospector`은 Jackson을 기반으로 하기 때문에 범용성이 높다는 장점이 있습니다.
프로덕션 코드에 코틀린과 자바 클래스가 모두 존재하는 경우에 해당 introspector를 사용하는 것이 권장됩니다.

다만, Jackson을 사용한 역직렬화는 성능 면에서 다른 introspector들에 비해 효율적이지 않을 수 있습니다. Jackson의 역직렬화 과정이 상대적으로 더 많은 시간을 소비할 수 있어서 전체적인 실행 속도에 영향을 미칠 수 있습니다.
