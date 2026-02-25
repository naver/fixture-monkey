package com.navercorp.fixturemonkey.tests.kotlin

/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo
import com.navercorp.fixturemonkey.customizer.Values
import com.navercorp.fixturemonkey.customizer.Values.NOT_NULL
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.kotlinInnerSpec
import com.navercorp.fixturemonkey.kotlin.setKotlinInner
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.ComplexObject
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.ComplexObjectObject
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.IntegerMapObject
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.ListStringObject
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.MapObject
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.NestedKeyMapObject
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.NestedListStringObject
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.SimpleObject
import com.navercorp.fixturemonkey.tests.kotlin.InnerSpecTestSpecs.SupplierStringObject

import net.jqwik.api.Arbitraries
import net.jqwik.api.Example
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import java.util.function.Supplier
import java.util.stream.Collectors
import kotlin.jvm.java

class KotlinInnerSpecTest {

    @Example
    fun key() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    minSize(1)
                    key("key")
                }
            }
            .sample()
            .strMap

        then(actual.keys).contains("key")
    }

    @Example
    fun value() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    minSize(1)
                    value("value")
                }
            }
            .sample()
            .strMap

        then(actual.values).contains("value")
    }

    @Example
    fun entry() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    minSize(1)
                    entry("key", "value")
                }
            }
            .sample()
            .strMap

        then(actual["key"]).isEqualTo("value")
    }

    @Example
    fun keys() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    minSize(3)
                    keys("key1", "key2", "key3")
                }
            }
            .sample()
            .strMap

        then(actual.keys).containsAll(setOf("key1", "key2", "key3"))
    }

    @Example
    fun values() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    minSize(3)
                    values("value1", "value2", "value3")
                }
            }
            .sample()
            .strMap

        then(actual.values).containsAll(setOf("value1", "value2", "value3"))
    }

    @Example
    fun entries() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    minSize(2)
                    entries("key1", "value1", "key2", "value2")
                }
            }
            .sample()
            .strMap

        then(actual["key1"]).isEqualTo("value1")
        then(actual["key2"]).isEqualTo("value2")
    }

    @Example
    fun entryTwice() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    minSize(2)
                    entry("key1", "value1")
                    entry("key2", "value2")
                }
            }
            .sample()

        then(actual.strMap["key1"]).isEqualTo("value1")
        then(actual.strMap["key2"]).isEqualTo("value2")
    }

    @Example
    fun valueNull() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    minSize(1)
                    value(null)
                }
            }
            .sample()

        then(actual.strMap.containsValue(null as String?)).isTrue()
    }

    @Example
    fun keyNullThrows() {
        thenThrownBy {
            SUT.giveMeBuilder<MapObject>()
                .setKotlinInner {
                    property("strMap") {
                        minSize(1)
                        key(null)
                    }
                }
                .sample()
        }.isExactlyInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Map key cannot be null.")
    }

    @Example
    fun keyInKey() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .defaultNotNull(true)
            .defaultArbitraryContainerInfoGenerator { _ -> ArbitraryContainerInfo(1, 3) }
            .build()

        val actual = sut.giveMeBuilder<NestedKeyMapObject>()
            .setKotlinInner {
                property("mapKeyMap") {
                    key { key("key") }
                }
            }
            .sample()

        val keyList = actual.mapKeyMap.keys.stream()
            .flatMap { it.keys.stream() }
            .collect(Collectors.toList())
        then(keyList).contains("key")
    }

    @Example
    fun valueInKey() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .defaultNotNull(true)
            .defaultArbitraryContainerInfoGenerator { _ -> ArbitraryContainerInfo(1, 3) }
            .build()

        val actual = sut.giveMeBuilder<NestedKeyMapObject>()
            .setKotlinInner {
                property("mapKeyMap") {
                    key { value("value") }
                }
            }
            .sample()

        val keyList = actual.mapKeyMap.keys.stream()
            .flatMap { it.values.stream() }
            .collect(Collectors.toList())
        then(keyList).contains("value")
    }

    @Example
    fun keyInValue() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("mapValueMap") {
                    minSize(1)
                    value {
                        minSize(1)
                        key("key")
                    }
                }
            }
            .sample()

        val valueList = actual.mapValueMap.values.stream()
            .flatMap { it.keys.stream() }
            .collect(Collectors.toList())
        then(valueList).contains("key")
    }

    @Example
    fun valueInValue() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("mapValueMap") {
                    minSize(1)
                    value {
                        minSize(1)
                        value("value")
                    }
                }
            }
            .sample()

        val valueList = actual.mapValueMap.values.stream()
            .flatMap { it.values.stream() }
            .collect(Collectors.toList())
        then(valueList).contains("value")
    }

    @Example
    fun sizeInValue() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("listValueMap") {
                    size(1)
                    value { size(10) }
                }
            }
            .sample()

        val sizeList = actual.listValueMap.values.stream()
            .map { it.size }
            .collect(Collectors.toList())
        then(sizeList).contains(10)
    }

    @Example
    fun listElementInValue() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("listValueMap") {
                    size(1)
                    value {
                        size(1)
                        listElement(0, "test")
                    }
                }
            }
            .sample()

        val elementList = actual.listValueMap.values.stream()
            .flatMap { it.stream() }
            .collect(Collectors.toList())
        then(elementList).contains("test")
    }

    @Example
    fun propertyInValue() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("objectValueMap") {
                    size(1)
                    value { property("str", "test") }
                }
            }
            .sample()

        val fieldList = actual.objectValueMap.values.stream()
            .filter { it != null }
            .map { it.str }
            .collect(Collectors.toList())
        then(fieldList).contains("test")
    }

    @Example
    fun entryInEntryValue() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("mapValueMap") {
                    minSize(1)
                    entry("key1") {
                        minSize(1)
                        entry("key2", "value")
                    }
                }
            }
            .sample()

        val value = actual.mapValueMap["key1"]
        then(value?.get("key2")).isEqualTo("value")
    }

    @Example
    fun entryInEntryKey() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .defaultNotNull(true)
            .defaultArbitraryContainerInfoGenerator { _ -> ArbitraryContainerInfo(1, 3) }
            .build()

        // when
        val actual = sut.giveMeBuilder<NestedKeyMapObject>()
            .setKotlinInner {
                property("mapKeyMap") {
                    entry({ entry("key", "value2") }, "value1")
                }
            }
            .sample()

        // then
        val expected = actual.mapKeyMap.entries
            .stream()
            .filter { "value1" == it.value }
            .findAny()
            .get()
            .key
        then(expected["key"]).isEqualTo("value2")
    }

    @Example
    fun entryValueSetNull() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    size(1)
                    entry("key", null)
                }
            }
            .sample()

        then(actual.strMap["key"]).isNull()
    }

    @Example
    fun listElementInListElement() {
        // when
        val actual = SUT.giveMeBuilder<NestedListStringObject>()
            .setKotlinInner {
                property("values") {
                    size(1)
                    listElement(0) {
                        size(1)
                        listElement(0, "test")
                    }
                }
            }
            .sample()

        then(actual.values[0][0]).isEqualTo("test")
    }

    @Example
    fun propertyInProperty() {
        // when
        val actual = SUT.giveMeBuilder<ComplexObjectObject>()
            .setKotlinInner {
                property("value") {
                    property("value") {
                        property("str", "test")
                    }
                }
            }
            .sample()

        then(actual.value.value.str).isEqualTo("test")
    }

    @Example
    fun sizeAndEntry() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    size(4)
                    entry("key", "test")
                }
            }
            .sample()
            .strMap

        then(actual).hasSize(4)
        then(actual["key"]).isEqualTo("test")
    }

    @Example
    fun entryAndSize() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    entry("key", "test")
                    size(4)
                }
            }
            .sample()
            .strMap

        then(actual).hasSize(4)
        then(actual["key"]).isEqualTo("test")
    }

    @Example
    fun sizeTwiceReturnsLatterSize() {
        // when
        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    size(1)
                    entry("key", "test")
                    size(0)
                }
            }
            .sample()
            .strMap

        then(actual).hasSize(0)
    }

    @Example
    fun keyLazy() {
        val variable = SUT.giveMeBuilder<String>()
        val builder = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    size(1)
                    keyLazy { variable.sample() }
                }
            }
        variable.set("key")

        val actual = builder.sample()

        then(actual.strMap.containsKey("key")).isTrue()
    }

    @Example
    fun valueLazy() {
        val variable = SUT.giveMeBuilder<String>()
        val builder = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    minSize(1)
                    valueLazy { variable.sample() }
                }
            }
        variable.set("value")

        val actual = builder.sample()

        then(actual.strMap.containsValue("value")).isTrue()
    }

    @Example
    fun entryLazy() {
        val keyVariable = SUT.giveMeBuilder<String>()
        val valueVariable = SUT.giveMeBuilder<String>()
        val builder = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    minSize(1)
                    entryLazy({ keyVariable.sample() }, { valueVariable.sample() })
                }
            }
        keyVariable.set("key")
        valueVariable.set("value")

        val actual = builder.sample()

        then(actual.strMap["key"]).isEqualTo("value")
    }

    @Example
    fun keyLazyNullThrows() {
        thenThrownBy {
            SUT.giveMeBuilder<MapObject>()
                .setKotlinInner {
                    property("strMap") {
                        minSize(1)
                        keyLazy { null }
                    }
                }
                .sample()
        }.isExactlyInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Map key cannot be null.")
    }

    @Example
    fun allKeyLazy() {
        val actual = SUT.giveMeBuilder<IntegerMapObject>()
            .setKotlinInner {
                property("integerMap") {
                    allKeyLazy { Arbitraries.integers().between(0, 100) }
                }
            }
            .sample()

        then(actual.integerMap.keys).allMatch { it in 0..100 }
    }

    @Example
    fun allValueLazy() {
        val actual = SUT.giveMeBuilder<IntegerMapObject>()
            .setKotlinInner {
                property("integerMap") {
                    allValueLazy { Arbitraries.integers().between(0, 100) }
                }
            }
            .sample()

        then(actual.integerMap.values).allMatch { it in 0..100 }
    }

    @Example
    fun allEntry() {
        val actual = SUT.giveMeBuilder<IntegerMapObject>()
            .setKotlinInner {
                property("integerMap") {
                    allEntry(
                        { Arbitraries.integers().between(0, 100) },
                        100
                    )
                }
            }
            .sample()

        then(actual.integerMap.keys).allMatch { it in 0..100 }
        then(actual.integerMap.values).allMatch { it == 100 }
    }

    @Example
    fun allEntryLazy() {
        val actual = SUT.giveMeBuilder<IntegerMapObject>()
            .setKotlinInner {
                property("integerMap") {
                    allEntryLazy(
                        { Arbitraries.integers().between(0, 100) },
                        { Arbitraries.integers().between(0, 100) }
                    )
                }
            }
            .sample()

        then(actual.integerMap.keys).allMatch { it in 0..100 }
        then(actual.integerMap.values).allMatch { it in 0..100 }
    }

    @Example
    fun allKey() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("objectKeyMap") {
                    allKey { property("str", expected) }
                }
            }
            .sample()
            .objectKeyMap
            .keys
            .map { it.str }

        then(actual).allMatch { it == expected }
    }

    @Example
    fun allValue() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    allValue(expected)
                }
            }
            .sample()
            .strMap
            .values

        then(actual).allMatch { it == expected }
    }

    @Example
    fun allValueInner() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("objectValueMap") {
                    allValue { property("str", expected) }
                }
            }
            .sample()
            .objectValueMap
            .values
            .map { it.str }

        then(actual).allMatch { it == expected }
    }

    @Example
    fun allListElement() {
        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<List<String>>()
            .setKotlinInner {
                allListElement(expected)
            }
            .sample()

        then(actual).allMatch { it == expected }
    }

    @Example
    fun allListElementInnerSpec() {
        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<List<List<String>>>()
            .setKotlinInner {
                allListElement { allListElement(expected) }
            }
            .sample()
            .flatten()

        then(actual).allMatch { it == expected }
    }

    @Example
    fun setPostCondition() {
        val actual = SUT.giveMeBuilder<SimpleObject>()
            .setKotlinInner {
                property("str") {
                    postCondition<String> { it.length > 5 }
                }
            }
            .sample()

        then(actual.str).hasSizeGreaterThan(5)
    }

    @Example
    fun inner() {
        val innerSpec = kotlinInnerSpec { property("str", "test") }

        val actual = SUT.giveMeBuilder<SimpleObject>()
            .setKotlinInner {
                inner(innerSpec.toInnerSpec())
            }
            .sample()

        then(actual.str).isEqualTo("test")
    }

    @Example
    fun propertyInner() {
        val innerSpec = kotlinInnerSpec { property("str", "test") }

        val actual = SUT.giveMeBuilder<ComplexObject>()
            .setKotlinInner {
                property("value", innerSpec.toInnerSpec())
            }
            .sample()

        then(actual.value.str).isEqualTo("test")
    }

    @Example
    fun listElementInMaxSize() {
        val expected = "expected"

        val actual = SUT.giveMeBuilder<List<String>>()
            .setKotlinInner {
                maxSize(2)
                listElement(0, expected)
                listElement(1, expected)
            }
            .sample()

        then(actual).allMatch { it == expected }
    }

    @Example
    fun setAfterSizeReturnsSet() {
        val actual = SUT.giveMeBuilder<ListStringObject>()
            .setKotlinInner {
                property("values") { size(2) }
                property("values", ArrayList<String>())
            }
            .sample()
            .values

        then(actual).isEmpty()
    }

    @Example
    fun sizeAfterSetReturnsSize() {
        val actual = SUT.giveMeBuilder<ListStringObject>()
            .setKotlinInner {
                property("values", ArrayList<String>())
                property("values") { size(2) }
            }
            .sample()
            .values

        then(actual).hasSize(2)
    }

    @Example
    fun sizeAfterSetWithSeparateInnerSpecReturnsSize() {
        val actual = SUT.giveMeBuilder<ListStringObject>()
            .setKotlinInner {
                property("values", ArrayList<String>())
            }
            .setKotlinInner {
                property("values") { size(2) }
            }
            .sample()
            .values

        then(actual).hasSize(2)
    }

    @Example
    fun setAfterSetWithSeparateInnerSpecReturnsSet() {
        val actual = SUT.giveMeBuilder<ListStringObject>()
            .setKotlinInner {
                property("values") { size(2) }
            }
            .setKotlinInner {
                property("values", ArrayList<String>())
            }
            .sample()
            .values

        then(actual).isEmpty()
    }

    @Example
    fun innerSpecIncrementsSequence() {
        val actual = SUT.giveMeBuilder<ListStringObject>()
            .setKotlinInner {
                property("values") { size(1) }
                property("values") { size(2) }
                property("values") { size(3) }
            }
            .size("values", 5)
            .sample()

        then(actual.values).hasSize(5)
    }

    @Example
    fun setNotNull() {
        val actual = SUT.giveMeBuilder<SimpleObject>()
            .setKotlinInner {
                property("str", NOT_NULL)
            }
            .sample()
            .str

        then(actual).isNotNull()
    }

    @Example
    fun keysForCollection() {
        val keyList = listOf("key1", "key2", "key3")

        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    keys(keyList)
                    size(3)
                }
            }
            .sample()
            .strMap

        then(actual.keys).containsAll(keyList)
    }

    @Example
    fun valuesForCollection() {
        val valueList = listOf("value1", "value2", "value3")

        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    values(valueList)
                    size(3)
                }
            }
            .sample()
            .strMap

        then(actual.values).containsAll(valueList)
    }

    @Example
    fun entriesForCollection() {
        val entries = listOf("key1", "value1", "key2", "value2")

        val actual = SUT.giveMeBuilder<MapObject>()
            .setKotlinInner {
                property("strMap") {
                    entries(entries)
                    size(2)
                }
            }
            .sample()
            .strMap

        then(actual["key1"]).isEqualTo("value1")
        then(actual["key2"]).isEqualTo("value2")
    }

    @Example
    fun supportSupplierWrapping() {
        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<Supplier<SimpleObject>>()
            .setKotlinInner {
                property("str", expected)
            }
            .sample()
            .get()
            .str

        then(actual).isEqualTo(expected)
    }

    @Example
    fun supportSupplierObjectField() {
        val expected = Supplier { "test" }

        // when
        val actual = SUT.giveMeBuilder<SupplierStringObject>()
            .setKotlinInner {
                property("value", Values.just(expected))
            }
            .sample()
            .value

        then(actual).isEqualTo(expected)
    }

    companion object {
        val SUT = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .defaultNotNull(true)
            .build()
    }
}
