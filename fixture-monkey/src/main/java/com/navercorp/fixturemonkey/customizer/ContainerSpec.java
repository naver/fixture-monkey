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
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.resolver.AddMapEntryNodeManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.MapNodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeFieldResolver;
import com.navercorp.fixturemonkey.resolver.NodeIndexResolver;
import com.navercorp.fixturemonkey.resolver.NodeKeyValueResolver;
import com.navercorp.fixturemonkey.resolver.NodeLastEntryResolver;
import com.navercorp.fixturemonkey.resolver.NodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeNullityManipulator;
import com.navercorp.fixturemonkey.resolver.NodeResolver;
import com.navercorp.fixturemonkey.resolver.NodeSetArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSizeManipulator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ContainerSpec {
	private final ArbitraryTraverser traverser;
	private final NodeResolver treePathResolver;
	private final List<ArbitraryManipulator> arbitraryManipulators;

	public ContainerSpec(ArbitraryTraverser traverser, NodeResolver treePathResolver) {
		this.traverser = traverser;
		this.treePathResolver = treePathResolver;
		this.arbitraryManipulators = new ArrayList<>();
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
		arbitraryManipulators.add(
			new ArbitraryManipulator(this.treePathResolver, new NodeSizeManipulator(traverser, min, max))
		);
	}

	public void key(Object key) {
		NodeManipulator manipulator = new MapNodeManipulator(
			traverser,
			Collections.singletonList(convertToNodeManipulator(key)),
			Collections.emptyList()
		);
		arbitraryManipulators.add(new ArbitraryManipulator(this.treePathResolver, manipulator));
	}

	public void key(Consumer<ContainerSpec> consumer) {
		if (consumer == null) {
			throw new IllegalArgumentException(
				"Map key cannot be null."
			);
		}

		arbitraryManipulators.add(
			new ArbitraryManipulator(this.treePathResolver, new AddMapEntryNodeManipulator(traverser))
		);

		NodeResolver nextResolver = new NodeKeyValueResolver(
			new NodeLastEntryResolver(this.treePathResolver),
			true
		);
		ContainerSpec containerSpec = new ContainerSpec(traverser, nextResolver);
		consumer.accept(containerSpec);
		arbitraryManipulators.addAll(containerSpec.arbitraryManipulators);
	}

	public void value(@Nullable Object value) {
		NodeManipulator manipulator = new MapNodeManipulator(
			traverser,
			Collections.emptyList(),
			Collections.singletonList(convertToNodeManipulator(value))
		);
		arbitraryManipulators.add(new ArbitraryManipulator(this.treePathResolver, manipulator));
	}

	public void value(Consumer<ContainerSpec> consumer) {
		if (consumer == null) {
			value((Object)null);
			return;
		}

		arbitraryManipulators.add(new ArbitraryManipulator(
			this.treePathResolver, new AddMapEntryNodeManipulator(traverser))
		);
		NodeResolver nextResolver = new NodeKeyValueResolver(
			new NodeLastEntryResolver(this.treePathResolver),
			false
		);
		ContainerSpec containerSpec = new ContainerSpec(traverser, nextResolver);
		consumer.accept(containerSpec);
		arbitraryManipulators.addAll(containerSpec.arbitraryManipulators);
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
		arbitraryManipulators.add(new ArbitraryManipulator(this.treePathResolver, mapManipulator));
	}

	public void listElement(int index, @Nullable Object value) {
		NodeResolver nextResolver = new NodeIndexResolver(this.treePathResolver, index);
		NodeManipulator manipulator = convertToNodeManipulator(value);
		arbitraryManipulators.add(new ArbitraryManipulator(nextResolver, manipulator));
	}

	public void listElement(int index, Consumer<ContainerSpec> consumer) {
		if (consumer == null) {
			listElement(index, (Object)null);
			return;
		}

		NodeResolver nextResolver = new NodeIndexResolver(this.treePathResolver, index);
		ContainerSpec containerSpec = new ContainerSpec(traverser, nextResolver);
		consumer.accept(containerSpec);
		arbitraryManipulators.addAll(containerSpec.arbitraryManipulators);
	}

	public void field(String field, @Nullable Object value) {
		NodeResolver nextResolver = new NodeFieldResolver(this.treePathResolver, field);
		NodeManipulator manipulator = convertToNodeManipulator(value);
		arbitraryManipulators.add(new ArbitraryManipulator(nextResolver, manipulator));
	}

	public void field(String field, Consumer<ContainerSpec> consumer) {
		if (consumer == null) {
			field(field, (Object)null);
			return;
		}

		NodeResolver nextResolver = new NodeFieldResolver(this.treePathResolver, field);
		ContainerSpec containerSpec = new ContainerSpec(traverser, nextResolver);
		consumer.accept(containerSpec);
		arbitraryManipulators.addAll(containerSpec.arbitraryManipulators);
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
