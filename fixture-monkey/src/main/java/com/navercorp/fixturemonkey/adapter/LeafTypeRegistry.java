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

package com.navercorp.fixturemonkey.adapter;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Determines whether a given type is a leaf type that should not be expanded further.
 * <p>
 * Leaf types (e.g. Java standard library types, Kotlin standard types) are terminal nodes
 * in the object tree and their internal fields should not be traversed.
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public interface LeafTypeRegistry {
	/**
	 * Checks whether the given type is a leaf type.
	 *
	 * @param type the class to check
	 * @return true if the type is a leaf type
	 */
	boolean isLeafType(Class<?> type);
}
