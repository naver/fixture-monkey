---
title: "기능"
images: []
menu:
  docs:
    parent: "datafaker-plugin"
    identifier: "datafaker-plugin-features"
weight: 101
---

Fixture Monkey에서 제공하는 DataFaker 플러그인을 사용하면 보다 현실적이고 의미 있는 테스트 데이터를 생성할 수 있습니다.
- 필드명을 기반으로 자동으로 실제적인 데이터를 생성합니다. (이름, 주소, 이메일 등)
- [DataFaker](https://www.datafaker.net/) 라이브러리를 활용하여 다양한 형태의 가짜 데이터를 제공합니다.
- 다국어 로케일을 지원하여 각 국가별 특성에 맞는 데이터를 생성할 수 있습니다.

{{< alert icon="💡" text="DataFaker 플러그인은 String 타입 필드의 이름을 분석하여 적절한 가짜 데이터를 자동으로 생성합니다." />}}

## 의존성
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-datafaker:{{< fixture-monkey-version >}}")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-datafaker</artifactId>
  <version>{{< fixture-monkey-version >}}</version>
  <scope>test</scope>
</dependency>
```

## 플러그인 설정
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new DataFakerPlugin())
    .build();
```

## 지원하는 필드 유형

DataFaker 플러그인은 필드명에 특정 키워드가 포함된 String 필드를 자동으로 인식하여 해당하는 가짜 데이터를 생성합니다.

### 이름 관련 필드
- `name`, `firstName`, `lastName`, `fullName`을 포함한 필드명
- 생성되는 데이터: 실제 사람 이름

```java
public class User {
    private String fullName;    // "John Doe"
    private String firstName;   // "Jane"
    private String lastName;    // "Smith"
}

User user = fixtureMonkey.giveMeOne(User.class);
```

### 주소 관련 필드
- `address`, `city`를 포함한 필드명
- 생성되는 데이터: 실제 주소 정보

```java
public class Address {
    private String homeAddress;  // "123 Main Street, Springfield"
    private String city;         // "New York"
}

Address address = fixtureMonkey.giveMeOne(Address.class);
```

### 이메일 관련 필드
- `email`을 포함한 필드명
- 생성되는 데이터: 유효한 형식의 이메일 주소

```java
public class Contact {
    private String email;        // "john.doe@example.com"
}

Contact contact = fixtureMonkey.giveMeOne(Contact.class);
```

### 전화번호 관련 필드
- `phone`, `phoneNumber`를 포함한 필드명
- 생성되는 데이터: 유효한 형식의 전화번호

```java
public class Contact {
    private String phoneNumber;  // "555-123-4567"
}

Contact contact = fixtureMonkey.giveMeOne(Contact.class);
```

### 금융 관련 필드
- `creditCard`를 포함한 필드명
- 생성되는 데이터: 유효한 형식의 신용카드 번호

```java
public class Payment {
    private String creditCard;   // "4532-1234-5678-9012"
}

Payment payment = fixtureMonkey.giveMeOne(Payment.class);
```

## 직접 사용

### 직접 DataFaker 임의 값 생성하기

플러그인 없이도 DataFaker의 임의 값 생성기를 직접 사용할 수 있습니다:

```java
// 이름 생성
String fullName = DataFakerStringArbitrary.name().fullName();
String firstName = DataFakerStringArbitrary.name().firstName();
String lastName = DataFakerStringArbitrary.name().lastName();

// 주소 생성
String city = DataFakerStringArbitrary.address().city();
String streetName = DataFakerStringArbitrary.address().streetName();
String streetAddress = DataFakerStringArbitrary.address().streetAddress();
String fullAddress = DataFakerStringArbitrary.address().fullAddress();
String zipCode = DataFakerStringArbitrary.address().zipCode();
String state = DataFakerStringArbitrary.address().state();
String country = DataFakerStringArbitrary.address().country();

// 인터넷 관련 생성
String email = DataFakerStringArbitrary.internet().emailAddress();
String url = DataFakerStringArbitrary.internet().url();
String domainName = DataFakerStringArbitrary.internet().domainName();
String password = DataFakerStringArbitrary.internet().password();
String ipV4 = DataFakerStringArbitrary.internet().ipV4Address();
String ipV6 = DataFakerStringArbitrary.internet().ipV6Address();

// 전화번호 생성
String phone = DataFakerStringArbitrary.phoneNumber().phoneNumber();
String cellPhone = DataFakerStringArbitrary.phoneNumber().cellPhone();
String extension = DataFakerStringArbitrary.phoneNumber().extension();

// 금융 관련 생성
String creditCard = DataFakerStringArbitrary.finance().creditCard();
String iban = DataFakerStringArbitrary.finance().iban();
String bic = DataFakerStringArbitrary.finance().bic();
```

### 로케일 지정

특정 로케일에 맞는 데이터를 생성하려면:

```java
// 한국어 로케일로 이름 생성
String koreanName = DataFakerStringArbitrary.name(Locale.KOREAN).fullName();

// 일본어 로케일로 주소 생성
String japaneseAddress = DataFakerStringArbitrary.address(Locale.JAPANESE).city();
```

## 실제 사용 예제

```java
public class UserProfile {
    private String userName;
    private String email;
    private String homeAddress;
    private String phoneNumber;
    private String creditCard;
    private int age;              // DataFaker가 적용되지 않음 (String이 아님)
    private String description;   // DataFaker가 적용되지 않음 (키워드 매칭 안됨)
}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new DataFakerPlugin())
    .build();

UserProfile profile = fixtureMonkey.giveMeOne(UserProfile.class);
// userName: "Michael Johnson"
// email: "sarah.wilson@example.org"  
// homeAddress: "456 Oak Avenue, Boston"
// phoneNumber: "555-987-6543"
// creditCard: "4532-8765-4321-0987"
// age: 임의의 정수 (기본 생성기 사용)
// description: 임의의 문자열 (기본 생성기 사용)
```

{{< alert icon="⚠️" text="DataFaker 플러그인은 필드명에 특정 키워드가 포함된 String 타입 필드에만 적용됩니다. 다른 타입의 필드나 키워드가 매칭되지 않는 필드는 기본 생성기를 사용합니다." />}}