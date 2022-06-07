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

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.builder.ArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.MapNodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;

public final class MapSpec implements ArbitraryBuilderVisitor {
	private final ArbitraryTraverser traverser;
	private final String mapName;
	private final List<NodeManipulator> manipulators;

	public MapSpec(ArbitraryTraverser traverser, String mapName) {
		this.traverser = traverser;
		this.mapName = mapName;
		this.manipulators = new ArrayList<>();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void visit(ArbitraryBuilder arbitraryBuilder) {
		arbitraryBuilder.set(this.mapName, manipulators);
	}

	public <K> MapSpec addKey(K key) {
		// null 처리
		NodeManipulator manipulator = getSetManipulator(key);
		NodeManipulator mapManipulator = new MapNodeManipulator(traverser,
			new ArrayList<>(Collections.singletonList(manipulator)), new ArrayList<>());
		manipulators.add(mapManipulator);
		return this;
	}

	public MapSpec addKey(Consumer<MapSpec> consumer) {
		MapSpec mapSpec = new MapSpec(traverser, mapName);
		consumer.accept(mapSpec);
		ArrayList<NodeManipulator> nextManipulators = new ArrayList<>(mapSpec.manipulators);
		NodeManipulator mapManipulator = new MapNodeManipulator(traverser, nextManipulators, new ArrayList<>());
		manipulators.add(mapManipulator);
		return this;
	}

	public <V> MapSpec addValue(V value) {
		// null 처리
		NodeManipulator manipulator = getSetManipulator(value);
		NodeManipulator mapManipulator = new MapNodeManipulator(traverser, new ArrayList<>(), new ArrayList<>(
			Collections.singletonList(manipulator)));
		manipulators.add(mapManipulator);
		return this;
	}

	public MapSpec addValue(Consumer<MapSpec> consumer) {
		MapSpec mapSpec = new MapSpec(traverser, mapName);
		consumer.accept(mapSpec);
		ArrayList<NodeManipulator> nextManipulators = new ArrayList<>(mapSpec.manipulators);
		NodeManipulator mapManipulator = new MapNodeManipulator(traverser, new ArrayList<>(), nextManipulators);
		manipulators.add(mapManipulator);
		return this;
	}

	public <K, V> MapSpec put(K key, V value) {
		NodeManipulator keyManipulator = getSetManipulator(key);
		NodeManipulator valueManipulator = getSetManipulator(value);
		NodeManipulator mapManipulator = new MapNodeManipulator(traverser,
			new ArrayList<>(Collections.singletonList(keyManipulator)),
			new ArrayList<>(Collections.singletonList(valueManipulator)));
		manipulators.add(mapManipulator);
		return this;
	}

	private NodeManipulator getSetManipulator(Object value) {
		if (value instanceof Arbitrary) {
			return new NodeSetArbitraryManipulator<>((Arbitrary<?>)value);
		} else if (value != null) {
			return new NodeSetDecomposedValueManipulator<>(this.traverser, value);
		}
		return null;
	}
}
