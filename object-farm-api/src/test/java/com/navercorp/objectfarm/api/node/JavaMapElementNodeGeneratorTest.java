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

import com.navercorp.objectfarm.api.nodecandidate.JvmMapEntryNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmMapNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

class JavaMapElementNodeGeneratorTest {

	private static final JavaNodeContext CONTEXT = JavaNodeContext.builder()
		.seed(-1L)
		.containerSizeResolver(new FixedContainerSizeResolver(1))
		.build();

	@Test
	void mapEntryKeyAndValueAreMapEntryCandidates() {
		// given
		ReflectiveJvmType entryType = new ReflectiveJvmType(
			Map.Entry.class,
			Arrays.asList(new ReflectiveJvmType(String.class), new ReflectiveJvmType(Integer.class)),
			Collections.emptyList()
		);
		ReflectiveJvmType mapType = new ReflectiveJvmType(
			Map.class,
			Arrays.asList(entryType, entryType),
			Collections.emptyList()
		);
		JvmNode mapNode = new JavaNode(mapType, "root");

		// when
		List<JvmNodeCandidate> actual = new JavaMapElementNodeGenerator().generateContainerElements(mapNode, CONTEXT);

		// then
		then(actual).hasSize(1);
		JvmMapNodeCandidate element = (JvmMapNodeCandidate)actual.get(0);
		then(element.getKey()).isInstanceOf(JvmMapEntryNodeCandidate.class);
		then(element.getValue()).isInstanceOf(JvmMapEntryNodeCandidate.class);
	}
}
