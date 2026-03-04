package com.navercorp.objectfarm.api.tree;

import org.jspecify.annotations.Nullable;

/**
 * A no-op implementation of {@link ResolutionListener} that does nothing.
 * <p>
 * This implementation is used when tracing is disabled, providing zero overhead.
 */
final class NoOpResolutionListener implements ResolutionListener {
	static final NoOpResolutionListener INSTANCE = new NoOpResolutionListener();

	private NoOpResolutionListener() {
	}

	@Override
	public void onInterfaceResolved(String path, String declaredType, String resolvedType, String reason) {
		// no-op
	}

	@Override
	public void onContainerSizeResolved(String path, String containerType, int size, String source) {
		// no-op
	}

	@Override
	public void onContainerSizeResolved(
		String path, String containerType, int size, String source, @Nullable String metadata
	) {
		// no-op
	}
}
