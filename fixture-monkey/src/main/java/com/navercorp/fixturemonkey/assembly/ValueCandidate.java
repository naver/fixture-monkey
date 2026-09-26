package com.navercorp.fixturemonkey.assembly;

import org.jspecify.annotations.Nullable;

/**
 * Unified value metadata for a single path in the assembly state.
 * <p>
 * Origin (which scope the value was declared in) is encoded in the {@link DirectivePrecedence}: the root scope or a
 * defined scope.
 * <p>
 * Derivation (how the value was created) is tracked separately:
 * decomposed paths are recorded in TraceContext.decomposedPaths.
 */
final class ValueCandidate {
	final @Nullable Object value;
	final DirectivePrecedence precedence;

	ValueCandidate(@Nullable Object value, DirectivePrecedence precedence) {
		this.value = value;
		this.precedence = precedence;
	}

	ValueCandidate withValue(@Nullable Object newValue) {
		return new ValueCandidate(newValue, this.precedence);
	}

	/**
	 * Returns the origin label of the {@link DirectivePrecedence}.
	 */
	String sourceLabel() {
		return precedence.sourceLabel();
	}
}
