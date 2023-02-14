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
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.ContainerInfoHolder;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.FilterHolder;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.NodeResolverObjectHolder;
import com.navercorp.fixturemonkey.resolver.CompositeNodeResolver;
import com.navercorp.fixturemonkey.resolver.ContainerElementPredicate;
import com.navercorp.fixturemonkey.resolver.DefaultNodeResolver;
import com.navercorp.fixturemonkey.resolver.IdentityNodeResolver;
import com.navercorp.fixturemonkey.resolver.NodeAllElementPredicate;
import com.navercorp.fixturemonkey.resolver.NodeElementPredicate;
import com.navercorp.fixturemonkey.resolver.NodeKeyPredicate;
import com.navercorp.fixturemonkey.resolver.NodeResolver;
import com.navercorp.fixturemonkey.resolver.NodeValuePredicate;
import com.navercorp.fixturemonkey.resolver.PropertyNameNodePredicate;

@SuppressWarnings("UnusedReturnValue")
@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class InnerSpec {
	private final NodeResolver treePathResolver;
	private final InnerSpecState state;
	private final List<InnerSpec> innerSpecs;

	private int entrySize = 0;
	private int manipulateSize = 0;

	public InnerSpec() {
		this(IdentityNodeResolver.INSTANCE, new InnerSpecState(), new ArrayList<>());
	}

	private InnerSpec(NodeResolver treePathResolver, InnerSpecState state, List<InnerSpec> innerSpecs) {
		this.treePathResolver = treePathResolver;
		this.state = state;
		this.innerSpecs = innerSpecs;
	}

	public InnerSpec inner(InnerSpec innerSpec) {
		InnerSpec appendInnerSpec = newAppendNodeResolver(innerSpec, this.treePathResolver);
		this.innerSpecs.add(appendInnerSpec);
		return this;
	}

	public InnerSpec size(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("should be min > max, min : " + min + " max : " + max);
		}

		this.state.setContainerInfoHolder(new ContainerInfoHolder(manipulateSize++, this.treePathResolver, min, max));
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
		setMapKey(key);
		return this;
	}

	public InnerSpec key(Consumer<InnerSpec> consumer) {
		entrySize++;
		setMapKey(consumer);
		return this;
	}

	public InnerSpec value(Object value) {
		entrySize++;
		setMapValue(value);
		return this;
	}

	public InnerSpec value(Consumer<InnerSpec> consumer) {
		entrySize++;
		setMapValue(consumer);
		return this;
	}

	public InnerSpec entry(Object key, @Nullable Object value) {
		entrySize++;
		setMapEntry(key, value);
		return this;
	}

	public InnerSpec entry(Object key, Consumer<InnerSpec> consumer) {
		entrySize++;
		setMapEntry(key, consumer);
		return this;
	}

	public InnerSpec entry(Consumer<InnerSpec> consumer, @Nullable Object mapValue) {
		entrySize++;
		setMapEntry(consumer, mapValue);
		return this;
	}

	public InnerSpec keyLazy(Supplier<?> supplier) {
		entrySize++;
		setMapKey(supplier);
		return this;
	}

	public InnerSpec valueLazy(Supplier<?> supplier) {
		entrySize++;
		setMapValue(supplier);
		return this;
	}

	public InnerSpec entryLazy(Supplier<?> keySupplier, Supplier<?> valueSupplier) {
		entrySize++;

		LazyArbitrary<?> keyLazyArbitrary = LazyArbitrary.lazy(keySupplier);
		LazyArbitrary<?> valueLazyArbitrary = LazyArbitrary.lazy(valueSupplier);

		setMapEntry(keyLazyArbitrary, valueLazyArbitrary);
		return this;
	}

	public InnerSpec allKey(Consumer<InnerSpec> consumer) {
		setMapAllKey(consumer);
		return this;
	}

	public InnerSpec allKeyLazy(Supplier<?> supplier) {
		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		setMapAllKey(lazyArbitrary);
		return this;
	}

	public InnerSpec allValue(@Nullable Object value) {
		setMapAllValue(value);
		return this;
	}

	public InnerSpec allValue(Consumer<InnerSpec> consumer) {
		setMapAllValue(consumer);
		return this;
	}

	public InnerSpec allValueLazy(Supplier<?> supplier) {
		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		setMapAllValue(lazyArbitrary);
		return this;
	}

	public InnerSpec allEntry(Supplier<?> keySupplier, Object value) {
		LazyArbitrary<?> keyLazyArbitrary = LazyArbitrary.lazy(keySupplier);

		setMapAllKey(keyLazyArbitrary);
		setMapAllValue(value);
		return this;
	}

	public InnerSpec allEntryLazy(Supplier<?> keySupplier, Supplier<?> valueSupplier) {
		return this.allEntry(keySupplier, valueSupplier);
	}

	public InnerSpec listElement(int index, @Nullable Object value) {
		setListElement(index, value);
		return this;
	}

	public InnerSpec listElement(int index, Consumer<InnerSpec> consumer) {
		setListElement(index, consumer);
		return this;
	}

	public InnerSpec allListElement(@Nullable Object value) {
		return listElement(NO_OR_ALL_INDEX_INTEGER_VALUE, value);
	}

	public InnerSpec allListElement(Consumer<InnerSpec> consumer) {
		return listElement(NO_OR_ALL_INDEX_INTEGER_VALUE, consumer);
	}

	public InnerSpec property(String propertyName, @Nullable Object value) {
		setPropertyValue(propertyName, value);
		return this;
	}

	public InnerSpec property(String propertyName, Consumer<InnerSpec> consumer) {
		setPropertyValue(propertyName, consumer);
		return this;
	}

	public <T> InnerSpec postCondition(Class<T> type, Predicate<T> filter) {
		this.state.setFilterHolder(new FilterHolder(manipulateSize++, this.treePathResolver, type, filter));
		return this;
	}

	public ManipulatorSet getMergedManipulatorSet() {
		return traverse(this);
	}

	private InnerSpec newAppendNodeResolver(InnerSpec innerSpec, NodeResolver nodeResolver) {
		InnerSpec newSpec = new InnerSpec(
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
		if (nextValue instanceof InnerSpec) {
			InnerSpec prefix = new InnerSpec(nextNodeResolver, new InnerSpecState(), new ArrayList<>());
			this.innerSpecs.add(prefix.inner((InnerSpec)nextValue));
			return;
		}

		if (nextValue instanceof Consumer) {
			Consumer<InnerSpec> consumer = (Consumer<InnerSpec>)nextValue;
			InnerSpec mapInnerSpec = new InnerSpec(nextNodeResolver, new InnerSpecState(), new ArrayList<>());
			consumer.accept(mapInnerSpec);
			this.innerSpecs.add(mapInnerSpec);
			return;
		}

		InnerSpec nextInnerSpec = new InnerSpec(nextNodeResolver, new InnerSpecState(), new ArrayList<>());
		nextInnerSpec.state.setObjectHolder(
			new NodeResolverObjectHolder(
				manipulateSize++,
				nextNodeResolver,
				nextValue
			)
		);
		this.innerSpecs.add(nextInnerSpec);
	}

	private ManipulatorSet traverse(InnerSpec innerSpec) {
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
			nodeResolverObjectHolders.addAll(traverse(spec).getNodeResolverObjectHolders());
			containerInfoManipulators.addAll(traverse(spec).getContainerInfoManipulators());
			postConditionManipulators.addAll(traverse(spec).getPostConditionManipulators());
		}

		return new ManipulatorSet(nodeResolverObjectHolders, containerInfoManipulators, postConditionManipulators);
	}
}
