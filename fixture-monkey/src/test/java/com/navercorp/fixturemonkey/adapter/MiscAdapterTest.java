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

package com.navercorp.fixturemonkey.adapter;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.DeepNestedListObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.DeepObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NestedStringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.RichObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SelfRecursiveObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapperPair;

@PropertyDefaults(tries = 10)
class MiscAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void sampleComplexObject() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class).sample();

		// then
		then(actual.getList()).isNotNull();
		then(actual.getMap()).isNotNull();
		then(actual.getMapEntry()).isNotNull();
	}

	@Property
	void giveMeManipulatedObjectRemainsManipulator() {
		StringListWrapper expected = SUT.giveMeBuilder(StringListWrapper.class).size("values", 2, 2).sample();

		StringListWrapper actual = SUT.giveMeBuilder(expected).set("values[1]", "test").sample();

		then(actual.getValues().get(1)).isEqualTo("test");
	}

	@Property
	void thenApplyContainerSizeConsistentAcrossSamples() {
		// given
		ArbitraryBuilder<ComplexObject> builder = SUT.giveMeBuilder(ComplexObject.class).thenApply((obj, b) -> {
			b.set("str", "applied");
		});

		// when
		Set<Integer> strListSizes = new HashSet<>();
		Set<Integer> listSizes = new HashSet<>();
		for (int i = 0; i < 20; i++) {
			ComplexObject sampled = builder.sample();
			strListSizes.add(sampled.getStrList().size());
			listSizes.add(sampled.getList().size());
		}

		// then
		then(strListSizes).as("strList sizes should be consistent across thenApply samples").hasSize(1);
		then(listSizes).as("list sizes should be consistent across thenApply samples").hasSize(1);
	}

	@Property
	void thenApplyNestedObjectContainerSizeConsistent() {
		// given
		ArbitraryBuilder<RichObject> builder = SUT.giveMeBuilder(RichObject.class).thenApply((obj, b) -> {
			b.set("code", "fixed-order-no");
		});

		// when
		Set<Integer> discountSizes = new HashSet<>();
		Set<Integer> optionSizes = new HashSet<>();
		Set<Integer> addonSizes = new HashSet<>();
		for (int i = 0; i < 20; i++) {
			RichObject sampled = builder.sample();
			discountSizes.add(sampled.getDiscounts().size());
			optionSizes.add(sampled.getContent().getOptions().size());
			addonSizes.add(sampled.getContent().getAddons().size());
		}

		// then
		then(discountSizes).as("discounts sizes should be consistent").hasSize(1);
		then(optionSizes).as("options sizes should be consistent").hasSize(1);
		then(addonSizes).as("addons sizes should be consistent").hasSize(1);
	}

	@Property
	void decomposedObjectPreservesContainerSize() {
		// given
		List<String> expectedList = new ArrayList<>(java.util.Arrays.asList("a", "b", "c"));
		StringListWrapper source = new StringListWrapper();
		source.setValues(expectedList);

		// when
		StringListWrapper actual = SUT.giveMeBuilder(StringListWrapper.class).set("$", source).sample();

		// then
		then(actual.getValues()).hasSize(3);
		then(actual.getValues()).containsExactly("a", "b", "c");
	}

	@Property
	void decomposedChildObjectPreservesContainerSize() {
		// given
		DeepObject product = SUT.giveMeBuilder(DeepObject.class).size("options", 2).fixed().sample();

		// when
		RichObject actual = SUT.giveMeBuilder(RichObject.class).set("content", product).sample();

		// then
		then(actual.getContent().getOptions()).hasSize(2);
	}

	@Property
	void decomposedNestedContainerPreservesExplicitSize() {
		// given
		ComplexObject sampled = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 3)
			.size("list", 2)
			.fixed()
			.sample();

		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class).set("$", sampled).sample();

		// then
		then(actual.getStrList()).hasSize(3);
		then(actual.getList()).hasSize(2);
	}

	@Property
	void thenApplyWithChildSampleContainerSizeConsistent() {
		// given
		ArbitraryBuilder<RichObject> builder = SUT.giveMeBuilder(RichObject.class).thenApply((order, b) -> {
			DeepObject product = SUT.giveMeBuilder(DeepObject.class).size("options", 0).fixed().sample();
			b.set("content", product);
		});

		// when
		Set<Integer> optionSizes = new HashSet<>();
		for (int i = 0; i < 20; i++) {
			RichObject sampled = builder.sample();
			optionSizes.add(sampled.getContent().getOptions().size());
		}

		// then
		then(optionSizes).as("options sizes should be 0 from explicit size()").containsExactly(0);
	}

	@Property
	void thenApplyWithChildSampleNoExplicitSizeContainerVaries() {
		// given
		ArbitraryBuilder<RichObject> builder = SUT.giveMeBuilder(RichObject.class).thenApply((order, b) -> {
			DeepObject product = SUT.giveMeBuilder(DeepObject.class).sample();
			b.set("content", product);
		});

		// when
		Set<Integer> optionSizes = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			RichObject sampled = builder.sample();
			optionSizes.add(sampled.getContent().getOptions().size());
		}

		// then
		then(optionSizes).as("without fixed(), option sizes should vary").hasSizeGreaterThan(1);
	}

	@Property
	void setFieldThenSetRootDecomposed_laterRootOverridesEarlierField() {
		// given
		SimpleObject earlierField = new SimpleObject();
		earlierField.setStr("Seoul");
		earlierField.setInteger(111);

		ComplexObject laterRoot = new ComplexObject();
		SimpleObject rootChild = new SimpleObject();
		rootChild.setStr("Busan");
		rootChild.setInteger(999);
		laterRoot.setObject(rootChild);
		laterRoot.setStr("rootStr");
		laterRoot.setInteger(42);

		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("object", earlierField)
			.set("$", laterRoot)
			.sample();

		// then
		then(actual.getStr()).isEqualTo("rootStr");
		then(actual.getInteger()).isEqualTo(42);
		then(actual.getObject().getStr()).isEqualTo("Busan");
		then(actual.getObject().getInteger()).isEqualTo(999);
	}

	@Property(tries = 1)
	void adapterShouldNotIntrospectJavaStandardLibraryTypes() throws Exception {
		// given
		TypeCache.clearCache();

		// when
		SimpleObject actual = SUT.giveMeOne(SimpleObject.class);

		// then
		then(actual).isNotNull();
		then(actual.getBigDecimal()).isNotNull();
		then(actual.getLocalDate()).isNotNull();

		Map<Class<?>, ?> fieldsCache = getFieldsCache();
		then(fieldsCache.containsKey(BigDecimal.class))
			.as("BigDecimal should not be introspected by inferPossibleProperties")
			.isFalse();
		then(fieldsCache.containsKey(LocalDate.class))
			.as("LocalDate should not be introspected by inferPossibleProperties")
			.isFalse();
	}

	@Property
	void setFieldOnNonRecursivePojoWithCacheEnabled() {
		// given
		StringWrapperPair actual = SUT.giveMeBuilder(StringWrapperPair.class).set("value1.value", "fixed").sample();

		// then
		then(actual.getValue1().getValue()).isEqualTo("fixed");
		then(actual.getValue2()).isNotNull();
	}

	@Property
	void setFieldOnNonRecursivePojoRepeatedSamplesRetainSetValue() {
		// given
		ArbitraryBuilder<StringWrapperPair> builder = SUT.giveMeBuilder(StringWrapperPair.class).set(
			"value1.value",
			"fixed"
		);

		Set<String> value2Values = new HashSet<>();
		for (int i = 0; i < 5; i++) {
			StringWrapperPair actual = builder.sample();
			then(actual.getValue1().getValue()).isEqualTo("fixed");
			value2Values.add(actual.getValue2().getValue());
		}

		// then
		then(value2Values.size()).as("non-set fields should vary across samples").isGreaterThan(1);
	}

	@Property
	void setDeepRecursiveValueWithCacheEnabled() {
		// given
		SelfRecursiveObject actual = SUT.giveMeBuilder(SelfRecursiveObject.class)
			.set("recursive.recursive.value", "deep")
			.sample();

		// then
		then(actual).isNotNull();
		then(actual.getRecursive()).as("first recursive").isNotNull();
		then(actual.getRecursive().getRecursive()).as("second recursive").isNotNull();
		then(actual.getRecursive().getRecursive().getValue()).as("value").isEqualTo("deep");
	}

	@Property
	void setRecursiveFieldValueWithCacheEnabled() {
		// given
		SelfRecursiveObject actual = SUT.giveMeBuilder(SelfRecursiveObject.class)
			.set("recursive.value", "test")
			.sample();

		// then
		then(actual.getRecursive()).isNotNull();
		then(actual.getRecursive().getValue()).isEqualTo("test");
	}

	@Property
	void sizeOnContainerWithCacheEnabled() {
		// given
		StringListWrapper actual = SUT.giveMeBuilder(StringListWrapper.class).size("values", 3).sample();

		// then
		then(actual.getValues()).hasSize(3);
	}

	@Property
	void nestedSizeWithCacheEnabled() {
		// given
		NestedStringListWrapper actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.size("values", 2)
			.size("values[0].values", 3)
			.size("values[1].values", 4)
			.sample();

		// then
		then(actual.getValues()).hasSize(2);
		then(actual.getValues().get(0).getValues()).hasSize(3);
		then(actual.getValues().get(1).getValues()).hasSize(4);
	}

	@Property
	void registerTypedContainerSizeWithCacheEnabled() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 4)
			)
			.build();

		// when
		NestedStringListWrapper actual = sut.giveMeOne(NestedStringListWrapper.class);

		// then
		for (StringListWrapper item : actual.getValues()) {
			then(item.getValues()).hasSize(4);
		}
	}

	@Property
	void registerPathSpecificSizeWithCacheEnabled() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(NestedStringListWrapper.class, fixture ->
				fixture
					.giveMeBuilder(NestedStringListWrapper.class)
					.size("values", 2)
					.size("values[0].values", 3)
					.size("values[1].values", 4)
			)
			.build();

		// when
		NestedStringListWrapper actual = sut.giveMeOne(NestedStringListWrapper.class);

		// then
		then(actual.getValues()).hasSize(2);
		then(actual.getValues().get(0).getValues()).hasSize(3);
		then(actual.getValues().get(1).getValues()).hasSize(4);
	}

	@Property
	void exactPathOverridesTypedSizeWithCacheEnabled() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 4)
			)
			.build();

		// when
		NestedStringListWrapper actual = sut
			.giveMeBuilder(NestedStringListWrapper.class)
			.size("values", 2)
			.size("values[0].values", 1)
			.sample();

		// then
		then(actual.getValues()).hasSize(2);
		then(actual.getValues().get(0).getValues()).hasSize(1);
		then(actual.getValues().get(1).getValues()).hasSize(4);
	}

	@Property(tries = 1)
	void tracerCapturesSubtreeCacheEvents() {
		// given
		List<ResolutionTrace> traces = new ArrayList<>();
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin().tracer(traces::add))
			.build();

		// when
		sut.giveMeBuilder(NestedStringListWrapper.class).size("values", 1).set("values[0].values[0]", "test").sample();
		sut.giveMeBuilder(NestedStringListWrapper.class).size("values", 1).set("values[0].values[0]", "test2").sample();

		// then
		then(traces).hasSize(2);

		ResolutionTrace first = traces.get(0);
		List<ResolutionTrace.SubtreeCacheEntry> firstEvents = first.getSubtreeCacheEvents();
		then(firstEvents)
			.as("first sample should have subtree cache events (MISS/STORE for StringListWrapper)")
			.isNotEmpty();

		then(firstEvents.stream().anyMatch(e -> "STORE".equals(e.event())))
			.as("first sample should STORE StringListWrapper snapshot")
			.isTrue();

		ResolutionTrace second = traces.get(1);
		List<ResolutionTrace.SubtreeCacheEntry> secondEvents = second.getSubtreeCacheEvents();
		then(secondEvents).as("second sample should have subtree cache events (HIT for StringListWrapper)")
			.isNotEmpty();

		then(secondEvents.stream().anyMatch(e -> "HIT".equals(e.event())))
			.as("second sample should HIT StringListWrapper from cache")
			.isTrue();
	}

	@Property
	void giveMeBuilderWithValueThenSetNestedContainerElementField() {
		// given
		DeepObject value = SUT.giveMeBuilder(DeepObject.class)
			.size("options", 2)
			.sample();
		String expected = "NOTHING";

		// when
		DeepObject actual = SUT.giveMeBuilder(value)
			.set("options[0].name", expected)
			.sample();

		// then
		then(actual.getOptions()).hasSize(2);
		then(actual.getOptions().get(0).getName()).isEqualTo(expected);
	}

	@Property
	void giveMeBuilderWithValueThenSetDeepNestedField() {
		// given
		DeepObject value = SUT.giveMeBuilder(DeepObject.class).sample();
		BigDecimal expected = BigDecimal.valueOf(9999);

		// when
		DeepObject actual = SUT.giveMeBuilder(value)
			.set("policy.threshold.limit", expected)
			.sample();

		// then
		then(actual.getPolicy().getThreshold().getLimit()).isEqualTo(expected);
	}

	@Property
	void giveMeBuilderWithValueThenSetMultipleNestedFields() {
		// given
		DeepObject value = SUT.giveMeBuilder(DeepObject.class)
			.size("options", 1)
			.sample();
		String expectedName = "NOTHING";
		String expectedMethod = "DIRECT";

		// when
		DeepObject actual = SUT.giveMeBuilder(value)
			.set("options[0].name", expectedName)
			.set("policy.method", expectedMethod)
			.sample();

		// then
		then(actual.getOptions().get(0).getName()).isEqualTo(expectedName);
		then(actual.getPolicy().getMethod()).isEqualTo(expectedMethod);
	}

	@Property
	void giveMeBuilderWithValueThenSetWildcardContainerElementField() {
		// given
		DeepObject value = SUT.giveMeBuilder(DeepObject.class)
			.size("options", 2)
			.sample();

		// when
		DeepObject actual = SUT.giveMeBuilder(value)
			.set("options[*].name", "NOTHING")
			.sample();

		// then
		then(actual.getOptions()).hasSize(2);
		then(actual.getOptions().get(0).getName()).isEqualTo("NOTHING");
		then(actual.getOptions().get(1).getName()).isEqualTo("NOTHING");
	}

	@Property
	void giveMeBuilderWithValueThenSetDeepNestedContainerPath() {
		// given — items[0].detail.spec.category 시나리오 재현
		// DeepObject.options[0].name 은 2-depth, policy.threshold.limit 은 3-depth
		// 여기서는 options[*]의 values[0]을 set (container > element > container > element)
		DeepObject value = SUT.giveMeBuilder(DeepObject.class)
			.size("options", 1)
			.size("options[0].values", 2)
			.sample();
		String expected = "OVERRIDE";

		// when
		DeepObject actual = SUT.giveMeBuilder(value)
			.set("options[0].values[0]", expected)
			.sample();

		// then
		then(actual.getOptions().get(0).getValues().get(0)).isEqualTo(expected);
	}

	@Property
	void giveMeBuilderWithValueThenSetDeep4DepthNestedField() {
		// given — items[0].detail.spec.category enum 시나리오
		DeepNestedListObject value = SUT.giveMeBuilder(DeepNestedListObject.class)
			.size("items", 2)
			.sample();
		DeepNestedListObject.Category expected = DeepNestedListObject.Category.NOTHING;

		// when
		DeepNestedListObject actual = SUT.giveMeBuilder(value)
			.set("items[0].detail.spec.category", expected)
			.sample();

		// then
		then(actual.getItems().get(0).getDetail().getSpec().getCategory())
			.isEqualTo(expected);
	}

	@Property
	void giveMeBuilderWithValueThenSetWildcard4DepthNestedField() {
		// given — items[*].detail.spec.category 시나리오 재현
		DeepNestedListObject value = SUT.giveMeBuilder(DeepNestedListObject.class)
			.size("items", 2)
			.sample();
		DeepNestedListObject.Category expected = DeepNestedListObject.Category.NOTHING;

		// when
		DeepNestedListObject actual = SUT.giveMeBuilder(value)
			.set("items[*].detail.spec.category", expected)
			.sample();

		// then
		then(actual.getItems()).hasSize(2);
		then(actual.getItems().get(0).getDetail().getSpec().getCategory())
			.isEqualTo(expected);
		then(actual.getItems().get(1).getDetail().getSpec().getCategory())
			.isEqualTo(expected);
	}

	@Property
	void giveMeBuilderWithValueThenSizeThenSetDeep4DepthNestedField() {
		// given — giveMeBuilder(value) + size + set indexed element
		DeepNestedListObject value = SUT.giveMeBuilder(DeepNestedListObject.class)
			.size("items", 2)
			.sample();
		DeepNestedListObject.Category expected = DeepNestedListObject.Category.NOTHING;

		// when
		DeepNestedListObject actual = SUT.giveMeBuilder(value)
			.size("items", 3)
			.set("items[0].detail.spec.category", expected)
			.sample();

		// then
		then(actual.getItems()).hasSize(3);
		then(actual.getItems().get(0).getDetail().getSpec().getCategory())
			.isEqualTo(expected);
	}

	@Property
	void setWildcard4DepthNestedFieldWithoutGiveMeValue() {
		// given — giveMeBuilder(Class)에서도 와일드카드 4-depth가 실패하는지 확인
		DeepNestedListObject.Category expected = DeepNestedListObject.Category.NOTHING;

		// when
		DeepNestedListObject actual = SUT.giveMeBuilder(DeepNestedListObject.class)
			.size("items", 2)
			.set("items[*].detail.spec.category", expected)
			.sample();

		// then
		then(actual.getItems()).hasSize(2);
		then(actual.getItems().get(0).getDetail().getSpec().getCategory())
			.isEqualTo(expected);
		then(actual.getItems().get(1).getDetail().getSpec().getCategory())
			.isEqualTo(expected);
	}

	@SuppressWarnings("unchecked")
	private static Map<Class<?>, ?> getFieldsCache() throws Exception {
		Field fieldsField = TypeCache.class.getDeclaredField("FIELDS");
		fieldsField.setAccessible(true);
		return (Map<Class<?>, ?>)fieldsField.get(null);
	}
}
