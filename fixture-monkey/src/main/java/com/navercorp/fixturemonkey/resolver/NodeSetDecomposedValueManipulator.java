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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
final class NodeSetDecomposedValueManipulator<T> implements NodeManipulator {
	private final T value;

	public NodeSetDecomposedValueManipulator(T value) {
		this.value = value;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		if (Types.getActualType(arbitraryNode.getProperty().getType()) != value.getClass()) {
			throw new IllegalArgumentException("The value is not of the same type as the property");
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
