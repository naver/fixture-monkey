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

package com.navercorp.fixturemonkey.projection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.objectfarm.api.expression.IndexSelector;
import com.navercorp.objectfarm.api.expression.NameSelector;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.expression.Selector;
import com.navercorp.objectfarm.api.expression.TypeSelector;
import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * Helpers around {@link LazyValueHolder} resolution and thenApply ancestor lookup.
 *
 * <p>Stateless: every method takes the {@link AssemblyState} (which owns the per-call
 * {@code resolvedLazyCache}) explicitly so the assembler keeps tree-traversal as its sole
 * responsibility.</p>
 */
final class LazyResolver {
	private LazyResolver() {
	}

	static @Nullable Object resolveLazyValue(@Nullable Object value, boolean isFromRegister, AssemblyState state) {
		if (!(value instanceof LazyValueHolder)) {
			return value;
		}
		// Register lazy: evaluate fresh each time (thenApply wraps entire object).
		// User lazy: cached within one sample for consistency (e.g., setLazy with Arbitraries.of()).
		return isFromRegister
			? ((LazyValueHolder)value).getValue()
			: resolveLazyWithCache((LazyValueHolder)value, state);
	}

	static @Nullable Object resolveLazyWithCache(LazyValueHolder holder, AssemblyState state) {
		if (state.resolvedLazyCache.containsKey(holder)) {
			return state.resolvedLazyCache.get(holder);
		}
		Object resolved = holder.getValue();
		if (resolved != null && resolved != LazyValueHolder.RECURSION_BLOCKED) {
			state.resolvedLazyCache.put(holder, resolved);
		}
		return resolved;
	}

	static @Nullable Object resolveThenApplyAncestorValue(
		JvmNode node,
		PathExpression currentPath,
		AssemblyState state
	) {
		if (state.rootTypeSelectors.isEmpty()) {
			return null;
		}

		for (Map.Entry<PathExpression, ValueCandidate> entry : state.rootTypeSelectors) {
			TypeSelector typeSelector = (TypeSelector)entry.getKey().getSegments().get(0).getFirstSelector();

			if (node.getConcreteType() != null && typeSelector.matchesType(node.getConcreteType().getRawType())) {
				Object value = entry.getValue().value;
				if (value instanceof LazyValueHolder) {
					Object resolved = resolveLazyWithCache((LazyValueHolder)value, state);
					if (resolved == LazyValueHolder.RECURSION_BLOCKED) {
						return null;
					}
					return resolved;
				}
				return value;
			}

			List<Segment> pathSegments = currentPath.getSegments();
			for (int pos = pathSegments.size() - 1; pos >= 0; pos--) {
				PathExpression ancestorPath = PathMatcher.buildPathUpTo(currentPath, pos - 1);
				JvmNode ancestorNode = PathMatcher.findNodeForPath(ancestorPath, state);
				if (ancestorNode != null
					&& ancestorNode.getConcreteType() != null
					&& typeSelector.matchesType(ancestorNode.getConcreteType().getRawType())) {
					Object value = entry.getValue().value;
					Object resolved;
					if (value instanceof LazyValueHolder) {
						resolved = resolveLazyWithCache((LazyValueHolder)value, state);
						if (resolved == LazyValueHolder.RECURSION_BLOCKED || resolved == null) {
							return null;
						}
					} else {
						resolved = value;
					}

					Object current = resolved;
					for (int i = pos; i < pathSegments.size() && current != null; i++) {
						Segment pathSeg = pathSegments.get(i);
						Selector selector = pathSeg.getFirstSelector();

						if (selector instanceof NameSelector) {
							current = getFieldValueByName(current, ((NameSelector)selector).getName());
						} else if (selector instanceof IndexSelector) {
							current = getElementAtIndex(current, ((IndexSelector)selector).getIndex());
						} else {
							current = null;
						}
					}
					if (current != null) {
						return current;
					}
				}
			}
		}
		return null;
	}

	static @Nullable Object getFieldValueByName(Object obj, String fieldName) {
		Field field = TypeCache.getFieldsByName(obj.getClass()).get(fieldName);
		if (field == null) {
			return null;
		}
		try {
			return field.get(obj);
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	static @Nullable Object getElementAtIndex(Object container, int index) {
		if (container instanceof List) {
			List<?> list = (List<?>)container;
			if (index >= 0 && index < list.size()) {
				return list.get(index);
			}
			return null;
		}
		if (container.getClass().isArray()) {
			int length = Array.getLength(container);
			if (index >= 0 && index < length) {
				return Array.get(container, index);
			}
			return null;
		}
		return null;
	}
}
