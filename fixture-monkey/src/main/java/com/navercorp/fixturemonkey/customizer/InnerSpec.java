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

import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MAX_SIZE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.CompositeNodeResolver;
import com.navercorp.fixturemonkey.resolver.ContainerElementPredicate;
import com.navercorp.fixturemonkey.resolver.DefaultNodeResolver;
import com.navercorp.fixturemonkey.resolver.ManipulateOptions;
import com.navercorp.fixturemonkey.resolver.MapNodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeKeyValuePredicate;
import com.navercorp.fixturemonkey.resolver.NodeLastEntryPredicate;
import com.navercorp.fixturemonkey.resolver.NodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeNullityManipulator;
import com.navercorp.fixturemonkey.resolver.NodeResolver;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetLazyManipulator;
import com.navercorp.fixturemonkey.resolver.PropertyNameNodePredicate;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class InnerSpec {
	private final ArbitraryTraverser traverser;
	private final ManipulateOptions manipulateOptions;
	private final NodeResolver treePathResolver;

	private int entrySize = 0;
	private final List<ArbitraryManipulator> arbitraryManipulators;
	private final Map<NodeResolver, ArbitraryContainerInfo> containerInfosByNodeResolver;

	public InnerSpec(
		ArbitraryTraverser traverser,
		ManipulateOptions manipulateOptions,
		NodeResolver treePathResolver
	) {
		this.traverser = traverser;
		this.manipulateOptions = manipulateOptions;
		this.treePathResolver = treePathResolver;
		this.arbitraryManipulators = new ArrayList<>();
		this.containerInfosByNodeResolver = new HashMap<>();
	}

	public void minSize(int min) {
		size(min, min + DEFAULT_ELEMENT_MAX_SIZE);
	}

	public void maxSize(int max) {
		size(Math.max(0, max - DEFAULT_ELEMENT_MAX_SIZE), max);
	}

	public void size(int size) {
		size(size, size);
	}

	public void size(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("should be min > max, min : " + min + " max : " + max);
		}
		containerInfosByNodeResolver.put(this.treePathResolver, new ArbitraryContainerInfo(min, max, true));
	}

	public void key(Object key) {
		entrySize++;
		NodeManipulator manipulator = new MapNodeManipulator(
			entrySize - 1, // index
			Collections.singletonList(convertToNodeManipulator(key)),
			Collections.emptyList()
		);
		this.containerInfosByNodeResolver.put(
			this.treePathResolver,
			new ArbitraryContainerInfo(entrySize, entrySize, false)
		);
		arbitraryManipulators.add(new ArbitraryManipulator(this.treePathResolver, manipulator));
	}

	public void key(Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}

		entrySize++;
		this.containerInfosByNodeResolver.put(
			this.treePathResolver,
			new ArbitraryContainerInfo(entrySize, entrySize, false)
		);
		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeLastEntryPredicate()),
			new DefaultNodeResolver(new NodeKeyValuePredicate(true))
		);
		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nextResolver);
		consumer.accept(innerSpec);
		arbitraryManipulators.addAll(innerSpec.arbitraryManipulators);
		containerInfosByNodeResolver.putAll(innerSpec.containerInfosByNodeResolver);
	}

	public void value(@Nullable Object value) {
		setValue(value);
	}

	public void value(Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			setValue(null);
			return;
		}

		entrySize++;
		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeLastEntryPredicate()),
			new DefaultNodeResolver(new NodeKeyValuePredicate(false))
		);
		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nextResolver);
		consumer.accept(innerSpec);
		arbitraryManipulators.addAll(innerSpec.arbitraryManipulators);
		containerInfosByNodeResolver.putAll(innerSpec.containerInfosByNodeResolver);
		this.containerInfosByNodeResolver.put(
			this.treePathResolver,
			new ArbitraryContainerInfo(entrySize, entrySize, false)
		);
	}

	public void entry(Object key, @Nullable Object value) {
		setEntry(key, value);
	}

	public void entry(Object key, Consumer<InnerSpec> consumer) {
		if (key == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}

		if (consumer == null) {
			setEntry(key, null);
			return;
		}

		entrySize++;
		NodeManipulator keyManipulator = convertToNodeManipulator(key);
		NodeManipulator mapManipulator = new MapNodeManipulator(
			entrySize - 1, // index
			Collections.singletonList(keyManipulator),
			Collections.emptyList()
		);
		this.containerInfosByNodeResolver.put(
			this.treePathResolver,
			new ArbitraryContainerInfo(entrySize, entrySize, false)
		);
		arbitraryManipulators.add(new ArbitraryManipulator(this.treePathResolver, mapManipulator));

		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeLastEntryPredicate()),
			new DefaultNodeResolver(new NodeKeyValuePredicate(false))
		);

		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nextResolver);
		consumer.accept(innerSpec);
		arbitraryManipulators.addAll(innerSpec.arbitraryManipulators);
		containerInfosByNodeResolver.putAll(innerSpec.containerInfosByNodeResolver);
	}

	public void entry(Consumer<InnerSpec> consumer, @Nullable Object value) {
		entrySize++;
		NodeManipulator valueManipulator = convertToNodeManipulator(value);
		NodeManipulator mapManipulator = new MapNodeManipulator(
			entrySize - 1, // index
			Collections.emptyList(),
			Collections.singletonList(valueManipulator)
		);
		this.containerInfosByNodeResolver.put(
			this.treePathResolver,
			new ArbitraryContainerInfo(entrySize, entrySize, false)
		);
		arbitraryManipulators.add(new ArbitraryManipulator(this.treePathResolver, mapManipulator));

		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeLastEntryPredicate()),
			new DefaultNodeResolver(new NodeKeyValuePredicate(true))
		);
		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nextResolver);
		consumer.accept(innerSpec);
		arbitraryManipulators.addAll(innerSpec.arbitraryManipulators);
		containerInfosByNodeResolver.putAll(innerSpec.containerInfosByNodeResolver);
	}

	public void listElement(int index, @Nullable Object value) {
		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new ContainerElementPredicate(index))
		);
		NodeManipulator manipulator = convertToNodeManipulator(value);
		arbitraryManipulators.add(new ArbitraryManipulator(nextResolver, manipulator));
	}

	public void listElement(int index, Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			listElement(index, (Object)null);
			return;
		}

		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new ContainerElementPredicate(index))
		);
		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nextResolver);
		consumer.accept(innerSpec);
		arbitraryManipulators.addAll(innerSpec.arbitraryManipulators);
		containerInfosByNodeResolver.putAll(innerSpec.containerInfosByNodeResolver);
	}

	public void property(String property, @Nullable Object value) {
		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new PropertyNameNodePredicate(property))
		);
		NodeManipulator manipulator = convertToNodeManipulator(value);
		arbitraryManipulators.add(new ArbitraryManipulator(nextResolver, manipulator));
	}

	public void property(String property, Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			property(property, (Object)null);
			return;
		}

		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new PropertyNameNodePredicate(property))
		);
		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nextResolver);
		consumer.accept(innerSpec);
		arbitraryManipulators.addAll(innerSpec.arbitraryManipulators);
		containerInfosByNodeResolver.putAll(innerSpec.containerInfosByNodeResolver);
	}

	public List<ArbitraryManipulator> getArbitraryManipulators() {
		return arbitraryManipulators;
	}

	public Map<NodeResolver, ArbitraryContainerInfo> getContainerInfosByNodeResolver() {
		return containerInfosByNodeResolver;
	}

	private void setValue(@Nullable Object value) {
		entrySize++;
		NodeManipulator manipulator = new MapNodeManipulator(
			entrySize - 1, // index
			Collections.emptyList(),
			Collections.singletonList(convertToNodeManipulator(value))
		);
		this.containerInfosByNodeResolver.put(
			this.treePathResolver,
			new ArbitraryContainerInfo(entrySize, entrySize, false)
		);
		this.arbitraryManipulators.add(new ArbitraryManipulator(this.treePathResolver, manipulator));
	}

	private void setEntry(Object key, @Nullable Object value) {
		if (key == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}
		entrySize++;
		NodeManipulator keyManipulator = convertToNodeManipulator(key);
		NodeManipulator valueManipulator = convertToNodeManipulator(value);
		NodeManipulator mapManipulator = new MapNodeManipulator(
			entrySize - 1, // index
			Collections.singletonList(keyManipulator),
			Collections.singletonList(valueManipulator)
		);
		this.containerInfosByNodeResolver.put(
			this.treePathResolver,
			new ArbitraryContainerInfo(entrySize, entrySize, false)
		);
		this.arbitraryManipulators.add(new ArbitraryManipulator(this.treePathResolver, mapManipulator));
	}

	private NodeManipulator convertToNodeManipulator(@Nullable Object value) {
		if (value instanceof Arbitrary) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy(() -> ((Arbitrary<?>)value).sample())
			);
		} else if (value == null) {
			return new NodeNullityManipulator(true);
		} else {
			return new NodeSetDecomposedValueManipulator<>(traverser, manipulateOptions, value);
		}
	}
}
