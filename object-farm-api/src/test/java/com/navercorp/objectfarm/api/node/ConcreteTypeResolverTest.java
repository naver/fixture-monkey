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

package com.navercorp.objectfarm.api.node;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.node.specs.InterfaceSpecs.Interface;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

class ConcreteTypeResolverTest {

	private static final ConcreteTypeResolver SUT = new ConcreteTypeResolver(
		JavaNodeContext.builder()
			.seed(-1L)
			.containerSizeResolver(new FixedContainerSizeResolver(1))
			.build()
	);

	private static final JvmType STRING_LIST_TYPE = new ReflectiveJvmType(
		List.class,
		Collections.singletonList(new ReflectiveJvmType(String.class)),
		Collections.emptyList()
	);

	@Test
	void containerInterfaceResolvesToDefaultImplementation() {
		// when
		JvmType actual = SUT.resolve(STRING_LIST_TYPE, null, null);

		// then
		then(actual.getRawType()).isEqualTo(ArrayList.class);
		then(actual.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}

	@Test
	void nonContainerInterfaceIsKeptAsGiven() {
		// given
		JvmType interfaceType = new ReflectiveJvmType(Interface.class);

		// when
		JvmType actual = SUT.resolve(interfaceType, null, null);

		// then
		then(actual).isSameAs(interfaceType);
	}

	@Test
	void pathInterfaceResolverTakesPrecedenceOverDefault() {
		// given
		InterfaceResolver pathInterfaceResolver =
			type -> new ReflectiveJvmType(LinkedList.class, type.getTypeVariables(), type.getAnnotations());

		// when
		JvmType actual = SUT.resolve(STRING_LIST_TYPE, pathInterfaceResolver, null);

		// then
		then(actual.getRawType()).isEqualTo(LinkedList.class);
	}

	@Test
	void jdkInternalContainerResolvesToImplementationOfNearestContainerSupertype() {
		// given
		JvmType unmodifiableListType = new ReflectiveJvmType(
			Collections.unmodifiableList(new ArrayList<String>()).getClass(),
			Collections.singletonList(new ReflectiveJvmType(String.class)),
			Collections.emptyList()
		);

		// when
		JvmType actual = SUT.resolve(unmodifiableListType, null, null);

		// then
		then(actual.getRawType()).isEqualTo(ArrayList.class);
	}

	@Test
	void pathGenericTypeResolverResolvesTypeArgumentsAfterInterface() {
		// given
		JvmType objectListType = new ReflectiveJvmType(
			List.class,
			Collections.singletonList(new ReflectiveJvmType(Object.class)),
			Collections.emptyList()
		);
		GenericTypeResolver pathGenericTypeResolver = type -> new ReflectiveJvmType(
			type.getRawType(),
			Collections.singletonList(new ReflectiveJvmType(Integer.class)),
			type.getAnnotations()
		);

		// when
		JvmType actual = SUT.resolve(objectListType, null, pathGenericTypeResolver);

		// then
		then(actual.getRawType()).isEqualTo(ArrayList.class);
		then(actual.getTypeVariables().get(0).getRawType()).isEqualTo(Integer.class);
	}

	@Test
	void annotationsOfGivenTypeAreKeptOnResolvedType() throws NoSuchFieldException {
		// given
		Annotation[] annotations = AnnotatedSpec.class.getDeclaredField("values").getAnnotations();
		JvmType annotatedListType = new ReflectiveJvmType(
			List.class,
			Collections.singletonList(new ReflectiveJvmType(String.class)),
			Arrays.asList(annotations)
		);
		InterfaceResolver annotationDroppingResolver =
			type -> new ReflectiveJvmType(LinkedList.class, type.getTypeVariables(), Collections.emptyList());

		// when
		JvmType actual = SUT.resolve(annotatedListType, annotationDroppingResolver, null);

		// then
		then(actual.getRawType()).isEqualTo(LinkedList.class);
		then(actual.getAnnotations()).extracting(Annotation::annotationType).containsExactly(Marker.class);
	}

	@Retention(RetentionPolicy.RUNTIME)
	private @interface Marker {
	}

	private static final class AnnotatedSpec {
		@Marker
		private List<String> values;
	}
}
