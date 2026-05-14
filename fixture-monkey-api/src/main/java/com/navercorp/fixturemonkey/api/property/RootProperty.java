package com.navercorp.fixturemonkey.api.property;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It is a property for a root type.
 * It does not support the equivalence of the type.
 */
@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class RootProperty implements TreeRootProperty {
	private final Property delgeatedProperty;

	public RootProperty(Property delgeatedProperty) {
		this.delgeatedProperty = delgeatedProperty;
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
}
