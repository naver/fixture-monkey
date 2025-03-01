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

package com.navercorp.fixturemonkey.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.NodeAllElementPredicate;
import com.navercorp.fixturemonkey.tree.NodeElementPredicate;
import com.navercorp.fixturemonkey.tree.NodeKeyPredicate;
import com.navercorp.fixturemonkey.tree.NodeValuePredicate;
import com.navercorp.fixturemonkey.tree.PropertyNameNodePredicate;
import com.navercorp.fixturemonkey.tree.StartNodePredicate;

/**
 * It is a default implementation of {@link DeclarativeExpression}.
 */
public final class DefaultDeclarativeExpression implements DeclarativeExpression {
	private final List<NextNodePredicate> nestedNextNodePredicates;

	/**
	 * It is for internal use only.
	 * It may be removed or changed in a future release.
	 */
	@Deprecated
	public DefaultDeclarativeExpression() {
		this.nestedNextNodePredicates = Collections.singletonList(StartNodePredicate.INSTANCE);
	}

	DefaultDeclarativeExpression(List<NextNodePredicate> nestedNextNodePredicates) {
		this.nestedNextNodePredicates = nestedNextNodePredicates;
	}

	@Override
	public DefaultDeclarativeExpression property(String propertyName) {
		List<NextNodePredicate> copied = new ArrayList<>(this.nestedNextNodePredicates);
		copied.add(new PropertyNameNodePredicate(propertyName));
		return new DefaultDeclarativeExpression(copied);
	}

	@Override
	public DefaultDeclarativeExpression element(int sequence) {
		List<NextNodePredicate> copied = new ArrayList<>(this.nestedNextNodePredicates);
		copied.add(new NodeElementPredicate(sequence));
		return new DefaultDeclarativeExpression(copied);
	}

	@Override
	public DefaultDeclarativeExpression allElement() {
		List<NextNodePredicate> copied = new ArrayList<>(this.nestedNextNodePredicates);
		copied.add(new NodeAllElementPredicate());
		return new DefaultDeclarativeExpression(copied);
	}

	@Override
	public DefaultDeclarativeExpression key() {
		List<NextNodePredicate> copied = new ArrayList<>(this.nestedNextNodePredicates);
		copied.add(new NodeKeyPredicate());
		return new DefaultDeclarativeExpression(copied);
	}

	@Override
	public DefaultDeclarativeExpression value() {
		List<NextNodePredicate> copied = new ArrayList<>(this.nestedNextNodePredicates);
		copied.add(new NodeValuePredicate());
		return new DefaultDeclarativeExpression(copied);
	}

	@API(since = "1.1.10", status = Status.INTERNAL)
	public DefaultDeclarativeExpression prepend(DefaultDeclarativeExpression parentDeclarativeExpression) {
		List<NextNodePredicate> concat = new ArrayList<>(parentDeclarativeExpression.getNestedNextNodePredicates());
		concat.addAll(this.getNestedNextNodePredicates().stream()
			.filter(it -> !(it instanceof StartNodePredicate))
			.collect(Collectors.toList()));
		return new DefaultDeclarativeExpression(concat);
	}

	@API(since = "1.1.10", status = Status.INTERNAL)
	public List<NextNodePredicate> getNestedNextNodePredicates() {
		return nestedNextNodePredicates;
	}
}
