package com.navercorp.fixturemonkey.api.arbitrary;

final class StringCombinableArbitrary implements CombinableArbitrary {
	// private final CombinableArbitrary combinableArbitrary;
	//
	// public StringCombinableArbitrary(CombinableArbitrary combinableArbitrary) {
	// 	this.combinableArbitrary = combinableArbitrary;
	// }

	@Override
	public Object combined() {

		return "string";
	}

	@Override
	public Object rawValue() {
		return "string";
	}

	@Override
	public void clear() {

	}

	@Override
	public boolean fixed() {
		return true;
	}
}
