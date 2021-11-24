---
title: "Register ArbitraryBuilder"
linkTitle: "Register ArbitraryBuilder"
weight: 5
---
{{< alert color="secondary" title="Note">}}
For detail information check out [here]({{< relref "/docs/v0.3.x/features/registerarbitrarybuilder" >}})
{{< /alert >}}

```java
class CustomObjectGroup{
	public ArbitraryBuilder<CustomObject> customObject(FixtureMonkey fixture){
		return fixture.giveMeBuilder(CustomObject.class)
            .set("value", "test");
    }
}

@Test
void registerGroup() {
	// given
    FixtureMonkey fixture = FixtureMonkey.builder()
        .registerGroup(CustomObjectGroup.class)
        .build();
	
    // when
    CustomObject customObject = fixture.giveMeOne(CustomObject.class);
    CustomObjectWrapper customObjectWrapper = fixture.giveMeOne(CustomObjectWrapper.class);
    
    then(customObject.getValue()).isEqualTo("test");
    then(customObjectWrapper.getCustomObject().getValue()).isEqualTo("test");
}
```

## Apply Manipulator To Registered ArbitraryBuilder
```java
class CustomObjectGroup{
	public ArbitraryBuilder<CustomObject> customObject(FixtureMonkey fixture){
		return fixture.giveMeBuilder(CustomObject.class)
            .set("value", "test");
    }
}
@Test
void applyManipulatorToRegisteredArbitraryBuilder() {
	// given
    FixtureMonkey fixture = FixtureMonkey.builder()
        .registerGroup(CustomObjectGroup.class)
        .build();
	
    // when
    CustomObject customObject = fixture.giveMeBuilder(CustomObject.class)
        .set("value", "set")
        .sample();
    CustomObjectWrapper customObjectWrapper = fixture.giveMeBuilder(CustomObjectWrapper.class)
        .set("customObject.value", "innerSet")
        .sample();
    
    then(customObject.getValue()).isEqualTo("set");
    then(customObjectWrapper.getCustomObject().getValue()).isEqualTo("innerSet");
}
```
