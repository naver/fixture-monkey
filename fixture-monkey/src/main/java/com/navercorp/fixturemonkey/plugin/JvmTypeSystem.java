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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.planner.AssemblyPlanner;

/**
 * Narrow SPI handed to {@link JvmTypeSystemPlugin#configure} that exposes only the
 * registration points a JVM language/type-system plugin needs. Hides the rest of
 * {@code FixtureMonkeyBuilder} so end users never see these hooks on the builder type.
 */
@API(since = "1.1.21", status = Status.EXPERIMENTAL)
public interface JvmTypeSystem {
	/**
	 * Installs the {@link AssemblyPlanner} that the resulting {@code FixtureMonkey} uses
	 * for JVM tree planning and assembly. Language-specific plugins typically supply a
	 * planner pre-configured with their own {@code JvmNodePromoter}s, {@code LeafTypeResolver}s,
	 * and {@code JvmNodeCandidateGenerator} decorators.
	 */
	void assemblyPlanner(AssemblyPlanner planner);
}
