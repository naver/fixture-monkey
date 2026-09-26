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

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.objectfarm.api.expression.TypeSelector;
import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * Selects the nodes of a type, or also of its subtypes, by the node's type alone: the type it is declared with or
 * the implementation chosen for it.
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public final class TypeScopeSelector implements ScopeSelector {
	private final TypeSelector typeSelector;

	TypeScopeSelector(Class<?> type, boolean exact) {
		this.typeSelector = new TypeSelector(type, exact);
	}

	@Override
	public boolean selects(JvmNode node, int depth, Function<JvmNode, Property> propertyOf) {
		return typeSelector.matchesType(node.getDeclaredType().getRawType())
			|| typeSelector.matchesType(node.getConcreteType().getRawType());
	}

	@Override
	public boolean dependsOnAncestors() {
		return false;
	}

	@Override
	public boolean selectsEveryNodeOf(Class<?> type) {
		return typeSelector.matchesType(type);
	}

	@Override
	public boolean mayAppearAmong(Set<Class<?>> types) {
		Class<?> type = typeSelector.getTargetType();
		if (types.contains(type)) {
			return true;
		}
		return !typeSelector.isExact() && types.stream().anyMatch(type::isAssignableFrom);
	}

	@Override
	public String toExpression() {
		return "scope:" + typeSelector.toExpression();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TypeScopeSelector)) {
			return false;
		}
		return typeSelector.equals(((TypeScopeSelector)obj).typeSelector);
	}

	@Override
	public int hashCode() {
		return typeSelector.hashCode();
	}

	@Override
	public String toString() {
		String name = typeSelector.getTargetType().getSimpleName();
		return typeSelector.isExact() ? name : "assignable:" + name;
	}
}
