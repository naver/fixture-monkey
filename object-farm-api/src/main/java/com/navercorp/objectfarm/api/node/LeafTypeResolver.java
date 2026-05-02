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

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A functional interface for determining whether a type should be treated as a leaf node.
 * <p>
 * Leaf types are types that should not have their children expanded during tree transformation.
 * This is useful for types like {@code kotlin.Unit} or other platform-specific types
 * that are not recognized by the default {@code Types.isJavaType()} check.
 */
@FunctionalInterface
public interface LeafTypeResolver extends NodeCustomizer {
	/**
	 * Checks if the given type should be treated as a leaf node.
	 *
	 * @param jvmType the type to check
	 * @return true if the type is a leaf type, false otherwise
	 */
	boolean isLeafType(JvmType jvmType);
}
