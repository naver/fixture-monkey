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

package com.navercorp.fixturemonkey.adapter;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.adapter.tracing.AdapterTracer;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;

/**
 * A plugin for configuring the node tree adapter for Java types in FixtureMonkey.
 * <p>
 * This plugin allows configuration of the adapter layer, which provides
 * an alternative object generation path using JvmNodeTree.
 *
 * <pre>{@code
 * FixtureMonkey.builder()
 *     .plugin(new JavaNodeTreeAdapterPlugin()
 *         .enabled(true)
 *         .seed(12345L)
 *         .tracer(AdapterTracer.console()))
 *     .build();
 * }</pre>
 *
 * For Kotlin support, use {@code KotlinNodeTreeAdapterPlugin} instead.
 *
 * @see NodeTreeAdapter
 * @see AdapterTracer
 * @since 1.2.0
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
public class JavaNodeTreeAdapterPlugin implements Plugin {
	private boolean enabled = true;
	private long seed = System.nanoTime();
	private AdapterTracer tracer = AdapterTracer.noOp();

	/**
	 * Enables or disables the node tree adapter.
	 * <p>
	 * When enabled, the default {@link DefaultNodeTreeAdapter} will be used
	 * for object generation using JvmNodeTree.
	 *
	 * @param enabled true to enable the adapter, false to disable
	 * @return this plugin for method chaining
	 */
	public JavaNodeTreeAdapterPlugin enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/**
	 * Sets the seed for random value generation in the adapter.
	 * <p>
	 * Using a fixed seed value ensures reproducible test results.
	 *
	 * @param seed the seed value for random generation
	 * @return this plugin for method chaining
	 */
	public JavaNodeTreeAdapterPlugin seed(long seed) {
		this.seed = seed;
		return this;
	}

	/**
	 * Sets the tracer for debugging resolution process.
	 * <p>
	 * The tracer receives detailed information about how values are resolved
	 * during fixture generation, including manipulator analysis and assembly steps.
	 *
	 * @param tracer the tracer to use for debugging
	 * @return this plugin for method chaining
	 * @see AdapterTracer#console()
	 * @see AdapterTracer#consoleJson()
	 */
	public JavaNodeTreeAdapterPlugin tracer(AdapterTracer tracer) {
		this.tracer = tracer;
		return this;
	}

	@Override
	public void accept(FixtureMonkeyOptionsBuilder optionsBuilder) {
		if (enabled) {
			optionsBuilder.nodeTreeAdapter(new DefaultNodeTreeAdapter(seed));
		}
		optionsBuilder.adapterTracer(tracer);
	}
}
