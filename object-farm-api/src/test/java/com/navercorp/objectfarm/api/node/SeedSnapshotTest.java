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

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;

class SeedSnapshotTest {
	private static final SeedSnapshot SAMPLE = new SeedSnapshot(20260925L, 3L);

	@Test
	void scopeOfPathEqualsNestingSegmentBySegment() {
		// given
		PathExpression path = PathExpression.of("$.items[1].labels");
		SeedSnapshot expected = SAMPLE;
		for (Segment segment : path.getSegments()) {
			expected = expected.scope(segment);
		}

		// when
		SeedSnapshot actual = SAMPLE.scopeOf(path);

		// then
		then(actual).isEqualTo(expected);
	}

	@Test
	void nestingOrderChangesScope() {
		// given
		SeedSnapshot ab = SAMPLE.scopeOf(PathExpression.of("$.a.b"));

		// when
		SeedSnapshot ba = SAMPLE.scopeOf(PathExpression.of("$.b.a"));

		// then
		then(ba).isNotEqualTo(ab);
	}

	@Test
	void siblingScopesDiffer() {
		// given
		SeedSnapshot first = SAMPLE.scopeOf(PathExpression.of("$.items[0]"));

		// when
		SeedSnapshot second = SAMPLE.scopeOf(PathExpression.of("$.items[1]"));

		// then
		then(second).isNotEqualTo(first);
	}

	@Test
	void differentSamplesGiveDifferentScopes() {
		// given
		PathExpression path = PathExpression.of("$.items");

		// when
		SeedSnapshot other = new SeedSnapshot(20260925L, 4L).scopeOf(path);

		// then
		then(other).isNotEqualTo(SAMPLE.scopeOf(path));
	}
}
