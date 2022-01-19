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

package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;
import java.util.function.BiConsumer;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public final class ArbitraryApply<T> implements BuilderManipulator {
	private final ArbitraryBuilder<T> toSampleArbitraryBuilder;
	private final BiConsumer<T, ArbitraryBuilder<T>> builderBiConsumer;

	public ArbitraryApply(
		ArbitraryBuilder<T> toSampleArbitraryBuilder,
		BiConsumer<T, ArbitraryBuilder<T>> builderBiConsumer
	) {
		this.toSampleArbitraryBuilder = toSampleArbitraryBuilder;
		this.builderBiConsumer = builderBiConsumer;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void accept(ArbitraryBuilder arbitraryBuilder) {
		arbitraryBuilder.apply(this);
	}

	@Override
	public BuilderManipulator copy() {
		return new ArbitraryApply<>(toSampleArbitraryBuilder, builderBiConsumer);
	}

	public ArbitraryBuilder<T> getToSampleArbitraryBuilder() {
		return toSampleArbitraryBuilder.copy();
	}

	public BiConsumer<T, ArbitraryBuilder<T>> getBuilderBiConsumer() {
		return builderBiConsumer;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryApply<?> that = (ArbitraryApply<?>)obj;
		return toSampleArbitraryBuilder.equals(that.toSampleArbitraryBuilder)
			&& builderBiConsumer.equals(that.builderBiConsumer);
	}

	@Override
	public int hashCode() {
		return Objects.hash(toSampleArbitraryBuilder, builderBiConsumer);
	}
}
