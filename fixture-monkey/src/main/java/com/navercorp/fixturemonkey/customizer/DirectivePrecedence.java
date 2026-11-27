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

package com.navercorp.fixturemonkey.customizer;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * The precedence of a directive among the directives that reach the same node, the one rule every directive is
 * decided by: the one whose scope the sampled builder selected by name later wins, then the one whose scope has the
 * lower priority number, then the one whose scope node sits outer, then the one declared later. A scope not selected
 * ranks below every selected one. A container size is the exception: among sizes of the same selection and priority,
 * the one whose scope node sits inner wins.
 * <p>
 * The root scope ranks above every selection, so it wins over every defined scope, selected or not, whatever their
 * priorities. Where a scope node sits differs from node to node, so its depth is given with each comparison.
 */
@API(since = "1.2.4", status = Status.INTERNAL)
public final class DirectivePrecedence implements Comparable<DirectivePrecedence> {
	private static final int ROOT_SELECTION_ORDER = Integer.MAX_VALUE;

	private final int selectionOrder;
	private final int priority;
	private final int sequence;

	private DirectivePrecedence(int selectionOrder, int priority, int sequence) {
		this.selectionOrder = selectionOrder;
		this.priority = priority;
		this.sequence = sequence;
	}

	/**
	 * Returns the rank of a directive the root scope declared.
	 *
	 * @param sequence the declaration order of the directive, a later one higher
	 * @return the rank
	 */
	public static DirectivePrecedence rootScope(int sequence) {
		return new DirectivePrecedence(ROOT_SELECTION_ORDER, Scope.ROOT_PRIORITY, sequence);
	}

	/**
	 * Returns the rank of a directive a defined scope declared.
	 *
	 * @param selectionOrder the position of the scope among the ones the sampled builder selected by name, from 1 for
	 *                       the one selected first, or 0 when it is not selected
	 * @param priority       the priority of the scope; a lower number takes precedence
	 * @param sequence       the declaration order of the directive among the directives of every scope, a later one
	 *                       higher
	 * @return the rank
	 */
	public static DirectivePrecedence definedScope(int selectionOrder, int priority, int sequence) {
		return new DirectivePrecedence(selectionOrder, priority, sequence);
	}

	/**
	 * Returns whether the directive was declared in the root scope.
	 */
	public boolean isRootScope() {
		return selectionOrder == ROOT_SELECTION_ORDER;
	}

	public int selectionOrder() {
		return selectionOrder;
	}

	public int priority() {
		return priority;
	}

	public int sequence() {
		return sequence;
	}

	/**
	 * Returns whether a directive of this precedence whose scope node sits {@code scopeDepth} deep wins over one of
	 * {@code other} whose scope node sits {@code otherScopeDepth} deep.
	 *
	 * @param scopeDepth      how many segments lead to this directive's scope node
	 * @param other           the precedence of the other directive
	 * @param otherScopeDepth how many segments lead to the other directive's scope node
	 * @return true when this directive wins
	 */
	public boolean outranks(int scopeDepth, DirectivePrecedence other, int otherScopeDepth) {
		int rank = compareRank(other);
		if (rank != 0) {
			return rank > 0;
		}
		if (scopeDepth != otherScopeDepth) {
			return scopeDepth < otherScopeDepth;
		}
		return sequence > other.sequence;
	}

	/**
	 * Returns whether a container size of this precedence whose scope node sits {@code scopeDepth} deep wins over a
	 * size of {@code other} whose scope node sits {@code otherScopeDepth} deep: like {@link #outranks}, except that the
	 * inner scope node wins among the same selection and priority.
	 *
	 * @param scopeDepth      how many segments lead to this size's scope node
	 * @param other           the precedence of the other size
	 * @param otherScopeDepth how many segments lead to the other size's scope node
	 * @return true when this size wins
	 */
	public boolean outranksSize(int scopeDepth, DirectivePrecedence other, int otherScopeDepth) {
		int rank = compareRank(other);
		if (rank != 0) {
			return rank > 0;
		}
		if (scopeDepth != otherScopeDepth) {
			return scopeDepth > otherScopeDepth;
		}
		return sequence > other.sequence;
	}

	/**
	 * Compares the precedence of two directives whose scope nodes sit at the same depth.
	 */
	@Override
	public int compareTo(DirectivePrecedence other) {
		int rank = compareRank(other);
		return rank != 0 ? rank : Integer.compare(sequence, other.sequence);
	}

	/**
	 * Returns whether a directive of this precedence wins over one of {@code other} wherever their scope nodes sit:
	 * the root scope's over a defined scope's, then the one whose scope was selected by name later, then the one whose
	 * scope has the lower priority number.
	 *
	 * @param other the precedence of the other directive
	 * @return true when this directive wins at any depth
	 */
	public boolean outranksAtAnyDepth(DirectivePrecedence other) {
		return compareRank(other) > 0;
	}

	private int compareRank(DirectivePrecedence other) {
		if (selectionOrder != other.selectionOrder) {
			return Integer.compare(selectionOrder, other.selectionOrder);
		}
		return Integer.compare(other.priority, priority);
	}

	/**
	 * Returns a label identifying the origin of the directive for tracing: "DIRECT" for the root scope, "REGISTER"
	 * for a defined scope.
	 */
	public String sourceLabel() {
		return isRootScope() ? "DIRECT" : "REGISTER";
	}
}
