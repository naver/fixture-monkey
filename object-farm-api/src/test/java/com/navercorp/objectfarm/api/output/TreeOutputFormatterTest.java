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

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.node.JavaDefaultNodePromoter;
import com.navercorp.objectfarm.api.node.JavaMapNodePromoter;
import com.navercorp.objectfarm.api.node.JavaNodeContext;
import com.navercorp.objectfarm.api.node.JavaObjectNodePromoter;
import com.navercorp.objectfarm.api.node.JvmNodePromoter;
import com.navercorp.objectfarm.api.node.RandomContainerSizeResolver;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTree;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.tree.JvmNodeTreeTransformer;
import com.navercorp.objectfarm.api.type.JavaType;

class TreeOutputFormatterTest {

	private static final JvmNodePromoter PROMOTER = new JavaDefaultNodePromoter(
		Arrays.asList(
			new JavaObjectNodePromoter(),
			new JavaMapNodePromoter()
		)
	);

	private static final JavaNodeContext CONTEXT = JavaNodeContext.builder()
		.seed(12345L)
		.nodePromoters(Arrays.asList(PROMOTER))
		.containerSizeResolver(new RandomContainerSizeResolver())
		.build();

	private JvmNodeTree tree;

	@BeforeEach
	void setUp() {
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(new JavaType(TestUser.class),
			CONTEXT).build();
		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);
		tree = transformer.transform(candidateTree);
	}

	@Test
	void jsonFormatterShouldProduceValidJson() {
		// given
		JsonTreeFormatter formatter = new JsonTreeFormatter();
		FormatOptions options = FormatOptions.defaults();

		// when
		String result = formatter.format(tree, options);

		// then
		then(result).startsWith("{");
		then(result).endsWith("}");
		then(result).contains("\"root\"");
		then(result).contains("\"name\"");
		then(result).contains("\"type\"");
		then(result).contains("\"children\"");
		then(result).contains("\"metadata\"");
		then(result).contains("\"totalNodes\"");
	}

	@Test
	void jsonFormatterCompactModeShouldNotContainNewlines() {
		// given
		JsonTreeFormatter formatter = new JsonTreeFormatter();
		FormatOptions options = FormatOptions.compact();

		// when
		String result = formatter.format(tree, options);

		// then
		then(result).doesNotContain("\n");
	}

	@Test
	void markdownFormatterShouldProduceReadableOutput() {
		// given
		MarkdownTreeFormatter formatter = new MarkdownTreeFormatter();
		FormatOptions options = FormatOptions.defaults();

		// when
		String result = formatter.format(tree, options);

		// then
		then(result).contains("# Type Structure:");
		then(result).contains("TestUser");
		then(result).contains("- **");  // member formatting
	}

	@Test
	void promptOptimizedFormatterShouldProducePathSyntax() {
		// given
		PromptOptimizedFormatter formatter = new PromptOptimizedFormatter();
		FormatOptions options = FormatOptions.defaults();

		// when
		String result = formatter.format(tree, options);

		// then
		then(result).contains("TYPE_TREE:");
		then(result).contains("STRUCTURE:");
		then(result).contains("$.");  // Path syntax
	}

	@Test
	void registryShouldSelectCorrectFormatter() {
		// given
		TreeOutputFormatterRegistry registry = TreeOutputFormatterRegistry.defaults();

		// when
		String jsonResult = registry.format(tree, OutputFormat.JSON);
		String markdownResult = registry.format(tree, OutputFormat.MARKDOWN);
		String promptResult = registry.format(tree, OutputFormat.PROMPT_OPTIMIZED);

		// then
		then(jsonResult).startsWith("{");
		then(markdownResult).contains("# Type Structure:");
		then(promptResult).contains("TYPE_TREE:");
	}

	@Test
	void formatOptionsShouldControlMaxDepth() {
		// given
		JsonTreeFormatter formatter = new JsonTreeFormatter();
		FormatOptions shallow = FormatOptions.builder().maxDepth(0).prettyPrint(false).build();
		FormatOptions deep = FormatOptions.builder().maxDepth(5).prettyPrint(false).build();

		// when
		String shallowResult = formatter.format(tree, shallow);
		String deepResult = formatter.format(tree, deep);

		// then - shallow result should have empty children at root level
		then(shallowResult.length()).isLessThan(deepResult.length());
	}

	@Test
	void formatOptionsShouldControlTypeInclusion() {
		// given
		JsonTreeFormatter formatter = new JsonTreeFormatter();
		FormatOptions withTypes = FormatOptions.builder().includeTypes(true).prettyPrint(false).build();
		FormatOptions withoutTypes = FormatOptions.builder().includeTypes(false).prettyPrint(false).build();

		// when
		String withTypesResult = formatter.format(tree, withTypes);
		String withoutTypesResult = formatter.format(tree, withoutTypes);

		// then
		then(withTypesResult).contains("\"type\":");
		then(withoutTypesResult).doesNotContain("\"type\":");
	}

	@SuppressWarnings("unused")
	static class TestUser {
		private String name;
		private int age;
		private List<String> tags;
	}
}
