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

package com.navercorp.fixturemonkey.test;

import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedString;
import static org.assertj.core.api.BDDAssertions.then;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.RepeatedTest;

import lombok.Data;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;

class RegisterDeclarationOrderTest {
	@RepeatedTest(10)
	void laterRegisterOfSameTypeWins() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "first").size("tags", 1))
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "second").size("tags", 2))
			.build();

		// when
		Order actual = sut.giveMeOne(Customer.class).getOrder();

		// then
		then(actual.getName()).isEqualTo("second");
		then(actual.getTags()).hasSize(2);
	}

	@RepeatedTest(10)
	void laterAnnotationMatcherRegisterWinsOverTypeRegister() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "type").size("tags", 3))
			.register(vipRegister())
			.build();

		// when
		Customer actual = sut.giveMeOne(Customer.class);

		// then
		then(actual.getVipOrder().getName()).isEqualTo("vip");
		then(actual.getVipOrder().getTags()).hasSize(1);
		then(actual.getOrder().getName()).isEqualTo("type");
		then(actual.getOrder().getTags()).hasSize(3);
	}

	@RepeatedTest(10)
	void laterTypeRegisterWinsOverAnnotationMatcherRegister() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(vipRegister())
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "type").size("tags", 3))
			.build();

		// when
		Order actual = sut.giveMeOne(Customer.class).getVipOrder();

		// then
		then(actual.getName()).isEqualTo("type");
		then(actual.getTags()).hasSize(3);
	}

	@RepeatedTest(10)
	void higherPriorityRegisterWinsRegardlessOfDeclarationOrder() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "prior").size("tags", 1), 1)
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "later").size("tags", 2))
			.build();

		// when
		Order actual = sut.giveMeOne(Customer.class).getOrder();

		// then
		then(actual.getName()).isEqualTo("prior");
		then(actual.getTags()).hasSize(1);
	}

	@RepeatedTest(10)
	void higherPriorityValueSkipsLowerPriorityCustomizer() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "prior"), 1)
			.register(
				Order.class,
				fm -> fm.giveMeBuilder(Order.class)
					.<String>customizeProperty(typedString("name"), it -> it.map(name -> name + "!")),
				2
			)
			.build();

		// when
		String actual = sut.giveMeOne(Customer.class).getOrder().getName();

		// then
		then(actual).isEqualTo("prior");
	}

	@RepeatedTest(10)
	void lowerPriorityValueTakesHigherPriorityCustomizer() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(
				Order.class,
				fm -> fm.giveMeBuilder(Order.class)
					.<String>customizeProperty(typedString("name"), it -> it.map(name -> name + "!")),
				1
			)
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "later"), 2)
			.build();

		// when
		String actual = sut.giveMeOne(Customer.class).getOrder().getName();

		// then
		then(actual).isEqualTo("later!");
	}

	private static FixtureMonkeyBuilder fixtureMonkey() {
		return FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true);
	}

	private static MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>> vipRegister() {
		return new MatcherOperator<>(
			property -> property.getAnnotation(Vip.class).isPresent(),
			fm -> fm.giveMeBuilder(Order.class).set("name", "vip").size("tags", 1)
		);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Vip {
	}

	@Data
	public static class Customer {
		@Vip
		private Order vipOrder;
		private Order order;
	}

	@Data
	public static class Order {
		private String name;
		private List<String> tags;
	}
}
