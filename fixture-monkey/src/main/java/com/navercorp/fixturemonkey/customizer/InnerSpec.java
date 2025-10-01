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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.Constants;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.ContainerInfoSnapshot;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.FilterSnapshot;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.ManipulatorHolderSet;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.NodeSetManipulatorSnapshot;
import com.navercorp.fixturemonkey.expression.DefaultDeclarativeExpression;

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
	private final DefaultDeclarativeExpression declarativeExpression;
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
		this(
			FIRST_MANIPULATOR_SEQUENCE,
			new DefaultDeclarativeExpression(),
			new InnerSpecState(),
			new ArrayList<>()
		);
	}

	private InnerSpec(
		int sequence,
		DefaultDeclarativeExpression declarativeExpression,
		InnerSpecState state,
		List<InnerSpec> innerSpecs
	) {
		this.sequence = sequence;
		this.declarativeExpression = declarativeExpression;
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
		InnerSpec nextInnerSpec = newNextInnerSpec(innerSpec, this.declarativeExpression);
		this.innerSpecs.add(nextInnerSpec);
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

		this.state.setContainerInfoSnapshot(
			new ContainerInfoSnapshot(this.sequence + manipulateSize, this.declarativeExpression, minSize, maxSize)
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
	 * Sets multiple keys in the currently referred map property from a Collection.
	 *
	 * @param keys The Collection of keys to set in the map. Can be empty.
	 */
	public InnerSpec keys(Collection<?> keys) {
		keys.forEach(this::key);
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
	 * Sets a value in the currently referred map property from a Collection.
	 *
	 * @param values The Collection of values to set in the map. Can be empty.
	 */
	public InnerSpec values(Collection<?> values) {
		values.forEach(this::value);
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
	 * Sets multiple key-value pairs in the map from a Collection.
	 *
	 * @param entries The entries to be added to the map. Should be entered in key, value order. Can be empty.
	 */
	public InnerSpec entries(Collection<?> entries) {
		if (entries.size() % 2 != 0) {
			throw new IllegalArgumentException("key-value pairs for the Map should be entered");
		}

		IntStream.range(0, entries.size())
			.filter(i -> i % 2 == 0)
			.forEach(i -> entry(entries.toArray()[i], entries.toArray()[i + 1]));

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
			.forEach(i -> entry(entries[i], entries[i + 1]));

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
	 * @param keySupplier   a supplier function that provides the value of the map key to set.
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
	 * @param value value of the elements to set
	 */
	public InnerSpec allListElement(@Nullable Object value) {
		setListAllElements(value);
		return this;
	}

	/**
	 * Sets every element within the currently referred container property using a consumer function.
	 *
	 * @param consumer a consumer function that takes an {@code InnerSpec} instance as an argument and configures
	 *                 each element.
	 */
	public InnerSpec allListElement(Consumer<InnerSpec> consumer) {
		setListAllElements(consumer);
		return this;
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
	 * @param type   type of the property to set
	 * @param filter a predicate function that determines the post-condition of the property
	 */
	public <T> InnerSpec postCondition(Class<T> type, Predicate<T> filter) {
		this.state.setFilterSnapshot(new FilterSnapshot(manipulateSize++, this.declarativeExpression, type, filter));
		return this;
	}

	public ManipulatorSet getManipulatorSet(MonkeyManipulatorFactory monkeyManipulatorFactory) {
		ManipulatorHolderSet manipulatorHolderSet = traverse(this);
		return monkeyManipulatorFactory.newManipulatorSet(manipulatorHolderSet);
	}

	private InnerSpec newNextInnerSpec(InnerSpec innerSpec, DefaultDeclarativeExpression parentDeclarativeExpression) {
		InnerSpec newSpec = new InnerSpec(
			innerSpec.sequence,
			innerSpec.declarativeExpression,
			innerSpec.state.withPrefix(parentDeclarativeExpression),
			new ArrayList<>()
		);

		for (InnerSpec childSpec : innerSpec.innerSpecs) {
			newSpec.innerSpecs.add(newNextInnerSpec(childSpec, parentDeclarativeExpression));
		}
		return newSpec;
	}

	private void setMapKey(Object mapKey) {
		if (mapKey == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}

		setValue(
			this.declarativeExpression.element(entrySize - 1).key(),
			mapKey
		);
	}

	private void setMapAllKey(Object mapKey) {
		if (mapKey == null) {
			throw new IllegalArgumentException(
				"Map mapKey cannot be null."
			);
		}

		setValue(this.declarativeExpression.allElement().key(), mapKey);
	}

	private void setMapValue(@Nullable Object mapValue) {
		setValue(this.declarativeExpression.element(entrySize - 1).value(), mapValue);
	}

	private void setMapAllValue(@Nullable Object mapValue) {
		setValue(this.declarativeExpression.allElement().value(), mapValue);
	}

	private void setMapEntry(Object key, @Nullable Object value) {
		setMapKey(key);
		setMapValue(value);
	}

	private void setPropertyValue(String propertyName, @Nullable Object value) {
		setValue(this.declarativeExpression.property(propertyName), value);
	}

	private void setListAllElements(@Nullable Object value) {
		setValue(this.declarativeExpression.allElement(), value);
	}

	private void setListElement(int index, @Nullable Object value) {
		setValue(this.declarativeExpression.element(index), value);
	}

	@SuppressWarnings("unchecked")
	private void setValue(DefaultDeclarativeExpression defaultDeclarativeExpression, @Nullable Object nextValue) {
		int nextSequence = this.sequence + manipulateSize;

		if (nextValue instanceof InnerSpec) {
			InnerSpec prefix = new InnerSpec(
				nextSequence,
				defaultDeclarativeExpression,
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
				defaultDeclarativeExpression,
				new InnerSpecState(),
				new ArrayList<>()
			);
			consumer.accept(nextInnerSpec);
			this.innerSpecs.add(nextInnerSpec);
			manipulateSize++;
			return;
		}

		InnerSpecState nextInnerSpecState = new InnerSpecState();
		nextInnerSpecState.setNodeManipulatorSnapshot(
			new NodeSetManipulatorSnapshot(
				nextSequence,
				defaultDeclarativeExpression,
				nextValue
			)
		);

		InnerSpec nextInnerSpec = new InnerSpec(
			nextSequence,
			defaultDeclarativeExpression,
			nextInnerSpecState,
			new ArrayList<>()
		);
		this.innerSpecs.add(nextInnerSpec);
		manipulateSize++;
	}

	private ManipulatorHolderSet traverse(InnerSpec innerSpec) {
		List<NodeSetManipulatorSnapshot> nodeSetManipulatorSnapshots = new ArrayList<>();
		List<ContainerInfoSnapshot> containerInfoManipulators = new ArrayList<>();
		List<FilterSnapshot> postConditionManipulators = new ArrayList<>();

		if (innerSpec.state.getNodeManipulatorSnapshot() != null) {
			nodeSetManipulatorSnapshots.add(innerSpec.state.getNodeManipulatorSnapshot());
		}

		if (innerSpec.state.getContainerInfoHolder() != null) {
			containerInfoManipulators.add(innerSpec.state.getContainerInfoHolder());
		}

		if (innerSpec.state.getFilterHolder() != null) {
			postConditionManipulators.add(innerSpec.state.getFilterHolder());
		}

		for (InnerSpec spec : innerSpec.innerSpecs) {
			ManipulatorHolderSet traversed = traverse(spec);
			nodeSetManipulatorSnapshots.addAll(traversed.getNodeResolverObjectHolders());
			containerInfoManipulators.addAll(traversed.getContainerInfoManipulators());
			postConditionManipulators.addAll(traversed.getPostConditionManipulators());
		}

		return new ManipulatorHolderSet(
			nodeSetManipulatorSnapshots,
			containerInfoManipulators,
			postConditionManipulators
		);
	}
}
