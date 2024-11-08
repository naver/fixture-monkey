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

package com.navercorp.fixturemonkey.api.expression;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

/**
 * It is designed to select and represent a property through a getter method reference in Java.
 *
 * @param <T> The type of the object that the getter method operates on.
 * @param <U> The type of the property that is being selected.
 */
@API(since = "1.0.0", status = Status.EXPERIMENTAL)
public final class JavaGetterMethodPropertySelector<T, U> implements JavaGetterPropertySelector<T, U> {
	private final Class<U> type;
	private final Property property;

	public JavaGetterMethodPropertySelector(Class<U> type, Property property) {
		this.type = type;
		this.property = property;
	}

	public static <T, R> JavaGetterMethodPropertySelector<T, R> javaGetter(
		JavaGetterMethodReference<T, R> methodReference
	) {
		return JavaGetterPropertySelectors.resolvePropertySelector(methodReference);
	}

	public Class<U> getType() {
		return type;
	}

	@Override
	public String generate(PropertyNameResolver propertyNameResolver) {
		return propertyNameResolver.resolve(property);
	}
}
