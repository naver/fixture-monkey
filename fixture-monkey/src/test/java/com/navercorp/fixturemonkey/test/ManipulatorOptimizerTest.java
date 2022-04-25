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

package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;
import com.navercorp.fixturemonkey.resolver.OptimizedManipulatorResult;

public class ManipulatorOptimizerTest {
	private final FixtureMonkey fixture = FixtureMonkey.create();
	private final ManipulatorOptimizer sut = new ManipulatorOptimizer();

	@Property
	public void optimize() {
		List<BuilderManipulator> manipulators = fixture.giveMeOne(new TypeReference<List<BuilderManipulator>>() {
		});

		OptimizedManipulatorResult actual = sut.optimize(manipulators);

		then(actual.getManipulators()).isEqualTo(manipulators);
	}
}
