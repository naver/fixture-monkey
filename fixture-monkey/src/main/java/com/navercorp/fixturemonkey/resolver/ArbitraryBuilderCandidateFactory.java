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

import java.util.function.UnaryOperator;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderCandidate;

@API(since = "0.5.7", status = Status.MAINTAINED)
public final class ArbitraryBuilderCandidateFactory {
	public static <T> CandidateBuilder<T> of(Class<T> classType) {
		return new CandidateBuilder<>(DefaultArbitraryBuilderCandidate.of(classType));
	}

	public static <T> CandidateBuilder<T> of(TypeReference<T> classType) {
		return new CandidateBuilder<>(DefaultArbitraryBuilderCandidate.of(classType));
	}

	public static class CandidateBuilder<T> {
		private final DefaultArbitraryBuilderCandidate.Builder<T> builder;

		private CandidateBuilder(
			DefaultArbitraryBuilderCandidate.Builder<T> builder
		) {
			this.builder = builder;
		}

		public ArbitraryBuilderCandidate<T> value(T value) {
			return builder.buildWithFixedValue(value);
		}

		public ArbitraryBuilderCandidate<T> builder(UnaryOperator<ArbitraryBuilder<T>> builderSpec) {
			return builder.register(builderSpec).build();
		}
	}
}
