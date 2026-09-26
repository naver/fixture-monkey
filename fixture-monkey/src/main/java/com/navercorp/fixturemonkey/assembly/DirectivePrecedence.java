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

package com.navercorp.fixturemonkey.assembly;

import com.navercorp.fixturemonkey.customizer.Scope;

/**
 * The precedence of a directive among the directives that reach the same node: the one whose scope node sits outer
 * wins, then the one whose scope has the lower priority number, then the one declared later.
 * <p>
 * The root scope's node is the node a sample starts from and its priority is {@link Scope#ROOT_PRIORITY}, so it
 * wins over every defined scope by the same rule.
 */
final class DirectivePrecedence implements Comparable<DirectivePrecedence> {
	private final int priority;
	private final int sequence;

	private DirectivePrecedence(int priority, int sequence) {
		this.priority = priority;
		this.sequence = sequence;
	}

	static DirectivePrecedence rootScope(int sequence) {
		return new DirectivePrecedence(Scope.ROOT_PRIORITY, sequence);
	}

	static DirectivePrecedence definedScope(int priority, int sequence) {
		return new DirectivePrecedence(priority, sequence);
	}

	/**
	 * Returns whether the directive was declared in the root scope, whose lazy values are evaluated once per sample.
	 */
	boolean isRootScope() {
		return priority == Scope.ROOT_PRIORITY;
	}

	int sequence() {
		return sequence;
	}

	/**
	 * Returns whether a directive of this precedence whose scope node sits {@code scopeDepth} deep wins over one of
	 * {@code other} whose scope node sits {@code otherScopeDepth} deep.
	 */
	boolean outranks(int scopeDepth, DirectivePrecedence other, int otherScopeDepth) {
		if (scopeDepth != otherScopeDepth) {
			return scopeDepth < otherScopeDepth;
		}
		return compareTo(other) > 0;
	}

	/**
	 * Returns whether this directive's scope, with its node {@code scopeDepth} deep, outranks a scope of
	 * {@code otherPriority} with its node {@code otherScopeDepth} deep, whatever the order they were declared in.
	 */
	boolean scopeOutranks(int scopeDepth, int otherPriority, int otherScopeDepth) {
		if (scopeDepth != otherScopeDepth) {
			return scopeDepth < otherScopeDepth;
		}
		return priority < otherPriority;
	}

	/**
	 * Compares the precedence of two directives at the same scope node.
	 */
	@Override
	public int compareTo(DirectivePrecedence other) {
		if (priority != other.priority) {
			return priority < other.priority ? 1 : -1;
		}
		return Integer.compare(sequence, other.sequence);
	}

	/**
	 * Returns a label identifying the origin of the directive for tracing: "DIRECT" for the root scope, "REGISTER"
	 * for a defined scope.
	 */
	String sourceLabel() {
		return isRootScope() ? "DIRECT" : "REGISTER";
	}
}
