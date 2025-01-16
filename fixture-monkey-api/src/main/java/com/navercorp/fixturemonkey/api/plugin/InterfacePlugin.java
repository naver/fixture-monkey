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

package com.navercorp.fixturemonkey.api.plugin;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.introspector.AnonymousArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.MatchArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.ExactTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.ConcreteTypeCandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.type.Types;

/**
 * This plugin is used to extend an interface.
 */
@API(since = "1.0.6", status = Status.EXPERIMENTAL)
public final class InterfacePlugin implements Plugin {
	private final List<MatcherOperator<CandidateConcretePropertyResolver>> candidateConcretePropertyResolvers =
		new ArrayList<>();
	private boolean useAnonymousArbitraryIntrospector = true;

	/**
	 * Registers implementations for a given interface.
	 * This method validates that the provided class is an interface.
	 * It throws an IllegalArgumentException if the validation fails.
	 *
	 * @param <T>             the type parameter of the interface
	 * @param interfaceType   the interface class to be implemented
	 * @param implementations a list of classes implementing the interface
	 * @return the InterfacePlugin instance for fluent chaining
	 * @throws IllegalArgumentException if the first parameter is not an interface
	 */
	public <T> InterfacePlugin interfaceImplements(
		Class<T> interfaceType,
		List<Class<? extends T>> implementations
	) {
		if (!Modifier.isInterface(interfaceType.getModifiers())) {
			throw new IllegalArgumentException(
				"interfaceImplements option first parameter should be interface. "
					+ interfaceType.getTypeName()
			);
		}

		return interfaceImplements(new ExactTypeMatcher(interfaceType), implementations);
	}

	/**
	 * Registers an interface implementation with a specified matcher.
	 * This method facilitates adding custom implementations for interfaces using a matcher.
	 *
	 * @param <T>             the type parameter of the interface
	 * @param matcher         the matcher to be used for matching interfaces
	 * @param implementations a list of classes implementing the interface
	 * @return the InterfacePlugin instance for fluent chaining
	 * @throws IllegalArgumentException if the first parameter is not an interface
	 */
	public <T> InterfacePlugin interfaceImplements(
		Matcher matcher,
		List<Class<? extends T>> implementations
	) {
		return this.interfaceImplements(matcher, new ConcreteTypeCandidateConcretePropertyResolver<>(implementations));
	}

	/**
	 * Registers an interface implementation with a specified matcher.
	 * This method facilitates adding custom implementations for interfaces using a matcher.
	 *
	 * @param matcher                  the matcher to be used for matching interfaces
	 * @param concretePropertyResolver the resolver to resolve the concrete properties
	 * @return the InterfacePlugin instance for fluent chaining
	 * @throws IllegalArgumentException if the first parameter is not an interface
	 */
	public InterfacePlugin interfaceImplements(
		Matcher matcher,
		CandidateConcretePropertyResolver concretePropertyResolver
	) {
		this.candidateConcretePropertyResolvers.add(
			new MatcherOperator<>(
				matcher.intersect(p -> Modifier.isInterface(Types.getActualType(p.getType()).getModifiers())),
				concretePropertyResolver
			)
		);

		return this;
	}

	/**
	 * Registers implementations for a given abstract class.
	 * This method validates that the provided class is an abstract class.
	 * It throws an IllegalArgumentException if the validation fails.
	 *
	 * @param <T>               the type parameter of the interface
	 * @param abstractClassType the abstract class type to be implemented
	 * @param implementations   a list of classes implementing the abstract class
	 * @return the InterfacePlugin instance for fluent chaining
	 * @throws IllegalArgumentException if the first parameter is not an abstract class
	 */
	public <T> InterfacePlugin abstractClassExtends(
		Class<T> abstractClassType,
		List<Class<? extends T>> implementations
	) {
		if (!(Modifier.isAbstract(abstractClassType.getModifiers())
			&& !Modifier.isInterface(abstractClassType.getModifiers()))) {
			throw new IllegalArgumentException(
				"abstractClassExtends option first parameter should be abstract class. "
					+ abstractClassType.getTypeName()
			);
		}

		return abstractClassExtends(new ExactTypeMatcher(abstractClassType), implementations);
	}

	/**
	 * Registers an abstract class implementation with a specified matcher.
	 * This method facilitates adding custom implementations for interfaces using a matcher.
	 *
	 * @param <T>             the type parameter of the abstract class
	 * @param matcher         the matcher to be used for matching abstract class
	 * @param implementations a list of classes implementing the abstract class
	 * @return the InterfacePlugin instance for fluent chaining
	 * @throws IllegalArgumentException if the first parameter is not an abstract class
	 */
	public <T> InterfacePlugin abstractClassExtends(
		Matcher matcher,
		List<Class<? extends T>> implementations
	) {
		return this.abstractClassExtends(matcher, new ConcreteTypeCandidateConcretePropertyResolver<>(implementations));
	}

	/**
	 * Registers an abstract class implementation with a specified matcher.
	 * This method facilitates adding custom implementations for interfaces using a matcher.
	 *
	 * @param matcher                  the matcher to be used for matching abstract class
	 * @param concretePropertyResolver the resolver to resolve the concrete properties
	 * @return the InterfacePlugin instance for fluent chaining
	 * @throws IllegalArgumentException if the first parameter is not an abstract class
	 */
	public InterfacePlugin abstractClassExtends(
		Matcher matcher,
		CandidateConcretePropertyResolver concretePropertyResolver
	) {
		this.candidateConcretePropertyResolvers.add(
			new MatcherOperator<>(
				matcher.intersect(p -> Modifier.isAbstract(Types.getActualType(p.getType()).getModifiers())),
				concretePropertyResolver
			)
		);

		return this;
	}

	/**
	 * Configures the use of an anonymous arbitrary introspector.
	 * By default, this option is enabled (default value is true).
	 * When enabled, it uses an instance of {@link AnonymousArbitraryIntrospector} as the fallback introspector.
	 *
	 * @param useAnonymousArbitraryIntrospector a boolean flag to enable (true) or disable (false)
	 *                                          the anonymous arbitrary introspector. Default value is true.
	 * @return the InterfacePlugin instance for fluent chaining
	 */
	public InterfacePlugin useAnonymousArbitraryIntrospector(boolean useAnonymousArbitraryIntrospector) {
		this.useAnonymousArbitraryIntrospector = useAnonymousArbitraryIntrospector;
		return this;
	}

	@Override
	public void accept(FixtureMonkeyOptionsBuilder optionsBuilder) {
		for (MatcherOperator<CandidateConcretePropertyResolver> resolver : candidateConcretePropertyResolvers) {
			optionsBuilder.insertFirstCandidateConcretePropertyResolvers(resolver);
		}

		if (useAnonymousArbitraryIntrospector) {
			optionsBuilder.fallbackIntrospector(it ->
				new MatchArbitraryIntrospector(Arrays.asList(it, AnonymousArbitraryIntrospector.INSTANCE))
			);
		}
	}
}
