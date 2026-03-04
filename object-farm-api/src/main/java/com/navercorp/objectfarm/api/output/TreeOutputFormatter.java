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

import com.navercorp.objectfarm.api.tree.JvmNodeTree;

/**
 * Interface for formatting {@link JvmNodeTree} to various output formats.
 * <p>
 * Implementations provide different output formats optimized for various use cases:
 * <ul>
 *   <li>{@link OutputFormat#JSON} - Machine-readable format for API responses</li>
 *   <li>{@link OutputFormat#MARKDOWN} - Human-readable documentation format</li>
 *   <li>{@link OutputFormat#PROMPT_OPTIMIZED} - LLM-optimized format with path syntax</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * TreeOutputFormatter formatter = new JsonTreeFormatter();
 * String output = formatter.format(tree, FormatOptions.defaults());
 * }</pre>
 */
public interface TreeOutputFormatter {

	/**
	 * Returns the format type that this formatter produces.
	 *
	 * @return the output format enum value
	 */
	OutputFormat formatType();

	/**
	 * Formats the tree using default options.
	 *
	 * @param tree the tree to format
	 * @return the formatted string representation
	 */
	default String format(JvmNodeTree tree) {
		return format(tree, FormatOptions.defaults());
	}

	/**
	 * Formats the tree with custom options.
	 *
	 * @param tree the tree to format
	 * @param options the formatting options
	 * @return the formatted string representation
	 */
	String format(JvmNodeTree tree, FormatOptions options);
}
