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

package com.navercorp.objectfarm.api.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.Collections;
import java.util.List;

/**
 * Represents a JVM type with complete type information, including generics and annotations.
 * <p>
 * JvmType encapsulates both the raw type and its type parameters, providing a structured
 * representation of Java types at runtime. It focuses on concrete, resolvable types
 * and ignores unbounded wildcards (?, ? extends, ? super) that cannot be instantiated.
 * <p>
 * Type structure examples:
 * <ul>
 *   <li>{@code List<String>} → {raw: List, typeVariables: [String]}</li>
 *   <li>{@code List<List<String>>} → {raw: List, typeVariables: [{raw: List, typeVariables: [String]}]}</li>
 *   <li>{@code Map<String, Integer>} → {raw: Map, typeVariables: [String, Integer]}</li>
 * </ul>
 * <p>
 * This abstraction allows for:
 * <ul>
 *   <li>Type-safe object creation and manipulation</li>
 *   <li>Generic type preservation during runtime</li>
 *   <li>Annotation-aware type handling</li>
 *   <li>Complex nested generic type support</li>
 * </ul>
 */
public interface JvmType {
	Class<?> getRawType();

	default List<? extends JvmType> getTypeVariables() {
		return Collections.emptyList();
	}

	List<Annotation> getAnnotations();

	// TODO: should remove
	default AnnotatedType getAnnotatedType() {
		throw new UnsupportedOperationException("This method is not supported for JvmType");
	}
}
