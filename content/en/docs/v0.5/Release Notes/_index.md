
---
title: "Release Notes"
weight: 6
---
### 0.5.0
#### Breaking changes
- Refactor `register`
  - As-is
      - `set` an sampled instance of `registered ArbitrayBuilder`
  - To-be
      - An `ArbitraryBuilder` has all manipulations `registered ArbitraryBuilder` has
- Remove a manipulation priority
  - As-is
      - `size` has a highest priority among all manipulations
  - To-be
      - All manipulations have same priority, they are executed in declared order
- Remove `defaultArbitraryContainerInfo`, `defaultArbitrayContainerSize` options, add `defaultArbitraryContainerInfoGenerator` option

#### New Features
- Add an `NOT_NULL` instance 
  - `set("expression", NOT_NULL)` == `setNotNull("expression")`
- Add more failed log for `set` when value type is different 
  - Add parent property type in a log if exists
