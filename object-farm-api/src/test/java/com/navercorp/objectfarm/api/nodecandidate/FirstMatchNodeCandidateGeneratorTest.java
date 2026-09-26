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

package com.navercorp.objectfarm.api.nodecandidate;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

class FirstMatchNodeCandidateGeneratorTest {
	private static final JvmType TYPE = new ReflectiveJvmType(Object.class);

	private static final JvmNodeCandidate FIRST =
		JavaNodeCandidateFactory.INSTANCE.create(new ReflectiveJvmType(String.class), "first", null);

	private static final JvmNodeCandidate SECOND =
		JavaNodeCandidateFactory.INSTANCE.create(new ReflectiveJvmType(Integer.class), "second", null);

	@Test
	void skipsUnsupportedGenerator() {
		// given
		FirstMatchNodeCandidateGenerator sut = new FirstMatchNodeCandidateGenerator(
			Arrays.asList(
				generator(false, Collections.singletonList(FIRST)),
				generator(true, Collections.singletonList(SECOND))
			)
		);

		// when
		List<JvmNodeCandidate> actual = sut.generateNextNodeCandidates(TYPE);

		// then
		then(actual).containsExactly(SECOND);
	}

	@Test
	void skipsGeneratorProducingNoCandidates() {
		// given
		FirstMatchNodeCandidateGenerator sut = new FirstMatchNodeCandidateGenerator(
			Arrays.asList(
				generator(true, Collections.emptyList()),
				generator(true, Collections.singletonList(SECOND))
			)
		);

		// when
		List<JvmNodeCandidate> actual = sut.generateNextNodeCandidates(TYPE);

		// then
		then(actual).containsExactly(SECOND);
	}

	@Test
	void returnsFirstMatchOnly() {
		// given
		FirstMatchNodeCandidateGenerator sut = new FirstMatchNodeCandidateGenerator(
			Arrays.asList(
				generator(true, Collections.singletonList(FIRST)),
				generator(true, Collections.singletonList(SECOND))
			)
		);

		// when
		List<JvmNodeCandidate> actual = sut.generateNextNodeCandidates(TYPE);

		// then
		then(actual).containsExactly(FIRST);
	}

	@Test
	void returnsEmptyWhenNoGeneratorProduces() {
		// given
		FirstMatchNodeCandidateGenerator sut = new FirstMatchNodeCandidateGenerator(
			Arrays.asList(
				generator(false, Collections.singletonList(FIRST)),
				generator(true, Collections.emptyList())
			)
		);

		// when
		List<JvmNodeCandidate> actual = sut.generateNextNodeCandidates(TYPE);

		// then
		then(actual).isEmpty();
	}

	@Test
	void isSupportedWhenAnyGeneratorSupports() {
		// given
		FirstMatchNodeCandidateGenerator sut = new FirstMatchNodeCandidateGenerator(
			Arrays.asList(
				generator(false, Collections.emptyList()),
				generator(true, Collections.emptyList())
			)
		);

		// when
		boolean actual = sut.isSupported(TYPE);

		// then
		then(actual).isTrue();
	}

	private static JvmNodeCandidateGenerator generator(boolean supported, List<JvmNodeCandidate> candidates) {
		return new JvmNodeCandidateGenerator() {
			@Override
			public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
				return candidates;
			}

			@Override
			public boolean isSupported(JvmType jvmType) {
				return supported;
			}
		};
	}
}
