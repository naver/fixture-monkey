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
 * Enum representing the available output formats for {@link JvmNodeCandidateTree}.
 * <p>
 * Each format is optimized for different use cases:
 * <ul>
 *   <li>{@link #JSON} - Machine-readable format suitable for API responses and data exchange</li>
 *   <li>{@link #MARKDOWN} - Human-readable documentation format</li>
 *   <li>{@link #PROMPT_OPTIMIZED} - Format optimized for LLM consumption with path syntax</li>
 * </ul>
 */
public enum OutputFormat {
	/**
	 * JSON format for machine consumption.
	 * Outputs a hierarchical JSON structure with type information and metadata.
	 */
	JSON,

	/**
	 * Markdown format for human-readable documentation.
	 * Outputs a formatted tree structure with type information.
	 */
	MARKDOWN,

	/**
	 * Prompt-optimized format for LLM consumption.
	 * Outputs a concise structure with path notation for easy navigation.
	 */
	PROMPT_OPTIMIZED
}
