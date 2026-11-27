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

package com.navercorp.objectfarm.api.tree;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;

class PathResolverContextTest {
	@Test
	void emptyContextShouldReturnEmpty() {
		// given
		PathResolverContext context = PathResolverContext.empty();
		PathExpression path = PathExpression.root().child("items");

		// when
		Optional<ContainerSizeResolver> resolver = context.findContainerSizeResolver(path);

		// then
		then(resolver).isEmpty();
	}

	@Test
	void builderWithNoResolversShouldReturnEmptyContext() {
		// given
		PathResolverContext context = PathResolverContext.builder().build();
		PathExpression path = PathExpression.root().child("items");

		// when
		Optional<ContainerSizeResolver> resolver = context.findContainerSizeResolver(path);

		// then
		then(resolver).isEmpty();
	}

	@Test
	void findContainerSizeResolverShouldReturnMatchingResolver() {
		// given
		PathResolverContext context = PathResolverContext.builder()
			.addContainerSizeResolver("$.items", 5)
			.build();

		PathExpression matchingPath = PathExpression.root().child("items");
		PathExpression nonMatchingPath = PathExpression.root().child("other");

		// when
		Optional<ContainerSizeResolver> matchingResolver = context.findContainerSizeResolver(matchingPath);
		Optional<ContainerSizeResolver> nonMatchingResolver = context.findContainerSizeResolver(nonMatchingPath);

		// then
		then(matchingResolver).isPresent();
		then(matchingResolver.get().resolveContainerSize(null)).isEqualTo(5);
		then(nonMatchingResolver).isEmpty();
	}

	@Test
	void wildcardPatternShouldMatchMultiplePaths() {
		// given
		PathResolverContext context = PathResolverContext.builder()
			.addContainerSizeResolver("$.items[*]", 3)
			.build();

		PathExpression path0 = PathExpression.root().child("items").index(0);
		PathExpression path1 = PathExpression.root().child("items").index(1);
		PathExpression path99 = PathExpression.root().child("items").index(99);

		// when & then
		then(context.findContainerSizeResolver(path0)).isPresent();
		then(context.findContainerSizeResolver(path1)).isPresent();
		then(context.findContainerSizeResolver(path99)).isPresent();
	}

	@Test
	void multipleResolversShouldReturnLastMatch() {
		// given
		PathResolverContext context = PathResolverContext.builder()
			.addContainerSizeResolver("$.items", 10)
			.addContainerSizeResolver("$.items", 20)  // second one for same pattern
			.build();

		PathExpression path = PathExpression.root().child("items");

		// when
		Optional<ContainerSizeResolver> resolver = context.findContainerSizeResolver(path);

		// then
		then(resolver).isPresent();
		then(resolver.get().resolveContainerSize(null)).isEqualTo(20);  // last match wins
	}

	@Test
	void differentPatternsForDifferentLevels() {
		// given
		PathResolverContext context = PathResolverContext.builder()
			.addContainerSizeResolver("$.items", 3)      // outer list
			.addContainerSizeResolver("$.items[*]", 5)   // inner lists
			.build();

		PathExpression outerPath = PathExpression.root().child("items");
		PathExpression innerPath0 = PathExpression.root().child("items").index(0);
		PathExpression innerPath1 = PathExpression.root().child("items").index(1);

		// when
		Optional<ContainerSizeResolver> outerResolver = context.findContainerSizeResolver(outerPath);
		Optional<ContainerSizeResolver> innerResolver0 = context.findContainerSizeResolver(innerPath0);
		Optional<ContainerSizeResolver> innerResolver1 = context.findContainerSizeResolver(innerPath1);

		// then
		then(outerResolver).isPresent();
		then(outerResolver.get().resolveContainerSize(null)).isEqualTo(3);

		then(innerResolver0).isPresent();
		then(innerResolver0.get().resolveContainerSize(null)).isEqualTo(5);

		then(innerResolver1).isPresent();
		then(innerResolver1.get().resolveContainerSize(null)).isEqualTo(5);
	}

	@Test
	void nestedWildcardPattern() {
		// given
		PathResolverContext context = PathResolverContext.builder()
			.addContainerSizeResolver("$.items[*][*]", 2)
			.build();

		PathExpression path = PathExpression.root()
			.child("items")
			.index(0)
			.index(0);

		// when
		Optional<ContainerSizeResolver> resolver = context.findContainerSizeResolver(path);

		// then
		then(resolver).isPresent();
		then(resolver.get().resolveContainerSize(null)).isEqualTo(2);
	}

	@Test
	void complexNestedPattern() {
		// given
		PathResolverContext context = PathResolverContext.builder()
			.addContainerSizeResolver("$.userOrders", 10)
			.addContainerSizeResolver("$.userOrders[*]", 3)
			.addContainerSizeResolver("$.userOrders[*][*].items", 5)
			.build();

		PathExpression mapPath = PathExpression.root().child("userOrders");
		PathExpression orderListPath = PathExpression.root().child("userOrders").index(0);
		PathExpression itemsPath = PathExpression.root()
			.child("userOrders")
			.index(0)
			.index(0)
			.child("items");

		// when & then
		then(context.findContainerSizeResolver(mapPath).get().resolveContainerSize(null)).isEqualTo(10);
		then(context.findContainerSizeResolver(orderListPath).get().resolveContainerSize(null)).isEqualTo(3);
		then(context.findContainerSizeResolver(itemsPath).get().resolveContainerSize(null)).isEqualTo(5);
	}

	@Test
	void addContainerSizeResolverWithCustomResolver() {
		// given
		ContainerSizeResolver customResolver = containerType -> 42;
		PathResolverContext context = PathResolverContext.builder()
			.addContainerSizeResolver("$.items", customResolver)
			.build();

		PathExpression path = PathExpression.root().child("items");

		// when
		Optional<ContainerSizeResolver> resolver = context.findContainerSizeResolver(path);

		// then
		then(resolver).isPresent();
		then(resolver.get().resolveContainerSize(null)).isEqualTo(42);
	}

	@Test
	void findInterfaceResolverReturnsLaterWildcardOverEarlierExactPattern() {
		// given
		InterfaceResolver exact = interfaceType -> null;
		InterfaceResolver wildcard = interfaceType -> null;
		PathResolverContext context = PathResolverContext.builder()
			.addInterfaceResolver("$.items[0]", exact)
			.addInterfaceResolver("$.items[*]", wildcard)
			.build();

		// when
		Optional<InterfaceResolver> actual = context.findInterfaceResolver(PathExpression.of("$.items[0]"));

		// then
		then(actual).containsSame(wildcard);
	}

	@Test
	void findInterfaceResolverReturnsLaterExactOverEarlierWildcardPattern() {
		// given
		InterfaceResolver wildcard = interfaceType -> null;
		InterfaceResolver exact = interfaceType -> null;
		PathResolverContext context = PathResolverContext.builder()
			.addInterfaceResolver("$.items[*]", wildcard)
			.addInterfaceResolver("$.items[0]", exact)
			.build();

		// when
		Optional<InterfaceResolver> matchedExactly = context.findInterfaceResolver(PathExpression.of("$.items[0]"));
		Optional<InterfaceResolver> matchedByWildcard = context.findInterfaceResolver(
			PathExpression.of("$.items[1]")
		);

		// then
		then(matchedExactly).containsSame(exact);
		then(matchedByWildcard).containsSame(wildcard);
	}

	@Test
	void findGenericTypeResolverReturnsLastOfSameExactPattern() {
		// given
		GenericTypeResolver first = jvmType -> null;
		GenericTypeResolver last = jvmType -> null;
		PathResolverContext context = PathResolverContext.builder()
			.addGenericTypeResolver("$.data", first)
			.addGenericTypeResolver("$.data", last)
			.build();

		// when
		Optional<GenericTypeResolver> actual = context.findGenericTypeResolver(PathExpression.of("$.data"));

		// then
		then(actual).containsSame(last);
	}

	@Test
	void findGenericTypeResolverMatchesTypeSelectorPatternWithSubtype() {
		// given
		GenericTypeResolver exact = jvmType -> null;
		GenericTypeResolver typed = jvmType -> null;
		PathResolverContext context = PathResolverContext.builder()
			.addGenericTypeResolver("$.data", exact)
			.addGenericTypeResolver(
				new PathGenericTypeResolver(PathExpression.root().child("data").type(CharSequence.class, false), typed)
			)
			.build();

		// when
		Optional<GenericTypeResolver> actual = context.findGenericTypeResolver(
			PathExpression.root().child("data").type(String.class)
		);

		// then
		then(actual).containsSame(typed);
	}

	@Test
	void contextWithInterfaceResolverHasPathSpecificResolvers() {
		// given
		PathResolverContext context = PathResolverContext.builder()
			.addInterfaceResolver("$.items", interfaceType -> null)
			.build();

		// when
		boolean actual = context.hasPathSpecificResolvers();

		// then
		then(actual).isTrue();
	}

	@Test
	void contextWithGenericTypeResolverHasPathSpecificResolvers() {
		// given
		PathResolverContext context = PathResolverContext.builder()
			.addGenericTypeResolver("$.data", jvmType -> null)
			.build();

		// when
		boolean actual = context.hasPathSpecificResolvers();

		// then
		then(actual).isTrue();
	}

	@Test
	void contextWithOnlyContainerSizeResolverHasNoPathSpecificResolvers() {
		// given
		PathResolverContext context = PathResolverContext.builder()
			.addContainerSizeResolver("$.items", 1)
			.build();

		// when
		boolean actual = context.hasPathSpecificResolvers();

		// then
		then(actual).isFalse();
	}
}
