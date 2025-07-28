package com.navercorp.objectfarm.api.type;

import java.util.Collections;
import java.util.List;

// runtime에 타입이 확정되는 경우 (?, ? extends, 이런 건 무시)

// List<String> : {List, [String]}
// List<List<String>>: {List, [{List, [String]}]}
public interface JvmType {
	Class<?> getRawType();

	default List<? extends JvmType> getTypeVariables() {
		return Collections.emptyList();
	}
}
