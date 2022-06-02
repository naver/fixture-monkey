package com.navercorp.fixturemonkey.resolver;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.MapKeyElementProperty;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;

public class NodeAddMapEntryManipulator implements NodeManipulator {
	private final ArbitraryTraverser traverser;
	private final Object key;
	private final Object value;

	public NodeAddMapEntryManipulator(ArbitraryTraverser traverser, Object key, Object value) {
		this.traverser = traverser;
		this.key = key;
		this.value = value;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		//Todo: map entry 타입이 key value 타입과 일치하는지 체크

		ArbitraryNode entryNode = addEntry(arbitraryNode);
		ArbitraryNode keyNode = entryNode.getChildren().get(0);
		ArbitraryNode valueNode = entryNode.getChildren().get(1);

		if (key instanceof Arbitrary) {
			NodeSetArbitraryManipulator<?> nodeSetArbitraryManipulator =
				new NodeSetArbitraryManipulator<>((Arbitrary<?>)key);
			nodeSetArbitraryManipulator.manipulate(keyNode);
		} else if (key != null) {
			NodeSetDecomposedValueManipulator<?> nodeSetDecomposedValueManipulator =
				new NodeSetDecomposedValueManipulator<>(traverser, key);
			nodeSetDecomposedValueManipulator.manipulate(keyNode);
		}

		if (value instanceof Arbitrary) {
			NodeSetArbitraryManipulator<?> nodeSetArbitraryManipulator =
				new NodeSetArbitraryManipulator<>((Arbitrary<?>)value);
			nodeSetArbitraryManipulator.manipulate(valueNode);
		} else if (value != null) {
			NodeSetDecomposedValueManipulator<?> nodeSetDecomposedValueManipulator =
				new NodeSetDecomposedValueManipulator<>(traverser, value);
			nodeSetDecomposedValueManipulator.manipulate(valueNode);
		}
	}

	public ArbitraryNode addEntry(ArbitraryNode arbitraryNode) {
		//generate new node
		ArbitraryProperty arbitraryProperty = arbitraryNode.getArbitraryProperty();
		ArbitraryContainerInfo containerInfo = arbitraryProperty
			.getContainerInfo().withElementMinSize(1).withElementMaxSize(1);
		ArbitraryNode entryNode = traverser.traverse(arbitraryProperty.getProperty(),
			containerInfo).getChildren().get(0);

		// Todo: set sequence
		// entryProperty, keyproperty, elementproperty
		// MapKeyElementProperty keyElementProperty = (MapKeyElementProperty)entryNode.getChildren().get(0).getProperty();
		// keyElementProperty.setSequence(arbitraryNode.getChildren().size());
		// entryNode.getChildren().get(0).setArbitraryProperty();

		//Add ChildProperty & EntryNode
		arbitraryProperty.getChildProperties().add(entryNode.getProperty());
		arbitraryNode.getChildren().add(entryNode);

		// ArbitraryContainerInfo newContainerInfo = arbitraryProperty
		// 	.getContainerInfo().withElementMinSize(1).withElementMaxSize(1);
		//
		// arbitraryNode.setArbitraryProperty(
		// 	arbitraryProperty
		// 		.withChildProperties(newMapProperty)
		// 		.withContainerInfo(arbitraryProperty.getContainerInfo())
		// );
		return entryNode;
	}
}
