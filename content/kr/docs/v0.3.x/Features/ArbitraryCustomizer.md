---
title: "ArbitraryCustomizer"
weight: 7
---

## customizerFields

Runs before `ArbitraryGenerator` instantiating, could modify `Arbitrary` in `FieldArbitraies`

## customizeFixture

Runs after `ArbitraryGenerator` instantiating, could determine return instance

### Lifecycle

1. customizerFields
2. customizeFixture

## BuilderArbitraryCustomizer

Use Builder to customize for preventing typo or mistakes

### Lifecycle

1. customizeFields
2. customizeBuilderFields
3. customizeBuilder
4. customizeFixture

## Example

You could find examples [here]({{< relref "/docs/v0.3.x/examples/arbitrarycustomizer" >}})
