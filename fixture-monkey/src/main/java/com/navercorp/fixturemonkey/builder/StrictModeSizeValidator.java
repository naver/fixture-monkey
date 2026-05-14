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

package com.navercorp.fixturemonkey.builder;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.objectfarm.api.expression.IndexSelector;
import com.navercorp.objectfarm.api.expression.KeySelector;
import com.navercorp.objectfarm.api.expression.NameSelector;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.expression.Selector;
import com.navercorp.objectfarm.api.expression.ValueSelector;
import com.navercorp.objectfarm.api.expression.WildcardSelector;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.JvmTypes;

/**
 * Validates that a size() path is reachable on a root type. Used by
 * {@link DefaultArbitraryBuilder} when {@code expressionStrictMode} is enabled.
 * <p>
 * Replaces the legacy {@code StrictModeNextNodePredicateContainer} validation that
 * lived inside the deleted {@code MonkeyExpression} chain.
 */
final class StrictModeSizeValidator {
	private StrictModeSizeValidator() {
	}

	static boolean validate(JvmType rootType, PathExpression path) {
		JvmType current = rootType;

		for (Segment segment : path.getSegments()) {
			Selector selector = segment.isSingleSelector() ? segment.getFirstSelector() : null;
			if (selector == null) {
				return true;
			}

			if (selector instanceof NameSelector) {
				String name = ((NameSelector)selector).getName();
				JvmType resolved = childByName(current, name);
				if (resolved == null) {
					return false;
				}
				current = resolved;
			} else if (selector instanceof IndexSelector || selector instanceof WildcardSelector) {
				JvmType element = elementType(current);
				if (element == null) {
					return false;
				}
				current = element;
			} else if (selector instanceof KeySelector) {
				JvmType element = mapTypeArg(current, 0);
				if (element == null) {
					return false;
				}
				current = element;
			} else if (selector instanceof ValueSelector) {
				JvmType element = mapTypeArg(current, 1);
				if (element == null) {
					return false;
				}
				current = element;
			} else {
				return true;
			}
		}
		return true;
	}

	private static @Nullable JvmType childByName(JvmType type, String name) {
		Class<?> raw = type.getRawType();
		Field field = TypeCache.getFieldsByName(raw).get(name);
		if (field == null) {
			return null;
		}
		AnnotatedType annotatedType = field.getAnnotatedType();
		return JvmTypes.resolveJvmType(type, annotatedType.getType(), Arrays.asList(field.getAnnotations()));
	}

	private static @Nullable JvmType elementType(JvmType type) {
		Class<?> raw = type.getRawType();
		if (raw.isArray()) {
			return type.getComponentType();
		}
		if (Collection.class.isAssignableFrom(raw)) {
			List<? extends JvmType> args = type.getTypeVariables();
			return args != null && !args.isEmpty() ? args.get(0) : null;
		}
		return null;
	}

	private static @Nullable JvmType mapTypeArg(JvmType type, int index) {
		Class<?> raw = type.getRawType();
		if (Map.class.isAssignableFrom(raw)) {
			List<? extends JvmType> args = type.getTypeVariables();
			return args != null && args.size() > index ? args.get(index) : null;
		}
		return null;
	}

}
