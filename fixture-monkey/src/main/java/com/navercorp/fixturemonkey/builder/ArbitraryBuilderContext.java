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
import java.util.concurrent.atomic.AtomicInteger;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.instantiator.InstantiatorProcessResult;
import com.navercorp.fixturemonkey.customizer.PathDirective;
import com.navercorp.fixturemonkey.customizer.Scope;
import com.navercorp.fixturemonkey.customizer.ScopeSelector;
import com.navercorp.fixturemonkey.customizer.SizeDirective;
import com.navercorp.objectfarm.api.node.SeedSnapshot;

/**
 * {@link FixtureMonkey} → {@link ArbitraryBuilder} → {@link CombinableArbitrary}
 * 1:N							1:1
 * <p>
 * It is a context within {@link ArbitraryBuilder}. It represents a status of the {@link ArbitraryBuilder}.
 * The {@link ArbitraryBuilder} should be the same if the {@link ArbitraryBuilderContext} is the same.
 * <p>
 * It is for internal use only. It can be changed or removed at any time.
 */
@API(since = "0.4.0", status = Status.INTERNAL)
public final class ArbitraryBuilderContext {
	private static final long SAMPLE = "sample".hashCode();
	private static final long CHILD = "child".hashCode();
	private static final long FIX = "fix".hashCode();

	private final List<PathDirective> directives;
	private final Map<Class<?>, InstantiatorProcessResult> instantiatorsByType;
	private final MonkeyContext monkeyContext;
	private final SeedSnapshot builderScope;
	private final AtomicInteger sampleCount = new AtomicInteger();
	private final AtomicInteger childCount = new AtomicInteger();
	private final AtomicInteger fixCount = new AtomicInteger();

	private @Nullable Boolean optionValidOnly;

	private @Nullable Boolean customizedValidOnly;

	private @Nullable FixedState fixedState = null;
	private @Nullable CombinableArbitrary<?> fixedCombinableArbitrary;

	private ArbitraryBuilderContext(
		List<PathDirective> directives,
		Map<Class<?>, InstantiatorProcessResult> instantiatorsByType,
		@Nullable FixedState fixedState,
		@Nullable CombinableArbitrary<?> fixedCombinableArbitrary,
		MonkeyContext monkeyContext,
		SeedSnapshot builderScope
	) {
		this.directives = directives;
		this.instantiatorsByType = instantiatorsByType;
		this.fixedState = fixedState;
		this.fixedCombinableArbitrary = fixedCombinableArbitrary;
		this.monkeyContext = monkeyContext;
		this.builderScope = builderScope;
	}

	/**
	 * It is in {@link ArbitraryBuilderContext} due to MonkeyContext is in api module.
	 * It will be removed when all related class migrate to api module.
	 */
	@Deprecated
	public static ArbitraryBuilderContext newBuilderContext(MonkeyContext monkeyContext, SeedSnapshot builderScope) {
		return new ArbitraryBuilderContext(
			new ArrayList<>(),
			new HashMap<>(),
			null, null,
			monkeyContext,
			builderScope
		);
	}

	public ArbitraryBuilderContext copy() {
		List<PathDirective> copiedDirectives = new ArrayList<>(this.directives);

		ArbitraryBuilderContext copiedContext = new ArbitraryBuilderContext(
			copiedDirectives,
			new HashMap<>(instantiatorsByType),
			fixedState,
			fixedCombinableArbitrary,
			monkeyContext,
			nextChildScope()
		);

		copiedContext.setCustomizedValidOnly(customizedValidOnly);
		copiedContext.setOptionValidOnly(optionValidOnly);

		return copiedContext;
	}

	/**
	 * Returns the seed scope of this builder's next sample; the n-th sample of a builder always gets the same scope.
	 *
	 * @return the next sample's seed scope
	 */
	public SeedSnapshot nextSampleScope() {
		return builderScope.scope(SAMPLE).scope(sampleCount.getAndIncrement());
	}

	/**
	 * Returns the seed scope of the next builder derived from this one, such as a copy.
	 *
	 * @return the derived builder's seed scope
	 */
	public SeedSnapshot nextChildScope() {
		return builderScope.scope(CHILD).scope(childCount.getAndIncrement());
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
		SeedSnapshot fixScope = builderScope.scope(FIX).scope(fixCount.getAndIncrement());
		for (int i = 0; i < directives.size(); i++) {
			PathDirective directive = directives.get(i);
			if (directive instanceof SizeDirective) {
				SizeDirective sizeDirective = (SizeDirective)directive;
				SeedSnapshot pathScope = fixScope.scope(sizeDirective.path().hashCode());
				directives.set(i, SeedSnapshot.runIn(pathScope, sizeDirective::fix));
			}
		}
	}

	public void putInstantiator(Class<?> type, InstantiatorProcessResult instantiator) {
		this.instantiatorsByType.put(type, instantiator);
	}

	/**
	 * Returns what this context declared as the root scope, for the builder being sampled.
	 */
	public Scope toRootScope() {
		return Scope.root(new ArrayList<>(directives), getInstantiators());
	}

	/**
	 * Returns what this context declared as a defined scope, for a {@code register(...)} builder.
	 *
	 * @param selector the nodes the scope applies to
	 * @param priority the priority of the scope; a lower number takes precedence
	 * @return the scope
	 */
	public Scope toScope(ScopeSelector selector, int priority) {
		return new Scope(selector, priority, new ArrayList<>(directives), getInstantiators());
	}

	public Map<Class<?>, InstantiatorProcessResult> getInstantiators() {
		if (instantiatorsByType.isEmpty()) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(new HashMap<>(instantiatorsByType));
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
			&& fixedStateLocal.getFixedDirectiveSize() == this.directives.size()) {
			return;
		}

		fixedState = new FixedState(this.directives.size());
		fixedCombinableArbitrary = null;
	}

	public boolean isFixed() {
		return fixedState != null;
	}

	@SuppressWarnings("argument")
	public boolean fixedExpired() {
		return directives.size() > Objects.requireNonNull(fixedState).getFixedDirectiveSize();
	}

	public void renewFixed(CombinableArbitrary<?> fixedCombinableArbitrary) {
		this.markFixed();
		this.fixedCombinableArbitrary = fixedCombinableArbitrary;
	}

	public @Nullable CombinableArbitrary<?> getFixedCombinableArbitrary() {
		return fixedCombinableArbitrary;
	}

	private static class FixedState {
		private final int fixedDirectiveSize;

		public FixedState(int fixedDirectiveSize) {
			this.fixedDirectiveSize = fixedDirectiveSize;
		}

		public int getFixedDirectiveSize() {
			return fixedDirectiveSize;
		}
	}
}
