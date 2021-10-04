---
title: "Manipulator"
linkTitle: "Manipulator"
weight: 4
---


## Component

- Expression
- Value or Filter
- Limit
- Decomposition

## Expression
Expression refers to fields would like to manipulate.

- Expression for field would be `fieldName`
- Expression for nested field would be `outerField.innerField`
- All fields in object would be `*`
- `$` means root, same as [JsonPath](https://github.com/json-path/JsonPath)
- Single list or array element would be `[index]`
- All list or array elements would be `[*]`

## Value or Filter
Fields match `Expression` would be set as `Value` or applied by `Filter`.

## Limit
How many fields that match `Expression` would like to manipulate.

## Decomposition
Turning instance into `ArbitraryBuilder` for manipulating. 

For example, you could make a String fixture which always returns "Value"

```
fixtureMonkey.giveMeBuilder("Value")
``` 

* Apply `setNotNull` to decomposed instance null field, it would be set as a new arbitrary value.
  
More examples in [here]({{< relref "/docs/v0.3.x/examples/manipulator/complexmanipulator" >}})

## [SimpleManipulator]({{< relref "/docs/v0.3.x/examples/manipulator/simplemanipulator" >}})
- Applies in given order, `set` → `setPostCondition` → `customize`
- `Manipulators` with same `Experssion` overwrite previous `Manipulator` except for `setPostcondition`
- Customize can be used multiple times
- Customize applies in declared order

## [ComplexManipulator]({{< relref "/docs/v0.3.x/examples/manipulator/complexmanipulator" >}})
- `ArbitraryBuilder` applied `ComplexManipulator` sampling always returns same value
- `fixed` makes ArbitraryBuilder returns same value
