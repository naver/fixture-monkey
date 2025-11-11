package com.navercorp.fixturemonkey.api.property;

import java.lang.reflect.AnnotatedType;
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * It is a property for a root type.
 * It does not support the equivalence of the type.
 */
@API(since = "1.1.6", status = Status.EXPERIMENTAL)
public final class ComparableRootProperty implements TreeRootProperty {
	private final Property delgeatedProperty;

	public ComparableRootProperty(AnnotatedType annotatedType) {
		this.delgeatedProperty = new TypeParameterProperty(annotatedType);
	}

	@Override
	public Property getDelgatedProperty() {
		return delgeatedProperty;
	}

	@Override
	public String toString() {
		return "RootProperty{"
			+ "annotatedType=" + delgeatedProperty.getAnnotatedType() + '}';
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ComparableRootProperty that = (ComparableRootProperty)obj;
		return Objects.equals(delgeatedProperty, that.delgeatedProperty);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(delgeatedProperty);
	}
}
