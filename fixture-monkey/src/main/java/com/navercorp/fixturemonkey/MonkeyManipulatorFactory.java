package com.navercorp.fixturemonkey;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.resolver.ApplyNodeCountManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.ManipulateOptions;
import com.navercorp.fixturemonkey.resolver.NodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeNullityManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetLazyManipulator;

public final class MonkeyManipulatorFactory {
	private final ArbitraryTraverser traverser;
	private final ManipulateOptions manipulateOptions;

	public MonkeyManipulatorFactory(ArbitraryTraverser traverser, ManipulateOptions manipulateOptions) {
		this.traverser = traverser;
		this.manipulateOptions = manipulateOptions;
	}

	public NodeManipulator convertToNodeManipulator(@Nullable Object value) {
		if (value instanceof Arbitrary) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy(() -> ((Arbitrary<?>)value).sample()),
				false
			);
		} else if (value instanceof Supplier) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy((Supplier<?>)value),
				false
			);
		} else if (value instanceof LazyArbitrary) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				(LazyArbitrary<?>)value,
				false
			);
		} else if (value == null) {
			return new NodeNullityManipulator(true);
		} else {
			return new NodeSetDecomposedValueManipulator<>(traverser, manipulateOptions, value, false);
		}
	}
}
