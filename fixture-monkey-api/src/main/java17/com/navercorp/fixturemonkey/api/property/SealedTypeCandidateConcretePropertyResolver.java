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

package com.navercorp.fixturemonkey.api.property;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.0.21", status = Status.EXPERIMENTAL)
public final class SealedTypeCandidateConcretePropertyResolver implements CandidateConcretePropertyResolver {
	@Override
	public List<Property> resolve(Property property) {
		Class<?> actualType = Types.getActualType(property.getType());
		Set<Class<?>> permittedSubclasses = collectPermittedSubclasses(actualType);

		return permittedSubclasses.stream()
			.map(PropertyUtils::toProperty)
			.toList();
	}

	private static Set<Class<?>> collectPermittedSubclasses(Class<?> type) {
		Set<Class<?>> subclasses = new HashSet<>();
		doCollectPermittedSubclasses(type, subclasses);
		return subclasses;
	}

	private static void doCollectPermittedSubclasses(Class<?> type, Set<Class<?>> subclasses) {
		if (type.isSealed()) {
			for (Class<?> subclass : type.getPermittedSubclasses()) {
				doCollectPermittedSubclasses(subclass, subclasses);
			}
		} else {
			subclasses.add(type);
		}
	}
}
