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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.adapter.converter.PredicatePathConverter;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.NodeAllElementPredicate;
import com.navercorp.fixturemonkey.tree.NodeElementPredicate;
import com.navercorp.fixturemonkey.tree.NodeKeyPredicate;
import com.navercorp.fixturemonkey.tree.NodeValuePredicate;
import com.navercorp.fixturemonkey.tree.PropertyNameNodePredicate;
import com.navercorp.fixturemonkey.tree.StartNodePredicate;
import com.navercorp.objectfarm.api.expression.PathExpression;

class PredicatePathConverterTest {

	@Property
	void startNodePredicateConvertsToRoot() {
		List<NextNodePredicate> predicates = Collections.singletonList(StartNodePredicate.INSTANCE);

		String expression = PredicatePathConverter.toExpression(predicates);

		assertThat(expression).isEqualTo("$");
	}

	@Property
	void propertyNamePredicateConvertsToPropertyName() {
		List<NextNodePredicate> predicates = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("items")
		);

		String expression = PredicatePathConverter.toExpression(predicates);

		assertThat(expression).isEqualTo("$.items");
	}

	@Property
	void nodeAllElementPredicateConvertsToWildcard() {
		List<NextNodePredicate> predicates = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("items"),
			new NodeAllElementPredicate()
		);

		String expression = PredicatePathConverter.toExpression(predicates);

		assertThat(expression).isEqualTo("$.items[*]");
	}

	@Property
	void nodeElementPredicateConvertsToIndex() {
		List<NextNodePredicate> predicates = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("items"),
			new NodeElementPredicate(2)
		);

		String expression = PredicatePathConverter.toExpression(predicates);

		assertThat(expression).isEqualTo("$.items[2]");
	}

	@Property
	void nodeKeyPredicateConvertsToKey() {
		List<NextNodePredicate> predicates = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("map"),
			new NodeElementPredicate(0),
			new NodeKeyPredicate()
		);

		String expression = PredicatePathConverter.toExpression(predicates);

		assertThat(expression).isEqualTo("$.map[0][key]");
	}

	@Property
	void nodeValuePredicateConvertsToValue() {
		List<NextNodePredicate> predicates = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("map"),
			new NodeElementPredicate(0),
			new NodeValuePredicate()
		);

		String expression = PredicatePathConverter.toExpression(predicates);

		assertThat(expression).isEqualTo("$.map[0][value]");
	}

	@Property
	void complexNestedPathConvertsCorrectly() {
		List<NextNodePredicate> predicates = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("orders"),
			new NodeAllElementPredicate(),
			new PropertyNameNodePredicate("items"),
			new NodeElementPredicate(0),
			new PropertyNameNodePredicate("name")
		);

		String expression = PredicatePathConverter.toExpression(predicates);

		assertThat(expression).isEqualTo("$.orders[*].items[0].name");
	}

	@Property
	void convertedExpressionCreatesValidPattern() {
		List<NextNodePredicate> predicates = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("items"),
			new NodeAllElementPredicate()
		);

		PathExpression pathExpression = PredicatePathConverter.convert(predicates);

		assertThat(pathExpression).isNotNull();
		assertThat(pathExpression.toExpression()).isEqualTo("$.items[*]");
	}

	@Property
	void wildcardPropertyNameConvertsToFieldWildcard() {
		List<NextNodePredicate> predicates = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("*")
		);

		String expression = PredicatePathConverter.toExpression(predicates);

		assertThat(expression).isEqualTo("$.*");
	}
}
