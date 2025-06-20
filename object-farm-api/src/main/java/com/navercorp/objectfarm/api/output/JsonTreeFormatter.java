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
 * Formats {@link JvmNodeTree} as JSON for machine consumption.
 * <p>
 * Output format example:
 * <pre>{@code
 * {
 *   "root": {
 *     "name": "root",
 *     "type": "com.example.User",
 *     "typeSimple": "User",
 *     "children": [
 *       {
 *         "name": "name",
 *         "type": "java.lang.String",
 *         "typeSimple": "String",
 *         "children": []
 *       }
 *     ]
 *   },
 *   "metadata": {
 *     "totalNodes": 5
 *   }
 * }
 * }</pre>
 */
public final class JsonTreeFormatter implements TreeOutputFormatter {

	@Override
	public OutputFormat formatType() {
		return OutputFormat.JSON;
	}

	@Override
	public String format(JvmNodeTree tree, FormatOptions options) {
		StringBuilder sb = new StringBuilder();

		if (options.isPrettyPrint()) {
			sb.append("{\n");
			sb.append(options.getIndentation()).append("\"root\": ");
			formatNode(tree.getRootNode(), tree, sb, 1, 0, options);
			sb.append(",\n");
			sb.append(options.getIndentation()).append("\"metadata\": ");
			formatMetadata(tree, sb, options);
			sb.append("\n}");
		} else {
			sb.append("{\"root\":");
			formatNode(tree.getRootNode(), tree, sb, 0, 0, options);
			sb.append(",\"metadata\":");
			formatMetadata(tree, sb, options);
			sb.append("}");
		}

		return sb.toString();
	}

	private void formatNode(
		JvmNode node,
		JvmNodeTree tree,
		StringBuilder sb,
		int depth,
		int currentDepth,
		FormatOptions options
	) {
		String indent = options.isPrettyPrint() ? repeatString(options.getIndentation(), depth) : "";
		String childIndent = options.isPrettyPrint() ? repeatString(options.getIndentation(), depth + 1) : "";
		String newline = options.isPrettyPrint() ? "\n" : "";
		String separator = options.isPrettyPrint() ? " " : "";

		sb.append("{").append(newline);

		// Name
		sb.append(childIndent).append("\"name\":").append(separator);
		sb.append("\"").append(escapeJson(formatNodeName(node))).append("\"");

		// Type
		if (options.isIncludeTypes()) {
			sb.append(",").append(newline);
			sb.append(childIndent).append("\"type\":").append(separator);
			sb.append("\"").append(escapeJson(formatFullType(node.getConcreteType()))).append("\"");

			sb.append(",").append(newline);
			sb.append(childIndent).append("\"typeSimple\":").append(separator);
			sb.append("\"").append(escapeJson(formatSimpleType(node.getConcreteType()))).append("\"");
		}

		// Children
		List<JvmNode> children = tree.getChildren(node);
		sb.append(",").append(newline);
		sb.append(childIndent).append("\"children\":").append(separator).append("[");

		if (currentDepth < options.getMaxDepth() && !children.isEmpty()) {
			sb.append(newline);
			for (int i = 0; i < children.size(); i++) {
				if (i > 0) {
					sb.append(",").append(newline);
				}
				sb.append(options.isPrettyPrint() ? repeatString(options.getIndentation(), depth + 2) : "");
				formatNode(children.get(i), tree, sb, depth + 2, currentDepth + 1, options);
			}
			sb.append(newline);
			sb.append(childIndent);
		}
		sb.append("]").append(newline);

		sb.append(indent).append("}");
	}

	private void formatMetadata(JvmNodeTree tree, StringBuilder sb, FormatOptions options) {
		String separator = options.isPrettyPrint() ? " " : "";
		sb.append("{\"totalNodes\":").append(separator).append(tree.size()).append("}");
	}

	private String formatNodeName(JvmNode node) {
		// Container elements show index, others show node name
		Integer index = node.getIndex();
		if (index != null) {
			return "[" + index + "]";
		}
		String name = node.getNodeName();
		return name != null ? name : "root";
	}

	private String escapeJson(String str) {
		if (str == null) {
			return "null";
		}
		return str
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t");
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
