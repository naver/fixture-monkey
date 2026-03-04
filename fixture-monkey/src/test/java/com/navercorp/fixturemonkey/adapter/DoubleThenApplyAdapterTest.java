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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.CompositeObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.IntWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NestedListObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringIntComposite;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ThreeLevelMid;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ThreeLevelObject;

@PropertyDefaults(tries = 10)
class DoubleThenApplyAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void directDoubleThenApplyMultiSampleShouldApplyBoth() {
		ArbitraryBuilder<StringIntComposite> builder = SUT.giveMeBuilder(StringIntComposite.class)
			.thenApply((obj, b) -> b.set("value1.value", "fromFirst"))
			.thenApply((obj, b) -> b.set("value2.value", 42));

		for (int i = 0; i < 5; i++) {
			StringIntComposite actual = builder.sample();

			then(actual.getValue1().getValue()).isEqualTo("fromFirst");
			then(actual.getValue2().getValue()).isEqualTo(42);
		}
	}

	@Property
	void registerThenApplyWithDirectDoubleThenApplyMultiSample() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringWrapper.class, fixture ->
				fixture.giveMeBuilder(StringWrapper.class)
					.thenApply((obj, builder) -> builder.set("value", "registered"))
			)
			.build();

		ArbitraryBuilder<StringIntComposite> builder = sut
			.giveMeBuilder(StringIntComposite.class)
			.thenApply((obj, b) -> b.set("value1.value", "fromFirst"))
			.thenApply((obj, b) -> b.set("value2.value", 42));

		for (int i = 0; i < 5; i++) {
			StringIntComposite actual = builder.sample();

			then(actual.getValue1().getValue()).isEqualTo("fromFirst");
			then(actual.getValue2().getValue()).isEqualTo(42);
		}
	}

	@Property
	void registerDoubleThenApplyWithDirectDoubleThenApplyMultiSample() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringWrapper.class, fixture ->
				fixture
					.giveMeBuilder(StringWrapper.class)
					.thenApply((obj, builder) -> builder.set("value", "reg1"))
					.thenApply((obj, builder) -> builder.set("value", "reg2"))
			)
			.build();

		ArbitraryBuilder<StringIntComposite> builder = sut
			.giveMeBuilder(StringIntComposite.class)
			.thenApply((obj, b) -> b.set("value1.value", "fromFirst"))
			.thenApply((obj, b) -> b.set("value2.value", 42));

		for (int i = 0; i < 5; i++) {
			StringIntComposite actual = builder.sample();

			then(actual.getValue1().getValue()).isEqualTo("fromFirst");
			then(actual.getValue2().getValue()).isEqualTo(42);
		}
	}

	@Property
	void nestedRegisteredThenApplyWithDirectDoubleThenApplyMultiSample() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(SimpleObject.class, fixture ->
				fixture.giveMeBuilder(SimpleObject.class).thenApply((obj, builder) -> builder.set("str", "regStr"))
			)
			.build();

		ArbitraryBuilder<StringIntComposite> builder = sut
			.giveMeBuilder(StringIntComposite.class)
			.thenApply((obj, b) -> {
				StringWrapper sv = sut.giveMeBuilder(StringWrapper.class).set("value", "built").sample();
				IntWrapper iv = sut.giveMeBuilder(IntWrapper.class).set("value", 100).sample();
				b.set("value1", sv);
				b.set("value2", iv);
			})
			.thenApply((obj, b) -> {
				b.set("value2.value", 42);
			});

		for (int i = 0; i < 10; i++) {
			StringIntComposite actual = builder.sample();

			then(actual.getValue1().getValue()).isEqualTo("built");
			then(actual.getValue2().getValue()).isEqualTo(42);
		}
	}

	@Property
	void doubleThenApplySecondSampleShouldApplyOverride() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		ArbitraryBuilder<StringIntComposite> builder = sut
			.giveMeBuilder(StringIntComposite.class)
			.thenApply((obj, b) -> {
				StringWrapper sv = sut.giveMeBuilder(StringWrapper.class).set("value", "fixed").sample();
				b.set("value1", sv);
			})
			.thenApply((obj, b) -> {
				b.set("value2.value", 42);
			});

		for (int i = 0; i < 5; i++) {
			StringIntComposite actual = builder.sample();

			then(actual.getValue1().getValue()).isEqualTo("fixed");
			then(actual.getValue2().getValue()).isEqualTo(42);
		}
	}

	@Property
	void doubleThenApplyWithRegisteredChildSecondSampleShouldApplyOverride() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringWrapper.class, fixture ->
				fixture.giveMeBuilder(StringWrapper.class)
					.thenApply((obj, builder) -> builder.set("value", "registered"))
			)
			.build();

		ArbitraryBuilder<StringIntComposite> builder = sut
			.giveMeBuilder(StringIntComposite.class)
			.thenApply((obj, b) -> {
				StringWrapper sv = sut.giveMeBuilder(StringWrapper.class).set("value", "built").sample();
				b.set("value1", sv);
				b.set("value2.value", 100);
			})
			.thenApply((obj, b) -> {
				b.set("value2.value", 42);
			});

		for (int i = 0; i < 5; i++) {
			StringIntComposite actual = builder.sample();

			then(actual.getValue1().getValue()).isEqualTo("built");
			then(actual.getValue2().getValue()).isEqualTo(42);
		}
	}

	@Property
	void doubleThenApplyWithComplexObjectMultiSample() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(SimpleObject.class, fixture ->
				fixture.giveMeBuilder(SimpleObject.class).thenApply((obj, builder) -> builder.set("str", "regStr"))
			)
			.build();

		ArbitraryBuilder<ComplexObject> builder = sut
			.giveMeBuilder(ComplexObject.class)
			.set("strList", new ArrayList<>())
			.thenApply((obj, b) -> {
				SimpleObject simpleObj = sut.giveMeBuilder(SimpleObject.class).set("str", "fromBuilder").sample();
				b.set("object", simpleObj);
			})
			.thenApply((obj, b) -> {
				b.set("str", "overridden");
			});

		for (int i = 0; i < 5; i++) {
			ComplexObject actual = builder.sample();

			then(actual.getObject().getStr()).isEqualTo("fromBuilder");
			then(actual.getStr()).isEqualTo("overridden");
		}
	}

	@Property
	void tripleThenApplySecondSampleShouldApplyAll() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		ArbitraryBuilder<StringIntComposite> builder = sut
			.giveMeBuilder(StringIntComposite.class)
			.thenApply((obj, b) -> {
				b.set("value1.value", "first");
			})
			.thenApply((obj, b) -> {
				b.set("value2.value", 42);
			})
			.thenApply((obj, b) -> {
				b.set("value1.value", obj.getValue1().getValue() + "_modified");
			});

		for (int i = 0; i < 5; i++) {
			StringIntComposite actual = builder.sample();

			then(actual.getValue1().getValue()).isEqualTo("first_modified");
			then(actual.getValue2().getValue()).isEqualTo(42);
		}
	}

	@Property
	void doubleThenApplyWithSharedLazyAndRegisteredBuildersMultiSample() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(SimpleObject.class, fixture ->
				fixture.giveMeBuilder(SimpleObject.class).thenApply((obj, builder) -> builder.set("str", "regStr"))
			)
			.build();

		ArbitraryBuilder<ComplexObject> builder = sut
			.giveMeBuilder(ComplexObject.class)
			.set("strList", new ArrayList<>())
			.size("list", 2)
			.thenApply((obj, b) -> {
				SimpleObject simpleObj = sut.giveMeBuilder(SimpleObject.class).set("str", "builtStr").sample();
				b.set("object", simpleObj);
			})
			.thenApply((obj, b) -> {
				b.set("integer", 42);
				b.set("object.str", "overridden");
			});

		for (int i = 0; i < 3; i++) {
			System.err.println("\n\n====== OUTER SAMPLE #" + i + " ======\n");
			ComplexObject actual = builder.sample();

			then(actual.getInteger()).as("sample #%d: integer should be 42", i).isEqualTo(42);
			then(actual.getObject().getStr())
				.as("sample #%d: object.str should be overridden", i)
				.isEqualTo("overridden");
		}
	}

	@Property
	void compositePatternRegisterMidFirst() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).thenApply((obj, builder) -> builder.set("code", "123456"))
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((obj, builder) -> builder.set("category", "CAT001"))
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((obj, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).sample();
				ThreeLevelObject right = sut.giveMeBuilder(ThreeLevelObject.class).size("mids", 2).sample();
				b.set("left", left);
				b.set("right", right);
			})
			.thenApply((obj, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		for (int i = 0; i < 3; i++) {
			CompositeObject actual = builder.sample();
			then(actual.getLeft().getStockQuantity()).as("sample #%d: stockQuantity should be 20", i).isEqualTo(20L);
		}
	}

	@Property
	void compositePatternRegisterObjectFirst() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((obj, builder) -> builder.set("category", "CAT001"))
			)
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).thenApply((obj, builder) -> builder.set("code", "123456"))
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((obj, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).sample();
				ThreeLevelObject right = sut.giveMeBuilder(ThreeLevelObject.class).size("mids", 2).sample();
				b.set("left", left);
				b.set("right", right);
			})
			.thenApply((obj, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		for (int i = 0; i < 3; i++) {
			CompositeObject actual = builder.sample();
			then(actual.getLeft().getStockQuantity()).as("sample #%d: stockQuantity should be 20", i).isEqualTo(20L);
		}
	}

	@Property
	void compositePatternWithCompositeObjectRegistered() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).thenApply((obj, builder) -> builder.set("code", "123456"))
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((obj, builder) -> builder.set("category", "CAT001"))
			)
			.register(CompositeObject.class, fixture ->
				fixture
					.giveMeBuilder(CompositeObject.class)
					.thenApply((obj, b) -> {
						NestedListObject left = fixture.giveMeBuilder(NestedListObject.class).sample();
						ThreeLevelObject right = fixture.giveMeBuilder(ThreeLevelObject.class).size("mids", 2).sample();
						b.set("left", left);
						b.set("right", right);
					})
					.thenApply((obj, b) -> {
						b.set("left.stockQuantity", 20L);
						b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
					})
			)
			.build();

		for (int i = 0; i < 3; i++) {
			CompositeObject actual = sut.giveMeOne(CompositeObject.class);
			then(actual.getLeft().getStockQuantity()).as("sample #%d: stockQuantity should be 20", i).isEqualTo(20L);
		}
	}

	@Property
	void compositePatternRegisteredBuilderMultiSample() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).thenApply((obj, builder) -> builder.set("code", "123456"))
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((obj, builder) -> builder.set("category", "CAT001"))
			)
			.register(CompositeObject.class, fixture ->
				fixture
					.giveMeBuilder(CompositeObject.class)
					.thenApply((obj, b) -> {
						NestedListObject left = fixture.giveMeBuilder(NestedListObject.class).sample();
						ThreeLevelObject right = fixture.giveMeBuilder(ThreeLevelObject.class).size("mids", 2).sample();
						b.set("left", left);
						b.set("right", right);
					})
					.thenApply((obj, b) -> {
						b.set("left.stockQuantity", 20L);
						b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
					})
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut.giveMeBuilder(CompositeObject.class);
		for (int i = 0; i < 3; i++) {
			CompositeObject actual = builder.sample();
			then(actual.getLeft().getStockQuantity()).as("sample #%d: stockQuantity should be 20", i).isEqualTo(20L);
		}
	}

	@Property
	void registerWithDoubleThenApplyBothShouldApply() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringIntComposite.class, fixture ->
				fixture
					.giveMeBuilder(StringIntComposite.class)
					.thenApply((obj, builder) -> builder.set("value1.value", "fromFirst"))
					.thenApply((obj, builder) -> builder.set("value2.value", 42))
			)
			.build();

		// when
		StringIntComposite actual = sut.giveMeOne(StringIntComposite.class);

		then(actual.getValue1().getValue()).isEqualTo("fromFirst");
		then(actual.getValue2().getValue()).isEqualTo(42);
	}

	@Property
	void directDoubleThenApply_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void directDoubleThenApply_giveMeOneTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		CompositeObject first = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
			})
			.sample();

		CompositeObject second = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
			})
			.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void directDoubleThenApply_sampleList() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		List<CompositeObject> results = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
			})
			.sampleList(3);

		// then
		for (CompositeObject result : results) {
			then(result.getLeft().getStockQuantity()).isEqualTo(20L);
		}
	}

	@Property
	void registeredChildSetOnly_directDoubleThenApply_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REGISTERED_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelObject.class).set("price", 648L).set("category", "CAT01")
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void registeredChildThenApply_directDoubleThenApply_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelMid.class)
					.thenApply((item, b) -> b.set("code", "LAZY_ITEM_" + item.getPrice()))
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "LAZY_CAT_" + product.getId()))
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void registeredChildDoubleThenApply_directDoubleThenApply_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelMid.class)
					.thenApply((item, b) -> b.set("code", "ITEM_" + item.getPrice()))
					.thenApply((item, b) -> b.set("memo", "MEMO_" + item.getCode()))
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void registeredDoubleThenApply_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.register(CompositeObject.class, fixture ->
				fixture
					.giveMeBuilder(CompositeObject.class)
					.thenApply((f, b) -> {
						NestedListObject left = fixture.giveMeBuilder(NestedListObject.class).fixed().sample();
						b.set("left", left);
					})
					.thenApply((f, b) -> {
						b.set("left.stockQuantity", 20L);
						b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
					})
			)
			.build();

		// when
		CompositeObject first = sut.giveMeOne(CompositeObject.class);
		CompositeObject second = sut.giveMeOne(CompositeObject.class);

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void registeredDoubleThenApply_giveMeBuilderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.register(CompositeObject.class, fixture ->
				fixture
					.giveMeBuilder(CompositeObject.class)
					.thenApply((f, b) -> {
						NestedListObject left = fixture.giveMeBuilder(NestedListObject.class).fixed().sample();
						b.set("left", left);
					})
					.thenApply((f, b) -> {
						b.set("left.stockQuantity", 20L);
						b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
					})
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut.giveMeBuilder(CompositeObject.class);

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void directDoubleThenApply_externalSampleNoFixed_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelObject.class).set("price", 648L)
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void directDoubleThenApply_externalSampleFixed_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelObject.class).set("price", 648L)
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				ThreeLevelObject right = sut.giveMeBuilder(ThreeLevelObject.class).fixed().sample();
				b.set("left", left);
				b.set("right", right);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void registerOrderItemFirst_directDoubleThenApply_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				ThreeLevelObject right = sut.giveMeBuilder(ThreeLevelObject.class).fixed().sample();
				b.set("left", left);
				b.set("right", right);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void registerOrderProductFirst_directDoubleThenApply_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				ThreeLevelObject right = sut.giveMeBuilder(ThreeLevelObject.class).fixed().sample();
				b.set("left", left);
				b.set("right", right);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void directTripleThenApply_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelObject.class).set("price", 648L)
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				ThreeLevelObject product = sut.giveMeBuilder(ThreeLevelObject.class).fixed().sample();
				b.set("right", product);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void directDoubleThenApply_outerFixed_sampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelObject.class).set("price", 648L)
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
			})
			.fixed();

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void directDoubleThenApply_interleavedOtherTypeSampling() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelObject.class).set("price", 648L)
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		// when
		CompositeObject first = builder.sample();
		sut.giveMeOne(NestedListObject.class);
		sut.giveMeOne(ThreeLevelObject.class);
		sut.giveMeOne(SimpleObject.class);
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void directDoubleThenApply_withContainerSize_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelObject.class).set("price", 648L)
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
				b.size("right.mids", 3);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.size("left.entries", 2);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(first.getLeft().getEntries()).hasSize(2);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getEntries()).hasSize(2);
	}

	@Property
	void registeredDoubleThenApply_directAdditionalThenApply_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.register(CompositeObject.class, fixture ->
				fixture
					.giveMeBuilder(CompositeObject.class)
					.thenApply((f, b) -> {
						NestedListObject left = fixture.giveMeBuilder(NestedListObject.class).fixed().sample();
						b.set("left", left);
					})
					.thenApply((f, b) -> {
						b.set("left.stockQuantity", 20L);
					})
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void complexObject_directDoubleThenApply_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(SimpleObject.class, fixture -> fixture.giveMeBuilder(SimpleObject.class).set("str", "REGISTERED"))
			.build();

		ArbitraryBuilder<ComplexObject> builder = sut
			.giveMeBuilder(ComplexObject.class)
			.thenApply((obj, b) -> {
				StringWrapper sv = sut.giveMeBuilder(StringWrapper.class).fixed().sample();
				b.set("str", sv.getValue());
			})
			.thenApply((obj, b) -> {
				b.set("strList", Arrays.asList("a", "b", "c"));
			});

		// when
		ComplexObject first = builder.sample();
		ComplexObject second = builder.sample();

		// then
		then(first.getStrList()).containsExactly("a", "b", "c");
		then(second.getStrList()).containsExactly("a", "b", "c");
	}

	@Property
	void directDoubleThenApply_setBothFieldsInFirst_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				ThreeLevelObject right = sut.giveMeBuilder(ThreeLevelObject.class).fixed().sample();
				b.set("left", left);
				b.set("right", right);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(first.getRight().getPrice()).isEqualTo(648L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getRight().getPrice()).isEqualTo(648L);
	}

	@Property
	void directDoubleThenApply_manySamples() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				ThreeLevelObject right = sut.giveMeBuilder(ThreeLevelObject.class).fixed().sample();
				b.set("left", left);
				b.set("right", right);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		for (int i = 0; i < 10; i++) {
			CompositeObject result = builder.sample();
			then(result.getLeft().getStockQuantity()).as("sample #%d stockQuantity", i).isEqualTo(20L);
		}
	}

	@Property
	void registeredWithSize_directDoubleThenApply_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM").size("leaves", 2)
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelObject.class).set("price", 648L).size("mids", 3)
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(first.getRight().getMids()).hasSize(3);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getRight().getMids()).hasSize(3);
	}

	@Property
	void directThenApplyThenAcceptIf_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).fixed().sample();
				b.set("left", left);
			})
			.acceptIf(
				fixture -> true,
				b -> {
					b.set("left.stockQuantity", 20L);
				}
			);

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void directDoubleThenApply_sharedFixtureMonkey_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(NestedListObject.class, fixture ->
				fixture.giveMeBuilder(NestedListObject.class).set("id", 12345L).size("entries", 2).size("items", 1)
			)
			.register(ThreeLevelMid.class, fixture ->
				fixture.giveMeBuilder(ThreeLevelMid.class).set("code", "REG_ITEM")
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeOne(NestedListObject.class);
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
				b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(first.getLeft().getId()).isEqualTo(12345L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getId()).isEqualTo(12345L);
	}

	@Property
	void registeredBuilderSamplesOtherRegisteredBuilder_doubleThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(NestedListObject.class, fixture ->
				fixture.giveMeBuilder(NestedListObject.class).set("id", 12345L).size("entries", 2).size("items", 1)
			)
			.register(ThreeLevelMid.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelMid.class)
					.thenApply((item, b) -> b.set("code", "LAZY_" + item.getPrice()))
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.register(CompositeObject.class, fixture ->
				fixture
					.giveMeBuilder(CompositeObject.class)
					.thenApply((f, b) -> {
						NestedListObject left = fixture.giveMeBuilder(NestedListObject.class).fixed().sample();
						ThreeLevelObject right = fixture.giveMeBuilder(ThreeLevelObject.class).fixed().sample();
						b.set("left", left);
						b.set("right", right);
					})
					.thenApply((f, b) -> {
						b.set("left.stockQuantity", 20L);
						b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
					})
			)
			.build();

		// when
		CompositeObject first = sut.giveMeOne(CompositeObject.class);
		CompositeObject second = sut.giveMeOne(CompositeObject.class);

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(first.getLeft().getId()).isEqualTo(12345L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getId()).isEqualTo(12345L);
	}

	@Property
	void registeredDoubleThenApply_builderFromGiveMeBuilder_sampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(NestedListObject.class, fixture ->
				fixture.giveMeBuilder(NestedListObject.class).set("id", 12345L)
			)
			.register(ThreeLevelMid.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelMid.class)
					.thenApply((item, b) -> b.set("code", "LAZY_" + item.getPrice()))
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.register(CompositeObject.class, fixture ->
				fixture
					.giveMeBuilder(CompositeObject.class)
					.thenApply((f, b) -> {
						NestedListObject left = fixture.giveMeBuilder(NestedListObject.class).fixed().sample();
						ThreeLevelObject right = fixture.giveMeBuilder(ThreeLevelObject.class).fixed().sample();
						b.set("left", left);
						b.set("right", right);
					})
					.thenApply((f, b) -> {
						b.set("left.stockQuantity", 20L);
						b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
					})
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut.giveMeBuilder(CompositeObject.class);

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();
		CompositeObject third = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
		then(third.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void registeredDoubleThenApply_directSetOverride_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelMid.class)
					.thenApply((item, b) -> b.set("code", "LAZY_" + item.getPrice()))
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.register(CompositeObject.class, fixture ->
				fixture
					.giveMeBuilder(CompositeObject.class)
					.thenApply((f, b) -> {
						NestedListObject left = fixture.giveMeBuilder(NestedListObject.class).fixed().sample();
						b.set("left", left);
					})
					.thenApply((f, b) -> {
						b.set("left.stockQuantity", 20L);
					})
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.set("left.items[*].stockQuantity", Long.MAX_VALUE);

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		// then
		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
	}

	@Property
	void setObjectInThenApply_nestedContainerSizePreserved_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.thenApply((fixture, b) -> {
				NestedListObject left = sut.giveMeBuilder(NestedListObject.class).size("entries", 3).fixed().sample();
				b.set("left", left);
			})
			.thenApply((fixture, b) -> {
				b.set("left.stockQuantity", 20L);
			});

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(first.getLeft().getEntries()).hasSize(3);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getEntries()).hasSize(3);
	}

	@Property
	void registeredDoubleThenApply_nestedContainerSizePreserved_builderSampleTwice() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ThreeLevelMid.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelMid.class)
					.thenApply((item, b) -> b.set("code", "LAZY_" + item.getPrice()))
			)
			.register(ThreeLevelObject.class, fixture ->
				fixture
					.giveMeBuilder(ThreeLevelObject.class)
					.set("price", 648L)
					.thenApply((product, b) -> b.set("category", "CAT_" + product.getId()))
			)
			.register(CompositeObject.class, fixture ->
				fixture
					.giveMeBuilder(CompositeObject.class)
					.thenApply((f, b) -> {
						NestedListObject left = fixture
							.giveMeBuilder(NestedListObject.class)
							.size("entries", 3)
							.fixed()
							.sample();
						b.set("left", left);
					})
					.thenApply((f, b) -> {
						b.set("left.stockQuantity", 20L);
					})
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut
			.giveMeBuilder(CompositeObject.class)
			.set("left.items[*].stockQuantity", Long.MAX_VALUE);

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();

		then(first.getLeft().getStockQuantity()).isEqualTo(20L);
		then(first.getLeft().getEntries()).hasSize(3);
		then(second.getLeft().getStockQuantity()).isEqualTo(20L);
		then(second.getLeft().getEntries()).hasSize(3);
	}

	@Property
	void registeredDoubleThenApply_childAndWildcardSet_consistentAcrossSamples() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(CompositeObject.class, fixture ->
				fixture
					.giveMeBuilder(CompositeObject.class)
					.thenApply((f, b) -> {
						NestedListObject left = fixture
							.giveMeBuilder(NestedListObject.class)
							.size("items", 1)
							.fixed()
							.sample();
						b.set("left", left);
					})
					.thenApply((f, b) -> {
						b.set("left.stockQuantity", 999L);
						b.set("left.items[*].stockQuantity", Long.MAX_VALUE);
					})
			)
			.build();

		ArbitraryBuilder<CompositeObject> builder = sut.giveMeBuilder(CompositeObject.class);

		// when
		CompositeObject first = builder.sample();
		CompositeObject second = builder.sample();
		CompositeObject third = builder.sample();

		for (CompositeObject result : new CompositeObject[] {first, second, third}) {
			then(result.getLeft().getStockQuantity()).isEqualTo(999L);
			then(result.getLeft().getItems()).isNotEmpty();
			for (NestedListObject.Item supplement : result.getLeft().getItems()) {
				then(supplement.getStockQuantity()).isEqualTo(Long.MAX_VALUE);
			}
		}
	}
}
