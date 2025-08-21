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
import com.navercorp.objectfarm.api.nodecandidate.JavaArrayElementNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.JavaFieldNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.JavaLinearContainerElementNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.JavaMapElementNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.JvmMapNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecontext.JavaContainerNodeContext;
import com.navercorp.objectfarm.api.nodecontext.JavaNodeContext;
import com.navercorp.objectfarm.api.nodepromoter.JavaNodePromoter;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ObjectTypeReference;

class JavaNodeTest {
	public static final JavaContainerNodeContext CONTAINER_CONTEXT =
		new JavaContainerNodeContext(3);

	public static final JavaNodeContext CONTEXT = new JavaNodeContext(
		-1L,
		Collections.singletonList(new JavaNodePromoter()),
		Arrays.asList(
			new JavaLinearContainerElementNodeCandidateGenerator(CONTAINER_CONTEXT),
			new JavaArrayElementNodeCandidateGenerator(CONTAINER_CONTEXT),
			new JavaMapElementNodeCandidateGenerator(CONTAINER_CONTEXT),
			new JavaFieldNodeCandidateGenerator()
		)
	);

	@Test
	void fieldNames() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(SimpleObject.class),
			"$",
			CONTEXT
		);

		// when
		List<JvmNodeCandidate> actual = node.getCandidateChildren();

		// then
		then(actual.get(0).getName()).isEqualTo("string");
		then(actual.get(1).getName()).isEqualTo("integer");
		then(actual.get(2).getName()).isEqualTo("list");
		then(actual.get(3).getName()).isEqualTo("obj");
	}

	@Test
	void list() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(ListObject.class),
			"$",
			CONTEXT
		);

		// when
		List<JvmNodeCandidate> actual = node.getChildren().get(0).getCandidateChildren();

		// then
		then(actual).hasSize(3);
		then(actual).allMatch(it -> it.getJvmType().getRawType() == String.class);
	}

	@Test
	void listSelf() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(new ObjectTypeReference<List<String>>() {
			}),
			"$",
			CONTEXT
		);

		// when
		List<JvmNodeCandidate> actual = node.getCandidateChildren();

		// then
		then(actual).hasSize(3);
		then(actual).allMatch(it -> it.getJvmType().getRawType() == String.class);
	}

	@Test
	void nestedListSelf() {
		// when
		JavaNode actual = new JavaNode(
			new JavaType(new ObjectTypeReference<List<List<String>>>() {
			}),
			"$",
			CONTEXT
		);

		// then
		then(actual.getType().getRawType()).isEqualTo(List.class);
		then(actual.getType().getTypeVariables().get(0).getRawType()).isEqualTo(List.class);
		then(actual.getType().getTypeVariables().get(0).getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
		List<JvmNodeCandidate> firstList = actual.getCandidateChildren();
		then(firstList).hasSize(3);
		then(firstList).allMatch(it -> it.getJvmType().getRawType() == List.class);
		then(firstList).allMatch(it -> it.getJvmType().getTypeVariables().get(0).getRawType() == String.class);
		List<JvmNodeCandidate> secondList = actual.getChildren().get(1).getCandidateChildren();
		then(secondList).hasSize(3);
		then(secondList).allMatch(it -> it.getJvmType().getRawType() == String.class);
	}

	@Test
	void array() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(ArrayObject.class),
			"$",
			CONTEXT
		);

		// when
		List<JvmNodeCandidate> actual = node.getChildren().get(0).getCandidateChildren();

		// then
		then(actual).hasSize(3);
		then(actual).allMatch(it -> it.getJvmType().getRawType() == String.class);
	}

	@Test
	void arraySelf() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(new ObjectTypeReference<String[]>() {
			}),
			"$",
			CONTEXT
		);

		// when
		List<JvmNodeCandidate> actual = node.getCandidateChildren();

		// then
		then(actual).hasSize(3);
		then(actual).allMatch(it -> it.getJvmType().getRawType() == String.class);
	}

	@Test
	void listWithWildcard() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(ListWildcardObject.class),
			"$",
			CONTEXT
		);

		// when
		List<JvmNodeCandidate> actual = node.getChildren().get(0).getCandidateChildren();

		// then
		then(actual).hasSize(3);
		then(actual).allMatch(it -> it.getJvmType().getRawType() == String.class);
	}

	@Test
	void listWithWildcardSelf() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(new ObjectTypeReference<List<? extends String>>() {
			}),
			"$",
			CONTEXT
		);

		// when
		List<JvmNodeCandidate> actual = node.getCandidateChildren();

		// then
		then(actual).hasSize(3);
		then(actual).allMatch(it -> it.getJvmType().getRawType() == String.class);
	}

	@Test
	void set() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(SetObject.class),
			"$",
			CONTEXT
		);

		// when
		List<JvmNodeCandidate> actual = node.getChildren().get(0).getCandidateChildren();

		// then
		then(actual).hasSize(3);
		then(actual).allMatch(it -> it.getJvmType().getRawType() == String.class);
	}

	@Test
	void map() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(MapObject.class),
			"$",
			CONTEXT
		);

		// when
		List<JvmNodeCandidate> actual = node.getChildren().get(0).getCandidateChildren();

		// then
		then(actual).hasSize(3);
		then(actual).allMatch(it -> it instanceof JvmMapNodeCandidate);
		JvmMapNodeCandidate map = (JvmMapNodeCandidate)actual.get(1);
		then(map.getKey().getJvmType().getRawType()).isEqualTo(String.class);
		then(map.getValue().getJvmType().getRawType()).isEqualTo(Integer.class);
	}

	@Test
	void mapSelf() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(new ObjectTypeReference<Map<String, Integer>>() {
			}),
			"$",
			CONTEXT
		);

		// when
		List<JvmNodeCandidate> actual = node.getCandidateChildren();

		// then
		then(actual).hasSize(3);
		then(actual).allMatch(it -> it instanceof JvmMapNodeCandidate);
		JvmMapNodeCandidate map = (JvmMapNodeCandidate)actual.get(1);
		then(map.getKey().getJvmType().getRawType()).isEqualTo(String.class);
		then(map.getValue().getJvmType().getRawType()).isEqualTo(Integer.class);
	}

	@Test
	void listMapSelf() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(new ObjectTypeReference<List<Map<String, Integer>>>() {
			}),
			"$",
			CONTEXT
		);

		// when
		List<JvmNodeCandidate> actual = node.getChildren().get(1).getCandidateChildren();

		// then
		then(actual).hasSize(3);
		JvmMapNodeCandidate map = (JvmMapNodeCandidate)actual.get(1);
		then(map.getKey().getJvmType().getRawType()).isEqualTo(String.class);
		then(map.getValue().getJvmType().getRawType()).isEqualTo(Integer.class);
	}

	@Test
	void objectList() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(ObjectListObject.class),
			"$",
			CONTEXT
		);

		// when
		JvmNode listNode = node.getChildren().get(0);
		JvmNode objectNode = listNode.getChildren().get(0);
		JvmNode stringNode = objectNode.getChildren().get(0);
		String actual = stringNode.getCandidateChildren().get(0).getName();

		// then
		then(listNode.getCandidateChildren()).hasSize(3);
		then(actual).isEqualTo("value");
	}

	@Test
	void objectListSelf() {
		// given
		JavaNode listNode = new JavaNode(
			new JavaType(new ObjectTypeReference<List<StringObject>>() {
			}),
			"$",
			CONTEXT
		);

		// when
		JvmNode objectNode = listNode.getChildren().get(0);
		JvmNode stringNode = objectNode.getChildren().get(0);
		String actual = stringNode.getCandidateChildren().get(0).getName();

		// then
		then(listNode.getCandidateChildren()).hasSize(3);
		then(actual).isEqualTo("value");
	}

	@Test
	void genericArray() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(GenericStringArrayObject.class),
			"$",
			CONTEXT
		);

		// when
		JvmType actual = node.getChildren().get(0).getType();

		// then
		then(actual.getRawType()).isEqualTo(GenericArrayObject.class);
		then(actual.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}

	@Test
	void genericImplementation() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(new ObjectTypeReference<GenericImplementation<String>>() {
			}),
			"$",
			CONTEXT
		);

		// when
		JvmType actual = node.getChildren().get(0).getType();

		// then
		then(actual.getRawType()).isEqualTo(String.class);
	}

	@Test
	void genericArrayTypeReference() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(new ObjectTypeReference<GenericArrayObject<String>>() {
			}),
			"$",
			CONTEXT
		);

		// when
		JvmType actual = node.getChildren().get(0).getType();

		// then
		then(actual.getRawType()).isEqualTo(GenericImplementation[].class);
		then(actual.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}
}
