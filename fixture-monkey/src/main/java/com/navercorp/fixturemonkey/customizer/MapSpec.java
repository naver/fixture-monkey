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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.resolver.AddMapEntryNodeManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.ChildrenNodeResolver;
import com.navercorp.fixturemonkey.resolver.CompositeNodeResolver;
import com.navercorp.fixturemonkey.resolver.MapNodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeNullityManipulator;
import com.navercorp.fixturemonkey.resolver.NodeResolver;
import com.navercorp.fixturemonkey.resolver.NodeSetArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSizeManipulator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class MapSpec {
	private final ArbitraryTraverser traverser;
	private final List<NodeResolver> resolvers;
	private final List<ArbitraryManipulator> arbitraryManipulators;

	public MapSpec(ArbitraryTraverser traverser, List<NodeResolver> resolvers) {
		this.traverser = traverser;
		this.resolvers = resolvers;
		this.arbitraryManipulators = new ArrayList<>();
	}

	public void size(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("should be min > max, min : " + min + " max : " + max);
		}
		arbitraryManipulators.add(new ArbitraryManipulator(new CompositeNodeResolver(new ArrayList<>(resolvers)), new NodeSizeManipulator(traverser, min, max)));
	}

	public void key(Object key) {
		NodeResolver resolver = new CompositeNodeResolver(new ArrayList<>(resolvers));
		NodeManipulator manipulator = new MapNodeManipulator(
			traverser,
			Collections.singletonList(convertToNodeManipulator(key)),
			Collections.emptyList()
		);
		arbitraryManipulators.add(new ArbitraryManipulator(resolver, manipulator));
	}

	public void key(Consumer<MapSpec> consumer) {
		if (consumer == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}
		NodeResolver resolver = new CompositeNodeResolver(new ArrayList<>(resolvers));
		arbitraryManipulators.add(new ArbitraryManipulator(resolver, new AddMapEntryNodeManipulator(traverser)));

		List<NodeResolver> nextResolvers = new ArrayList<>(resolvers);
		nextResolvers.add(new ChildrenNodeResolver("[-1]"));
		nextResolvers.add(new ChildrenNodeResolver("[0]"));
		MapSpec mapSpec = new MapSpec(traverser, nextResolvers);
		consumer.accept(mapSpec);
		arbitraryManipulators.addAll(mapSpec.arbitraryManipulators);
	}

	public void value(@Nullable Object value) {
		NodeResolver resolver = new CompositeNodeResolver(new ArrayList<>(resolvers));
		NodeManipulator manipulator = new MapNodeManipulator(
			traverser,
			Collections.emptyList(),
			Collections.singletonList(convertToNodeManipulator(value))
		);
		arbitraryManipulators.add(new ArbitraryManipulator(resolver, manipulator));
	}

	public void value(Consumer<MapSpec> consumer) {
		if (consumer == null) {
			value((Object)null);
			return;
		}
		NodeResolver resolver = new CompositeNodeResolver(new ArrayList<>(resolvers));
		arbitraryManipulators.add(new ArbitraryManipulator(resolver, new AddMapEntryNodeManipulator(traverser)));

		List<NodeResolver> nextResolvers = new ArrayList<>(resolvers);
		nextResolvers.add(new ChildrenNodeResolver("[-1]"));
		nextResolvers.add(new ChildrenNodeResolver("[1]"));
		MapSpec mapSpec = new MapSpec(traverser, nextResolvers);
		consumer.accept(mapSpec);
		arbitraryManipulators.addAll(mapSpec.arbitraryManipulators);
	}

	public void entry(Object key, @Nullable Object value) {
		if (key == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}
		NodeResolver resolver = new CompositeNodeResolver(new ArrayList<>(resolvers));
		NodeManipulator keyManipulator = convertToNodeManipulator(key);
		NodeManipulator valueManipulator = convertToNodeManipulator(value);
		NodeManipulator mapManipulator = new MapNodeManipulator(
			traverser,
			Collections.singletonList(keyManipulator),
			Collections.singletonList(valueManipulator));
		arbitraryManipulators.add(new ArbitraryManipulator(resolver, mapManipulator));
	}

	public void setElement(int index, @Nullable Object value) {
		List<NodeResolver> nextResolvers = new ArrayList<>(resolvers);
		nextResolvers.add(new ChildrenNodeResolver("[" + Integer.toString(index) + "]"));
		NodeResolver resolver = new CompositeNodeResolver(new ArrayList<>(nextResolvers));
		NodeManipulator manipulator = convertToNodeManipulator(value);
		arbitraryManipulators.add(new ArbitraryManipulator(resolver, manipulator));
	}

	public void listElement(int index, Consumer<MapSpec> consumer) {
		List<NodeResolver> nextResolvers = new ArrayList<>(resolvers);
		nextResolvers.add(new ChildrenNodeResolver("[" + Integer.toString(index) + "]"));
		MapSpec mapSpec = new MapSpec(traverser, nextResolvers);
		consumer.accept(mapSpec);
		arbitraryManipulators.addAll(mapSpec.arbitraryManipulators);
	}

	public void setField(String field, @Nullable Object value) {
		List<NodeResolver> nextResolvers = new ArrayList<>(resolvers);
		nextResolvers.add(new ChildrenNodeResolver(field));
		NodeResolver resolver = new CompositeNodeResolver(new ArrayList<>(nextResolvers));
		NodeManipulator manipulator = convertToNodeManipulator(value);
		arbitraryManipulators.add(new ArbitraryManipulator(resolver, manipulator));
	}

	public void fieldElement(String field, Consumer<MapSpec> consumer) {
		List<NodeResolver> nextResolvers = new ArrayList<>(resolvers);
		nextResolvers.add(new ChildrenNodeResolver(field));
		MapSpec mapSpec = new MapSpec(traverser, nextResolvers);
		consumer.accept(mapSpec);
		arbitraryManipulators.addAll(mapSpec.arbitraryManipulators);
	}

	public List<ArbitraryManipulator> getArbitraryManipulators() {
		return arbitraryManipulators;
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