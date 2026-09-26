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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.RepeatedTest;

import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class DeclaredTypeMatchingTest {
	private static final FixtureMonkey CONTAINER_SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.register(
			new MatcherOperator<>(
				it -> it.getJvmType().getRawType().equals(Set.class),
				fixture -> fixture.giveMeBuilder(new TypeReference<Set<String>>() {
					})
					.size("$", 3)
			)
		)
		.register(
			new MatcherOperator<>(
				it -> it.getJvmType().getRawType().equals(Map.class),
				fixture -> fixture.giveMeBuilder(new TypeReference<Map<String, String>>() {
					})
					.size("$", 2)
			)
		)
		.build();

	private static final FixtureMonkey LIST_SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.register(
			new MatcherOperator<>(
				it -> it.getJvmType().getRawType().equals(List.class)
					&& it.getJvmType().getTypeVariables().size() == 1
					&& it.getJvmType().getTypeVariables().get(0).getRawType().equals(String.class),
				fixture -> fixture.giveMeBuilder(new TypeReference<List<String>>() {
					})
					.size("$", 2)
					.set("$[0]", "x")
			)
		)
		.build();

	@RepeatedTest(10)
	void customMatcherSeesDeclaredTypeOfSampledRoot() {
		// when
		List<String> actual = LIST_SUT.giveMeOne(new TypeReference<List<String>>() {
		});

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo("x");
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredTypeOfField() {
		// when
		List<String> actual = LIST_SUT.giveMeOne(ListHolder.class).getNames();

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo("x");
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredTypeOfContainerElement() {
		// when
		List<List<String>> actual = LIST_SUT.giveMeBuilder(ListHolder.class).size("nested", 2).sample().getNested();

		// then
		then(actual).hasSize(2).allSatisfy(it -> {
			then(it).hasSize(2);
			then(it.get(0)).isEqualTo("x");
		});
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredSetTypeOfField() {
		// when
		Set<String> actual = CONTAINER_SUT.giveMeOne(ContainerHolder.class).getSet();

		// then
		then(actual).hasSize(3);
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredMapTypeOfField() {
		// when
		Map<String, String> actual = CONTAINER_SUT.giveMeOne(ContainerHolder.class).getMap();

		// then
		then(actual).hasSize(2);
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredMapTypeOfSampledRoot() {
		// when
		Map<String, String> actual = CONTAINER_SUT.giveMeOne(new TypeReference<Map<String, String>>() {
		});

		// then
		then(actual).hasSize(2);
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredInterfaceTypeOfField() {
		// given
		FixtureMonkey sut = interfaceFixtureMonkey()
			.register(
				new MatcherOperator<>(
					it -> it.getJvmType().getRawType().equals(Named.class),
					fixture -> fixture.giveMeBuilder(NamedImpl.class).set("name", "fixed")
				)
			)
			.build();

		// when
		Named actual = sut.giveMeOne(InterfaceHolder.class).getNamed();

		// then
		then(actual.getName()).isEqualTo("fixed");
	}

	@RepeatedTest(10)
	void exactTypeRegisterOfInterfaceAppliesToInterfaceField() {
		// given
		FixtureMonkey sut = interfaceFixtureMonkey()
			.registerExactType(Named.class, fixture -> fixture.giveMeBuilder(NamedImpl.class).set("name", "fixed"))
			.build();

		// when
		Named actual = sut.giveMeOne(InterfaceHolder.class).getNamed();

		// then
		then(actual.getName()).isEqualTo("fixed");
	}

	@RepeatedTest(10)
	void exactTypeRegisterOfInterfaceAppliesToInterfaceElements() {
		// given
		FixtureMonkey sut = interfaceFixtureMonkey()
			.registerExactType(Named.class, fixture -> fixture.giveMeBuilder(NamedImpl.class).set("name", "fixed"))
			.build();

		// when
		List<Named> actual = sut.giveMeBuilder(InterfaceHolder.class).size("names", 2).sample().getNames();

		// then
		then(actual).hasSize(2).allSatisfy(it -> then(it.getName()).isEqualTo("fixed"));
	}

	@RepeatedTest(10)
	void exactTypeRegisterOfImplementationAppliesToInterfaceField() {
		// given
		FixtureMonkey sut = interfaceFixtureMonkey()
			.registerExactType(NamedImpl.class, fixture -> fixture.giveMeBuilder(NamedImpl.class).set("name", "fixed"))
			.build();

		// when
		Named actual = sut.giveMeOne(InterfaceHolder.class).getNamed();

		// then
		then(actual.getName()).isEqualTo("fixed");
	}

	private static FixtureMonkeyBuilder interfaceFixtureMonkey() {
		return FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Named.class, Collections.singletonList(NamedImpl.class)));
	}

	@Data
	public static class ListHolder {
		private List<String> names;
		private List<List<String>> nested;
	}

	@Data
	public static class ContainerHolder {
		private Set<String> set;
		private Map<String, String> map;
	}

	@Data
	public static class InterfaceHolder {
		private Named named;
		private List<Named> names;
	}

	public interface Named {
		String getName();
	}

	@Data
	public static class NamedImpl implements Named {
		private String name;
	}
}
