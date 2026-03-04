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

package com.navercorp.fixturemonkey.adapter.nodecandidate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.BaseStream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.property.JvmNodePropertyFactory;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.JavaNode;
import com.navercorp.objectfarm.api.node.JvmContainerNodeGenerator;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.node.JvmNodeContext;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Bridges {@link ContainerPropertyGenerator} to {@link JvmContainerNodeGenerator}.
 * <p>
 * This adapter allows fixture-monkey's {@link ContainerPropertyGenerator} (used in the non-adapter path)
 * to be used within the adapter path's node tree transformation infrastructure.
 * <p>
 * Only matches types that are NOT already handled by the default {@link JvmContainerNodeGenerator}s
 * (Array, Collection, Map, Supplier, Optional, etc.). This ensures that standard container types
 * continue to use the optimized default generators while custom container types (e.g., Kotlin lambdas)
 * are handled by delegating to the registered {@link ContainerPropertyGenerator}s.
 *
 * @since 1.1.17
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class ContainerPropertyGeneratorNodeGenerator implements JvmContainerNodeGenerator {
	private final List<MatcherOperator<ContainerPropertyGenerator>> generators;

	public ContainerPropertyGeneratorNodeGenerator(List<MatcherOperator<ContainerPropertyGenerator>> generators) {
		this.generators = generators;
	}

	@Override
	public boolean isSupported(JvmType containerType) {
		if (isHandledByDefaultGenerators(containerType.getRawType())) {
			return false;
		}
		return findMatchingGenerator(containerType) != null;
	}

	@Override
	public List<JvmNode> generateContainerElements(JvmNode containerNode, JvmNodeContext context) {
		return generateContainerElements(containerNode, context, context.getContainerSizeResolver());
	}

	@Override
	public List<JvmNode> generateContainerElements(
		JvmNode containerNode,
		JvmNodeContext context,
		ContainerSizeResolver sizeResolver
	) {
		JvmType containerType = containerNode.getConcreteType();
		ContainerPropertyGenerator generator = findMatchingGenerator(containerType);
		if (generator == null) {
			return new ArrayList<>();
		}

		Property property = JvmNodePropertyFactory.fromType(containerType);
		ContainerPropertyGeneratorContext genContext = new ContainerPropertyGeneratorContext(property, null, null);
		ContainerProperty containerProperty = generator.generate(genContext);

		List<Property> childProperties = containerProperty.getElementProperties();
		List<JvmNode> elements = new ArrayList<>(childProperties.size());
		for (Property childProperty : childProperties) {
			JvmType elementType = Types.toJvmType(childProperty.getAnnotatedType(), childProperty.getAnnotations());
			elements.add(new JavaNode(elementType, ""));
		}
		return elements;
	}

	private @Nullable ContainerPropertyGenerator findMatchingGenerator(JvmType containerType) {
		Property property = JvmNodePropertyFactory.fromType(containerType);
		for (MatcherOperator<ContainerPropertyGenerator> op : generators) {
			if (op.getMatcher().match(property)) {
				return op.getOperator();
			}
		}
		return null;
	}

	private static boolean isHandledByDefaultGenerators(Class<?> rawType) {
		return (rawType.isArray()
			|| Collection.class.isAssignableFrom(rawType)
			|| Iterable.class.isAssignableFrom(rawType)
			|| Iterator.class.isAssignableFrom(rawType)
			|| Map.class.isAssignableFrom(rawType)
			|| Map.Entry.class.isAssignableFrom(rawType)
			|| BaseStream.class.isAssignableFrom(rawType)
			|| Supplier.class.isAssignableFrom(rawType)
			|| Function.class.isAssignableFrom(rawType)
			|| Optional.class.isAssignableFrom(rawType)
			|| OptionalInt.class.isAssignableFrom(rawType)
			|| OptionalLong.class.isAssignableFrom(rawType)
			|| OptionalDouble.class.isAssignableFrom(rawType));
	}
}
