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

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * Selects the node a sample starts from, by its position alone: what the sampled builder itself declared applies
 * there. It wins over every defined scope.
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public final class RootScopeSelector implements ScopeSelector {
	static final RootScopeSelector INSTANCE = new RootScopeSelector();

	private RootScopeSelector() {
	}

	@Override
	public boolean selects(JvmNode node, int depth, Function<JvmNode, Property> propertyOf) {
		return depth == 0;
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
		return "scope:root";
	}

	@Override
	public String toString() {
		return "root";
	}
}
