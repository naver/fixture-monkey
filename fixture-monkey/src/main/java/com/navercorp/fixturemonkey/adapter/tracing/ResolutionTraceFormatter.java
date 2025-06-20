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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.AssemblyEntry;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.BuilderContextEntry;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.CacheStatusEntry;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.ContainerSizeEntry;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.InterfaceResolutionEntry;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.JustPathEntry;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.ManipulatorEntry;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.ManipulatorOverrideEntry;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.ManipulatorRecord;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.MergedCandidateEntry;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.NodeCollision;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.OverriddenSize;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.PropertyDiscoveryEntry;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.RegisteredBuilderEntry;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.SubtreeCacheEntry;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.Timing;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace.UnresolvedPathEntry;
import com.navercorp.objectfarm.api.expression.NameSelector;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.expression.Selector;

/**
 * Formats {@link ResolutionTrace} as human-readable text (tree format) or JSON.
 * <p>
 * This class contains all formatting logic extracted from {@code ResolutionTrace},
 * keeping the data structure class focused on data storage and access.
 */
final class ResolutionTraceFormatter {
	private ResolutionTraceFormatter() {
	}

	/**
	 * Formats the trace as a tree-structured string for console output.
	 *
	 * @param trace the trace to format
	 * @return formatted trace string
	 */
	@SuppressWarnings({"argument", "dereference.of.nullable"})
	static String toTreeFormat(ResolutionTrace trace) {
		StringBuilder sb = new StringBuilder();
		String separator = repeatChar('=', 65);
		sb.append(separator).append("\n");
		sb.append(" Fixture Monkey Resolution Trace\n");
		sb.append(separator).append("\n\n");

		List<ManipulatorEntry> manipulators = trace.getManipulators();
		Map<PathExpression, @Nullable Object> valuesByPath = trace.getValuesByPath();
		Map<PathExpression, Integer> valueOrderByPath = trace.getValueOrderByPath();
		List<NodeCollision> nodeCollisions = trace.getNodeCollisions();
		List<AssemblyEntry> assemblySteps = trace.getAssemblySteps();
		Timing timing = trace.getTiming();
		List<ManipulatorOverrideEntry> manipulatorOverrides = trace.getManipulatorOverrides();
		List<InterfaceResolutionEntry> interfaceResolutions = trace.getInterfaceResolutions();
		List<ContainerSizeEntry> containerSizeResolutions = trace.getContainerSizeResolutions();
		CacheStatusEntry cacheStatus = trace.getCacheStatus();
		List<RegisteredBuilderEntry> registeredBuilders = trace.getRegisteredBuilders();
		List<UnresolvedPathEntry> unresolvedPaths = trace.getUnresolvedPaths();
		List<JustPathEntry> justPaths = trace.getJustPaths();
		BuilderContextEntry builderContext = trace.getBuilderContext();
		List<PropertyDiscoveryEntry> propertyDiscoveries = trace.getPropertyDiscoveries();
		Set<String> decomposedValuePaths = trace.getDecomposedValuePaths();
		List<MergedCandidateEntry> mergedCandidates = trace.getMergedCandidates();
		List<SubtreeCacheEntry> subtreeCacheEvents = trace.getSubtreeCacheEvents();
		String rootType = trace.getRootType();

		// 1. Builder Context section
		if (builderContext != null) {
			sb.append("\u25b8 Builder Context\n");
			sb.append("  isFixed:    ").append(builderContext.isFixed()).append("\n");
			sb.append("  validOnly:  ").append(builderContext.isValidOnly()).append("\n\n");
		}

		// 2. Analysis section
		int analysisPathWidth = maxLength(manipulators, ManipulatorEntry::path) + 2;
		sb.append("\u25b8 Analysis (").append(manipulators.size()).append(" manipulators)\n");
		for (int i = 0; i < manipulators.size(); i++) {
			ManipulatorEntry manipulatorEntry = manipulators.get(i);
			boolean isLast = (i == manipulators.size() - 1);
			String prefix = isLast ? "  \u2514\u2500 " : "  \u251c\u2500 ";
			sb.append(prefix)
				.append(pad(manipulatorEntry.path(), analysisPathWidth))
				.append(" ")
				.append(pad(manipulatorEntry.type(), 16));
			if (manipulatorEntry.sequence() >= 0) {
				sb.append(" [seq=").append(manipulatorEntry.sequence()).append("]");
			}
			if (manipulatorEntry.value() != null) {
				sb.append(" = ").append(summarize(manipulatorEntry.value()));
			}
			if (manipulatorEntry.source() != null) {
				sb.append("     [").append(manipulatorEntry.source()).append("]");
			}
			sb.append("\n");

			// Decompose matching summary
			if (manipulatorEntry.decomposedFieldCount() >= 0) {
				String innerPrefix = isLast ? "      " : "  \u2502   ";
				sb
					.append(innerPrefix)
					.append("decomposed: ")
					.append(manipulatorEntry.decomposedFieldCount())
					.append(" fields extracted, ")
					.append(manipulatorEntry.decomposedMatchedCount())
					.append(" matched");
				if (manipulatorEntry.decomposedSourceType() != null) {
					sb.append("  source type: ").append(manipulatorEntry.decomposedSourceType());
				}
				sb.append("\n");
			}

			if (manipulatorEntry.decomposed() != null && !manipulatorEntry.decomposed().isEmpty()) {
				String innerPrefix = isLast ? "      " : "  \u2502   ";
				List<Map.Entry<String, Object>> decomposedEntries = new ArrayList<>(
					manipulatorEntry.decomposed().entrySet());
				for (int j = 0; j < decomposedEntries.size(); j++) {
					Map.Entry<String, Object> entry = decomposedEntries.get(j);
					boolean isLastDecomposed = (j == decomposedEntries.size() - 1);
					String innerBranch = isLastDecomposed ? "\u2514\u2500 " : "\u251c\u2500 ";
					sb
						.append(innerPrefix)
						.append(innerBranch)
						.append(pad(entry.getKey(), analysisPathWidth))
						.append(" = ")
						.append(summarize(entry.getValue()))
						.append("\n");
				}
			}
		}

		// 3. Values by path section — split user-set vs decomposed
		Map<String, Object> userSetValues = new LinkedHashMap<>();
		Map<String, PathExpression> userSetKeyMap = new LinkedHashMap<>();
		int decomposedCount = 0;
		for (Map.Entry<PathExpression, @Nullable Object> entry : valuesByPath.entrySet()) {
			String pathStr = entry.getKey().toExpression();
			if (decomposedValuePaths.contains(pathStr)) {
				decomposedCount++;
			} else {
				userSetValues.put(pathStr, entry.getValue());
				userSetKeyMap.put(pathStr, entry.getKey());
			}
		}

		int valuesPathWidth = 28;
		if (!userSetValues.isEmpty()) {
			int maxLen = 0;
			for (String key : userSetValues.keySet()) {
				if (key.length() > maxLen) {
					maxLen = key.length();
				}
			}
			valuesPathWidth = maxLen + 2;
		}
		sb.append("\n\u25b8 Values by Path");
		if (!valueOrderByPath.isEmpty()) {
			sb.append(" (with sequence order)");
		}
		sb.append("\n");
		for (Map.Entry<String, Object> entry : userSetValues.entrySet()) {
			sb.append("  ").append(pad(entry.getKey(), valuesPathWidth));
			PathExpression pathExpr = userSetKeyMap.get(entry.getKey());
			Integer order = pathExpr != null ? valueOrderByPath.get(pathExpr) : null;
			if (order != null) {
				sb.append(" [seq=").append(order).append("]");
			}
			sb.append(" = ").append(summarize(entry.getValue())).append("\n");
		}
		if (decomposedCount > 0) {
			sb.append("  (").append(decomposedCount).append(" decomposed values hidden)\n");
		}

		// 4. Node collisions section (if any)
		if (!nodeCollisions.isEmpty()) {
			sb.append("\n\u25b8 Node Collisions (").append(nodeCollisions.size()).append(")\n");
			for (NodeCollision collision : nodeCollisions) {
				sb
					.append("  \u2514\u2500 ")
					.append(collision.path())
					.append(": [seq=")
					.append(collision.previousOrder())
					.append("] ")
					.append(summarize(collision.previousValue()))
					.append(" \u2192 [seq=")
					.append(collision.newOrder())
					.append("] ")
					.append(summarize(collision.newValue()))
					.append("\n");
			}
		}

		// 5. Manipulator Overrides section
		if (!manipulatorOverrides.isEmpty()) {
			sb
				.append("\n\u25b8 Manipulator Overrides (")
				.append(manipulatorOverrides.size())
				.append(" paths with conflicts)\n");
			for (ManipulatorOverrideEntry override : manipulatorOverrides) {
				sb.append("  ").append(override.path()).append("\n");
				ManipulatorRecord winner = override.winner();
				sb
					.append("    \u2514\u2500 WINNER:  ")
					.append(winner.type())
					.append(" [seq=")
					.append(winner.sequence())
					.append("]");
				if (winner.value() != null) {
					sb.append(" = ").append(summarize(winner.value()));
				}
				sb.append("     [").append(winner.source()).append("]\n");
				for (ManipulatorRecord overridden : override.overridden()) {
					sb
						.append("       overrode: ")
						.append(overridden.type())
						.append(" [seq=")
						.append(overridden.sequence())
						.append("]");
					if (overridden.value() != null) {
						sb.append(" = ").append(summarize(overridden.value()));
					}
					sb.append("    [").append(overridden.source()).append("]\n");
				}
			}
			sb.append("\n");
		}

		// 6. Registered Builders section
		if (!registeredBuilders.isEmpty()) {
			sb.append("\u25b8 Registered Builders Applied (").append(registeredBuilders.size()).append(")\n");
			for (RegisteredBuilderEntry builder : registeredBuilders) {
				sb
					.append("  ")
					.append(builder.targetType())
					.append(": ")
					.append(builder.manipulatorCount())
					.append(" manipulators")
					.append(", ")
					.append(builder.containerSizeCount())
					.append(" container size\n");
			}
			sb.append("\n");
		}

		// 7. Merged Candidates section
		if (!mergedCandidates.isEmpty()) {
			int userSetCount = 0;
			int registerCount = 0;
			for (MergedCandidateEntry e : mergedCandidates) {
				if ("DIRECT".equals(e.source())) {
					userSetCount++;
				} else if ("REGISTER".equals(e.source())) {
					registerCount++;
				}
			}

			sb.append("\u25b8 Merged Candidates (").append(mergedCandidates.size()).append(" paths");
			if (userSetCount > 0) {
				sb.append(", ").append(userSetCount).append(" DIRECT");
			}
			if (registerCount > 0) {
				sb.append(", ").append(registerCount).append(" REGISTER");
			}
			sb.append(")\n");

			int mcPathWidth = maxLength(mergedCandidates, MergedCandidateEntry::path) + 2;
			if (mcPathWidth < 6) {
				mcPathWidth = 6;
			}
			sb
				.append("  ")
				.append(pad("PATH", mcPathWidth))
				.append(pad("SOURCE", 14))
				.append(pad("ORDER", 8))
				.append("VALUE\n");

			for (MergedCandidateEntry e : mergedCandidates) {
				sb
					.append("  ")
					.append(pad(e.path(), mcPathWidth))
					.append(pad(e.source(), 14))
					.append(pad(String.valueOf(e.order()), 8))
					.append(summarize(e.value()))
					.append("\n");
			}
			sb.append("\n");
		}

		// 8. Interface Resolutions section
		if (!interfaceResolutions.isEmpty()) {
			int pathWidth = maxLength(interfaceResolutions, InterfaceResolutionEntry::path) + 2;
			sb.append("\u25b8 Interface Resolutions (").append(interfaceResolutions.size()).append(")\n");
			for (InterfaceResolutionEntry resolution : interfaceResolutions) {
				sb
					.append("  ")
					.append(pad(resolution.path(), pathWidth))
					.append(pad(resolution.declaredType(), 20))
					.append(" \u2192 ")
					.append(pad(resolution.resolvedType(), 20))
					.append(" [")
					.append(resolution.reason())
					.append("]\n");
			}
			sb.append("\n");
		}

		// 9. Container Size Resolutions section
		if (!containerSizeResolutions.isEmpty()) {
			int pathWidth = maxLength(containerSizeResolutions, ContainerSizeEntry::path) + 2;
			sb.append("\u25b8 Container Size Resolutions (").append(containerSizeResolutions.size()).append(")\n");
			for (ContainerSizeEntry sizeEntry : containerSizeResolutions) {
				sb
					.append("  ")
					.append(pad(sizeEntry.path(), pathWidth))
					.append(pad(sizeEntry.containerType(), 18))
					.append("size=")
					.append(String.format("%-4d", sizeEntry.finalSize()))
					.append(" [")
					.append(sizeEntry.source());
				if (sizeEntry.metadata() != null) {
					sb.append(" ").append(sizeEntry.metadata());
				}
				sb.append("]\n");
				for (OverriddenSize overridden : sizeEntry.overridden()) {
					sb
						.append(repeatChar(' ', pathWidth + 4))
						.append("\u2514\u2500 overrode: size=")
						.append(overridden.size())
						.append(" [")
						.append(overridden.source())
						.append(", seq=")
						.append(overridden.sequence())
						.append("]\n");
				}
			}
			sb.append("\n");
		}

		// 10. Just Paths section
		if (!justPaths.isEmpty()) {
			sb.append("\u25b8 Just Paths (child values ignored)\n");
			for (JustPathEntry justPath : justPaths) {
				sb.append("  ").append(justPath.path());
				if (!justPath.ignoredChildPaths().isEmpty()) {
					sb.append("       \u2192 ignored: ").append(String.join(", ", justPath.ignoredChildPaths()));
				}
				sb.append("\n");
			}
			sb.append("\n");
		}

		// 11. Unresolved Paths section (strict mode) — grouped by available fields
		if (!unresolvedPaths.isEmpty()) {
			// Group by available fields list
			Map<List<String>, List<UnresolvedPathEntry>> grouped = new LinkedHashMap<>();
			for (UnresolvedPathEntry entry : unresolvedPaths) {
				List<String> key = entry.availableFields() != null ? entry.availableFields() : Collections.emptyList();
				grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
			}

			int totalPaths = unresolvedPaths.size();
			int totalGroups = grouped.size();
			sb
				.append("\u25b8 Unresolved Paths (strict mode) \u2014 ")
				.append(totalPaths)
				.append(" paths in ")
				.append(totalGroups)
				.append(totalGroups == 1 ? " type" : " types")
				.append("\n");

			for (Map.Entry<List<String>, List<UnresolvedPathEntry>> group : grouped.entrySet()) {
				List<String> availableFields = group.getKey();
				List<UnresolvedPathEntry> entries = group.getValue();

				if (!availableFields.isEmpty()) {
					sb.append("  (").append(availableFields.size()).append(" fields available)\n");
					sb.append("    available: ").append(availableFields).append("\n");
				}
				sb.append("    unresolved:\n");
				int pathWidth = maxLength(entries, UnresolvedPathEntry::path) + 2;
				for (UnresolvedPathEntry entry : entries) {
					sb.append("      ").append(pad(entry.path(), pathWidth));
					if (entry.reason() != null) {
						sb.append(entry.reason());
					}
					sb.append("\n");
				}
			}
			sb.append("\n");
		}

		// 12. Assembly section - tree format
		sb.append("\u25b8 Assembly (").append(rootType).append(")\n");
		formatAssemblyTree(sb, assemblySteps);

		// 13. Timing section (if available)
		if (timing != null) {
			sb.append("\n\u25b8 Timing Information\n");
			sb.append("  Analyze:      ").append(formatNanos(timing.getAnalyzeTimeNanos())).append("\n");
			sb.append("  Tree Build:   ").append(formatNanos(timing.getTreeBuildTimeNanos())).append("\n");
			sb.append("  Assembly:     ").append(formatNanos(timing.getAssemblyTimeNanos())).append("\n");
			sb.append("  Total:        ").append(formatNanos(timing.getTotalTimeNanos())).append("\n");
			sb.append("  Node Count:   ").append(timing.getNodeCount()).append("\n");
			sb.append("  Cache Hit:    ").append(timing.isCacheHit()).append("\n");
			sb.append("  Manipulators: ").append(timing.getManipulatorCount()).append("\n");
			sb.append("  Values:       ").append(timing.getValueCount()).append("\n");
			sb.append("  PathMatches:  ").append(timing.getPathMatchCount()).append("\n\n");
		}

		// 14. Cache Status section (extended)
		if (cacheStatus != null) {
			sb.append("\u25b8 Cache Status\n");
			sb.append("  baseResult:     ").append(cacheStatus.baseResult()).append("\n");
			sb.append("  candidateTree:  ").append(cacheStatus.candidateTree()).append("\n");
			sb.append("  nodeContext:    ").append(cacheStatus.nodeContext()).append("\n");
			sb.append("  tree:           ").append(cacheStatus.tree()).append("\n");
			if (cacheStatus.skipReason() != null) {
				sb.append("  skipReason:     ").append(cacheStatus.skipReason()).append("\n");
			}
			sb.append("\n");
		}

		// 15. Property Discovery section
		if (!propertyDiscoveries.isEmpty()) {
			int typeWidth = maxLength(propertyDiscoveries, PropertyDiscoveryEntry::typeName) + 2;
			sb.append("\u25b8 Property Discovery\n");
			for (PropertyDiscoveryEntry discovery : propertyDiscoveries) {
				sb
					.append("  ")
					.append(pad(discovery.typeName(), typeWidth))
					.append("fields=")
					.append(discovery.discoveredFields().size())
					.append("\n");
				if (!discovery.unmatchedTargets().isEmpty()) {
					sb
						.append(repeatChar(' ', typeWidth + 2))
						.append("unmatched targets: ")
						.append(discovery.unmatchedTargets())
						.append("\n");
				}
			}
			sb.append("\n");
		}

		// 16. Subtree Cache section
		if (!subtreeCacheEvents.isEmpty()) {
			int pathWidth = maxLength(subtreeCacheEvents, SubtreeCacheEntry::path) + 2;
			int typeWidth = maxLength(subtreeCacheEvents, SubtreeCacheEntry::typeName) + 2;
			int eventWidth = maxLength(subtreeCacheEvents, SubtreeCacheEntry::event) + 2;
			sb.append("\u25b8 Subtree Cache\n");
			for (SubtreeCacheEntry entry : subtreeCacheEvents) {
				sb
					.append("  ")
					.append(pad(entry.path(), pathWidth))
					.append(pad(entry.typeName(), typeWidth))
					.append(pad(entry.event(), eventWidth));
				if (entry.detail() != null) {
					sb.append(entry.detail());
				}
				sb.append("\n");
			}
			sb.append("\n");
		}

		sb.append(separator);
		return sb.toString();
	}

	/**
	 * Formats the trace as a JSON string for logging/analysis.
	 *
	 * @param trace the trace to format
	 * @return JSON formatted trace string
	 */
	@SuppressWarnings({"argument", "dereference.of.nullable"})
	static String toJsonFormat(ResolutionTrace trace) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");

		List<ManipulatorEntry> manipulators = trace.getManipulators();
		Map<PathExpression, @Nullable Object> valuesByPath = trace.getValuesByPath();
		Map<PathExpression, Integer> valueOrderByPath = trace.getValueOrderByPath();
		List<NodeCollision> nodeCollisions = trace.getNodeCollisions();
		List<AssemblyEntry> assemblySteps = trace.getAssemblySteps();
		List<MergedCandidateEntry> mergedCandidates = trace.getMergedCandidates();
		List<SubtreeCacheEntry> subtreeCacheEvents = trace.getSubtreeCacheEvents();
		String rootType = trace.getRootType();

		// Analysis section
		sb.append("  \"analysis\": {\n");
		sb.append("    \"manipulators\": [\n");
		for (int i = 0; i < manipulators.size(); i++) {
			ManipulatorEntry manipulatorEntry = manipulators.get(i);
			sb
				.append("      {\"path\": \"")
				.append(escapeJson(manipulatorEntry.path()))
				.append("\", \"type\": \"")
				.append(escapeJson(manipulatorEntry.type()))
				.append("\"");
			if (manipulatorEntry.sequence() >= 0) {
				sb.append(", \"sequence\": ").append(manipulatorEntry.sequence());
			}
			if (manipulatorEntry.value() != null) {
				sb.append(", \"value\": ").append(toJsonValue(manipulatorEntry.value()));
			}
			if (manipulatorEntry.decomposed() != null && !manipulatorEntry.decomposed().isEmpty()) {
				sb.append(", \"decomposed\": {");
				List<Map.Entry<String, Object>> entries = new ArrayList<>(manipulatorEntry.decomposed().entrySet());
				for (int j = 0; j < entries.size(); j++) {
					Map.Entry<String, Object> entry = entries.get(j);
					sb
						.append("\"")
						.append(escapeJson(entry.getKey()))
						.append("\": ")
						.append(toJsonValue(entry.getValue()));
					if (j < entries.size() - 1) {
						sb.append(", ");
					}
				}
				sb.append("}");
			}
			if (manipulatorEntry.source() != null) {
				sb.append(", \"source\": \"").append(escapeJson(manipulatorEntry.source())).append("\"");
			}
			sb.append("}");
			if (i < manipulators.size() - 1) {
				sb.append(",");
			}
			sb.append("\n");
		}
		sb.append("    ],\n");

		// Values by path with order
		sb.append("    \"valuesByPath\": {");
		List<Map.Entry<PathExpression, Object>> valueEntries = new ArrayList<>(valuesByPath.entrySet());
		for (int i = 0; i < valueEntries.size(); i++) {
			Map.Entry<PathExpression, Object> entry = valueEntries.get(i);
			sb.append("\"")
				.append(escapeJson(entry.getKey().toExpression()))
				.append("\": ")
				.append(toJsonValue(entry.getValue()));
			if (i < valueEntries.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append("},\n");

		// Value order by path
		if (!valueOrderByPath.isEmpty()) {
			sb.append("    \"valueOrderByPath\": {");
			List<Map.Entry<PathExpression, Integer>> orderEntries = new ArrayList<>(valueOrderByPath.entrySet());
			for (int i = 0; i < orderEntries.size(); i++) {
				Map.Entry<PathExpression, Integer> entry = orderEntries.get(i);
				sb.append("\"")
					.append(escapeJson(entry.getKey().toExpression()))
					.append("\": ")
					.append(entry.getValue());
				if (i < orderEntries.size() - 1) {
					sb.append(", ");
				}
			}
			sb.append("},\n");
		}

		// Node collisions
		if (!nodeCollisions.isEmpty()) {
			sb.append("    \"nodeCollisions\": [\n");
			for (int i = 0; i < nodeCollisions.size(); i++) {
				NodeCollision collision = nodeCollisions.get(i);
				sb
					.append("      {\"path\": \"")
					.append(escapeJson(collision.path()))
					.append("\", \"previousOrder\": ")
					.append(collision.previousOrder())
					.append(", \"previousValue\": ")
					.append(toJsonValue(collision.previousValue()))
					.append(", \"newOrder\": ")
					.append(collision.newOrder())
					.append(", \"newValue\": ")
					.append(toJsonValue(collision.newValue()))
					.append("}");
				if (i < nodeCollisions.size() - 1) {
					sb.append(",");
				}
				sb.append("\n");
			}
			sb.append("    ],\n");
		}

		sb.append("    \"manipulatorCount\": ").append(manipulators.size()).append("\n");
		sb.append("  },\n");

		// Merged Candidates section
		if (!mergedCandidates.isEmpty()) {
			sb.append("  \"mergedCandidates\": [\n");
			for (int i = 0; i < mergedCandidates.size(); i++) {
				MergedCandidateEntry mc = mergedCandidates.get(i);
				sb
					.append("    {\"path\": \"")
					.append(escapeJson(mc.path()))
					.append("\", \"source\": \"")
					.append(escapeJson(mc.source()))
					.append("\", \"order\": ")
					.append(mc.order());
				if (mc.value() != null) {
					sb.append(", \"value\": ").append(toJsonValue(mc.value()));
				}
				sb.append("}");
				if (i < mergedCandidates.size() - 1) {
					sb.append(",");
				}
				sb.append("\n");
			}
			sb.append("  ],\n");
		}

		// Assembly section
		sb.append("  \"assembly\": {\n");
		sb.append("    \"rootType\": \"").append(escapeJson(rootType)).append("\",\n");
		sb.append("    \"nodes\": [\n");
		for (int i = 0; i < assemblySteps.size(); i++) {
			AssemblyEntry assemblyEntry = assemblySteps.get(i);
			sb
				.append("      {\"path\": \"")
				.append(escapeJson(assemblyEntry.path()))
				.append("\", \"source\": \"")
				.append(escapeJson(assemblyEntry.source()))
				.append("\"");
			if (assemblyEntry.value() != null) {
				sb.append(", \"value\": ").append(toJsonValue(assemblyEntry.value()));
			}
			sb.append(", \"nullInject\": ").append(assemblyEntry.nullInject());
			sb.append(", \"isContainer\": ").append(assemblyEntry.isContainer());
			if (assemblyEntry.ownerIsContainer() != null) {
				sb.append(", \"ownerIsContainer\": ").append(assemblyEntry.ownerIsContainer());
			}
			if (assemblyEntry.typeName() != null) {
				sb.append(", \"typeName\": \"").append(escapeJson(assemblyEntry.typeName())).append("\"");
			}
			if (assemblyEntry.creationMethod() != null) {
				sb.append(", \"creationMethod\": \"").append(escapeJson(assemblyEntry.creationMethod())).append("\"");
			}
			if (assemblyEntry.creationDetail() != null) {
				sb.append(", \"creationDetail\": \"").append(escapeJson(assemblyEntry.creationDetail())).append("\"");
			}
			if (assemblyEntry.introspector() != null) {
				sb.append(", \"introspector\": \"").append(escapeJson(assemblyEntry.introspector())).append("\"");
			}
			if (assemblyEntry.declaredType() != null) {
				sb.append(", \"declaredType\": \"").append(escapeJson(assemblyEntry.declaredType())).append("\"");
			}
			if (assemblyEntry.actualType() != null) {
				sb.append(", \"actualType\": \"").append(escapeJson(assemblyEntry.actualType())).append("\"");
			}
			sb.append("}");
			if (i < assemblySteps.size() - 1) {
				sb.append(",");
			}
			sb.append("\n");
		}
		sb.append("    ]\n");
		sb.append("  }");

		if (!subtreeCacheEvents.isEmpty()) {
			sb.append(",\n");
			sb.append("  \"subtreeCache\": [\n");
			for (int i = 0; i < subtreeCacheEvents.size(); i++) {
				SubtreeCacheEntry cacheEntry = subtreeCacheEvents.get(i);
				sb.append("    {\"path\": \"").append(escapeJson(cacheEntry.path())).append("\"");
				sb.append(", \"type\": \"").append(escapeJson(cacheEntry.typeName())).append("\"");
				sb.append(", \"event\": \"").append(escapeJson(cacheEntry.event())).append("\"");
				if (cacheEntry.detail() != null) {
					sb.append(", \"detail\": \"").append(escapeJson(cacheEntry.detail())).append("\"");
				}
				sb.append("}");
				if (i < subtreeCacheEvents.size() - 1) {
					sb.append(",");
				}
				sb.append("\n");
			}
			sb.append("  ]");
		}

		sb.append("\n}");
		return sb.toString();
	}

	// --- Tree rendering helpers ---

	private static void formatAssemblyTree(StringBuilder sb, List<AssemblyEntry> entries) {
		if (entries.isEmpty()) {
			return;
		}

		List<TreeNode> roots = buildTreeFromPaths(entries);

		for (int i = 0; i < roots.size(); i++) {
			boolean isLast = (i == roots.size() - 1);
			renderTreeNode(sb, roots.get(i), "", isLast);
		}
	}

	private static List<TreeNode> buildTreeFromPaths(List<AssemblyEntry> entries) {
		Map<String, TreeNode> nodeMap = new LinkedHashMap<>();

		for (AssemblyEntry entry : entries) {
			String path = entry.path();
			String nodeName = getNodeName(path);
			nodeMap.put(path, new TreeNode(nodeName, entry));
		}

		List<TreeNode> roots = new ArrayList<>();
		for (AssemblyEntry entry : entries) {
			String path = entry.path();
			String parentPath = getParentPath(path);
			TreeNode node = nodeMap.get(path);
			if (node == null) {
				continue;
			}

			if (parentPath == null || parentPath.isEmpty()) {
				roots.add(node);
			} else {
				TreeNode parent = nodeMap.get(parentPath);
				if (parent != null) {
					parent.children.add(node);
				} else {
					roots.add(node);
				}
			}
		}

		return roots;
	}

	private static @Nullable String getParentPath(String path) {
		if (path == null || path.equals(PathExpression.ROOT_EXPRESSION)) {
			return null;
		}

		PathExpression pathExpr = PathExpression.of(path);
		PathExpression parent = pathExpr.getParent();
		if (parent.equals(pathExpr)) {
			return null;
		}
		return parent.toExpression();
	}

	private static String getNodeName(String path) {
		if (path == null || path.equals(PathExpression.ROOT_EXPRESSION)) {
			return PathExpression.ROOT_EXPRESSION;
		}

		PathExpression pathExpr = PathExpression.of(path);
		Segment lastSegment = pathExpr.getLastSegment();
		if (lastSegment == null) {
			return path;
		}

		Selector firstSelector = lastSegment.getFirstSelector();
		if (firstSelector instanceof NameSelector) {
			return ((NameSelector)firstSelector).getName();
		}
		return lastSegment.toExpression();
	}

	private static void renderTreeNode(StringBuilder sb, TreeNode node, String prefix, boolean isLast) {
		String connector = isLast ? "\u2514\u2500 " : "\u251c\u2500 ";
		String childPrefix = isLast ? "   " : "\u2502  ";

		sb.append("  ").append(prefix).append(connector);
		sb.append(node.name);

		AssemblyEntry assemblyEntry = node.entry;
		if (assemblyEntry != null) {
			sb.append(" \u2190 ").append(assemblyEntry.source());
			Object entryValue = assemblyEntry.value();
			if (entryValue != null) {
				sb.append(" = ").append(summarize(entryValue));
			}
			if (assemblyEntry.typeName() != null) {
				sb.append(" (").append(assemblyEntry.typeName()).append(")");
			}
			if (assemblyEntry.nullInject() > 0.0) {
				sb.append(" [null:").append(Math.round(assemblyEntry.nullInject() * 100)).append("%]");
			}
			if (assemblyEntry.introspector() != null) {
				sb.append(" [via ").append(assemblyEntry.introspector()).append("]");
			}
			if (assemblyEntry.creationMethod() != null) {
				sb.append(" (").append(assemblyEntry.creationMethod());
				if (assemblyEntry.creationDetail() != null) {
					sb.append(": ").append(assemblyEntry.creationDetail());
				}
				sb.append(")");
			}
		}
		sb.append("\n");

		for (int i = 0; i < node.children.size(); i++) {
			boolean childIsLast = (i == node.children.size() - 1);
			renderTreeNode(sb, node.children.get(i), prefix + childPrefix, childIsLast);
		}
	}

	// --- Utility methods ---

	static String repeatChar(char character, int count) {
		if (count <= 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(count);
		for (int i = 0; i < count; i++) {
			sb.append(character);
		}
		return sb.toString();
	}

	static String pad(String str, int width) {
		if (str == null) {
			str = "";
		}
		if (str.length() >= width) {
			return str;
		}
		return str + repeatChar(' ', width - str.length());
	}

	static <T> int maxLength(List<T> items, Function<T, String> extractor) {
		int max = 0;
		for (T item : items) {
			String value = extractor.apply(item);
			if (value != null && value.length() > max) {
				max = value.length();
			}
		}
		return max;
	}

	static String summarize(Object value) {
		if (value == null) {
			return "null";
		}
		String str = value.toString();
		if (str.length() > 50) {
			return str.substring(0, 47) + "...";
		}
		return str;
	}

	static String formatNanos(long nanos) {
		double ms = nanos / 1_000_000.0;
		return String.format("%.2f ms", ms);
	}

	static String escapeJson(String str) {
		if (str == null) {
			return "";
		}
		return str
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t");
	}

	static String toJsonValue(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof Number || value instanceof Boolean) {
			return value.toString();
		}
		return "\"" + escapeJson(summarize(value)) + "\"";
	}

	// --- Internal tree structure ---

	private static class TreeNode {

		final String name;
		final AssemblyEntry entry;
		final List<TreeNode> children = new ArrayList<>();

		TreeNode(String name, AssemblyEntry entry) {
			this.name = name;
			this.entry = entry;
		}
	}
}
