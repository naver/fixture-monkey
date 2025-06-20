/*
 * Test for nested container element fields
 */
package com.navercorp.objectfarm.api.tree;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.JavaDefaultNodePromoter;
import com.navercorp.objectfarm.api.node.JavaInterfaceNodePromoter;
import com.navercorp.objectfarm.api.node.JavaMapNodePromoter;
import com.navercorp.objectfarm.api.node.JavaNodeContext;
import com.navercorp.objectfarm.api.node.JavaObjectNodePromoter;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.node.JvmNodePromoter;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.type.JavaType;

class NestedContainerTreeTest {
	@Test
	void stringValueCandidateTreeHasValueField() {
		// Given
		JavaNodeContext context = createContext();
		JavaType stringValueType = new JavaType(StringValue.class);

		// When
		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(stringValueType, context)
			.withTreeContext(new JvmNodeCandidateTreeContext())
			.build();

		JvmNodeCandidate rootCandidate = tree.getRootNode();
		List<JvmNodeCandidate> children = tree.getChildren(rootCandidate);

		// Then - StringValue should have "value" field as child
		then(children).hasSize(1);
		then(children.get(0).getName()).isEqualTo("value");
	}

	@Test
	void nestedStringListHolderNodeTreeHasNestedFields() {
		// Given
		JavaNodeContext context = createContext();
		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();
		JavaType rootType = new JavaType(NestedStringListHolder.class);

		// Build candidate tree
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(rootType, context)
			.withTreeContext(treeContext)
			.withPreBuildResolvedTypes(true)
			.build();

		// Transform to node tree
		PathResolverContext resolverContext = PathResolverContext.builder()
			.addContainerSizeResolver("$.values", 1)
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(context, treeContext, resolverContext);
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// Then - verify tree structure
		JvmNode root = nodeTree.getRootNode();

		// Verify: $.values[0] should have "value" as child
		List<JvmNode> rootChildren = nodeTree.getChildren(root);
		then(rootChildren).isNotEmpty();

		JvmNode valuesNode = rootChildren.stream()
			.filter(n -> "values".equals(n.getNodeName()))
			.findFirst()
			.orElse(null);
		then(valuesNode).isNotNull();

		List<JvmNode> valuesChildren = nodeTree.getChildren(valuesNode);
		then(valuesChildren).isNotEmpty();

		JvmNode element0 = valuesChildren.get(0);
		List<JvmNode> element0Children = nodeTree.getChildren(element0);

		// This is what we expect to work
		then(element0Children).hasSize(1);
		then(element0Children.get(0).getNodeName()).isEqualTo("value");
	}

	private JavaNodeContext createContext() {
		ContainerSizeResolver sizeResolver = containerType -> 1;

		List<JvmNodePromoter> promoters = java.util.Arrays.asList(
			new JavaInterfaceNodePromoter(),
			new JavaMapNodePromoter(),
			new JavaObjectNodePromoter()
		);

		return JavaNodeContext.builder()
			.seed(12345L)
			.nodePromoters(Collections.singletonList(new JavaDefaultNodePromoter(promoters)))
			.containerSizeResolver(sizeResolver)
			.build();
	}

	// Test class
	static class StringValue {
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	// Test class with List<StringValue>
	static class NestedStringListHolder {
		private List<StringValue> values;

		public List<StringValue> getValues() {
			return values;
		}

		public void setValues(List<StringValue> values) {
			this.values = values;
		}
	}
}
