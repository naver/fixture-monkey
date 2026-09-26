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

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * The nodes from the outermost one down to a node, where the scopes that apply to the node are looked up. A scope
 * applies to the node when it selects a node on the chain, the scope node; a directive of the scope reaches the
 * node when its path matches the path from the scope node down to the node.
 * <p>
 * Positions on the chain are depths: the outermost node sits at 0 and the node itself at {@link #depth()}.
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public abstract class ScopeChain {
	private final Function<JvmNode, Property> propertyOf;

	private ScopeChain(Function<JvmNode, Property> propertyOf) {
		this.propertyOf = propertyOf;
	}

	/**
	 * Returns the chain of the given nodes, where the segment into each node is its index or name.
	 *
	 * @param nodes      the nodes from the outermost one down to the node
	 * @param propertyOf the property of a node, for scopes not named by a type
	 * @return the chain
	 */
	public static ScopeChain ofNodes(List<JvmNode> nodes, Function<JvmNode, Property> propertyOf) {
		return new NodeScopeChain(nodes, propertyOf);
	}

	/**
	 * Returns the chain of the nodes {@code path} passes through, found by their paths.
	 *
	 * @param path       the path of the node from the outermost one
	 * @param nodeAt     the node at a path, null when none is placed there
	 * @param propertyOf the property of a node, for scopes not named by a type
	 * @return the chain
	 */
	public static ScopeChain ofPath(
		PathExpression path,
		Function<PathExpression, @Nullable JvmNode> nodeAt,
		Function<JvmNode, Property> propertyOf
	) {
		return new PathScopeChain(path, nodeAt, propertyOf);
	}

	/**
	 * Returns the depth of the node itself.
	 *
	 * @return the depth of the node
	 */
	public abstract int depth();

	/**
	 * Returns the node at {@code depth}.
	 *
	 * @param depth the depth, from 0 to {@link #depth()}
	 * @return the node, or null when none is known there
	 */
	public abstract @Nullable JvmNode nodeAt(int depth);

	/**
	 * Returns the scope node at {@code depth}: the node a scope was found to select there, or the node a sample starts
	 * from at depth 0.
	 *
	 * @param depth the depth of the scope node
	 * @return the scope node
	 */
	@SuppressWarnings("argument")
	public JvmNode scopeNodeAt(int depth) {
		return Objects.requireNonNull(nodeAt(depth), "no node at scope depth " + depth);
	}

	/**
	 * Returns the segment from the node at {@code depth} to the node right below it.
	 *
	 * @param depth the depth, from 0 to {@link #depth()} - 1
	 * @return the segment, or null when the node below has neither an index nor a name
	 */
	public abstract @Nullable Segment segmentAt(int depth);

	/**
	 * Returns whether {@code scope} selects the node at {@code depth}.
	 *
	 * @param scope the scope
	 * @param depth the depth of the candidate scope node
	 * @return true when the node there is selected
	 */
	public boolean selects(ScopeSelector scope, int depth) {
		JvmNode node = nodeAt(depth);
		return node != null && scope.selects(node, depth, propertyOf);
	}

	/**
	 * Returns whether {@code pattern} matches the path from the node at {@code depth} down to the node itself,
	 * segment by segment as {@link PathExpression#matches(PathExpression)} does.
	 *
	 * @param depth   the depth of the scope node
	 * @param pattern the path relative to the scope node
	 * @return true when the pattern matches the whole path below the scope node
	 */
	public boolean matchesBelow(int depth, PathExpression pattern) {
		List<Segment> patternSegments = pattern.getSegments();
		return depth >= 0
			&& patternSegments.size() == depth() - depth
			&& matchesFrom(depth, patternSegments, patternSegments.size());
	}

	/**
	 * Returns the segment of {@code pattern} right below the node itself when {@code pattern} reaches past it: the
	 * path from the node at {@code depth} down to the node matches the beginning of the pattern.
	 *
	 * @param depth   the depth of the scope node
	 * @param pattern the path relative to the scope node
	 * @return the segment of the pattern below the node, or null when the pattern does not reach past it
	 */
	public @Nullable Segment patternSegmentBelow(int depth, PathExpression pattern) {
		List<Segment> patternSegments = pattern.getSegments();
		int covered = depth() - depth;
		if (depth < 0 || covered >= patternSegments.size() || !matchesFrom(depth, patternSegments, covered)) {
			return null;
		}
		return patternSegments.get(covered);
	}

	/**
	 * Returns whether the node itself is at or inside a node {@code pattern} reaches from the node at {@code depth}.
	 *
	 * @param depth   the depth of the scope node
	 * @param pattern the path relative to the scope node
	 * @return true when the beginning of the path below the scope node matches the whole pattern
	 */
	public boolean coversFrom(int depth, PathExpression pattern) {
		List<Segment> patternSegments = pattern.getSegments();
		return depth >= 0
			&& patternSegments.size() <= depth() - depth
			&& matchesFrom(depth, patternSegments, patternSegments.size());
	}

	/**
	 * Returns the depth of the node {@code scope} selects from which {@code relativePath} reaches the node itself.
	 *
	 * @param scope        the scope
	 * @param relativePath the path relative to a node the scope selects
	 * @return the scope depth, or -1 when the path does not reach the node from a node the scope selects
	 */
	public int scopeDepthOf(ScopeSelector scope, PathExpression relativePath) {
		int scopeDepth = depth() - relativePath.getSegments().size();
		return matchesBelow(scopeDepth, relativePath) && selects(scope, scopeDepth) ? scopeDepth : -1;
	}

	/**
	 * Returns the depth of the node {@code scope} selects, at most {@code scopeDepthLimit} deep, from which
	 * {@code relativePath} reaches past the node itself.
	 *
	 * @param scope           the scope
	 * @param relativePath    the path relative to a node the scope selects
	 * @param scopeDepthLimit the deepest scope node to look at
	 * @return the scope depth, or -1 when the path does not reach below the node
	 */
	public int scopeDepthBelow(ScopeSelector scope, PathExpression relativePath, int scopeDepthLimit) {
		int maxScopeDepth = Math.min(depth(), scopeDepthLimit);
		for (int scopeDepth = 0; scopeDepth <= maxScopeDepth; scopeDepth++) {
			if (patternSegmentBelow(scopeDepth, relativePath) != null && selects(scope, scopeDepth)) {
				return scopeDepth;
			}
		}
		return -1;
	}

	/**
	 * Returns the segment of {@code relativePath} right below the node itself when it reaches past the node from a
	 * node {@code scope} selects, at most {@code scopeDepthLimit} deep.
	 *
	 * @param scope           the scope
	 * @param relativePath    the path relative to a node the scope selects
	 * @param scopeDepthLimit the deepest scope node to look at
	 * @return the segment below, or null when the path does not reach below the node
	 */
	public @Nullable Segment segmentBelow(ScopeSelector scope, PathExpression relativePath, int scopeDepthLimit) {
		int scopeDepth = scopeDepthBelow(scope, relativePath, scopeDepthLimit);
		return scopeDepth >= 0 ? patternSegmentBelow(scopeDepth, relativePath) : null;
	}

	private boolean matchesFrom(int depth, List<Segment> patternSegments, int count) {
		for (int i = 0; i < count; i++) {
			Segment segment = segmentAt(depth + i);
			if (segment == null || !patternSegments.get(i).matches(segment)) {
				return false;
			}
		}
		return true;
	}

	private static final class NodeScopeChain extends ScopeChain {
		private final List<JvmNode> nodes;

		private NodeScopeChain(List<JvmNode> nodes, Function<JvmNode, Property> propertyOf) {
			super(propertyOf);
			this.nodes = nodes;
		}

		@Override
		public int depth() {
			return nodes.size() - 1;
		}

		@Override
		public @Nullable JvmNode nodeAt(int depth) {
			return nodes.get(depth);
		}

		@Override
		public @Nullable Segment segmentAt(int depth) {
			JvmNode below = nodes.get(depth + 1);
			Integer index = below.getIndex();
			if (index != null) {
				return PathExpression.root().index(index).getLastSegment();
			}
			String name = below.getNodeName();
			return name != null ? PathExpression.root().child(name).getLastSegment() : null;
		}
	}

	private static final class PathScopeChain extends ScopeChain {
		private final PathExpression path;
		private final List<Segment> segments;
		private final Function<PathExpression, @Nullable JvmNode> nodeAt;

		private PathScopeChain(
			PathExpression path,
			Function<PathExpression, @Nullable JvmNode> nodeAt,
			Function<JvmNode, Property> propertyOf
		) {
			super(propertyOf);
			this.path = path;
			this.segments = path.getSegments();
			this.nodeAt = nodeAt;
		}

		@Override
		public int depth() {
			return segments.size();
		}

		@Override
		public @Nullable JvmNode nodeAt(int depth) {
			return nodeAt.apply(path.truncateTo(depth));
		}

		@Override
		public Segment segmentAt(int depth) {
			return segments.get(depth);
		}
	}
}
