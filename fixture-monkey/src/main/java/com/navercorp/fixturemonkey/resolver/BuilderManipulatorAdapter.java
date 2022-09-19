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

package com.navercorp.fixturemonkey.resolver;

import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MAX_SIZE;
import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MIN_SIZE;

import java.util.AbstractMap;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpressionManipulator;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNullity;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetArbitrary;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetLazyValue;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetPostCondition;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;
import com.navercorp.fixturemonkey.arbitrary.ContainerSizeManipulator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class BuilderManipulatorAdapter {
	private final ArbitraryTraverser traverser;
	private final ManipulateOptions manipulateOptions;

	public BuilderManipulatorAdapter(ArbitraryTraverser traverser, ManipulateOptions manipulateOptions) {
		this.traverser = traverser;
		this.manipulateOptions = manipulateOptions;
	}

	public ArbitraryManipulator convertToArbitraryManipulator(BuilderManipulator builderManipulator) {
		NodeResolver nodeResolver = getNodeResolver(builderManipulator);
		NodeManipulator nodeManipulator = getNodeManipulator(builderManipulator);

		return new ArbitraryManipulator(
			nodeResolver,
			nodeManipulator
		);
	}

	public Map.Entry<NodeResolver, ArbitraryContainerInfo> convertToContainerInfosByNodeResolverEntry(
		BuilderManipulator builderManipulator
	) {
		NodeResolver nodeResolver = getNodeResolver(builderManipulator);
		ArbitraryContainerInfo containerInfo = getContainerInfo(builderManipulator);

		return new AbstractMap.SimpleEntry<>(
			nodeResolver,
			containerInfo
		);
	}

	private NodeResolver getNodeResolver(BuilderManipulator builderManipulator) {
		if (builderManipulator instanceof ArbitraryExpressionManipulator) {
			return ((ArbitraryExpressionManipulator)builderManipulator).getArbitraryExpression().toNodeResolver();
		}
		return IdentityNodeResolver.INSTANCE;
	}

	private NodeManipulator getNodeManipulator(BuilderManipulator builderManipulator) {
		if (builderManipulator instanceof ArbitrarySet) {
			ArbitrarySet<?> manipulator = (ArbitrarySet<?>)builderManipulator;
			int limit = safeCastLongToInt(manipulator.getLimit());
			return new ApplyNodeCountManipulator(
				new NodeSetDecomposedValueManipulator<>(
					traverser,
					manipulateOptions,
					manipulator.getApplicableValue()
				),
				limit
			);
		} else if (builderManipulator instanceof ArbitrarySetArbitrary) {
			ArbitrarySetArbitrary<?> manipulator = (ArbitrarySetArbitrary<?>)builderManipulator;
			int limit = safeCastLongToInt(manipulator.getLimit());
			return new ApplyNodeCountManipulator(
				new NodeSetLazyManipulator<>(
					traverser,
					manipulateOptions,
					LazyArbitrary.lazy(manipulator::getApplicableValue)
				),
				limit
			);
		} else if (builderManipulator instanceof ArbitrarySetLazyValue) {
			ArbitrarySetLazyValue<?> manipulator = (ArbitrarySetLazyValue<?>)builderManipulator;
			int limit = safeCastLongToInt(manipulator.getLimit());
			return new ApplyNodeCountManipulator(
				new NodeSetLazyManipulator<>(
					traverser,
					manipulateOptions,
					LazyArbitrary.lazy(manipulator::getApplicableValue)
				),
				limit
			);
		} else if (builderManipulator instanceof ArbitrarySetPostCondition) {
			ArbitrarySetPostCondition<?> manipulator = (ArbitrarySetPostCondition<?>)builderManipulator;
			int limit = safeCastLongToInt(manipulator.getLimit());
			return new ApplyNodeCountManipulator(
				new NodeFilterManipulator(
					manipulator.getClazz(),
					manipulator.getFilter()
				),
				limit
			);
		} else if (builderManipulator instanceof ArbitraryNullity) {
			ArbitraryNullity manipulator = (ArbitraryNullity)builderManipulator;
			return new NodeNullityManipulator(manipulator.toNull());
		} else {
			throw new IllegalArgumentException(
				"No convertable NodeManipulator exists : " + builderManipulator.getClass().getTypeName()
			);
		}
	}

	private ArbitraryContainerInfo getContainerInfo(BuilderManipulator builderManipulator) {
		if (builderManipulator instanceof ContainerSizeManipulator) {
			ContainerSizeManipulator manipulator = (ContainerSizeManipulator)builderManipulator;
			Integer min = manipulator.getMin();
			Integer max = manipulator.getMax();

			if (min == null && max != null) {
				min = Math.max(0, manipulator.getMax() - DEFAULT_ELEMENT_MAX_SIZE);
			} else if (min != null && max == null) {
				max = manipulator.getMin() + DEFAULT_ELEMENT_MAX_SIZE;
			}
			return new ArbitraryContainerInfo(min, max, true);
		} else {
			throw new IllegalArgumentException(
				"BuilderManipulator not convertable to ArbitraryContainerInfo. BuilderManipulator type : "
					+ builderManipulator.getClass().getTypeName()
			);
		}
	}

	private int safeCastLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
				"Limit should be within the range of int type. limit : " + l
			);
		}
		return (int)l;
	}
}
