package com.navercorp.objectfarm.api.expression;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * A type selector that matches nodes by their Java type.
 * <p>
 * Supports two matching modes:
 * <ul>
 *   <li>Exact matching: only the exact class matches</li>
 *   <li>Assignable matching: the class and its subtypes match</li>
 * </ul>
 * <p>
 * Example path expression: {@code $[type:com.example.MyType].fieldName}
 *
 * @see Selector
 */
public final class TypeSelector implements Selector {
	private final Class<?> targetType;
	private final boolean exact;

	public TypeSelector(Class<?> targetType) {
		this(targetType, true);
	}

	public TypeSelector(Class<?> targetType, boolean exact) {
		this.targetType = Objects.requireNonNull(targetType, "targetType must not be null");
		this.exact = exact;
	}

	public Class<?> getTargetType() {
		return targetType;
	}

	public boolean isExact() {
		return exact;
	}

	public boolean matchesType(Class<?> candidateType) {
		if (candidateType == null) {
			return false;
		}
		if (exact) {
			return targetType == candidateType;
		}
		return targetType.isAssignableFrom(candidateType);
	}

	@Override
	public String toExpression() {
		return "type:" + targetType.getName();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		TypeSelector that = (TypeSelector)obj;
		return exact == that.exact && Objects.equals(targetType, that.targetType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(targetType, exact);
	}

	@Override
	public String toString() {
		return "TypeSelector{targetType=" + targetType.getName() + ", exact=" + exact + "}";
	}
}
