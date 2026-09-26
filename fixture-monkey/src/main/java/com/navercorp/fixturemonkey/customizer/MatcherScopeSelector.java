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
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * Selects the nodes whose property a {@link Matcher} matches. The property may depend on the node's ancestors, so
 * the same type can be selected at one position and not at another.
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public final class MatcherScopeSelector implements ScopeSelector {
	private final Matcher matcher;

	MatcherScopeSelector(Matcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public boolean selects(JvmNode node, int depth, Function<JvmNode, Property> propertyOf) {
		return matcher.match(propertyOf.apply(node));
	}

	@Override
	public boolean dependsOnAncestors() {
		return true;
	}

	@Override
	public boolean selectsEveryNodeOf(Class<?> type) {
		return false;
	}

	@Override
	public boolean mayAppearAmong(Set<Class<?>> types) {
		return true;
	}

	@Override
	public String toExpression() {
		return "scope:matcher";
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MatcherScopeSelector)) {
			return false;
		}
		return matcher.equals(((MatcherScopeSelector)obj).matcher);
	}

	@Override
	public int hashCode() {
		return matcher.hashCode();
	}

	@Override
	public String toString() {
		return "matcher:" + matcher;
	}
}
