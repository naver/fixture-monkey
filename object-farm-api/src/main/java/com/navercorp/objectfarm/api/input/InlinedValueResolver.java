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

package com.navercorp.objectfarm.api.input;

/**
 * Reconstructs a value type that its language inlined into the owning member's field.
 * <p>
 * Some JVM languages compile a single-value wrapper away: the wrapper's underlying value is stored
 * directly in the owner's field, and the wrapper only reappears at source-level boundaries such as
 * constructor calls made through the language's own reflection. A Kotlin
 * {@code @JvmInline value class} works this way, and so do Kotlin's own {@code Duration} and
 * {@code UInt}.
 * <p>
 * That makes reading and writing such a member asymmetric. Reading it off the JVM yields the
 * underlying value; writing it through the language expects the wrapper. Implementations bridge the
 * two by rebuilding the wrapper around what was read.
 * <p>
 * A {@link FieldExtractor} decides which members to visit and what path to key each one by; only
 * the read step goes through this resolver, so an extractor that applies a naming policy to its
 * paths keeps applying it.
 * <p>
 * Implementations must return {@code extracted} unchanged for members they do not handle, which is
 * the common case.
 */
@FunctionalInterface
public interface InlinedValueResolver {
	/**
	 * Rebuilds the inlined value type of a single member, if it has one.
	 *
	 * @param owner      the object the member was read from
	 * @param memberName the name of the member on {@code owner}, which is not necessarily the last
	 *                   segment of the path it is keyed by
	 * @param extracted  the value and type as read off the JVM
	 * @return the reconstructed value, or {@code extracted} if this resolver does not apply
	 */
	ExtractedField resolve(Object owner, String memberName, ExtractedField extracted);

	/**
	 * Returns a resolver that leaves every member as read.
	 *
	 * @return the no-op resolver
	 */
	static InlinedValueResolver noOp() {
		return (owner, memberName, extracted) -> extracted;
	}
}
