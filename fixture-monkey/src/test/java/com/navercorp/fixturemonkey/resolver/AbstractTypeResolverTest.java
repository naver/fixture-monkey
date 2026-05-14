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

package com.navercorp.fixturemonkey.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.property.JvmNodePropertyFactory;
import com.navercorp.objectfarm.api.node.SeedState;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

class AbstractTypeResolverTest {

	interface Animal {
	}

	interface Mammal extends Animal {
	}

	static class Dog implements Mammal {
	}

	static class Cat implements Animal {
	}

	abstract static class Bird {
	}

	static class Sparrow extends Bird {
	}

	private final AbstractTypeResolver resolver = new AbstractTypeResolver(new SeedState(0));
	private static final Function<Property, CandidateConcretePropertyResolver> NO_LOOKUP = p -> null;

	@Test
	void concreteInputReturnsUnchanged() {
		// given
		JvmType input = new JavaType(Dog.class);

		// when
		JvmType result = resolver.resolve(input, NO_LOOKUP, 10);

		// then
		assertThat(result.getRawType()).isEqualTo(Dog.class);
	}

	@Test
	void abstractInputWithoutLookupReturnsUnchanged() {
		// given
		JvmType input = new JavaType(Animal.class);

		// when
		JvmType result = resolver.resolve(input, NO_LOOKUP, 10);

		// then
		assertThat(result.getRawType()).isEqualTo(Animal.class);
	}

	@Test
	void interfaceResolvesToConcrete() {
		// given
		Function<Property, CandidateConcretePropertyResolver> lookup = property ->
			property.getJvmType().getRawType() == Animal.class ? candidates(Dog.class) : null;

		// when
		JvmType result = resolver.resolve(new JavaType(Animal.class), lookup, 10);

		// then
		assertThat(result.getRawType()).isEqualTo(Dog.class);
	}

	@Test
	void abstractClassResolvesToConcrete() {
		// given
		Function<Property, CandidateConcretePropertyResolver> lookup = property ->
			property.getJvmType().getRawType() == Bird.class ? candidates(Sparrow.class) : null;

		// when
		JvmType result = resolver.resolve(new JavaType(Bird.class), lookup, 10);

		// then
		assertThat(result.getRawType()).isEqualTo(Sparrow.class);
	}

	@Test
	void multiHopChainResolves() {
		// given
		Function<Property, CandidateConcretePropertyResolver> lookup = property -> {
			Class<?> raw = property.getJvmType().getRawType();
			if (raw == Animal.class) {
				return candidates(Mammal.class);
			}
			if (raw == Mammal.class) {
				return candidates(Dog.class);
			}
			return null;
		};

		// when
		JvmType result = resolver.resolve(new JavaType(Animal.class), lookup, 10);

		// then
		assertThat(result.getRawType()).isEqualTo(Dog.class);
	}

	@Test
	void selfCycleTerminates() {
		// given
		Function<Property, CandidateConcretePropertyResolver> lookup = property ->
			property.getJvmType().getRawType() == Animal.class ? candidates(Animal.class) : null;

		// when
		JvmType result = resolver.resolve(new JavaType(Animal.class), lookup, 10);

		// then
		assertThat(result.getRawType()).isEqualTo(Animal.class);
	}

	@Test
	void depthLimitStopsBeforeReachingConcrete() {
		// given
		Function<Property, CandidateConcretePropertyResolver> lookup = property -> {
			Class<?> raw = property.getJvmType().getRawType();
			if (raw == Animal.class) {
				return candidates(Mammal.class);
			}
			if (raw == Mammal.class) {
				return candidates(Dog.class);
			}
			return null;
		};

		// when
		JvmType result = resolver.resolve(new JavaType(Animal.class), lookup, 1);

		// then
		assertThat(result.getRawType()).isEqualTo(Mammal.class);
	}

	@Test
	void zeroDepthReturnsInputUnchanged() {
		// given
		Function<Property, CandidateConcretePropertyResolver> lookup = property -> candidates(Dog.class);

		// when
		JvmType result = resolver.resolve(new JavaType(Animal.class), lookup, 0);

		// then
		assertThat(result.getRawType()).isEqualTo(Animal.class);
	}

	@Test
	void emptyCandidatesReturnUnchanged() {
		// given
		Function<Property, CandidateConcretePropertyResolver> lookup = property -> p -> Collections.emptyList();

		// when
		JvmType result = resolver.resolve(new JavaType(Animal.class), lookup, 10);

		// then
		assertThat(result.getRawType()).isEqualTo(Animal.class);
	}

	@Test
	void multipleCandidatesPickOneOfThem() {
		// given
		Function<Property, CandidateConcretePropertyResolver> lookup = property ->
			property.getJvmType().getRawType() == Animal.class ? candidates(Dog.class, Cat.class) : null;

		// when
		JvmType result = resolver.resolve(new JavaType(Animal.class), lookup, 10);

		// then
		assertThat(result.getRawType()).isIn(Dog.class, Cat.class);
	}

	private static CandidateConcretePropertyResolver candidates(Class<?>... classes) {
		return property -> Arrays.stream(classes)
			.map(c -> JvmNodePropertyFactory.fromType(new JavaType(c)))
			.collect(Collectors.toList());
	}
}
