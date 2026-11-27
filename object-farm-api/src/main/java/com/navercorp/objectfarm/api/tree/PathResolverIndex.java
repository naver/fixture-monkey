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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.IndexSelector;
import com.navercorp.objectfarm.api.expression.KeySelector;
import com.navercorp.objectfarm.api.expression.NameSelector;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.expression.Selector;
import com.navercorp.objectfarm.api.expression.ValueSelector;
import com.navercorp.objectfarm.api.node.NodeCustomizer;

final class PathResolverIndex<T extends NodeCustomizer> {
	private final List<PathResolver<T>> resolvers;
	private final Map<PathExpression, Integer> lastPositionByExactPath = new HashMap<>();
	private final List<Integer> positionsToMatch = new ArrayList<>();

	PathResolverIndex(List<PathResolver<T>> resolvers) {
		this.resolvers = resolvers;
		for (int position = 0; position < resolvers.size(); position++) {
			PathExpression pattern = getPattern(resolvers.get(position));
			if (pattern != null && isConcrete(pattern)) {
				lastPositionByExactPath.put(pattern, position);
			} else {
				positionsToMatch.add(position);
			}
		}
	}

	boolean isEmpty() {
		return resolvers.isEmpty();
	}

	@Nullable
	T findLastMatching(PathExpression path) {
		if (!isConcrete(path)) {
			return findLastMatchingOneByOne(path);
		}

		Integer exactPosition = lastPositionByExactPath.get(path);
		int lastPosition = exactPosition != null ? exactPosition : -1;
		for (int i = positionsToMatch.size() - 1; i >= 0; i--) {
			int position = positionsToMatch.get(i);
			if (position < lastPosition) {
				break;
			}
			if (resolvers.get(position).matches(path)) {
				lastPosition = position;
				break;
			}
		}
		return lastPosition < 0 ? null : resolvers.get(lastPosition).getCustomizer();
	}

	static boolean isConcrete(PathExpression path) {
		for (Segment segment : path.getSegments()) {
			if (!segment.isSingleSelector() || !matchesOnlyItself(segment.getFirstSelector())) {
				return false;
			}
		}
		return true;
	}

	private static boolean matchesOnlyItself(Selector selector) {
		if (selector instanceof NameSelector) {
			return !"*".equals(((NameSelector)selector).getName());
		}
		return selector instanceof IndexSelector
			|| selector instanceof KeySelector
			|| selector instanceof ValueSelector;
	}

	@Nullable
	private T findLastMatchingOneByOne(PathExpression path) {
		for (int position = resolvers.size() - 1; position >= 0; position--) {
			PathResolver<T> resolver = resolvers.get(position);
			if (resolver.matches(path)) {
				return resolver.getCustomizer();
			}
		}
		return null;
	}

	@Nullable
	private static PathExpression getPattern(PathResolver<?> resolver) {
		if (resolver instanceof PathInterfaceResolver) {
			return ((PathInterfaceResolver)resolver).getPattern();
		}
		if (resolver instanceof PathGenericTypeResolver) {
			return ((PathGenericTypeResolver)resolver).getPattern();
		}
		return null;
	}
}
