package com.navercorp.objectfarm.api.tree;

import javax.annotation.Nullable;

/**
 * Listener interface for tracking resolution decisions during tree transformation.
 * <p>
 * This interface follows the NoOp pattern - implementations are either active (collecting data)
 * or no-op (doing nothing). The no-op implementation has zero overhead.
 * <p>
 * Used by {@link JvmNodeTreeTransformer} to report:
 * <ul>
 *   <li>Interface resolution: when an interface/abstract type is resolved to a concrete implementation</li>
 *   <li>Container size resolution: when a container's size is determined</li>
 * </ul>
 *
 * @see JvmNodeTreeTransformer
 */
public interface ResolutionListener {
	/**
	 * Called when an interface or abstract type is resolved to a concrete implementation.
	 *
	 * @param path the path where resolution occurred (e.g., "$.payment", "$.items[0]")
	 * @param declaredType the declared interface/abstract type name
	 * @param resolvedType the resolved concrete implementation type name
	 * @param reason the reason for this resolution (PATH_BASED, DEFAULT, etc.)
	 */
	void onInterfaceResolved(String path, String declaredType, String resolvedType, String reason);

	/**
	 * Called when a container's size is determined.
	 *
	 * @param path the container path (e.g., "$.items", "$.tags")
	 * @param containerType the container type name (e.g., "List", "Set", "Map")
	 * @param size the resolved size
	 * @param source the source of this size (EXACT_PATH, TYPE_BASED, WILDCARD, DEFAULT)
	 */
	void onContainerSizeResolved(String path, String containerType, int size, String source);

	/**
	 * Called when a container's size is determined, with additional metadata.
	 *
	 * @param path the container path
	 * @param containerType the container type name
	 * @param size the resolved size
	 * @param source the source of this size
	 * @param metadata additional metadata (e.g., "fixed" for fixed mode)
	 */
	default void onContainerSizeResolved(
		String path,
		String containerType,
		int size,
		String source,
		@Nullable String metadata
	) {
		onContainerSizeResolved(path, containerType, size, source);
	}

	/**
	 * Called when a promoted subtree cache hit occurs.
	 *
	 * @param path the path where the cache lookup occurred
	 * @param typeName the type name of the cached subtree
	 * @param snapshotNodeCount the number of nodes in the cached snapshot
	 */
	default void onSubtreeCacheHit(String path, String typeName, int snapshotNodeCount) {
	}

	/**
	 * Called when a promoted subtree cache miss occurs (no snapshot exists for this type).
	 *
	 * @param path the path where the cache lookup occurred
	 * @param typeName the type name that was looked up
	 */
	default void onSubtreeCacheMiss(String path, String typeName) {
	}

	/**
	 * Called when a promoted subtree cache lookup is skipped due to incompatibility.
	 *
	 * @param path the path where the skip occurred
	 * @param typeName the type name of the snapshot
	 * @param reason the reason for skipping (e.g., "ancestors incompatible", "self-recursive with expansion context")
	 */
	default void onSubtreeCacheSkip(String path, String typeName, String reason) {
	}

	/**
	 * Called when a new snapshot is stored in the promoted subtree cache.
	 *
	 * @param path the path where the snapshot was collected
	 * @param typeName the type name being cached
	 * @param nodeCount the number of nodes in the snapshot
	 * @param selfRecursive whether the subtree is self-recursive
	 */
	default void onSubtreeCacheStore(String path, String typeName, int nodeCount, boolean selfRecursive) {
	}

	/**
	 * Returns a no-op listener that does nothing.
	 * All methods are safe to call but perform no operations.
	 *
	 * @return a singleton no-op instance
	 */
	static ResolutionListener noOp() {
		return NoOpResolutionListener.INSTANCE;
	}
}
