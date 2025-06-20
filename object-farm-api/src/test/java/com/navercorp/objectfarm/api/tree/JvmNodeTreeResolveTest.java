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

package com.navercorp.objectfarm.api.tree;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.FixedContainerSizeResolver;
import com.navercorp.objectfarm.api.node.JavaDefaultNodePromoter;
import com.navercorp.objectfarm.api.node.JavaMapNodePromoter;
import com.navercorp.objectfarm.api.node.JavaNodeContext;
import com.navercorp.objectfarm.api.node.JavaObjectNodePromoter;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.node.JvmNodePromoter;
import com.navercorp.objectfarm.api.type.JavaType;

/**
 * Tests for JvmNodeTree.resolve() and JvmNodeTree.getPath() methods.
 */
class JvmNodeTreeResolveTest {
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

	@Nested
	@DisplayName("resolve(PathExpression)")
	class ResolveTests {

		@Test
		@DisplayName("should return root node for root path")
		void resolveRoot() {
			// given
			JvmNodeTree tree = createTree(new JavaType(String.class));

			// when
			JvmNode resolved = tree.resolve(PathExpression.root());

			// then
			then(resolved).isEqualTo(tree.getRootNode());
		}

		@Test
		@DisplayName("should resolve field by name")
		void resolveFieldByName() {
			// given
			JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));

			// when
			JvmNode nameNode = tree.resolve(PathExpression.of("$.name"));
			JvmNode valueNode = tree.resolve(PathExpression.of("$.value"));

			// then
			then(nameNode).isNotNull();
			then(nameNode.getNodeName()).isEqualTo("name");
			then(nameNode.getConcreteType().getRawType()).isEqualTo(String.class);

			then(valueNode).isNotNull();
			then(valueNode.getNodeName()).isEqualTo("value");
			then(valueNode.getConcreteType().getRawType()).isEqualTo(int.class);
		}

		@Test
		@DisplayName("should resolve list element by index")
		void resolveListElementByIndex() {
			// given
			JvmNodeTree tree = createTree(new JavaType(ListContainer.class));

			// when
			JvmNode firstItem = tree.resolve(PathExpression.of("$.items[0]"));
			JvmNode secondItem = tree.resolve(PathExpression.of("$.items[1]"));

			// then
			then(firstItem).isNotNull();
			then(firstItem.getIndex()).isEqualTo(0);
			then(firstItem.getConcreteType().getRawType()).isEqualTo(String.class);

			then(secondItem).isNotNull();
			then(secondItem.getIndex()).isEqualTo(1);
		}

		@Test
		@DisplayName("should resolve nested path")
		void resolveNestedPath() {
			// given
			JvmNodeTree tree = createTree(new JavaType(NestedContainer.class));

			// when
			JvmNode nestedName = tree.resolve(PathExpression.of("$.nested.name"));

			// then
			then(nestedName).isNotNull();
			then(nestedName.getNodeName()).isEqualTo("name");
		}

		@Test
		@DisplayName("should resolve map key and value")
		void resolveMapKeyValue() {
			// given
			JvmNodeTree tree = createTree(new JavaType(Map.class,
				Arrays.asList(new JavaType(String.class), new JavaType(Integer.class)),
				Collections.emptyList()));

			// when
			JvmNode keyNode = tree.resolve(PathExpression.of("$[0][key]"));
			JvmNode valueNode = tree.resolve(PathExpression.of("$[0][value]"));

			// then
			then(keyNode).isNotNull();
			then(keyNode.getConcreteType().getRawType()).isEqualTo(String.class);

			then(valueNode).isNotNull();
			then(valueNode.getConcreteType().getRawType()).isEqualTo(Integer.class);
		}

		@Test
		@DisplayName("should return null for non-existent path")
		void resolveNonExistentPath() {
			// given
			JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));

			// when
			JvmNode result = tree.resolve(PathExpression.of("$.nonexistent"));

			// then
			then(result).isNull();
		}

		@Test
		@DisplayName("should return null for out of range index")
		void resolveOutOfRangeIndex() {
			// given
			JvmNodeTree tree = createTree(new JavaType(ListContainer.class));

			// when
			JvmNode result = tree.resolve(PathExpression.of("$.items[999]"));

			// then
			then(result).isNull();
		}

		@Test
		@DisplayName("should resolve using string path")
		void resolveWithStringPath() {
			// given
			JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));

			// when
			JvmNode nameNode = tree.resolve("$.name");

			// then
			then(nameNode).isNotNull();
			then(nameNode.getNodeName()).isEqualTo("name");
		}
	}

	@Nested
	@DisplayName("getPath(JvmNode)")
	class GetPathTests {

		@Test
		@DisplayName("should return root path for root node")
		void getPathForRoot() {
			// given
			JvmNodeTree tree = createTree(new JavaType(String.class));

			// when
			PathExpression path = tree.getPath(tree.getRootNode());

			// then
			then(path).isNotNull();
			then(path.isRoot()).isTrue();
			then(path.toExpression()).isEqualTo("$");
		}

		@Test
		@DisplayName("should return correct path for field node")
		void getPathForField() {
			// given
			JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
			JvmNode nameNode = tree.resolve("$.name");

			// when
			PathExpression path = tree.getPath(nameNode);

			// then
			then(path).isNotNull();
			then(path.toExpression()).isEqualTo("$.name");
		}

		@Test
		@DisplayName("should return correct path for list element")
		void getPathForListElement() {
			// given
			JvmNodeTree tree = createTree(new JavaType(ListContainer.class));
			JvmNode firstItem = tree.resolve("$.items[0]");
			JvmNode secondItem = tree.resolve("$.items[1]");

			// when
			PathExpression path0 = tree.getPath(firstItem);
			PathExpression path1 = tree.getPath(secondItem);

			// then
			then(path0).isNotNull();
			then(path0.toExpression()).isEqualTo("$.items[0]");

			then(path1).isNotNull();
			then(path1.toExpression()).isEqualTo("$.items[1]");
		}

		@Test
		@DisplayName("should return correct path for nested node")
		void getPathForNestedNode() {
			// given
			JvmNodeTree tree = createTree(new JavaType(NestedContainer.class));
			JvmNode nestedName = tree.resolve("$.nested.name");

			// when
			PathExpression path = tree.getPath(nestedName);

			// then
			then(path).isNotNull();
			then(path.toExpression()).isEqualTo("$.nested.name");
		}

		@Test
		@DisplayName("should return correct path for map key and value")
		void getPathForMapKeyValue() {
			// given
			JvmNodeTree tree = createTree(new JavaType(Map.class,
				Arrays.asList(new JavaType(String.class), new JavaType(Integer.class)),
				Collections.emptyList()));
			JvmNode keyNode = tree.resolve("$[0][key]");
			JvmNode valueNode = tree.resolve("$[0][value]");

			// when
			PathExpression keyPath = tree.getPath(keyNode);
			PathExpression valuePath = tree.getPath(valueNode);

			// then
			then(keyPath).isNotNull();
			then(keyPath.toExpression()).isEqualTo("$[0][key]");

			then(valuePath).isNotNull();
			then(valuePath.toExpression()).isEqualTo("$[0][value]");
		}

		@Test
		@DisplayName("should return null for node not in tree")
		void getPathForExternalNode() {
			// given
			JvmNodeTree tree = createTree(new JavaType(String.class));
			JvmNodeTree otherTree = createTree(new JavaType(Integer.class));

			// when
			PathExpression path = tree.getPath(otherTree.getRootNode());

			// then
			then(path).isNull();
		}
	}

	@Nested
	@DisplayName("getParent(JvmNode)")
	class GetParentTests {

		@Test
		@DisplayName("should return null for root node")
		void getParentForRoot() {
			// given
			JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));

			// when
			JvmNode parent = tree.getParent(tree.getRootNode());

			// then
			then(parent).isNull();
		}

		@Test
		@DisplayName("should return correct parent for field node")
		void getParentForField() {
			// given
			JvmNodeTree tree = createTree(new JavaType(SimpleObject.class));
			JvmNode nameNode = tree.resolve("$.name");

			// when
			JvmNode parent = tree.getParent(nameNode);

			// then
			then(parent).isEqualTo(tree.getRootNode());
		}

		@Test
		@DisplayName("should return correct parent for nested node")
		void getParentForNestedNode() {
			// given
			JvmNodeTree tree = createTree(new JavaType(NestedContainer.class));
			JvmNode nestedNode = tree.resolve("$.nested");
			JvmNode nestedName = tree.resolve("$.nested.name");

			// when
			JvmNode parent = tree.getParent(nestedName);

			// then
			then(parent).isEqualTo(nestedNode);
		}
	}

	@Nested
	@DisplayName("Round-trip: resolve -> getPath")
	class RoundTripTests {

		@Test
		@DisplayName("resolve(getPath(node)) should return same node")
		void roundTripFromNode() {
			// given
			JvmNodeTree tree = createTree(new JavaType(NestedContainer.class));
			JvmNode nestedName = tree.resolve("$.nested.name");

			// when
			PathExpression path = tree.getPath(nestedName);
			JvmNode resolved = tree.resolve(path);

			// then
			then(resolved).isSameAs(nestedName);
		}

		@Test
		@DisplayName("getPath(resolve(path)) should return equivalent path")
		void roundTripFromPath() {
			// given
			JvmNodeTree tree = createTree(new JavaType(NestedContainer.class));
			PathExpression originalPath = PathExpression.of("$.nested.name");

			// when
			JvmNode resolved = tree.resolve(originalPath);
			PathExpression resultPath = tree.getPath(resolved);

			// then
			then(resultPath).isEqualTo(originalPath);
		}
	}

	// Helper methods

	private JvmNodeTree createTree(JavaType rootType) {
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(rootType, CONTEXT).build();
		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);
		return transformer.transform(candidateTree);
	}

	// Test classes

	@SuppressWarnings("unused")
	private static class SimpleObject {
		private String name;
		private int value;
	}

	@SuppressWarnings("unused")
	private static class ListContainer {
		private List<String> items;
	}

	@SuppressWarnings("unused")
	private static class NestedContainer {
		private SimpleObject nested;
	}
}
