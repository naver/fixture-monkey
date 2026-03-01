---
title: "Features"
sidebar_position: 101
---


The DataFaker plugin provided by Fixture Monkey allows you to generate more realistic and meaningful test data automatically.
- Automatically generates realistic data based on field names (names, addresses, emails, etc.)
- Leverages the [DataFaker](https://www.datafaker.net/) library to provide various types of fake data
- Supports multiple locales to generate country-specific data

:::tip
The DataFaker plugin analyzes String field names and automatically generates appropriate fake data based on keyword matching.
:::

## Dependencies
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-datafaker:1.1.15")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-datafaker</artifactId>
  <version>1.1.15</version>
  <scope>test</scope>
</dependency>
```

## Plugin Setup
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new DataFakerPlugin())
    .build();
```

## Supported Field Types

The DataFaker plugin automatically recognizes String fields containing specific keywords and generates corresponding fake data.

### Name Fields
- Field names containing: `name`, `firstName`, `lastName`, `fullName`
- Generated data: Realistic person names

```java
public class User {
    private String fullName;    // "John Doe"
    private String firstName;   // "Jane"
    private String lastName;    // "Smith"
}

User user = fixtureMonkey.giveMeOne(User.class);
```

### Address Fields
- Field names containing: `address`, `city`
- Generated data: Realistic address information

```java
public class Address {
    private String homeAddress;  // "123 Main Street, Springfield"
    private String city;         // "New York"
}

Address address = fixtureMonkey.giveMeOne(Address.class);
```

### Email Fields
- Field names containing: `email`
- Generated data: Valid email addresses

```java
public class Contact {
    private String email;        // "john.doe@example.com"
}

Contact contact = fixtureMonkey.giveMeOne(Contact.class);
```

### Phone Number Fields
- Field names containing: `phone`, `phoneNumber`
- Generated data: Valid phone numbers

```java
public class Contact {
    private String phoneNumber;  // "555-123-4567"
}

Contact contact = fixtureMonkey.giveMeOne(Contact.class);
```

### Finance Fields
- Field names containing: `creditCard`
- Generated data: Valid credit card numbers

```java
public class Payment {
    private String creditCard;   // "4532-1234-5678-9012"
}

Payment payment = fixtureMonkey.giveMeOne(Payment.class);
```

## Direct Usage

### Direct DataFaker Arbitrary Generation

You can directly use DataFaker arbitrary generators without the plugin:

```java
// Name generation
String fullName = DataFakerStringArbitrary.name().fullName();
String firstName = DataFakerStringArbitrary.name().firstName();
String lastName = DataFakerStringArbitrary.name().lastName();

// Address generation
String city = DataFakerStringArbitrary.address().city();
String streetName = DataFakerStringArbitrary.address().streetName();
String streetAddress = DataFakerStringArbitrary.address().streetAddress();
String fullAddress = DataFakerStringArbitrary.address().fullAddress();
String zipCode = DataFakerStringArbitrary.address().zipCode();
String state = DataFakerStringArbitrary.address().state();
String country = DataFakerStringArbitrary.address().country();

// Internet-related generation
String email = DataFakerStringArbitrary.internet().emailAddress();
String url = DataFakerStringArbitrary.internet().url();
String domainName = DataFakerStringArbitrary.internet().domainName();
String password = DataFakerStringArbitrary.internet().password();
String ipV4 = DataFakerStringArbitrary.internet().ipV4Address();
String ipV6 = DataFakerStringArbitrary.internet().ipV6Address();

// Phone number generation
String phone = DataFakerStringArbitrary.phoneNumber().phoneNumber();
String cellPhone = DataFakerStringArbitrary.phoneNumber().cellPhone();
String extension = DataFakerStringArbitrary.phoneNumber().extension();

// Finance-related generation
String creditCard = DataFakerStringArbitrary.finance().creditCard();
String iban = DataFakerStringArbitrary.finance().iban();
String bic = DataFakerStringArbitrary.finance().bic();
```

### Locale Support

To generate locale-specific data:

```java
// Generate Korean names
String koreanName = DataFakerStringArbitrary.name(Locale.KOREAN).fullName();

// Generate Japanese addresses
String japaneseAddress = DataFakerStringArbitrary.address(Locale.JAPANESE).city();
```

## Real-world Example

```java
public class UserProfile {
    private String userName;
    private String email;
    private String homeAddress;
    private String phoneNumber;
    private String creditCard;
    private int age;              // Not affected by DataFaker (not String type)
    private String description;   // Not affected by DataFaker (no keyword match)
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
// age: random integer (uses default generator)
// description: random string (uses default generator)
```

:::warning
The DataFaker plugin only applies to String type fields that contain specific keywords in their names. Other field types or fields without matching keywords will use the default generators.
:::
