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

package com.navercorp.fixturemonkey.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.PriorityMatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.LazyPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.customizer.PathDirective;
import com.navercorp.fixturemonkey.customizer.SizeDirective;
import com.navercorp.fixturemonkey.api.type.Types;

/**
 * {@link FixtureMonkey} → {@link ArbitraryBuilder} → adapter pipeline → {@link CombinableArbitrary}
 * 1:N							1:N					1:1
 * <p>
 * It is a context within {@link ArbitraryBuilder}. It represents a status of the {@link ArbitraryBuilder}.
 * The {@link ArbitraryBuilder} should be the same if the {@link ArbitraryBuilderContext} is the same.
 * <p>
 * It is for internal use only. It can be changed or removed at any time.
 */
@API(since = "0.4.0", status = Status.INTERNAL)
public final class ArbitraryBuilderContext {
	private final List<PathDirective> directives;
	private final Map<Class<?>, List<Property>> propertyConfigurers;
	private final Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorsByType;
	private final MonkeyContext monkeyContext;

	private @Nullable Boolean optionValidOnly;

	private @Nullable Boolean customizedValidOnly;

	private @Nullable FixedState fixedState = null;
	private @Nullable CombinableArbitrary<?> fixedCombinableArbitrary;

	private ArbitraryBuilderContext(
		List<PathDirective> directives,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorsByType,
		@Nullable FixedState fixedState,
		@Nullable CombinableArbitrary<?> fixedCombinableArbitrary,
		MonkeyContext monkeyContext
	) {
		this.directives = directives;
		this.propertyConfigurers = propertyConfigurers;
		this.arbitraryIntrospectorsByType = arbitraryIntrospectorsByType;
		this.fixedState = fixedState;
		this.fixedCombinableArbitrary = fixedCombinableArbitrary;
		this.monkeyContext = monkeyContext;
	}

	/**
	 * It is in {@link ArbitraryBuilderContext} due to MonkeyContext is in api module.
	 * It will be removed when all related class migrate to api module.
	 */
	@Deprecated
	public static ArbitraryBuilderContext newBuilderContext(MonkeyContext monkeyContext) {
		return new ArbitraryBuilderContext(
			new ArrayList<>(),
			new HashMap<>(),
			new HashMap<>(),
			null, null,
			monkeyContext
		);
	}

	public ArbitraryBuilderContext copy() {
		List<PathDirective> copiedDirectives = new ArrayList<>(this.directives);

		ArbitraryBuilderContext copiedContext = new ArbitraryBuilderContext(
			copiedDirectives,
			new HashMap<>(propertyConfigurers),
			new HashMap<>(arbitraryIntrospectorsByType),
			fixedState,
			fixedCombinableArbitrary,
			monkeyContext
		);

		copiedContext.setCustomizedValidOnly(customizedValidOnly);
		copiedContext.setOptionValidOnly(optionValidOnly);

		return copiedContext;
	}

	public void addDirective(PathDirective directive) {
		this.directives.add(directive);
	}

	public void addDirectives(Collection<PathDirective> directives) {
		this.directives.addAll(directives);
	}

	public List<PathDirective> getDirectives() {
		return Collections.unmodifiableList(directives);
	}

	/**
	 * Filters this context's directives down to {@link SizeDirective}s. Used by adapter consumers
	 * that only care about container-size manipulation.
	 */
	public List<SizeDirective> getSizeDirectives() {
		List<SizeDirective> sizes = new ArrayList<>();
		for (PathDirective directive : directives) {
			if (directive instanceof SizeDirective) {
				sizes.add((SizeDirective)directive);
			}
		}
		return sizes;
	}

	/**
	 * Locks every {@link SizeDirective} on this context to a single random size — used by
	 * {@code ArbitraryBuilder.fixed()} so subsequent samples produce deterministic container sizes.
	 */
	public void fixContainerSizes() {
		for (int i = 0; i < directives.size(); i++) {
			PathDirective directive = directives.get(i);
			if (directive instanceof SizeDirective) {
				directives.set(i, ((SizeDirective)directive).fix());
			}
		}
	}

	public void putPropertyConfigurer(Class<?> type, List<Property> propertyConfigurer) {
		this.propertyConfigurers.put(type, propertyConfigurer);
	}

	public void putArbitraryIntrospector(Class<?> type, ArbitraryIntrospector arbitraryIntrospector) {
		this.arbitraryIntrospectorsByType.put(type, arbitraryIntrospector);
	}

	public Map<Class<?>, ArbitraryIntrospector> getArbitraryIntrospectorsByType() {
		return arbitraryIntrospectorsByType;
	}

	public Map<Class<?>, List<Property>> getPropertyConfigurers() {
		return propertyConfigurers;
	}

	public void setOptionValidOnly(@Nullable Boolean optionValidOnly) {
		this.optionValidOnly = optionValidOnly;
	}

	public void setCustomizedValidOnly(@Nullable Boolean customizedValidOnly) {
		this.customizedValidOnly = customizedValidOnly;
	}

	public boolean isValidOnly() {
		if (this.customizedValidOnly != null) {
			return this.customizedValidOnly;
		}

		if (this.optionValidOnly != null) {
			return this.optionValidOnly;
		}
		return true;
	}

	public void markFixed() {
		FixedState fixedStateLocal = fixedState;
		if (fixedStateLocal != null
			&& fixedStateLocal.getFixedManipulateSize() == this.directives.size()) {
			return;
		}

		fixedState = new FixedState(this.directives.size());
		fixedCombinableArbitrary = null;
	}

	public boolean isFixed() {
		return fixedState != null;
	}

	@SuppressWarnings({"dereference.of.nullable", "argument"})
	public boolean fixedExpired() {
		return directives.size() > Objects.requireNonNull(fixedState).getFixedManipulateSize();
	}

	public void renewFixed(CombinableArbitrary<?> fixedCombinableArbitrary) {
		this.markFixed();
		this.fixedCombinableArbitrary = fixedCombinableArbitrary;
	}

	public @Nullable CombinableArbitrary<?> getFixedCombinableArbitrary() {
		return fixedCombinableArbitrary;
	}

	private static class FixedState {
		private final int fixedManipulateSize;

		public FixedState(int fixedManipulateSize) {
			this.fixedManipulateSize = fixedManipulateSize;
		}

		public int getFixedManipulateSize() {
			return fixedManipulateSize;
		}
	}

	private static LazyPropertyGenerator initializeResolvedPropertyGenerator(
		Map<Class<?>, List<Property>> propertyConfigurers,
		List<MatcherOperator<PropertyGenerator>> optionalPropertyGenerators,
		ArbitraryGenerator defaultArbitraryGenerator,
		PropertyGenerator defaultPropertyGenerator
	) {
		PropertyGenerator resolvedPropertyGenerator = property -> {
			Class<?> type = property.getJvmType().getRawType();
			List<Property> propertyConfigurer = propertyConfigurers.get(type);
			if (propertyConfigurer != null) {
				return propertyConfigurer;
			}

			PropertyGenerator propertyGenerator = optionalPropertyGenerators.stream()
				.filter(it -> it.match(property))
				.map(MatcherOperator::getOperator)
				.findFirst()
				.orElse(null);

			if (propertyGenerator != null) {
				return propertyGenerator.generateChildProperties(property);
			}

			PropertyGenerator defaultArbitraryGeneratorPropertyGenerator =
				defaultArbitraryGenerator.getRequiredPropertyGenerator(property);

			if (defaultArbitraryGeneratorPropertyGenerator != null) {
				return defaultArbitraryGeneratorPropertyGenerator.generateChildProperties(property);
			}

			return defaultPropertyGenerator.generateChildProperties(property);
		};

		return new LazyPropertyGenerator(resolvedPropertyGenerator);
	}
}
