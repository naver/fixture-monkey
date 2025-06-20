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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.Timing;

/**
 * Interface for tracing the resolution process in Fixture Monkey adapter.
 * <p>
 * Implementations can be used to debug and understand how values are resolved
 * during fixture generation.
 *
 * @see ResolutionTrace
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface AdapterTracer {
	/**
	 * Called when resolution is complete with the collected trace data.
	 *
	 * @param trace the resolution trace containing analysis and assembly information
	 */
	void onResolutionComplete(ResolutionTrace trace);

	/**
	 * Creates a TraceContext for collecting trace data.
	 * <p>
	 * The default implementation returns an active TraceContext that collects data.
	 * The noOp() tracer overrides this to return a NoOp TraceContext for zero overhead.
	 *
	 * @return a TraceContext instance (never null)
	 */
	default TraceContext createTraceContext() {
		return TraceContext.active();
	}

	/**
	 * Returns a no-op tracer that does nothing.
	 * <p>
	 * This tracer returns a NoOp TraceContext for zero overhead during tracing.
	 *
	 * @return a tracer that ignores all trace data
	 */
	static AdapterTracer noOp() {
		return NoOpAdapterTracer.INSTANCE;
	}

	/**
	 * Singleton NoOp implementation that returns NoOp TraceContext.
	 */
	final class NoOpAdapterTracer implements AdapterTracer {

		static final NoOpAdapterTracer INSTANCE = new NoOpAdapterTracer();

		private NoOpAdapterTracer() {
		}

		@Override
		public void onResolutionComplete(ResolutionTrace trace) {
			// No-op
		}

		@Override
		public TraceContext createTraceContext() {
			return TraceContext.noOp();
		}
	}

	/**
	 * Returns a tracer that prints trace data to the console in tree format.
	 *
	 * @return a tracer that outputs to System.out
	 */
	static AdapterTracer console() {
		return trace -> System.out.println(trace.toTreeFormat());
	}

	/**
	 * Returns a tracer that prints trace data to the console in JSON format.
	 *
	 * @return a tracer that outputs JSON to System.out
	 */
	static AdapterTracer consoleJson() {
		return trace -> System.out.println(trace.toJsonFormat());
	}

	/**
	 * Returns a tracer that prints only timing information to the console.
	 * Useful for performance analysis without the full trace output.
	 *
	 * @return a tracer that outputs timing data to System.out
	 */
	static AdapterTracer timing() {
		return trace -> {
			Timing timing = trace.getTiming();
			if (timing != null) {
				System.out.println("=== Adapter Timing ===");
				System.out.printf("  Prep:         %.2f ms%n", timing.getPrepTimeNanos() / 1_000_000.0);
				System.out.printf("  Analyze:      %.2f ms%n", timing.getAnalyzeTimeNanos() / 1_000_000.0);
				System.out.printf("  TreeBuild:    %.2f ms%n", timing.getTreeBuildTimeNanos() / 1_000_000.0);
				System.out.printf("  Assembly:     %.2f ms%n", timing.getAssemblyTimeNanos() / 1_000_000.0);
				System.out.printf("  Total:        %.2f ms%n", timing.getTotalTimeNanos() / 1_000_000.0);
				System.out.println("  Nodes:        " + timing.getNodeCount());
				System.out.println("  CacheHit:     " + timing.isCacheHit());
				System.out.println("  Manipulators: " + timing.getManipulatorCount());
				System.out.println("  Values:       " + timing.getValueCount());
				System.out.println("  PathMatches:  " + timing.getPathMatchCount());
			}
		};
	}

	/**
	 * Returns a tracer that writes tree-formatted trace output to a file.
	 * Each trace is appended to the file, making it suitable for multiple calls.
	 *
	 * @param path the file path to write trace output to
	 * @return a tracer that appends trace data to the specified file
	 */
	static AdapterTracer file(Path path) {
		return trace -> {
			try {
				Files.write(
					path,
					trace.toTreeFormat().getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.CREATE,
					StandardOpenOption.APPEND
				);
			} catch (IOException e) {
				System.err.println("[AdapterTracer] Failed to write to " + path + ": " + e.getMessage());
			}
		};
	}

	/**
	 * Returns a summary tracer that collects all traces and prints a summary table.
	 * Use {@link SummaryTracer#printSummary()} to output the formatted summary.
	 *
	 * @return a new summary tracer instance
	 */
	static SummaryTracer summary() {
		return new SummaryTracer();
	}

	/**
	 * A tracer that collects all traces and can output a summary table.
	 * <p>
	 * Example output:
	 * <pre>
	 * === Fixture Monkey Summary (3 traces) ===
	 * #  | Type                     | Manipulators | Values | Merged | Unresolved | Time
	 * 1  | SimpleObject             |            2 |      2 |      5 |          0 | 0.12ms
	 * 2  | ListStringObject         |            1 |      1 |      3 |          0 | 0.05ms
	 * Totals: 3 traces, 5 manipulators, 0 unresolved paths, 0.34ms total
	 * </pre>
	 */
	@API(status = Status.EXPERIMENTAL)
	class SummaryTracer implements AdapterTracer {

		private final List<ResolutionTrace> traces = new ArrayList<>();

		@Override
		public void onResolutionComplete(ResolutionTrace trace) {
			traces.add(trace);
		}

		/**
		 * Returns all collected traces.
		 */
		public List<ResolutionTrace> getTraces() {
			return Collections.unmodifiableList(traces);
		}

		/**
		 * Prints a summary table of all collected traces.
		 */
		public void printSummary() {
			if (traces.isEmpty()) {
				System.out.println("=== Fixture Monkey Summary (0 traces) ===");
				return;
			}

			// Compute column widths
			int maxTypeLen = 4; // "Type"
			for (ResolutionTrace trace : traces) {
				if (trace.getRootType() != null && trace.getRootType().length() > maxTypeLen) {
					maxTypeLen = trace.getRootType().length();
				}
			}

			int totalManipulators = 0;
			int totalValues = 0;
			int totalMerged = 0;
			int totalUnresolved = 0;
			long totalTimeNanos = 0;

			System.out.printf("=== Fixture Monkey Summary (%d traces) ===%n", traces.size());
			String headerFormat = "%-4s| %-" + maxTypeLen + "s | %12s | %6s | %6s | %10s | %s%n";
			String rowFormat = "%-4d| %-" + maxTypeLen + "s | %12d | %6d | %6d | %10d | %s%n";
			System.out.printf(headerFormat, "#", "Type", "Manipulators", "Values", "Merged", "Unresolved", "Time");

			for (int i = 0; i < traces.size(); i++) {
				ResolutionTrace trace = traces.get(i);
				int manipulators = trace.getManipulators().size();
				int values = trace.getValuesByPath().size();
				int merged = trace.getMergedCandidates().size();
				int unresolved = trace.getUnresolvedPaths().size();

				totalManipulators += manipulators;
				totalValues += values;
				totalMerged += merged;
				totalUnresolved += unresolved;

				String time = "N/A";
				Timing timing = trace.getTiming();
				if (timing != null) {
					totalTimeNanos += timing.getTotalTimeNanos();
					time = String.format("%.2fms", timing.getTotalTimeNanos() / 1_000_000.0);
				}

				System.out.printf(
					rowFormat,
					i + 1,
					trace.getRootType() != null ? trace.getRootType() : "?",
					manipulators,
					values,
					merged,
					unresolved,
					time
				);
			}

			System.out.printf(
				"Totals: %d traces, %d manipulators, %d values, %d merged, %d unresolved paths, %.2fms total%n",
				traces.size(),
				totalManipulators,
				totalValues,
				totalMerged,
				totalUnresolved,
				totalTimeNanos / 1_000_000.0
			);
		}

		/**
		 * Prints a detailed timing breakdown across all collected traces.
		 * Useful for performance analysis over many objects.
		 *
		 * @param wallClockTimeNanos the total wall clock time in nanoseconds
		 */
		public void printTimingBreakdown(long wallClockTimeNanos) {
			int count = traces.size();
			long totalPrep = 0;
			long totalAnalyze = 0;
			long totalTreeBuild = 0;
			long totalAssembly = 0;
			long totalAdapter = 0;
			long firstTreeBuild = 0;
			int totalManipulators = 0;
			int totalValues = 0;
			int totalPathMatches = 0;

			for (ResolutionTrace trace : traces) {
				Timing timing = trace.getTiming();
				if (timing != null) {
					if (firstTreeBuild == 0) {
						firstTreeBuild = timing.getTreeBuildTimeNanos();
					}
					totalPrep += timing.getPrepTimeNanos();
					totalAnalyze += timing.getAnalyzeTimeNanos();
					totalTreeBuild += timing.getTreeBuildTimeNanos();
					totalAssembly += timing.getAssemblyTimeNanos();
					totalAdapter += timing.getTotalTimeNanos();
					totalManipulators += timing.getManipulatorCount();
					totalValues += timing.getValueCount();
					totalPathMatches += timing.getPathMatchCount();
				}
			}

			System.out.println("\n========================================");
			System.out.println("=== TIMING BREAKDOWN (" + count + " objects) ===");
			System.out.println("========================================");
			System.out.printf("Wall Clock Time:     %8.2f ms%n", wallClockTimeNanos / 1_000_000.0);
			System.out.println("----------------------------------------");
			System.out.printf("First TreeBuild:     %8.2f ms (one-time cost)%n", firstTreeBuild / 1_000_000.0);
			System.out.println("----------------------------------------");
			System.out.printf(
				"Total Prep:          %8.2f ms (%5.1f%%)%n",
				totalPrep / 1_000_000.0,
				(100.0 * totalPrep) / wallClockTimeNanos
			);
			System.out.printf(
				"Total Analyze:       %8.2f ms (%5.1f%%)%n",
				totalAnalyze / 1_000_000.0,
				(100.0 * totalAnalyze) / wallClockTimeNanos
			);
			System.out.printf(
				"Total TreeBuild*:    %8.2f ms (%5.1f%%) *cached after first%n",
				totalTreeBuild / 1_000_000.0,
				(100.0 * totalTreeBuild) / wallClockTimeNanos
			);
			System.out.printf(
				"Total Assembly:      %8.2f ms (%5.1f%%)%n",
				totalAssembly / 1_000_000.0,
				(100.0 * totalAssembly) / wallClockTimeNanos
			);
			System.out.printf(
				"Total Adapter Time:  %8.2f ms (%5.1f%%)%n",
				totalAdapter / 1_000_000.0,
				(100.0 * totalAdapter) / wallClockTimeNanos
			);
			long totalMeasured = totalPrep + totalAdapter;
			System.out.println("----------------------------------------");
			System.out.printf(
				"Measured Total:      %8.2f ms (%5.1f%%)%n",
				totalMeasured / 1_000_000.0,
				(100.0 * totalMeasured) / wallClockTimeNanos
			);
			System.out.printf(
				"Unmeasured:          %8.2f ms (%5.1f%%)%n",
				(wallClockTimeNanos - totalMeasured) / 1_000_000.0,
				(100.0 * (wallClockTimeNanos - totalMeasured)) / wallClockTimeNanos
			);
			System.out.println("----------------------------------------");
			System.out.printf("Avg per object:      %8.3f ms%n", wallClockTimeNanos / 1_000_000.0 / count);
			System.out.printf("Avg Prep/obj:        %8.3f ms%n", totalPrep / 1_000_000.0 / count);
			System.out.printf("Avg Assembly/obj:    %8.3f ms%n", totalAssembly / 1_000_000.0 / count);
			System.out.println("----------------------------------------");
			System.out.printf(
				"Total Manipulators:  %8d (%.1f/obj)%n",
				totalManipulators,
				(double)totalManipulators / count
			);
			System.out.printf("Total Values:        %8d (%.1f/obj)%n", totalValues, (double)totalValues / count);
			System.out.printf(
				"Total PathMatches:   %8d (%.1f/obj)%n",
				totalPathMatches,
				(double)totalPathMatches / count
			);
			System.out.println("========================================");
		}

		/**
		 * Clears all collected traces.
		 */
		public void reset() {
			traces.clear();
		}
	}
}
