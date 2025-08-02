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

import java.lang.reflect.Field;
import java.util.List;

import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.PropertyNameNodePredicate;

public class ExpressionPathValidator {

	public static boolean isValidFieldPath(Class<?> rootClass, List<NextNodePredicate> predicates) {
		if (predicates == null || predicates.isEmpty()) {
			return false;
		}
		Class<?> currentClass = Types.getActualType(rootClass);
		for (NextNodePredicate predicate : predicates) {
			if (predicate instanceof PropertyNameNodePredicate) {
				String fieldName = ((PropertyNameNodePredicate)predicate).getPropertyName();
				try {
					Field field = currentClass.getDeclaredField(fieldName);
					currentClass = field.getType();
				} catch (NoSuchFieldException e) {
					return false;
				}
			}
		}
		return true;
	}
}
