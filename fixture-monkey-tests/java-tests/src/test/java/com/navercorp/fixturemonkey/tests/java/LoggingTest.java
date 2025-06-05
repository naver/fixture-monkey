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

package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.ContainerObject;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.JavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.RootJavaTypeObject;

class LoggingTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void logRoot() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(String.class)
				.setPostCondition(it -> it.equals("test"))
				.sample()
		)
			.getCause()
			.hasMessageContaining("\"$\"");
	}

	@Test
	void logProperty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(JavaTypeObject.class)
				.setPostCondition("string", String.class, it -> it.equals("test"))
				.sample()
		)
			.getCause()
			.hasMessageContaining("\"string\"");
	}

	@Test
	void logNestedProperty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(RootJavaTypeObject.class)
				.setPostCondition("value.string", String.class, it -> it.equals("test"))
				.sample()
		)
			.getCause()
			.hasMessageContaining("\"value.string\"");
	}

	@Test
	void logArrayElement() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(ContainerObject.class)
				.size("array", 1)
				.setPostCondition("array[0]", String.class, it -> it.equals("test"))
				.sample()
		)
			.getCause()
			.hasMessageContaining("\"array[0]\"");
	}

	@Test
	void logListElement() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(ContainerObject.class)
				.size("list", 1)
				.setPostCondition("list[0]", String.class, it -> it.equals("test"))
				.sample()
		)
			.getCause()
			.hasMessageContaining("\"list[0]\"");
	}

	@Test
	void logListElementProperty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(ContainerObject.class)
				.size("complexList", 1)
				.setPostCondition("complexList[0].string", String.class, it -> it.equals("test"))
				.sample()
		)
			.getCause()
			.hasMessageContaining("\"complexList[0].string\"");
	}

	@Test
	void logMapElementKeyProperty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(ContainerObject.class)
				.setInner(
					new InnerSpec()
						.property("map", m ->
							m.size(1)
								.key(v -> v.postCondition(String.class, it -> it.equals("test")))
						)
				)
				.sample()
		)
			.getCause()
			.hasMessageContaining("\"map{key}\"");
	}

	@Test
	void logMapElementValueProperty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(ContainerObject.class)
				.setInner(
					new InnerSpec()
						.property("map", m ->
							m.size(1)
								.value(v -> v.postCondition(Integer.class, it -> it == -987654321))
						)
				)
				.sample()
		)
			.getCause()
			.hasMessageContaining("\"map{value}\"");
	}
}
