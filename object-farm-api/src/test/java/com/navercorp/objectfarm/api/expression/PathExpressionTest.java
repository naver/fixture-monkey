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

package com.navercorp.objectfarm.api.expression;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import org.junit.jupiter.api.Test;

class PathExpressionTest {

	@Test
	void rootPathShouldReturnDollarSign() {
		// given
		PathExpression root = PathExpression.root();

		// when
		String expression = root.toExpression();

		// then
		then(expression).isEqualTo("$");
	}

	@Test
	void childNameShouldAddNameSelector() {
		// given
		PathExpression root = PathExpression.root();

		// when
		PathExpression path = root.child("items");

		// then
		then(path.toExpression()).isEqualTo("$.items");
		then(path.getSegments()).hasSize(1);
		then(path.getSegments().get(0).getFirstSelector()).isInstanceOf(NameSelector.class);
	}

	@Test
	void indexShouldAddIndexSelector() {
		// given
		PathExpression path = PathExpression.root().child("items");

		// when
		PathExpression indexedPath = path.index(0);

		// then
		then(indexedPath.toExpression()).isEqualTo("$.items[0]");
		then(indexedPath.getSegments()).hasSize(2);
		then(indexedPath.getSegments().get(1).getFirstSelector()).isInstanceOf(IndexSelector.class);
	}

	@Test
	void multipleAppendsShouldChainCorrectly() {
		// given
		PathExpression root = PathExpression.root();

		// when
		PathExpression path = root
			.child("items")
			.index(0)
			.index(2)
			.child("name");

		// then
		then(path.toExpression()).isEqualTo("$.items[0][2].name");
		then(path.getSegments()).hasSize(4);
	}

	@Test
	void appendShouldBeImmutable() {
		// given
		PathExpression original = PathExpression.root().child("items");

		// when
		PathExpression extended = original.index(0);

		// then
		then(original.toExpression()).isEqualTo("$.items");
		then(extended.toExpression()).isEqualTo("$.items[0]");
	}

	@Test
	void equalPathsShouldBeEqual() {
		// given
		PathExpression path1 = PathExpression.root().child("items").index(0);
		PathExpression path2 = PathExpression.root().child("items").index(0);

		// then
		then(path1).isEqualTo(path2);
		then(path1.hashCode()).isEqualTo(path2.hashCode());
	}

	@Test
	void differentPathsShouldNotBeEqual() {
		// given
		PathExpression path1 = PathExpression.root().child("items").index(0);
		PathExpression path2 = PathExpression.root().child("items").index(1);

		// then
		then(path1).isNotEqualTo(path2);
	}

	@Test
	void childWithNullNameShouldThrowException() {
		// given
		PathExpression root = PathExpression.root();

		// when & then
		thenThrownBy(() -> root.child(null))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void toStringShouldReturnExpression() {
		// given
		PathExpression path = PathExpression.root().child("items").index(0);

		// when
		String str = path.toString();

		// then
		then(str).isEqualTo("$.items[0]");
	}

	@Test
	void rootContainerIndexPath() {
		// given
		PathExpression root = PathExpression.root();

		// when
		PathExpression path = root.index(0);

		// then
		then(path.toExpression()).isEqualTo("$[0]");
	}

	@Test
	void keyShouldAddKeySelector() {
		// given
		PathExpression path = PathExpression.root().child("map").index(0);

		// when
		PathExpression keyPath = path.key();

		// then
		then(keyPath.toExpression()).isEqualTo("$.map[0][key]");
		then(keyPath.getSegments()).hasSize(3);
		then(keyPath.getSegments().get(2).getFirstSelector()).isInstanceOf(KeySelector.class);
	}

	@Test
	void valueShouldAddValueSelector() {
		// given
		PathExpression path = PathExpression.root().child("map").index(0);

		// when
		PathExpression valuePath = path.value();

		// then
		then(valuePath.toExpression()).isEqualTo("$.map[0][value]");
		then(valuePath.getSegments()).hasSize(3);
		then(valuePath.getSegments().get(2).getFirstSelector()).isInstanceOf(ValueSelector.class);
	}

	@Test
	void mapEntryPathWithNestedContainer() {
		// given
		PathExpression root = PathExpression.root();

		// when - Map<String, List<Order>> userOrders
		PathExpression path = root
			.child("userOrders")
			.index(0)
			.value()
			.index(0);

		// then
		then(path.toExpression()).isEqualTo("$.userOrders[0][value][0]");
	}

	@Test
	void nestedMapPath() {
		// given
		PathExpression root = PathExpression.root();

		// when - Map<String, Map<Integer, Order>> data
		PathExpression path = root
			.child("data")
			.index(0)
			.value()
			.index(0)
			.key();

		// then
		then(path.toExpression()).isEqualTo("$.data[0][value][0][key]");
	}

	@Test
	void keyAndValuePathsShouldNotBeEqual() {
		// given
		PathExpression base = PathExpression.root().child("map").index(0);
		PathExpression keyPath = base.key();
		PathExpression valuePath = base.value();

		// then
		then(keyPath).isNotEqualTo(valuePath);
	}

	@Test
	void sameKeyPathsShouldBeEqual() {
		// given
		PathExpression path1 = PathExpression.root().child("map").index(0).key();
		PathExpression path2 = PathExpression.root().child("map").index(0).key();

		// then
		then(path1).isEqualTo(path2);
		then(path1.hashCode()).isEqualTo(path2.hashCode());
	}

	// Parsing tests

	@Test
	void parseRootExpression() {
		// given
		String expression = "$";

		// when
		PathExpression path = PathExpression.of(expression);

		// then
		then(path).isEqualTo(PathExpression.root());
		then(path.toExpression()).isEqualTo("$");
	}

	@Test
	void parseSimpleNameExpression() {
		// given
		String expression = "$.items";

		// when
		PathExpression path = PathExpression.of(expression);

		// then
		then(path.toExpression()).isEqualTo(expression);
		then(path.getSegments()).hasSize(1);
		then(path.getSegments().get(0).getFirstSelector()).isInstanceOf(NameSelector.class);
	}

	@Test
	void parseNameWithIndexExpression() {
		// given
		String expression = "$.items[0]";

		// when
		PathExpression path = PathExpression.of(expression);

		// then
		then(path.toExpression()).isEqualTo(expression);
		then(path.getSegments()).hasSize(2);
	}

	@Test
	void parseKeyExpression() {
		// given
		String expression = "$.map[0][key]";

		// when
		PathExpression path = PathExpression.of(expression);

		// then
		then(path.toExpression()).isEqualTo(expression);
		then(path.getSegments()).hasSize(3);
		then(path.getSegments().get(2).getFirstSelector()).isInstanceOf(KeySelector.class);
	}

	@Test
	void parseValueExpression() {
		// given
		String expression = "$.map[0][value]";

		// when
		PathExpression path = PathExpression.of(expression);

		// then
		then(path.toExpression()).isEqualTo(expression);
		then(path.getSegments()).hasSize(3);
		then(path.getSegments().get(2).getFirstSelector()).isInstanceOf(ValueSelector.class);
	}

	// Wildcard and Pattern Matching tests

	@Test
	void parseWildcardPattern() {
		// given
		String expression = "$.items[*]";

		// when
		PathExpression pattern = PathExpression.of(expression);

		// then
		then(pattern.getSegments()).hasSize(2);
		then(pattern.getSegments().get(0).getFirstSelector()).isInstanceOf(NameSelector.class);
		then(pattern.getSegments().get(1).getFirstSelector()).isInstanceOf(WildcardSelector.class);
	}

	@Test
	void parseNestedWildcardPattern() {
		// given
		String expression = "$.items[*][*]";

		// when
		PathExpression pattern = PathExpression.of(expression);

		// then
		then(pattern.getSegments()).hasSize(3);
		then(pattern.getSegments().get(0).getFirstSelector()).isInstanceOf(NameSelector.class);
		then(pattern.getSegments().get(1).getFirstSelector()).isInstanceOf(WildcardSelector.class);
		then(pattern.getSegments().get(2).getFirstSelector()).isInstanceOf(WildcardSelector.class);
	}

	@Test
	void parseRootWildcardPattern() {
		// given
		String expression = "$[*]";

		// when
		PathExpression pattern = PathExpression.of(expression);

		// then
		then(pattern.getSegments()).hasSize(1);
		then(pattern.getSegments().get(0).getFirstSelector()).isInstanceOf(WildcardSelector.class);
	}

	@Test
	void hasWildcardShouldReturnTrueForPatterns() {
		// when & then
		then(PathExpression.of("$.items[*]").hasWildcard()).isTrue();
		then(PathExpression.of("$.items[*][*]").hasWildcard()).isTrue();
		then(PathExpression.of("$[*]").hasWildcard()).isTrue();
	}

	@Test
	void hasWildcardShouldReturnFalseForConcretePaths() {
		// when & then
		then(PathExpression.of("$.items[0]").hasWildcard()).isFalse();
		then(PathExpression.of("$.items").hasWildcard()).isFalse();
		then(PathExpression.root().hasWildcard()).isFalse();
	}

	@Test
	void matchExactPath() {
		// given
		PathExpression pattern = PathExpression.of("$.items");
		PathExpression path = PathExpression.root().child("items");

		// when & then
		then(pattern.matches(path)).isTrue();
	}

	@Test
	void matchWithIndex() {
		// given
		PathExpression pattern = PathExpression.of("$.items[0]");
		PathExpression path = PathExpression.root().child("items").index(0);

		// when & then
		then(pattern.matches(path)).isTrue();
	}

	@Test
	void wildcardMatchesAnyIndex() {
		// given
		PathExpression pattern = PathExpression.of("$.items[*]");

		// when & then
		then(pattern.matches(PathExpression.root().child("items").index(0))).isTrue();
		then(pattern.matches(PathExpression.root().child("items").index(5))).isTrue();
		then(pattern.matches(PathExpression.root().child("items").index(100))).isTrue();
	}

	@Test
	void wildcardDoesNotMatchName() {
		// given
		PathExpression pattern = PathExpression.of("$.items[*]");
		PathExpression path = PathExpression.root().child("items").child("name");

		// when & then
		then(pattern.matches(path)).isFalse();
	}

	@Test
	void nestedWildcardMatching() {
		// given
		PathExpression pattern = PathExpression.of("$.items[*][*]");

		// when & then
		then(pattern.matches(PathExpression.root().child("items").index(0).index(0))).isTrue();
		then(pattern.matches(PathExpression.root().child("items").index(3).index(7))).isTrue();
	}

	@Test
	void pathLengthMismatchShouldNotMatch() {
		// given
		PathExpression pattern = PathExpression.of("$.items[*]");

		// when & then
		then(pattern.matches(PathExpression.root().child("items"))).isFalse();
		then(pattern.matches(PathExpression.root().child("items").index(0).child("name"))).isFalse();
	}

	@Test
	void differentNameShouldNotMatch() {
		// given
		PathExpression pattern = PathExpression.of("$.items");
		PathExpression path = PathExpression.root().child("other");

		// when & then
		then(pattern.matches(path)).isFalse();
	}

	@Test
	void differentIndexShouldNotMatch() {
		// given
		PathExpression pattern = PathExpression.of("$.items[0]");
		PathExpression path = PathExpression.root().child("items").index(1);

		// when & then
		then(pattern.matches(path)).isFalse();
	}

	@Test
	void wildcardMatchesKeySelector() {
		// given
		PathExpression pattern = PathExpression.of("$.map[*][*]");
		PathExpression keyPath = PathExpression.root().child("map").index(0).key();

		// when & then
		then(pattern.matches(keyPath)).isTrue();
	}

	@Test
	void wildcardMatchesValueSelector() {
		// given
		PathExpression pattern = PathExpression.of("$.map[*][*]");
		PathExpression valuePath = PathExpression.root().child("map").index(0).value();

		// when & then
		then(pattern.matches(valuePath)).isTrue();
	}

	@Test
	void keyPatternMatchesKeySelector() {
		// given
		PathExpression pattern = PathExpression.of("$.map[*][key]");
		PathExpression keyPath = PathExpression.root().child("map").index(0).key();

		// when & then
		then(pattern.matches(keyPath)).isTrue();
	}

	@Test
	void keyPatternDoesNotMatchValueSelector() {
		// given
		PathExpression pattern = PathExpression.of("$.map[*][key]");
		PathExpression valuePath = PathExpression.root().child("map").index(0).value();

		// when & then
		then(pattern.matches(valuePath)).isFalse();
	}

	@Test
	void valuePatternMatchesValueSelector() {
		// given
		PathExpression pattern = PathExpression.of("$.map[*][value]");
		PathExpression valuePath = PathExpression.root().child("map").index(0).value();

		// when & then
		then(pattern.matches(valuePath)).isTrue();
	}

	@Test
	void valuePatternDoesNotMatchKeySelector() {
		// given
		PathExpression pattern = PathExpression.of("$.map[*][value]");
		PathExpression keyPath = PathExpression.root().child("map").index(0).key();

		// when & then
		then(pattern.matches(keyPath)).isFalse();
	}

	// Union tests

	@Test
	void parseIndexUnion() {
		// given
		String expression = "$.items[0,1,2]";

		// when
		PathExpression pattern = PathExpression.of(expression);

		// then
		then(pattern.getSegments()).hasSize(2);
		then(pattern.getSegments().get(1).getSelectors()).hasSize(3);
		then(pattern.getSegments().get(1).getSelectors().get(0)).isInstanceOf(IndexSelector.class);
		then(pattern.getSegments().get(1).getSelectors().get(1)).isInstanceOf(IndexSelector.class);
		then(pattern.getSegments().get(1).getSelectors().get(2)).isInstanceOf(IndexSelector.class);
	}

	@Test
	void parseKeyValueUnion() {
		// given
		String expression = "$.map[*][key,value]";

		// when
		PathExpression pattern = PathExpression.of(expression);

		// then
		then(pattern.getSegments()).hasSize(3);
		Segment last = pattern.getSegments().get(2);
		then(last.getSelectors()).hasSize(2);
		then(last.getSelectors().get(0)).isInstanceOf(KeySelector.class);
		then(last.getSelectors().get(1)).isInstanceOf(ValueSelector.class);
	}

	@Test
	void indexUnionMatchesAnyIndex() {
		// given
		PathExpression pattern = PathExpression.of("$.items[0,1,2]");

		// when & then
		then(pattern.matches(PathExpression.root().child("items").index(0))).isTrue();
		then(pattern.matches(PathExpression.root().child("items").index(1))).isTrue();
		then(pattern.matches(PathExpression.root().child("items").index(2))).isTrue();
		then(pattern.matches(PathExpression.root().child("items").index(3))).isFalse();
	}

	@Test
	void keyValueUnionMatchesBoth() {
		// given
		PathExpression pattern = PathExpression.of("$.map[*][key,value]");

		// when & then
		then(pattern.matches(PathExpression.root().child("map").index(0).key())).isTrue();
		then(pattern.matches(PathExpression.root().child("map").index(0).value())).isTrue();
		then(pattern.matches(PathExpression.root().child("map").index(0).index(1))).isFalse();
	}

	@Test
	void wildcardMethodShouldAddWildcardSegment() {
		// given
		PathExpression path = PathExpression.root().child("items");

		// when
		PathExpression withWildcard = path.wildcard();

		// then
		then(withWildcard.toExpression()).isEqualTo("$.items[*]");
		then(withWildcard.hasWildcard()).isTrue();
	}

	@Test
	void parseWithoutDollarShouldWork() {
		// given
		String expression = "items[0]";

		// when
		PathExpression path = PathExpression.of(expression);

		// then - should be equivalent to $.items[0]
		PathExpression withDollar = PathExpression.of("$.items[0]");
		then(path).isEqualTo(withDollar);
	}

	@Test
	void parsedPathEqualsBuiltPath() {
		// given
		PathExpression built = PathExpression.root().child("items").index(0).child("name");
		PathExpression parsed = PathExpression.of("$.items[0].name");

		// then
		then(parsed).isEqualTo(built);
	}

	// Utility method tests

	@Test
	void startsWithShouldWorkCorrectly() {
		// given
		PathExpression prefix = PathExpression.root().child("items");
		PathExpression path = PathExpression.root().child("items").index(0);
		PathExpression otherPath = PathExpression.root().child("other").index(0);

		// then
		then(path.startsWith(prefix)).isTrue();
		then(otherPath.startsWith(prefix)).isFalse();
		then(prefix.startsWith(path)).isFalse();
	}

	@Test
	void isChildOfShouldWorkCorrectly() {
		// given
		PathExpression parent = PathExpression.root().child("items");
		PathExpression child = PathExpression.root().child("items").index(0);

		// then
		then(child.isChildOf(parent)).isTrue();
		then(parent.isChildOf(child)).isFalse();
		then(parent.isChildOf(parent)).isFalse();
	}

	@Test
	void isRootShouldWorkCorrectly() {
		// then
		then(PathExpression.root().isRoot()).isTrue();
		then(PathExpression.root().child("items").isRoot()).isFalse();
	}

	@Test
	void depthShouldWorkCorrectly() {
		// then
		then(PathExpression.root().depth()).isEqualTo(0);
		then(PathExpression.root().child("items").depth()).isEqualTo(1);
		then(PathExpression.root().child("items").index(0).depth()).isEqualTo(2);
	}

	@Test
	void compareToShouldWorkCorrectly() {
		// given
		PathExpression pathA = PathExpression.of("$.a");
		PathExpression pathB = PathExpression.of("$.b");
		PathExpression pathAWithIndex = PathExpression.of("$.a[0]");

		// then
		then(pathA.compareTo(pathB)).isLessThan(0);
		then(pathB.compareTo(pathA)).isGreaterThan(0);
		then(pathA.compareTo(pathA)).isEqualTo(0);
		then(pathA.compareTo(pathAWithIndex)).isLessThan(0);
	}
}
