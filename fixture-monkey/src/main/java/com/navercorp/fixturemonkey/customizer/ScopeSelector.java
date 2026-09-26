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

package com.navercorp.fixturemonkey.customizer;

import java.util.Set;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.ExactTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * Selects the nodes that scope a directive: the directive's paths are relative to a selected node and apply to
 * that node and what is inside it.
 * <p>
 * Every selected node is a scope of its own, at whatever depth it sits. Where scopes nest and both direct the
 * same path, the outer scope wins, since it describes its whole object including what is inside.
 *
 * @see TypeScopeSelector
 * @see MatcherScopeSelector
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public interface ScopeSelector {
	/**
	 * Returns the root scope: the node a sample starts from, where what the sampled builder itself declared applies.
	 *
	 * @return the root scope
	 */
	static ScopeSelector root() {
		return RootScopeSelector.INSTANCE;
	}

	/**
	 * Returns the scope of the nodes {@code matcher} matches: a type matcher selects by the node's type alone, any
	 * other matcher is given the node's property.
	 *
	 * @param matcher the matcher
	 * @return the scope
	 */
	static ScopeSelector of(Matcher matcher) {
		if (matcher instanceof ExactTypeMatcher) {
			return new TypeScopeSelector(((ExactTypeMatcher)matcher).getType(), true);
		}
		if (matcher instanceof AssignableTypeMatcher) {
			return new TypeScopeSelector(((AssignableTypeMatcher)matcher).getAnchorType(), false);
		}
		return new MatcherScopeSelector(matcher);
	}

	/**
	 * Returns whether {@code node} is selected.
	 *
	 * @param node       the node
	 * @param depth      how many segments lead from the node a sample starts from down to {@code node}
	 * @param propertyOf the property of a node, for a scope that selects by it
	 * @return true when the node is selected
	 */
	boolean selects(JvmNode node, int depth, Function<JvmNode, Property> propertyOf);

	/**
	 * Returns whether which nodes are selected depends on where they sit.
	 *
	 * @return {@code false} when the node's type alone decides
	 */
	boolean dependsOnAncestors();

	/**
	 * Returns whether every node of {@code type} is selected wherever it sits, so a directive scoped to this
	 * scope applies to all of them as it would without a scope.
	 *
	 * @param type the type of the nodes
	 * @return true when every node of the type is selected
	 */
	boolean selectsEveryNodeOf(Class<?> type);

	/**
	 * Returns whether this scope may select a node when the sample's tree has only nodes of {@code types}; a scope
	 * that cannot is left out of the sample.
	 *
	 * @param types the types that can appear in the sample's tree
	 * @return false only when no node of these types can be selected
	 */
	boolean mayAppearAmong(Set<Class<?>> types);

	/**
	 * Returns how this scope is written in a trace, as {@code scope:<type>}.
	 *
	 * @return the expression of this scope
	 */
	String toExpression();
}
