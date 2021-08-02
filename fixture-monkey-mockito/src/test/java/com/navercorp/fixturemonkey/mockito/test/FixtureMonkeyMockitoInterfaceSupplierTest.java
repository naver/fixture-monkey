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

package com.navercorp.fixturemonkey.mockito.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import javax.annotation.Nonnull;

import net.jqwik.api.Example;

import lombok.Getter;
import lombok.Setter;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.mockito.arbitrary.MockitoInterfaceSupplier;

class FixtureMonkeyMockitoInterfaceSupplierTest {
	private final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
		.defaultInterfaceSupplier(MockitoInterfaceSupplier.INSTANCE)
		.build();

	@Example
	void interfaceTypeIsMock() {
		HasInterfaceType value = fixtureMonkey.giveMeOne(HasInterfaceType.class);
		assertThat(value.getInterfaceType()).isNotNull();

		when(value.getInterfaceType().getName()).thenReturn("test-value");
		assertThat(value.getInterfaceType().getName()).isEqualTo("test-value");
	}

	@Example
	void abstractTypeIsMock() {
		HasAbstractType value = fixtureMonkey.giveMeOne(HasAbstractType.class);
		assertThat(value.getAbstractType()).isNotNull();

		when(value.getAbstractType().getName()).thenReturn("test-value");
		assertThat(value.getAbstractType().getName()).isEqualTo("test-value");
	}

	@Getter
	@Setter
	public static class HasInterfaceType {
		private String name;

		@Nonnull
		private InterfaceType interfaceType;
	}

	public interface InterfaceType {
		String getName();
	}

	@Getter
	@Setter
	public static class HasAbstractType {
		private String name;

		@Nonnull
		private AbstractType abstractType;
	}

	public abstract static class AbstractType {
		public abstract String getName();
	}
}
