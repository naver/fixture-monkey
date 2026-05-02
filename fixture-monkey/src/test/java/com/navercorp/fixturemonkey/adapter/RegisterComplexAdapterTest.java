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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.tracing.AdapterTracer;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.DeepObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.DoubleNestedStringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.Envelope;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NestedStringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.RichObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.WrapperObject;

/**
 * Reproduction tests for complex registered builder scenarios.
 * Tests various suspicious patterns from fixture-test.txt trace analysis.
 */
@PropertyDefaults(tries = 10)
class RegisterComplexAdapterTest {

	@Property
	void registeredBuilderFieldLevelSetLazyShouldApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(DeepObject.Policy.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
			)
			.register(DeepObject.Policy.QuantityRule.class, fixture ->
				fixture.giveMeBuilder(DeepObject.Policy.QuantityRule.class).setLazy("type", () -> "REPEAT")
			)
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture.giveMeBuilder(DeepObject.Policy.AreaRule.class).setLazy("enabled", () -> true)
			)
			.register(DeepObject.Option.class, fixture ->
				fixture.giveMeBuilder(DeepObject.Option.class).setLazy("name", () -> "COLOR")
			)
			.build();

		// when
		DeepObject actual = sut.giveMeOne(DeepObject.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getType()).isEqualTo("TAX");
		then(actual.getPolicy().getMethod()).isEqualTo("DELIVERY");
		then(actual.getPolicy().getFeeType()).isEqualTo("CONDITIONAL_FREE");
		then(actual.getPolicy().getPayType()).isEqualTo("PAY_SELECT");
		then(actual.getPolicy().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getPolicy().getAreaRule().isEnabled()).isTrue();
	}

	@Property
	void exactTracePattern_sixRegisteredBuildersZeroActive() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class)
					.set("type", "3")
					.thenApply((obj, builder) -> {
						builder.set("from", BigDecimal.ZERO);
						builder.set("to", BigDecimal.valueOf(Long.parseLong(obj.getType()) * 1000));
					})
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.OptionSpec.class).setLazy("type", () -> "SELECT")
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.thenApply((obj, builder) -> builder.set("repeat", "REPEAT".equals(obj.getType()) ? 2L : 1L))
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class)
					.setLazy("enabled", () -> true)
					.thenApply((obj, builder) -> builder.set("unit", obj.isEnabled() ? "AREA" : "NONE"))
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> builder.set("groupId", "GRP_" + obj.getMethod()))
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getDeliveryInfo().getMethod()).isEqualTo("DELIVERY");
		then(actual.getDeliveryInfo().getFeeType()).isEqualTo("CONDITIONAL_FREE");
		then(actual.getDeliveryInfo().getPayType()).isEqualTo("PAY_SELECT");
		then(actual.getDeliveryInfo().getGroupId()).isEqualTo("GRP_DELIVERY");
		then(actual.getDeliveryInfo().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getDeliveryInfo().getQuantityRule().getRepeat()).isEqualTo(2L);
		then(actual.getDeliveryInfo().getAreaRule().isEnabled()).isTrue();
		then(actual.getDeliveryInfo().getAreaRule().getUnit()).isEqualTo("AREA");
		then(actual.getDeliveryInfo().getRange().getType()).isEqualTo("3");
		then(actual.getDeliveryInfo().getRange().getTo()).isEqualByComparingTo(BigDecimal.valueOf(3000));
	}

	@Property
	void registeredNestedTypeViaWrapperWithSixBuilders() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class)
					.set("type", "3")
					.thenApply((obj, builder) -> {
						builder.set("from", BigDecimal.ZERO);
						builder.set("to", BigDecimal.valueOf(Long.parseLong(obj.getType()) * 1000));
					})
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.OptionSpec.class).setLazy("type", () -> "SELECT")
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.thenApply((obj, builder) -> builder.set("repeat", "REPEAT".equals(obj.getType()) ? 2L : 1L))
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class)
					.setLazy("enabled", () -> true)
					.thenApply((obj, builder) -> builder.set("unit", obj.isEnabled() ? "AREA" : "NONE"))
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> builder.set("groupId", "GRP_" + obj.getMethod()))
			)
			.build();

		// when
		FixtureMonkeyTestSpecs.Envelope actual = sut.giveMeOne(FixtureMonkeyTestSpecs.Envelope.class);

		// then
		then(actual.getBody().getStatus()).isEqualTo("ON_SALE");
		then(actual.getBody().getTaxType()).isEqualTo("TAX");
		then(actual.getBody().getDeliveryInfo().getMethod()).isEqualTo("DELIVERY");
		then(actual.getBody().getDeliveryInfo().getGroupId()).isEqualTo("GRP_DELIVERY");
		then(actual.getBody().getDeliveryInfo().getQuantityRule().getRepeat()).isEqualTo(2L);
		then(actual.getBody().getDeliveryInfo().getAreaRule().getUnit()).isEqualTo("AREA");
	}

	@Property
	void rootTypeWithBothFieldSetLazyAndThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
					.thenApply((obj, builder) -> builder.set("name", "STATUS_" + obj.getStatus()))
			)
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class)
					.set("type", "3")
					.thenApply((obj, builder) ->
						builder.set("from", BigDecimal.valueOf(Integer.parseInt(obj.getType())))
					)
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.thenApply((obj, builder) -> builder.set("repeat", "REPEAT".equals(obj.getType()) ? 5L : 0L))
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class)
					.setLazy("enabled", () -> true)
					.thenApply((obj, builder) -> {
						builder.set("unit", obj.isEnabled() ? "REGION" : "NONE");
						builder.set("rate1", BigDecimal.valueOf(3000));
					})
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						String suffix = "DELIVERY".equals(obj.getMethod()) ? "DLV" : "PKP";
						builder.set("groupId", "GRP_" + suffix);
					})
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getName()).isEqualTo("STATUS_ON_SALE");
		then(actual.getDeliveryInfo().getGroupId()).isEqualTo("GRP_DLV");
		then(actual.getDeliveryInfo().getAreaRule().getUnit()).isEqualTo("REGION");
		then(actual.getDeliveryInfo().getAreaRule().getRate1()).isEqualByComparingTo(BigDecimal.valueOf(3000));
	}

	@Property
	void rootThenApplyConditionalMultiFieldSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class)
					.set("type", "3")
					.thenApply((obj, builder) -> {
						int multiplier = Integer.parseInt(obj.getType());
						builder.set("from", BigDecimal.valueOf(multiplier * 100));
						builder.set("to", BigDecimal.valueOf(multiplier * 200));
					})
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.OptionSpec.class)
					.setLazy("type", () -> "SELECT")
					.thenApply((obj, builder) -> builder.set("name", "OPT_" + obj.getType()))
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
					.thenApply((obj, builder) -> {
						if ("ON_SALE".equals(obj.getStatus())) {
							builder.set("supplementSupport", true);
							builder.set("optionSupport", true);
						} else {
							builder.set("supplementSupport", false);
							builder.set("optionSupport", false);
						}
					})
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.thenApply((obj, builder) -> builder.set("repeat", "REPEAT".equals(obj.getType()) ? 3L : 1L))
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class)
					.setLazy("enabled", () -> true)
					.thenApply((obj, builder) -> {
						if (obj.isEnabled()) {
							builder.set("unit", "DISTRICT");
							builder.set("rate1", BigDecimal.valueOf(2500));
							builder.set("rate2", BigDecimal.valueOf(5000));
						}
					})
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						builder.set("groupId", obj.getMethod() + "_" + obj.getFeeType());
					})
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getSupplementSupport()).isTrue();
		then(actual.getOptionSupport()).isTrue();
		then(actual.getDeliveryInfo().getGroupId()).isEqualTo("DELIVERY_CONDITIONAL_FREE");
		then(actual.getDeliveryInfo().getQuantityRule().getRepeat()).isEqualTo(3L);
		then(actual.getDeliveryInfo().getAreaRule().getUnit()).isEqualTo("DISTRICT");
		then(actual.getDeliveryInfo().getAreaRule().getRate1()).isEqualByComparingTo(BigDecimal.valueOf(2500));
		then(actual.getDeliveryInfo().getAreaRule().getRate2()).isEqualByComparingTo(BigDecimal.valueOf(5000));
		then(actual.getDeliveryInfo().getRange().getFrom()).isEqualByComparingTo(BigDecimal.valueOf(300));
		then(actual.getDeliveryInfo().getRange().getTo()).isEqualByComparingTo(BigDecimal.valueOf(600));
	}

	@Property
	void giveMeOneWithZeroActiveAndTwoRegisteredBuilders() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(DeepObject.Policy.class, fixture ->
				fixture.giveMeBuilder(DeepObject.Policy.class).setLazy("method", () -> "DELIVERY")
			)
			.build();

		// when
		DeepObject actual = sut.giveMeOne(DeepObject.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getType()).isEqualTo("TAX");
		then(actual.getPolicy().getMethod()).isEqualTo("DELIVERY");
	}

	@Property
	void rootFieldSetLazyWithNestedConditionalThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(DeepObject.Policy.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						if ("CONDITIONAL_FREE".equals(obj.getFeeType())) {
							builder.set("groupId", "FREE_" + obj.getMethod());
						} else {
							builder.set("groupId", "PAID_" + obj.getMethod());
						}
					})
			)
			.register(DeepObject.Policy.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.thenApply((obj, builder) -> builder.set("repeat", "REPEAT".equals(obj.getType()) ? 4L : 1L))
			)
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.AreaRule.class)
					.setLazy("enabled", () -> true)
					.thenApply((obj, builder) -> builder.set("unit", obj.isEnabled() ? "ZIP" : "NONE"))
			)
			.build();

		// when
		DeepObject actual = sut.giveMeOne(DeepObject.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getType()).isEqualTo("TAX");
		then(actual.getPolicy().getMethod()).isEqualTo("DELIVERY");
		then(actual.getPolicy().getFeeType()).isEqualTo("CONDITIONAL_FREE");
		then(actual.getPolicy().getPayType()).isEqualTo("PAY_SELECT");
		then(actual.getPolicy().getGroupId()).isEqualTo("FREE_DELIVERY");
		then(actual.getPolicy().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getPolicy().getQuantityRule().getRepeat()).isEqualTo(4L);
		then(actual.getPolicy().getAreaRule().isEnabled()).isTrue();
		then(actual.getPolicy().getAreaRule().getUnit()).isEqualTo("ZIP");
	}

	@Property
	void multipleSamplesWithConditionalThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class)
					.set("type", "3")
					.thenApply((obj, builder) ->
						builder.set("to", BigDecimal.valueOf(Long.parseLong(obj.getType()) * 500))
					)
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.OptionSpec.class)
					.setLazy("type", () -> "SELECT")
					.thenApply((obj, builder) -> builder.set("name", obj.getType() + "_ITEM"))
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.thenApply((obj, builder) -> builder.set("repeat", "REPEAT".equals(obj.getType()) ? 10L : 1L))
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class)
					.setLazy("enabled", () -> true)
					.thenApply((obj, builder) -> builder.set("unit", obj.isEnabled() ? "METRO" : "RURAL"))
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> builder.set("groupId", obj.getFeeType() + "_" + obj.getPayType()))
			)
			.build();

		for (int i = 0; i < 10; i++) {
			// when
			Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

			// then
			then(actual.getStatus()).as("sample %d status", i).isEqualTo("ON_SALE");
			then(actual.getTaxType()).as("sample %d taxType", i).isEqualTo("TAX");
			then(actual.getDeliveryInfo().getGroupId())
				.as("sample %d groupId", i)
				.isEqualTo("CONDITIONAL_FREE_PAY_SELECT");
			then(actual.getDeliveryInfo().getQuantityRule().getRepeat()).as("sample %d repeat", i).isEqualTo(10L);
			then(actual.getDeliveryInfo().getAreaRule().getUnit()).as("sample %d split", i).isEqualTo("METRO");
		}
	}

	@Property
	void registeredSetWithConditionalThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class)
					.set("type", "3")
					.set("from", BigDecimal.valueOf(0))
					.set("to", BigDecimal.valueOf(100))
					.thenApply((obj, builder) -> {
						if (obj.getTo().compareTo(BigDecimal.valueOf(50)) > 0) {
							builder.set("type", obj.getType() + "_LARGE");
						}
					})
			)
			.build();

		// when
		Envelope.Body.DeliveryInfo.Range actual = sut.giveMeOne(Envelope.Body.DeliveryInfo.Range.class);

		// then
		then(actual.getType()).isEqualTo("3_LARGE");
		then(actual.getFrom()).isEqualByComparingTo(BigDecimal.ZERO);
		then(actual.getTo()).isEqualByComparingTo(BigDecimal.valueOf(100));
	}

	@Property
	void directSetOverridesRegisteredSetLazy() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeBuilder(Envelope.Body.class).set("status", "SOLD_OUT").sample();

		// then
		then(actual.getStatus()).isEqualTo("SOLD_OUT");
		then(actual.getTaxType()).isEqualTo("TAX");
	}

	@Property
	void registeredBuilderOnlyOnDeeplyNestedType() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.thenApply((obj, builder) -> builder.set("repeat", "REPEAT".equals(obj.getType()) ? 7L : 0L))
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getDeliveryInfo().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getDeliveryInfo().getQuantityRule().getRepeat()).isEqualTo(7L);
	}

	@Property
	void registeredContentViaWrapperObjectWithThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(WrapperObject.Content.class, fixture ->
				fixture
					.giveMeBuilder(WrapperObject.Content.class)
					.setLazy("status", () -> "ACTIVE")
					.setLazy("type", () -> "FREE")
					.thenApply((obj, builder) -> builder.set("name", obj.getStatus() + "_" + obj.getType()))
			)
			.register(WrapperObject.ContentItem.class, fixture ->
				fixture
					.giveMeBuilder(WrapperObject.ContentItem.class)
					.setLazy("type", () -> "BASIC")
					.thenApply((obj, builder) -> builder.set("value", "VAL_" + obj.getType()))
			)
			.build();

		// when
		WrapperObject actual = sut.giveMeOne(WrapperObject.class);

		// then
		then(actual.getContent().getStatus()).isEqualTo("ACTIVE");
		then(actual.getContent().getType()).isEqualTo("FREE");
		then(actual.getContent().getName()).isEqualTo("ACTIVE_FREE");
	}

	@Property
	void parentThenApplyWithRealLogicAndChildRegisteredSetLazy() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
					.thenApply((obj, builder) -> builder.set("name", "P_" + obj.getStatus()))
			)
			.register(DeepObject.Policy.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.class)
					.setLazy("method", () -> "DELIVERY")
					.thenApply((obj, builder) -> builder.set("feeType", "FT_" + obj.getMethod()))
			)
			.build();

		// when
		RichObject actual = sut
			.giveMeBuilder(RichObject.class)
			.set("code", "ORD001")
			.thenApply((obj, builder) -> {
				DeepObject product = sut.giveMeOne(DeepObject.class);
				builder.set("content", product);
			})
			.sample();

		// then
		then(actual.getContent().getStatus()).isEqualTo("ON_SALE");
		then(actual.getContent().getType()).isEqualTo("TAX");
		then(actual.getContent().getName()).isEqualTo("P_ON_SALE");
	}

	@Property
	void registeredBuilderContainerSizeWithFieldSetLazy() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
					.size("options", 0)
					.size("addons", 2)
			)
			.build();

		// when
		DeepObject actual = sut.giveMeOne(DeepObject.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getType()).isEqualTo("TAX");
		then(actual.getOptions()).isEmpty();
		then(actual.getAddons()).hasSize(2);
	}

	@Property
	void registeredBuilderDoubleThenApplyWithConditions() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
					.thenApply((obj, builder) -> {
						if ("ON_SALE".equals(obj.getStatus())) {
							builder.set("name", "SELLING_" + obj.getId());
						} else {
							builder.set("name", "STOPPED_" + obj.getId());
						}
					})
					.thenApply((obj, builder) -> {
						if (obj.getName() != null && obj.getName().startsWith("SELLING")) {
							builder.set("flag", true);
						} else {
							builder.set("flag", false);
						}
					})
			)
			.build();

		// when
		DeepObject actual = sut.giveMeOne(DeepObject.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getType()).isEqualTo("TAX");
		then(actual.getName()).startsWith("SELLING_");
		then(actual.getFlag()).isTrue();
	}

	@Property
	void sameRegisteredTypeAtMultipleTreeLocations() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(DeepObject.Policy.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.class)
					.setLazy("method", () -> "DELIVERY")
					.thenApply((obj, builder) -> builder.set("feeType", obj.getMethod() + "_FEE"))
			)
			.build();

		// when
		RichObject actual = sut.giveMeOne(RichObject.class);

		// then
		then(actual.getContent().getStatus()).isEqualTo("ON_SALE");
		then(actual.getContent().getType()).isEqualTo("TAX");
		then(actual.getPolicy().getMethod()).isEqualTo("DELIVERY");
		then(actual.getPolicy().getFeeType()).isEqualTo("DELIVERY_FEE");
		then(actual.getContent().getPolicy().getMethod()).isEqualTo("DELIVERY");
		then(actual.getContent().getPolicy().getFeeType()).isEqualTo("DELIVERY_FEE");
	}

	@Property
	void registeredBuilderMixedSetAndSetLazyWithThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.set("taxType", "TAX")
					.setLazy("status", () -> "ON_SALE")
					.setNull("optionSupport")
					.thenApply((obj, builder) -> builder.set("name", obj.getTaxType() + "_" + obj.getStatus()))
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.set("feeType", "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> builder.set("groupId", obj.getFeeType() + "_GRP"))
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getOptionSupport()).isNull();
		then(actual.getName()).isEqualTo("TAX_ON_SALE");
		then(actual.getDeliveryInfo().getMethod()).isEqualTo("DELIVERY");
		then(actual.getDeliveryInfo().getFeeType()).isEqualTo("CONDITIONAL_FREE");
		then(actual.getDeliveryInfo().getPayType()).isEqualTo("PAY_SELECT");
		then(actual.getDeliveryInfo().getGroupId()).isEqualTo("CONDITIONAL_FREE_GRP");
	}

	@Property
	void registeredBuilderContainerSizeThenApplyFieldSetLazy() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
					.size("optionGroups", 0)
					.size("addonInfos", 2)
					.thenApply((obj, builder) -> builder.set("name", "FIXED_" + obj.getStatus()))
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.thenApply((obj, builder) -> builder.set("groupId", obj.getMethod() + "_" + obj.getFeeType()))
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getName()).isEqualTo("FIXED_ON_SALE");
		then(actual.getOptionGroups()).isEmpty();
		then(actual.getAddonInfos()).hasSize(2);
		then(actual.getDeliveryInfo().getMethod()).isEqualTo("DELIVERY");
		then(actual.getDeliveryInfo().getFeeType()).isEqualTo("CONDITIONAL_FREE");
		then(actual.getDeliveryInfo().getGroupId()).isEqualTo("DELIVERY_CONDITIONAL_FREE");
	}

	@Property
	void registeredThenApplySamplesAnotherRegisteredType() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(RichObject.Metadata.class, fixture ->
				fixture
					.giveMeBuilder(RichObject.Metadata.class)
					.setLazy("accountId", () -> "ACC")
					.setLazy("trackingCode", () -> "TRACK")
					.setLazy("verified", () -> true)
			)
			.register(RichObject.class, fixture ->
				fixture
					.giveMeBuilder(RichObject.class)
					.setLazy("code", () -> "ORD001")
					.thenApply((obj, builder) -> {
						RichObject.Metadata ext = fixture.giveMeOne(RichObject.Metadata.class);
						builder.set("metadata", ext);
					})
			)
			.build();

		// when
		RichObject actual = sut.giveMeOne(RichObject.class);

		// then
		then(actual.getCode()).isEqualTo("ORD001");
		then(actual.getMetadata().getAccountId()).isEqualTo("ACC");
		then(actual.getMetadata().getTrackingCode()).isEqualTo("TRACK");
		then(actual.getMetadata().isVerified()).isTrue();
	}

	@Property
	void sixRegisteredBuildersWithActiveManipulatorOnRoot() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class)
					.set("type", "3")
					.thenApply((obj, builder) ->
						builder.set("to", BigDecimal.valueOf(Long.parseLong(obj.getType()) * 100))
					)
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.OptionSpec.class)
					.setLazy("type", () -> "SELECT")
					.thenApply((obj, builder) -> builder.set("name", "N_" + obj.getType()))
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.thenApply((obj, builder) -> builder.set("repeat", "REPEAT".equals(obj.getType()) ? 2L : 1L))
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class)
					.setLazy("enabled", () -> true)
					.thenApply((obj, builder) -> builder.set("unit", obj.isEnabled() ? "AREA" : "NONE"))
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> builder.set("groupId", "GRP_" + obj.getMethod()))
			)
			.build();

		Envelope.Body actual = sut.giveMeBuilder(Envelope.Body.class).set("name", "MANUAL_NAME").sample();

		// then
		then(actual.getName()).isEqualTo("MANUAL_NAME");
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getDeliveryInfo().getMethod()).isEqualTo("DELIVERY");
		then(actual.getDeliveryInfo().getGroupId()).isEqualTo("GRP_DELIVERY");
	}

	@Property
	void allRegisteredBuildersFieldSetLazyOnlyNoThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class).set("type", "3")
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.OptionSpec.class).setLazy("type", () -> "SELECT")
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class).setLazy("type", () -> "REPEAT")
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class).setLazy("enabled", () -> true)
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getDeliveryInfo().getMethod()).isEqualTo("DELIVERY");
		then(actual.getDeliveryInfo().getFeeType()).isEqualTo("CONDITIONAL_FREE");
		then(actual.getDeliveryInfo().getPayType()).isEqualTo("PAY_SELECT");
		then(actual.getDeliveryInfo().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getDeliveryInfo().getAreaRule().isEnabled()).isTrue();
		then(actual.getDeliveryInfo().getRange().getType()).isEqualTo("3");
	}

	@Property
	void registeredParentThenApplySamplesRegisteredChild() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(RichObject.Discount.class, fixture ->
				fixture
					.giveMeBuilder(RichObject.Discount.class)
					.setLazy("name", () -> "COUPON")
					.thenApply((obj, builder) -> builder.set("seq", "SEQ_" + obj.getName()))
			)
			.register(RichObject.Location.class, fixture ->
				fixture
					.giveMeBuilder(RichObject.Location.class)
					.setLazy("line2", () -> "101호")
					.thenApply((obj, builder) -> builder.set("zip", "ZIP_" + obj.getLine2()))
			)
			.register(RichObject.Metadata.class, fixture ->
				fixture
					.giveMeBuilder(RichObject.Metadata.class)
					.setLazy("accountId", () -> "ACC")
					.thenApply((obj, builder) -> builder.set("trackingCode", "TRACK_" + obj.getAccountId()))
			)
			.register(RichObject.class, fixture ->
				fixture
					.giveMeBuilder(RichObject.class)
					.setLazy("code", () -> "ORD001")
					.size("discounts", 1)
					.thenApply((obj, builder) -> {
						DeepObject product = fixture.giveMeOne(DeepObject.class);
						builder.set("content", product);

						RichObject.Location address = fixture.giveMeOne(RichObject.Location.class);
						builder.set("location", address);

						RichObject.Metadata ext = fixture.giveMeOne(RichObject.Metadata.class);
						builder.set("metadata", ext);
					})
			)
			.build();

		// when
		RichObject actual = sut.giveMeOne(RichObject.class);

		// then
		then(actual.getCode()).isEqualTo("ORD001");
		then(actual.getContent().getStatus()).isEqualTo("ON_SALE");
		then(actual.getContent().getType()).isEqualTo("TAX");
		then(actual.getLocation().getLine2()).isEqualTo("101호");
		then(actual.getLocation().getZip()).isEqualTo("ZIP_101호");
		then(actual.getMetadata().getAccountId()).isEqualTo("ACC");
		then(actual.getMetadata().getTrackingCode()).isEqualTo("TRACK_ACC");
	}

	@Property
	void registeredBuilderSetLazyMixedWithSetNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
					.setNull("optionSupport")
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getOptionSupport()).isNull();
	}

	@Property
	void largeTreeWithRegisteredValuesOnFewFields() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("status", () -> "ON_SALE")
					.setLazy("taxType", () -> "TAX")
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getId()).isNotNull();
		then(actual.getName()).isNotNull();
		then(actual.getProductUrl()).isNotNull();
	}

	@Property
	void registeredBuilderRootSetFollowedByConditionalThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.thenApply((obj, builder) -> {
						if ("REPEAT".equals(obj.getType())) {
							builder.set("repeat", 99L);
						}
					})
			)
			.build();

		// when
		DeepObject.Policy.QuantityRule actual = sut
			.giveMeBuilder(DeepObject.Policy.QuantityRule.class)
			.set("$", sut.giveMeOne(DeepObject.Policy.QuantityRule.class))
			.setNull("repeat")
			.sample();

		// then
		then(actual.getType()).isEqualTo("REPEAT");
		then(actual.getRepeat()).isNull();
	}

	@Property
	void registeredBuilderWithSetAndConditionalThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.set("taxType", "TAX")
					.set("status", "ON_SALE")
					.thenApply((obj, builder) -> {
						if ("ON_SALE".equals(obj.getStatus())) {
							builder.set("name", "AVAILABLE_" + obj.getTaxType());
						}
					})
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.set("method", "DELIVERY")
					.set("feeType", "CONDITIONAL_FREE")
					.thenApply((obj, builder) -> builder.set("groupId", obj.getMethod() + "_" + obj.getFeeType()))
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getName()).isEqualTo("AVAILABLE_TAX");
		then(actual.getDeliveryInfo().getMethod()).isEqualTo("DELIVERY");
		then(actual.getDeliveryInfo().getFeeType()).isEqualTo("CONDITIONAL_FREE");
		then(actual.getDeliveryInfo().getGroupId()).isEqualTo("DELIVERY_CONDITIONAL_FREE");
	}

	@Property
	void shippingPolicyThenApplySetsEntireChildObjectsByMethod() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(DeepObject.Policy.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						if ("DELIVERY".equals(obj.getMethod())) {
							DeepObject.Policy.AreaRule surcharge = new DeepObject.Policy.AreaRule();
							surcharge.setEnabled(true);
							surcharge.setUnit("AREA");
							surcharge.setRate1(BigDecimal.valueOf(3000));
							builder.set("areaRule", surcharge);

							DeepObject.Policy.QuantityRule charge = new DeepObject.Policy.QuantityRule();
							charge.setType("REPEAT");
							charge.setRepeat(2L);
							builder.set("quantityRule", charge);

							DeepObject.Policy.Threshold condFree = new DeepObject.Policy.Threshold();
							condFree.setLimit(BigDecimal.valueOf(50000));
							builder.set("threshold", condFree);
						} else {
							builder.setNull("areaRule");
							builder.setNull("quantityRule");
							builder.setNull("threshold");
						}
					})
			)
			.build();

		// when
		DeepObject actual = sut.giveMeOne(DeepObject.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getType()).isEqualTo("TAX");
		then(actual.getPolicy().getMethod()).isEqualTo("DELIVERY");
		then(actual.getPolicy().getFeeType()).isEqualTo("CONDITIONAL_FREE");
		then(actual.getPolicy().getAreaRule().isEnabled()).isTrue();
		then(actual.getPolicy().getAreaRule().getUnit()).isEqualTo("AREA");
		then(actual.getPolicy().getAreaRule().getRate1()).isEqualByComparingTo(BigDecimal.valueOf(3000));
		then(actual.getPolicy().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getPolicy().getQuantityRule().getRepeat()).isEqualTo(2L);
		then(actual.getPolicy().getThreshold().getLimit()).isEqualByComparingTo(BigDecimal.valueOf(50000));
	}

	@Property
	void shippingPolicyThenApplySetsChildObjectsViaFixtureSample() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.AreaRule.class)
					.setLazy("enabled", () -> true)
					.setLazy("unit", () -> "AREA")
					.set("rate1", BigDecimal.valueOf(3000))
			)
			.register(DeepObject.Policy.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.setLazy("repeat", () -> 2L)
			)
			.register(DeepObject.Policy.Threshold.class, fixture ->
				fixture.giveMeBuilder(DeepObject.Policy.Threshold.class).set("limit", BigDecimal.valueOf(50000))
			)
			.register(DeepObject.Policy.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						if ("DELIVERY".equals(obj.getMethod())) {
							builder.set("areaRule", fixture.giveMeOne(DeepObject.Policy.AreaRule.class));
							builder.set("quantityRule", fixture.giveMeOne(DeepObject.Policy.QuantityRule.class));
							builder.set("threshold", fixture.giveMeOne(DeepObject.Policy.Threshold.class));
						}
					})
			)
			.build();

		// when
		DeepObject actual = sut.giveMeOne(DeepObject.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getType()).isEqualTo("TAX");
		then(actual.getPolicy().getMethod()).isEqualTo("DELIVERY");
		then(actual.getPolicy().getAreaRule().isEnabled()).isTrue();
		then(actual.getPolicy().getAreaRule().getUnit()).isEqualTo("AREA");
		then(actual.getPolicy().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getPolicy().getQuantityRule().getRepeat()).isEqualTo(2L);
		then(actual.getPolicy().getThreshold().getLimit()).isEqualByComparingTo(BigDecimal.valueOf(50000));
	}

	@Property
	void remoteShippingInfoThenApplySetsEntireChildObjectsByMethod() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class)
					.set("type", "3")
					.thenApply((obj, builder) ->
						builder.set("to", BigDecimal.valueOf(Long.parseLong(obj.getType()) * 1000))
					)
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.OptionSpec.class).setLazy("type", () -> "SELECT")
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.thenApply((obj, builder) -> builder.set("repeat", "REPEAT".equals(obj.getType()) ? 2L : 1L))
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class)
					.setLazy("enabled", () -> true)
					.thenApply((obj, builder) -> builder.set("unit", obj.isEnabled() ? "AREA" : "NONE"))
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						if ("DELIVERY".equals(obj.getMethod())) {
							Envelope.Body.DeliveryInfo.AreaRule surcharge = new Envelope.Body.DeliveryInfo.AreaRule();
							surcharge.setEnabled(true);
							surcharge.setUnit("AREA");
							surcharge.setRate1(BigDecimal.valueOf(3000));
							builder.set("areaRule", surcharge);

							Envelope.Body.DeliveryInfo.QuantityRule charge =
								new Envelope.Body.DeliveryInfo.QuantityRule();
							charge.setType("REPEAT");
							charge.setRepeat(2L);
							builder.set("quantityRule", charge);

							Envelope.Body.DeliveryInfo.Threshold condFree = new Envelope.Body.DeliveryInfo.Threshold();
							condFree.setLimit(BigDecimal.valueOf(50000));
							builder.set("threshold", condFree);
						} else {
							builder.setNull("areaRule");
							builder.setNull("quantityRule");
							builder.setNull("threshold");
						}
						builder.set("groupId", "GRP_" + obj.getMethod());
					})
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getDeliveryInfo().getMethod()).isEqualTo("DELIVERY");
		then(actual.getDeliveryInfo().getGroupId()).isEqualTo("GRP_DELIVERY");
		then(actual.getDeliveryInfo().getAreaRule().isEnabled()).isTrue();
		then(actual.getDeliveryInfo().getAreaRule().getUnit()).isEqualTo("AREA");
		then(actual.getDeliveryInfo().getAreaRule().getRate1()).isEqualByComparingTo(BigDecimal.valueOf(3000));
		then(actual.getDeliveryInfo().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getDeliveryInfo().getQuantityRule().getRepeat()).isEqualTo(2L);
		then(actual.getDeliveryInfo().getThreshold().getLimit()).isEqualByComparingTo(BigDecimal.valueOf(50000));
	}

	@Property
	void remoteShippingInfoThenApplySetsChildObjectsViaFixtureSample() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class).set("type", "3")
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.OptionSpec.class).setLazy("type", () -> "SELECT")
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.setLazy("repeat", () -> 2L)
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class)
					.setLazy("enabled", () -> true)
					.setLazy("unit", () -> "AREA")
			)
			.register(Envelope.Body.DeliveryInfo.Threshold.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.Threshold.class)
					.set("limit", BigDecimal.valueOf(50000))
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						if ("DELIVERY".equals(obj.getMethod())) {
							builder.set("areaRule", fixture.giveMeOne(Envelope.Body.DeliveryInfo.AreaRule.class));
							builder.set(
								"quantityRule",
								fixture.giveMeOne(Envelope.Body.DeliveryInfo.QuantityRule.class)
							);
							builder.set("threshold", fixture.giveMeOne(Envelope.Body.DeliveryInfo.Threshold.class));
						}
						builder.set("groupId", "GRP_" + obj.getMethod());
					})
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getDeliveryInfo().getMethod()).isEqualTo("DELIVERY");
		then(actual.getDeliveryInfo().getGroupId()).isEqualTo("GRP_DELIVERY");
		then(actual.getDeliveryInfo().getAreaRule().isEnabled()).isTrue();
		then(actual.getDeliveryInfo().getAreaRule().getUnit()).isEqualTo("AREA");
		then(actual.getDeliveryInfo().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getDeliveryInfo().getQuantityRule().getRepeat()).isEqualTo(2L);
		then(actual.getDeliveryInfo().getThreshold().getLimit()).isEqualByComparingTo(BigDecimal.valueOf(50000));
	}

	@Property
	void shippingPolicyThenApplyThreeBranchMethodRouting() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(DeepObject.Policy.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						switch (obj.getMethod()) {
							case "DELIVERY":
								DeepObject.Policy.AreaRule deliverySurcharge = new DeepObject.Policy.AreaRule();
								deliverySurcharge.setEnabled(true);
								deliverySurcharge.setUnit("AREA");
								builder.set("areaRule", deliverySurcharge);

								DeepObject.Policy.QuantityRule deliveryCharge = new DeepObject.Policy.QuantityRule();
								deliveryCharge.setType("REPEAT");
								deliveryCharge.setRepeat(3L);
								builder.set("quantityRule", deliveryCharge);

								DeepObject.Policy.Threshold deliveryFree = new DeepObject.Policy.Threshold();
								deliveryFree.setLimit(BigDecimal.valueOf(50000));
								builder.set("threshold", deliveryFree);
								break;
							case "PICKUP":
								builder.setNull("areaRule");
								builder.setNull("quantityRule");
								builder.setNull("threshold");
								break;
							default:
								builder.setNull("areaRule");
								builder.setNull("quantityRule");
								builder.setNull("threshold");
								break;
						}
						builder.set("groupId", "GRP_" + obj.getMethod());
					})
			)
			.build();

		// when
		DeepObject actual = sut.giveMeOne(DeepObject.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getPolicy().getMethod()).isEqualTo("DELIVERY");
		then(actual.getPolicy().getGroupId()).isEqualTo("GRP_DELIVERY");
		then(actual.getPolicy().getAreaRule().isEnabled()).isTrue();
		then(actual.getPolicy().getAreaRule().getUnit()).isEqualTo("AREA");
		then(actual.getPolicy().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getPolicy().getQuantityRule().getRepeat()).isEqualTo(3L);
		then(actual.getPolicy().getThreshold().getLimit()).isEqualByComparingTo(BigDecimal.valueOf(50000));
	}

	@Property
	void shippingPolicyThenApplySetsObjectsViaBuilderChain() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(DeepObject.Policy.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						if ("DELIVERY".equals(obj.getMethod())) {
							DeepObject.Policy.AreaRule surcharge = fixture
								.giveMeBuilder(DeepObject.Policy.AreaRule.class)
								.set("enabled", true)
								.set("unit", "AREA")
								.set("rate1", BigDecimal.valueOf(3000))
								.sample();
							builder.set("areaRule", surcharge);

							DeepObject.Policy.QuantityRule charge = fixture
								.giveMeBuilder(DeepObject.Policy.QuantityRule.class)
								.set("type", "REPEAT")
								.set("repeat", 5L)
								.sample();
							builder.set("quantityRule", charge);

							DeepObject.Policy.Threshold condFree = fixture
								.giveMeBuilder(DeepObject.Policy.Threshold.class)
								.set("limit", BigDecimal.valueOf(30000))
								.sample();
							builder.set("threshold", condFree);
						}
						builder.set("groupId", obj.getMethod() + "_" + obj.getFeeType());
					})
			)
			.build();

		// when
		DeepObject actual = sut.giveMeOne(DeepObject.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getPolicy().getMethod()).isEqualTo("DELIVERY");
		then(actual.getPolicy().getGroupId()).isEqualTo("DELIVERY_CONDITIONAL_FREE");
		then(actual.getPolicy().getAreaRule().isEnabled()).isTrue();
		then(actual.getPolicy().getAreaRule().getUnit()).isEqualTo("AREA");
		then(actual.getPolicy().getAreaRule().getRate1()).isEqualByComparingTo(BigDecimal.valueOf(3000));
		then(actual.getPolicy().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getPolicy().getQuantityRule().getRepeat()).isEqualTo(5L);
		then(actual.getPolicy().getThreshold().getLimit()).isEqualByComparingTo(BigDecimal.valueOf(30000));
	}

	@Property
	void wrapperSampledNestedPolicyThenApplySetsObjects() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class).set("type", "3")
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.OptionSpec.class).setLazy("type", () -> "SELECT")
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
					.setLazy("type", () -> "REPEAT")
					.setLazy("repeat", () -> 2L)
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class)
					.setLazy("enabled", () -> true)
					.setLazy("unit", () -> "AREA")
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						if ("DELIVERY".equals(obj.getMethod())) {
							Envelope.Body.DeliveryInfo.AreaRule surcharge = new Envelope.Body.DeliveryInfo.AreaRule();
							surcharge.setEnabled(true);
							surcharge.setUnit("AREA");
							surcharge.setRate1(BigDecimal.valueOf(3000));
							builder.set("areaRule", surcharge);

							Envelope.Body.DeliveryInfo.QuantityRule charge =
								new Envelope.Body.DeliveryInfo.QuantityRule();
							charge.setType("REPEAT");
							charge.setRepeat(2L);
							builder.set("quantityRule", charge);

							Envelope.Body.DeliveryInfo.Threshold condFree = new Envelope.Body.DeliveryInfo.Threshold();
							condFree.setLimit(BigDecimal.valueOf(50000));
							builder.set("threshold", condFree);
						}
						builder.set("groupId", "GRP_" + obj.getMethod());
					})
			)
			.build();

		FixtureMonkeyTestSpecs.Envelope actual = sut.giveMeOne(FixtureMonkeyTestSpecs.Envelope.class);

		// then
		then(actual.getBody().getStatus()).isEqualTo("ON_SALE");
		then(actual.getBody().getTaxType()).isEqualTo("TAX");
		then(actual.getBody().getDeliveryInfo().getMethod()).isEqualTo("DELIVERY");
		then(actual.getBody().getDeliveryInfo().getGroupId()).isEqualTo("GRP_DELIVERY");
		then(actual.getBody().getDeliveryInfo().getAreaRule().isEnabled()).isTrue();
		then(actual.getBody().getDeliveryInfo().getAreaRule().getUnit()).isEqualTo("AREA");
		then(actual.getBody().getDeliveryInfo().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getBody().getDeliveryInfo().getQuantityRule().getRepeat()).isEqualTo(2L);
		then(actual.getBody().getDeliveryInfo().getThreshold().getLimit()).isEqualByComparingTo(
			BigDecimal.valueOf(50000)
		);
	}

	@Property
	void multipleSamplesPolicyThenApplySetsObjects() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.class)
					.setLazy("type", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(DeepObject.Policy.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						if ("DELIVERY".equals(obj.getMethod())) {
							DeepObject.Policy.AreaRule surcharge = new DeepObject.Policy.AreaRule();
							surcharge.setEnabled(true);
							surcharge.setUnit("AREA");
							builder.set("areaRule", surcharge);

							DeepObject.Policy.QuantityRule charge = new DeepObject.Policy.QuantityRule();
							charge.setType("REPEAT");
							charge.setRepeat(2L);
							builder.set("quantityRule", charge);

							DeepObject.Policy.Threshold condFree = new DeepObject.Policy.Threshold();
							condFree.setLimit(BigDecimal.valueOf(50000));
							builder.set("threshold", condFree);
						}
						builder.set("groupId", "GRP_" + obj.getMethod());
					})
			)
			.build();

		for (int i = 0; i < 10; i++) {
			// when
			DeepObject actual = sut.giveMeOne(DeepObject.class);

			// then
			then(actual.getStatus()).as("sample %d status", i).isEqualTo("ON_SALE");
			then(actual.getPolicy().getMethod()).as("sample %d method", i).isEqualTo("DELIVERY");
			then(actual.getPolicy().getGroupId()).as("sample %d groupId", i).isEqualTo("GRP_DELIVERY");
			then(actual.getPolicy().getAreaRule().isEnabled()).as("sample %d apiSupport", i).isTrue();
			then(actual.getPolicy().getQuantityRule().getType()).as("sample %d chargeType", i).isEqualTo("REPEAT");
			then(actual.getPolicy().getThreshold().getLimit())
				.as("sample %d basePrice", i)
				.isEqualByComparingTo(BigDecimal.valueOf(50000));
		}
	}

	@Property
	void exactRealPattern_thenApplySetsDollarWithSampledObject() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin().tracer(AdapterTracer.console()))
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class)
					.setLazy("$", () ->
						fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class).set("type", "3").sample()
					)
					.set("type", "3")
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.OptionSpec.class).setLazy("type", () -> "SELECT")
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
					.setLazy("$", () ->
						fixture
							.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
							.setLazy("type", () -> "REPEAT")
							.sample()
					)
					.setLazy("type", () -> "REPEAT")
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class)
					.setLazy("$", () ->
						fixture
							.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class)
							.setLazy("enabled", () -> true)
							.sample()
					)
					.setLazy("enabled", () -> true)
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("$", () ->
						fixture
							.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
							.setLazy("method", () -> "DELIVERY")
							.setLazy("feeType", () -> "CONDITIONAL_FREE")
							.setLazy("payType", () -> "PAY_SELECT")
							.sample()
					)
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
	}

	@Property
	void nestedPathSet_shippingPolicyFieldsViaNestedExpression() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin().tracer(AdapterTracer.console()))
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class).set("type", "3")
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.OptionSpec.class).setLazy("type", () -> "SELECT")
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class).setLazy("type", () -> "REPEAT")
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class).setLazy("enabled", () -> true)
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						if ("DELIVERY".equals(obj.getMethod())) {
							builder.set("areaRule.enabled", true);
							builder.set("areaRule.unit", "AREA");
							builder.set("quantityRule.type", "REPEAT");
							builder.set("quantityRule.repeat", 2L);
							builder.set("threshold.limit", BigDecimal.valueOf(50000));
						} else {
							builder.setNull("areaRule");
							builder.setNull("quantityRule");
							builder.setNull("threshold");
						}
						builder.set("groupId", "GRP_" + obj.getMethod());
					})
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getDeliveryInfo().getMethod()).isEqualTo("DELIVERY");
		then(actual.getDeliveryInfo().getGroupId()).isEqualTo("GRP_DELIVERY");
		then(actual.getDeliveryInfo().getAreaRule().isEnabled()).isTrue();
		then(actual.getDeliveryInfo().getAreaRule().getUnit()).isEqualTo("AREA");
		then(actual.getDeliveryInfo().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getDeliveryInfo().getQuantityRule().getRepeat()).isEqualTo(2L);
		then(actual.getDeliveryInfo().getThreshold().getLimit()).isEqualByComparingTo(BigDecimal.valueOf(50000));
	}

	@Property
	void newObjectSet_shippingPolicyChildObjectsViaNew() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin().tracer(AdapterTracer.console()))
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class).set("type", "3")
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.OptionSpec.class).setLazy("type", () -> "SELECT")
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class).setLazy("type", () -> "REPEAT")
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class).setLazy("enabled", () -> true)
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CONDITIONAL_FREE")
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						if ("DELIVERY".equals(obj.getMethod())) {
							Envelope.Body.DeliveryInfo.AreaRule surcharge = new Envelope.Body.DeliveryInfo.AreaRule();
							surcharge.setEnabled(true);
							surcharge.setUnit("AREA");
							builder.set("areaRule", surcharge);

							Envelope.Body.DeliveryInfo.QuantityRule charge =
								new Envelope.Body.DeliveryInfo.QuantityRule();
							charge.setType("REPEAT");
							charge.setRepeat(2L);
							builder.set("quantityRule", charge);

							Envelope.Body.DeliveryInfo.Threshold condFree = new Envelope.Body.DeliveryInfo.Threshold();
							condFree.setLimit(BigDecimal.valueOf(50000));
							builder.set("threshold", condFree);
						} else {
							builder.setNull("areaRule");
							builder.setNull("quantityRule");
							builder.setNull("threshold");
						}
						builder.set("groupId", "GRP_" + obj.getMethod());
					})
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		// then
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");
		then(actual.getDeliveryInfo().getMethod()).isEqualTo("DELIVERY");
		then(actual.getDeliveryInfo().getGroupId()).isEqualTo("GRP_DELIVERY");
		then(actual.getDeliveryInfo().getAreaRule().isEnabled()).isTrue();
		then(actual.getDeliveryInfo().getAreaRule().getUnit()).isEqualTo("AREA");
		then(actual.getDeliveryInfo().getQuantityRule().getType()).isEqualTo("REPEAT");
		then(actual.getDeliveryInfo().getQuantityRule().getRepeat()).isEqualTo(2L);
		then(actual.getDeliveryInfo().getThreshold().getLimit()).isEqualByComparingTo(BigDecimal.valueOf(50000));
	}

	@Property
	void registeredSetLazyArbitrariesOf_methodFieldRandomSelection() {
		// given
		List<String> validMethods = Arrays.asList("DELIVERY", "VISIT", "DIRECT");

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin().tracer(AdapterTracer.console()))
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class).set("type", "3")
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.OptionSpec.class).setLazy("type", () -> "SELECT")
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "ON_SALE")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class)
					.setLazy("type", () -> Arbitraries.of("REPEAT", "RANGE").sample())
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class).setLazy("enabled", () -> true)
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> Arbitraries.of("DELIVERY", "VISIT", "DIRECT").sample())
					.setLazy("feeType", () -> Arbitraries.of("FREE", "CONDITIONAL_FREE", "CHARGE").sample())
					.setLazy("payType", () -> "PAY_SELECT")
					.thenApply((obj, builder) -> {
						if ("DELIVERY".equals(obj.getMethod())) {
							Envelope.Body.DeliveryInfo.AreaRule surcharge = new Envelope.Body.DeliveryInfo.AreaRule();
							surcharge.setEnabled(true);
							surcharge.setUnit("AREA");
							surcharge.setRate1(BigDecimal.valueOf(3000));
							builder.set("areaRule", surcharge);

							Envelope.Body.DeliveryInfo.QuantityRule charge =
								new Envelope.Body.DeliveryInfo.QuantityRule();
							charge.setType("REPEAT");
							charge.setRepeat(2L);
							builder.set("quantityRule", charge);

							Envelope.Body.DeliveryInfo.Threshold condFree = new Envelope.Body.DeliveryInfo.Threshold();
							condFree.setLimit(BigDecimal.valueOf(50000));
							builder.set("threshold", condFree);
						} else {
							builder.setNull("areaRule");
							builder.setNull("quantityRule");
							builder.setNull("threshold");
						}
						builder.set("groupId", "GRP_" + obj.getMethod());
					})
			)
			.build();

		// when
		Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

		then(actual.getDeliveryInfo().getMethod()).isIn(validMethods);
		then(actual.getDeliveryInfo().getFeeType()).isIn("FREE", "CONDITIONAL_FREE", "CHARGE");
		then(actual.getDeliveryInfo().getPayType()).isEqualTo("PAY_SELECT");
		then(actual.getDeliveryInfo().getGroupId()).startsWith("GRP_");
		then(actual.getStatus()).isEqualTo("ON_SALE");
		then(actual.getTaxType()).isEqualTo("TAX");

		String method = actual.getDeliveryInfo().getMethod();
		if ("DELIVERY".equals(method)) {
			then(actual.getDeliveryInfo().getAreaRule()).isNotNull();
			then(actual.getDeliveryInfo().getAreaRule().isEnabled()).isTrue();
			then(actual.getDeliveryInfo().getQuantityRule()).isNotNull();
			then(actual.getDeliveryInfo().getQuantityRule().getType()).isEqualTo("REPEAT");
			then(actual.getDeliveryInfo().getThreshold()).isNotNull();
		} else {
			then(actual.getDeliveryInfo().getAreaRule()).isNull();
			then(actual.getDeliveryInfo().getQuantityRule()).isNull();
			then(actual.getDeliveryInfo().getThreshold()).isNull();
		}
	}

	@Property
	void registeredSetLazyBeforeAndAfterThenApply_allFieldsAppliedInNestedType() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.class)
					.setLazy("method", () -> "DELIVERY")
					.thenApply((obj, builder) -> {
					})
					.setLazy("feeType", () -> "CHARGE")
					.setLazy("payType", () -> "PREPAID")
			)
			.build();

		for (int i = 0; i < 10; i++) {
			// when
			DeepObject actual = sut.giveMeOne(DeepObject.class);

			// then
			then(actual.getPolicy()).isNotNull();
			then(actual.getPolicy().getMethod()).as("sample #%d: method (before thenApply)", i).isEqualTo("DELIVERY");
			then(actual.getPolicy().getFeeType()).as("sample #%d: feeType (after thenApply)", i).isEqualTo("CHARGE");
			then(actual.getPolicy().getPayType())
				.as("sample #%d: feePayType (after thenApply)", i)
				.isEqualTo("PREPAID");
		}
	}

	@Property
	void registeredFieldLevelSetLazy_allFieldsShouldResolveOnDirectSample() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(Envelope.Body.DeliveryInfo.Range.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.Range.class).set("type", "3")
			)
			.register(Envelope.Body.DeliveryInfo.QuantityRule.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.QuantityRule.class).setLazy("type", () -> "REPEAT")
			)
			.register(Envelope.Body.OptionSpec.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.OptionSpec.class).setLazy("type", () -> "SELECT")
			)
			.register(Envelope.Body.DeliveryInfo.AreaRule.class, fixture ->
				fixture.giveMeBuilder(Envelope.Body.DeliveryInfo.AreaRule.class).setLazy("enabled", () -> false)
			)
			.register(Envelope.Body.DeliveryInfo.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.DeliveryInfo.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "CHARGE_BY_QUANTITY")
					.setLazy("payType", () -> "FREE")
					.thenApply((obj, builder) -> {
					})
			)
			.register(Envelope.Body.class, fixture ->
				fixture
					.giveMeBuilder(Envelope.Body.class)
					.setLazy("taxType", () -> "TAX")
					.setLazy("status", () -> "SOLD_OUT")
			)
			.build();

		for (int i = 0; i < 1000; i++) {
			// when
			Envelope.Body actual = sut.giveMeOne(Envelope.Body.class);

			then(actual.getTaxType()).as("sample #%d: taxType (field-level setLazy)", i).isEqualTo("TAX");
			then(actual.getStatus()).as("sample #%d: status (field-level setLazy)", i).isEqualTo("SOLD_OUT");
		}
	}

	@Property
	void registerSizeThenApply_directSetDecomposed() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("a");
		decomposed.add("b");
		decomposed.add("c");

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture
					.giveMeBuilder(StringListWrapper.class)
					.size("values", 5)
					.thenApply((obj, builder) -> {
					})
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(StringListWrapper.class).set("values", decomposed).sample().getValues();

		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("a");
		then(actual.get(1)).isEqualTo("b");
		then(actual.get(2)).isEqualTo("c");
	}

	@Property
	void registerSetDecomposed_directThenApplyWildcardSize() {
		// given
		List<String> innerList = new ArrayList<>();
		innerList.add("1");
		innerList.add("2");
		innerList.add("3");

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(NestedStringListWrapper.class, fixture ->
				fixture.giveMeBuilder(NestedStringListWrapper.class)
					.set("values[0].values", innerList)
					.size("values", 2)
			)
			.build();

		// when
		List<StringListWrapper> actual = sut
			.giveMeBuilder(NestedStringListWrapper.class)
			.thenApply((obj, builder) -> builder.size("values[*].values", 2))
			.sample()
			.getValues();

		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(0).getValues().get(0)).isEqualTo("1");
		then(actual.get(0).getValues().get(1)).isEqualTo("2");
		then(actual.get(1).getValues()).hasSize(2);
	}

	@Property
	void registerThenApplyWildcardSize_directSetDecomposed() {
		// given
		List<String> innerList = new ArrayList<>();
		innerList.add("x");
		innerList.add("y");
		innerList.add("z");

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(NestedStringListWrapper.class, fixture ->
				fixture
					.giveMeBuilder(NestedStringListWrapper.class)
					.size("values", 2)
					.thenApply((obj, builder) -> builder.size("values[*].values", 2))
			)
			.build();

		// when
		List<StringListWrapper> actual = sut
			.giveMeBuilder(NestedStringListWrapper.class)
			.set("values[0].values", innerList)
			.sample()
			.getValues();

		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(3);
		then(actual.get(0).getValues().get(0)).isEqualTo("x");
		then(actual.get(0).getValues().get(1)).isEqualTo("y");
		then(actual.get(0).getValues().get(2)).isEqualTo("z");
		then(actual.get(1).getValues()).hasSize(2);
	}

	@Property
	void registerThenApplyWildcardSet_directSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture
					.giveMeBuilder(StringListWrapper.class)
					.size("values", 3)
					.thenApply((obj, builder) -> builder.set("values[*]", "registered"))
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(StringListWrapper.class).size("values", 5).sample().getValues();

		then(actual).hasSize(5);
	}

	@Property
	void registerThenApply_directThenApplyWildcardSize_conflict() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(NestedStringListWrapper.class, fixture ->
				fixture
					.giveMeBuilder(NestedStringListWrapper.class)
					.size("values", 3)
					.thenApply((obj, builder) -> builder.size("values[*].values", 5))
			)
			.build();

		// when
		List<StringListWrapper> actual = sut
			.giveMeBuilder(NestedStringListWrapper.class)
			.thenApply((obj, builder) -> builder.size("values[*].values", 2))
			.sample()
			.getValues();

		then(actual).hasSize(3);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(1).getValues()).hasSize(2);
		then(actual.get(2).getValues()).hasSize(2);
	}

	@Property
	void nestedRegister_parentThenApplySetsChildContainer_childRegisterSize() {
		// given
		List<String> innerList = new ArrayList<>();
		innerList.add("p1");
		innerList.add("p2");

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 4)
			)
			.register(NestedStringListWrapper.class, fixture ->
				fixture
					.giveMeBuilder(NestedStringListWrapper.class)
					.size("values", 2)
					.thenApply((obj, builder) -> builder.set("values[0].values", innerList))
			)
			.build();

		// when
		List<StringListWrapper> actual = sut.giveMeOne(NestedStringListWrapper.class).getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(0).getValues().get(0)).isEqualTo("p1");
		then(actual.get(0).getValues().get(1)).isEqualTo("p2");
		then(actual.get(1).getValues()).hasSize(4);
	}

	@Property
	void exactPathSizeOverridesTypedBasedRegisterSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 4)
			)
			.build();

		// when
		StringListWrapper actual = sut.giveMeBuilder(StringListWrapper.class).size("values", 2).sample();

		// then
		then(actual.getValues()).hasSize(2);
	}

	@Property
	void typeBasedRegisterSizeOverridesWildcardSize() {
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
			.size("values[*].values", 2)
			.sample();

		// then
		then(actual.getValues()).hasSize(2);
		then(actual.getValues().get(0).getValues()).hasSize(4);
		then(actual.getValues().get(1).getValues()).hasSize(4);
	}

	@Property
	void nestedRegisterSize_withoutThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 4)
			)
			.register(NestedStringListWrapper.class, fixture ->
				fixture.giveMeBuilder(NestedStringListWrapper.class).size("values", 2)
			)
			.build();

		// when
		NestedStringListWrapper actual = sut.giveMeOne(NestedStringListWrapper.class);

		// then
		then(actual.getValues()).hasSize(2);
		then(actual.getValues().get(0).getValues()).hasSize(4);
		then(actual.getValues().get(1).getValues()).hasSize(4);
	}

	@Property
	void tripleNestedRegisterSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 3)
			)
			.register(NestedStringListWrapper.class, fixture ->
				fixture.giveMeBuilder(NestedStringListWrapper.class).size("values", 2)
			)
			.register(DoubleNestedStringListWrapper.class, fixture ->
				fixture.giveMeBuilder(DoubleNestedStringListWrapper.class).size("values", 1)
			)
			.build();

		// when
		DoubleNestedStringListWrapper actual = sut.giveMeOne(DoubleNestedStringListWrapper.class);

		// then
		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0).getValues()).hasSize(2);
		then(actual.getValues().get(0).getValues().get(0).getValues()).hasSize(3);
		then(actual.getValues().get(0).getValues().get(1).getValues()).hasSize(3);
	}

	@Property
	void exactPathPartiallyOverridesTypeBasedSize() {
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

	@Property
	void registerRootSetLazy_directDecomposed_directFieldSetNull() {
		// given
		DeepObject.Policy.AreaRule registeredAreaRule = new DeepObject.Policy.AreaRule();
		registeredAreaRule.setEnabled(true);
		registeredAreaRule.setUnit("AREA");
		registeredAreaRule.setRate1(BigDecimal.valueOf(3000));
		registeredAreaRule.setRate2(BigDecimal.valueOf(5000));

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture.giveMeBuilder(DeepObject.Policy.AreaRule.class).setLazy("$", () -> registeredAreaRule)
			)
			.build();

		// when
		DeepObject.Policy.AreaRule actual = sut
			.giveMeBuilder(DeepObject.Policy.AreaRule.class)
			.set("$", registeredAreaRule)
			.setNull("rate1")
			.setNull("rate2")
			.sample();

		// then
		then(actual.isEnabled()).isTrue();
		then(actual.getUnit()).isEqualTo("AREA");
		then(actual.getRate1()).isNull();
		then(actual.getRate2()).isNull();
	}

	@Property
	void registerRootSetLazy_directDecomposed_directFieldSetLazy() {
		// given
		DeepObject.Policy.AreaRule registeredAreaRule = new DeepObject.Policy.AreaRule();
		registeredAreaRule.setEnabled(true);
		registeredAreaRule.setUnit("AREA");
		registeredAreaRule.setRate1(BigDecimal.valueOf(3000));
		registeredAreaRule.setRate2(BigDecimal.valueOf(5000));

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture.giveMeBuilder(DeepObject.Policy.AreaRule.class).setLazy("$", () -> registeredAreaRule)
			)
			.build();

		// when
		DeepObject.Policy.AreaRule actual = sut
			.giveMeBuilder(DeepObject.Policy.AreaRule.class)
			.set("$", registeredAreaRule)
			.setLazy("unit", () -> "OVERRIDDEN_UNIT")
			.sample();

		// then
		then(actual.isEnabled()).isTrue();
		then(actual.getUnit()).isEqualTo("OVERRIDDEN_UNIT");
		then(actual.getRate1()).isEqualByComparingTo(BigDecimal.valueOf(3000));
		then(actual.getRate2()).isEqualByComparingTo(BigDecimal.valueOf(5000));
	}

	@Property
	void registerRootSetLazy_directDecomposed_directRootSetLazy() {
		// given
		DeepObject.Policy.AreaRule decomposedAreaRule = new DeepObject.Policy.AreaRule();
		decomposedAreaRule.setEnabled(false);
		decomposedAreaRule.setUnit("DECOMPOSED");
		decomposedAreaRule.setRate1(BigDecimal.valueOf(1000));
		decomposedAreaRule.setRate2(BigDecimal.valueOf(2000));

		DeepObject.Policy.AreaRule lazyAreaRule = new DeepObject.Policy.AreaRule();
		lazyAreaRule.setEnabled(true);
		lazyAreaRule.setUnit("LAZY_FINAL");
		lazyAreaRule.setRate1(BigDecimal.valueOf(9000));
		lazyAreaRule.setRate2(BigDecimal.valueOf(8000));

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.AreaRule.class)
					.setLazy("enabled", () -> false)
					.setLazy("unit", () -> "REGISTERED")
			)
			.build();

		// when
		DeepObject.Policy.AreaRule actual = sut
			.giveMeBuilder(DeepObject.Policy.AreaRule.class)
			.set("$", decomposedAreaRule)
			.setLazy("$", () -> lazyAreaRule)
			.sample();

		// then
		then(actual.isEnabled()).isTrue();
		then(actual.getUnit()).isEqualTo("LAZY_FINAL");
		then(actual.getRate1()).isEqualByComparingTo(BigDecimal.valueOf(9000));
		then(actual.getRate2()).isEqualByComparingTo(BigDecimal.valueOf(8000));
	}

	@Property
	void directDecomposedChildOverridesRegisteredBuilder() {
		// given
		DeepObject.Policy.AreaRule customAreaRule = new DeepObject.Policy.AreaRule();
		customAreaRule.setEnabled(false);
		customAreaRule.setUnit("CUSTOM");
		customAreaRule.setRate1(BigDecimal.valueOf(7777));
		customAreaRule.setRate2(BigDecimal.valueOf(8888));

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.AreaRule.class)
					.setLazy("enabled", () -> true)
					.setLazy("unit", () -> "REGISTERED_UNIT")
					.set("rate1", BigDecimal.valueOf(3000))
			)
			.register(DeepObject.Policy.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.class)
					.setLazy("method", () -> "DELIVERY")
					.setLazy("feeType", () -> "FREE")
			)
			.build();

		// when
		DeepObject.Policy actual = sut.giveMeBuilder(DeepObject.Policy.class).set("areaRule", customAreaRule).sample();

		// then
		then(actual.getMethod()).isEqualTo("DELIVERY");
		then(actual.getFeeType()).isEqualTo("FREE");
		then(actual.getAreaRule().isEnabled()).isFalse();
		then(actual.getAreaRule().getUnit()).isEqualTo("CUSTOM");
		then(actual.getAreaRule().getRate1()).isEqualByComparingTo(BigDecimal.valueOf(7777));
		then(actual.getAreaRule().getRate2()).isEqualByComparingTo(BigDecimal.valueOf(8888));
	}

	@Property
	void registerSetLazy_directAcceptIf() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.AreaRule.class)
					.setLazy("enabled", () -> true)
					.setLazy("unit", () -> "REGISTERED")
					.set("rate1", BigDecimal.valueOf(1000))
			)
			.build();

		// when
		DeepObject.Policy.AreaRule actual = sut
			.giveMeBuilder(DeepObject.Policy.AreaRule.class)
			.set("enabled", true)
			.acceptIf(it -> it.isEnabled(), builder -> builder.set("unit", "ACCEPTED"))
			.sample();

		// then
		then(actual.isEnabled()).isTrue();
		then(actual.getUnit()).isEqualTo("ACCEPTED");
		then(actual.getRate1()).isEqualByComparingTo(BigDecimal.valueOf(1000));
	}

	@Property
	void registerThenApply_directSetNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.AreaRule.class)
					.set("enabled", true)
					.thenApply((obj, builder) -> {
						builder.set("unit", "APPLIED_UNIT");
						builder.set("rate1", BigDecimal.valueOf(2000));
					})
			)
			.build();

		// when
		DeepObject.Policy.AreaRule actual = sut
			.giveMeBuilder(DeepObject.Policy.AreaRule.class)
			.setNull("unit")
			.sample();

		// then
		then(actual.isEnabled()).isTrue();
		then(actual.getUnit()).isNull();
		then(actual.getRate1()).isEqualByComparingTo(BigDecimal.valueOf(2000));
	}

	@Property
	void registerThenApply_directSetNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.AreaRule.class)
					.set("enabled", true)
					.thenApply((obj, builder) -> {
						builder.setNull("unit");
						builder.set("rate1", BigDecimal.valueOf(3000));
					})
			)
			.build();

		// when
		DeepObject.Policy.AreaRule actual = sut
			.giveMeBuilder(DeepObject.Policy.AreaRule.class)
			.setNotNull("unit")
			.sample();

		then(actual.isEnabled()).isTrue();
		then(actual.getUnit()).isNotNull();
		then(actual.getRate1()).isEqualByComparingTo(BigDecimal.valueOf(3000));
	}

	@Property
	void registerFieldSetLazy_directSetNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.AreaRule.class)
					.setLazy("unit", () -> null)
					.set("enabled", true)
			)
			.build();

		// when
		DeepObject.Policy.AreaRule actual = sut
			.giveMeBuilder(DeepObject.Policy.AreaRule.class)
			.setNotNull("unit")
			.sample();

		// then
		then(actual.isEnabled()).isTrue();
		then(actual.getUnit()).isNotNull();
	}

	@Property
	void registerFieldSetLazy_directSetLazySameField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.AreaRule.class)
					.setLazy("unit", () -> "REGISTERED_LAZY")
					.set("enabled", true)
			)
			.build();

		// when
		DeepObject.Policy.AreaRule actual = sut
			.giveMeBuilder(DeepObject.Policy.AreaRule.class)
			.setLazy("unit", () -> "DIRECT_LAZY")
			.sample();

		// then
		then(actual.isEnabled()).isTrue();
		then(actual.getUnit()).isEqualTo("DIRECT_LAZY");
	}

	@Property
	void setChildFieldWhereChildHasRegister() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.AreaRule.class)
					.setLazy("enabled", () -> true)
					.setLazy("unit", () -> "REGISTERED_UNIT")
					.set("rate1", BigDecimal.valueOf(3000))
					.set("rate2", BigDecimal.valueOf(5000))
			)
			.build();

		// when
		DeepObject.Policy actual = sut
			.giveMeBuilder(DeepObject.Policy.class)
			.set("areaRule.unit", "OVERRIDDEN_UNIT")
			.sample();

		then(actual.getAreaRule().getUnit()).isEqualTo("OVERRIDDEN_UNIT");
		then(actual.getAreaRule().isEnabled()).isTrue();
		then(actual.getAreaRule().getRate1()).isEqualByComparingTo(BigDecimal.valueOf(3000));
		then(actual.getAreaRule().getRate2()).isEqualByComparingTo(BigDecimal.valueOf(5000));
	}

	@Property
	void registerSize_directSetElement() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 3)
			)
			.build();

		// when
		StringListWrapper actual = sut.giveMeBuilder(StringListWrapper.class)
			.set("values[0]", "EXPLICIT_FIRST")
			.sample();

		// then
		then(actual.getValues()).hasSize(3);
		then(actual.getValues().get(0)).isEqualTo("EXPLICIT_FIRST");
	}

	@Property
	void registerSize_directSetNullWildcard() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 3)
			)
			.build();

		// when
		StringListWrapper actual = sut.giveMeBuilder(StringListWrapper.class).setNull("values[*]").sample();

		// then
		then(actual.getValues()).hasSize(3);
		then(actual.getValues()).allMatch(v -> v == null);
	}

	@Property
	void registerDuplicateSetSameField_lastSetWins() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringWrapper.class, fixture ->
				fixture.giveMeBuilder(StringWrapper.class).set("value", "FIRST").set("value", "SECOND")
			)
			.build();

		// when
		StringWrapper actual = sut.giveMeBuilder(StringWrapper.class).sample();

		then(actual.getValue()).isEqualTo("SECOND");
	}

	@Property
	void registerSetField_directWildcardSetNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(DeepObject.Policy.AreaRule.class, fixture ->
				fixture
					.giveMeBuilder(DeepObject.Policy.AreaRule.class)
					.set("unit", "REGISTERED_UNIT")
					.set("rate1", BigDecimal.valueOf(1000))
			)
			.build();

		// when
		DeepObject.Policy.AreaRule actual = sut.giveMeBuilder(DeepObject.Policy.AreaRule.class).setNull("*").sample();

		then(actual.getUnit()).isNull();
		then(actual.getRate1()).isNull();
		then(actual.getRate2()).isNull();
	}

	@Property
	void registerContainerSetOnly_sizeInferredFromValue() {
		// given
		StringWrapper registeredElement = new StringWrapper();
		registeredElement.setValue("REGISTERED");

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(FixtureMonkeyTestSpecs.StringWrapperList.class, fixture ->
				fixture
					.giveMeBuilder(FixtureMonkeyTestSpecs.StringWrapperList.class)
					.set("values", Collections.singletonList(registeredElement))
			)
			.build();

		// when
		FixtureMonkeyTestSpecs.StringWrapperList actual =
			sut.giveMeBuilder(FixtureMonkeyTestSpecs.StringWrapperList.class)
				.set("values[0].value", "DIRECT")
				.sample();

		// then
		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0).getValue()).isEqualTo("DIRECT");
	}

	@Property
	void registerContainerSet_directElementOverride_fieldShouldUseExplicitElement() {
		// given
		StringWrapper registeredElement = new StringWrapper();
		registeredElement.setValue("REGISTERED");

		StringWrapper listElement = new StringWrapper();
		listElement.setValue("FROM_LIST");

		StringWrapper explicitElement = new StringWrapper();
		explicitElement.setValue("FROM_ELEMENT");

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(FixtureMonkeyTestSpecs.StringWrapperList.class, fixture ->
				fixture
					.giveMeBuilder(FixtureMonkeyTestSpecs.StringWrapperList.class)
					.set("values", Collections.singletonList(registeredElement))
			)
			.build();

		// when
		FixtureMonkeyTestSpecs.StringWrapperList actual =
			sut.giveMeBuilder(FixtureMonkeyTestSpecs.StringWrapperList.class)
				.set("values", Collections.singletonList(listElement))
				.set("values[0]", explicitElement)
				.size("values", 1, 10)
				.sample();

		// then
		StringWrapper actualStringWrapper = actual.getValues().get(0);
		then(actualStringWrapper).isInstanceOf(StringWrapper.class);
		then(actualStringWrapper.getValue()).isEqualTo("FROM_ELEMENT");
	}
}
