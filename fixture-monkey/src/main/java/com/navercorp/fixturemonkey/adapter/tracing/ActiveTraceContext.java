package com.navercorp.fixturemonkey.adapter.tracing;

import java.util.ArrayList;
import java.util.EnumMap;
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
 * An active implementation of {@link TraceContext} that collects trace data.
 * <p>
 * This implementation wraps {@link ResolutionTrace.Builder} to collect
 * timing information, manipulator entries, and assembly steps.
 *
 * @see TraceContext
 * @see ResolutionTrace
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
final class ActiveTraceContext implements TraceContext {
	private final ResolutionTrace.Builder builder;
	private int pathMatchCount;

	// Extended tracing data
	private final Map<String, List<ManipulatorAppliedRecord>> manipulatorsByPath = new LinkedHashMap<>();
	private final List<ResolutionTrace.InterfaceResolutionEntry> interfaceResolutions = new ArrayList<>();
	private final Map<String, ContainerSizeRecord> containerSizeByPath = new LinkedHashMap<>();
	private final Map<CacheType, CacheRecord> cacheStatuses = new EnumMap<>(CacheType.class);
	private final List<ResolutionTrace.RegisteredBuilderEntry> registeredBuilders = new ArrayList<>();
	private final List<UnresolvedPathRecord> unresolvedPaths = new ArrayList<>();
	private final List<ResolutionTrace.PropertyDiscoveryEntry> propertyDiscoveries = new ArrayList<>();
	private final List<ResolutionTrace.JustPathEntry> justPaths = new ArrayList<>();

	// Final values from AnalysisResult for winner determination
	private ResolutionTrace.BuilderContextEntry builderContext;
	private Map<PathExpression, @Nullable Object> finalValuesByPath = new LinkedHashMap<>();
	private Map<PathExpression, Integer> finalOrderByPath = new LinkedHashMap<>();
	private final Set<String> decomposedPaths = new HashSet<>();
	private final List<ResolutionTrace.SubtreeCacheEntry> subtreeCacheEvents = new ArrayList<>();

	ActiveTraceContext() {
		this.builder = ResolutionTrace.builder();
		this.pathMatchCount = 0;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void recordAssemblyStep(
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
		builder.addAssemblyStep(
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
		);
	}

	@Override
	public void recordManipulator(
		String path,
		String type,
		int sequence,
		@Nullable Object value,
		@Nullable Map<String, @Nullable Object> decomposed,
		String source
	) {
		builder.addManipulator(path, type, sequence, value, decomposed, source);
		// Also record for override tracking
		manipulatorsByPath
			.computeIfAbsent(path, k -> new ArrayList<>())
			.add(new ManipulatorAppliedRecord(type, sequence, value, source != null ? source : "DIRECT"));
	}

	@Override
	public void recordValues(
		Map<PathExpression, @Nullable Object> valuesByPath,
		Map<PathExpression, Integer> valueOrderByPath
	) {
		builder.putAllValuesWithOrder(valuesByPath, valueOrderByPath);
	}

	@Override
	public void recordNodeCollision(
		String path,
		int previousOrder,
		@Nullable Object previousValue,
		int newOrder,
		@Nullable Object newValue
	) {
		builder.addNodeCollision(path, previousOrder, previousValue, newOrder, newValue);
	}

	@Override
	public TimingSample startTimer(String phase) {
		long startTime = System.nanoTime();
		return () -> {
			long elapsed = System.nanoTime() - startTime;
			recordTiming(phase, elapsed);
			return elapsed;
		};
	}

	@Override
	public void recordTiming(String phase, long nanos) {
		switch (phase) {
			case "prep":
				builder.prepTime(nanos);
				break;
			case "analyze":
				builder.analyzeTime(nanos);
				break;
			case "treeBuild":
				builder.treeBuildTime(nanos);
				break;
			case "assembly":
				builder.assemblyTime(nanos);
				break;
			case "total":
				builder.totalTime(nanos);
				break;
			default:
				// ignore unknown phases
				break;
		}
	}

	@Override
	public void markDecomposedPath(String path) {
		decomposedPaths.add(path);
	}

	@Override
	public boolean isDecomposedPath(String path) {
		return decomposedPaths.contains(path);
	}

	@Override
	public void incrementPathMatch() {
		pathMatchCount++;
	}

	@Override
	public void setRootType(String rootType) {
		builder.rootType(rootType);
	}

	@Override
	public void setNodeCount(int count) {
		builder.nodeCount(count);
	}

	@Override
	public void setCacheHit(boolean cacheHit) {
		builder.cacheHit(cacheHit);
	}

	@Override
	public void setManipulatorCount(int count) {
		builder.manipulatorCount(count);
	}

	@Override
	public void setValueCount(int count) {
		builder.valueCount(count);
	}

	// ========== Extended Tracing Methods ==========

	@Override
	public void recordManipulatorApplied(String path, String type, int sequence, Object value, String source) {
		manipulatorsByPath
			.computeIfAbsent(path, k -> new ArrayList<>())
			.add(new ManipulatorAppliedRecord(type, sequence, value, source));
	}

	@Override
	public void recordManipulatorOverridden(
		String path,
		String type,
		int sequence,
		Object value,
		String source,
		int overriddenBySequence
	) {
		List<ManipulatorAppliedRecord> records = manipulatorsByPath.get(path);
		if (records != null) {
			for (ManipulatorAppliedRecord record : records) {
				if (record.sequence == sequence && record.type.equals(type)) {
					record.overridden = true;
					record.overriddenBySequence = overriddenBySequence;
					break;
				}
			}
		}
	}

	@Override
	public void recordInterfaceResolution(String path, String declaredType, String resolvedType, String reason) {
		interfaceResolutions.add(
			new ResolutionTrace.InterfaceResolutionEntry(path, declaredType, resolvedType, reason)
		);
	}

	@Override
	public void recordContainerSize(String path, String containerType, int size, String source, int sequence) {
		ContainerSizeRecord existing = containerSizeByPath.get(path);
		if (existing != null) {
			// Only record as overridden if the size actually changed
			if (existing.finalSize != size || !existing.finalSource.equals(source)) {
				existing.overridden.add(
					new ResolutionTrace.OverriddenSize(existing.finalSize, existing.finalSource, existing.finalSequence)
				);
				// Update with new values
				existing.finalSize = size;
				existing.finalSource = source;
				existing.finalSequence = sequence;
			}
			// If same size and source, this is a duplicate call - ignore
		} else {
			containerSizeByPath.put(path, new ContainerSizeRecord(containerType, size, source, sequence));
		}
	}

	@Override
	public void recordContainerSizeOverridden(String path, int oldSize, String oldSource, int oldSequence) {
		ContainerSizeRecord record = containerSizeByPath.get(path);
		if (record != null) {
			record.overridden.add(new ResolutionTrace.OverriddenSize(oldSize, oldSource, oldSequence));
		}
	}

	@Override
	public void recordCacheStatus(String cacheType, ResolutionTrace.CacheResult result, @Nullable String reason) {
		CacheType type = parseCacheType(cacheType);
		if (type != null) {
			cacheStatuses.put(type, new CacheRecord(result, reason));
		}
	}

	@Override
	public void recordSubtreeCacheEvent(String path, String typeName, String event, @Nullable String detail) {
		subtreeCacheEvents.add(new ResolutionTrace.SubtreeCacheEntry(path, typeName, event, detail));
	}

	private @Nullable CacheType parseCacheType(String cacheType) {
		switch (cacheType) {
			case "baseResult":
				return CacheType.BASE_RESULT;
			case "tree":
				return CacheType.TREE;
			case "candidateTree":
				return CacheType.CANDIDATE_TREE;
			case "nodeContext":
				return CacheType.NODE_CONTEXT;
			default:
				return null;
		}
	}

	@Override
	public void recordRegisteredBuilder(String targetType, int manipulatorCount, int containerSizeCount) {
		registeredBuilders.add(
			new ResolutionTrace.RegisteredBuilderEntry(targetType, manipulatorCount, containerSizeCount)
		);
	}

	@Override
	public void addUnresolvedPath(String path, @Nullable String reason, @Nullable List<String> availableFields) {
		unresolvedPaths.add(new UnresolvedPathRecord(path, reason, availableFields));
	}

	@Override
	public void recordBuilderContext(boolean isFixed, boolean validOnly) {
		this.builderContext = new ResolutionTrace.BuilderContextEntry(isFixed, validOnly);
	}

	@Override
	public void recordPropertyDiscovery(String typeName, List<String> discoveredFields, List<String> unmatchedTargets) {
		propertyDiscoveries.add(
			new ResolutionTrace.PropertyDiscoveryEntry(typeName, discoveredFields, unmatchedTargets)
		);
	}

	@Override
	public void recordManipulator(
		String path,
		String type,
		int sequence,
		@Nullable Object value,
		@Nullable Map<String, @Nullable Object> decomposed,
		String source,
		int decomposedFieldCount,
		int decomposedMatchedCount,
		@Nullable String decomposedSourceType
	) {
		builder.addManipulator(
			path,
			type,
			sequence,
			value,
			decomposed,
			source,
			decomposedFieldCount,
			decomposedMatchedCount,
			decomposedSourceType
		);
		manipulatorsByPath
			.computeIfAbsent(path, k -> new ArrayList<>())
			.add(new ManipulatorAppliedRecord(type, sequence, value, source != null ? source : "DIRECT"));
	}

	@Override
	public void recordContainerSize(
		String path,
		String containerType,
		int size,
		String source,
		int sequence,
		@Nullable String metadata
	) {
		ContainerSizeRecord existing = containerSizeByPath.get(path);
		if (existing != null) {
			if (existing.finalSize != size || !existing.finalSource.equals(source)) {
				existing.overridden.add(
					new ResolutionTrace.OverriddenSize(existing.finalSize, existing.finalSource, existing.finalSequence)
				);
				existing.finalSize = size;
				existing.finalSource = source;
				existing.finalSequence = sequence;
			}
			existing.metadata = metadata;
		} else {
			ContainerSizeRecord record = new ContainerSizeRecord(containerType, size, source, sequence);
			record.metadata = metadata;
			containerSizeByPath.put(path, record);
		}
	}

	@Override
	public void recordJustPath(String path, List<String> ignoredChildPaths) {
		justPaths.add(new ResolutionTrace.JustPathEntry(path, ignoredChildPaths));
	}

	@Override
	public void setFinalValues(
		Map<PathExpression, @Nullable Object> finalValuesByPath,
		Map<PathExpression, Integer> finalOrderByPath
	) {
		this.finalValuesByPath = finalValuesByPath != null ? finalValuesByPath : new LinkedHashMap<>();
		this.finalOrderByPath = finalOrderByPath != null ? finalOrderByPath : new LinkedHashMap<>();
	}

	@Override
	public void recordMergedCandidates(
		Map<String, @Nullable Object> candidatesByPath,
		Map<String, Integer> orderByPath,
		Map<String, String> sourceByPath
	) {
		for (Map.Entry<String, @Nullable Object> entry : candidatesByPath.entrySet()) {
			String path = entry.getKey();
			builder.addMergedCandidate(
				path,
				sourceByPath.getOrDefault(path, "UNKNOWN"),
				orderByPath.getOrDefault(path, 0),
				entry.getValue()
			);
		}
	}

	@Override
	public ResolutionTrace build() {
		builder.pathMatchCount(pathMatchCount);

		// Build manipulator overrides using final values from AnalysisResult
		for (Map.Entry<String, List<ManipulatorAppliedRecord>> entry : manipulatorsByPath.entrySet()) {
			String path = entry.getKey();
			List<ManipulatorAppliedRecord> records = entry.getValue();

			if (records.size() > 1) {
				// Determine winner based on finalOrderByPath (the actual applied order from AnalysisResult)
				Integer winnerOrder = finalOrderByPath.get(PathExpression.of(path));
				ManipulatorAppliedRecord winner = null;
				List<ResolutionTrace.ManipulatorRecord> overridden = new ArrayList<>();

				if (winnerOrder != null) {
					// Find the winner by matching sequence with final order
					for (ManipulatorAppliedRecord record : records) {
						if (record.sequence == winnerOrder) {
							winner = record;
						} else {
							overridden.add(
								new ResolutionTrace.ManipulatorRecord(
									record.type,
									record.sequence,
									record.value,
									record.source
								)
							);
						}
					}
				}

				// Only report override if we have a confirmed winner from AnalysisResult
				if (winner != null && !overridden.isEmpty()) {
					builder.addManipulatorOverride(
						new ResolutionTrace.ManipulatorOverrideEntry(
							path,
							new ResolutionTrace.ManipulatorRecord(
								winner.type,
								winner.sequence,
								winner.value,
								winner.source
							),
							overridden
						)
					);
				} else if (winnerOrder == null && records.size() > 1) {
					// No final order info - record all as "unresolved conflict"
					// This means the path had multiple manipulators but we couldn't determine the winner
					// (e.g., path was overridden by a more specific path)
					List<ResolutionTrace.ManipulatorRecord> allRecords = new ArrayList<>();
					for (ManipulatorAppliedRecord record : records) {
						allRecords.add(
							new ResolutionTrace.ManipulatorRecord(
								record.type,
								record.sequence,
								record.value,
								record.source
							)
						);
					}
					// Use first record as placeholder, mark source as UNRESOLVED to indicate ambiguity
					ManipulatorAppliedRecord first = records.get(0);
					builder.addManipulatorOverride(
						new ResolutionTrace.ManipulatorOverrideEntry(
							path,
							new ResolutionTrace.ManipulatorRecord(
								first.type,
								first.sequence,
								first.value,
								"UNRESOLVED"
							),
							allRecords.subList(1, allRecords.size())
						)
					);
				}
			}
		}

		// Add interface resolutions
		for (ResolutionTrace.InterfaceResolutionEntry resolution : interfaceResolutions) {
			builder.addInterfaceResolution(resolution);
		}

		// Build container size entries from collected records
		for (Map.Entry<String, ContainerSizeRecord> entry : containerSizeByPath.entrySet()) {
			ContainerSizeRecord record = entry.getValue();
			builder.addContainerSizeResolution(
				new ResolutionTrace.ContainerSizeEntry(
					entry.getKey(),
					record.containerType,
					record.finalSize,
					record.finalSource,
					record.overridden,
					record.metadata
				)
			);
		}

		// Build cache status if any records exist
		if (!cacheStatuses.isEmpty()) {
			builder.cacheStatus(
				new ResolutionTrace.CacheStatusEntry(
					cacheStatuses.containsKey(CacheType.BASE_RESULT)
						? cacheStatuses.get(CacheType.BASE_RESULT).result
						: ResolutionTrace.CacheResult.MISS,
					cacheStatuses.containsKey(CacheType.TREE)
						? cacheStatuses.get(CacheType.TREE).result
						: ResolutionTrace.CacheResult.MISS,
					cacheStatuses.containsKey(CacheType.CANDIDATE_TREE)
						? cacheStatuses.get(CacheType.CANDIDATE_TREE).result
						: ResolutionTrace.CacheResult.MISS,
					cacheStatuses.containsKey(CacheType.NODE_CONTEXT)
						? cacheStatuses.get(CacheType.NODE_CONTEXT).result
						: ResolutionTrace.CacheResult.MISS,
					cacheStatuses
						.values()
						.stream()
						.filter(r -> r.reason != null)
						.map(r -> r.reason)
						.findFirst()
						.orElse(null)
				)
			);
		}

		// Add registered builders
		for (ResolutionTrace.RegisteredBuilderEntry entry : registeredBuilders) {
			builder.addRegisteredBuilder(entry);
		}

		// Add unresolved paths
		for (UnresolvedPathRecord record : unresolvedPaths) {
			builder.addUnresolvedPath(
				new ResolutionTrace.UnresolvedPathEntry(record.path, record.reason, record.availableFields)
			);
		}

		// Add just paths
		for (ResolutionTrace.JustPathEntry entry : justPaths) {
			builder.addJustPath(entry);
		}

		// Add builder context
		if (builderContext != null) {
			builder.builderContext(builderContext);
		}

		// Add property discoveries
		for (ResolutionTrace.PropertyDiscoveryEntry entry : propertyDiscoveries) {
			builder.addPropertyDiscovery(entry);
		}

		// Add decomposed paths
		for (String path : decomposedPaths) {
			builder.addDecomposedPath(path);
		}

		// Add subtree cache events
		for (ResolutionTrace.SubtreeCacheEntry entry : subtreeCacheEvents) {
			builder.addSubtreeCacheEntry(entry);
		}

		return builder.build();
	}

	private enum CacheType {
		BASE_RESULT,
		TREE,
		CANDIDATE_TREE,
		NODE_CONTEXT,
	}

	private static final class ManipulatorAppliedRecord {
		final String type;
		final int sequence;
		final @Nullable Object value;
		final String source;
		boolean overridden;
		int overriddenBySequence;

		ManipulatorAppliedRecord(String type, int sequence, @Nullable Object value, String source) {
			this.type = type;
			this.sequence = sequence;
			this.value = value;
			this.source = source;
			this.overridden = false;
			this.overriddenBySequence = -1;
		}
	}

	private static final class ContainerSizeRecord {
		final String containerType;
		int finalSize;
		String finalSource;
		int finalSequence;
		@Nullable String metadata;
		final List<ResolutionTrace.OverriddenSize> overridden = new ArrayList<>();

		ContainerSizeRecord(String containerType, int size, String source, int sequence) {
			this.containerType = containerType;
			this.finalSize = size;
			this.finalSource = source;
			this.finalSequence = sequence;
		}
	}

	private static final class CacheRecord {
		ResolutionTrace.CacheResult result;
		@Nullable String reason;

		CacheRecord(ResolutionTrace.CacheResult result, @Nullable String reason) {
			this.result = result;
			this.reason = reason;
		}
	}

	private static final class UnresolvedPathRecord {
		final String path;
		final @Nullable String reason;
		final @Nullable List<String> availableFields;

		UnresolvedPathRecord(String path, @Nullable String reason, @Nullable List<String> availableFields) {
			this.path = path;
			this.reason = reason;
			this.availableFields = availableFields;
		}
	}
}
