package com.navercorp.fixturemonkey.resolver;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.MapKeyElementProperty;

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

		//map 자체를 set하려는 것이랑 entry를 add 하려는 것을 어떻게 구분하면 좋을까?
		//일단은 새로운 manipulator를 만들어서 사용
		ArbitraryNode entryNode = addEntry(arbitraryNode);
		ArbitraryNode keyNode = entryNode.getChildren().get(0);
		ArbitraryNode valueNode = entryNode.getChildren().get(1);

		//Todo: null일 경우?
		if (key instanceof Arbitrary) {
			NodeSetArbitraryManipulator<?> nodeSetArbitraryManipulator = new NodeSetArbitraryManipulator<>(
				(Arbitrary<?>)key);
			nodeSetArbitraryManipulator.manipulate(keyNode);
		} else if (key == null) {

		}
		else {
			NodeSetDecomposedValueManipulator nodeSetDecomposedValueManipulator =
				new NodeSetDecomposedValueManipulator<>(traverser, key);
			nodeSetDecomposedValueManipulator.manipulate(keyNode);
		}

		if (value instanceof Arbitrary) {
			NodeSetArbitraryManipulator<?> nodeSetArbitraryManipulator = new NodeSetArbitraryManipulator<>(
				(Arbitrary<?>)value);
			nodeSetArbitraryManipulator.manipulate(valueNode);
		} else if (value == null) {

		}
		else {
			NodeSetDecomposedValueManipulator nodeSetDecomposedValueManipulator =
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


		// set sequence
		MapKeyElementProperty keyElementProperty = (MapKeyElementProperty)entryNode.getChildren().get(0).getProperty();
		keyElementProperty.setSequence(arbitraryNode.getChildren().size());

		//Add ChildProperty & EntryNode
		arbitraryProperty.getChildProperties().add(entryNode.getProperty());
		arbitraryNode.getChildren().add(entryNode);

		// selectedNode.setArbitraryProperty(
		// 	arbitraryProperty
		// 		.withChildProperties(newMapProperty)
		// 		//Todo: change min max size
		// 		.withContainerInfo(arbitraryProperty.getContainerInfo())
		// );
		return entryNode;
	}
}
