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

package com.navercorp.fixturemonkey.api.instantiator;

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.TypeReference;

/**
 * The {@link InstantiatorUtils} class provides utility methods for instantiating objects using {@link Instantiator}.
 */
@API(since = "0.6.12", status = Status.INTERNAL)
public abstract class InstantiatorUtils {
	/**
	 * Resolves and returns a list of parameter names by prioritizing input parameter names over
	 * default parameter names, if available.
	 *
	 * @param parameterNames The default parameter names.
	 * @param inputParameterNames The input parameter names, which may be null or empty.
	 * @return A list of resolved parameter names.
	 */
	public static List<String> resolvedParameterNames(
		List<String> parameterNames,
		List<@Nullable String> inputParameterNames
	) {
		List<String> resolvedParameterNames = new ArrayList<>();
		for (int i = 0; i < parameterNames.size(); i++) {
			String resolvedParameterName;

			if (inputParameterNames.size() > i && inputParameterNames.get(i) != null) {
				resolvedParameterName = inputParameterNames.get(i);
			} else {
				resolvedParameterName = parameterNames.get(i);
			}
			resolvedParameterNames.add(resolvedParameterName);
		}
		return resolvedParameterNames;
	}

	/**
	 * Resolves and returns a list of parameter types by prioritizing input parameter types over
	 * default parameter types, if available.
	 *
	 * @param parameterTypes The default parameter types.
	 * @param inputParameterTypes The input parameter types, which may be null or empty.
	 * @return A list of resolved parameter types.
	 */
	public static List<TypeReference<?>> resolveParameterTypes(
		List<TypeReference<?>> parameterTypes,
		List<TypeReference<?>> inputParameterTypes
	) {
		List<TypeReference<?>> resolvedParameterTypes = new ArrayList<>();
		for (int i = 0; i < parameterTypes.size(); i++) {
			TypeReference<?> resolvedTypeReference = inputParameterTypes.size() > i
				? inputParameterTypes.get(i)
				: parameterTypes.get(i);
			resolvedParameterTypes.add(resolvedTypeReference);
		}
		return resolvedParameterTypes;
	}
}
