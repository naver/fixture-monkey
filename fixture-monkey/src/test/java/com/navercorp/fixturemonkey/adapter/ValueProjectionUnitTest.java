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

package com.navercorp.fixturemonkey.adapter;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.adapter.projection.ValueProjection;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.FixedContainerSizeResolver;
import com.navercorp.objectfarm.api.node.JavaDefaultNodePromoter;
import com.navercorp.objectfarm.api.node.JavaMapNodePromoter;
import com.navercorp.objectfarm.api.node.JavaNodeContext;
import com.navercorp.objectfarm.api.node.JavaObjectNodePromoter;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.node.JvmNodePromoter;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTree;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.tree.JvmNodeTreeTransformer;
import com.navercorp.objectfarm.api.type.JavaType;

/**
 * Tests for ValueProjection and ValueProjectionBuilder.
 */
class ValueProjectionUnitTest {

	private static final ContainerSizeResolver FIXED_SIZE_RESOLVER = new FixedContainerSizeResolver(2);

	private static final List<JvmNodePromoter> PROMOTERS = Arrays.asList(
		new JavaMapNodePromoter(),
		new JavaObjectNodePromoter()
	);

	private static final JavaNodeContext CONTEXT = JavaNodeContext.builder()
		.seed(-1L)
		.nodePromoters(Collections.singletonList(new JavaDefaultNodePromoter(PROMOTERS)))
		.containerSizeResolver(FIXED_SIZE_RESOLVER)
		.build();

	@Property(tries = 1)
	void buildWithPut() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");
		JvmNode valueNode = tree.resolve("$.value");

		// when
		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, "John").put(valueNode, 42).build();

		// then
		then(projection.get(nameNode)).isEqualTo("John");
		then(projection.get(valueNode)).isEqualTo(42);
		then(projection.size()).isEqualTo(2);
	}

	@Property(tries = 1)
	void buildWithPutByPathExpression() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");
		JvmNode valueNode = tree.resolve("$.value");

		// when
		ValueProjection projection = ValueProjection.builder(tree)
			.putByPath(PathExpression.of("$.name"), "Jane")
			.putByPath(PathExpression.of("$.value"), 100)
			.build();

		// then
		then(projection.get(nameNode)).isEqualTo("Jane");
		then(projection.get(valueNode)).isEqualTo(100);
	}

	@Property(tries = 1)
	void buildWithPutByPathString() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");
		JvmNode valueNode = tree.resolve("$.value");

		// when
		ValueProjection projection = ValueProjection.builder(tree)
			.putByPath("$.name", "Bob")
			.putByPath("$.value", 200)
			.build();

		// then
		then(projection.get(nameNode)).isEqualTo("Bob");
		then(projection.get(valueNode)).isEqualTo(200);
	}

	@Property(tries = 1)
	void putByPathWithUnresolvablePathStoresIt() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));

		// when
		ValueProjection projection = ValueProjection.builder(tree).putByPath("$.nonexistent", "value").build();

		// then
		then(projection.size()).isEqualTo(1);
		then(projection.getByPath("$.nonexistent")).isEqualTo("value");
		then(projection.getUnresolvedNonWildcardPaths()).containsExactly("$.nonexistent");
	}

	@Property(tries = 1)
	void allowNullValues() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");

		// when
		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, null).build();

		// then
		then(projection.containsNode(nameNode)).isTrue();
		then(projection.get(nameNode)).isNull();
	}

	@Property(tries = 1)
	void getExistingNode() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");

		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, "Test").build();

		// when
		Object value = projection.get(nameNode);

		// then
		then(value).isEqualTo("Test");
	}

	@Property(tries = 1)
	void getNonExistingNode() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");
		JvmNode valueNode = tree.resolve("$.value");

		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, "Test").build();

		// when
		Object value = projection.get(valueNode);

		// then
		then(value).isNull();
	}

	@Property(tries = 1)
	void getByPathExpression() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");

		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, "PathTest").build();

		// when
		Object value = projection.getByPath(PathExpression.of("$.name"));

		// then
		then(value).isEqualTo("PathTest");
	}

	@Property(tries = 1)
	void getByPathString() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");

		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, "StringPathTest").build();

		// when
		Object value = projection.getByPath("$.name");

		// then
		then(value).isEqualTo("StringPathTest");
	}

	@Property(tries = 1)
	void getByInvalidPath() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");

		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, "Test").build();

		// when
		Object value = projection.getByPath("$.nonexistent");

		// then
		then(value).isNull();
	}

	@Property(tries = 1)
	void forEachIteratesAll() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");
		JvmNode valueNode = tree.resolve("$.value");

		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, "Name").put(valueNode, 42).build();

		AtomicInteger count = new AtomicInteger(0);

		// when
		projection.forEach((node, value) -> count.incrementAndGet());

		// then
		then(count.get()).isEqualTo(2);
	}

	@Property(tries = 1)
	void filterByPredicate() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");
		JvmNode valueNode = tree.resolve("$.value");

		ValueProjection projection = ValueProjection.builder(tree)
			.put(nameNode, "Name")
			.put(valueNode, null)
			.build();

		// when
		List<JvmNode> nonNullNodes = projection.filter((node, value) -> value != null);

		// then
		then(nonNullNodes).hasSize(1);
		then(nonNullNodes).contains(nameNode);
	}

	@Property(tries = 1)
	void filterNoMatch() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");

		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, "Name").build();

		// when
		List<JvmNode> result = projection.filter((node, value) -> value == null);

		// then
		then(result).isEmpty();
	}

	@Property(tries = 1)
	void sizeReturnsCount() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");
		JvmNode valueNode = tree.resolve("$.value");

		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, "a").put(valueNode, "b").build();

		// then
		then(projection.size()).isEqualTo(2);
	}

	@Property(tries = 1)
	void isEmptyForEmpty() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));

		ValueProjection projection = ValueProjection.builder(tree).build();

		// then
		then(projection.isEmpty()).isTrue();
	}

	@Property(tries = 1)
	void isEmptyForNonEmpty() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");

		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, "test").build();

		// then
		then(projection.isEmpty()).isFalse();
	}

	@Property(tries = 1)
	void containsNodeForExisting() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");

		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, "test").build();

		// then
		then(projection.containsNode(nameNode)).isTrue();
	}

	@Property(tries = 1)
	void containsNodeForNonExisting() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");
		JvmNode valueNode = tree.resolve("$.value");

		ValueProjection projection = ValueProjection.builder(tree).put(nameNode, "test").build();

		// then
		then(projection.containsNode(valueNode)).isFalse();
	}

	@Property(tries = 1)
	void fromStringPathMap() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");
		JvmNode valueNode = tree.resolve("$.value");

		Map<String, Object> valuesByPath = new HashMap<>();
		valuesByPath.put("$.name", "FromStringMap");
		valuesByPath.put("$.value", 888);

		// when
		ValueProjection projection = ValueProjection.of(tree, valuesByPath);

		// then
		then(projection.get(nameNode)).isEqualTo("FromStringMap");
		then(projection.get(valueNode)).isEqualTo(888);
	}

	@Property(tries = 1)
	void fromStringPathMapKeepsUnresolvablePaths() {
		// given
		JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
		JvmNode nameNode = tree.resolve("$.name");

		Map<String, Object> valuesByPath = new HashMap<>();
		valuesByPath.put("$.name", "Valid");
		valuesByPath.put("$.invalid", "Invalid");

		// when
		ValueProjection projection = ValueProjection.of(tree, valuesByPath);

		// then
		then(projection.size()).isEqualTo(2);
		then(projection.get(nameNode)).isEqualTo("Valid");
		then(projection.getByPath("$.invalid")).isEqualTo("Invalid");
		then(projection.getUnresolvedNonWildcardPaths()).containsExactly("$.invalid");
	}

	@Property(tries = 1)
	void listElementPaths() {
		// given
		JvmNodeTree listTree = createTree(new JavaType(ListContainer.class));
		JvmNode firstItem = listTree.resolve("$.items[0]");
		JvmNode secondItem = listTree.resolve("$.items[1]");

		// when
		ValueProjection projection = ValueProjection.builder(listTree)
			.put(firstItem, "First")
			.put(secondItem, "Second")
			.build();

		// then
		then(projection.getByPath("$.items[0]")).isEqualTo("First");
		then(projection.getByPath("$.items[1]")).isEqualTo("Second");
	}

	private static JvmNodeTree createTree(JavaType rootType) {
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(rootType, CONTEXT).build();
		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);
		return transformer.transform(candidateTree);
	}

	@SuppressWarnings("unused")
	private static class SimpleObject {

		private String name;
		private int value;
	}

	@SuppressWarnings("unused")
	private static class ListContainer {

		private List<String> items;
	}
}
