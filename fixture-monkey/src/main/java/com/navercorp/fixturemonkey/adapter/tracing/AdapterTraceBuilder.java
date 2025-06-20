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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.analysis.AdaptationResult;
import com.navercorp.fixturemonkey.adapter.analysis.AnalysisResult;
import com.navercorp.fixturemonkey.adapter.analysis.ManipulatorAnalyzer;
import com.navercorp.fixturemonkey.adapter.converter.PredicatePathConverter;
import com.navercorp.fixturemonkey.adapter.projection.LazyValueHolder;
import com.navercorp.fixturemonkey.adapter.projection.ValueProjection;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.customizer.ApplyNodeCountManipulator;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.customizer.NodeManipulator;
import com.navercorp.fixturemonkey.customizer.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.customizer.NodeSetJustManipulator;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Builds the resolution trace from collected data and invokes the tracer.
 */
public final class AdapterTraceBuilder {
	private AdapterTraceBuilder() {
	}

	/**
	 * Builds the resolution trace from collected data and invokes the tracer.
	 *
	 * @param relevantTypes types that exist in the sample target's type tree; only register
	 *                      operations for these types are included in the trace output
	 * @param isFixed whether the builder is in fixed (deterministic) mode
	 */
	public static void buildAndInvoke(
		TraceContext traceContext,
		List<ArbitraryManipulator> manipulators,
		List<ContainerInfoManipulator> containerInfoManipulators,
		AnalysisResult analysisResult,
		AdaptationResult adaptationResult,
		long prepTimeNanos,
		long assemblyTimeNanos,
		long totalAdapterTimeNanos,
		Map<JvmType, Map<String, @Nullable Object>> typedValues,
		Map<JvmType, Map<String, ArbitraryContainerInfo>> typedContainerSizes,
		Set<Class<?>> relevantTypes,
		boolean isFixed,
		AdapterTracer adapterTracer
	) {
		if (!traceContext.isEnabled()) {
			return;
		}

		traceContext.recordBuilderContext(isFixed, analysisResult.isStrictMode());

		int sequence = 0;

		// Register entries are recorded first — they execute before direct manipulators
		if (typedValues != null) {
			for (Map.Entry<JvmType, Map<String, @Nullable Object>> entry : typedValues.entrySet()) {
				Class<?> rawType = entry.getKey().getRawType();
				if (!relevantTypes.contains(rawType)) {
					continue;
				}
				String typeName = rawType.getSimpleName();
				for (Map.Entry<String, @Nullable Object> fieldEntry : entry.getValue().entrySet()) {
					String fieldPath = fieldEntry.getKey();
					String path = "$".equals(fieldPath) ? typeName + ".$" : typeName + "." + fieldPath;
					Object value = fieldEntry.getValue();
					String type;
					if (value instanceof LazyValueHolder) {
						type = "SetLazy";
						value = "<lazy>";
					} else {
						type = "SetDecomposedValue";
					}
					traceContext.recordManipulator(path, type, sequence++, value, null, "REGISTER");
				}
			}
		}

		if (typedContainerSizes != null) {
			for (Map.Entry<JvmType, Map<String, ArbitraryContainerInfo>> entry : typedContainerSizes.entrySet()) {
				Class<?> rawType = entry.getKey().getRawType();
				if (!relevantTypes.contains(rawType)) {
					continue;
				}
				String typeName = rawType.getSimpleName();
				for (Map.Entry<String, ArbitraryContainerInfo> sizeEntry : entry.getValue().entrySet()) {
					String path = typeName + "." + sizeEntry.getKey();
					ArbitraryContainerInfo info = sizeEntry.getValue();
					String sizeInfo = "size=" + info.getElementMinSize() + "-" + info.getElementMaxSize();
					traceContext.recordManipulator(path, "ContainerInfo", sequence++, sizeInfo, null, "REGISTER");
				}
			}
		}

		for (ArbitraryManipulator manipulator : manipulators) {
			List<NextNodePredicate> predicates = PredicatePathConverter.extractPredicates(
				manipulator.getNodeResolver()
			);
			String path = PredicatePathConverter.toExpression(predicates);

			boolean isRegistered = ManipulatorAnalyzer.hasStaticNodeResolver(manipulator.getNodeResolver());
			String source = isRegistered ? "REGISTERED_BUILDER" : "DIRECT";

			NodeManipulator nm = manipulator.getNodeManipulator();
			if (nm instanceof ApplyNodeCountManipulator) {
				nm = ((ApplyNodeCountManipulator)nm).getNodeManipulator();
			}

			String type = nm.getClass().getSimpleName().replace("Node", "").replace("Manipulator", "");
			Object value = null;

			if (nm instanceof NodeSetJustManipulator) {
				value = ((NodeSetJustManipulator)nm).getValue();
			} else if (nm instanceof NodeSetDecomposedValueManipulator) {
				value = ((NodeSetDecomposedValueManipulator<?>)nm).getValue();
			}

			if (nm instanceof NodeSetDecomposedValueManipulator) {
				Object decomposedValue = ((NodeSetDecomposedValueManipulator<?>)nm).getValue();
				String sourceType = decomposedValue != null ? decomposedValue.getClass().getSimpleName() : null;

				String pathPrefix = path + ".";
				int totalDecomposed = 0;
				int unmatched = 0;
				for (Map.Entry<PathExpression, @Nullable Object> pe : analysisResult.getValuesByPath().entrySet()) {
					if (pe.getKey().toExpression().startsWith(pathPrefix)) {
						totalDecomposed++;
					}
				}
				Set<String> unresolvedNonWildcard = adaptationResult.getValues().getUnresolvedNonWildcardPaths();
				for (String up : unresolvedNonWildcard) {
					if (up.startsWith(pathPrefix)) {
						unmatched++;
					}
				}
				int matched = totalDecomposed - unmatched;

				traceContext.recordManipulator(
					path,
					type,
					sequence++,
					value,
					null,
					source,
					totalDecomposed,
					matched,
					sourceType
				);
			} else {
				traceContext.recordManipulator(path, type, sequence++, value, null, source);
			}
		}

		for (ContainerInfoManipulator cim : containerInfoManipulators) {
			String path = PredicatePathConverter.toExpression(cim.getNextNodePredicates());
			ArbitraryContainerInfo info = cim.getContainerInfo();
			String sizeInfo = "size=" + info.getElementMinSize() + "-" + info.getElementMaxSize();
			traceContext.recordManipulator(path, "ContainerInfo", sequence++, sizeInfo, null, "DIRECT");
		}

		for (AnalysisResult.LazyManipulatorDescriptor lazyDescriptor : analysisResult.getLazyManipulators()) {
			Map<String, @Nullable Object> decomposed = new HashMap<>();
			for (Map.Entry<PathExpression, @Nullable Object> entry : analysisResult.getValuesByPath().entrySet()) {
				PathExpression valuePath = entry.getKey();
				Integer valueOrder = analysisResult.getValueOrderByPath().get(valuePath);
				if (
					valueOrder != null
						&& valueOrder > lazyDescriptor.getOrder()
						&& valuePath.startsWith(lazyDescriptor.getPathExpression())
				) {
					decomposed.put(valuePath.toExpression(), entry.getValue());
				}
			}
			if (!decomposed.isEmpty()) {
				traceContext.recordManipulator(
					lazyDescriptor.getPathExpression().toExpression(),
					"Lazy",
					sequence++,
					"evaluated",
					decomposed,
					"LAZY_EVALUATED"
				);
			}
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

		if (adaptationResult.isCacheHit()) {
			traceContext.recordCacheStatus("tree", ResolutionTrace.CacheResult.HIT, null);
		} else {
			traceContext.recordCacheStatus("tree", ResolutionTrace.CacheResult.MISS, "has manipulators");
		}

		Map<String, ValueProjection.UnresolvedPathInfo> unresolvedWithDiag = adaptationResult
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

		JvmNodeTree nodeTree = adaptationResult.getNodeTree();
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

			Set<String> unresolvedNonWildcard = adaptationResult.getValues().getUnresolvedNonWildcardPaths();
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

		if (typedValues != null) {
			for (Map.Entry<JvmType, Map<String, @Nullable Object>> entry : typedValues.entrySet()) {
				if (!relevantTypes.contains(entry.getKey().getRawType())) {
					continue;
				}
				String targetType = entry.getKey().getRawType().getSimpleName();
				int manipulatorCount = entry.getValue().size();
				int containerSizeCount =
					typedContainerSizes != null && typedContainerSizes.containsKey(entry.getKey())
						? typedContainerSizes.get(entry.getKey()).size()
						: 0;
				traceContext.recordRegisteredBuilder(targetType, manipulatorCount, containerSizeCount);
			}
		}

		traceContext.recordTiming("analyze", adaptationResult.getAnalyzeTimeNanos());
		traceContext.recordTiming("treeBuild", adaptationResult.getTreeBuildTimeNanos());
		traceContext.recordTiming("assembly", assemblyTimeNanos);
		traceContext.recordTiming("total", totalAdapterTimeNanos);
		traceContext.recordTiming("prep", prepTimeNanos);

		traceContext.setNodeCount(adaptationResult.getNodeTree().size());
		traceContext.setManipulatorCount(sequence);
		traceContext.setValueCount(analysisResult.getValuesByPath().size());
		traceContext.setCacheHit(adaptationResult.isCacheHit());

		ResolutionTrace trace = traceContext.build();
		if (trace != null) {
			adapterTracer.onResolutionComplete(trace);
		}
	}
}
