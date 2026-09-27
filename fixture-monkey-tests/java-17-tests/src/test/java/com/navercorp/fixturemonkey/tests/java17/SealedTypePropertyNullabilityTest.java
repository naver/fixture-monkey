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

package com.navercorp.fixturemonkey.tests.java17;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.function.Function;
import java.util.stream.IntStream;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;

class SealedTypePropertyNullabilityTest {
	private static final int SAMPLE_COUNT = 200;

	@Test
	void notNullSealedInterfacePropertyIsNeverNull() {
		FixtureMonkey sut = recordMonkey();

		long nullCount = countNulls(sut, SealedInterfaceHolder.class, SealedInterfaceHolder::value);

		then(nullCount).isZero();
	}

	@Test
	void notNullSealedClassPropertyIsNeverNull() {
		FixtureMonkey sut = recordMonkey();

		long nullCount = countNulls(sut, SealedClassHolder.class, SealedClassHolder::value);

		then(nullCount).isZero();
	}

	private FixtureMonkey recordMonkey() {
		return FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();
	}

	private <T> long countNulls(
		FixtureMonkey sut,
		Class<T> type,
		Function<T, Object> extractor
	) {
		return IntStream.range(0, SAMPLE_COUNT)
			.filter(it -> extractor.apply(sut.giveMeOne(type)) == null)
			.count();
	}

	public sealed interface SealedValue permits FirstSealedValue, SecondSealedValue {
	}

	public record FirstSealedValue(String name) implements SealedValue {
	}

	public record SecondSealedValue(String name) implements SealedValue {
	}

	public abstract static sealed class SealedClassValue permits FirstSealedClassValue, SecondSealedClassValue {
	}

	public static final class FirstSealedClassValue extends SealedClassValue {
	}

	public static final class SecondSealedClassValue extends SealedClassValue {
	}

	public record SealedInterfaceHolder(@NonNull SealedValue value) {
	}

	public record SealedClassHolder(@NonNull SealedClassValue value) {
	}
}
