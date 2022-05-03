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

package com.navercorp.fixturemonkey.jackson.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.DefaultArbitraryGenerator;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.module.Module;
import com.navercorp.fixturemonkey.api.option.GenerateOptionsBuilder;
import com.navercorp.fixturemonkey.jackson.introspector.JacksonArbitraryIntrospector;
import com.navercorp.fixturemonkey.jackson.property.JacksonPropertyNameResolver;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JacksonModule implements Module {
	private final List<Matcher> matchers = new ArrayList<>();
	private boolean defaultGenerator = false;

	public JacksonModule by(Matcher matcher) {
		this.matchers.add(matcher);
		return this;
	}

	public JacksonModule by(Class<?> matchType) {
		this.matchers.add(new AssignableTypeMatcher(matchType));
		return this;
	}

	public JacksonModule defaultGenerator(boolean defaultGenerator) {
		this.defaultGenerator = defaultGenerator;
		return this;
	}

	@Override
	public void accept(GenerateOptionsBuilder optionsBuilder) {
		if (!this.matchers.isEmpty()) {
			Matcher matcher = property -> matchers.stream().anyMatch(it -> it.match(property));

			optionsBuilder.insertFirstArbitraryIntrospector(matcher, new JacksonArbitraryIntrospector());
			optionsBuilder.insertFirstPropertyNameResolver(matcher, new JacksonPropertyNameResolver());
		}

		if (this.defaultGenerator) {
			optionsBuilder.defaultArbitraryGenerator(
				new DefaultArbitraryGenerator(
					Arrays.asList(
						DefaultArbitraryGenerator.JAVA_INTROSPECTOR,
						DefaultArbitraryGenerator.JAVA_CONTAINER_INTROSPECTOR,
						new JacksonArbitraryIntrospector()
					)
				)
			);
		}
	}
}
