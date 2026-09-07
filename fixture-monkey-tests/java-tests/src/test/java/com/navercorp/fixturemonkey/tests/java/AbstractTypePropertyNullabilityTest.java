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

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.stream.IntStream;

import javax.validation.constraints.NotNull;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;

class AbstractTypePropertyNullabilityTest {
	private static final int SAMPLE_COUNT = 200;

	@Test
	void notNullConcretePropertyIsNeverNull() {
		FixtureMonkey sut = interfaceMonkey();

		long nullCount = countNulls(sut, ConcreteHolder.class, it -> it.value);

		then(nullCount).isZero();
	}

	@Test
	void notNullInterfacePropertyIsNeverNull() {
		FixtureMonkey sut = interfaceMonkey();

		long nullCount = countNulls(sut, InterfaceHolder.class, it -> it.value);

		then(nullCount).isZero();
	}

	@Test
	void notNullAbstractClassPropertyIsNeverNull() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new InterfacePlugin()
				.abstractClassExtends(AbstractValue.class, Arrays.asList(FirstAbstractValue.class,
					SecondAbstractValue.class)))
			.build();

		long nullCount = countNulls(sut, AbstractClassHolder.class, it -> it.value);

		then(nullCount).isZero();
	}

	@Test
	void interfaceAtRootIsNeverNull() {
		FixtureMonkey sut = interfaceMonkey();

		long nullCount = IntStream.range(0, SAMPLE_COUNT)
			.filter(it -> sut.giveMeOne(Value.class) == null)
			.count();

		then(nullCount).isZero();
	}

	private FixtureMonkey interfaceMonkey() {
		return FixtureMonkey.builder()
			.plugin(new InterfacePlugin()
				.interfaceImplements(Value.class, Arrays.asList(FirstValue.class, SecondValue.class)))
			.build();
	}

	private <T> long countNulls(
		FixtureMonkey sut,
		Class<T> type,
		java.util.function.Function<T, Object> extractor
	) {
		return IntStream.range(0, SAMPLE_COUNT)
			.filter(it -> extractor.apply(sut.giveMeOne(type)) == null)
			.count();
	}

	public interface Value {
	}

	public static class FirstValue implements Value {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class SecondValue implements Value {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public abstract static class AbstractValue {
	}

	public static class FirstAbstractValue extends AbstractValue {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class SecondAbstractValue extends AbstractValue {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class ConcreteHolder {
		@NotNull
		private FirstValue value;

		public FirstValue getValue() {
			return value;
		}

		public void setValue(FirstValue value) {
			this.value = value;
		}
	}

	public static class InterfaceHolder {
		@NotNull
		private Value value;

		public Value getValue() {
			return value;
		}

		public void setValue(Value value) {
			this.value = value;
		}
	}

	public static class AbstractClassHolder {
		@NotNull
		private AbstractValue value;

		public AbstractValue getValue() {
			return value;
		}

		public void setValue(AbstractValue value) {
			this.value = value;
		}
	}
}
