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

package com.navercorp.fixturemonkey.plugin;

import java.util.List;
import java.util.function.UnaryOperator;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.objectfarm.api.input.InlinedValueResolver;
import com.navercorp.objectfarm.api.node.JvmNodePromoter;
import com.navercorp.objectfarm.api.node.LeafTypeResolver;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator;

/**
 * Narrow SPI handed to {@link JvmTypeSystemPlugin#configure} that exposes only the
 * registration points a JVM language/type-system plugin needs. Hides the rest of
 * {@code FixtureMonkeyBuilder} so end users never see these hooks on the builder type.
 * <p>
 * A plugin registers the parts its language needs; {@code FixtureMonkeyBuilder} constructs the
 * single {@code AssemblyPlanner} from them once every plugin has been applied. Registering parts
 * rather than a ready-made planner is what lets several plugins contribute at the same time, and
 * lets the builder inject configuration that is only settled at build time.
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
public interface JvmTypeSystem {
	/**
	 * Registers {@link JvmNodePromoter}s that rewrite nodes while the candidate tree is built,
	 * for language constructs the default promoters do not recognize.
	 * <p>
	 * Promoters from multiple plugins accumulate in registration order.
	 */
	void nodePromoters(List<JvmNodePromoter> nodePromoters);

	/**
	 * Registers {@link LeafTypeResolver}s that decide which language types are leaves, meaning
	 * they are generated as a whole instead of being expanded into child nodes.
	 * <p>
	 * Resolvers from multiple plugins accumulate in registration order, and the first one that
	 * answers {@code true} wins.
	 */
	void leafTypeResolvers(List<LeafTypeResolver> leafTypeResolvers);

	/**
	 * Registers a decorator around the {@link JvmNodeCandidateGenerator}, typically to add a
	 * language-specific {@code isSupported} check ahead of the default generator.
	 * <p>
	 * Decorators from multiple plugins compose in registration order, so the one registered last
	 * ends up outermost.
	 */
	void candidateGeneratorWrapper(UnaryOperator<JvmNodeCandidateGenerator> candidateGeneratorWrapper);

	/**
	 * Installs the {@link InlinedValueResolver} applied while a value passed to {@code set(...)}
	 * is decomposed into child paths.
	 * <p>
	 * Decomposition reads each field of the value off the JVM. When a language inlines a value type
	 * into its owner's field — as Kotlin does for value classes — that read yields the underlying
	 * value, while rebuilding the owner expects the value type. The resolver reconstructs it.
	 * <p>
	 * Only the read step is replaced. Decomposition happens at two points that enumerate and name
	 * fields differently — directive analysis keys paths through the configured
	 * {@code PropertyNameResolver}, assembly keys them by field name — and both keep doing so.
	 * <p>
	 * Resolvers from multiple plugins apply in registration order.
	 */
	void inlinedValueResolver(InlinedValueResolver inlinedValueResolver);
}
