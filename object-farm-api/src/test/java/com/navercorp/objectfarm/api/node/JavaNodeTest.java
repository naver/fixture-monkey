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

package com.navercorp.objectfarm.api.node;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.node.specs.GenericObject.GenericArrayObject;
import com.navercorp.objectfarm.api.node.specs.GenericObject.GenericImplementation;
import com.navercorp.objectfarm.api.node.specs.GenericObject.GenericStringArrayObject;
import com.navercorp.objectfarm.api.node.specs.ImmutableObject.ArrayObject;
import com.navercorp.objectfarm.api.node.specs.ImmutableObject.ListObject;
import com.navercorp.objectfarm.api.node.specs.ImmutableObject.ListWildcardObject;
import com.navercorp.objectfarm.api.node.specs.ImmutableObject.MapObject;
import com.navercorp.objectfarm.api.node.specs.ImmutableObject.ObjectListObject;
import com.navercorp.objectfarm.api.node.specs.ImmutableObject.SetObject;
import com.navercorp.objectfarm.api.node.specs.ImmutableObject.SimpleObject;
import com.navercorp.objectfarm.api.node.specs.ImmutableObject.StringObject;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTree;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.tree.JvmNodeTreeTransformer;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ObjectTypeReference;

class JavaNodeTest {
	public static final FixedContainerSizeResolver CONTAINER_SIZE_RESOLVER =
		new FixedContainerSizeResolver(3);

	public static final JvmNodePromoter PROMOTER = new JavaDefaultNodePromoter(
		Arrays.asList(
			new JavaObjectNodePromoter(),
			new JavaMapNodePromoter()
		)
	);
	public static final JavaNodeContext CONTEXT = JavaNodeContext.builder()
		.seed(-1L)
		.nodePromoters(Collections.singletonList(PROMOTER))
		.containerSizeResolver(CONTAINER_SIZE_RESOLVER)
		.build();

	private JvmNodeTree createNodeTree(JvmType type) {
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(type, CONTEXT).build();
		return new JvmNodeTreeTransformer(CONTEXT).transform(candidateTree);
	}

	@Test
	void fieldNames() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(SimpleObject.class));

		// when
		List<JvmNode> children = tree.getChildren(tree.getRootNode());

		// then
		then(children.get(0).getNodeName()).isEqualTo("string");
		then(children.get(1).getNodeName()).isEqualTo("integer");
		then(children.get(2).getNodeName()).isEqualTo("list");
		then(children.get(3).getNodeName()).isEqualTo("obj");
	}

	@Test
	void list() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(ListObject.class));

		// when
		JvmNode listField = tree.getChildren(tree.getRootNode()).get(0);
		List<JvmNode> elements = tree.getChildren(listField);

		// then
		then(elements).hasSize(3);
		then(elements).allMatch(it -> it.getConcreteType().getRawType() == String.class);
	}

	@Test
	void listSelf() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(new ObjectTypeReference<List<String>>() {
		}));

		// when
		List<JvmNode> elements = tree.getChildren(tree.getRootNode());

		// then
		then(elements).hasSize(3);
		then(elements).allMatch(it -> it.getConcreteType().getRawType() == String.class);
	}

	@Test
	void nestedListSelf() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(new ObjectTypeReference<List<List<String>>>() {
		}));
		JvmNode root = tree.getRootNode();

		// then
		then(root.getConcreteType().getRawType()).isEqualTo(List.class);
		then(root.getConcreteType().getTypeVariables().get(0).getRawType()).isEqualTo(List.class);
		then(root.getConcreteType().getTypeVariables().get(0).getTypeVariables().get(0).getRawType()).isEqualTo(
			String.class);

		List<JvmNode> firstList = tree.getChildren(root);
		then(firstList).hasSize(3);
		// Container elements are resolved to concrete types (List -> ArrayList)
		then(firstList).allMatch(it -> List.class.isAssignableFrom(it.getConcreteType().getRawType()));
		then(firstList).allMatch(it -> it.getConcreteType().getTypeVariables().get(0).getRawType() == String.class);

		List<JvmNode> secondList = tree.getChildren(firstList.get(1));
		then(secondList).hasSize(3);
		then(secondList).allMatch(it -> it.getConcreteType().getRawType() == String.class);
	}

	@Test
	void array() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(ArrayObject.class));

		// when
		JvmNode arrayField = tree.getChildren(tree.getRootNode()).get(0);
		List<JvmNode> elements = tree.getChildren(arrayField);

		// then
		then(elements).hasSize(3);
		then(elements).allMatch(it -> it.getConcreteType().getRawType() == String.class);
	}

	@Test
	void arraySelf() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(new ObjectTypeReference<String[]>() {
		}));

		// when
		List<JvmNode> elements = tree.getChildren(tree.getRootNode());

		// then
		then(elements).hasSize(3);
		then(elements).allMatch(it -> it.getConcreteType().getRawType() == String.class);
	}

	@Test
	void listWithWildcard() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(ListWildcardObject.class));

		// when
		JvmNode listField = tree.getChildren(tree.getRootNode()).get(0);
		List<JvmNode> elements = tree.getChildren(listField);

		// then
		then(elements).hasSize(3);
		then(elements).allMatch(it -> it.getConcreteType().getRawType() == String.class);
	}

	@Test
	void listWithWildcardSelf() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(new ObjectTypeReference<List<? extends String>>() {
		}));

		// when
		List<JvmNode> elements = tree.getChildren(tree.getRootNode());

		// then
		then(elements).hasSize(3);
		then(elements).allMatch(it -> it.getConcreteType().getRawType() == String.class);
	}

	@Test
	void set() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(SetObject.class));

		// when
		JvmNode setField = tree.getChildren(tree.getRootNode()).get(0);
		List<JvmNode> elements = tree.getChildren(setField);

		// then
		then(elements).hasSize(3);
		then(elements).allMatch(it -> it.getConcreteType().getRawType() == String.class);
	}

	@Test
	void map() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(MapObject.class));

		// when
		JvmNode mapField = tree.getChildren(tree.getRootNode()).get(0);
		List<JvmNode> mapEntries = tree.getChildren(mapField);

		// then
		then(mapEntries).hasSize(3);
		then(mapEntries).allMatch(it -> it instanceof JvmMapNode);

		JvmMapNode firstEntry = (JvmMapNode)mapEntries.get(0);
		then(firstEntry.getKeyNode().getConcreteType().getRawType()).isEqualTo(String.class);
		then(firstEntry.getValueNode().getConcreteType().getRawType()).isEqualTo(Integer.class);
	}

	@Test
	void mapSelf() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(new ObjectTypeReference<Map<String, Integer>>() {
		}));

		// when
		List<JvmNode> mapEntries = tree.getChildren(tree.getRootNode());

		// then
		then(mapEntries).hasSize(3);
		then(mapEntries).allMatch(it -> it instanceof JvmMapNode);

		JvmMapNode firstEntry = (JvmMapNode)mapEntries.get(0);
		then(firstEntry.getKeyNode().getConcreteType().getRawType()).isEqualTo(String.class);
		then(firstEntry.getValueNode().getConcreteType().getRawType()).isEqualTo(Integer.class);
	}

	@Test
	void listMapSelf() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(new ObjectTypeReference<List<Map<String, Integer>>>() {
		}));

		// when
		JvmNode listElement = tree.getChildren(tree.getRootNode()).get(1);
		List<JvmNode> mapEntries = tree.getChildren(listElement);

		// then
		then(mapEntries).hasSize(3);
		then(mapEntries).allMatch(it -> it instanceof JvmMapNode);

		JvmMapNode firstEntry = (JvmMapNode)mapEntries.get(0);
		then(firstEntry.getKeyNode().getConcreteType().getRawType()).isEqualTo(String.class);
		then(firstEntry.getValueNode().getConcreteType().getRawType()).isEqualTo(Integer.class);
	}

	@Test
	void objectList() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(ObjectListObject.class));

		// when
		JvmNode listNode = tree.getChildren(tree.getRootNode()).get(0);
		List<JvmNode> listElements = tree.getChildren(listNode);
		JvmNode objectNode = listElements.get(0);
		List<JvmNode> objectFields = tree.getChildren(objectNode);

		// then
		then(listElements).hasSize(3);
		then(objectFields).hasSize(1);
		then(objectFields.get(0).getNodeName()).isEqualTo("value");
		then(objectFields.get(0).getConcreteType().getRawType()).isEqualTo(String.class);
	}

	@Test
	void objectListSelf() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(new ObjectTypeReference<List<StringObject>>() {
		}));

		// when
		List<JvmNode> listElements = tree.getChildren(tree.getRootNode());
		JvmNode objectNode = listElements.get(0);
		List<JvmNode> objectFields = tree.getChildren(objectNode);

		// then
		then(listElements).hasSize(3);
		then(objectFields).hasSize(1);
		then(objectFields.get(0).getNodeName()).isEqualTo("value");
	}

	@Test
	void genericArray() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(GenericStringArrayObject.class));

		// when
		JvmType actual = tree.getChildren(tree.getRootNode()).get(0).getConcreteType();

		// then
		then(actual.getRawType()).isEqualTo(GenericArrayObject.class);
		then(actual.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}

	@Test
	void genericImplementation() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(new ObjectTypeReference<GenericImplementation<String>>() {
		}));

		// when
		JvmType actual = tree.getChildren(tree.getRootNode()).get(0).getConcreteType();

		// then
		then(actual.getRawType()).isEqualTo(String.class);
	}

	@Test
	void genericArrayTypeReference() {
		// given
		JvmNodeTree tree = createNodeTree(new JavaType(new ObjectTypeReference<GenericArrayObject<String>>() {
		}));

		// when
		JvmType actual = tree.getChildren(tree.getRootNode()).get(0).getConcreteType();

		// then
		then(actual.getRawType()).isEqualTo(GenericImplementation[].class);
		then(actual.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}
}
