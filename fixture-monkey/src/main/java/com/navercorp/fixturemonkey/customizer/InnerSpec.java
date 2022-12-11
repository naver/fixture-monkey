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
import static com.navercorp.fixturemonkey.Constants.NO_OR_ALL_INDEX_INTEGER_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.resolver.CompositeNodeResolver;
import com.navercorp.fixturemonkey.resolver.ContainerElementPredicate;
import com.navercorp.fixturemonkey.resolver.DefaultNodeResolver;
import com.navercorp.fixturemonkey.resolver.NodeAllEntryPredicate;
import com.navercorp.fixturemonkey.resolver.NodeEntryPredicate;
import com.navercorp.fixturemonkey.resolver.NodeKeyValuePredicate;
import com.navercorp.fixturemonkey.resolver.NodeResolver;
import com.navercorp.fixturemonkey.resolver.PropertyNameNodePredicate;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class InnerSpec {
	private static final Object UNINITIALIZED_VALUE = new Object();
	private final NodeResolver treePathResolver;
	@Nullable
	private final Object value;
	@Nullable
	private ArbitraryContainerInfo containerInfo;
	private final List<InnerSpec> innerSpecs;

	private int entrySize = 0;

	public InnerSpec(
		NodeResolver treePathResolver,
		@Nullable Object value,
		@Nullable ArbitraryContainerInfo containerInfo
	) {
		this.treePathResolver = treePathResolver;
		this.value = value;
		this.containerInfo = containerInfo;
		this.innerSpecs = new ArrayList<>();
	}

	public InnerSpec size(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("should be min > max, min : " + min + " max : " + max);
		}

		containerInfo = new ArbitraryContainerInfo(min, max, true);
		return this;
	}

	public InnerSpec size(int size) {
		return this.size(size, size);
	}

	public InnerSpec minSize(int min) {
		return this.size(min, min + DEFAULT_ELEMENT_MAX_SIZE);
	}

	public InnerSpec maxSize(int max) {
		return this.size(Math.max(DEFAULT_ELEMENT_MIN_SIZE, max - DEFAULT_ELEMENT_MAX_SIZE), max);
	}

	public InnerSpec key(Object key) {
		entrySize++;

		this.innerSpecs.add(
			new InnerSpec(
				nextKeyNodeResolver(),
				key,
				null
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

		InnerSpec nextNodeInnerSpec = new InnerSpec(nextKeyNodeResolver(), UNINITIALIZED_VALUE, null);
		consumer.accept(nextNodeInnerSpec);
		this.innerSpecs.add(nextNodeInnerSpec);
		return this;
	}

	public InnerSpec value(Object value) {
		setValue(value);
		return this;
	}

	public InnerSpec value(Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			setValue(null);
			return this;
		}

		entrySize++;

		InnerSpec nextNodeInnerSpec = new InnerSpec(nextValueNodeResolver(), UNINITIALIZED_VALUE, null);
		consumer.accept(nextNodeInnerSpec);
		this.innerSpecs.add(nextNodeInnerSpec);
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

		this.innerSpecs.add(
			new InnerSpec(
				nextKeyNodeResolver(),
				key,
				null
			)
		);

		InnerSpec nextNodeInnerSpec = new InnerSpec(nextValueNodeResolver(), UNINITIALIZED_VALUE, null);
		consumer.accept(nextNodeInnerSpec);
		this.innerSpecs.add(nextNodeInnerSpec);
		return this;
	}

	public InnerSpec entry(Consumer<InnerSpec> consumer, @Nullable Object value) {
		entrySize++;

		this.innerSpecs.add(
			new InnerSpec(
				nextValueNodeResolver(),
				value,
				null
			)
		);

		InnerSpec nextNodeInnerSpec = new InnerSpec(nextKeyNodeResolver(), UNINITIALIZED_VALUE, null);
		consumer.accept(nextNodeInnerSpec);
		this.innerSpecs.add(nextNodeInnerSpec);
		return this;
	}

	public InnerSpec keyLazy(Supplier<?> supplier) {
		entrySize++;

		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		this.innerSpecs.add(
			new InnerSpec(
				nextKeyNodeResolver(),
				lazyArbitrary,
				null
			)
		);
		return this;
	}

	public InnerSpec valueLazy(Supplier<?> supplier) {
		entrySize++;

		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		this.innerSpecs.add(
			new InnerSpec(
				nextValueNodeResolver(),
				lazyArbitrary,
				null
			)
		);
		return this;
	}

	public InnerSpec entryLazy(Supplier<?> keySupplier, Supplier<?> valueSupplier) {
		entrySize++;

		LazyArbitrary<?> keyLazyArbitrary = LazyArbitrary.lazy(keySupplier);
		LazyArbitrary<?> valueLazyArbitrary = LazyArbitrary.lazy(valueSupplier);

		this.innerSpecs.add(
			new InnerSpec(
				nextKeyNodeResolver(),
				keyLazyArbitrary,
				null
			)
		);

		this.innerSpecs.add(
			new InnerSpec(
				nextValueNodeResolver(),
				valueLazyArbitrary,
				null
			)
		);
		return this;
	}

	public InnerSpec allKey(Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			setKeyAll(null);
			return this;
		}

		InnerSpec nextNodeInnerSpec = new InnerSpec(nextAllKeyNodeResolver(), UNINITIALIZED_VALUE, null);
		consumer.accept(nextNodeInnerSpec);
		this.innerSpecs.add(nextNodeInnerSpec);
		return this;
	}

	public InnerSpec allKeyLazy(Supplier<?> supplier) {
		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		this.innerSpecs.add(
			new InnerSpec(
				nextAllKeyNodeResolver(),
				lazyArbitrary,
				null
			)
		);
		return this;
	}

	public InnerSpec allValue(@Nullable Object value) {
		this.innerSpecs.add(
			new InnerSpec(
				nextAllValueNodeResolver(),
				value,
				null
			)
		);

		return this;
	}

	public InnerSpec allValue(Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			allValue((Object)null);
			return this;
		}

		InnerSpec nextNodeInnerSpec = new InnerSpec(nextAllValueNodeResolver(), UNINITIALIZED_VALUE, null);
		consumer.accept(nextNodeInnerSpec);
		this.innerSpecs.add(nextNodeInnerSpec);
		return this;
	}

	public InnerSpec allValueLazy(Supplier<?> supplier) {
		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		this.innerSpecs.add(
			new InnerSpec(
				nextAllValueNodeResolver(),
				lazyArbitrary,
				null
			)
		);
		return this;
	}

	public InnerSpec allEntry(Supplier<?> keySupplier, Object value) {
		LazyArbitrary<?> keyLazyArbitrary = LazyArbitrary.lazy(keySupplier);

		this.innerSpecs.add(
			new InnerSpec(
				nextAllKeyNodeResolver(),
				keyLazyArbitrary,
				null
			)
		);

		this.innerSpecs.add(
			new InnerSpec(
				nextAllValueNodeResolver(),
				value,
				null
			)
		);
		return this;
	}

	public InnerSpec allEntryLazy(Supplier<?> keySupplier, Supplier<?> valueSupplier) {
		return this.allEntry(keySupplier, valueSupplier);
	}

	public InnerSpec listElement(int index, @Nullable Object value) {
		this.innerSpecs.add(
			new InnerSpec(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new ContainerElementPredicate(index))
				),
				value,
				null
			)
		);

		return this;
	}

	public InnerSpec listElement(int index, Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			return listElement(index, (Object)null);
		}

		NodeResolver nextNodeResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new ContainerElementPredicate(index))
		);

		InnerSpec nextNodeInnerSpec = new InnerSpec(nextNodeResolver, UNINITIALIZED_VALUE, null);
		consumer.accept(nextNodeInnerSpec);
		this.innerSpecs.add(nextNodeInnerSpec);
		return this;
	}

	public InnerSpec allListElement(@Nullable Object value) {
		return listElement(NO_OR_ALL_INDEX_INTEGER_VALUE, value);
	}

	public InnerSpec allListElement(Consumer<InnerSpec> consumer) {
		return listElement(NO_OR_ALL_INDEX_INTEGER_VALUE, consumer);
	}

	public InnerSpec property(String property, @Nullable Object value) {
		this.innerSpecs.add(
			new InnerSpec(
				new CompositeNodeResolver(
					this.treePathResolver,
					new DefaultNodeResolver(new PropertyNameNodePredicate(property))
				),
				value,
				null
			)
		);

		return this;
	}

	public InnerSpec property(String property, Consumer<InnerSpec> consumer) {
		if (consumer == null) {
			return property(property, (Object)null);
		}

		NodeResolver nextNodeResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new PropertyNameNodePredicate(property))
		);

		InnerSpec nextNodeInnerSpec = new InnerSpec(nextNodeResolver, UNINITIALIZED_VALUE, null);
		consumer.accept(nextNodeInnerSpec);
		this.innerSpecs.add(nextNodeInnerSpec);
		return this;
	}

	public static Object getUninitializedValue() {
		return UNINITIALIZED_VALUE;
	}

	public NodeResolver getTreePathResolver() {
		return treePathResolver;
	}

	public Object getValue() {
		return value;
	}

	public ArbitraryContainerInfo getContainerInfo() {
		return containerInfo;
	}

	public List<InnerSpec> getInnerSpecs() {
		return innerSpecs;
	}

	public boolean isLeaf() {
		return innerSpecs.isEmpty();
	}

	private void setValue(@Nullable Object value) {
		entrySize++;

		this.innerSpecs.add(
			new InnerSpec(
				nextValueNodeResolver(),
				value,
				null
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

		this.innerSpecs.add(
			new InnerSpec(
				nextKeyNodeResolver(),
				key,
				null
			)
		);

		this.innerSpecs.add(
			new InnerSpec(
				nextValueNodeResolver(),
				value,
				null
			)
		);
	}

	private void setKeyAll(@Nullable Object value) {
		this.innerSpecs.add(
			new InnerSpec(
				nextAllKeyNodeResolver(),
				value,
				null
			)
		);
	}

	private NodeResolver nextKeyNodeResolver() {
		return new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
			new DefaultNodeResolver(new NodeKeyValuePredicate(true))
		);
	}

	private NodeResolver nextValueNodeResolver() {
		return new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeEntryPredicate(entrySize - 1)),
			new DefaultNodeResolver(new NodeKeyValuePredicate(false))
		);
	}

	private NodeResolver nextAllKeyNodeResolver() {
		return new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeAllEntryPredicate()),
			new DefaultNodeResolver(new NodeKeyValuePredicate(true))
		);
	}

	private NodeResolver nextAllValueNodeResolver() {
		return new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeAllEntryPredicate()),
			new DefaultNodeResolver(new NodeKeyValuePredicate(false))
		);
	}
}
