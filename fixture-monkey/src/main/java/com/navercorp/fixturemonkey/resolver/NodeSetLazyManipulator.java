/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.resolver;

import java.util.List;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class NodeSetLazyManipulator<T> implements NodeManipulator {
	private final Supplier<T> supplier;

	NodeSetLazyManipulator(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		T value = supplier.get();
		if (value instanceof Arbitrary) {
			arbitraryNode.setArbitrary((Arbitrary<?>)value);
			return;
		}

		Class<?> actualType = Types.getActualType(arbitraryNode.getProperty().getType());
		if (!actualType.isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException(
				"The value is not of the same type as the property."
					+ " node type: " + arbitraryNode.getProperty().getType().getTypeName()
					+ " value type: " + value.getClass().getTypeName()
			);
		}
		setValue(arbitraryNode, value);
	}

	private void setValue(ArbitraryNode arbitraryNode, Object value) {
		List<ArbitraryNode> children = arbitraryNode.getChildren();
		if (children.isEmpty()) {
			arbitraryNode.setArbitrary(Arbitraries.just(value));
			return;
		}

		for (ArbitraryNode child : children) {
			Property childProperty = child.getProperty();
			setValue(child, childProperty.getValue(value));
		}
	}
}
