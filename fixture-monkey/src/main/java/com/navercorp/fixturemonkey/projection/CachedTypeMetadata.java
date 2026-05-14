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

package com.navercorp.fixturemonkey.projection;

import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

/**
 * Per-{@code JvmType} memoization of derived assembly metadata.
 * Populated once on first encounter and reused across all assembly calls (cross-call cache
 * lives on {@code AssemblyPlanner.nodeMetadataCache}).
 */
final class CachedTypeMetadata {
	final PropertyNameResolver nameResolver;
	final NullInjectGenerator nullInjectGenerator;
	final boolean isContainerType;
	final boolean hasCandidateConcretePropertyResolvers;

	CachedTypeMetadata(
		PropertyNameResolver nameResolver,
		NullInjectGenerator nullInjectGenerator,
		boolean isContainerType,
		boolean hasCandidateConcretePropertyResolvers
	) {
		this.nameResolver = nameResolver;
		this.nullInjectGenerator = nullInjectGenerator;
		this.isContainerType = isContainerType;
		this.hasCandidateConcretePropertyResolvers = hasCandidateConcretePropertyResolvers;
	}
}
