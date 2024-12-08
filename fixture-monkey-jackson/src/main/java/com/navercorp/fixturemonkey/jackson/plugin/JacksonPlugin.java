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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.navercorp.fixturemonkey.api.introspector.MatchArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.api.property.ContainerElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.jackson.FixtureMonkeyJackson;
import com.navercorp.fixturemonkey.jackson.generator.JsonNodeContainerPropertyGenerator;
import com.navercorp.fixturemonkey.jackson.introspector.JacksonArrayArbitraryIntrospector;
import com.navercorp.fixturemonkey.jackson.introspector.JacksonCollectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.jackson.introspector.JacksonMapArbitraryIntrospector;
import com.navercorp.fixturemonkey.jackson.introspector.JacksonObjectArbitraryIntrospector;
import com.navercorp.fixturemonkey.jackson.introspector.JsonNodeIntrospector;
import com.navercorp.fixturemonkey.jackson.property.ElementJsonSubTypesConcreteTypeResolver;
import com.navercorp.fixturemonkey.jackson.property.JacksonPropertyNameResolver;
import com.navercorp.fixturemonkey.jackson.property.PropertyJsonSubTypesConcreteTypeResolver;

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
	public void accept(FixtureMonkeyOptionsBuilder optionsBuilder) {
		if (!this.matchers.isEmpty()) {
			Matcher matcher = property -> matchers.stream().anyMatch(it -> it.match(property));

			optionsBuilder
				.insertFirstArbitraryIntrospector(matcher, new JacksonObjectArbitraryIntrospector(objectMapper))
				.insertFirstPropertyNameResolver(matcher, new JacksonPropertyNameResolver());
		}

		if (this.defaultOptions) {
			optionsBuilder
				.objectIntrospector(it -> new JacksonObjectArbitraryIntrospector(objectMapper))
				.defaultPropertyNameResolver(new JacksonPropertyNameResolver())
				.containerIntrospector(container -> new MatchArbitraryIntrospector(
					Arrays.asList(
						new JacksonCollectionArbitraryIntrospector(objectMapper),
						new JacksonArrayArbitraryIntrospector(objectMapper),
						new JacksonMapArbitraryIntrospector(objectMapper),
						container
					)
				))
				.insertFirstCandidateConcretePropertyResolvers(
					new MatcherOperator<>(
						property -> getJacksonAnnotation(property, JsonSubTypes.class) != null
							&& isNotJavaContainerType(property)
							&& Modifier.isAbstract(Types.getActualType(property.getType()).getModifiers()),
						PropertyJsonSubTypesConcreteTypeResolver.INSTANCE
					)
				)
				.insertFirstCandidateConcretePropertyResolvers(
					new MatcherOperator<>(
						property -> property instanceof ContainerElementProperty
							&& getJacksonAnnotation(((ContainerElementProperty)property).getContainerProperty(),
							JsonSubTypes.class
						) != null
							&& Modifier.isAbstract(Types.getActualType(property.getType()).getModifiers()),
						ElementJsonSubTypesConcreteTypeResolver.INSTANCE
					)
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

	private static boolean isNotJavaContainerType(Property property) {
		Class<?> actualType = Types.getActualType(property.getType());
		return !Collection.class.isAssignableFrom(actualType);
	}
}
