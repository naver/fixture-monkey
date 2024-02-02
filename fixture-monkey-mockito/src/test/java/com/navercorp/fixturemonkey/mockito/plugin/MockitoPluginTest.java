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

import java.util.Collections;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Example;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.mockito.plugin.MockitoPluginTestSpecs.AbstractSample;
import com.navercorp.fixturemonkey.mockito.plugin.MockitoPluginTestSpecs.AbstractSampleImpl;
import com.navercorp.fixturemonkey.mockito.plugin.MockitoPluginTestSpecs.InterfaceSample;
import com.navercorp.fixturemonkey.mockito.plugin.MockitoPluginTestSpecs.InterfaceSampleImpl;
import com.navercorp.fixturemonkey.mockito.plugin.MockitoPluginTestSpecs.Sample;

class MockitoPluginTest {
	@Example
	void mockitoAbstractInterface() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new MockitoPlugin())
			.defaultNullInjectGenerator(context -> 0)
			.build();

		// when
		Sample actual = sut.giveMeOne(Sample.class);

		// then
		then(actual.getAbstractSample()).isNotNull();
		String mockStringValue = Arbitraries.strings().sample();
		when(actual.getAbstractSample().getValue()).thenReturn(mockStringValue);
		then(actual.getAbstractSample().getValue()).isEqualTo(mockStringValue);

		then(actual.getInterfaceSample()).isNotNull();

		int mockIntValue = Arbitraries.integers().sample();
		when(actual.getInterfaceSample().getValue()).thenReturn(mockIntValue);
		then(actual.getInterfaceSample().getValue()).isEqualTo(mockIntValue);
	}

	@Example
	void interfaceImplementsAndMockitoInterface() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new MockitoPlugin())
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(InterfaceSample.class, Collections.singletonList(InterfaceSampleImpl.class))
			)
			.defaultNullInjectGenerator(context -> 0)
			.build();

		int actual = sut.giveMeOne(Sample.class).getInterfaceSample().getValue();

		then(actual).isNotNull();
	}

	@Example
	void interfaceImplementsAndMockitoAbstract() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new MockitoPlugin())
			.defaultNullInjectGenerator(context -> 0)
			.plugin(
				new InterfacePlugin()
					.abstractClassExtends(AbstractSample.class, Collections.singletonList(AbstractSampleImpl.class))
			)
			.build();

		String actual = sut.giveMeOne(Sample.class).getAbstractSample().getValue();

		then(actual).isNotNull();
	}
}
