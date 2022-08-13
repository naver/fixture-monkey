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

package com.navercorp.fixturemonkey.api.generator;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryGeneratorContext {
	private final ArbitraryProperty property;

	private final List<ArbitraryProperty> children;

	@Nullable
	private final ArbitraryGeneratorContext ownerContext;

	private final BiFunction<ArbitraryGeneratorContext, ArbitraryProperty, Arbitrary<?>> resolveArbitrary;

	@SuppressWarnings("rawtypes")
	private final List<MatcherOperator<? extends FixtureCustomizer>> arbitraryCustomizers;

	@SuppressWarnings("rawtypes")
	public ArbitraryGeneratorContext(
		ArbitraryProperty property,
		List<ArbitraryProperty> children,
		@Nullable ArbitraryGeneratorContext ownerContext,
		BiFunction<ArbitraryGeneratorContext, ArbitraryProperty, Arbitrary<?>> resolveArbitrary,
		List<MatcherOperator<? extends FixtureCustomizer>> arbitraryCustomizers
	) {
		this.property = property;
		this.children = new ArrayList<>(children);
		this.ownerContext = ownerContext;
		this.resolveArbitrary = resolveArbitrary;
		this.arbitraryCustomizers = arbitraryCustomizers;
	}

	public ArbitraryProperty getArbitraryProperty() {
		return this.property;
	}

	public Property getProperty() {
		return this.getArbitraryProperty().getProperty();
	}

	public AnnotatedType getAnnotatedType() {
		return this.getProperty().getAnnotatedType();
	}

	public Type getType() {
		return this.getProperty().getType();
	}

	public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass) {
		return this.getProperty().getAnnotation(annotationClass);
	}

	public List<ArbitraryProperty> getChildren() {
		return Collections.unmodifiableList(this.children);
	}

	public ChildArbitraryContext getChildrenArbitraryContexts() {
		Map<ArbitraryProperty, Arbitrary<?>> childrenValues = new LinkedHashMap<>();
		for (ArbitraryProperty child : this.getChildren()) {
			Arbitrary<?> arbitrary = this.resolveArbitrary.apply(this, child);
			childrenValues.put(child, arbitrary);
		}

		ChildArbitraryContext childArbitraryContext = new ChildArbitraryContext(property.getProperty(), childrenValues);
		arbitraryCustomizers.stream()
			.filter(it -> it.match(property.getProperty()))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.ifPresent(customizer -> customizer.customizeProperties(childArbitraryContext));

		return childArbitraryContext;
	}

	@Nullable
	public ArbitraryGeneratorContext getOwnerContext() {
		return this.ownerContext;
	}

	public boolean isRootContext() {
		return this.property.isRoot();
	}

	@SuppressWarnings("rawtypes")
	public List<MatcherOperator<? extends FixtureCustomizer>> getArbitraryCustomizers() {
		return arbitraryCustomizers;
	}
}
