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

package com.navercorp.fixturemonkey.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import net.jqwik.api.Property;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.input.InterfaceResolverConverter;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

class InterfaceResolverConverterTest {

	interface Animal {
		String getName();
	}

	static class Dog implements Animal {

		@Override
		public String getName() {
			return "Dog";
		}
	}

	static class Cat implements Animal {

		@Override
		public String getName() {
			return "Cat";
		}
	}

	@Property
	void createsResolverFromValueType() {
		Dog dog = new Dog();

		PathResolver<InterfaceResolver> resolver = InterfaceResolverConverter.fromValue("$.animal", dog);

		assertThat(resolver).isNotNull();

		PathExpression path = PathExpression.root().child("animal");
		assertThat(resolver.matches(path)).isTrue();

		InterfaceResolver interfaceResolver = resolver.getCustomizer();
		JvmType animalType = new JavaType(Animal.class);
		JvmType resolvedType = interfaceResolver.resolve(animalType);

		assertThat(resolvedType).isNotNull();
		assertThat(resolvedType.getRawType()).isEqualTo(Dog.class);
	}

	@Property
	void createsResolverWithSpecificConcreteType() {
		PathResolver<InterfaceResolver> resolver = InterfaceResolverConverter.createResolver("$.animal", Cat.class);

		assertThat(resolver).isNotNull();

		InterfaceResolver interfaceResolver = resolver.getCustomizer();
		JvmType animalType = new JavaType(Animal.class);
		JvmType resolvedType = interfaceResolver.resolve(animalType);

		assertThat(resolvedType).isNotNull();
		assertThat(resolvedType.getRawType()).isEqualTo(Cat.class);
	}

	@Property
	void returnsNullForNonAssignableTypes() {
		PathResolver<InterfaceResolver> resolver = InterfaceResolverConverter.createResolver("$.animal", Dog.class);

		InterfaceResolver interfaceResolver = resolver.getCustomizer();

		JvmType stringType = new JavaType(String.class);
		JvmType resolvedType = interfaceResolver.resolve(stringType);

		assertThat(resolvedType).isNull();
	}

	@Property
	void nullValueReturnsNullResolver() {
		PathResolver<InterfaceResolver> resolver = InterfaceResolverConverter.fromValue("$.animal", null);

		assertThat(resolver).isNull();
	}

	@Property
	void createsResolverWithTypeVariables() {
		List<String> stringList = Collections.singletonList("test");
		JvmType stringType = new JavaType(String.class);

		PathResolver<InterfaceResolver> resolver = InterfaceResolverConverter.fromValueWithGenerics(
			"$.items",
			stringList,
			Collections.singletonList(stringType)
		);

		assertThat(resolver).isNotNull();

		InterfaceResolver interfaceResolver = resolver.getCustomizer();
		JvmType listType = new JavaType(List.class);
		JvmType resolvedType = interfaceResolver.resolve(listType);

		assertThat(resolvedType).isNotNull();
		assertThat(resolvedType.getTypeVariables()).hasSize(1);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}

	@Property
	void pathPatternMatchingWorksCorrectly() {
		PathResolver<InterfaceResolver> resolver = InterfaceResolverConverter.createResolver(
			"$.items[*].animal",
			Dog.class
		);

		PathExpression path1 = PathExpression.root().child("items").index(0).child("animal");
		PathExpression path2 = PathExpression.root().child("items").index(5).child("animal");

		assertThat(resolver.matches(path1)).isTrue();
		assertThat(resolver.matches(path2)).isTrue();

		PathExpression path3 = PathExpression.root().child("items").child("animal");
		PathExpression path4 = PathExpression.root().child("animal");

		assertThat(resolver.matches(path3)).isFalse();
		assertThat(resolver.matches(path4)).isFalse();
	}
}
