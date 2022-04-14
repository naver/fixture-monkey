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

package com.navercorp.fixturemonkey.junit.jupiter.extension;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.ParameterizedType;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterContext;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.GiveMe;
import com.navercorp.fixturemonkey.junit.jupiter.extension.support.ParameterContextAwareFixtureMonkey;

@ExtendWith(CustomFixtureMonkeyParameterExtensionTest.CustomFixtureMonkeyParameterExtension.class)
class CustomFixtureMonkeyParameterExtensionTest {

	private static final long FIXED_ID = 111L;

	@RepeatedTest(10)
	void giveMe(@GiveMe Container<Order> order) {
		then(order.getFoo().getId()).isEqualTo(FIXED_ID);
	}

	public static class CustomFixtureMonkeyParameterExtension extends FixtureMonkeyParameterExtension {
		public CustomFixtureMonkeyParameterExtension() {
			super(new FixtureMonkeyBuilder().addCustomizer(Order.class, object -> {
				assert object != null;
				object.setId(FIXED_ID);
				return object;
			}).build());
		}

		@Override
		protected ParameterContextAwareFixtureMonkey getParameterContextAwareFixtureMonkey(
			ParameterContext parameterContext) {
			return () -> {
				if (parameterContext.getParameter().getType() == Container.class) {
					ParameterizedType parameterizedType =
						(ParameterizedType)parameterContext.getParameter().getParameterizedType();
					Class<?> genericType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
					//noinspection rawtypes,unchecked
					return new Container(fixtureMonkey.giveMeOne(genericType));
				}
				return ParameterContextAwareFixtureMonkey.of(parameterContext, fixtureMonkey);
			};
		}
	}

	@Data
	public static class Order {
		private Long id;
	}

	@AllArgsConstructor
	@Data
	static class Container<T> {
		T foo;
	}
}
