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

package com.navercorp.fixturemonkey.resolver;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;
import com.navercorp.fixturemonkey.arbitrary.MetadataManipulator;
import com.navercorp.fixturemonkey.arbitrary.PostArbitraryManipulator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ManipulatorOptimizer {

	@SuppressWarnings("rawtypes")
	public OptimizedManipulatorResult optimize(List<BuilderManipulator> manipulators) {
		List<BuilderManipulator> optimizedManipulators = new ArrayList<>();

		List<MetadataManipulator> metadataManipulators = extractMetadataManipulators(manipulators);
		List<BuilderManipulator> orderedManipulators = extractOrderedManipulators(manipulators);
		List<PostArbitraryManipulator> postArbitraryManipulators = extractPostArbitraryManipulators(manipulators);

		optimizedManipulators.addAll(metadataManipulators);
		optimizedManipulators.addAll(orderedManipulators);
		optimizedManipulators.addAll(postArbitraryManipulators);

		return new OptimizedManipulatorResult(optimizedManipulators);
	}

	private List<MetadataManipulator> extractMetadataManipulators(List<BuilderManipulator> manipulators) {
		return manipulators.stream()
			.filter(MetadataManipulator.class::isInstance)
			.map(MetadataManipulator.class::cast)
			.sorted()
			.collect(toList());
	}

	private List<BuilderManipulator> extractOrderedManipulators(List<BuilderManipulator> manipulators) {
		return manipulators.stream()
			.filter(it -> !(it instanceof MetadataManipulator))
			.filter(it -> !(it instanceof PostArbitraryManipulator))
			.collect(toList());
	}

	@SuppressWarnings("rawtypes")
	private List<PostArbitraryManipulator> extractPostArbitraryManipulators(List<BuilderManipulator> manipulators) {
		return manipulators.stream()
			.filter(PostArbitraryManipulator.class::isInstance)
			.map(PostArbitraryManipulator.class::cast)
			.collect(toList());
	}
}
