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

package com.navercorp.fixturemonkey.planner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.customizer.Scope;
import com.navercorp.fixturemonkey.customizer.ScopeSelector;
import com.navercorp.fixturemonkey.planner.AnalysisResult.PostConditionFilter;
import com.navercorp.fixturemonkey.planner.AnalysisResult.PropertyCustomizer;
import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * A {@link Scope} after its directives are analyzed: what it declared, by kind and by path relative to a node its
 * selector selects. The selected node is {@code $}, so the root scope's paths are the paths from the node a sample
 * starts from.
 * <p>
 * A scope declares its directives as an ordered list in which a later directive can change an earlier one: a set
 * removes an earlier null, a value replaces what an earlier customizer applies to, a lazy {@code $} drops the values
 * before it. Analyzing settles that order once, so assembly looks a directive up by kind and path instead of
 * replaying the list at every node.
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public final class AnalyzedScope {
	private final Scope scope;
	private final Map<PathExpression, @Nullable Object> values = new LinkedHashMap<>();
	private final Set<PathExpression> notNullPaths = new LinkedHashSet<>();
	private final Map<PathExpression, List<PostConditionFilter>> filters = new LinkedHashMap<>();
	private final Map<PathExpression, List<PropertyCustomizer>> customizers = new LinkedHashMap<>();
	private final Map<PathExpression, ArbitraryContainerInfo> containerSizes = new LinkedHashMap<>();
	private final Map<PathExpression, Integer> limits = new LinkedHashMap<>();

	AnalyzedScope(Scope scope) {
		this.scope = scope;
	}

	public Scope getScope() {
		return scope;
	}

	public ScopeSelector getSelector() {
		return scope.getSelector();
	}

	/**
	 * The priority of the scope; a lower number takes precedence.
	 */
	public int getPriority() {
		return scope.getPriority();
	}

	void putValue(PathExpression path, @Nullable Object value) {
		values.put(path, value);
	}

	void removeNullValue(PathExpression path) {
		if (values.containsKey(path) && values.get(path) == null) {
			values.remove(path);
		}
	}

	void markNotNull(PathExpression path) {
		notNullPaths.add(path);
	}

	void unmarkNotNull(PathExpression path) {
		notNullPaths.remove(path);
	}

	void addFilter(PathExpression path, PostConditionFilter filter) {
		filters.computeIfAbsent(path, key -> new ArrayList<>()).add(filter);
	}

	void addCustomizer(PathExpression path, PropertyCustomizer customizer) {
		customizers.computeIfAbsent(path, key -> new ArrayList<>()).add(customizer);
	}

	void overrideCustomizersAt(PathExpression path) {
		for (List<PropertyCustomizer> declaredCustomizers : customizers.values()) {
			for (PropertyCustomizer customizer : declaredCustomizers) {
				customizer.overriddenAt(path);
			}
		}
	}

	void putLimit(PathExpression path, int limit) {
		limits.put(path, limit);
	}

	void putContainerSize(PathExpression path, ArbitraryContainerInfo containerInfo) {
		containerSizes.put(path, containerInfo);
	}

	void clearSupersededByRootLazy() {
		values.clear();
		notNullPaths.clear();
	}

	public Map<PathExpression, @Nullable Object> getValuesByPath() {
		return Collections.unmodifiableMap(values);
	}

	public Set<PathExpression> getNotNullPaths() {
		return Collections.unmodifiableSet(notNullPaths);
	}

	public Map<PathExpression, List<PostConditionFilter>> getFiltersByPath() {
		return Collections.unmodifiableMap(filters);
	}

	public Map<PathExpression, List<PropertyCustomizer>> getCustomizersByPath() {
		return Collections.unmodifiableMap(customizers);
	}

	/**
	 * How many of the nodes a path reaches inside one selected node a directive applies to, by path.
	 */
	public Map<PathExpression, Integer> getLimitsByPath() {
		return Collections.unmodifiableMap(limits);
	}

	public Map<PathExpression, ArbitraryContainerInfo> getContainerSizesByPath() {
		return Collections.unmodifiableMap(containerSizes);
	}
}
