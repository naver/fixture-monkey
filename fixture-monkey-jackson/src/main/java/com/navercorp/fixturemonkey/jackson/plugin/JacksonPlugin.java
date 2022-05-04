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

package com.navercorp.fixturemonkey.jackson.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.option.GenerateOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.jackson.introspector.JacksonArbitraryIntrospector;
import com.navercorp.fixturemonkey.jackson.property.JacksonPropertyNameResolver;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JacksonPlugin implements Plugin {
	private final List<Matcher> matchers = new ArrayList<>();
	private boolean defaultOptions = true;

	public JacksonPlugin by(Matcher matcher) {
		this.matchers.add(matcher);
		return this;
	}

	public JacksonPlugin by(Class<?> matchType) {
		this.matchers.add(new AssignableTypeMatcher(matchType));
		return this;
	}

	public JacksonPlugin defaultOptions(boolean defaultOptions) {
		this.defaultOptions = defaultOptions;
		return this;
	}

	@Override
	public void accept(GenerateOptionsBuilder optionsBuilder) {
		if (!this.matchers.isEmpty()) {
			Matcher matcher = property -> matchers.stream().anyMatch(it -> it.match(property));

			optionsBuilder
				.insertFirstArbitraryIntrospector(matcher, new JacksonArbitraryIntrospector())
				.insertFirstPropertyNameResolver(matcher, new JacksonPropertyNameResolver());
		}

		if (this.defaultOptions) {
			optionsBuilder
				.objectIntrospector(it -> new JacksonArbitraryIntrospector())
				.defaultPropertyNameResolver(new JacksonPropertyNameResolver());
		}
	}
}
