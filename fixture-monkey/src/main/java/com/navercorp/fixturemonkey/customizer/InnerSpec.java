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
import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MIN_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
import com.navercorp.fixturemonkey.resolver.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.resolver.DefaultNodeResolver;
import com.navercorp.fixturemonkey.resolver.ManipulateOptions;
import com.navercorp.fixturemonkey.resolver.NodeAllEntryPredicate;
import com.navercorp.fixturemonkey.resolver.NodeEntryPredicate;
import com.navercorp.fixturemonkey.resolver.NodeKeyValuePredicate;
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

	private int min = DEFAULT_ELEMENT_MIN_SIZE;
	private int max = DEFAULT_ELEMENT_MAX_SIZE;
	private int entrySize = 0;
	private final List<ArbitraryManipulator> arbitraryManipulators;
	private final List<ContainerInfoManipulator> containerInfoManipulators;

	public InnerSpec(
		ArbitraryTraverser traverser,
		ManipulateOptions manipulateOptions,
		NodeResolver treePathResolver
	) {
		this.traverser = traverser;
		this.manipulateOptions = manipulateOptions;
		this.treePathResolver = treePathResolver;
		this.arbitraryManipulators = new ArrayList<>();
		this.containerInfoManipulators = new ArrayList<>();
	}

	public InnerSpec minSize(int min) {
		return size(min, min + DEFAULT_ELEMENT_MAX_SIZE);
	}

	public InnerSpec maxSize(int max) {
		return size(Math.max(DEFAULT_ELEMENT_MIN_SIZE, max - DEFAULT_ELEMENT_MAX_SIZE), max);
	}

	public InnerSpec size(int size) {
		return size(size, size);
	}

	public InnerSpec size(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("should be min > max, min : " + min + " max : " + max);
		}
		this.min = min;
		this.max = max;

		containerInfoManipulators.add(
			new ContainerInfoManipulator(
				this.treePathResolver,
				new ArbitraryContainerInfo(this.min, this.max, true)
			)
		);
		return this;
	}

	public InnerSpec key(Object key) {
		entrySize++;

		arbitraryManipulators.add(new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
					new DefaultNodeResolver(new NodeKeyValuePredicate(true))
				),
				convertToNodeManipulator(key)
			)
		);
		return this;
	}

	public InnerSpec key(Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}

		entrySize++;

		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
			new DefaultNodeResolver(new NodeKeyValuePredicate(true))
		);
		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nextResolver);
		consumer.accept(innerSpec);
		arbitraryManipulators.addAll(innerSpec.arbitraryManipulators);
		containerInfoManipulators.addAll(innerSpec.containerInfoManipulators);
		return this;
	}

	public InnerSpec value(@Nullable Object value) {
		setValue(value);
		return this;
	}

	public InnerSpec value(Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			setValue(null);
			return this;
		}

		entrySize++;
		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
			new DefaultNodeResolver(new NodeKeyValuePredicate(false))
		);
		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nextResolver);
		consumer.accept(innerSpec);
		arbitraryManipulators.addAll(innerSpec.arbitraryManipulators);
		containerInfoManipulators.addAll(innerSpec.containerInfoManipulators);
		this.containerInfoManipulators.add(
			new ContainerInfoManipulator(
				this.treePathResolver,
				new ArbitraryContainerInfo(this.entrySize + this.min, this.entrySize + this.max, true)
			)
		);
		return this;
	}

	public InnerSpec entry(Object key, @Nullable Object value) {
		setEntry(key, value);
		return this;
	}

	public InnerSpec entry(Object key, Consumer<InnerSpec> consumer) {
		if (key == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}

		if (consumer == null) {
			setEntry(key, null);
			return this;
		}

		entrySize++;

		arbitraryManipulators.add(
			new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
					new DefaultNodeResolver(new NodeKeyValuePredicate(true))
				),
				convertToNodeManipulator(key)
			)
		);

		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
			new DefaultNodeResolver(new NodeKeyValuePredicate(false))
		);

		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nextResolver);
		consumer.accept(innerSpec);
		arbitraryManipulators.addAll(innerSpec.arbitraryManipulators);
		containerInfoManipulators.addAll(innerSpec.containerInfoManipulators);
		return this;
	}

	public InnerSpec entry(Consumer<InnerSpec> consumer, @Nullable Object value) {
		entrySize++;

		this.containerInfoManipulators.add(
			new ContainerInfoManipulator(
				this.treePathResolver,
				new ArbitraryContainerInfo(this.entrySize + this.min, this.entrySize + this.max, true)
			)
		);
		arbitraryManipulators.add(
			new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
					new DefaultNodeResolver(new NodeKeyValuePredicate(false))
				),
				convertToNodeManipulator(value)
			)
		);

		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
			new DefaultNodeResolver(new NodeKeyValuePredicate(true))
		);
		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nextResolver);
		consumer.accept(innerSpec);
		arbitraryManipulators.addAll(innerSpec.arbitraryManipulators);
		containerInfoManipulators.addAll(innerSpec.containerInfoManipulators);
		return this;
	}

	public InnerSpec listElement(int index, @Nullable Object value) {
		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new ContainerElementPredicate(index))
		);
		NodeManipulator manipulator = convertToNodeManipulator(value);
		arbitraryManipulators.add(new ArbitraryManipulator(nextResolver, manipulator));
		return this;
	}

	public InnerSpec listElement(int index, Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			return listElement(index, (Object)null);
		}

		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new ContainerElementPredicate(index))
		);
		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nextResolver);
		consumer.accept(innerSpec);
		arbitraryManipulators.addAll(innerSpec.arbitraryManipulators);
		containerInfoManipulators.addAll(innerSpec.containerInfoManipulators);
		return this;
	}

	public InnerSpec property(String property, @Nullable Object value) {
		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new PropertyNameNodePredicate(property))
		);
		NodeManipulator manipulator = convertToNodeManipulator(value);
		arbitraryManipulators.add(new ArbitraryManipulator(nextResolver, manipulator));
		return this;
	}

	public InnerSpec property(String property, Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			return property(property, (Object)null);
		}

		NodeResolver nextResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new PropertyNameNodePredicate(property))
		);
		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nextResolver);
		consumer.accept(innerSpec);
		arbitraryManipulators.addAll(innerSpec.arbitraryManipulators);
		containerInfoManipulators.addAll(innerSpec.containerInfoManipulators);
		return this;
	}

	public InnerSpec keyLazy(Supplier<?> supplier) {
		entrySize++;

		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		arbitraryManipulators.add(new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
					new DefaultNodeResolver(new NodeKeyValuePredicate(true))
				),
				convertToNodeManipulator(lazyArbitrary)
			)
		);
		return this;
	}

	public InnerSpec valueLazy(Supplier<?> supplier) {
		entrySize++;

		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		arbitraryManipulators.add(new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
					new DefaultNodeResolver(new NodeKeyValuePredicate(false))
				),
				convertToNodeManipulator(lazyArbitrary)
			)
		);
		return this;
	}

	public InnerSpec entryLazy(Supplier<?> keySupplier, Supplier<?> valueSupplier) {
		entrySize++;

		LazyArbitrary<?> keyLazyArbitrary = LazyArbitrary.lazy(keySupplier);
		LazyArbitrary<?> valueLazyArbitrary = LazyArbitrary.lazy(valueSupplier);

		this.arbitraryManipulators.add(
			new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
					new DefaultNodeResolver(new NodeKeyValuePredicate(true))
				),
				convertToNodeManipulator(keyLazyArbitrary)
			)
		);

		this.arbitraryManipulators.add(
			new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
					new DefaultNodeResolver(new NodeKeyValuePredicate(false))
				),
				convertToNodeManipulator(valueLazyArbitrary)
			)
		);
		return this;
	}

	public InnerSpec allKeyLazy(Supplier<?> supplier) {
		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		arbitraryManipulators.add(new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeAllEntryPredicate()),
					new DefaultNodeResolver(new NodeKeyValuePredicate(true))
				),
				convertToNodeManipulator(lazyArbitrary)
			)
		);
		return this;
	}

	public InnerSpec allValueLazy(Supplier<?> supplier) {
		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		arbitraryManipulators.add(new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeAllEntryPredicate()),
					new DefaultNodeResolver(new NodeKeyValuePredicate(false))
				),
				convertToNodeManipulator(lazyArbitrary)
			)
		);
		return this;
	}

	public InnerSpec allEntryLazy(Supplier<?> keySupplier, Supplier<?> valueSupplier) {
		LazyArbitrary<?> keyLazyArbitrary = LazyArbitrary.lazy(keySupplier);
		LazyArbitrary<?> valueLazyArbitrary = LazyArbitrary.lazy(valueSupplier);

		this.arbitraryManipulators.add(
			new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeAllEntryPredicate()),
					new DefaultNodeResolver(new NodeKeyValuePredicate(true))
				),
				convertToNodeManipulator(keyLazyArbitrary)
			)
		);

		this.arbitraryManipulators.add(
			new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeAllEntryPredicate()),
					new DefaultNodeResolver(new NodeKeyValuePredicate(false))
				),
				convertToNodeManipulator(valueLazyArbitrary)
			)
		);
		return this;
	}

	public List<ArbitraryManipulator> getArbitraryManipulators() {
		return arbitraryManipulators;
	}

	public List<ContainerInfoManipulator> getContainerInfoManipulators() {
		return containerInfoManipulators;
	}

	private void setValue(@Nullable Object value) {
		entrySize++;

		this.arbitraryManipulators.add(
			new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
					new DefaultNodeResolver(new NodeKeyValuePredicate(false))
				),
				convertToNodeManipulator(value)
			)
		);
	}

	private void setEntry(Object key, @Nullable Object value) {
		if (key == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}
		entrySize++;

		this.arbitraryManipulators.add(
			new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
					new DefaultNodeResolver(new NodeKeyValuePredicate(true))
				),
				convertToNodeManipulator(key)
			)
		);

		this.arbitraryManipulators.add(
			new ArbitraryManipulator(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
					new DefaultNodeResolver(new NodeKeyValuePredicate(false))
				),
				convertToNodeManipulator(value)
			)
		);

	}

	private NodeManipulator convertToNodeManipulator(@Nullable Object value) {
		if (value instanceof Arbitrary) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy(() -> ((Arbitrary<?>)value).sample()),
				false
			);
		} else if (value instanceof LazyArbitrary) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				(LazyArbitrary<?>)value,
				false
			);
		} else if (value == null) {
			return new NodeNullityManipulator(true);
		} else {
			return new NodeSetDecomposedValueManipulator<>(traverser, manipulateOptions, value, false);
		}
	}
}
