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

import java.beans.ConstructorProperties;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.tests.java.RuntimeSubtreeInstantiateTest.BoxHolder;
import com.navercorp.fixturemonkey.tests.java.RuntimeSubtreeInstantiateTest.Box;
import com.navercorp.fixturemonkey.tests.java.RuntimeSubtreeInstantiateTest.ListBox;

class RegisterSizeInRuntimeSubtreeTest {
	@Test
	void registeredSizeOfSampleRootAppliesInsideInterfaceImplementationChosenDuringAssembly() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Box.class, Collections.singletonList(ListBox.class)))
			.register(BoxHolder.class, fm -> fm.giveMeBuilder(BoxHolder.class).size("box.items", 5))
			.build();

		// when
		Box actual = sut.giveMeOne(BoxHolder.class).getBox();

		// then
		then(((ListBox)actual).getItems()).hasSize(5);
	}

	@Test
	void registeredSizeOfNestedTypeAppliesInsideInterfaceImplementationChosenDuringAssembly() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Box.class, Collections.singletonList(ListBox.class)))
			.register(BoxHolder.class, fm -> fm.giveMeBuilder(BoxHolder.class).size("box.items", 5))
			.build();

		// when
		Box actual = sut.giveMeOne(BoxHolderOwner.class).getBoxHolder().getBox();

		// then
		then(((ListBox)actual).getItems()).hasSize(5);
	}

	@Test
	void registeredSizeOfInterfaceImplementationAppliesInsideIt() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Box.class, Collections.singletonList(ListBox.class)))
			.register(ListBox.class, fm -> fm.giveMeBuilder(ListBox.class).size("items", 5))
			.build();

		// when
		Box actual = sut.giveMeOne(BoxHolder.class).getBox();

		// then
		then(((ListBox)actual).getItems()).hasSize(5);
	}

	public static class BoxHolderOwner {
		private final BoxHolder boxHolder;

		@ConstructorProperties("boxHolder")
		public BoxHolderOwner(BoxHolder boxHolder) {
			this.boxHolder = boxHolder;
		}

		public BoxHolder getBoxHolder() {
			return boxHolder;
		}
	}
}
