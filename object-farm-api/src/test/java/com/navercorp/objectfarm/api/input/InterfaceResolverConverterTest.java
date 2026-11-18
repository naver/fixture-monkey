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

package com.navercorp.objectfarm.api.input;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

class InterfaceResolverConverterTest {
	private static final JvmType OBJECT = new ReflectiveJvmType(Object.class);

	@Test
	void resolveNonInstantiableJdkListToArrayListForObjectType() {
		// given
		InterfaceResolver resolver = InterfaceResolverConverter
			.createResolver("$.value", Collections.singletonList("a").getClass())
			.getCustomizer();

		// when
		JvmType actual = resolver.resolve(OBJECT);

		// then
		then(actual).isNotNull();
		then(actual.getRawType()).isEqualTo(ArrayList.class);
	}

	@Test
	void resolveNonInstantiableJdkSetToHashSetForObjectType() {
		// given
		InterfaceResolver resolver = InterfaceResolverConverter
			.createResolver("$.value", Collections.singleton("a").getClass())
			.getCustomizer();

		// when
		JvmType actual = resolver.resolve(OBJECT);

		// then
		then(actual).isNotNull();
		then(actual.getRawType()).isEqualTo(HashSet.class);
	}

	@Test
	void skipNonInstantiableJdkListForListType() {
		// given
		InterfaceResolver resolver = InterfaceResolverConverter
			.createResolver("$.value", Collections.singletonList("a").getClass())
			.getCustomizer();

		// when
		JvmType actual = resolver.resolve(new ReflectiveJvmType(List.class));

		// then
		then(actual).isNull();
	}

	@Test
	void keepInstantiableTypeForObjectType() {
		// given
		InterfaceResolver resolver = InterfaceResolverConverter
			.createResolver("$.value", LinkedList.class)
			.getCustomizer();

		// when
		JvmType actual = resolver.resolve(OBJECT);

		// then
		then(actual).isNotNull();
		then(actual.getRawType()).isEqualTo(LinkedList.class);
	}
}
