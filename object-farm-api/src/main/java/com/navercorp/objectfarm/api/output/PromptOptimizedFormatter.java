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

package com.navercorp.objectfarm.api.output;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Formats {@link JvmNodeTree} in a format optimized for LLM consumption.
 * <p>
 * The format is designed to be:
 * <ul>
 *   <li>Token-efficient: Minimal redundancy</li>
 *   <li>Structured: Clear hierarchy with consistent patterns</li>
 *   <li>Context-rich: Includes type information and path syntax</li>
 * </ul>
 * <p>
 * Output format example:
 * <pre>
 * TYPE_TREE: User
 * STRUCTURE:
 * $.name: String
 * $.age: int
 * $.addresses: List&lt;Address&gt;
 *   $[*].city: String
 *   $[*].street: String
 *
 * PATH_SYNTAX:
 * - Property access: $.propertyName
 * - Array index: $.arrayProp[0]
 * - All array elements: $.arrayProp[*]
 * - Nested access: $.parent.child.grandchild
 * </pre>
 */
public final class PromptOptimizedFormatter implements TreeOutputFormatter {

	@Override
	public OutputFormat formatType() {
		return OutputFormat.PROMPT_OPTIMIZED;
	}

	@Override
	public String format(JvmNodeTree tree, FormatOptions options) {
		StringBuilder sb = new StringBuilder();

		JvmNode root = tree.getRootNode();

		// Type header
		sb.append("TYPE_TREE: ").append(formatSimpleType(root.getConcreteType())).append("\n");
		sb.append("STRUCTURE:\n");

		// Structure section with path notation
		formatNodePaths(root, tree, sb, "$", 0, options);
		sb.append("\n");

		// Path examples for LLM
		sb.append("PATH_SYNTAX:\n");
		sb.append("- Property access: $.propertyName\n");
		sb.append("- Array index: $.arrayProp[0]\n");
		sb.append("- All array elements: $.arrayProp[*]\n");
		sb.append("- Nested access: $.parent.child.grandchild\n");

		return sb.toString();
	}

	private void formatNodePaths(
		JvmNode node,
		JvmNodeTree tree,
		StringBuilder sb,
		String currentPath,
		int currentDepth,
		FormatOptions options
	) {
		List<JvmNode> children = tree.getChildren(node);

		for (JvmNode child : children) {
			String childPath = buildPath(currentPath, child);

			// Format: path: Type
			sb.append(childPath).append(": ");
			sb.append(formatSimpleType(child.getConcreteType()));

			// Add container marker
			if (isContainer(child.getConcreteType())) {
				sb.append(" (container)");
			}
			sb.append("\n");

			// Recurse with depth check
			if (currentDepth < options.getMaxDepth()) {
				formatNodePaths(child, tree, sb, childPath, currentDepth + 1, options);
			}
		}
	}

	private String buildPath(String parentPath, JvmNode node) {
		// Container element: use index
		Integer index = node.getIndex();
		if (index != null) {
			return parentPath + "[" + index + "]";
		}
		// Property: $.name
		String name = node.getNodeName();
		if (name == null || name.isEmpty()) {
			return parentPath;
		}
		return parentPath + "." + name;
	}

	private boolean isContainer(JvmType type) {
		if (type == null) {
			return false;
		}
		Class<?> rawType = type.getRawType();
		return Iterable.class.isAssignableFrom(rawType)
			|| Collection.class.isAssignableFrom(rawType)
			|| Map.class.isAssignableFrom(rawType)
			|| rawType.isArray();
	}

	private String formatSimpleType(JvmType type) {
		if (type == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(type.getRawType().getSimpleName());

		List<? extends JvmType> typeVars = type.getTypeVariables();
		if (!typeVars.isEmpty()) {
			sb.append("<");
			for (int i = 0; i < typeVars.size(); i++) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(formatSimpleType(typeVars.get(i)));
			}
			sb.append(">");
		}

		return sb.toString();
	}
}
