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

package com.navercorp.objectfarm.api.nodepromoter;

import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecontext.JvmNodeContext;

/**
 * Responsible for promoting JvmNodeCandidate instances to JvmNode instances.
 * <p>
 * JvmNodePromoter serves as a factory that transforms intermediate node candidates
 * into actual nodes. This promotion process must be idempotent and repeatable,
 * ensuring consistent results across multiple executions.
 * <p>
 * Key responsibilities include:
 * <ul>
 *   <li>Determining concrete implementations for abstract types (interfaces, abstract classes)</li>
 *   <li>Resolving type-specific instantiation logic</li>
 *   <li>Providing consistent and reproducible node creation</li>
 * </ul>
 * <p>
 * The promotion process should be deterministic and safe to execute multiple times
 * with the same input parameters.
 */
public interface JvmNodePromoter {
	/**
	 * Checks whether this promoter can handle the promotion of the given node candidate.
	 * 
	 * @param node the node candidate to check
	 * @return true if this promoter can promote the given node, false otherwise
	 */
	default boolean canPromote(JvmNodeCandidate node) {
		return true;
	}

	/**
	 * Promotes a JvmNodeCandidate to a JvmNode using the provided context.
	 * This operation must be idempotent and deterministic.
	 * 
	 * @param node the node candidate to promote
	 * @param context the context containing additional information for promotion
	 * @return the promoted JvmNode instance
	 */
	JvmNode promote(JvmNodeCandidate node, JvmNodeContext context);
}
