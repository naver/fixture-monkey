package com.navercorp.fixturemonkey.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.ExpressionNodeResolver;
import com.navercorp.fixturemonkey.resolver.NodeSetArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;

public final class ExpressionSpec {
	private final List<ArbitraryManipulator> manipulators;
	private final ArbitraryTraverser traverser;

	public ExpressionSpec(ArbitraryTraverser traverser) {
		this(new ArrayList<>(), traverser);
	}

	public ExpressionSpec(List<ArbitraryManipulator> manipulators, ArbitraryTraverser traverser) {
		this.manipulators = manipulators;
		this.traverser = traverser;
	}

	public ExpressionSpec map(String mapName, Consumer<MapSpec> mapSpecSupplier) {
		// make new map spec
		MapSpec mapSpec = new MapSpec(mapName);
		// set map spec using consumer accept
		mapSpecSupplier.accept(mapSpec);
		//expression spec에 mapspec 내용 추가
		mapSpec.visit(this);
		return this;
	}

	public Collection<ArbitraryManipulator> getManipulators() {
		return manipulators;
	}

	// 아래 방식대로 하면 traverser를 입력받을 수 없음! 일단은 ExpressionSpec이 traverser를 입력받을 수 있게 했지만 이렇게 구현하면 안됨.

	public ExpressionSpec set(String mapName, List<Object> keys, List<Boolean> isSetKey, Object value) {
		ExpressionNodeResolver nodeResolver =
			new ExpressionNodeResolver(ArbitraryExpression.from(mapName, keys, isSetKey));
		if (value instanceof Arbitrary) {
			manipulators.add(
				new ArbitraryManipulator(
					nodeResolver,
					new NodeSetArbitraryManipulator<>((Arbitrary<?>)value)
				)
			);
		} else if (value == null) {
			// TODO: setNull
		} else {
			manipulators.add(
				new ArbitraryManipulator(
					nodeResolver,
					new NodeSetDecomposedValueManipulator<>(traverser, value)
				)
			);
		}
		return this;
	}


}
