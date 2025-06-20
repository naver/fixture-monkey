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

/**
 * Options for customizing tree formatting output.
 * <p>
 * Example usage:
 * <pre>{@code
 * FormatOptions options = FormatOptions.builder()
 *     .maxDepth(5)
 *     .includeTypes(true)
 *     .prettyPrint(true)
 *     .build();
 *
 * String output = formatter.format(tree, options);
 * }</pre>
 */
public final class FormatOptions {
	private final int maxDepth;
	private final boolean includeTypes;
	private final boolean includeIndices;
	private final boolean prettyPrint;
	private final String indentation;

	private FormatOptions(
		int maxDepth,
		boolean includeTypes,
		boolean includeIndices,
		boolean prettyPrint,
		String indentation
	) {
		this.maxDepth = maxDepth;
		this.includeTypes = includeTypes;
		this.includeIndices = includeIndices;
		this.prettyPrint = prettyPrint;
		this.indentation = indentation;
	}

	/**
	 * Returns default format options.
	 *
	 * @return default options with pretty printing enabled
	 */
	public static FormatOptions defaults() {
		return builder().build();
	}

	/**
	 * Returns compact format options.
	 *
	 * @return options optimized for minimal output size
	 */
	public static FormatOptions compact() {
		return builder()
			.prettyPrint(false)
			.build();
	}

	/**
	 * Returns verbose format options.
	 *
	 * @return options with all information included
	 */
	public static FormatOptions verbose() {
		return builder()
			.includeTypes(true)
			.includeIndices(true)
			.build();
	}

	/**
	 * Creates a new builder for FormatOptions.
	 *
	 * @return a new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns the maximum depth to traverse in the tree.
	 *
	 * @return the max depth
	 */
	public int getMaxDepth() {
		return maxDepth;
	}

	/**
	 * Returns whether to include type information in the output.
	 *
	 * @return true if types should be included
	 */
	public boolean isIncludeTypes() {
		return includeTypes;
	}

	/**
	 * Returns whether to include indices for container elements.
	 *
	 * @return true if indices should be included
	 */
	public boolean isIncludeIndices() {
		return includeIndices;
	}

	/**
	 * Returns whether to format output with indentation.
	 *
	 * @return true if pretty printing is enabled
	 */
	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	/**
	 * Returns the indentation string used for pretty printing.
	 *
	 * @return the indentation string
	 */
	public String getIndentation() {
		return indentation;
	}

	/**
	 * Builder for creating FormatOptions instances.
	 */
	public static final class Builder {
		private int maxDepth = Integer.MAX_VALUE;
		private boolean includeTypes = true;
		private boolean includeIndices = true;
		private boolean prettyPrint = true;
		private String indentation = "  ";

		private Builder() {
		}

		/**
		 * Sets the maximum depth to traverse.
		 *
		 * @param maxDepth the max depth
		 * @return this builder
		 */
		public Builder maxDepth(int maxDepth) {
			this.maxDepth = maxDepth;
			return this;
		}

		/**
		 * Sets whether to include type information.
		 *
		 * @param includeTypes true to include types
		 * @return this builder
		 */
		public Builder includeTypes(boolean includeTypes) {
			this.includeTypes = includeTypes;
			return this;
		}

		/**
		 * Sets whether to include indices for container elements.
		 *
		 * @param includeIndices true to include indices
		 * @return this builder
		 */
		public Builder includeIndices(boolean includeIndices) {
			this.includeIndices = includeIndices;
			return this;
		}

		/**
		 * Sets whether to format output with indentation.
		 *
		 * @param prettyPrint true for pretty printing
		 * @return this builder
		 */
		public Builder prettyPrint(boolean prettyPrint) {
			this.prettyPrint = prettyPrint;
			return this;
		}

		/**
		 * Sets the indentation string.
		 *
		 * @param indentation the indentation string
		 * @return this builder
		 */
		public Builder indentation(String indentation) {
			this.indentation = indentation;
			return this;
		}

		/**
		 * Builds the FormatOptions.
		 *
		 * @return the built FormatOptions
		 */
		public FormatOptions build() {
			return new FormatOptions(maxDepth, includeTypes, includeIndices, prettyPrint, indentation);
		}
	}
}
