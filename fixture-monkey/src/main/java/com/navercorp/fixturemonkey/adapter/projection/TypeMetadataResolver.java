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

package com.navercorp.fixturemonkey.adapter.projection;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Supplier;
import java.util.stream.BaseStream;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.property.JvmNodePropertyFactory;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Stateless helpers around per-type assembly metadata: container detection, name resolver,
 * null inject generator, and the {@link CachedTypeMetadata} write-back.
 *
 * <p>All methods are pure functions of {@code AssemblyState} (or {@code FixtureMonkeyOptions})
 * + the input node/type/property. Extracted from {@code ValueProjectionAssembler} to keep
 * the assembler focused on tree-traversal driver logic.</p>
 */
final class TypeMetadataResolver {
	private TypeMetadataResolver() {
	}

	static PropertyNameResolver resolveNameResolver(JvmNode node, Property property, AssemblyState state) {
		if (state.typeMetadataCache != null) {
			CachedTypeMetadata cached = state.typeMetadataCache.get(node.getConcreteType());
			if (cached != null) {
				return cached.nameResolver;
			}
		}
		return state.options.getPropertyNameResolver(property);
	}

	static NullInjectGenerator resolveNullInjectGenerator(JvmNode node, Property property, AssemblyState state) {
		if (state.typeMetadataCache != null) {
			CachedTypeMetadata cached = state.typeMetadataCache.get(node.getConcreteType());
			if (cached != null) {
				return cached.nullInjectGenerator;
			}
		}
		return state.options.getNullInjectGenerator(property);
	}

	@SuppressWarnings("deprecation")
	static void writeBackTypeMetadata(JvmNode node, Property property, AssemblyState state) {
		if (state.typeMetadataCache == null) {
			return;
		}
		JvmType jvmType = node.getConcreteType();
		if (!state.typeMetadataCache.containsKey(jvmType)) {
			PropertyNameResolver resolver = state.options.getPropertyNameResolver(property);
			NullInjectGenerator generator = state.options.getNullInjectGenerator(property);
			boolean isContainer = computeIsContainerType(jvmType, state.options);
			boolean hasCandidateResolvers = state.options.getCandidateConcretePropertyResolver(property) != null;
			state.typeMetadataCache.putIfAbsent(
				jvmType,
				new CachedTypeMetadata(resolver, generator, isContainer, hasCandidateResolvers)
			);
		}
	}

	static boolean computeIsContainerType(JvmType jvmType, @Nullable FixtureMonkeyOptions options) {
		Class<?> rawType = jvmType.getRawType();
		if (
			rawType.isArray()
				|| Collection.class.isAssignableFrom(rawType)
				|| Map.class.isAssignableFrom(rawType)
				|| Map.Entry.class.isAssignableFrom(rawType)
				|| Iterable.class.isAssignableFrom(rawType)
				|| BaseStream.class.isAssignableFrom(rawType)
				|| isSingleElementWrapper(jvmType)
		) {
			return true;
		}

		if (options != null) {
			Property property = JvmNodePropertyFactory.fromType(jvmType);
			for (MatcherOperator<ContainerPropertyGenerator> op : options.getContainerPropertyGenerators()) {
				if (op.getMatcher().match(property)) {
					return true;
				}
			}
		}

		return false;
	}

	static boolean isSingleElementWrapper(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();
		return (Supplier.class.isAssignableFrom(rawType)
			|| Optional.class.isAssignableFrom(rawType)
			|| OptionalInt.class.isAssignableFrom(rawType)
			|| OptionalLong.class.isAssignableFrom(rawType)
			|| OptionalDouble.class.isAssignableFrom(rawType));
	}
}
