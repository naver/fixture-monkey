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

import java.util.HashMap;
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
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
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
 * <p>Extracted from {@link DefaultNodeTreeAdapter} to separate container size
 * resolution concerns from the main adapt orchestration logic.
 */
final class ContainerSizeResolverFactory {
	private static final int DEFAULT_MIN_CONTAINER_SIZE = 0;
	private static final int DEFAULT_MAX_CONTAINER_SIZE = 3;

	private final SeedState seedState;

	ContainerSizeResolverFactory(SeedState seedState) {
		this.seedState = seedState;
	}

	Map<PathExpression, Integer> collectSizeSequences(
		List<ContainerInfoManipulator> containerManipulators,
		Map<ContainerInfoManipulator, PathExpression> containerPathCache
	) {
		Map<PathExpression, Integer> sizeSequenceByPath = new HashMap<>();
		for (ContainerInfoManipulator manipulator : containerManipulators) {
			PathExpression path = containerPathCache.get(manipulator);
			if (path != null) {
				sizeSequenceByPath.put(path, manipulator.getManipulatingSequence());
			}
		}
		return sizeSequenceByPath;
	}

	void addLazyContainerSizeResolvers(
		PathResolverContext.Builder builder,
		AnalysisResult analysisResult,
		Map<PathExpression, Integer> sizeSequenceByPath,
		List<Map.Entry<PathExpression, Integer>> wildcardSizeSequences
	) {
		Map<PathExpression, Integer> lazyContainerSizeSequence = analysisResult.getContainerSizeSequenceByPath();
		for (PathResolver<ContainerSizeResolver> resolver : analysisResult.getContainerSizeResolvers()) {
			PathExpression resolverPath = extractResolverPath(resolver);
			if (resolverPath == null) {
				builder.addContainerSizeResolver(resolver);
				continue;
			}

			Integer explicitSizeSequence = sizeSequenceByPath.get(resolverPath);
			Integer lazySizeSequence = lazyContainerSizeSequence.get(resolverPath);

			// If no explicit size() call for this exact path, check wildcard matches
			if (explicitSizeSequence == null) {
				if (lazySizeSequence != null && !wildcardSizeSequences.isEmpty()) {
					boolean overriddenByWildcard = false;
					for (Map.Entry<PathExpression, Integer> wildcardEntry : wildcardSizeSequences) {
						if (
							wildcardEntry.getKey().matches(resolverPath) && wildcardEntry.getValue() > lazySizeSequence
						) {
							overriddenByWildcard = true;
							break;
						}
					}
					if (overriddenByWildcard) {
						continue;
					}
				}
				builder.addContainerSizeResolver(resolver);
				continue;
			}

			// If lazy sequence is higher than explicit size sequence, the lazy value should win
			if (lazySizeSequence != null && lazySizeSequence > explicitSizeSequence) {
				builder.addContainerSizeResolver(resolver);
			}
		}
	}

	void addManipulatorContainerSizeResolvers(
		PathResolverContext.Builder builder,
		List<ContainerInfoManipulator> containerManipulators,
		Map<PathExpression, Integer> globalValueSequences,
		Map<PathExpression, Integer> sizeSequenceByPath,
		Map<ContainerInfoManipulator, PathExpression> containerPathCache
	) {
		for (ContainerInfoManipulator manipulator : containerManipulators) {
			PathExpression path = containerPathCache.get(manipulator);
			if (path == null) {
				continue;
			}

			Integer setSequence = globalValueSequences.get(path);
			Integer sizeSequence = sizeSequenceByPath.get(path);

			// Skip if set() was called after size() for this path — sequence determines priority
			if (setSequence != null && sizeSequence != null && setSequence > sizeSequence) {
				continue;
			}

			ArbitraryContainerInfo containerInfo = manipulator.getContainerInfo();
			int minSize = containerInfo.getElementMinSize();
			int maxSize = containerInfo.getElementMaxSize();

			ContainerSizeResolver sizeResolver = containerType -> {
				int effectiveMin = minSize;
				int effectiveMax = maxSize;

				Integer enumLimit = getEnumSizeLimit(containerType);
				if (enumLimit != null) {
					effectiveMax = Math.min(effectiveMax, enumLimit);
					effectiveMin = Math.min(effectiveMin, effectiveMax);
				}

				if (effectiveMin == effectiveMax) {
					return effectiveMin;
				}
				return effectiveMin + (int) (Math.random() * (effectiveMax - effectiveMin + 1));
			};

			builder.addContainerSizeResolver(new PathContainerSizeResolver(path, sizeResolver));
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
					return minSize + (int) (Math.random() * (maxSize - minSize + 1));
				};
				builder.addTypedContainerSizeResolver(ownerType, fieldEntry.getKey(), resolver);
			}
		}
	}

	/**
	 * Creates a ContainerSizeResolver based on the given options.
	 * Each invocation generates a new snapshot from seedState for reproducible randomness.
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
				Random typeRandom = seedState.snapshot().randomFor(containerType.hashCode());
				return minSize + typeRandom.nextInt(maxSize - minSize + 1);
			};
		}

		return containerType -> {
			int range = DEFAULT_MAX_CONTAINER_SIZE - DEFAULT_MIN_CONTAINER_SIZE + 1;
			Random typeRandom = seedState.snapshot().randomFor(containerType.hashCode());
			return DEFAULT_MIN_CONTAINER_SIZE + typeRandom.nextInt(range);
		};
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
			Random typeRandom = fixedSnapshot.randomFor(containerType.hashCode());
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
