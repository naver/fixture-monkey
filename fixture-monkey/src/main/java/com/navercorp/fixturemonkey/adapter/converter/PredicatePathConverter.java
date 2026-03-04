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

package com.navercorp.fixturemonkey.adapter.converter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.Constants;
import com.navercorp.fixturemonkey.adapter.analysis.ManipulatorAnalyzer.DecomposeNameResolver;
import com.navercorp.fixturemonkey.tree.ApplyStrictModeResolver;
import com.navercorp.fixturemonkey.tree.CompositeNodeResolver;
import com.navercorp.fixturemonkey.tree.ContainerElementPredicate;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.NodeAllElementPredicate;
import com.navercorp.fixturemonkey.tree.NodeElementPredicate;
import com.navercorp.fixturemonkey.tree.NodeKeyPredicate;
import com.navercorp.fixturemonkey.tree.NodePredicateResolver;
import com.navercorp.fixturemonkey.tree.NodeResolver;
import com.navercorp.fixturemonkey.tree.NodeTypePredicate;
import com.navercorp.fixturemonkey.tree.NodeValuePredicate;
import com.navercorp.fixturemonkey.tree.PropertyNameNodePredicate;
import com.navercorp.fixturemonkey.tree.PropertyPredicate;
import com.navercorp.fixturemonkey.tree.StartNodePredicate;
import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Converts NextNodePredicate chains to PathExpression.
 * <p>
 * This converter translates fixture-monkey's path predicates to object-farm-api's
 * path expressions for use in tree transformation.
 * <p>
 * Mapping rules:
 * <ul>
 *   <li>{@link StartNodePredicate} → {@code $} (root)</li>
 *   <li>{@link PropertyPredicate} → {@code .propertyName}</li>
 *   <li>{@link PropertyNameNodePredicate} → {@code .propertyName}</li>
 *   <li>{@link NodeAllElementPredicate} → {@code [*]}</li>
 *   <li>{@link NodeElementPredicate} → {@code [n]}</li>
 *   <li>{@link NodeKeyPredicate} → {@code [key]}</li>
 *   <li>{@link NodeValuePredicate} → {@code [value]}</li>
 * </ul>
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class PredicatePathConverter {
	private PredicatePathConverter() {
	}

	/**
	 * Converts a list of NextNodePredicates to a PathExpression.
	 * <p>
	 * This method builds a PathExpression using the builder API directly,
	 * supporting predicates like {@link NodeTypePredicate} that cannot be
	 * represented as plain strings.
	 *
	 * @param predicates the list of predicates representing the path
	 * @return the corresponding PathExpression
	 */
	public static PathExpression convert(List<NextNodePredicate> predicates) {
		return convert(predicates, null);
	}

	/**
	 * Converts a list of NextNodePredicates to a PathExpression, using the given name resolver
	 * for PropertyPredicate names.
	 * <p>
	 * When a {@link DecomposeNameResolver} is provided, {@link PropertyPredicate} nodes use
	 * the resolver to determine property names (e.g., Jackson {@code @JsonProperty} names)
	 * instead of the Java field name.
	 *
	 * @param predicates the list of predicates representing the path
	 * @param nameResolver optional resolver for property names
	 * @return the corresponding PathExpression
	 */
	public static PathExpression convert(
		List<NextNodePredicate> predicates,
		@Nullable DecomposeNameResolver nameResolver
	) {
		PathExpression path = PathExpression.root();

		for (NextNodePredicate predicate : predicates) {
			path = appendPredicateToPath(path, predicate, nameResolver);
		}

		return path;
	}

	/**
	 * Converts a list of NextNodePredicates to a path expression string.
	 *
	 * @param predicates the list of predicates representing the path
	 * @return the path expression string (e.g., "$.items[*]")
	 */
	public static String toExpression(List<NextNodePredicate> predicates) {
		StringBuilder sb = new StringBuilder("$");

		for (NextNodePredicate predicate : predicates) {
			appendPredicate(sb, predicate);
		}

		return sb.toString();
	}

	private static void appendPredicate(StringBuilder sb, NextNodePredicate predicate) {
		if (predicate instanceof StartNodePredicate) {
			// Root is already represented by "$"
			return;
		}

		if (predicate instanceof PropertyPredicate) {
			PropertyPredicate propertyPredicate = (PropertyPredicate)predicate;
			String propertyName = propertyPredicate.getProperty().getName();
			sb.append(".").append(propertyName);
			return;
		}

		if (predicate instanceof PropertyNameNodePredicate) {
			PropertyNameNodePredicate namePredicate = (PropertyNameNodePredicate)predicate;
			String propertyName = namePredicate.getPropertyName();
			// "*" means all fields (field wildcard), not all elements (array wildcard)
			// So it should be converted to ".*" not "[*]"
			sb.append(".").append(propertyName);
			return;
		}

		if (predicate instanceof NodeAllElementPredicate) {
			sb.append("[*]");
			return;
		}

		if (predicate instanceof NodeElementPredicate) {
			NodeElementPredicate elementPredicate = (NodeElementPredicate)predicate;
			int index = elementPredicate.getIndex();
			sb.append("[").append(index).append("]");
			return;
		}

		if (predicate instanceof NodeKeyPredicate) {
			sb.append("[key]");
			return;
		}

		if (predicate instanceof NodeValuePredicate) {
			sb.append("[value]");
			return;
		}

		if (predicate instanceof ContainerElementPredicate) {
			ContainerElementPredicate elementPredicate = (ContainerElementPredicate)predicate;
			int sequence = elementPredicate.getSequence();
			if (sequence == Constants.NO_OR_ALL_INDEX_INTEGER_VALUE) {
				sb.append("[*]");
			} else {
				sb.append("[").append(sequence).append("]");
			}
			return;
		}

		throw new IllegalArgumentException("Unknown predicate type: " + predicate.getClass().getName());
	}

	/**
	 * Extracts NextNodePredicates from a NodeResolver by unwrapping composite and
	 * strict-mode wrappers recursively.
	 *
	 * @param nodeResolver the resolver to extract predicates from
	 * @return the list of extracted predicates
	 */
	public static List<NextNodePredicate> extractPredicates(NodeResolver nodeResolver) {
		List<NextNodePredicate> predicates = new ArrayList<>();
		extractPredicatesRecursive(nodeResolver, predicates);
		return predicates;
	}

	private static void extractPredicatesRecursive(NodeResolver nodeResolver, List<NextNodePredicate> predicates) {
		if (nodeResolver instanceof ApplyStrictModeResolver) {
			ApplyStrictModeResolver strictResolver = (ApplyStrictModeResolver)nodeResolver;
			extractPredicatesRecursive(strictResolver.getNodeResolver(), predicates);
		} else if (nodeResolver instanceof CompositeNodeResolver) {
			CompositeNodeResolver composite = (CompositeNodeResolver)nodeResolver;
			for (NodeResolver childResolver : composite.getNodeResolvers()) {
				extractPredicatesRecursive(childResolver, predicates);
			}
		} else if (nodeResolver instanceof NodePredicateResolver) {
			NextNodePredicate predicate = ((NodePredicateResolver)nodeResolver).getNextNodePredicate();
			if (predicate != null) {
				predicates.add(predicate);
			}
		}
	}

	private static PathExpression appendPredicateToPath(
		PathExpression path,
		NextNodePredicate predicate,
		@Nullable DecomposeNameResolver nameResolver
	) {
		if (predicate instanceof StartNodePredicate) {
			return path;
		}

		if (predicate instanceof PropertyPredicate) {
			PropertyPredicate propertyPredicate = (PropertyPredicate)predicate;
			String propertyName =
				nameResolver != null
					? nameResolver.resolve(propertyPredicate.getProperty())
					: propertyPredicate.getProperty().getName();
			if (propertyName == null) {
				return path;
			}
			return path.child(propertyName);
		}

		if (predicate instanceof PropertyNameNodePredicate) {
			PropertyNameNodePredicate namePredicate = (PropertyNameNodePredicate)predicate;
			return path.child(namePredicate.getPropertyName());
		}

		if (predicate instanceof NodeAllElementPredicate) {
			return path.wildcard();
		}

		if (predicate instanceof NodeElementPredicate) {
			NodeElementPredicate elementPredicate = (NodeElementPredicate)predicate;
			return path.index(elementPredicate.getIndex());
		}

		if (predicate instanceof NodeKeyPredicate) {
			return path.key();
		}

		if (predicate instanceof NodeValuePredicate) {
			return path.value();
		}

		if (predicate instanceof ContainerElementPredicate) {
			ContainerElementPredicate elementPredicate = (ContainerElementPredicate)predicate;
			int sequence = elementPredicate.getSequence();
			if (sequence == Constants.NO_OR_ALL_INDEX_INTEGER_VALUE) {
				return path.wildcard();
			}
			return path.index(sequence);
		}

		if (predicate instanceof NodeTypePredicate) {
			NodeTypePredicate typePredicate = (NodeTypePredicate)predicate;
			return path.type(typePredicate.getType(), typePredicate.isExact());
		}

		throw new IllegalArgumentException("Unknown predicate type: " + predicate.getClass().getName());
	}
}
