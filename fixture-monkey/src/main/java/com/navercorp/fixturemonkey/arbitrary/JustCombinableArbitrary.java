package com.navercorp.fixturemonkey.arbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.customizer.Values.Just;

public final class JustCombinableArbitrary<T>  implements CombinableArbitrary<T> {
	private final Just just;

	public JustCombinableArbitrary(Just just) {
		this.just = just;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T combined() {
		return (T)just.getValue();
	}

	@Override
	public Object rawValue() {
		return just.getValue();
	}

	@Override
	public void clear() {

	}

	@Override
	public boolean fixed() {
		return true;
	}
}
