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

package com.navercorp.fixturemonkey.mockito.plugin;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.when;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Example;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.mockito.plugin.MockitoPluginTestSpecs.Sample;

class MockitoPluginTest {
	private final LabMonkey sut = FixtureMonkey.labMonkeyBuilder()
		.plugin(new MockitoPlugin())
		.defaultNullInjectGenerator(context -> 0)
		.build();

	@Example
	void mockitoAbstractInterface() {
		Sample actual = this.sut.giveMeOne(Sample.class);
		then(actual.getAbstractSample()).isNotNull();

		String mockStringValue = Arbitraries.strings().sample();
		when(actual.getAbstractSample().getValue()).thenReturn(mockStringValue);
		then(actual.getAbstractSample().getValue()).isEqualTo(mockStringValue);

		then(actual.getInterfaceSample()).isNotNull();

		int mockIntValue = Arbitraries.integers().sample();
		when(actual.getInterfaceSample().getInt()).thenReturn(mockIntValue);
		then(actual.getInterfaceSample().getInt()).isEqualTo(mockIntValue);
	}
}
