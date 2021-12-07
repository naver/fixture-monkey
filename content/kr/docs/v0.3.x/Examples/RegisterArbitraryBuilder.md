---
title: "ArbitraryBuilder 등록하기"
linkTitle: "ArbitraryBuilder 등록하기"
weight: 5
---
{{< alert color="secondary" title="Note">}}
자세한 내용은 [여기]({{< relref "/docs/v0.3.x/features/registerarbitrarybuilder" >}})를 확인해주세요.
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
    CustomObject customObject = this.fixture.giveMeOne(CustomObject.class);
    CustomObjectWrapper customObjectWrapper = this.fixture.giveMeOne(CustomObjectWrapper.class);
    
    then(customObject.getValue()).isEqualTo("test");
    then(customObjectWrapper.getCustomObject().getValue()).isEqualTo("test");
}
```

## 등록한 ArbitraryBuilder에 연산 적용하기
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
    CustomObject customObject = this.fixture.giveMeBuilder(CustomObject.class)
        .set("value", "set")
        .sample();
    CustomObjectWrapper customObjectWrapper = this.fixture.giveMeBuilder(CustomObjectWrapper.class)
        .set("customObject.value", "innerSet")
        .sample();
    
    then(customObject.getValue()).isEqualTo("set");
    then(customObjectWrapper.getCustomObject().getValue()).isEqualTo("innerSet");
}
```
