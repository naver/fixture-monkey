package com.navercorp.fixturemonkey.adapter.tracing;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A timing sample for measuring elapsed time of a phase.
 * <p>
 * This interface follows the Micrometer Timer.Sample pattern for timing measurements.
 * <p>
 * Usage:
 * <pre>{@code
 * TimingSample sample = traceContext.startTimer("analyze");
 * // ... do work ...
 * long elapsedNanos = sample.stop();
 * }</pre>
 *
 * @see TraceContext#startTimer(String)
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface TimingSample {
	/**
	 * Stop the timing and record the elapsed time.
	 *
	 * @return the elapsed time in nanoseconds
	 */
	long stop();

	/**
	 * Returns a no-op timing sample that does nothing.
	 *
	 * @return a no-op timing sample that returns 0
	 */
	static TimingSample noOp() {
		return () -> 0L;
	}
}
