
---
title: "Hello, Fixture Monkey"
linkTitle: "Hello, Fixture Monkey"
weight: 1
menu:
  main:
    weight: 1
---
## What is it?

<img src="../../images/fixture-monkey.png" width="15%"/>

Fixture Monkey is designed to generate controllable arbitrary instances easily. It allows you to reuse same configurations of the instances in several tests.

You could write countless tests including edge cases by only one instance of the FixtureMonkey type. You could generate instances of complex types automatically and set fields with values from builders of the ArbitraryBuilder type. The well-defined builders could be reused in any tests. Writing integration tests is easier with fixture-monkey.

### "Write once, Test anywhere"

## Why do I want it?
{{< alert color="warning" title="Warning">}}
Fixture Monkey is designed for test environment, not recommended for production use
{{< /alert >}}

You should definitely check Fixture Monkey out If you want to
* reduce boilerplate and improve readability when you set up test scenario
* write integration tests easily
* recognize your concern within test easily
* manage complex dependencies effectively
* enjoy writing tests
