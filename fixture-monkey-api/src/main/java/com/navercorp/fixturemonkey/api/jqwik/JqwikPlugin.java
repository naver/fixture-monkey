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

package com.navercorp.fixturemonkey.api.jqwik;

import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;

public final class JqwikPlugin implements Plugin {
	private JavaArbitraryResolver javaArbitraryResolver;
	private JavaTypeArbitraryGenerator javaTypeArbitraryGenerator = new JavaTypeArbitraryGenerator() {
	};
	private JavaTimeArbitraryResolver javaTimeArbitraryResolver;
	private JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator = new JavaTimeTypeArbitraryGenerator() {
	};

	public JqwikPlugin javaArbitraryResolver(JavaArbitraryResolver javaArbitraryResolver) {
		this.javaArbitraryResolver = javaArbitraryResolver;
		return this;
	}

	public JqwikPlugin javaTypeArbitraryGenerator(JavaTypeArbitraryGenerator javaTypeArbitraryGenerator) {
		this.javaTypeArbitraryGenerator = javaTypeArbitraryGenerator;
		return this;
	}

	public JqwikPlugin javaTimeArbitraryResolver(JavaTimeArbitraryResolver javaTimeArbitraryResolver) {
		this.javaTimeArbitraryResolver = javaTimeArbitraryResolver;
		return this;
	}

	public JqwikPlugin javaTimeTypeArbitraryGenerator(JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator) {
		this.javaTimeTypeArbitraryGenerator = javaTimeTypeArbitraryGenerator;
		return this;
	}

	@Override
	public void accept(FixtureMonkeyOptionsBuilder optionsBuilder) {
		optionsBuilder.javaTypeArbitraryGeneratorSet(
				constraintGenerator -> new JqwikJavaTypeArbitraryGeneratorSet(
					javaTypeArbitraryGenerator,
					javaArbitraryResolver == null
						? new JqwikJavaArbitraryResolver(constraintGenerator)
						: javaArbitraryResolver
				)
			)
			.javaTimeArbitraryGeneratorSet(
				constraintGenerator -> new JqwikJavaTimeArbitraryGeneratorSet(
					javaTimeTypeArbitraryGenerator,
					javaTimeArbitraryResolver == null
						? new JqwikJavaTimeArbitraryResolver(constraintGenerator)
						: javaTimeArbitraryResolver
				)
			);
	}
}
