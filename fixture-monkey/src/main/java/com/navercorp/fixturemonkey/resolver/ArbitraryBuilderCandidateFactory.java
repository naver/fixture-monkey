package com.navercorp.fixturemonkey.resolver;

import java.util.function.UnaryOperator;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderCandidate;

@API(since = "0.5.7", status = Status.EXPERIMENTAL)
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
