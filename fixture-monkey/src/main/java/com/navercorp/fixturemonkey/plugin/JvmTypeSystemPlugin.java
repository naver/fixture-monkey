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

/**
 * SPI for configuring JVM language/type-system specific machinery that the standard
 * {@link com.navercorp.fixturemonkey.api.plugin.Plugin} cannot reach through
 * {@code FixtureMonkeyOptionsBuilder} alone.
 * <p>
 * The typical use case is supplying a customized {@code AssemblyPlanner} with
 * language-specific {@code JvmNodePromoter}s, {@code LeafTypeResolver}s, and
 * {@code JvmNodeCandidateGenerator} decorators (e.g., Kotlin value classes, Scala
 * case classes).
 * <p>
 * Implementations are commonly combined with {@code Plugin} on the same class; a single
 * {@code FixtureMonkeyBuilder.plugin(...)} call invokes both SPIs.
 */
@FunctionalInterface
@API(since = "1.1.21", status = Status.EXPERIMENTAL)
public interface JvmTypeSystemPlugin {
	void configure(JvmTypeSystem typeSystem);
}
