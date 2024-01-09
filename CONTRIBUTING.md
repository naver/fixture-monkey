# Contributing To fixture-monkey

## Code of Conduct

This project is governed by the [Code of Conduct](./CODE_OF_CONDUCT.md). By participating you are expected to uphold
this code. Please report unacceptable behavior as an [issue](https://github.com/naver/fixture-monkey/issues).

## Build from source

Get the code.

```shell
git clone git@github.com:naver/fixture-monkey.git
cd fixture-monkey
```

Build from the Command Line.

```shell
./gradlew build
```

Run all checks.

```shell
./gradlew check
```

Install to local Maven repository.

```shell
./gradlew publishToMavenLocal
```

## How to submit Pull Requests

1. Fork `fixture-monkey` to your repository.
2. Create a new branch from your fixture-monkey main branch (and be sure to keep it up to date).
3. Do your work.
4. Create test code for your work.
5. Run the test code. Make sure all tests pass.
6. Create a new PR from your branch to the `fixture-monkey/main` branch.
7. Wait for reviews. When your contribution is good enough to be accepted, then it will be merged into our branch.
8. All Done! Thank you for your contribution.

## How to contribute to the document

The Fixture Monkey documentation website is built using `Hugo`. The code for the document is maintained in the `docs` directory in the main branch.

1. Fork `fixture-monkey` to your repository.
2. Create a new branch from your fixture-monkey main branch (and be sure to keep it up to date).
3. Contribute to the document (Write new content, Translate, Fix typos, Add examples, etc...).
4. Create a new PR from your branch to the `fixture-monkey/main` branch.
5. Wait for reviews. If your contribution is good enough to be accepted, it will be merged into our branch.
6. All Done! Thank you for your contribution.

## Convention

* [intellij-formatter](./tool/naver-intellij-formatter.xml)
* [checkstyle](./tool/naver-checkstyle-rules.xml)

### Test

Write test code using [jqwik](https://github.com/jlink/jqwik) `@Property`

## License

By contributing to fixture-monkey, you're agreeing that your contributions will be licensed under
its [Apache license 2.0](./LICENSE)
