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

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.tree.ApplyStrictModeResolver;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.NodeResolver;

public final class StrictModeMonkeyExpressionFactory implements MonkeyExpressionFactory {
	private final MonkeyExpressionFactory delegate;
	private final PropertyNameResolver propertyNameResolver;

	public StrictModeMonkeyExpressionFactory(
		MonkeyExpressionFactory delegate, PropertyNameResolver propertyNameResolver
	) {
		this.delegate = delegate;
		this.propertyNameResolver = propertyNameResolver;
	}

	@Override
	public MonkeyExpression from(String expression) {
		return from(expression, null);
	}

	@Override
	public MonkeyExpression from(String expression, @Nullable Class<?> rootClass) {
		MonkeyExpression monkeyExpression = delegate.from(expression);
		return new StrictModeMonkeyExpression(monkeyExpression, rootClass, propertyNameResolver);
	}

	private static final class StrictModeMonkeyExpression implements MonkeyExpression {
		private final MonkeyExpression delegate;
		private final @Nullable Class<?> rootClass;
		private final PropertyNameResolver propertyNameResolver;

		public StrictModeMonkeyExpression(
			MonkeyExpression delegate, @Nullable Class<?> rootClass, PropertyNameResolver propertyNameResolver
		) {
			this.delegate = delegate;
			this.rootClass = rootClass;
			this.propertyNameResolver = propertyNameResolver;
		}

		@Override
		public NodeResolver toNodeResolver() {
			return new ApplyStrictModeResolver(delegate.toNodeResolver());
		}

		@Override
		public List<NextNodePredicate> toNextNodePredicate() {
			if (rootClass == null) {
				return delegate.toNextNodePredicate();
			}
			List<NextNodePredicate> predicates = delegate.toNextNodePredicate();
			return new StrictModeNextNodePredicateContainer(predicates, rootClass, propertyNameResolver);
		}
	}
}
