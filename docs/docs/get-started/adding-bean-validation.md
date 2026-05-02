---
title: "Adding Bean Validation"
sidebar_position: 25
---

import CodeSnippet from '@site/src/components/CodeSnippet';
import BeanValidationTestJava from '@examples-java/getstarted/BeanValidationTest.java';

Sometimes, you might want to create a valid test object that adheres to the constraints specified by the Bean Validation annotations on your class.
Fixture Monkey makes this easy with support for `jakarta.validation.constraints` and `javax.validation.constraints` packages.

For example, consider a Product class with validation constraints:

<CodeSnippet src={BeanValidationTestJava} language="java" showClass />

To generate objects that satisfy these constraints, first add the appropriate dependency:

##### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jakarta-validation:{{fixtureMonkeyVersion}}")
```

##### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jakarta-validation</artifactId>
  <version>{{fixtureMonkeyVersion}}</version>
  <scope>test</scope>
</dependency>
```

Then, add the validation plugin to your FixtureMonkey configuration:
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JakartaValidationPlugin()) // or new JavaxValidationPlugin()
    .build();
```
Note: If you're using `fixture-monkey-starter`, the validation plugin is already included.

Now you can generate valid objects that satisfy all constraints:

<CodeSnippet src={BeanValidationTestJava} language="java" method="test" />

When you run this code, Fixture Monkey will generate a Product instance that satisfies all validation constraints.
Below is just an example, and the actual values will be different each time:

```java
Product(
    id=42,                   // Satisfies @Min(1)
    productName="product-1", // Satisfies @NotBlank
    price=75000,            // Satisfies @Max(100000)
    options=[               // Satisfies @Size(min = 3)
        "option1",          // Each string satisfies @NotBlank
        "option2",
        "option3"
    ],
    createdAt=2024-03-20T10:15:30Z  // Satisfies @Past
)
```

