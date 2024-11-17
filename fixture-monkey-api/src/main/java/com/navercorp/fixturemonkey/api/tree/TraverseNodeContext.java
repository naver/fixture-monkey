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

package com.navercorp.fixturemonkey.api.tree;

/**
 * It defines how to use the nodes traversed, its implementation perform the actions to accomplish its mission.
 * <p>
 * For example instantiation of an object.
 * <p>
 * You must implement this interface, no default implementation is provided as it varies depending on the situation.
 */
public interface TraverseNodeContext {
	/**
	 * Sets the implementation of {@link TraverseNode}, it may vary depending on its usage.
	 * It may throw an exception if type casting is failed.
	 *
	 * @param traverseNode the implementation of {@link TraverseNode}.
	 * @throws IllegalArgumentException throws an exception if type casting is failed.
	 */
	void setTraverseNode(TraverseNode traverseNode) throws IllegalArgumentException;

	/**
	 * Generates the child node of {@link TraverseNodeContext}.
	 * <p>
	 * It must comply with the following contracts.
	 * 1. It should not share the parent's mutable states.
	 * 2. It may share the global immutable state.
	 *
	 * @return the child node of {@link TraverseNodeContext}.
	 */
	TraverseNodeContext newChildNodeContext();
}
