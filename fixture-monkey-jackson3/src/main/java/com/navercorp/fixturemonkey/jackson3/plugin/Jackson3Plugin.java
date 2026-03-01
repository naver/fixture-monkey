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

package com.navercorp.fixturemonkey.jackson3.plugin;

import static com.navercorp.fixturemonkey.jackson3.property.Jackson3Annotations.getJacksonAnnotation;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.navercorp.fixturemonkey.api.introspector.MatchArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.api.property.ContainerElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.jackson3.FixtureMonkeyJackson3;
import com.navercorp.fixturemonkey.jackson3.generator.Jackson3JsonNodeContainerPropertyGenerator;
import com.navercorp.fixturemonkey.jackson3.introspector.Jackson3ArrayArbitraryIntrospector;
import com.navercorp.fixturemonkey.jackson3.introspector.Jackson3CollectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.jackson3.introspector.Jackson3JsonNodeIntrospector;
import com.navercorp.fixturemonkey.jackson3.introspector.Jackson3MapArbitraryIntrospector;
import com.navercorp.fixturemonkey.jackson3.introspector.Jackson3ObjectArbitraryIntrospector;
import com.navercorp.fixturemonkey.jackson3.property.Jackson3ElementJsonSubTypesConcreteTypeResolver;
import com.navercorp.fixturemonkey.jackson3.property.Jackson3PropertyJsonSubTypesConcreteTypeResolver;
import com.navercorp.fixturemonkey.jackson3.property.Jackson3PropertyNameResolver;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public final class Jackson3Plugin implements Plugin {
	private final ObjectMapper objectMapper;
	private final List<Matcher> matchers = new ArrayList<>();
	private boolean defaultOptions = true;

	public Jackson3Plugin(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public Jackson3Plugin() {
		this(FixtureMonkeyJackson3.defaultJsonMapper());
	}

	public Jackson3Plugin by(Matcher matcher) {
		this.matchers.add(matcher);
		return this;
	}

	public Jackson3Plugin by(Class<?> matchType) {
		this.matchers.add(new AssignableTypeMatcher(matchType));
		return this;
	}

	public Jackson3Plugin defaultOptions(boolean defaultOptions) {
		this.defaultOptions = defaultOptions;
		return this;
	}

	@Override
	public void accept(FixtureMonkeyOptionsBuilder optionsBuilder) {
		if (!this.matchers.isEmpty()) {
			Matcher matcher = property -> matchers.stream().anyMatch(it -> it.match(property));

			optionsBuilder
				.insertFirstArbitraryIntrospector(matcher, new Jackson3ObjectArbitraryIntrospector(objectMapper))
				.insertFirstPropertyNameResolver(matcher, new Jackson3PropertyNameResolver());
		}

		if (this.defaultOptions) {
			optionsBuilder
				.objectIntrospector(it -> new Jackson3ObjectArbitraryIntrospector(objectMapper))
				.defaultPropertyNameResolver(new Jackson3PropertyNameResolver())
				.containerIntrospector(container -> new MatchArbitraryIntrospector(
					Arrays.asList(
						new Jackson3CollectionArbitraryIntrospector(objectMapper),
						new Jackson3ArrayArbitraryIntrospector(objectMapper),
						new Jackson3MapArbitraryIntrospector(objectMapper),
						container
					)
				))
				.insertFirstCandidateConcretePropertyResolvers(
					new MatcherOperator<>(
						property -> getJacksonAnnotation(property, JsonSubTypes.class) != null
							&& isNotJavaContainerType(property)
							&& Modifier.isAbstract(Types.getActualType(property.getType()).getModifiers()),
						Jackson3PropertyJsonSubTypesConcreteTypeResolver.INSTANCE
					)
				)
				.insertFirstCandidateConcretePropertyResolvers(
					new MatcherOperator<>(
						property -> property instanceof ContainerElementProperty
							&& getJacksonAnnotation(((ContainerElementProperty)property).getContainerProperty(),
							JsonSubTypes.class
						) != null
							&& Modifier.isAbstract(Types.getActualType(property.getType()).getModifiers()),
						Jackson3ElementJsonSubTypesConcreteTypeResolver.INSTANCE
					)
				);
		}

		optionsBuilder
			.insertFirstArbitraryContainerPropertyGenerator(
				JsonNode.class,
				Jackson3JsonNodeContainerPropertyGenerator.INSTANCE
			)
			.insertFirstArbitraryIntrospector(
				JsonNode.class,
				Jackson3JsonNodeIntrospector.INSTANCE
			);
	}

	private static boolean isNotJavaContainerType(Property property) {
		Class<?> actualType = Types.getActualType(property.getType());
		return !Collection.class.isAssignableFrom(actualType);
	}
}
