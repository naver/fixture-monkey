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

package com.navercorp.fixturemonkey.api.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import lombok.Builder;
import lombok.Getter;

class ReflectionsTest {

	@Test
	void createNewInstanceWithNoArgsConstructor() {
		// given
		Class<TestTarget> clazz = TestTarget.class;

		// when
		TestTarget actual = Reflections.newInstance(clazz);

		then(actual).isNotNull();
	}

	@Test
	void createNewInstanceWithAnotherConstructor() throws NoSuchMethodException {
		// given
		Constructor<TestTarget> constructor = TestTarget.class.getDeclaredConstructor(String.class);
		String argument = "Hello, World!";

		// when
		TestTarget actual = Reflections.newInstance(constructor, argument);

		then(actual)
			.satisfies(
				it -> then(it.name).isEqualTo(argument)
			);
	}

	@Test
	void invokeNoArgsMethod() {
		// given
		String name = Arbitraries.strings().sample();
		TestTarget target = new TestTarget(name);
		String methodName = "getName";
		Method method = Reflections.findMethod(TestTarget.class, methodName);

		// when
		Object actual = Reflections.invokeMethod(method, target);

		then(actual).isEqualTo(name);
	}

	@Test
	void invokeMethodWithArgument() {
		// given
		String name = Arbitraries.strings().sample();
		int age = Arbitraries.integers().sample();
		TestTarget target = new TestTarget(name);
		String methodName = "withAge";
		Method method = Reflections.findMethod(TestTarget.class, methodName, int.class);

		// when
		Object actual = Reflections.invokeMethod(method, target, age);

		then(actual).isEqualTo(name + " " + age);
	}

	@Test
	void invokeBuilderMethod() {
		// given
		String methodName = "builder";
		Method method = Reflections.findMethod(TestTarget.class, methodName);

		// when
		Object actual = Reflections.invokeMethod(method, null);

		then(actual).isNotNull();
	}

	@Test
	void invokeBuildMethod() {
		// given
		String name = Arbitraries.strings().sample();
		TestTarget.TestTargetBuilder builder = TestTarget.builder().name(name);
		String methodName = "build";
		Method method = Reflections.findMethod(TestTarget.TestTargetBuilder.class, methodName);

		// when
		Object actual = Reflections.invokeMethod(method, builder);

		then(actual)
			.isInstanceOfSatisfying(
				TestTarget.class,
				it -> then(it.name).isEqualTo(name)
			);
	}

	@Test
	void findNoArgsMethod() {
		// given
		String methodName = "getName";

		// when
		Method actual = Reflections.findMethod(TestTarget.class, methodName);

		then(actual).isNotNull();
	}

	@Test
	void findMethodWithArgument() {
		// given
		String methodName = "withAge";

		// when
		Method actual = Reflections.findMethod(TestTarget.class, methodName, int.class);

		then(actual).isNotNull();
	}

	@Test
	void findMethodWithGenericArgument() {
		// given
		String methodName = "withOtherNames";

		// when
		Method actual = Reflections.findMethod(TestTarget.class, methodName, List.class);

		then(actual).isNotNull();
	}

	@Test
	void findBuilderMethod() {
		// given
		String methodName = "builder";

		// when
		Method actual = Reflections.findMethod(TestTarget.class, methodName);

		then(actual).isNotNull();
	}

	@Test
	void findBuilderFieldMethod() {
		// given
		String methodName = "name";

		// when
		Method actual = Reflections.findMethod(TestTarget.TestTargetBuilder.class, methodName, String.class);

		then(actual).isNotNull();
	}

	@Test
	void findBuildMethod() {
		// given
		String methodName = "build";

		// when
		Method actual = Reflections.findMethod(TestTarget.TestTargetBuilder.class, methodName);

		then(actual).isNotNull();
	}

	@Test
	void findAllFields() {
		// given
		Class<?> clazz = LeafChild.class;

		// when
		List<Field> actual = Reflections.findFields(clazz);

		then(actual)
			.hasSize(4)
			.map(Field::getName)
			.contains("parentName", "firstChildName", "leafChildName", "isAnimal");
	}

	private interface Person {
		boolean isAnimal = false;
	}

	private static class TestTarget {
		@Getter
		private String name;
		private int number;

		private TestTarget() {
		}

		@Builder
		private TestTarget(String name) {
			this.name = name;
		}

		private String withAge(int age) {
			return String.join(" ", name, String.valueOf(age));
		}

		private String withOtherNames(List<String> names) {
			return String.join(" ", name, String.join(" ", names));
		}
	}

	private static class LeafChild extends FirstChild {
		private int firstChildName; // duplicate
		private int leafChildName;
	}

	private static class FirstChild extends Parent {
		private int firstChildName;
	}

	private abstract static class Parent implements Person {
		private int parentName;
	}
}
