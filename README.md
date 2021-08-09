# Fixture Monkey

### "Write once, Test anywhere"

Enjoy your test with Fixture Monkey.

You can write countless tests including edge cases by `only one fixture`.

See the [wiki](../../wiki) for further details and documentation.

## Example

```java

@Data   // lombok getter, setter
public class Order {
    @NotNull
    private Long id;

    @NotBlank
    private String orderNo;

    @Size(min = 2, max = 10)
    private String productName;

    @Min(1)
    @Max(100)
    private int quantity;

    @Min(0)
    private long price;

    @Size(max = 3)
    private List<@NotBlank @Size(max = 10) String> items = new ArrayList<>();

    @PastOrPresent
    private Instant orderedAt;

    @Email
    private String sellerEmail;
}

@Test
void test() {
    // given
    FixtureMonkey sut = FixtureMonkey.builder().build();

    // when
    Order actual = sut.giveMeOne(Order.class);

    // then
    then(actual.getId()).isNotNull();
}
```

## Requirements

* JDK 1.8 or higher

## Install

### Gradle

```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey:1.0.0-SNAPSHOT")
```

### Maven

```xml

<dependency>
    <groupId>com.navercorp.fixturemonkey</groupId>
    <artifactId>fixture-monkey</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Submodule

* fixture-monkey-jackson
* fixture-monkey-kotlin
* fixture-monkey-autoparams
* fixture-monkey-mockito

## License

```
Copyright 2021-present NAVER Corp.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
