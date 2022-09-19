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

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.arbitrary.AbstractArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpressionManipulator;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNullity;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetArbitrary;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetLazyValue;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetPostCondition;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;
import com.navercorp.fixturemonkey.arbitrary.ContainerSizeManipulator;

import java.util.AbstractMap;
import java.util.Map;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class BuilderManipulatorAdapter {
	private final ArbitraryTraverser traverser;
	private final ManipulateOptions manipulateOptions;

	public BuilderManipulatorAdapter(ArbitraryTraverser traverser, ManipulateOptions manipulateOptions) {
		this.traverser = traverser;
		this.manipulateOptions = manipulateOptions;
	}

	public ArbitraryManipulator convertToArbitraryManipulator(BuilderManipulator builderManipulator) {
		NodeResolver nodeResolver;
		if (builderManipulator instanceof ArbitraryExpressionManipulator) {
			nodeResolver = ((ArbitraryExpressionManipulator) builderManipulator).getArbitraryExpression()
				.toNodeResolver();
		} else {
			nodeResolver = IdentityNodeResolver.INSTANCE;
		}

		NodeManipulator nodeManipulator = null;
		if (builderManipulator instanceof ArbitrarySet) {
			ArbitrarySet<?> manipulator = (ArbitrarySet<?>) builderManipulator;
			nodeManipulator = new NodeSetDecomposedValueManipulator<>(
				traverser,
				manipulateOptions,
				manipulator.getApplicableValue()
			);
		} else if (builderManipulator instanceof ArbitrarySetArbitrary) {
			ArbitrarySetArbitrary<?> manipulator = (ArbitrarySetArbitrary<?>) builderManipulator;
			nodeManipulator = new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy(manipulator::getApplicableValue)
			);
		} else if (builderManipulator instanceof ArbitrarySetLazyValue) {
			ArbitrarySetLazyValue<?> manipulator = (ArbitrarySetLazyValue<?>) builderManipulator;
			nodeManipulator = new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy(manipulator::getApplicableValue)
			);
		} else if (builderManipulator instanceof ArbitrarySetPostCondition) {
			ArbitrarySetPostCondition<?> manipulator = (ArbitrarySetPostCondition<?>) builderManipulator;
			nodeManipulator = new NodeFilterManipulator(
				manipulator.getClazz(),
				manipulator.getFilter()
			);
		} else if (builderManipulator instanceof ArbitraryNullity) {
			ArbitraryNullity manipulator = (ArbitraryNullity) builderManipulator;
			nodeManipulator = new NodeNullityManipulator(manipulator.toNull());
		} else {
			throw new IllegalArgumentException(
				"No convertable NodeManipulator exists : " + builderManipulator.getClass().getTypeName()
			);
		}

		return new ArbitraryManipulator(
			nodeResolver,
			nodeManipulator
		);
	}

	public Map.Entry<NodeResolver, ArbitraryContainerInfo> convertToContainerInfosByNodeResolverEntry(BuilderManipulator builderManipulator) {
		NodeResolver nodeResolver;
		if (builderManipulator instanceof ArbitraryExpressionManipulator) {
			nodeResolver = ((ArbitraryExpressionManipulator) builderManipulator).getArbitraryExpression()
				.toNodeResolver();
		} else {
			nodeResolver = IdentityNodeResolver.INSTANCE;
		}

		ContainerSizeManipulator manipulator = (ContainerSizeManipulator) builderManipulator;

		return new AbstractMap.SimpleEntry<>(nodeResolver, new ArbitraryContainerInfo(manipulator.getMin(), manipulator.getMax(), true));
	}
}
