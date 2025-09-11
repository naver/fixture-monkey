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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.tree.ContainerElementPredicate;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.PropertyNameNodePredicate;
import com.navercorp.fixturemonkey.tree.StartNodePredicate;

public final class StrictModeNextNodePredicateContainer extends AbstractList<NextNodePredicate> {
	private final List<NextNodePredicate> delegate;

	public StrictModeNextNodePredicateContainer(
		List<NextNodePredicate> delegate, AnnotatedType rootAnnotatedType, PropertyNameResolver propertyNameResolver
	) {
		this.delegate = delegate;
		if (!isValidFieldPath(rootAnnotatedType, delegate, propertyNameResolver)) {
			throw new IllegalArgumentException("No matching results for given container expression.");
		}
	}

	@Override
	public NextNodePredicate get(int index) {
		return delegate.get(index);
	}

	@Override
	public int size() {
		return delegate.size();
	}

	/**
	 * Validates if the given expression path corresponds to a valid field path within the root class.
	 * <p>
	 * Currently, this validation only checks for the existence of property names along the path.
	 * It does not validate array or list indices.
	 * For example, for an expression like {@code list[1]}, it checks for the existence of the {@code list} field,
	 * but it does not verify if index {@code 1} is valid for that list.
	 * This can lead to runtime errors in some edge cases, instead of errors at the validation stage.
	 *
	 * @param rootAnnotatedType    The class to start path validation from.
	 * @param predicates           A list of predicates representing the expression path.
	 * @param propertyNameResolver The resolver used to check property names.
	 * @return {@code true} if the path is valid, {@code false} otherwise.
	 */
	private boolean isValidFieldPath(
		AnnotatedType rootAnnotatedType, List<NextNodePredicate> predicates, PropertyNameResolver propertyNameResolver
	) {
		if (predicates == null || predicates.isEmpty()) {
			return false;
		}
		AnnotatedType currentAnnotatedType = rootAnnotatedType;
		for (NextNodePredicate predicate : predicates) {
			if (predicate instanceof StartNodePredicate) {
				continue;
			}
			if (predicate instanceof PropertyNameNodePredicate) {
				String fieldName = ((PropertyNameNodePredicate)predicate).getPropertyName();
				Class<?> actualClass = Types.getActualType(currentAnnotatedType);
				Optional<Field> field = Arrays.stream(actualClass.getDeclaredFields())
					.filter(f -> {
						String resolvedFieldName = propertyNameResolver.resolve(new FieldProperty(f));
						return resolvedFieldName.equals(fieldName);
					})
					.findFirst();

				if (!field.isPresent()) {
					return false;
				}

				currentAnnotatedType = field.get().getAnnotatedType();
			} else if (predicate instanceof ContainerElementPredicate) {
				List<AnnotatedType> genericAnnotatedTypes = Types.getGenericsTypes(currentAnnotatedType);

				if (genericAnnotatedTypes.isEmpty()) {
					return false;
				}
				currentAnnotatedType = genericAnnotatedTypes.get(0);
			} else {
				return false;
			}
		}
		return true;
	}
}
