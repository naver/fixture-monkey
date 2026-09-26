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

package com.navercorp.fixturemonkey.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.customizer.JustDirective;
import com.navercorp.fixturemonkey.customizer.PathDirective;
import com.navercorp.fixturemonkey.customizer.SetDirective;
import com.navercorp.fixturemonkey.customizer.SizeDirective;
import com.navercorp.fixturemonkey.planner.AnalysisResult;
import com.navercorp.fixturemonkey.planner.AnalysisResult.PostConditionFilter;
import com.navercorp.fixturemonkey.planner.AnalyzedScope;
import com.navercorp.fixturemonkey.planner.AssemblyPlan;
import com.navercorp.fixturemonkey.planner.LazyValueHolder;
import com.navercorp.fixturemonkey.planner.ValueProjection;
import com.navercorp.fixturemonkey.tracing.AssemblyTracer;
import com.navercorp.fixturemonkey.tracing.ResolutionTrace;
import com.navercorp.fixturemonkey.tracing.TraceContext;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;

/**
 * Builds the resolution trace from collected data and invokes the tracer.
 */
@API(since = "1.1.20", status = Status.EXPERIMENTAL)
public final class AssemblyTraceBuilder {
	private static final String SOURCE_DIRECT = "DIRECT";
	private static final String SOURCE_REGISTER = "REGISTER";

	private AssemblyTraceBuilder() {
	}

	private static String registeredPath(String typeName, PathExpression relativePath) {
		if (relativePath.isRoot()) {
			return typeName + ".$";
		}
		return typeName + relativePath.toExpression().substring(1);
	}

	/**
	 * Builds the resolution trace from collected data and invokes the tracer.
	 *
	 * @param isFixed whether the builder is in fixed (deterministic) mode
	 */
	public static void buildAndInvoke(
		TraceContext traceContext,
		AssemblyPlan plan,
		long prepTimeNanos,
		long assemblyTimeNanos,
		long totalAdapterTimeNanos,
		boolean isFixed,
		AssemblyTracer tracer
	) {
		if (!traceContext.isEnabled()) {
			return;
		}

		AnalysisResult analysisResult = plan.getAnalysisResult();
		List<PathDirective> manipulators = plan.getScopeSet().getRootScope().getDirectives();
		traceContext.recordBuilderContext(isFixed, analysisResult.isStrictMode());
		List<AnalyzedScope> analyzedDefinedScopes = plan.getAnalyzedDefinedScopes();

		int sequence = 0;

		// Register entries are recorded first — they execute before direct manipulators
		for (AnalyzedScope directives : analyzedDefinedScopes) {
			String typeName = directives.getSelector().toString();

			for (Map.Entry<PathExpression, @Nullable Object> valueEntry : directives.getValuesByPath().entrySet()) {
				Object value = valueEntry.getValue();
				String type;
				if (value instanceof LazyValueHolder) {
					type = "SetLazy";
					value = "<lazy>";
				} else {
					type = "SetDecomposedValue";
				}
				traceContext.recordManipulator(
					registeredPath(typeName, valueEntry.getKey()), type, sequence++, value, null, SOURCE_REGISTER
				);
			}

			for (PathExpression notNullPath : directives.getNotNullPaths()) {
				traceContext.recordManipulator(
					registeredPath(typeName, notNullPath), "SetNotNull", sequence++, null, null, SOURCE_REGISTER
				);
			}

			for (Map.Entry<PathExpression, List<PostConditionFilter>> filterEntry
				: directives.getFiltersByPath().entrySet()) {
				for (int i = 0; i < filterEntry.getValue().size(); i++) {
					traceContext.recordManipulator(
						registeredPath(typeName, filterEntry.getKey()),
						"SetPostCondition",
						sequence++,
						null,
						null,
						SOURCE_REGISTER
					);
				}
			}

			for (Map.Entry<PathExpression, ArbitraryContainerInfo> sizeEntry
				: directives.getContainerSizesByPath().entrySet()) {
				ArbitraryContainerInfo info = sizeEntry.getValue();
				String sizeInfo = "size=" + info.getElementMinSize() + "-" + info.getElementMaxSize();
				traceContext.recordManipulator(
					registeredPath(typeName, sizeEntry.getKey()),
					"ContainerInfo",
					sequence++,
					sizeInfo,
					null,
					SOURCE_REGISTER
				);
			}
		}

		for (PathDirective directive : manipulators) {
			String path = directive.path().toExpression();
			String source = SOURCE_DIRECT;

			String type = directive.getClass().getSimpleName().replace("Directive", "");

			if (directive instanceof SetDirective) {
				SetDirective setDirective = (SetDirective)directive;
				Object value = setDirective.value();
				String sourceType = value != null ? value.getClass().getSimpleName() : null;

				String pathPrefix = path + ".";
				int totalDecomposed = 0;
				int unmatched = 0;
				for (Map.Entry<PathExpression, @Nullable Object> pe : analysisResult.getValuesByPath().entrySet()) {
					if (pe.getKey().toExpression().startsWith(pathPrefix)) {
						totalDecomposed++;
					}
				}
				Set<String> unresolvedNonWildcard = plan.getValues().getUnresolvedNonWildcardPaths();
				for (String up : unresolvedNonWildcard) {
					if (up.startsWith(pathPrefix)) {
						unmatched++;
					}
				}
				int matched = totalDecomposed - unmatched;

				traceContext.recordManipulator(
					path,
					"SetDecomposedValue",
					sequence++,
					value,
					null,
					source,
					totalDecomposed,
					matched,
					sourceType
				);
			} else if (directive instanceof JustDirective) {
				traceContext.recordManipulator(
					path,
					"SetJust",
					sequence++,
					((JustDirective)directive).value(),
					null,
					source
				);
			} else {
				traceContext.recordManipulator(path, type, sequence++, null, null, source);
			}
		}

		for (PathDirective sizeManipulator : manipulators) {
			if (!(sizeManipulator instanceof SizeDirective)) {
				continue;
			}
			SizeDirective directive = (SizeDirective)sizeManipulator;
			String path = directive.path().toExpression();
			ArbitraryContainerInfo info = directive.containerInfo();
			String sizeInfo = "size=" + info.getElementMinSize() + "-" + info.getElementMaxSize();
			traceContext.recordManipulator(path, "ContainerInfo", sequence++, sizeInfo, null, SOURCE_DIRECT);
		}

		Map<PathExpression, @Nullable Object> valuesByPath = analysisResult.getValuesByPath();
		Map<PathExpression, Integer> valueOrderByPath = analysisResult.getValueOrderByPath();
		traceContext.recordValues(valuesByPath, valueOrderByPath);

		// Collisions: same path overwritten by a later set() call
		for (ResolutionTrace.NodeCollision collision : analysisResult.getNodeCollisions()) {
			traceContext.recordNodeCollision(
				collision.path(),
				collision.previousOrder(),
				collision.previousValue(),
				collision.newOrder(),
				collision.newValue()
			);
		}

		// Set final values for accurate winner determination in manipulator override tracking
		traceContext.setFinalValues(valuesByPath, valueOrderByPath);

		for (PathExpression justPath : analysisResult.getJustPaths()) {
			String justPathStr = justPath.toExpression();
			// Find child paths that would be ignored due to this just path
			List<String> ignoredChildPaths = new ArrayList<>();
			for (PathExpression p : valuesByPath.keySet()) {
				String pStr = p.toExpression();
				if (pStr.startsWith(justPathStr + ".") || pStr.startsWith(justPathStr + "[")) {
					ignoredChildPaths.add(pStr);
				}
			}
			traceContext.recordJustPath(justPathStr, ignoredChildPaths);
		}

		traceContext.recordCacheStatus("tree", ResolutionTrace.CacheResult.MISS, "has manipulators");

		Map<String, ValueProjection.UnresolvedPathInfo> unresolvedWithDiag = plan
			.getValues()
			.getUnresolvedPathsWithDiagnostics();
		for (Map.Entry<String, ValueProjection.UnresolvedPathInfo> entry : unresolvedWithDiag.entrySet()) {
			JvmNodeTree.ResolutionDiagnostic diag = entry.getValue().getDiagnostic();
			traceContext.addUnresolvedPath(
				entry.getKey(),
				diag != null ? diag.getReason() : null,
				diag != null ? diag.getAvailableNames() : null
			);
		}

		JvmNodeTree nodeTree = plan.getNodeTree();
		JvmNode root = nodeTree.getRootNode();
		List<JvmNode> rootChildren = nodeTree.getChildren(root);
		if (rootChildren != null && !rootChildren.isEmpty()) {
			List<String> fieldNames = new ArrayList<>();
			for (JvmNode child : rootChildren) {
				String name = child.getNodeName();
				if (name != null) {
					fieldNames.add(name);
				}
			}
			Collections.sort(fieldNames);

			Set<String> unresolvedNonWildcard = plan.getValues().getUnresolvedNonWildcardPaths();
			List<String> unmatchedTargets = new ArrayList<>();
			for (String p : unresolvedNonWildcard) {
				if (p.startsWith("$.") && !p.substring(2).contains(".") && !p.contains("[")) {
					unmatchedTargets.add(p.substring(2));
				}
			}
			Collections.sort(unmatchedTargets);

			if (!unmatchedTargets.isEmpty()) {
				String rootTypeName = root.getConcreteType().getRawType().getSimpleName();
				traceContext.recordPropertyDiscovery(rootTypeName, fieldNames, unmatchedTargets);
			}
		}

		for (AnalyzedScope directives : analyzedDefinedScopes) {
			int manipulatorCount =
				directives.getValuesByPath().size()
					+ directives.getNotNullPaths().size()
					+ directives.getFiltersByPath().size();
			traceContext.recordRegisteredBuilder(
				directives.getSelector().toString(),
				manipulatorCount,
				directives.getContainerSizesByPath().size()
			);
		}

		traceContext.recordTiming("analyze", plan.getAnalyzeTimeNanos());
		traceContext.recordTiming("treeBuild", plan.getTreeBuildTimeNanos());
		traceContext.recordTiming("assembly", assemblyTimeNanos);
		traceContext.recordTiming("total", totalAdapterTimeNanos);
		traceContext.recordTiming("prep", prepTimeNanos);

		traceContext.setNodeCount(plan.getNodeTree().size());
		traceContext.setManipulatorCount(sequence);
		traceContext.setValueCount(analysisResult.getValuesByPath().size());

		ResolutionTrace trace = traceContext.build();
		if (trace != null) {
			tracer.onResolutionComplete(trace);
		}
	}
}
