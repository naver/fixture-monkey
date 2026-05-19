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

package com.navercorp.objectfarm.api.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ReflectiveJvmTypeTest {
	@Test
	void componentType_isNullForNonArrayType() {
		// given
		JvmType type = new ReflectiveJvmType(new ObjectTypeReference<List<String>>() {
		});

		// then
		then(type.getComponentType()).isNull();
	}

	@Test
	void componentType_preservesRawComponentForPlainArrayClass() {
		// given
		JvmType type = new ReflectiveJvmType(String[].class);

		// when
		JvmType componentType = type.getComponentType();

		// then
		then(componentType).isNotNull();
		then(componentType.getRawType()).isEqualTo(String.class);
	}

	@Test
	void componentType_preservesGenericsForGenericArrayType() {
		// given
		JvmType arrayType = new ReflectiveJvmType(new ObjectTypeReference<List<String>[]>() {
		});

		// when
		JvmType componentType = arrayType.getComponentType();

		// then
		then(arrayType.getRawType()).isEqualTo(List[].class);
		then(componentType).isNotNull();
		then(componentType.getRawType()).isEqualTo(List.class);
		then(componentType.getTypeVariables()).hasSize(1);
		then(componentType.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}

	@Test
	void componentType_preservesNestedGenericsForGenericArrayType() {
		// given
		JvmType arrayType = new ReflectiveJvmType(new ObjectTypeReference<Map<String, Integer>[]>() {
		});

		// when
		JvmType componentType = arrayType.getComponentType();

		// then
		then(arrayType.getRawType()).isEqualTo(Map[].class);
		then(componentType).isNotNull();
		then(componentType.getRawType()).isEqualTo(Map.class);
		then(componentType.getTypeVariables()).hasSize(2);
		then(componentType.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
		then(componentType.getTypeVariables().get(1).getRawType()).isEqualTo(Integer.class);
	}

	@Test
	void componentType_explicitlyProvided_isStoredAndReturned() {
		// given
		JvmType explicitComponent = new ReflectiveJvmType(
			List.class,
			Collections.singletonList(new ReflectiveJvmType(Integer.class)),
			Collections.emptyList()
		);

		// when
		JvmType arrayType = new ReflectiveJvmType(
			List[].class,
			Collections.emptyList(),
			Collections.emptyList(),
			explicitComponent,
			null
		);

		// then
		then(arrayType.getComponentType()).isSameAs(explicitComponent);
	}

	@Test
	void equals_considersComponentType() {
		// given
		JvmType componentA = new ReflectiveJvmType(
			List.class,
			Collections.singletonList(new ReflectiveJvmType(String.class)),
			Collections.emptyList()
		);
		JvmType componentB = new ReflectiveJvmType(
			List.class,
			Collections.singletonList(new ReflectiveJvmType(Integer.class)),
			Collections.emptyList()
		);
		JvmType arrayA = new ReflectiveJvmType(
			List[].class,
			Collections.emptyList(),
			Collections.emptyList(),
			componentA,
			null
		);
		JvmType arrayB = new ReflectiveJvmType(
			List[].class,
			Collections.emptyList(),
			Collections.emptyList(),
			componentB,
			null
		);

		// then
		then(arrayA).isNotEqualTo(arrayB);
	}
}
