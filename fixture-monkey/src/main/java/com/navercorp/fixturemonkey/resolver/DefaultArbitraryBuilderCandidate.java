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

package com.navercorp.fixturemonkey.resolver;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderCandidate;

class DefaultArbitraryBuilderCandidate<T> implements ArbitraryBuilderCandidate<T> {

	private final TypeReference<T> typeReference;

	private final Function<FixtureMonkey, ArbitraryBuilder<T>> arbitraryBuilderRegisterer;

	private DefaultArbitraryBuilderCandidate(
		TypeReference<T> typeReference,
		Function<FixtureMonkey, ArbitraryBuilder<T>> arbitraryBuilderRegisterer
	) {
		this.typeReference = typeReference;
		this.arbitraryBuilderRegisterer = arbitraryBuilderRegisterer;
	}

	public static <T> Builder<T> of(Class<T> classType) {
		return new Builder<T>()
			.classType(classType);
	}

	public static <T> Builder<T> of(TypeReference<T> typeReference) {
		return new Builder<T>()
			.typeReference(typeReference);
	}

	@Override
	public Class<?> getClassType() {
		return Types.getActualType(
			requireNonNull(this.typeReference).getAnnotatedType()
		);
	}

	@Override
	public Function<FixtureMonkey, ArbitraryBuilder<T>> getArbitraryBuilderRegisterer() {
		return arbitraryBuilderRegisterer;
	}

	static class Builder<T> {
		private TypeReference<T> typeReference;

		private UnaryOperator<ArbitraryBuilder<T>> builderSpec;

		public Builder<T> register(UnaryOperator<ArbitraryBuilder<T>> builderSpec) {
			this.builderSpec = builderSpec;
			return this;
		}

		public DefaultArbitraryBuilderCandidate<T> buildWithFixedValue(T value) {
			if (typeReference == null) {
				throw new IllegalArgumentException("typeReference must exist for fixed value");
			}

			return new DefaultArbitraryBuilderCandidate<>(
				typeReference,
				(fixtureMonkey) -> fixtureMonkey.giveMeBuilder(value)
			);
		}

		public DefaultArbitraryBuilderCandidate<T> build() {
			if (typeReference == null) {
				throw new IllegalArgumentException("typeReference must exist.");
			}

			if (builderSpec == null) {
				throw new IllegalArgumentException("builderSpec must be registered.");
			}

			Function<FixtureMonkey, ArbitraryBuilder<T>> registerer =
				(fixtureMonkey) ->
					builderSpec.apply(fixtureMonkey.giveMeBuilder(typeReference));

			return new DefaultArbitraryBuilderCandidate<>(typeReference, registerer);
		}

		private Builder<T> classType(Class<T> classType) {
			this.typeReference = new TypeReference<T>(classType) {
			};
			return this;
		}

		private Builder<T> typeReference(TypeReference<T> typeReference) {
			this.typeReference = typeReference;
			return this;
		}
	}
}
