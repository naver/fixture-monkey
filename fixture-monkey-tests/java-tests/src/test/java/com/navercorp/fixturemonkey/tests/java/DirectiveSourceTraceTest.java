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

package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.tracing.ResolutionTrace;

class DirectiveSourceTraceTest {
	@Test
	void directivesAreTracedWithTheirSource() {
		// given
		List<ResolutionTrace> traces = new ArrayList<>();
		FixtureMonkey sut = sutTracingInto(traces);

		// when
		sut.giveMeBuilder(Parent.class).set("title", "user").sample();

		// then
		Map<String, String> sourceByPath = traces.get(0).getDirectives()
			.stream()
			.collect(Collectors.toMap(
				ResolutionTrace.DirectiveEntry::path,
				it -> String.valueOf(it.source()),
				(first, second) -> first
			));
		then(sourceByPath).containsEntry("$.title", "DIRECT");
		then(sourceByPath).containsEntry("assignable:Child.name", "REGISTER");
	}

	@Test
	void mergedCandidatesAreTracedWithTheirSource() {
		// given
		List<ResolutionTrace> traces = new ArrayList<>();
		FixtureMonkey sut = sutTracingInto(traces);

		// when
		sut.giveMeBuilder(Parent.class).set("title", "user").sample();

		// then
		Map<String, String> sourceByPath = traces.get(0).getMergedCandidates()
			.stream()
			.collect(Collectors.toMap(
				ResolutionTrace.MergedCandidateEntry::path,
				ResolutionTrace.MergedCandidateEntry::source
			));
		then(sourceByPath).containsEntry("$.title", "DIRECT");
		then(sourceByPath).containsEntry("$[scope:type:" + Child.class.getName() + "].name", "REGISTER");
	}

	@Test
	void assembledNodesAreTracedWithTheSourceOfTheirValue() {
		// given
		List<ResolutionTrace> traces = new ArrayList<>();
		FixtureMonkey sut = sutTracingInto(traces);

		// when
		sut.giveMeBuilder(Parent.class).set("title", "user").sample();

		// then
		Map<String, String> sourceByPath = traces.get(0).getAssemblySteps()
			.stream()
			.collect(Collectors.toMap(
				ResolutionTrace.AssemblyEntry::path,
				ResolutionTrace.AssemblyEntry::source,
				(first, second) -> first
			));
		then(sourceByPath).containsEntry("$.title", "DIRECT");
		then(sourceByPath).containsEntry("$.child.name", "REGISTER");
	}

	private static FixtureMonkey sutTracingInto(List<ResolutionTrace> traces) {
		return FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Child.class, fm -> fm.giveMeBuilder(Child.class).set("name", "registered"))
			.tracer(traces::add)
			.build();
	}

	@Data
	public static class Parent {
		private String title;
		private Child child;
	}

	@Data
	public static class Child {
		private String name;
	}
}
