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


/**
 * A marker interface for customizers that can be used to modify node structure or behavior
 * during JvmNode generation.
 * <p>
 * This interface serves as a common type for various customizers that modify how nodes
 * are generated and processed:
 * <ul>
 *   <li>{@link GenericTypeResolver} - for resolving generic types</li>
 *   <li>{@link InterfaceResolver} - for resolving interface implementations</li>
 *   <li>{@link ContainerSizeResolver} - for resolving container sizes</li>
 *   <li>{@link LeafTypeResolver} - for identifying custom leaf types</li>
 * </ul>
 * <p>
 * NodeCustomizer is designed to be used with JvmNode, not JvmNodeCandidate.
 * This ensures that customization happens during the node promotion/generation phase,
 * keeping JvmNodeCandidateTree deterministic and type-based only.
 */
public interface NodeCustomizer {
}
