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

package com.navercorp.fixturemonkey.customizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.MapNodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeNullityManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSizeManipulator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class MapSpec {
	private final ArbitraryTraverser traverser;
	private final List<NodeManipulator> manipulators;

	public MapSpec(ArbitraryTraverser traverser) {
		this.traverser = traverser;
		this.manipulators = new ArrayList<>();
	}

	public void size(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("should be min > max, min : " + min + " max : " + max);
		}
		manipulators.add(new NodeSizeManipulator(traverser, min, max));
	}

	public void key(Object key) {
		NodeManipulator manipulator = convertToNodeManipulator(key);
		NodeManipulator mapManipulator = new MapNodeManipulator(
			traverser,
			Collections.singletonList(manipulator),
			Collections.emptyList());
		manipulators.add(mapManipulator);
	}

	public void key(Consumer<MapSpec> consumer) {
		if (consumer == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}
		MapSpec mapSpec = new MapSpec(traverser);
		consumer.accept(mapSpec);
		ArrayList<NodeManipulator> nextManipulators = new ArrayList<>(mapSpec.manipulators);
		NodeManipulator mapManipulator = new MapNodeManipulator(
			traverser,
			nextManipulators,
			Collections.emptyList());
		manipulators.add(mapManipulator);
	}

	public void value(@Nullable Object value) {
		NodeManipulator manipulator = convertToNodeManipulator(value);
		NodeManipulator mapManipulator = new MapNodeManipulator(
			traverser,
			Collections.emptyList(),
			Collections.singletonList(manipulator));
		manipulators.add(mapManipulator);
	}

	public void value(Consumer<MapSpec> consumer) {
		if (consumer == null) {
			value((Object)null);
			return;
		}
		MapSpec mapSpec = new MapSpec(traverser);
		consumer.accept(mapSpec);
		ArrayList<NodeManipulator> nextManipulators = new ArrayList<>(mapSpec.manipulators);
		NodeManipulator mapManipulator = new MapNodeManipulator(
			traverser,
			Collections.emptyList(),
			nextManipulators);
		manipulators.add(mapManipulator);
	}

	public void entry(Object key, @Nullable Object value) {
		if (key == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}
		NodeManipulator keyManipulator = convertToNodeManipulator(key);
		NodeManipulator valueManipulator = convertToNodeManipulator(value);
		NodeManipulator mapManipulator = new MapNodeManipulator(
			traverser,
			Collections.singletonList(keyManipulator),
			Collections.singletonList(valueManipulator));
		manipulators.add(mapManipulator);
	}

	public List<NodeManipulator> getManipulators() {
		return manipulators;
	}

	private NodeManipulator convertToNodeManipulator(@Nullable Object value) {
		if (value instanceof Arbitrary) {
			return new NodeSetArbitraryManipulator<>((Arbitrary<?>)value);
		} else if (value == null) {
			return new NodeNullityManipulator(true);
		} else {
			return new NodeSetDecomposedValueManipulator<>(this.traverser, value);
		}
	}
}
