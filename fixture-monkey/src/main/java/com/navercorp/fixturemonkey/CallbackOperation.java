package com.navercorp.fixturemonkey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.navercorp.fixturemonkey.arbitrary.AbstractArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;

public final class CallbackOperation<T extends BuilderManipulator> {
	private final Consumer<T> consumer;
	private final Function<T, List<T>> transformer;

	CallbackOperation(Consumer<T> callback, Function<T, List<T>> transformer) {
		this.consumer = callback;
		this.transformer = transformer;
	}

	void accept(T value) {
		consumer.accept(value);
	}

	List<T> apply(T value) {
		return transformer.apply(value);
	}

	public static <U extends BuilderManipulator> CallbackOperationBuilder<U> builder() {
		return new CallbackOperationBuilder<>();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static final class CallbackOperationBuilder<T extends BuilderManipulator> {
		private Consumer<T> callback = t -> {
		};
		private Consumer<AbstractArbitrarySet> callbackIfSet = null;
		private Function<T, List<T>> transformer = t -> {
			List<T> list = new ArrayList<>();
			list.add(t);
			return list;
		};
		private Function<AbstractArbitrarySet, List<AbstractArbitrarySet>> transformerIfSet = null;

		public CallbackOperationBuilder<T> callback(Consumer<T> callback) {
			this.callback = callback;
			return this;
		}

		public CallbackOperationBuilder<T> callbackIfSet(Consumer<AbstractArbitrarySet> callback) {
			this.callbackIfSet = callback;
			return this;
		}

		public CallbackOperationBuilder<T> transformer(Function<T, List<T>> transformer) {
			this.transformer = transformer;
			return this;
		}

		public CallbackOperationBuilder<T> transformerIfSet(
			Function<AbstractArbitrarySet, List<AbstractArbitrarySet>> transformer
		) {
			this.transformerIfSet = transformer;
			return this;
		}

		public CallbackOperation<T> build() {
			return new CallbackOperation<>(combineCallback(), combineTransformer());
		}

		private Consumer<T> combineCallback() {
			return t -> {
				if (callbackIfSet != null && t instanceof AbstractArbitrarySet) {
					callbackIfSet.accept((AbstractArbitrarySet)t);
				} else {
					callback.accept(t);
				}
			};
		}

		private Function<T, List<T>> combineTransformer() {
			return t -> {
				if (transformerIfSet != null && t instanceof AbstractArbitrarySet) {
					return (List)transformerIfSet.apply((AbstractArbitrarySet)t);
				}
				return transformer.apply(t);
			};
		}
	}
}
