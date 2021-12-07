---
title: "ArbitraryCustomizer"
linkTitle: "ArbitraryCustomizer"
weight: 3
---
{{< alert color="secondary" title="Note">}}
자세한 내용은 [여기]({{< relref "/docs/v0.3.x/features/arbitrarycustomizer" >}})를 확인해주세요.
{{< /alert >}}

```java
class PersonArbitraryCustomizer implements ArbitraryCustomizer<Person> {
    @Override
    public void customizeFields(Class<Person> type, FieldArbitraries fieldArbitraries) {
        fieldArbitraries.removeArbitrary("id");   
        fieldArbitraries.replaceArbitrary("name", Arbitraries.strings().ofMaxLength(30));   
        ....
    }

    @Nullable
    @Override
    public Person customizeFixture(@Nullable Person person) {
        person.setAge(20); 
        return person;
    }
}

```

## 람다로 정의하기

```java
FixtureCustomizer<Person> personAgeFixedCustomizer = person -> {
    person.setAge(20);
    return person;
};

```

## 전역적으로 FixtureCustomizer 적용하기

```java
FixtureMonkey fixture = FixtureMonkey.builder()
        .addFixtureCustomizer(Person.class, new PersonFixtureCustomizer())
        .build();

```


## ArbitraryBuilder에 FixtureCustomizer 적용하기

```java
FixtureMonkey fixture = FixtureMonkey.builder().build();

Person person = fixture.giveMeBuilder(Person.class)
    .customize(new PersonFixtureCustomizer())
    .sample();

List<Person> persons = fixture.giveMeBuilder(Person.class)
    .customize(new PersonFixtureCustomizer())
    .sampleList(3);

Person person20Age = fixture.giveMeBuilder(Person.class)
    .customize(p -> {
        p.setAge(20);
        return p;
    })
    .sample();
```

## BuilderArbitraryCustomizer

```java
public static class PersonCustomizer implements BuilderArbitraryCustomizer<Person, Person.PersonBuilder> {
       // modify attribute Arbitrary by Builder
       @Override
       public void customizeBuilderFields(BuilderFieldArbitraries<B> builderFieldArbitraries) {
                builderFieldArbitraries
                          .use(Arbitraries.integer.between(10, 30)).in(Person.PersonBuilder::age)
                          .use(Arbitraries.string().ofLength(20)).in(Person.PersonBuilder::address);
        }

       // modify Builder attribute
        @Nullable
        @Override
        public Person.PersonBuilder customizeBuilder(@Nullable Person.PersonBuilder builder) {
               return builder.name("hello");
        }
    }
}

```
