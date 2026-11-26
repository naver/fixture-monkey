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

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.InterfaceResolver;

class PathResolverIndexTest {

	@Test
	void pathOfNamesIndicesAndKeysIsConcrete() {
		// given
		PathExpression path = PathExpression.root().child("map").index(0).key();

		// when
		boolean actual = PathResolverIndex.isConcrete(path);

		// then
		then(actual).isTrue();
	}

	@Test
	void pathOfValueIsConcrete() {
		// given
		PathExpression path = PathExpression.root().child("map").index(0).value();

		// when
		boolean actual = PathResolverIndex.isConcrete(path);

		// then
		then(actual).isTrue();
	}

	@Test
	void nameWildcardPathIsNotConcrete() {
		// given
		PathExpression path = PathExpression.root().child("*");

		// when
		boolean actual = PathResolverIndex.isConcrete(path);

		// then
		then(actual).isFalse();
	}

	@Test
	void wildcardPathIsNotConcrete() {
		// given
		PathExpression path = PathExpression.of("$.items[*]");

		// when
		boolean actual = PathResolverIndex.isConcrete(path);

		// then
		then(actual).isFalse();
	}

	@Test
	void indexUnionPathIsNotConcrete() {
		// given
		PathExpression path = PathExpression.of("$.items[0,1]");

		// when
		boolean actual = PathResolverIndex.isConcrete(path);

		// then
		then(actual).isFalse();
	}

	@Test
	void typeSelectorPathIsNotConcrete() {
		// given
		PathExpression path = PathExpression.root().child("data").type(CharSequence.class, false);

		// when
		boolean actual = PathResolverIndex.isConcrete(path);

		// then
		then(actual).isFalse();
	}

	@Test
	void findLastMatchingMatchesResolverOfOtherImplementation() {
		// given
		InterfaceResolver customizer = interfaceType -> null;
		PathResolver<InterfaceResolver> resolver = new PathResolver<InterfaceResolver>() {
			@Override
			public boolean matches(PathExpression path) {
				return true;
			}

			@Override
			public InterfaceResolver getCustomizer() {
				return customizer;
			}
		};
		PathResolverIndex<InterfaceResolver> index = new PathResolverIndex<>(Collections.singletonList(resolver));

		// when
		InterfaceResolver actual = index.findLastMatching(PathExpression.of("$.items[0]"));

		// then
		then(actual).isSameAs(customizer);
	}
}
