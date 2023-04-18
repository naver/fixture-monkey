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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderCandidate;

@API(since = "0.5.6", status = Status.EXPERIMENTAL)
public final class ArbitraryBuilderSpecs {

	private final List<ArbitraryBuilderCandidate<?>> candidates = new ArrayList<>();

	private ArbitraryBuilderSpecs() {
	}

	public static ArbitraryBuilderSpecs newInstance() {
		return new ArbitraryBuilderSpecs();
	}

	public <T> SpecBuilder<T> of(Class<T> classType) {
		return new SpecBuilder<>(DefaultArbitraryBuilderCandidate.of(classType), this);
	}

	public <T> SpecBuilder<T> of(TypeReference<T> classType) {
		return new SpecBuilder<>(DefaultArbitraryBuilderCandidate.of(classType), this);
	}

	public List<ArbitraryBuilderCandidate<?>> getCandidates() {
		return Collections.unmodifiableList(candidates);
	}

	public void addCandidate(ArbitraryBuilderCandidate<?> candidate) {
		candidates.add(candidate);
	}

	public static class SpecBuilder<T> {
		private final DefaultArbitraryBuilderCandidate.Builder<T> builder;
		private final ArbitraryBuilderSpecs parent;

		private SpecBuilder(
			DefaultArbitraryBuilderCandidate.Builder<T> builder,
			ArbitraryBuilderSpecs parent
		) {
			this.builder = builder;
			this.parent = parent;
		}

		public ArbitraryBuilderSpecs registerValue(T value) {
			parent.addCandidate(
				builder.buildWithFixedValue(value)
			);
			return parent;
		}

		public ArbitraryBuilderSpecs registerBuilder(UnaryOperator<ArbitraryBuilder<T>> builderSpec) {
			parent.addCandidate(
				builder.register(builderSpec)
					.build()
			);
			return parent;
		}
	}
}
