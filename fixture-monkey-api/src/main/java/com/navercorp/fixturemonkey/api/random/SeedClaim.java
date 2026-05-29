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

package com.navercorp.fixturemonkey.api.random;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * Thread-scoped claim of priority over the global seed managed by {@link Randoms}.
 *
 * <p>While a claim is active on the current thread, {@link Randoms#applyBuilderSeed(long)}
 * becomes a no-op so that lower-priority seed configuration (e.g.
 * {@code FixtureMonkeyBuilder.seed(long)}) cannot override the claim.
 *
 * <p>Claims are re-entrant: nested {@link #establish(long)} calls are tracked and
 * unwound in LIFO order via {@link #close()}. Use try-with-resources or hand the
 * claim to a JUnit {@code ExtensionContext.Store} as a {@code CloseableResource}
 * to guarantee release.
 *
 * <pre>{@code
 * try (SeedClaim ignored = SeedClaim.establish(42L)) {
 *     // FixtureMonkeyBuilder.seed(...) calls inside this block are ignored.
 * }
 * }</pre>
 */
@API(since = "1.1.18", status = Status.INTERNAL)
public final class SeedClaim implements AutoCloseable {
	@SuppressWarnings("type.argument")
	private static final ThreadLocal<SeedClaim> ACTIVE = new ThreadLocal<>();

	@Nullable
	private final SeedClaim previous;
	private boolean closed;

	private SeedClaim(@Nullable SeedClaim previous) {
		this.previous = previous;
	}

	/**
	 * Establishes a new claim on the current thread, applies the given seed
	 * via {@link Randoms#newGlobalSeed(long)}, and returns the claim handle.
	 *
	 * <p>Existing claims (if any) are stacked and restored when this claim
	 * is closed.
	 *
	 * @param seed the seed value to apply
	 * @return a closeable handle that releases the claim when closed
	 */
	public static SeedClaim establish(long seed) {
		SeedClaim claim = new SeedClaim(ACTIVE.get());
		ACTIVE.set(claim);
		Randoms.newGlobalSeed(seed);
		return claim;
	}

	/**
	 * @return {@code true} if any claim is active on the current thread
	 */
	public static boolean isActive() {
		return ACTIVE.get() != null;
	}

	/**
	 * Releases this claim, restoring the previously active claim if any.
	 * Safe to call multiple times. Releasing a claim that is not the topmost
	 * is a no-op for the active slot, but still marks this claim closed so
	 * the previous link is dropped.
	 */
	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		if (ACTIVE.get() == this) {
			if (previous == null) {
				ACTIVE.remove();
			} else {
				ACTIVE.set(previous);
			}
		}
	}
}
