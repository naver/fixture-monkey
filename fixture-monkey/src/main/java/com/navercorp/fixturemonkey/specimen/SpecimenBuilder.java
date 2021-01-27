package com.navercorp.fixturemonkey.specimen;

public final class SpecimenBuilder<T> {
	private final Class<T> specimenClass;

	public SpecimenBuilder(Class<T> specimenClass) {
		this.specimenClass = specimenClass;
	}

	public Class<T> getSpecimenClass() {
		return this.specimenClass;
	}
}
