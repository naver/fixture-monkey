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

import java.util.EnumMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.tree.JvmNodeTree;

/**
 * Registry for {@link TreeOutputFormatter} implementations.
 * <p>
 * Provides a unified interface for formatting trees to different output formats.
 * <p>
 * Example usage:
 * <pre>{@code
 * TreeOutputFormatterRegistry registry = TreeOutputFormatterRegistry.defaults();
 *
 * // Format to JSON
 * String json = registry.format(tree, OutputFormat.JSON);
 *
 * // Format to Markdown with options
 * String markdown = registry.format(tree, OutputFormat.MARKDOWN, FormatOptions.verbose());
 *
 * // Get specific formatter
 * TreeOutputFormatter jsonFormatter = registry.getFormatter(OutputFormat.JSON);
 * }</pre>
 */
public final class TreeOutputFormatterRegistry {
	private final Map<OutputFormat, TreeOutputFormatter> formatters;

	private TreeOutputFormatterRegistry(Map<OutputFormat, TreeOutputFormatter> formatters) {
		this.formatters = new EnumMap<>(formatters);
	}

	/**
	 * Creates a registry with default formatters.
	 * <p>
	 * Default formatters include:
	 * <ul>
	 *   <li>{@link JsonTreeFormatter} for {@link OutputFormat#JSON}</li>
	 *   <li>{@link MarkdownTreeFormatter} for {@link OutputFormat#MARKDOWN}</li>
	 *   <li>{@link PromptOptimizedFormatter} for {@link OutputFormat#PROMPT_OPTIMIZED}</li>
	 * </ul>
	 *
	 * @return the default registry
	 */
	public static TreeOutputFormatterRegistry defaults() {
		return builder()
			.register(new JsonTreeFormatter())
			.register(new MarkdownTreeFormatter())
			.register(new PromptOptimizedFormatter())
			.build();
	}

	/**
	 * Creates a new builder for TreeOutputFormatterRegistry.
	 *
	 * @return a new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Formats the tree using the specified format with default options.
	 *
	 * @param tree the tree to format
	 * @param format the output format
	 * @return the formatted string
	 * @throws IllegalArgumentException if the format is not supported
	 */
	public String format(JvmNodeTree tree, OutputFormat format) {
		return format(tree, format, FormatOptions.defaults());
	}

	/**
	 * Formats the tree using the specified format and options.
	 *
	 * @param tree the tree to format
	 * @param format the output format
	 * @param options the formatting options
	 * @return the formatted string
	 * @throws IllegalArgumentException if the format is not supported
	 */
	public String format(JvmNodeTree tree, OutputFormat format, FormatOptions options) {
		TreeOutputFormatter formatter = formatters.get(format);
		if (formatter == null) {
			throw new IllegalArgumentException("No formatter registered for format: " + format);
		}
		return formatter.format(tree, options);
	}

	/**
	 * Returns the formatter for the specified format.
	 *
	 * @param format the output format
	 * @return the formatter, or null if not registered
	 */
	@Nullable
	public TreeOutputFormatter getFormatter(OutputFormat format) {
		return formatters.get(format);
	}

	/**
	 * Checks if a formatter is registered for the specified format.
	 *
	 * @param format the output format
	 * @return true if a formatter is registered
	 */
	public boolean hasFormatter(OutputFormat format) {
		return formatters.containsKey(format);
	}

	/**
	 * Builder for creating TreeOutputFormatterRegistry instances.
	 */
	public static final class Builder {
		private final Map<OutputFormat, TreeOutputFormatter> formatters = new EnumMap<>(OutputFormat.class);

		private Builder() {
		}

		/**
		 * Registers a formatter.
		 *
		 * @param formatter the formatter to register
		 * @return this builder
		 */
		public Builder register(TreeOutputFormatter formatter) {
			formatters.put(formatter.formatType(), formatter);
			return this;
		}

		/**
		 * Builds the TreeOutputFormatterRegistry.
		 *
		 * @return the built registry
		 */
		public TreeOutputFormatterRegistry build() {
			return new TreeOutputFormatterRegistry(formatters);
		}
	}
}
