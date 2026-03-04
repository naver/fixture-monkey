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

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.node.specs.InterfaceSpecs.Interface;
import com.navercorp.objectfarm.api.node.specs.InterfaceSpecs.InterfaceImplementation;
import com.navercorp.objectfarm.api.node.specs.InterfaceSpecs.InterfaceObject;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.Types;

class JavaNodePromoterTest {
	public static final FixedContainerSizeResolver CONTAINER_SIZE_RESOLVER =
		new FixedContainerSizeResolver(3);

	public static final JavaNodeContext CONTEXT = JavaNodeContext.builder()
		.seed(-1L)
		.nodePromoters(Collections.singletonList(new JavaDefaultNodePromoter(
			Arrays.asList(
				new JavaObjectNodePromoter(),
				new JavaMapNodePromoter()
			)
		)))
		.containerSizeResolver(CONTAINER_SIZE_RESOLVER)
		.build();

	@Test
	void promoteInterface() {
		// given - Create a context with an interface resolver
		JavaNodeContext contextWithResolver = JavaNodeContext.builder()
			.seed(-1L)
			.nodePromoters(Collections.singletonList(new JavaDefaultNodePromoter(
				Arrays.asList(
					new JavaObjectNodePromoter(),
					new JavaMapNodePromoter()
				)
			)))
			.containerSizeResolver(CONTAINER_SIZE_RESOLVER)
			.interfaceResolver(type -> {
				if (Types.isAssignable(Interface.class, type.getRawType())) {
					return new JavaType(InterfaceImplementation.class);
				}
				return null;
			})
			.build();

		JavaNode node = new JavaNode(
			new JavaType(InterfaceObject.class),
			"$"
		);

		JvmNodePromoter promoter = new JavaInterfaceNodePromoter();

		// when
		List<JvmNode> actualList = promoter.promote(
			// We need to get the candidate manually for this test
			contextWithResolver.getCandidateNodeGenerators().stream()
				.filter(gen -> gen.isSupported(node.getConcreteType()))
				.flatMap(gen -> gen.generateNextNodeCandidates(node.getConcreteType()).stream())
				.findFirst().orElseThrow(() -> new RuntimeException("No candidate found")),
			contextWithResolver
		);

		// then
		then(actualList).hasSize(1);
		JvmNode actual = actualList.get(0);
		then(actual.getConcreteType().getRawType()).isEqualTo(InterfaceImplementation.class);
	}
}
