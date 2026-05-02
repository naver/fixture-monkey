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

import java.util.List;

import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Formats {@link JvmNodeTree} as human-readable Markdown.
 * <p>
 * Output format example:
 * <pre>
 * # Type Structure: User
 *
 * ## Properties
 *
 * - **name**: `String`
 * - **age**: `int`
 * - **addresses**: `List&lt;Address&gt;`
 *   - **[0]**: `Address`
 *     - **city**: `String`
 *     - **street**: `String`
 *
 * ## Statistics
 *
 * | Metric | Value |
 * |--------|-------|
 * | Total Nodes | 6 |
 * | Root Type | `User` |
 * </pre>
 */
public final class MarkdownTreeFormatter implements TreeOutputFormatter {

	@Override
	public OutputFormat formatType() {
		return OutputFormat.MARKDOWN;
	}

	@Override
	public String format(JvmNodeTree tree, FormatOptions options) {
		StringBuilder sb = new StringBuilder();

		JvmNode root = tree.getRootNode();

		// Header
		sb.append("# Type Structure: ");
		sb.append(formatSimpleType(root.getConcreteType()));
		sb.append("\n\n");

		// Properties section
		sb.append("## Properties\n\n");
		formatNodeMarkdown(root, tree, sb, 0, 0, options);
		sb.append("\n");

		// Statistics section
		sb.append("## Statistics\n\n");
		sb.append("| Metric | Value |\n");
		sb.append("|--------|-------|\n");
		sb.append("| Total Nodes | ").append(tree.size()).append(" |\n");
		sb.append("| Root Type | `").append(formatFullType(root.getConcreteType())).append("` |\n");

		return sb.toString();
	}

	private void formatNodeMarkdown(
		JvmNode node,
		JvmNodeTree tree,
		StringBuilder sb,
		int depth,
		int currentDepth,
		FormatOptions options
	) {
		String indent = repeatString("  ", depth);
		List<JvmNode> children = tree.getChildren(node);

		for (JvmNode child : children) {
			sb.append(indent).append("- ");

			// Name
			sb.append("**").append(formatNodeName(child)).append("**");

			// Type
			if (options.isIncludeTypes()) {
				sb.append(": `").append(formatSimpleType(child.getConcreteType())).append("`");
			}
			sb.append("\n");

			// Recurse for children
			if (currentDepth < options.getMaxDepth()) {
				formatNodeMarkdown(child, tree, sb, depth + 1, currentDepth + 1, options);
			}
		}
	}

	private String formatNodeName(JvmNode node) {
		Integer index = node.getIndex();
		if (index != null) {
			return "[" + index + "]";
		}
		String name = node.getNodeName();
		return name != null ? name : "root";
	}

	private String formatFullType(JvmType type) {
		if (type == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(type.getRawType().getName());

		List<? extends JvmType> typeVars = type.getTypeVariables();
		if (!typeVars.isEmpty()) {
			sb.append("<");
			for (int i = 0; i < typeVars.size(); i++) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(formatFullType(typeVars.get(i)));
			}
			sb.append(">");
		}

		return sb.toString();
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

	private String repeatString(String str, int count) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append(str);
		}
		return sb.toString();
	}
}
