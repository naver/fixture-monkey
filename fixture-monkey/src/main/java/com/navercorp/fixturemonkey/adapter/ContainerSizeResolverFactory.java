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

package com.navercorp.fixturemonkey.adapter;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.analysis.AnalysisResult;
import com.navercorp.fixturemonkey.adapter.property.JvmNodePropertyFactory;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.SeedSnapshot;
import com.navercorp.objectfarm.api.node.SeedState;
import com.navercorp.objectfarm.api.tree.PathContainerSizeResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.tree.PathResolverContext;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Factory for creating {@link ContainerSizeResolver}s from manipulators and options.
 *
 * <p>Extracted from {@link AssemblyPlanner} to separate container size
 * resolution concerns from the main adapt orchestration logic.
 */
final class ContainerSizeResolverFactory {
	private static final int DEFAULT_MIN_CONTAINER_SIZE = 0;
	private static final int DEFAULT_MAX_CONTAINER_SIZE = 3;

	private final SeedState seedState;

	ContainerSizeResolverFactory(SeedState seedState) {
		this.seedState = seedState;
	}

	void addAnalysisContainerSizeResolvers(
		PathResolverContext.Builder builder,
		AnalysisResult analysisResult,
		List<Map.Entry<PathExpression, Integer>> wildcardSizeSequences
	) {
		Map<PathExpression, Integer> sequenceByPath = analysisResult.getContainerSizeSequenceByPath();
		for (PathResolver<ContainerSizeResolver> resolver : analysisResult.getContainerSizeResolvers()) {
			PathExpression resolverPath = extractResolverPath(resolver);
			if (resolverPath == null) {
				builder.addContainerSizeResolver(resolver);
				continue;
			}

			// Sequence wins: a wildcard resolver with higher sequence shadows any exact-path
			// resolver at a matching path. Build-time pruning is what enables this — the runtime
			// EXACT-over-WILDCARD precedence in JvmNodeTreeTransformer would otherwise prevent
			// the wildcard from taking effect at the shadowed exact path.
			if (!resolverPath.hasWildcard() && !wildcardSizeSequences.isEmpty()) {
				Integer ownSequence = sequenceByPath.get(resolverPath);
				boolean overriddenByWildcard = false;
				for (Map.Entry<PathExpression, Integer> wildcardEntry : wildcardSizeSequences) {
					if (wildcardEntry.getKey().matches(resolverPath)
						&& (ownSequence == null || wildcardEntry.getValue() > ownSequence)) {
						overriddenByWildcard = true;
						break;
					}
				}
				if (overriddenByWildcard) {
					continue;
				}
			}

			builder.addContainerSizeResolver(resolver);
		}
	}

	void addTypedContainerSizeResolvers(
		PathResolverContext.Builder builder,
		Map<JvmType, Map<String, ArbitraryContainerInfo>> typedContainerSizes
	) {
		for (Map.Entry<JvmType, Map<String, ArbitraryContainerInfo>> entry : typedContainerSizes.entrySet()) {
			JvmType ownerType = entry.getKey();
			for (Map.Entry<String, ArbitraryContainerInfo> fieldEntry : entry.getValue().entrySet()) {
				ArbitraryContainerInfo containerInfo = fieldEntry.getValue();
				int minSize = containerInfo.getElementMinSize();
				int maxSize = containerInfo.getElementMaxSize();
				ContainerSizeResolver resolver = containerType -> {
					if (minSize == maxSize) {
						return minSize;
					}
					int range = maxSize - minSize + 1;
					return minSize + nextRandomForType(containerType).nextInt(range);
				};
				builder.addTypedContainerSizeResolver(ownerType, fieldEntry.getKey(), resolver);
			}
		}
	}

	/**
	 * Creates a ContainerSizeResolver based on the given options.
	 *
	 * <p>Sizes come from the next {@link SeedState#snapshot()} combined with the container
	 * type's hash. Each resolve advances the seed sequence, so consecutive calls produce
	 * varied sizes; two adapters created with the same seed see the same sequence and
	 * therefore the same sizes for the same call pattern.
	 */
	ContainerSizeResolver createContainerSizeResolver(@Nullable FixtureMonkeyOptions options) {
		if (options != null) {
			return containerType -> {
				Property property = JvmNodePropertyFactory.fromType(containerType);
				ArbitraryContainerInfoGenerator generator = options.getArbitraryContainerInfoGenerator(property);

				ContainerPropertyGeneratorContext context = new ContainerPropertyGeneratorContext(
					property,
					null,
					generator
				);
				ArbitraryContainerInfo containerInfo = context.getContainerInfo();

				int minSize = containerInfo.getElementMinSize();
				int maxSize = containerInfo.getElementMaxSize();

				// Cap size for enum-based containers (EnumMap, EnumSet, Map<Enum,?>, Set<Enum>)
				Integer enumLimit = getEnumSizeLimit(containerType);
				if (enumLimit != null) {
					maxSize = Math.min(maxSize, enumLimit);
					minSize = Math.min(minSize, maxSize);
				}

				if (minSize == maxSize) {
					return minSize;
				}
				return minSize + nextRandomForType(containerType).nextInt(maxSize - minSize + 1);
			};
		}

		return containerType -> {
			int range = DEFAULT_MAX_CONTAINER_SIZE - DEFAULT_MIN_CONTAINER_SIZE + 1;
			return DEFAULT_MIN_CONTAINER_SIZE + nextRandomForType(containerType).nextInt(range);
		};
	}

	private Random nextRandomForType(JvmType containerType) {
		// Use the dedicated container-size counter on SeedState so cache hit/miss in other
		// snapshot() consumers does not perturb the size sequence. Combining the snapshot
		// with the type hash (via SeedSnapshot.seedFor) gives per-call variation plus
		// per-type spread. Class.hashCode() is identityHashCode and varies across JVM runs,
		// so we hash the fully-qualified class name instead to keep seeds reproducible.
		return seedState.containerSizeSnapshot().randomFor(stableTypeHash(containerType));
	}

	private static int stableTypeHash(JvmType containerType) {
		return containerType.getRawType().getName().hashCode();
	}

	/**
	 * Creates a deterministic ContainerSizeResolver for fixed() mode.
	 * Uses a constant snapshot (sequence=0) so every resolve produces the same sizes.
	 * Respects annotation-based size constraints (e.g., @Size) when options are available.
	 */
	ContainerSizeResolver createFixedContainerSizeResolver(@Nullable FixtureMonkeyOptions options) {
		SeedSnapshot fixedSnapshot = seedState.snapshotAt(0);

		return containerType -> {
			int minSize = DEFAULT_MIN_CONTAINER_SIZE;
			int maxSize = DEFAULT_MAX_CONTAINER_SIZE;

			if (options != null) {
				Property property = JvmNodePropertyFactory.fromType(containerType);
				ArbitraryContainerInfoGenerator generator = options.getArbitraryContainerInfoGenerator(property);
				ContainerPropertyGeneratorContext context = new ContainerPropertyGeneratorContext(
					property,
					null,
					generator
				);
				ArbitraryContainerInfo containerInfo = context.getContainerInfo();
				minSize = containerInfo.getElementMinSize();
				maxSize = containerInfo.getElementMaxSize();

				Integer enumLimit = getEnumSizeLimit(containerType);
				if (enumLimit != null) {
					maxSize = Math.min(maxSize, enumLimit);
					minSize = Math.min(minSize, maxSize);
				}
			}

			if (minSize == maxSize) {
				return minSize;
			}
			int range = maxSize - minSize + 1;
			Random typeRandom = fixedSnapshot.randomFor(stableTypeHash(containerType));
			return minSize + typeRandom.nextInt(range);
		};
	}

	private static @Nullable PathExpression extractResolverPath(PathResolver<ContainerSizeResolver> resolver) {
		if (resolver instanceof PathContainerSizeResolver) {
			PathContainerSizeResolver sizeResolver = (PathContainerSizeResolver) resolver;
			return sizeResolver.getPattern();
		}
		return null;
	}

	static @Nullable Integer getEnumSizeLimit(JvmType containerType) {
		Class<?> rawType = containerType.getRawType();
		List<? extends JvmType> typeVariables = containerType.getTypeVariables();

		// Map with enum key
		if (Map.class.isAssignableFrom(rawType) && typeVariables != null && !typeVariables.isEmpty()) {
			Class<?> keyType = typeVariables.get(0).getRawType();
			Object[] keyConstants = keyType.getEnumConstants();
			if (keyType.isEnum() && keyConstants != null) {
				return keyConstants.length;
			}
		}

		// Set with enum element
		if (java.util.Set.class.isAssignableFrom(rawType) && typeVariables != null && !typeVariables.isEmpty()) {
			Class<?> elementType = typeVariables.get(0).getRawType();
			Object[] elementConstants = elementType.getEnumConstants();
			if (elementType.isEnum() && elementConstants != null) {
				return elementConstants.length;
			}
		}

		return null;
	}
}
