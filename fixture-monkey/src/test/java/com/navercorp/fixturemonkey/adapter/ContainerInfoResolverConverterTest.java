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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.adapter.converter.ContainerInfoResolverConverter;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.PropertyNameNodePredicate;
import com.navercorp.fixturemonkey.tree.StartNodePredicate;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.type.JavaType;

class ContainerInfoResolverConverterTest {

	@Property
	void convertsContainerInfoManipulatorToResolver() {
		List<NextNodePredicate> predicates = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("items")
		);
		ContainerInfoManipulator manipulator = new ContainerInfoManipulator(
			predicates,
			new ArbitraryContainerInfo(3, 3),
			0
		);

		PathResolver<ContainerSizeResolver> resolver =
			ContainerInfoResolverConverter.convertSingle(manipulator);

		assertThat(resolver).isNotNull();

		PathExpression path = PathExpression.root().child("items");
		assertThat(resolver.matches(path)).isTrue();

		ContainerSizeResolver sizeResolver = resolver.getCustomizer();
		int size = sizeResolver.resolveContainerSize(new JavaType(List.class));
		assertThat(size).isEqualTo(3);
	}

	@Property
	void convertsMultipleManipulatorsSortedBySequence() {
		List<NextNodePredicate> predicates1 = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("items")
		);
		List<NextNodePredicate> predicates2 = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("orders")
		);

		ContainerInfoManipulator manipulator1 = new ContainerInfoManipulator(
			predicates1,
			new ArbitraryContainerInfo(5, 5),
			1
		);
		ContainerInfoManipulator manipulator2 = new ContainerInfoManipulator(
			predicates2,
			new ArbitraryContainerInfo(2, 2),
			0
		);

		List<ContainerInfoManipulator> manipulators = Arrays.asList(manipulator1, manipulator2);
		List<PathResolver<ContainerSizeResolver>> resolvers =
			ContainerInfoResolverConverter.convert(manipulators);

		assertThat(resolvers).hasSize(2);

		PathExpression ordersPath = PathExpression.root().child("orders");
		assertThat(resolvers.get(0).matches(ordersPath)).isTrue();
		assertThat(resolvers.get(0).getCustomizer().resolveContainerSize(new JavaType(List.class))).isEqualTo(2);

		PathExpression itemsPath = PathExpression.root().child("items");
		assertThat(resolvers.get(1).matches(itemsPath)).isTrue();
		assertThat(resolvers.get(1).getCustomizer().resolveContainerSize(new JavaType(List.class))).isEqualTo(5);
	}

	@Property
	void fixedSizeIsDeterminedFromContainerInfoRange() {
		List<NextNodePredicate> predicates = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("items")
		);

		ContainerInfoManipulator manipulator = new ContainerInfoManipulator(
			predicates,
			new ArbitraryContainerInfo(1, 5),
			0
		);

		PathResolver<ContainerSizeResolver> resolver =
			ContainerInfoResolverConverter.convertSingle(manipulator);

		ContainerSizeResolver sizeResolver = resolver.getCustomizer();
		int size = sizeResolver.resolveContainerSize(new JavaType(List.class));

		assertThat(size).isBetween(1, 5);
	}

	@Property
	void emptyListReturnsEmptyResolvers() {
		List<ContainerInfoManipulator> manipulators = new ArrayList<>();

		List<PathResolver<ContainerSizeResolver>> resolvers =
			ContainerInfoResolverConverter.convert(manipulators);

		assertThat(resolvers).isEmpty();
	}
}
