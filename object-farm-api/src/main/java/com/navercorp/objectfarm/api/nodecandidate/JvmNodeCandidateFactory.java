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

package com.navercorp.objectfarm.api.nodecandidate;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Factory for creating {@link JvmNodeCandidate} instances from a {@link JvmType}.
 * <p>
 * Centralizes the decision of which candidate type to create based on the given type
 * (e.g., {@link JavaMapEntryNodeCandidate} for {@link java.util.Map.Entry} types).
 *
 * @since 1.2.0
 */
public interface JvmNodeCandidateFactory {
	JvmNodeCandidate create(JvmType type, String name, @Nullable CreationMethod creationMethod);
}
