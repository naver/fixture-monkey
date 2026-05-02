package com.navercorp.fixturemonkey.adapter.projection;

import javax.annotation.Nullable;

/**
 * Unified value metadata for a single path in the assembly state.
 * <p>
 * Origin (where the value came from) is encoded in the {@link ValueOrder} type:
 * {@link ValueOrder.UserOrder} for direct builder operations,
 * {@link ValueOrder.RegisterOrder} for registered builders.
 * <p>
 * Derivation (how the value was created) is tracked separately:
 * decomposed paths are recorded in TraceContext.decomposedPaths.
 */
final class ValueCandidate {
	@Nullable
	final Object value;
	final ValueOrder order;

	ValueCandidate(@Nullable Object value, ValueOrder order) {
		this.value = value;
		this.order = order;
	}

	ValueCandidate withValue(@Nullable Object newValue) {
		return new ValueCandidate(newValue, this.order);
	}

	/**
	 * Returns origin label derived from the ValueOrder type.
	 * "DIRECT" for user builder operations, "REGISTER" for registered builders.
	 */
	String sourceLabel() {
		return order.sourceLabel();
	}
}
