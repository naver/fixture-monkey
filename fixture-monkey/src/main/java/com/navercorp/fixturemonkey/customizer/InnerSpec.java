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
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.Constants;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.ContainerInfoHolder;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.FilterHolder;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.ManipulatorHolderSet;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.NodeResolverObjectHolder;
import com.navercorp.fixturemonkey.tree.CompositeNodeResolver;
import com.navercorp.fixturemonkey.tree.ContainerElementPredicate;
import com.navercorp.fixturemonkey.tree.DefaultNodeResolver;
import com.navercorp.fixturemonkey.tree.IdentityNodeResolver;
import com.navercorp.fixturemonkey.tree.NodeAllElementPredicate;
import com.navercorp.fixturemonkey.tree.NodeElementPredicate;
import com.navercorp.fixturemonkey.tree.NodeKeyPredicate;
import com.navercorp.fixturemonkey.tree.NodeResolver;
import com.navercorp.fixturemonkey.tree.NodeValuePredicate;
import com.navercorp.fixturemonkey.tree.PropertyNameNodePredicate;

/**
 * A type-independent specification for configuring nested properties.
 * <p>
 * Provides methods for setting configuration for nested properties, and can be particularly
 * useful for configuring map-type properties.
 * </p>
 * <p>
 * Instances of this class can be reused to consistently and easily configure nested properties.
 * </p>
 */
@SuppressWarnings("UnusedReturnValue")
@API(since = "0.4.0", status = Status.MAINTAINED)
public final class InnerSpec {
	private static final int FIRST_MANIPULATOR_SEQUENCE = 0;

	private final int sequence;
	private final NodeResolver treePathResolver;
	private final InnerSpecState state;
	private final List<InnerSpec> innerSpecs;

	private int entrySize = 0;
	private int manipulateSize = 0;

	/**
	 * Constructs a new {@code InnerSpec} instance that refers to the root property.
	 * <p>
	 * Can be further configured using methods provided by the class.
	 * </p>
	 */
	public InnerSpec() {
		this(FIRST_MANIPULATOR_SEQUENCE, IdentityNodeResolver.INSTANCE, new InnerSpecState(), new ArrayList<>());
	}

	private InnerSpec(int sequence, NodeResolver treePathResolver, InnerSpecState state, List<InnerSpec> innerSpecs) {
		this.sequence = sequence;
		this.treePathResolver = treePathResolver;
		this.state = state;
		this.innerSpecs = innerSpecs;
	}

	/**
	 * Configures the currently referred property further
	 * with the specifications provided inside the given {@link InnerSpec} object
	 *
	 * @param innerSpec An instance of {@link InnerSpec} containing the specifications
	 *                  to be applied to the currently referred property.
	 */
	public InnerSpec inner(InnerSpec innerSpec) {
		InnerSpec appendInnerSpec = newAppendNodeResolver(innerSpec, this.treePathResolver);
		this.innerSpecs.add(appendInnerSpec);
		return this;
	}

	/**
	 * Sets the size of the currently referred container property.
	 * {@code minSize} should be less than or equal to {@code maxSize}.
	 *
	 * @param minSize minimum size of the container to generate
	 * @param maxSize maximum size of the container to generate
	 */
	public InnerSpec size(int minSize, int maxSize) {
		if (minSize > maxSize) {
			throw new IllegalArgumentException("should be min > max, min : " + minSize + " max : " + maxSize);
		}

		this.state.setContainerInfoHolder(
			new ContainerInfoHolder(this.sequence + manipulateSize, this.treePathResolver, minSize, maxSize)
		);
		manipulateSize++;
		return this;
	}

	/**
	 * Sets the size of the currently referred container property.
	 *
	 * @param size size of the container to generate
	 */
	public InnerSpec size(int size) {
		return this.size(size, size);
	}

	/**
	 * Sets the size of the currently referred container property.
	 * The size of container property would be between {@code minSize} and
	 * {@code minSize} + {@link Constants#DEFAULT_ELEMENT_MAX_SIZE}
	 *
	 * @param minSize minimum size of the container to generate
	 */
	public InnerSpec minSize(int minSize) {
		return this.size(minSize, minSize + DEFAULT_ELEMENT_MAX_SIZE);
	}

	/**
	 * Sets the size of the currently referred container property.
	 * The size of container property would be between
	 * max(0, {@code maxSize} - {@link Constants#DEFAULT_ELEMENT_MAX_SIZE}) and {@code maxSize}
	 *
	 * @param maxSize maximum size of the container to generate
	 */
	public InnerSpec maxSize(int maxSize) {
		return this.size(Math.max(DEFAULT_ELEMENT_MIN_SIZE, maxSize - DEFAULT_ELEMENT_MAX_SIZE), maxSize);
	}

	/**
	 * Sets a key in the currently referred map property.
	 *
	 * @param key value of the map key to set
	 */
	public InnerSpec key(Object key) {
		entrySize++;
		setMapKey(key);
		return this;
	}

	/**
	 * Sets multiple keys in the currently referred map property.
	 *
	 * @param keys The keys to set in the map. Can be empty.
	 */
	public InnerSpec keys(Object... keys) {
		Arrays.stream(keys).forEach(this::key);
		return this;
	}

	/**
	 * Sets a nested map key within the currently referred map property.
	 *
	 * @param consumer a consumer function that takes an {@code InnerSpec} instance as an argument and configures
	 *                 the nested map key
	 */
	public InnerSpec key(Consumer<InnerSpec> consumer) {
		entrySize++;
		setMapKey(consumer);
		return this;
	}

	/**
	 * Sets a value in the currently referred map property.
	 *
	 * @param value value of the map value to set
	 */
	public InnerSpec value(Object value) {
		entrySize++;
		setMapValue(value);
		return this;
	}

	/**
	 * Sets multiple values in the currently referred map property.
	 *
	 * @param values The values to be added to the map. Can be empty.
	 */
	public InnerSpec values(Object... values) {
		Arrays.stream(values).forEach(this::value);
		return this;
	}

	/**
	 * Sets a nested map value within the currently referred map property.
	 *
	 * @param consumer a consumer function that takes an {@code InnerSpec} instance as an argument and configures
	 *                 the nested map value
	 */
	public InnerSpec value(Consumer<InnerSpec> consumer) {
		entrySize++;
		setMapValue(consumer);
		return this;
	}

	/**
	 * Sets an entry in the currently referred map property.
	 *
	 * @param key   value of the entry key to set
	 * @param value value of the entry value to set
	 */
	public InnerSpec entry(Object key, @Nullable Object value) {
		entrySize++;
		setMapEntry(key, value);
		return this;
	}

	/**
	 * Sets multiple key-value pairs in the map.
	 *
	 * @param entries The entries to be added to the map. Should be entered in key, value order. Can be empty.
	 */
	public InnerSpec entries(Object... entries) {
		if (entries.length % 2 != 0) {
			throw new IllegalArgumentException("key-value pairs for the Map should be entered");
		}

		IntStream.range(0, entries.length)
			.filter(i -> i % 2 == 0)
			.forEach(i -> {
				entry(entries[i], entries[i + 1]);
			});

		return this;
	}

	/**
	 * Sets an entry with a specified key within the currently referred map property,
	 * and applies a consumer function to configure the value.
	 *
	 * @param key      value of the map key to set
	 * @param consumer a consumer function that takes an {@code InnerSpec} instance as an argument and configures
	 *                 the nested map value
	 */
	public InnerSpec entry(Object key, Consumer<InnerSpec> consumer) {
		entrySize++;
		setMapEntry(key, consumer);
		return this;
	}

	/**
	 * Sets an entry with a specified value within the currently referred map property,
	 * and applies a consumer function to configure the key.
	 *
	 * @param consumer a consumer function that takes an {@code InnerSpec} instance as an argument and configures
	 *                 the nested map key
	 * @param value    value of the map value to set
	 */
	public InnerSpec entry(Consumer<InnerSpec> consumer, @Nullable Object value) {
		entrySize++;
		setMapEntry(consumer, value);
		return this;
	}

	/**
	 * Sets a key in the currently referred map property with a key obtained lazily from the given supplier.
	 *
	 * @param supplier a supplier function that provides the value of the map key to set.
	 */
	public InnerSpec keyLazy(Supplier<?> supplier) {
		entrySize++;
		setMapKey(supplier);
		return this;
	}

	/**
	 * Sets a value in the currently referred map property with a value obtained lazily from the given supplier.
	 *
	 * @param supplier a supplier function that provides the value of the map value to set.
	 */
	public InnerSpec valueLazy(Supplier<?> supplier) {
		entrySize++;
		setMapValue(supplier);
		return this;
	}

	/**
	 * Sets an entry in the currently referred map property with a key and value
	 * obtained lazily from the given suppliers.
	 *
	 * @param keySupplier a supplier function that provides the value of the map key to set.
	 * @param valueSupplier a function that provides the value of the map value to set.
	 */
	public InnerSpec entryLazy(Supplier<?> keySupplier, Supplier<?> valueSupplier) {
		entrySize++;

		LazyArbitrary<?> keyLazyArbitrary = LazyArbitrary.lazy(keySupplier);
		LazyArbitrary<?> valueLazyArbitrary = LazyArbitrary.lazy(valueSupplier);

		setMapEntry(keyLazyArbitrary, valueLazyArbitrary);
		return this;
	}

	/**
	 * Sets every key in the currently referred map property using a consumer function.
	 *
	 * @param consumer a consumer function that takes an {@code InnerSpec} instance as an argument and configures
	 *                 each key in the map property.
	 */
	public InnerSpec allKey(Consumer<InnerSpec> consumer) {
		setMapAllKey(consumer);
		return this;
	}

	/**
	 * Sets every key in the currently referred map property with a key obtained lazily from the given supplier.
	 *
	 * @param supplier a supplier function that provides the value of the map keys to set.
	 */
	public InnerSpec allKeyLazy(Supplier<?> supplier) {
		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		setMapAllKey(lazyArbitrary);
		return this;
	}

	/**
	 * Sets every value in the currently referred map property.
	 *
	 * @param value value of the map value to set
	 */
	public InnerSpec allValue(@Nullable Object value) {
		setMapAllValue(value);
		return this;
	}

	/**
	 * Sets every value in the currently referred map property using a consumer function.
	 *
	 * @param consumer a consumer function that takes an {@code InnerSpec} instance as an argument and configures
	 *                 each value in the map property.
	 */
	public InnerSpec allValue(Consumer<InnerSpec> consumer) {
		setMapAllValue(consumer);
		return this;
	}

	/**
	 * Sets every value in the currently referred map property with a value obtained lazily from the given supplier.
	 *
	 * @param supplier a supplier function that provides the value of the map values to set.
	 */
	public InnerSpec allValueLazy(Supplier<?> supplier) {
		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		setMapAllValue(lazyArbitrary);
		return this;
	}

	/**
	 * Sets every entry in the currently referred map property with a key
	 * obtained lazily from the given supplier and a specified value.
	 *
	 * @param keySupplier a supplier function that provides the value of the map keys to set.
	 * @param value       the value to set
	 */
	public InnerSpec allEntry(Supplier<?> keySupplier, Object value) {
		LazyArbitrary<?> keyLazyArbitrary = LazyArbitrary.lazy(keySupplier);

		setMapAllKey(keyLazyArbitrary);
		setMapAllValue(value);
		return this;
	}

	/**
	 * Sets every entry in the currently referred map property with a key and value
	 * obtained lazily from the given suppliers.
	 *
	 * @param keySupplier   a supplier function that provides the value of the map keys to set.
	 * @param valueSupplier a supplier function that provides the value of the map values to set.
	 */
	public InnerSpec allEntryLazy(Supplier<?> keySupplier, Supplier<?> valueSupplier) {
		return this.allEntry(keySupplier, valueSupplier);
	}

	/**
	 * Sets an element at the specified index within the currently referred container property.
	 *
	 * @param index index of the element to set
	 * @param value value of the element to set
	 */
	public InnerSpec listElement(int index, @Nullable Object value) {
		setListElement(index, value);
		return this;
	}

	/**
	 * Sets an element at the specified index within the currently referred container property
	 * using a consumer function.
	 *
	 * @param index    index of the element to set
	 * @param consumer a consumer function that takes an {@code InnerSpec} instance as an argument and configures
	 *                 the element.
	 */
	public InnerSpec listElement(int index, Consumer<InnerSpec> consumer) {
		setListElement(index, consumer);
		return this;
	}

	/**
	 * Sets every element within the currently referred container property.
	 *
	 * @param value    value of the elements to set
	 */
	public InnerSpec allListElement(@Nullable Object value) {
		return listElement(NO_OR_ALL_INDEX_INTEGER_VALUE, value);
	}

	/**
	 * Sets every element within the currently referred container property using a consumer function.
	 *
	 * @param consumer a consumer function that takes an {@code InnerSpec} instance as an argument and configures
	 *                 each element.
	 */
	public InnerSpec allListElement(Consumer<InnerSpec> consumer) {
		return listElement(NO_OR_ALL_INDEX_INTEGER_VALUE, consumer);
	}

	/**
	 * Sets a property within the currently referred property.
	 *
	 * @param propertyName name of the property to set
	 *                     (only string-formatted property names are allowed, and expressions are not supported)
	 * @param value        value of the property to set
	 */
	public InnerSpec property(String propertyName, @Nullable Object value) {
		setPropertyValue(propertyName, value);
		return this;
	}

	/**
	 * Sets a property within the currently referred property using a consumer function.
	 *
	 * @param propertyName name of the property to set
	 *                     (only string-formatted property names are allowed, and expressions are not supported)
	 * @param consumer     a consumer function that takes an {@code InnerSpec} instance as an argument and configures
	 *                     the nested property.
	 */
	public InnerSpec property(String propertyName, Consumer<InnerSpec> consumer) {
		setPropertyValue(propertyName, consumer);
		return this;
	}

	/**
	 * Sets the post-condition for the currently referred property.
	 *
	 * @param type         type of the property to set
	 * @param filter       a predicate function that determines the post-condition of the property
	 */
	public <T> InnerSpec postCondition(Class<T> type, Predicate<T> filter) {
		this.state.setFilterHolder(new FilterHolder(manipulateSize++, this.treePathResolver, type, filter));
		return this;
	}

	public ManipulatorSet getManipulatorSet(MonkeyManipulatorFactory monkeyManipulatorFactory) {
		ManipulatorHolderSet manipulatorHolderSet = traverse(this);
		return monkeyManipulatorFactory.newManipulatorSet(manipulatorHolderSet);
	}

	private InnerSpec newAppendNodeResolver(InnerSpec innerSpec, NodeResolver nodeResolver) {
		InnerSpec newSpec = new InnerSpec(
			innerSpec.sequence,
			innerSpec.treePathResolver,
			innerSpec.state.withPrefix(nodeResolver),
			new ArrayList<>()
		);

		for (InnerSpec spec : innerSpec.innerSpecs) {
			newSpec.innerSpecs.add(newAppendNodeResolver(spec, nodeResolver));
		}
		return newSpec;
	}

	private void setMapKey(Object mapKey) {
		if (mapKey == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}

		NodeResolver nextKeyNodeResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeElementPredicate(entrySize - 1)),
			new DefaultNodeResolver(new NodeKeyPredicate())
		);

		setValue(nextKeyNodeResolver, mapKey);
	}

	private void setMapAllKey(Object mapKey) {
		if (mapKey == null) {
			throw new IllegalArgumentException(
				"Map mapKey cannot be null."
			);
		}

		NodeResolver nextKeyNodeResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeAllElementPredicate()),
			new DefaultNodeResolver(new NodeKeyPredicate())
		);

		setValue(nextKeyNodeResolver, mapKey);
	}

	private void setMapValue(@Nullable Object mapValue) {
		NodeResolver nextValueNodeResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeElementPredicate(entrySize - 1)),
			new DefaultNodeResolver(new NodeValuePredicate())
		);

		setValue(nextValueNodeResolver, mapValue);
	}

	private void setMapAllValue(@Nullable Object mapValue) {
		NodeResolver nextValueNodeResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new NodeAllElementPredicate()),
			new DefaultNodeResolver(new NodeValuePredicate())
		);

		setValue(nextValueNodeResolver, mapValue);
	}

	private void setMapEntry(Object key, @Nullable Object value) {
		setMapKey(key);
		setMapValue(value);
	}

	private void setPropertyValue(String propertyName, @Nullable Object value) {
		CompositeNodeResolver nextNodeResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new PropertyNameNodePredicate(propertyName))
		);

		setValue(nextNodeResolver, value);
	}

	private void setListElement(int index, @Nullable Object value) {
		CompositeNodeResolver nextNodeResolver = new CompositeNodeResolver(
			this.treePathResolver,
			new DefaultNodeResolver(new ContainerElementPredicate(index))
		);

		setValue(nextNodeResolver, value);
	}

	@SuppressWarnings("unchecked")
	private void setValue(NodeResolver nextNodeResolver, @Nullable Object nextValue) {
		int nextSequence = this.sequence + manipulateSize;

		if (nextValue instanceof InnerSpec) {
			InnerSpec prefix = new InnerSpec(
				nextSequence,
				nextNodeResolver,
				new InnerSpecState(),
				new ArrayList<>()
			);
			this.innerSpecs.add(prefix.inner((InnerSpec)nextValue));
			manipulateSize++;
			return;
		}

		if (nextValue instanceof Consumer) {
			Consumer<InnerSpec> consumer = (Consumer<InnerSpec>)nextValue;
			InnerSpec nextInnerSpec = new InnerSpec(
				nextSequence,
				nextNodeResolver,
				new InnerSpecState(),
				new ArrayList<>()
			);
			consumer.accept(nextInnerSpec);
			this.innerSpecs.add(nextInnerSpec);
			manipulateSize++;
			return;
		}

		InnerSpecState nextInnerSpecState = new InnerSpecState();
		nextInnerSpecState.setObjectHolder(
			new NodeResolverObjectHolder(
				nextSequence,
				nextNodeResolver,
				nextValue
			)
		);

		InnerSpec nextInnerSpec = new InnerSpec(
			nextSequence,
			nextNodeResolver,
			nextInnerSpecState,
			new ArrayList<>()
		);
		this.innerSpecs.add(nextInnerSpec);
		manipulateSize++;
	}

	private ManipulatorHolderSet traverse(InnerSpec innerSpec) {
		List<NodeResolverObjectHolder> nodeResolverObjectHolders = new ArrayList<>();
		List<ContainerInfoHolder> containerInfoManipulators = new ArrayList<>();
		List<FilterHolder> postConditionManipulators = new ArrayList<>();

		if (innerSpec.state.getObjectHolder() != null) {
			nodeResolverObjectHolders.add(innerSpec.state.getObjectHolder());
		}

		if (innerSpec.state.getContainerInfoHolder() != null) {
			containerInfoManipulators.add(innerSpec.state.getContainerInfoHolder());
		}

		if (innerSpec.state.getFilterHolder() != null) {
			postConditionManipulators.add(innerSpec.state.getFilterHolder());
		}

		for (InnerSpec spec : innerSpec.innerSpecs) {
			ManipulatorHolderSet traversed = traverse(spec);
			nodeResolverObjectHolders.addAll(traversed.getNodeResolverObjectHolders());
			containerInfoManipulators.addAll(traversed.getContainerInfoManipulators());
			postConditionManipulators.addAll(traversed.getPostConditionManipulators());
		}

		return new ManipulatorHolderSet(
			nodeResolverObjectHolders,
			containerInfoManipulators,
			postConditionManipulators
		);
	}
}
