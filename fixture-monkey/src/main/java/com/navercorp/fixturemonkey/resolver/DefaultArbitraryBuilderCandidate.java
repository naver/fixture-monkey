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

import javax.annotation.Nullable;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderCandidate;

class DefaultArbitraryBuilderCandidate<T> implements ArbitraryBuilderCandidate<T> {

	@Nullable
	private final Class<T> classType;

	@Nullable
	private final TypeReference<T> typeReference;

	private final Function<FixtureMonkey, ArbitraryBuilder<T>> arbitraryBuilderRegisterer;

	private DefaultArbitraryBuilderCandidate(
		@Nullable Class<T> classType,
		@Nullable TypeReference<T> typeReference,
		Function<FixtureMonkey, ArbitraryBuilder<T>> arbitraryBuilderRegisterer
	) {
		this.classType = classType;
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
		if (classType == null) {
			return Types.getActualType(
				requireNonNull(this.typeReference).getAnnotatedType()
			);
		}

		return classType;
	}

	@Override
	public Function<FixtureMonkey, ArbitraryBuilder<T>> getArbitraryBuilderRegisterer() {
		return arbitraryBuilderRegisterer;
	}

	static class Builder<T> {
		private Class<T> classType;

		private TypeReference<T> typeReference;

		private UnaryOperator<ArbitraryBuilder<T>> builderSpec;

		public Builder<T> register(UnaryOperator<ArbitraryBuilder<T>> builderSpec) {
			this.builderSpec = builderSpec;
			return this;
		}

		public DefaultArbitraryBuilderCandidate<T> buildWithFixedValue(T value) {
			if (classType == null) {
				throw new IllegalArgumentException("classType must exist for fixed value");
			}

			return new DefaultArbitraryBuilderCandidate<>(
				classType,
				typeReference,
				(fixtureMonkey) -> fixtureMonkey.giveMeBuilder(value)
			);
		}

		public DefaultArbitraryBuilderCandidate<T> build() {
			if (classType == null && typeReference == null) {
				throw new IllegalArgumentException("Either classType or typeReference must exist.");
			}

			if (classType != null && typeReference != null) {
				throw new IllegalArgumentException("Cannot declare classType and typeReference at the same time");
			}

			if (builderSpec == null) {
				throw new IllegalArgumentException("builderSpec must be registered.");
			}

			Function<FixtureMonkey, ArbitraryBuilder<T>> registerer;

			if (classType != null) {
				registerer = (fixtureMonkey) ->
					builderSpec.apply(fixtureMonkey.giveMeBuilder(classType));
			} else {
				registerer = (fixtureMonkey) ->
					builderSpec.apply(fixtureMonkey.giveMeBuilder(typeReference));
			}

			return new DefaultArbitraryBuilderCandidate<>(classType, typeReference, registerer);
		}

		private Builder<T> classType(Class<T> classType) {
			this.classType = classType;
			return this;
		}

		private Builder<T> typeReference(TypeReference<T> typeReference) {
			this.typeReference = typeReference;
			return this;
		}
	}
}
