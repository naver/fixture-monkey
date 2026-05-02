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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.FixedContainerSizeResolver;
import com.navercorp.objectfarm.api.node.JavaNodeContext;
import com.navercorp.objectfarm.api.node.specs.InterfaceSpecs.Interface;
import com.navercorp.objectfarm.api.node.specs.InterfaceSpecs.InterfaceWithField;
import com.navercorp.objectfarm.api.nodecandidate.JavaNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.type.JavaType;

/**
 * Tests for JvmNodeCandidateTree.
 * <p>
 * Key design principles tested:
 * <ul>
 *   <li>Container types (Collection, Map, Array) are leaf nodes in the candidate tree</li>
 *   <li>Tree structure is deterministic based only on types</li>
 *   <li>Subtree caching works correctly</li>
 * </ul>
 */
class JvmNodeCandidateTreeTest {
	public static final ContainerSizeResolver CONTAINER_SIZE_RESOLVER =
		new FixedContainerSizeResolver(2);

	public static final JavaNodeContext CONTEXT = JavaNodeContext.builder()
		.seed(-1L)
		.nodePromoters(Collections.emptyList())
		.containerSizeResolver(CONTAINER_SIZE_RESOLVER)
		.interfaceResolver(
			jvmType -> {
				if (jvmType.getRawType() == Interface.class) {
					return new JavaType(InterfaceWithField.class);
				}
				return jvmType;
			}
		)
		.build();

	@Test
	void getChildrenWithExistingParentShouldReturnChildren() {
		// given
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(new JavaType(String.class), CONTEXT).build();

		// when
		List<JvmNodeCandidate> children = tree.getChildren(tree.getRootNode());

		// then
		then(children).isNotNull();
	}

	@Test
	void constructorWithRootNodeShouldCreateTree() {
		// given & when
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(new JavaType(String.class), CONTEXT).build();

		// then
		then(tree).isNotNull();
		then(tree.size()).isGreaterThan(0);
		then(tree.contains(tree.getRootNode())).isTrue();
	}

	@Test
	void containsWithExistingNodeShouldReturnTrue() {
		// given
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(new JavaType(String.class), CONTEXT).build();

		// when & then
		then(tree.contains(tree.getRootNode())).isTrue();
	}

	@Test
	void containsWithNonExistingNodeShouldReturnFalse() {
		// given
		JavaNodeCandidate rootNode = new JavaNodeCandidate(new JavaType(String.class), "root");
		JavaNodeCandidate otherNode = new JavaNodeCandidate(new JavaType(Integer.class), "other");
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(rootNode, CONTEXT).build();

		// when & then
		then(tree.contains(otherNode)).isFalse();
	}

	@Test
	void treeWithListTypeShouldBeLeafNode() {
		// given - Container types are leaf nodes in JvmNodeCandidateTree
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(
			new JavaType(List.class, Collections.singletonList(new JavaType(String.class)), Collections.emptyList()),
			CONTEXT
		).build();

		// when
		List<JvmNodeCandidate> children = tree.getChildren(tree.getRootNode());

		// then - Container types have no children in candidate tree (children are generated at JvmNode level)
		then(children).isEmpty();
	}

	@Test
	void treeWithArrayTypeShouldBeLeafNode() {
		// given - Container types are leaf nodes in JvmNodeCandidateTree
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(new JavaType(String[].class), CONTEXT).build();

		// when
		List<JvmNodeCandidate> children = tree.getChildren(tree.getRootNode());

		// then - Container types have no children in candidate tree
		then(children).isEmpty();
	}

	@Test
	void treeWithMapTypeShouldBeLeafNode() {
		// given - Container types are leaf nodes in JvmNodeCandidateTree
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(
			new JavaType(Map.class, java.util.Arrays.asList(new JavaType(String.class), new JavaType(Integer.class)),
				Collections.emptyList()),
			CONTEXT
		).build();

		// when
		List<JvmNodeCandidate> children = tree.getChildren(tree.getRootNode());

		// then - Container types have no children in candidate tree
		then(children).isEmpty();
	}

	@Test
	void treeWithCustomClassShouldGenerateFieldCandidates() {
		// given
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(new JavaType(TestClass.class), CONTEXT).build();

		// when
		List<JvmNodeCandidate> children = tree.getChildren(tree.getRootNode());

		// then
		then(children).hasSize(3);
	}

	@Test
	void getChildrenWithNullParentShouldReturnEmptyList() {
		// given
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(new JavaType(String.class), CONTEXT).build();

		// when
		List<JvmNodeCandidate> children = tree.getChildren(null);

		// then
		then(children).isNotNull();
		then(children).isEmpty();
	}

	@Test
	void treeWithPrimitiveTypeShouldNotGenerateChildren() {
		// given
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(new JavaType(int.class), CONTEXT).build();

		// when
		List<JvmNodeCandidate> children = tree.getChildren(tree.getRootNode());

		// then
		then(children).isEmpty();
	}

	@Test
	void selfReferenceClassShouldHaveExactlyTwoNodes() {
		// given & when
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(
			new JavaType(SelfReferenceClass.class), CONTEXT
		).build();

		// then
		then(tree.size()).isEqualTo(2);

		List<JvmNodeCandidate> rootChildren = tree.getChildren(tree.getRootNode());
		then(rootChildren).hasSize(1);
		then(rootChildren.get(0).getName()).isEqualTo("self");

		List<JvmNodeCandidate> selfChildren = tree.getChildren(rootChildren.get(0));
		then(selfChildren).isEmpty();
	}

	@Test
	void mutualReferenceClassesShouldDetectCycle() {
		// given & when
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(
			new JavaType(ClassA.class), CONTEXT
		).build();

		// then
		then(tree.size()).isEqualTo(3);

		List<JvmNodeCandidate> rootChildren = tree.getChildren(tree.getRootNode());
		then(rootChildren).hasSize(1);
		then(rootChildren.get(0).getName()).isEqualTo("classB");
		then(rootChildren.get(0).getType().getRawType()).isEqualTo(ClassB.class);

		List<JvmNodeCandidate> classBChildren = tree.getChildren(rootChildren.get(0));
		then(classBChildren).hasSize(1);
		then(classBChildren.get(0).getName()).isEqualTo("classA");
		then(classBChildren.get(0).getType().getRawType()).isEqualTo(ClassA.class);

		List<JvmNodeCandidate> classAChildren = tree.getChildren(classBChildren.get(0));
		then(classAChildren).isEmpty();
	}

	@Test
	void threeWayCircularReferenceShouldDetectCycle() {
		// given & when
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(
			new JavaType(ClassX.class), CONTEXT
		).build();

		// then
		then(tree.size()).isEqualTo(4);
	}

	@Test
	void treeWithTreeContextShouldCacheSubtree() {
		// given
		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();
		JavaType userType = new JavaType(TestUser.class);

		// when
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(userType, CONTEXT)
			.withTreeContext(treeContext)
			.build();

		// then
		then(treeContext.getCacheSize()).isGreaterThan(0);
	}

	@Test
	void secondTreeWithSameTypeShouldUseCachedSubtree() {
		// given
		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();
		JavaType addressType = new JavaType(TestAddress.class);

		new JvmNodeCandidateTree.Builder(addressType, CONTEXT)
			.withTreeContext(treeContext)
			.build();

		int cacheSizeAfterFirstTree = treeContext.getCacheSize();

		// when
		new JvmNodeCandidateTree.Builder(addressType, CONTEXT)
			.withTreeContext(treeContext)
			.build();

		// then
		then(treeContext.getCacheSize()).isEqualTo(cacheSizeAfterFirstTree);
	}

	@Test
	void isCachedShouldReturnTrueForCachedType() {
		// given
		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();
		JavaType addressType = new JavaType(TestAddress.class);

		// when
		new JvmNodeCandidateTree.Builder(new JavaType(TestUser.class), CONTEXT)
			.withTreeContext(treeContext)
			.build();

		// then - TestAddress should be cached as a subtree type
		then(treeContext.isCached(addressType)).isTrue();
	}

	@Test
	void clearCacheShouldRemoveAllCachedSubtrees() {
		// given
		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();

		new JvmNodeCandidateTree.Builder(new JavaType(TestUser.class), CONTEXT)
			.withTreeContext(treeContext)
			.build();

		then(treeContext.getCacheSize()).isGreaterThan(0);

		// when
		treeContext.clearCache();

		// then
		then(treeContext.getCacheSize()).isEqualTo(0);
	}

	@Test
	void sameTypeProducesSameDeterministicTree() {
		// given - Trees for the same type should be identical
		JavaType userType = new JavaType(TestUser.class);

		// when
		JvmNodeCandidateTree tree1 = new JvmNodeCandidateTree.Builder(userType, CONTEXT).build();
		JvmNodeCandidateTree tree2 = new JvmNodeCandidateTree.Builder(userType, CONTEXT).build();

		// then
		then(tree1.size()).isEqualTo(tree2.size());
	}

	@Test
	void preBuildResolvedTypesShouldCacheContainerElementTypes() {
		// given
		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();
		JavaType listOfAddressType = new JavaType(
			List.class,
			Collections.singletonList(new JavaType(TestAddress.class)),
			Collections.emptyList()
		);

		// when - build with pre-build enabled
		new JvmNodeCandidateTree.Builder(listOfAddressType, CONTEXT)
			.withTreeContext(treeContext)
			.withPreBuildResolvedTypes(true)
			.build();

		// then - TestAddress should be pre-built and cached
		then(treeContext.isCached(new JavaType(TestAddress.class))).isTrue();
	}

	@Test
	void preBuildResolvedTypesShouldCacheInterfaceImplementationTypes() {
		// given
		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();
		JavaType interfaceType = new JavaType(Interface.class);

		// when - build with pre-build enabled
		new JvmNodeCandidateTree.Builder(interfaceType, CONTEXT)
			.withTreeContext(treeContext)
			.withPreBuildResolvedTypes(true)
			.build();

		// then - InterfaceWithField (the implementation) should be pre-built and cached
		then(treeContext.isCached(new JavaType(InterfaceWithField.class))).isTrue();
	}

	@Test
	void preBuildResolvedTypesDisabledShouldNotCacheElementTypes() {
		// given
		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();
		JavaType listOfAddressType = new JavaType(
			List.class,
			Collections.singletonList(new JavaType(TestAddress.class)),
			Collections.emptyList()
		);

		// when - build with pre-build disabled (default)
		new JvmNodeCandidateTree.Builder(listOfAddressType, CONTEXT)
			.withTreeContext(treeContext)
			.withPreBuildResolvedTypes(false)
			.build();

		// then - TestAddress should NOT be cached
		then(treeContext.isCached(new JavaType(TestAddress.class))).isFalse();
	}

	@Test
	void preBuildResolvedTypesWithMapShouldCacheKeyAndValueTypes() {
		// given
		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();
		JavaType mapType = new JavaType(
			Map.class,
			java.util.Arrays.asList(new JavaType(TestAddress.class), new JavaType(TestUser.class)),
			Collections.emptyList()
		);

		// when - build with pre-build enabled
		new JvmNodeCandidateTree.Builder(mapType, CONTEXT)
			.withTreeContext(treeContext)
			.withPreBuildResolvedTypes(true)
			.build();

		// then - Both key and value types should be pre-built and cached
		then(treeContext.isCached(new JavaType(TestAddress.class))).isTrue();
		then(treeContext.isCached(new JavaType(TestUser.class))).isTrue();
	}

	@Test
	void preBuildResolvedTypesWithNestedContainerShouldCacheRecursively() {
		// given
		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();
		// List<ClassWithListField> where ClassWithListField has List<TestAddress>
		JavaType listType = new JavaType(
			List.class,
			Collections.singletonList(new JavaType(ClassWithListField.class)),
			Collections.emptyList()
		);

		// when - build with pre-build enabled
		new JvmNodeCandidateTree.Builder(listType, CONTEXT)
			.withTreeContext(treeContext)
			.withPreBuildResolvedTypes(true)
			.build();

		// then - ClassWithListField should be cached, and TestAddress within it should also be cached
		then(treeContext.isCached(new JavaType(ClassWithListField.class))).isTrue();
		then(treeContext.isCached(new JavaType(TestAddress.class))).isTrue();
	}

	@Test
	void preBuildResolvedTypesWithoutTreeContextShouldNotFail() {
		// given
		JavaType listOfAddressType = new JavaType(
			List.class,
			Collections.singletonList(new JavaType(TestAddress.class)),
			Collections.emptyList()
		);

		// when & then - should not throw even without tree context
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(listOfAddressType, CONTEXT)
			.withPreBuildResolvedTypes(true)
			.build();

		then(tree).isNotNull();
	}

	@SuppressWarnings("unused")
	private static class TestClass {
		private String name;
		private int value;
		private List<String> items;
	}

	@SuppressWarnings("unused")
	private static class SelfReferenceClass {
		private SelfReferenceClass self;
	}

	@SuppressWarnings("unused")
	private static class ClassA {
		private ClassB classB;
	}

	@SuppressWarnings("unused")
	private static class ClassB {
		private ClassA classA;
	}

	@SuppressWarnings("unused")
	private static class ClassX {
		private ClassY classY;
	}

	@SuppressWarnings("unused")
	private static class ClassY {
		private ClassZ classZ;
	}

	@SuppressWarnings("unused")
	private static class ClassZ {
		private ClassX classX;
	}

	static class TestUser {
		private String name;
		private String email;
		private TestAddress address;
		private List<String> tags;

		public String getName() {
			return name;
		}

		public String getEmail() {
			return email;
		}

		public TestAddress getAddress() {
			return address;
		}

		public List<String> getTags() {
			return tags;
		}
	}

	static class TestAddress {
		private String city;
		private String street;

		public String getCity() {
			return city;
		}

		public String getStreet() {
			return street;
		}
	}

	@SuppressWarnings("unused")
	static class ClassWithListField {
		private String name;
		private List<TestAddress> addresses;
	}
}
