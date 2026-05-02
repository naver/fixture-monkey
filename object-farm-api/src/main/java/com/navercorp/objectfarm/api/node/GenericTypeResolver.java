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

package com.navercorp.objectfarm.api.node;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A functional interface for resolving generic types to different generic type parameters.
 * <p>
 * This resolver is used to modify or replace the generic type parameters of a type
 * when generating nodes. It allows users to control what concrete types
 * should be used for generic type parameters.
 * <p>
 * For deterministic resolution when multiple type options are available,
 * implementations should store a seed in their constructor and use it internally.
 * <p>
 * Example implementations:
 * <pre>
 * GenericTypeResolver resolver = genericType -&gt; {
 *     if (genericType.getRawType() == List.class) {
 *         // Change List&lt;?&gt; to List&lt;String&gt;
 *         return JvmTypes.of(List.class, JvmTypes.of(String.class));
 *     }
 *     if (genericType.getRawType() == Map.class) {
 *         // Change Map&lt;?, ?&gt; to Map&lt;String, Integer&gt;
 *         return JvmTypes.of(Map.class, JvmTypes.of(String.class), JvmTypes.of(Integer.class));
 *     }
 *     return genericType;
 * };
 * </pre>
 */
@FunctionalInterface
public interface GenericTypeResolver extends NodeCustomizer {
	/**
	 * Resolves a type to a potentially different type with modified generic type parameters.
	 *
	 * @param jvmType the type to resolve
	 * @return the resolved type with potentially different generic type parameters, or null if not resolvable
	 */
	@Nullable
	JvmType resolve(JvmType jvmType);
}

