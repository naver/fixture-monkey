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

package com.navercorp.fixturemonkey.adapter.tracing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Collects and formats debugging information about the resolution process.
 * <p>
 * This class captures:
 * <ul>
 *   <li>Manipulator analysis - what manipulators were applied and their types</li>
 *   <li>Values by path - the resolved values mapped to paths</li>
 *   <li>Assembly steps - how values were assembled into the final object</li>
 * </ul>
 *
 * @see AdapterTracer
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
public final class ResolutionTrace {
	private final String rootType;
	private final List<ManipulatorEntry> manipulators;
	private final Map<PathExpression, @Nullable Object> valuesByPath;
	private final Map<PathExpression, Integer> valueOrderByPath;
	private final List<NodeCollision> nodeCollisions;
	private final List<AssemblyEntry> assemblySteps;
	private final ResolutionTrace.@Nullable Timing timing;

	// Extended tracing fields
	private final List<ManipulatorOverrideEntry> manipulatorOverrides;
	private final List<InterfaceResolutionEntry> interfaceResolutions;
	private final List<ContainerSizeEntry> containerSizeResolutions;

	private final @Nullable CacheStatusEntry cacheStatus;

	private final List<RegisteredBuilderEntry> registeredBuilders;
	private final List<UnresolvedPathEntry> unresolvedPaths;
	private final List<JustPathEntry> justPaths;

	private final @Nullable BuilderContextEntry builderContext;

	private final List<PropertyDiscoveryEntry> propertyDiscoveries;
	private final Set<String> decomposedValuePaths;
	private final List<MergedCandidateEntry> mergedCandidates;
	private final List<SubtreeCacheEntry> subtreeCacheEvents;

	private ResolutionTrace(
		String rootType,
		List<ManipulatorEntry> manipulators,
		Map<PathExpression, @Nullable Object> valuesByPath,
		Map<PathExpression, Integer> valueOrderByPath,
		List<NodeCollision> nodeCollisions,
		List<AssemblyEntry> assemblySteps,
		ResolutionTrace.@Nullable Timing timing,
		List<ManipulatorOverrideEntry> manipulatorOverrides,
		List<InterfaceResolutionEntry> interfaceResolutions,
		List<ContainerSizeEntry> containerSizeResolutions,
		@Nullable CacheStatusEntry cacheStatus,
		List<RegisteredBuilderEntry> registeredBuilders,
		List<UnresolvedPathEntry> unresolvedPaths,
		List<JustPathEntry> justPaths,
		@Nullable BuilderContextEntry builderContext,
		List<PropertyDiscoveryEntry> propertyDiscoveries,
		Set<String> decomposedValuePaths,
		List<MergedCandidateEntry> mergedCandidates,
		List<SubtreeCacheEntry> subtreeCacheEvents
	) {
		this.rootType = rootType;
		this.manipulators = Collections.unmodifiableList(new ArrayList<>(manipulators));
		this.valuesByPath = Collections.unmodifiableMap(new LinkedHashMap<>(valuesByPath));
		this.valueOrderByPath = Collections.unmodifiableMap(new LinkedHashMap<>(valueOrderByPath));
		this.nodeCollisions = Collections.unmodifiableList(new ArrayList<>(nodeCollisions));
		this.assemblySteps = Collections.unmodifiableList(new ArrayList<>(assemblySteps));
		this.timing = timing;
		this.manipulatorOverrides = Collections.unmodifiableList(new ArrayList<>(manipulatorOverrides));
		this.interfaceResolutions = Collections.unmodifiableList(new ArrayList<>(interfaceResolutions));
		this.containerSizeResolutions = Collections.unmodifiableList(new ArrayList<>(containerSizeResolutions));
		this.cacheStatus = cacheStatus;
		this.registeredBuilders = Collections.unmodifiableList(new ArrayList<>(registeredBuilders));
		this.unresolvedPaths = Collections.unmodifiableList(new ArrayList<>(unresolvedPaths));
		this.justPaths = Collections.unmodifiableList(new ArrayList<>(justPaths));
		this.builderContext = builderContext;
		this.propertyDiscoveries = Collections.unmodifiableList(new ArrayList<>(propertyDiscoveries));
		this.decomposedValuePaths = Collections.unmodifiableSet(new HashSet<>(decomposedValuePaths));
		this.mergedCandidates = Collections.unmodifiableList(new ArrayList<>(mergedCandidates));
		this.subtreeCacheEvents = Collections.unmodifiableList(new ArrayList<>(subtreeCacheEvents));
	}

	public String getRootType() {
		return rootType;
	}

	public List<ManipulatorEntry> getManipulators() {
		return manipulators;
	}

	public Map<PathExpression, @Nullable Object> getValuesByPath() {
		return valuesByPath;
	}

	public Set<String> getDecomposedValuePaths() {
		return decomposedValuePaths;
	}

	public Map<PathExpression, Integer> getValueOrderByPath() {
		return valueOrderByPath;
	}

	public List<NodeCollision> getNodeCollisions() {
		return nodeCollisions;
	}

	public List<AssemblyEntry> getAssemblySteps() {
		return assemblySteps;
	}

	public ResolutionTrace.@Nullable Timing getTiming() {
		return timing;
	}

	// Extended tracing getters

	public List<ManipulatorOverrideEntry> getManipulatorOverrides() {
		return manipulatorOverrides;
	}

	public List<InterfaceResolutionEntry> getInterfaceResolutions() {
		return interfaceResolutions;
	}

	public List<ContainerSizeEntry> getContainerSizeResolutions() {
		return containerSizeResolutions;
	}

	public @Nullable CacheStatusEntry getCacheStatus() {
		return cacheStatus;
	}

	public List<RegisteredBuilderEntry> getRegisteredBuilders() {
		return registeredBuilders;
	}

	public List<UnresolvedPathEntry> getUnresolvedPaths() {
		return unresolvedPaths;
	}

	public List<JustPathEntry> getJustPaths() {
		return justPaths;
	}

	public @Nullable BuilderContextEntry getBuilderContext() {
		return builderContext;
	}

	public List<PropertyDiscoveryEntry> getPropertyDiscoveries() {
		return propertyDiscoveries;
	}

	public List<MergedCandidateEntry> getMergedCandidates() {
		return mergedCandidates;
	}

	public List<SubtreeCacheEntry> getSubtreeCacheEvents() {
		return subtreeCacheEvents;
	}

	/**
	 * Formats the trace as a tree-structured string for console output.
	 *
	 * @return formatted trace string
	 */
	public String toTreeFormat() {
		return ResolutionTraceFormatter.toTreeFormat(this);
	}

	/**
	 * Formats the trace as a JSON string for logging/analysis.
	 *
	 * @return JSON formatted trace string
	 */
	public String toJsonFormat() {
		return ResolutionTraceFormatter.toJsonFormat(this);
	}

	/**
	 * Creates a new builder for ResolutionTrace.
	 *
	 * @return a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static final class ManipulatorEntry {

		private final String path;
		private final String type;
		private final int sequence;

		private final @Nullable Object value;

		private final @Nullable Map<String, @Nullable Object> decomposed;

		private final @Nullable String source;

		// Decompose matching summary fields
		private final int decomposedFieldCount;
		private final int decomposedMatchedCount;

		private final @Nullable String decomposedSourceType;

		public ManipulatorEntry(String path, String type) {
			this(path, type, -1, null, null, null);
		}

		public ManipulatorEntry(String path, String type, @Nullable Object value) {
			this(path, type, -1, value, null, null);
		}

		public ManipulatorEntry(
			String path,
			String type,
			@Nullable Object value,
			@Nullable Map<String, @Nullable Object> decomposed
		) {
			this(path, type, -1, value, decomposed, null);
		}

		public ManipulatorEntry(
			String path,
			String type,
			int sequence,
			@Nullable Object value,
			@Nullable Map<String, @Nullable Object> decomposed
		) {
			this(path, type, sequence, value, decomposed, null);
		}

		public ManipulatorEntry(
			String path,
			String type,
			int sequence,
			@Nullable Object value,
			@Nullable Map<String, @Nullable Object> decomposed,
			@Nullable String source
		) {
			this(path, type, sequence, value, decomposed, source, -1, -1, null);
		}

		public ManipulatorEntry(
			String path,
			String type,
			int sequence,
			@Nullable Object value,
			@Nullable Map<String, @Nullable Object> decomposed,
			@Nullable String source,
			int decomposedFieldCount,
			int decomposedMatchedCount,
			@Nullable String decomposedSourceType
		) {
			this.path = path;
			this.type = type;
			this.sequence = sequence;
			this.value = value;
			this.decomposed = decomposed;
			this.source = source;
			this.decomposedFieldCount = decomposedFieldCount;
			this.decomposedMatchedCount = decomposedMatchedCount;
			this.decomposedSourceType = decomposedSourceType;
		}

		public String path() {
			return path;
		}

		public String type() {
			return type;
		}

		public int sequence() {
			return sequence;
		}

		public @Nullable Object value() {
			return value;
		}

		public @Nullable Map<String, @Nullable Object> decomposed() {
			return decomposed;
		}

		public @Nullable String source() {
			return source;
		}

		public int decomposedFieldCount() {
			return decomposedFieldCount;
		}

		public int decomposedMatchedCount() {
			return decomposedMatchedCount;
		}

		public @Nullable String decomposedSourceType() {
			return decomposedSourceType;
		}
	}

	/**
	 * Entry representing an assembly step during value resolution.
	 */
	public static final class AssemblyEntry {

		private final String path;
		private final String source;

		private final @Nullable Object value;

		private final double nullInject;
		private final boolean isContainer;

		private final @Nullable Boolean ownerIsContainer;

		private final @Nullable String typeName;

		private final @Nullable String creationMethod;

		private final @Nullable String creationDetail;

		private final @Nullable String introspector;

		private final @Nullable String declaredType;

		private final @Nullable String actualType;

		public AssemblyEntry(String path, String source, @Nullable Object value) {
			this(path, source, value, 0.0, false, null, null, null, null, null, null, null);
		}

		public AssemblyEntry(
			String path,
			String source,
			@Nullable Object value,
			double nullInject,
			boolean isContainer,
			@Nullable Boolean ownerIsContainer,
			@Nullable String typeName
		) {
			this(
				path,
				source,
				value,
				nullInject,
				isContainer,
				ownerIsContainer,
				typeName,
				null,
				null,
				null,
				null,
				null
			);
		}

		public AssemblyEntry(
			String path,
			String source,
			@Nullable Object value,
			double nullInject,
			boolean isContainer,
			@Nullable Boolean ownerIsContainer,
			@Nullable String typeName,
			@Nullable String creationMethod,
			@Nullable String creationDetail,
			@Nullable String introspector,
			@Nullable String declaredType,
			@Nullable String actualType
		) {
			this.path = path;
			this.source = source;
			this.value = value;
			this.nullInject = nullInject;
			this.isContainer = isContainer;
			this.ownerIsContainer = ownerIsContainer;
			this.typeName = typeName;
			this.creationMethod = creationMethod;
			this.creationDetail = creationDetail;
			this.introspector = introspector;
			this.declaredType = declaredType;
			this.actualType = actualType;
		}

		public String path() {
			return path;
		}

		public String source() {
			return source;
		}

		public @Nullable Object value() {
			return value;
		}

		public double nullInject() {
			return nullInject;
		}

		public boolean isContainer() {
			return isContainer;
		}

		public @Nullable Boolean ownerIsContainer() {
			return ownerIsContainer;
		}

		public @Nullable String typeName() {
			return typeName;
		}

		public @Nullable String creationMethod() {
			return creationMethod;
		}

		public @Nullable String creationDetail() {
			return creationDetail;
		}

		public @Nullable String introspector() {
			return introspector;
		}

		public @Nullable String declaredType() {
			return declaredType;
		}

		public @Nullable String actualType() {
			return actualType;
		}
	}

	/**
	 * Represents a value collision where the same path gets overwritten by a later set() call.
	 * Records both the previous and new values with their sequence orders.
	 */
	public static final class NodeCollision {

		private final String path;
		private final int previousOrder;

		private final @Nullable Object previousValue;

		private final int newOrder;

		private final @Nullable Object newValue;

		public NodeCollision(
			String path,
			int previousOrder,
			@Nullable Object previousValue,
			int newOrder,
			@Nullable Object newValue
		) {
			this.path = path;
			this.previousOrder = previousOrder;
			this.previousValue = previousValue;
			this.newOrder = newOrder;
			this.newValue = newValue;
		}

		public String path() {
			return path;
		}

		public int previousOrder() {
			return previousOrder;
		}

		public @Nullable Object previousValue() {
			return previousValue;
		}

		public int newOrder() {
			return newOrder;
		}

		public @Nullable Object newValue() {
			return newValue;
		}
	}

	/**
	 * Contains timing information for the adapter resolution process.
	 */
	public static final class Timing {

		private final long prepTimeNanos;
		private final long analyzeTimeNanos;
		private final long treeBuildTimeNanos;
		private final long assemblyTimeNanos;
		private final long totalTimeNanos;
		private final int nodeCount;
		private final boolean cacheHit;
		private final int manipulatorCount;
		private final int valueCount;
		private final int pathMatchCount;

		public Timing(
			long prepTimeNanos,
			long analyzeTimeNanos,
			long treeBuildTimeNanos,
			long assemblyTimeNanos,
			long totalTimeNanos,
			int nodeCount,
			boolean cacheHit,
			int manipulatorCount,
			int valueCount,
			int pathMatchCount
		) {
			this.prepTimeNanos = prepTimeNanos;
			this.analyzeTimeNanos = analyzeTimeNanos;
			this.treeBuildTimeNanos = treeBuildTimeNanos;
			this.assemblyTimeNanos = assemblyTimeNanos;
			this.totalTimeNanos = totalTimeNanos;
			this.nodeCount = nodeCount;
			this.cacheHit = cacheHit;
			this.manipulatorCount = manipulatorCount;
			this.valueCount = valueCount;
			this.pathMatchCount = pathMatchCount;
		}

		public long getPrepTimeNanos() {
			return prepTimeNanos;
		}

		public long getAnalyzeTimeNanos() {
			return analyzeTimeNanos;
		}

		public long getTreeBuildTimeNanos() {
			return treeBuildTimeNanos;
		}

		public long getAssemblyTimeNanos() {
			return assemblyTimeNanos;
		}

		public long getTotalTimeNanos() {
			return totalTimeNanos;
		}

		public int getNodeCount() {
			return nodeCount;
		}

		public boolean isCacheHit() {
			return cacheHit;
		}

		public int getManipulatorCount() {
			return manipulatorCount;
		}

		public int getValueCount() {
			return valueCount;
		}

		public int getPathMatchCount() {
			return pathMatchCount;
		}
	}

	/**
	 * Builder for constructing ResolutionTrace instances.
	 */
	public static final class Builder {

		private String rootType = "Unknown";
		private final List<ManipulatorEntry> manipulators = new ArrayList<>();
		private final Map<PathExpression, @Nullable Object> valuesByPath = new LinkedHashMap<>();
		private final Map<PathExpression, Integer> valueOrderByPath = new LinkedHashMap<>();
		private final List<NodeCollision> nodeCollisions = new ArrayList<>();
		private final List<AssemblyEntry> assemblySteps = new ArrayList<>();

		private long prepTimeNanos;
		private long analyzeTimeNanos;
		private long treeBuildTimeNanos;
		private long assemblyTimeNanos;
		private long totalTimeNanos;
		private int nodeCount;
		private boolean cacheHit;
		private int manipulatorCount;
		private int valueCount;
		private int pathMatchCount;
		private boolean hasTiming;

		// Extended tracing fields
		private final List<ManipulatorOverrideEntry> manipulatorOverrides = new ArrayList<>();
		private final List<InterfaceResolutionEntry> interfaceResolutions = new ArrayList<>();
		private final List<ContainerSizeEntry> containerSizeResolutions = new ArrayList<>();
		private CacheStatusEntry cacheStatus;
		private final List<RegisteredBuilderEntry> registeredBuilders = new ArrayList<>();
		private final List<UnresolvedPathEntry> unresolvedPaths = new ArrayList<>();
		private final List<JustPathEntry> justPaths = new ArrayList<>();
		private BuilderContextEntry builderContext;
		private final List<PropertyDiscoveryEntry> propertyDiscoveries = new ArrayList<>();
		private final Set<String> decomposedValuePaths = new HashSet<>();
		private final List<MergedCandidateEntry> mergedCandidates = new ArrayList<>();
		private final List<SubtreeCacheEntry> subtreeCacheEvents = new ArrayList<>();

		private Builder() {
		}

		public Builder rootType(String rootType) {
			this.rootType = rootType;
			return this;
		}

		public Builder addManipulator(ManipulatorEntry entry) {
			this.manipulators.add(entry);
			return this;
		}

		public Builder addManipulator(String path, String type) {
			return addManipulator(new ManipulatorEntry(path, type));
		}

		public Builder addManipulator(String path, String type, @Nullable Object value) {
			return addManipulator(new ManipulatorEntry(path, type, value));
		}

		public Builder addManipulator(
			String path,
			String type,
			@Nullable Object value,
			@Nullable Map<String, @Nullable Object> decomposed
		) {
			return addManipulator(new ManipulatorEntry(path, type, value, decomposed));
		}

		public Builder addManipulator(
			String path,
			String type,
			int sequence,
			@Nullable Object value,
			@Nullable Map<String, @Nullable Object> decomposed
		) {
			return addManipulator(new ManipulatorEntry(path, type, sequence, value, decomposed));
		}

		public Builder addManipulator(
			String path,
			String type,
			int sequence,
			@Nullable Object value,
			@Nullable Map<String, @Nullable Object> decomposed,
			@Nullable String source
		) {
			return addManipulator(new ManipulatorEntry(path, type, sequence, value, decomposed, source));
		}

		public Builder addManipulator(
			String path,
			String type,
			int sequence,
			@Nullable Object value,
			@Nullable Map<String, @Nullable Object> decomposed,
			@Nullable String source,
			int decomposedFieldCount,
			int decomposedMatchedCount,
			@Nullable String decomposedSourceType
		) {
			return addManipulator(
				new ManipulatorEntry(
					path,
					type,
					sequence,
					value,
					decomposed,
					source,
					decomposedFieldCount,
					decomposedMatchedCount,
					decomposedSourceType
				)
			);
		}

		public Builder putValue(PathExpression path, Object value) {
			this.valuesByPath.put(path, value);
			return this;
		}

		public Builder putAllValues(Map<PathExpression, Object> values) {
			this.valuesByPath.putAll(values);
			return this;
		}

		public Builder putValueWithOrder(PathExpression path, Object value, int order) {
			this.valuesByPath.put(path, value);
			this.valueOrderByPath.put(path, order);
			return this;
		}

		public Builder putAllValuesWithOrder(
			Map<PathExpression, @Nullable Object> values,
			Map<PathExpression, Integer> orders
		) {
			this.valuesByPath.putAll(values);
			this.valueOrderByPath.putAll(orders);
			return this;
		}

		public Builder addNodeCollision(NodeCollision collision) {
			this.nodeCollisions.add(collision);
			return this;
		}

		public Builder addNodeCollision(
			String path,
			int previousOrder,
			@Nullable Object previousValue,
			int newOrder,
			@Nullable Object newValue
		) {
			return addNodeCollision(new NodeCollision(path, previousOrder, previousValue, newOrder, newValue));
		}

		public Builder addAssemblyStep(AssemblyEntry entry) {
			this.assemblySteps.add(entry);
			return this;
		}

		public Builder addAssemblyStep(String path, String source, @Nullable Object value) {
			return addAssemblyStep(new AssemblyEntry(path, source, value));
		}

		public Builder addAssemblyStep(
			String path,
			String source,
			@Nullable Object value,
			double nullInject,
			boolean isContainer,
			@Nullable Boolean ownerIsContainer,
			@Nullable String typeName
		) {
			return addAssemblyStep(
				new AssemblyEntry(path, source, value, nullInject, isContainer, ownerIsContainer, typeName)
			);
		}

		public Builder addAssemblyStep(
			String path,
			String source,
			@Nullable Object value,
			double nullInject,
			boolean isContainer,
			@Nullable Boolean ownerIsContainer,
			@Nullable String typeName,
			@Nullable String creationMethod,
			@Nullable String creationDetail,
			@Nullable String introspector,
			@Nullable String declaredType,
			@Nullable String actualType
		) {
			return addAssemblyStep(
				new AssemblyEntry(
					path,
					source,
					value,
					nullInject,
					isContainer,
					ownerIsContainer,
					typeName,
					creationMethod,
					creationDetail,
					introspector,
					declaredType,
					actualType
				)
			);
		}

		public Builder prepTime(long nanos) {
			this.prepTimeNanos = nanos;
			this.hasTiming = true;
			return this;
		}

		public Builder analyzeTime(long nanos) {
			this.analyzeTimeNanos = nanos;
			this.hasTiming = true;
			return this;
		}

		public Builder treeBuildTime(long nanos) {
			this.treeBuildTimeNanos = nanos;
			this.hasTiming = true;
			return this;
		}

		public Builder assemblyTime(long nanos) {
			this.assemblyTimeNanos = nanos;
			this.hasTiming = true;
			return this;
		}

		public Builder totalTime(long nanos) {
			this.totalTimeNanos = nanos;
			this.hasTiming = true;
			return this;
		}

		public Builder nodeCount(int count) {
			this.nodeCount = count;
			this.hasTiming = true;
			return this;
		}

		public Builder cacheHit(boolean hit) {
			this.cacheHit = hit;
			this.hasTiming = true;
			return this;
		}

		public Builder manipulatorCount(int count) {
			this.manipulatorCount = count;
			this.hasTiming = true;
			return this;
		}

		public Builder valueCount(int count) {
			this.valueCount = count;
			this.hasTiming = true;
			return this;
		}

		public Builder pathMatchCount(int count) {
			this.pathMatchCount = count;
			this.hasTiming = true;
			return this;
		}

		public Builder incrementPathMatchCount() {
			this.pathMatchCount++;
			return this;
		}

		// Extended tracing builder methods

		public Builder addManipulatorOverride(ManipulatorOverrideEntry entry) {
			this.manipulatorOverrides.add(entry);
			return this;
		}

		public Builder addInterfaceResolution(InterfaceResolutionEntry entry) {
			this.interfaceResolutions.add(entry);
			return this;
		}

		public Builder addContainerSizeResolution(ContainerSizeEntry entry) {
			this.containerSizeResolutions.add(entry);
			return this;
		}

		public Builder cacheStatus(CacheStatusEntry status) {
			this.cacheStatus = status;
			return this;
		}

		public Builder addRegisteredBuilder(RegisteredBuilderEntry entry) {
			this.registeredBuilders.add(entry);
			return this;
		}

		public Builder addUnresolvedPath(UnresolvedPathEntry entry) {
			this.unresolvedPaths.add(entry);
			return this;
		}

		public Builder addJustPath(JustPathEntry entry) {
			this.justPaths.add(entry);
			return this;
		}

		public Builder builderContext(BuilderContextEntry entry) {
			this.builderContext = entry;
			return this;
		}

		public Builder addPropertyDiscovery(PropertyDiscoveryEntry entry) {
			this.propertyDiscoveries.add(entry);
			return this;
		}

		Builder addDecomposedPath(String path) {
			this.decomposedValuePaths.add(path);
			return this;
		}

		public Builder addMergedCandidate(String path, String source, int order, @Nullable Object value) {
			this.mergedCandidates.add(new MergedCandidateEntry(path, source, order, value));
			return this;
		}

		Builder addSubtreeCacheEntry(SubtreeCacheEntry entry) {
			this.subtreeCacheEvents.add(entry);
			return this;
		}

		public ResolutionTrace build() {
			Timing timing = hasTiming
				?
				new Timing(
					prepTimeNanos,
					analyzeTimeNanos,
					treeBuildTimeNanos,
					assemblyTimeNanos,
					totalTimeNanos,
					nodeCount,
					cacheHit,
					manipulatorCount,
					valueCount,
					pathMatchCount
				)
				: null;
			return new ResolutionTrace(
				rootType,
				manipulators,
				valuesByPath,
				valueOrderByPath,
				nodeCollisions,
				assemblySteps,
				timing,
				manipulatorOverrides,
				interfaceResolutions,
				containerSizeResolutions,
				cacheStatus,
				registeredBuilders,
				unresolvedPaths,
				justPaths,
				builderContext,
				propertyDiscoveries,
				decomposedValuePaths,
				mergedCandidates,
				subtreeCacheEvents
			);
		}
	}

	// ========== New Entry Types for Extended Tracing ==========

	/**
	 * Records a manipulator that was applied or overridden.
	 */
	public static final class ManipulatorRecord {

		private final String type;
		private final int sequence;

		private final @Nullable Object value;

		private final String source;

		public ManipulatorRecord(String type, int sequence, @Nullable Object value, String source) {
			this.type = type;
			this.sequence = sequence;
			this.value = value;
			this.source = source;
		}

		public String type() {
			return type;
		}

		public int sequence() {
			return sequence;
		}

		public @Nullable Object value() {
			return value;
		}

		public String source() {
			return source;
		}
	}

	/**
	 * Entry tracking manipulator override history for a specific path.
	 */
	public static final class ManipulatorOverrideEntry {

		private final String path;
		private final ManipulatorRecord winner;
		private final List<ManipulatorRecord> overridden;

		public ManipulatorOverrideEntry(String path, ManipulatorRecord winner, List<ManipulatorRecord> overridden) {
			this.path = path;
			this.winner = winner;
			this.overridden = overridden;
		}

		public String path() {
			return path;
		}

		public ManipulatorRecord winner() {
			return winner;
		}

		public List<ManipulatorRecord> overridden() {
			return overridden;
		}
	}

	/**
	 * Entry tracking interface resolution decisions.
	 */
	public static final class InterfaceResolutionEntry {

		private final String path;
		private final String declaredType;
		private final String resolvedType;
		private final String reason;

		public InterfaceResolutionEntry(String path, String declaredType, String resolvedType, String reason) {
			this.path = path;
			this.declaredType = declaredType;
			this.resolvedType = resolvedType;
			this.reason = reason;
		}

		public String path() {
			return path;
		}

		public String declaredType() {
			return declaredType;
		}

		public String resolvedType() {
			return resolvedType;
		}

		public String reason() {
			return reason;
		}
	}

	/**
	 * Records an overridden container size setting.
	 */
	public static final class OverriddenSize {

		private final int size;
		private final String source;
		private final int sequence;

		public OverriddenSize(int size, String source, int sequence) {
			this.size = size;
			this.source = source;
			this.sequence = sequence;
		}

		public int size() {
			return size;
		}

		public String source() {
			return source;
		}

		public int sequence() {
			return sequence;
		}
	}

	public static final class ContainerSizeEntry {

		private final String path;
		private final String containerType;
		private final int finalSize;
		private final String source;
		private final List<OverriddenSize> overridden;

		private final @Nullable String metadata;

		public ContainerSizeEntry(
			String path,
			String containerType,
			int finalSize,
			String source,
			List<OverriddenSize> overridden
		) {
			this(path, containerType, finalSize, source, overridden, null);
		}

		public ContainerSizeEntry(
			String path,
			String containerType,
			int finalSize,
			String source,
			List<OverriddenSize> overridden,
			@Nullable String metadata
		) {
			this.path = path;
			this.containerType = containerType;
			this.finalSize = finalSize;
			this.source = source;
			this.overridden = overridden;
			this.metadata = metadata;
		}

		public ContainerSizeEntry(String path, String containerType, int finalSize, String source) {
			this(path, containerType, finalSize, source, Collections.emptyList(), null);
		}

		public String path() {
			return path;
		}

		public String containerType() {
			return containerType;
		}

		public int finalSize() {
			return finalSize;
		}

		public String source() {
			return source;
		}

		public List<OverriddenSize> overridden() {
			return overridden;
		}

		public @Nullable String metadata() {
			return metadata;
		}
	}

	/**
	 * Enum representing cache lookup results.
	 */
	public enum CacheResult {
		HIT,
		MISS,
		SKIPPED,
	}

	/**
	 * Entry tracking cache status for different cache types.
	 */
	public static final class CacheStatusEntry {

		private final CacheResult baseResult;
		private final CacheResult tree;
		private final CacheResult candidateTree;
		private final CacheResult nodeContext;

		private final @Nullable String skipReason;

		public CacheStatusEntry(
			CacheResult baseResult,
			CacheResult tree,
			CacheResult candidateTree,
			CacheResult nodeContext,
			@Nullable String skipReason
		) {
			this.baseResult = baseResult;
			this.tree = tree;
			this.candidateTree = candidateTree;
			this.nodeContext = nodeContext;
			this.skipReason = skipReason;
		}

		public CacheResult baseResult() {
			return baseResult;
		}

		public CacheResult tree() {
			return tree;
		}

		public CacheResult candidateTree() {
			return candidateTree;
		}

		public CacheResult nodeContext() {
			return nodeContext;
		}

		public @Nullable String skipReason() {
			return skipReason;
		}
	}

	/**
	 * Entry tracking registered builder application.
	 */
	public static final class RegisteredBuilderEntry {

		private final String targetType;
		private final int manipulatorCount;
		private final int containerSizeCount;

		public RegisteredBuilderEntry(String targetType, int manipulatorCount, int containerSizeCount) {
			this.targetType = targetType;
			this.manipulatorCount = manipulatorCount;
			this.containerSizeCount = containerSizeCount;
		}

		public String targetType() {
			return targetType;
		}

		public int manipulatorCount() {
			return manipulatorCount;
		}

		public int containerSizeCount() {
			return containerSizeCount;
		}
	}

	/**
	 * Entry tracking paths where Values.just() was applied.
	 */
	public static final class JustPathEntry {

		private final String path;
		private final List<String> ignoredChildPaths;

		public JustPathEntry(String path, List<String> ignoredChildPaths) {
			this.path = path;
			this.ignoredChildPaths = ignoredChildPaths;
		}

		public String path() {
			return path;
		}

		public List<String> ignoredChildPaths() {
			return ignoredChildPaths;
		}
	}

	/**
	 * Entry tracking builder context information.
	 */
	public static final class BuilderContextEntry {
		private final boolean isFixed;
		private final boolean validOnly;

		public BuilderContextEntry(boolean isFixed, boolean validOnly) {
			this.isFixed = isFixed;
			this.validOnly = validOnly;
		}

		public boolean isFixed() {
			return isFixed;
		}

		public boolean isValidOnly() {
			return validOnly;
		}
	}

	/**
	 * Entry tracking property discovery information.
	 */
	public static final class PropertyDiscoveryEntry {

		private final String typeName;
		private final List<String> discoveredFields;
		private final List<String> unmatchedTargets;

		public PropertyDiscoveryEntry(String typeName, List<String> discoveredFields, List<String> unmatchedTargets) {
			this.typeName = typeName;
			this.discoveredFields = Collections.unmodifiableList(new ArrayList<>(discoveredFields));
			this.unmatchedTargets = Collections.unmodifiableList(new ArrayList<>(unmatchedTargets));
		}

		public String typeName() {
			return typeName;
		}

		public List<String> discoveredFields() {
			return discoveredFields;
		}

		public List<String> unmatchedTargets() {
			return unmatchedTargets;
		}
	}

	/**
	 * Entry tracking an unresolved path with diagnostic information.
	 */
	public static final class UnresolvedPathEntry {

		private final String path;

		private final @Nullable String reason;

		private final @Nullable List<String> availableFields;

		public UnresolvedPathEntry(String path, @Nullable String reason, @Nullable List<String> availableFields) {
			this.path = path;
			this.reason = reason;
			this.availableFields =
				availableFields != null ? Collections.unmodifiableList(new ArrayList<>(availableFields)) : null;
		}

		public String path() {
			return path;
		}

		public @Nullable String reason() {
			return reason;
		}

		public @Nullable List<String> availableFields() {
			return availableFields;
		}
	}

	/**
	 * Entry representing a merged candidate value at assembly time.
	 * Shows the final state after DIRECT and REGISTER values are merged.
	 */
	public static final class MergedCandidateEntry {

		private final String path;
		private final String source;
		private final int order;

		private final @Nullable Object value;

		public MergedCandidateEntry(String path, String source, int order, @Nullable Object value) {
			this.path = path;
			this.source = source;
			this.order = order;
			this.value = value;
		}

		public String path() {
			return path;
		}

		public String source() {
			return source;
		}

		public int order() {
			return order;
		}

		public @Nullable Object value() {
			return value;
		}
	}

	/**
	 * Entry tracking a promoted subtree cache event (HIT, MISS, SKIP, STORE).
	 */
	public static final class SubtreeCacheEntry {

		private final String path;
		private final String typeName;
		private final String event;

		private final @Nullable String detail;

		public SubtreeCacheEntry(String path, String typeName, String event, @Nullable String detail) {
			this.path = path;
			this.typeName = typeName;
			this.event = event;
			this.detail = detail;
		}

		public String path() {
			return path;
		}

		public String typeName() {
			return typeName;
		}

		public String event() {
			return event;
		}

		public @Nullable String detail() {
			return detail;
		}
	}
}
