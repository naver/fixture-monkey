package com.navercorp.fixturemonkey.api.property;

import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * It is a property for a root type.
 * It does not support the equivalence of the type.
 */
@API(since = "1.1.6", status = Status.EXPERIMENTAL)
public final class ComparableRootProperty implements TreeRootProperty {
	private final Property delgeatedProperty;

	public ComparableRootProperty(JvmType jvmType) {
		this.delgeatedProperty = new TypeParameterProperty(jvmType);
	}

	@Override
	public Property getDelgatedProperty() {
		return delgeatedProperty;
	}

	@Override
	public String toString() {
		return "RootProperty{"
			+ "jvmType=" + delgeatedProperty.getJvmType() + '}';
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
