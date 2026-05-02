package com.navercorp.fixturemonkey.adapter.tracing;

import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * A no-op implementation of {@link TraceContext} that does nothing.
 * <p>
 * All methods are safe to call but perform no operations.
 * This eliminates null checks throughout the codebase.
 *
 * @see TraceContext
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
final class NoOpTraceContext implements TraceContext {
	static final NoOpTraceContext INSTANCE = new NoOpTraceContext();

	private NoOpTraceContext() {
	}

	@Override
	public boolean isEnabled() {
		return false;
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
		// no-op
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
		// no-op
	}

	@Override
	public void recordValues(
		Map<PathExpression, @Nullable Object> valuesByPath,
		Map<PathExpression, Integer> valueOrderByPath
	) {
		// no-op
	}

	@Override
	public void recordNodeCollision(
		String path,
		int previousOrder,
		@Nullable Object previousValue,
		int newOrder,
		@Nullable Object newValue
	) {
		// no-op
	}

	@Override
	public TimingSample startTimer(String phase) {
		return TimingSample.noOp();
	}

	@Override
	public void recordTiming(String phase, long nanos) {
		// no-op
	}

	@Override
	public void markDecomposedPath(String path) {
		// no-op
	}

	@Override
	public boolean isDecomposedPath(String path) {
		return false;
	}

	@Override
	public void incrementPathMatch() {
		// no-op
	}

	@Override
	public void setRootType(String rootType) {
		// no-op
	}

	@Override
	public void setNodeCount(int count) {
		// no-op
	}

	@Override
	public void setCacheHit(boolean cacheHit) {
		// no-op
	}

	@Override
	public void setManipulatorCount(int count) {
		// no-op
	}

	@Override
	public void setValueCount(int count) {
		// no-op
	}

	// ========== Extended Tracing Methods ==========

	@Override
	public void recordManipulatorApplied(String path, String type, int sequence, Object value, String source) {
		// no-op
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
		// no-op
	}

	@Override
	public void recordInterfaceResolution(String path, String declaredType, String resolvedType, String reason) {
		// no-op
	}

	@Override
	public void recordContainerSize(String path, String containerType, int size, String source, int sequence) {
		// no-op
	}

	@Override
	public void recordContainerSizeOverridden(String path, int oldSize, String oldSource, int oldSequence) {
		// no-op
	}

	@Override
	public void recordCacheStatus(String cacheType, ResolutionTrace.CacheResult result, @Nullable String reason) {
		// no-op
	}

	@Override
	public void recordRegisteredBuilder(String targetType, int manipulatorCount, int containerSizeCount) {
		// no-op
	}

	@Override
	public void addUnresolvedPath(String path, @Nullable String reason, @Nullable List<String> availableFields) {
		// no-op
	}

	@Override
	public void recordBuilderContext(boolean isFixed, boolean validOnly) {
		// no-op
	}

	@Override
	public void recordPropertyDiscovery(String typeName, List<String> discoveredFields, List<String> unmatchedTargets) {
		// no-op
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
		// no-op
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
		// no-op
	}

	@Override
	public void recordJustPath(String path, List<String> ignoredChildPaths) {
		// no-op
	}

	@Override
	public void setFinalValues(Map<PathExpression, @Nullable Object> finalValuesByPath,
		Map<PathExpression, Integer> finalOrderByPath) {
		// no-op
	}

	@Override
	public void recordMergedCandidates(
		java.util.Map<String, @Nullable Object> candidatesByPath,
		java.util.Map<String, Integer> orderByPath,
		java.util.Map<String, String> sourceByPath
	) {
		// no-op
	}

	@Override
	public void recordSubtreeCacheEvent(String path, String typeName, String event, @Nullable String detail) {
		// no-op
	}

	@Override
	public @Nullable ResolutionTrace build() {
		return null;
	}
}
