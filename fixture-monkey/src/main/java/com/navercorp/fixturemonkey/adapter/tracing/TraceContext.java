package com.navercorp.fixturemonkey.adapter.tracing;

import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Context interface for tracing the resolution process in Fixture Monkey adapter.
 * <p>
 * This interface follows the NoOp pattern - implementations are either active (collecting data)
 * or no-op (doing nothing). This eliminates null checks throughout the codebase.
 * <p>
 * Design inspired by:
 * <ul>
 *   <li>SLF4J Logger - {@link #isEnabled()} guard method pattern</li>
 *   <li>Micrometer Timer - {@link TimingSample} for timing measurements</li>
 * </ul>
 *
 * @see AdapterTracer
 * @see TimingSample
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public interface TraceContext {
	/**
	 * Check if tracing is enabled.
	 * <p>
	 * Use this method to guard expensive trace data computation,
	 * similar to SLF4J's {@code isDebugEnabled()}.
	 *
	 * @return true if tracing is active, false for no-op
	 */
	boolean isEnabled();

	/**
	 * Record an assembly step during value resolution.
	 *
	 * @param path the node path (e.g., "$.name", "$.items[0]")
	 * @param source the value source (e.g., "DIRECT", "REGISTER", "DECOMPOSED", "GENERATED")
	 * @param value the resolved value
	 * @param nullInject the null injection probability
	 * @param isContainer whether the node is a container type
	 * @param ownerIsContainer whether the parent node is a container
	 * @param typeName the type name
	 * @param creationMethod the creation method (e.g., "CONSTRUCTOR", "FACTORY_METHOD")
	 * @param creationDetail additional creation details
	 * @param introspector the introspector name
	 * @param declaredType the declared type
	 * @param actualType the actual runtime type
	 */
	void recordAssemblyStep(
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
	);

	/**
	 * Record a manipulator entry.
	 *
	 * @param path the manipulator path
	 * @param type the manipulator type
	 * @param sequence the manipulation sequence order
	 * @param value the manipulator value
	 * @param decomposed decomposed field values (for object decomposition)
	 * @param source the source of this manipulator (DIRECT, REGISTERED_BUILDER, DECOMPOSED, LAZY_EVALUATED)
	 */
	void recordManipulator(
		String path,
		String type,
		int sequence,
		@Nullable Object value,
		@Nullable Map<String, @Nullable Object> decomposed,
		String source
	);

	/**
	 * Record values by path with their order.
	 *
	 * @param valuesByPath the values mapped by path
	 * @param valueOrderByPath the order of values by path
	 */
	void recordValues(
		Map<PathExpression, @Nullable Object> valuesByPath,
		Map<PathExpression, Integer> valueOrderByPath
	);

	/**
	 * Record a value collision where the same path gets overwritten by a later set() call.
	 *
	 * @param path the path where the collision occurred
	 * @param previousOrder the previous value's sequence order
	 * @param previousValue the previous value that was overwritten
	 * @param newOrder the new value's sequence order
	 * @param newValue the new value that overwrote the previous one
	 */
	void recordNodeCollision(
		String path,
		int previousOrder,
		@Nullable Object previousValue,
		int newOrder,
		@Nullable Object newValue
	);

	/**
	 * Start a timing measurement for a phase.
	 * <p>
	 * Usage pattern (similar to Micrometer Timer.Sample):
	 * <pre>{@code
	 * TimingSample sample = traceContext.startTimer("analyze");
	 * // ... do work ...
	 * sample.stop();
	 * }</pre>
	 *
	 * @param phase the phase name (e.g., "analyze", "treeBuild", "assembly")
	 * @return a timing sample to stop when the phase completes
	 */
	TimingSample startTimer(String phase);

	/**
	 * Record a pre-measured timing value for a phase.
	 *
	 * @param phase the phase name (e.g., "analyze", "treeBuild", "assembly", "total", "prep")
	 * @param nanos the elapsed time in nanoseconds
	 */
	void recordTiming(String phase, long nanos);

	/**
	 * Mark a path as populated via value decomposition (not directly user-set).
	 *
	 * @param path the decomposed path expression string
	 */
	void markDecomposedPath(String path);

	/**
	 * Check if a path was populated via value decomposition.
	 *
	 * @param path the path expression string to check
	 * @return true if the path was marked as decomposed
	 */
	boolean isDecomposedPath(String path);

	/**
	 * Increment the path match counter.
	 */
	void incrementPathMatch();

	/**
	 * Set the root type name for the trace.
	 *
	 * @param rootType the root type name
	 */
	void setRootType(String rootType);

	/**
	 * Set the node count.
	 *
	 * @param count the number of nodes
	 */
	void setNodeCount(int count);

	/**
	 * Set the cache hit status.
	 *
	 * @param cacheHit true if cache was hit
	 */
	void setCacheHit(boolean cacheHit);

	/**
	 * Set the manipulator count.
	 *
	 * @param count the number of manipulators
	 */
	void setManipulatorCount(int count);

	/**
	 * Set the value count.
	 *
	 * @param count the number of values
	 */
	void setValueCount(int count);

	// ========== Extended Tracing Methods ==========

	/**
	 * Record a manipulator that was applied.
	 *
	 * @param path the path where the manipulator was applied
	 * @param type the manipulator type (SET, SET_NULL, SIZE, etc.)
	 * @param sequence the application sequence order
	 * @param value the value being set (may be null)
	 * @param source the source (DIRECT, REGISTERED_BUILDER, DECOMPOSED, LAZY_EVALUATED)
	 */
	void recordManipulatorApplied(String path, String type, int sequence, Object value, String source);

	/**
	 * Record a manipulator that was overridden by another.
	 *
	 * @param path the path where the override occurred
	 * @param type the overridden manipulator type
	 * @param sequence the overridden manipulator sequence
	 * @param value the overridden value
	 * @param source the overridden source
	 * @param overriddenBySequence the sequence of the manipulator that overrode this one
	 */
	void recordManipulatorOverridden(
		String path,
		String type,
		int sequence,
		Object value,
		String source,
		int overriddenBySequence
	);

	/**
	 * Record an interface resolution decision.
	 *
	 * @param path the path where resolution occurred
	 * @param declaredType the declared interface/abstract type
	 * @param resolvedType the actual implementation type chosen
	 * @param reason the reason for this resolution (PATH_BASED, DEFAULT, DIRECT)
	 */
	void recordInterfaceResolution(String path, String declaredType, String resolvedType, String reason);

	/**
	 * Record a container size resolution.
	 *
	 * @param path the container path
	 * @param containerType the container type (List, Set, Map, etc.)
	 * @param size the resolved size
	 * @param source the source of this size (DIRECT, REGISTERED_BUILDER, WILDCARD, DEFAULT)
	 * @param sequence the sequence order of this size setting
	 */
	void recordContainerSize(String path, String containerType, int size, String source, int sequence);

	/**
	 * Record a container size that was overridden.
	 *
	 * @param path the container path
	 * @param oldSize the overridden size
	 * @param oldSource the overridden source
	 * @param oldSequence the overridden sequence
	 */
	void recordContainerSizeOverridden(String path, int oldSize, String oldSource, int oldSequence);

	/**
	 * Record cache status for a specific cache type.
	 *
	 * @param cacheType the cache type (baseResult, tree, candidateTree, nodeContext)
	 * @param result the cache result (HIT, MISS, SKIPPED)
	 * @param reason the reason for SKIPPED or MISS (may be null)
	 */
	void recordCacheStatus(String cacheType, ResolutionTrace.CacheResult result, @Nullable String reason);

	/**
	 * Record that a registered builder was applied.
	 *
	 * @param targetType the type the builder targets
	 * @param manipulatorCount the number of manipulators from this builder
	 * @param containerSizeCount the number of container size settings from this builder
	 */
	void recordRegisteredBuilder(String targetType, int manipulatorCount, int containerSizeCount);

	/**
	 * Add an unresolved path with diagnostic information (for strict mode debugging).
	 *
	 * @param path the path that could not be resolved
	 * @param reason the reason for resolution failure (e.g., FIELD_NOT_FOUND)
	 * @param availableFields the available field names at the failure point
	 */
	void addUnresolvedPath(String path, @Nullable String reason, @Nullable List<String> availableFields);

	/**
	 * Record builder context information.
	 *
	 * @param isFixed whether the builder is fixed (deterministic)
	 * @param validOnly whether validOnly (strict mode) is enabled
	 */
	void recordBuilderContext(boolean isFixed, boolean validOnly);

	/**
	 * Record property discovery information.
	 *
	 * @param typeName the type name
	 * @param discoveredFields the discovered field names
	 * @param unmatchedTargets manipulator targets that were not found in the type structure
	 */
	void recordPropertyDiscovery(String typeName, List<String> discoveredFields, List<String> unmatchedTargets);

	/**
	 * Record a manipulator entry with decompose matching summary.
	 *
	 * @param path the manipulator path
	 * @param type the manipulator type
	 * @param sequence the manipulation sequence order
	 * @param value the manipulator value
	 * @param decomposed decomposed field values
	 * @param source the source of this manipulator
	 * @param decomposedFieldCount the number of decomposed fields (-1 if not decomposed)
	 * @param decomposedMatchedCount the number of matched fields (-1 if not decomposed)
	 * @param decomposedSourceType the source type name (null if not decomposed)
	 */
	void recordManipulator(
		String path,
		String type,
		int sequence,
		@Nullable Object value,
		@Nullable Map<String, @Nullable Object> decomposed,
		String source,
		int decomposedFieldCount,
		int decomposedMatchedCount,
		@Nullable String decomposedSourceType
	);

	/**
	 * Record a container size resolution with metadata.
	 *
	 * @param path the container path
	 * @param containerType the container type
	 * @param size the resolved size
	 * @param source the source of this size
	 * @param sequence the sequence order
	 * @param metadata additional metadata (e.g., "fixed" for fixed mode default)
	 */
	void recordContainerSize(
		String path,
		String containerType,
		int size,
		String source,
		int sequence,
		@Nullable String metadata
	);

	/**
	 * Record a path where Values.just() was applied, causing child paths to be ignored.
	 *
	 * @param path the path where just() was applied
	 * @param ignoredChildPaths the child paths that were ignored
	 */
	void recordJustPath(String path, java.util.List<String> ignoredChildPaths);

	/**
	 * Set the final applied values from AnalysisResult.
	 * This is used to determine which manipulators actually "won" vs were overridden.
	 *
	 * @param finalValuesByPath the final values that were actually applied
	 * @param finalOrderByPath the order of final values
	 */
	void setFinalValues(
		java.util.Map<PathExpression, @Nullable Object> finalValuesByPath,
		java.util.Map<PathExpression, Integer> finalOrderByPath
	);

	/**
	 * Record the merged candidates at assembly time.
	 * This captures the final state after DIRECT and REGISTER values are merged.
	 *
	 * @param candidatesByPath path → value
	 * @param orderByPath path → order
	 * @param sourceByPath path → source ("DIRECT", "REGISTER")
	 */
	void recordMergedCandidates(
		java.util.Map<String, @Nullable Object> candidatesByPath,
		java.util.Map<String, Integer> orderByPath,
		java.util.Map<String, String> sourceByPath
	);

	/**
	 * Record a promoted subtree cache event.
	 *
	 * @param path the path where the event occurred
	 * @param typeName the type name involved
	 * @param event the event type (HIT, MISS, SKIP, STORE)
	 * @param detail additional detail (e.g., "nodes=5", "ancestors incompatible")
	 */
	void recordSubtreeCacheEvent(String path, String typeName, String event, @Nullable String detail);

	/**
	 * Build the final ResolutionTrace.
	 *
	 * @return the built trace, or null for no-op
	 */
	@Nullable ResolutionTrace build();

	/**
	 * Returns a no-op trace context that does nothing.
	 * All methods are safe to call but perform no operations.
	 *
	 * @return a singleton no-op instance
	 */
	static TraceContext noOp() {
		return NoOpTraceContext.INSTANCE;
	}

	/**
	 * Creates an active trace context that collects data.
	 *
	 * @return a new active trace context
	 */
	static TraceContext active() {
		return new ActiveTraceContext();
	}
}
