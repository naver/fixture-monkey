---
title: "Release Notes"
images: []
menu:
docs:
weight: 100
---
### v0.6.8
> `DefaultDecomposedContainerValueFactory` supports `Map.Entry`
>
> A concurrency bug fixed in `PrimaryConstructorArbitraryIntrospector`

### v0.6.7
> Deprecate `List<Property> generateChildProperties(AnnotatedType annotatedType)` in `PropertyGenerator` interface, it will be removed in 0.7.0
>
> Add `List<Property> generateChildProperties(Property property)` in `PropertyGenerator` interface

### v0.6.6
> Log the error instead of throwing an exception if setting field is failed with `FieldReflectionArbitraryIntrospector`
>
> Add more support for kotlin extensions in FixtureMonkeyBuilder
