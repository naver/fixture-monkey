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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.tree.ResolutionListener;

/**
 * Adapter that connects {@link ResolutionListener} from object-farm-api to {@link TraceContext}.
 * <p>
 * This class bridges the resolution events from JvmNodeTreeTransformer to the tracing system
 * in fixture-monkey.
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
public final class TraceContextResolutionListener implements ResolutionListener {
	private final TraceContext traceContext;

	public TraceContextResolutionListener(TraceContext traceContext) {
		this.traceContext = traceContext;
	}

	@Override
	public void onInterfaceResolved(String path, String declaredType, String resolvedType, String reason) {
		traceContext.recordInterfaceResolution(path, declaredType, resolvedType, reason);
	}

	@Override
	public void onContainerSizeResolved(String path, String containerType, int size, String source) {
		traceContext.recordContainerSize(path, containerType, size, source, 0);
	}

	@Override
	public void onContainerSizeResolved(
		String path, String containerType, int size, String source, @Nullable String metadata
	) {
		traceContext.recordContainerSize(path, containerType, size, source, 0, metadata);
	}

	@Override
	public void onSubtreeCacheHit(String path, String typeName, int snapshotNodeCount) {
		traceContext.recordSubtreeCacheEvent(path, typeName, "HIT", "nodes=" + snapshotNodeCount);
	}

	@Override
	public void onSubtreeCacheMiss(String path, String typeName) {
		traceContext.recordSubtreeCacheEvent(path, typeName, "MISS", null);
	}

	@Override
	public void onSubtreeCacheSkip(String path, String typeName, String reason) {
		traceContext.recordSubtreeCacheEvent(path, typeName, "SKIP", reason);
	}

	@Override
	public void onSubtreeCacheStore(String path, String typeName, int nodeCount, boolean selfRecursive) {
		traceContext.recordSubtreeCacheEvent(
			path,
			typeName,
			"STORE",
			"nodes=" + nodeCount + (selfRecursive ? ", selfRecursive" : "")
		);
	}

	/**
	 * Creates a ResolutionListener that delegates to the given TraceContext.
	 * If the TraceContext is not enabled, returns a no-op listener for zero overhead.
	 *
	 * @param traceContext the trace context to delegate to
	 * @return a ResolutionListener
	 */
	public static ResolutionListener of(@Nullable TraceContext traceContext) {
		if (traceContext == null || !traceContext.isEnabled()) {
			return ResolutionListener.noOp();
		}
		return new TraceContextResolutionListener(traceContext);
	}
}
