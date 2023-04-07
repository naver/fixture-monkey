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

import static com.navercorp.fixturemonkey.jackson.property.JacksonAnnotations.getJacksonAnnotation;

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.option.GenerateOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.jackson.FixtureMonkeyJackson;
import com.navercorp.fixturemonkey.jackson.generator.ElementJsonSubTypesObjectPropertyGenerator;
import com.navercorp.fixturemonkey.jackson.generator.JsonNodeContainerPropertyGenerator;
import com.navercorp.fixturemonkey.jackson.generator.PropertyJsonSubTypesObjectPropertyGenerator;
import com.navercorp.fixturemonkey.jackson.introspector.JacksonArbitraryIntrospector;
import com.navercorp.fixturemonkey.jackson.introspector.JsonNodeIntrospector;
import com.navercorp.fixturemonkey.jackson.property.JacksonPropertyNameResolver;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class JacksonPlugin implements Plugin {
	private final ObjectMapper objectMapper;
	private final List<Matcher> matchers = new ArrayList<>();
	private boolean defaultOptions = true;

	public JacksonPlugin(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public JacksonPlugin() {
		this(FixtureMonkeyJackson.defaultObjectMapper());
	}

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
				.insertFirstArbitraryIntrospector(matcher, new JacksonArbitraryIntrospector(objectMapper))
				.insertFirstPropertyNameResolver(matcher, new JacksonPropertyNameResolver());
		}

		if (this.defaultOptions) {
			optionsBuilder
				.objectIntrospector(it -> new JacksonArbitraryIntrospector(objectMapper))
				.defaultPropertyNameResolver(new JacksonPropertyNameResolver())
				.insertFirstArbitraryObjectPropertyGenerator(
					property -> getJacksonAnnotation(property, JsonSubTypes.class) != null,
					PropertyJsonSubTypesObjectPropertyGenerator.INSTANCE
				)
				.insertFirstArbitraryObjectPropertyGenerator(
					property -> property instanceof ElementProperty
						&& getJacksonAnnotation(
						((ElementProperty)property).getContainerProperty(),
						JsonSubTypes.class
					) != null,
					ElementJsonSubTypesObjectPropertyGenerator.INSTANCE
				);
		}

		optionsBuilder
			.insertFirstArbitraryContainerPropertyGenerator(
				JsonNode.class,
				JsonNodeContainerPropertyGenerator.INSTANCE
			)
			.insertFirstArbitraryIntrospector(
				JsonNode.class,
				JsonNodeIntrospector.INSTANCE
			);
	}
}
