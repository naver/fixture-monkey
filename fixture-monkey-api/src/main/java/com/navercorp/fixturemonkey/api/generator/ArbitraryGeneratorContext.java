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
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.context.MonkeyGeneratorContext;
import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryGeneratorContext {
	private final Property resolvedProperty;

	private final ArbitraryProperty property;

	private final List<ArbitraryProperty> children;

	@Nullable
	private final ArbitraryGeneratorContext ownerContext;

	private final BiFunction<ArbitraryGeneratorContext, ArbitraryProperty, CombinableArbitrary> resolveArbitrary;

	@SuppressWarnings("rawtypes")
	private final List<MatcherOperator<? extends FixtureCustomizer>> fixtureCustomizers;

	private final MonkeyGeneratorContext monkeyGeneratorContext;

	private final LazyArbitrary<PropertyPath> pathProperty = LazyArbitrary.lazy(this::initPathProperty);

	private final LazyArbitrary<ChildArbitraryContext> childArbitraryContext =
		LazyArbitrary.lazy(this::initChildArbitraryContext);

	@SuppressWarnings("rawtypes")
	public ArbitraryGeneratorContext(
		Property resolvedProperty,
		ArbitraryProperty property,
		List<ArbitraryProperty> children,
		@Nullable ArbitraryGeneratorContext ownerContext,
		BiFunction<ArbitraryGeneratorContext, ArbitraryProperty, CombinableArbitrary> resolveArbitrary,
		List<MatcherOperator<? extends FixtureCustomizer>> fixtureCustomizers,
		MonkeyGeneratorContext monkeyGeneratorContext
	) {
		this.resolvedProperty = resolvedProperty;
		this.property = property;
		this.children = new ArrayList<>(children);
		this.ownerContext = ownerContext;
		this.resolveArbitrary = resolveArbitrary;
		this.fixtureCustomizers = fixtureCustomizers;
		this.monkeyGeneratorContext = monkeyGeneratorContext;
	}

	public ArbitraryProperty getArbitraryProperty() {
		return this.property;
	}

	public Property getResolvedProperty() {
		return this.resolvedProperty;
	}

	public AnnotatedType getResolvedAnnotatedType() {
		return this.getResolvedProperty().getAnnotatedType();
	}

	public Type getResolvedType() {
		return this.getResolvedProperty().getType();
	}

	public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass) {
		return this.getResolvedProperty().getAnnotation(annotationClass);
	}

	public List<ArbitraryProperty> getChildren() {
		return Collections.unmodifiableList(this.children);
	}

	public Map<ArbitraryProperty, CombinableArbitrary> getCombinableArbitrariesByArbitraryProperty() {
		return childArbitraryContext.getValue().getCombinableArbitrariesByArbitraryProperty();
	}

	public Map<String, CombinableArbitrary> getCombinableArbitrariesByResolvedName() {
		return childArbitraryContext.getValue().getCombinableArbitrariesByResolvedName();
	}

	public Map<String, CombinableArbitrary> getCombinableArbitrariesByPropertyName() {
		return childArbitraryContext.getValue().getCombinableArbitraryByPropertyName();
	}

	public List<CombinableArbitrary> getElementCombinableArbitraryList() {
		return childArbitraryContext.getValue().getElementCombinableArbitraryList();
	}

	@Deprecated // would be removed in 0.6.0
	public List<Object> getArbitraries() {
		return childArbitraryContext.getValue().getArbitraries();
	}

	@Nullable
	public ArbitraryGeneratorContext getOwnerContext() {
		return this.ownerContext;
	}

	public boolean isRootContext() {
		return this.property.getObjectProperty().isRoot();
	}

	@SuppressWarnings("rawtypes")
	public List<MatcherOperator<? extends FixtureCustomizer>> getFixtureCustomizers() {
		return fixtureCustomizers;
	}

	public synchronized boolean isUniqueAndCheck(PropertyPath property, Object value) {
		return monkeyGeneratorContext.isUniqueAndCheck(property, value);
	}

	public void evictUnique(PropertyPath propertyPath) {
		monkeyGeneratorContext.evictUnique(propertyPath);
	}

	public PropertyPath getPathProperty() {
		return pathProperty.getValue();
	}

	private PropertyPath initPathProperty() {
		if (ownerContext == null) {
			return new PropertyPath(property.getObjectProperty().getProperty(), null, 1);
		}

		PropertyPath parentPropertyPath = ownerContext.getPathProperty();
		return new PropertyPath(
			property.getObjectProperty().getProperty(),
			parentPropertyPath,
			parentPropertyPath.getDepth() + 1
		);
	}

	private ChildArbitraryContext initChildArbitraryContext() {
		Map<ArbitraryProperty, CombinableArbitrary> childrenValues = new LinkedHashMap<>();
		for (ArbitraryProperty child : this.getChildren()) {
			CombinableArbitrary arbitrary = this.resolveArbitrary.apply(this, child);
			childrenValues.put(child, arbitrary);
		}

		ChildArbitraryContext childArbitraryContext = new ChildArbitraryContext(
			property.getObjectProperty().getProperty(),
			childrenValues
		);

		fixtureCustomizers.stream()
			.filter(it -> it.match(property.getObjectProperty().getProperty()))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.ifPresent(customizer -> customizer.customizeProperties(childArbitraryContext));

		return childArbitraryContext;
	}

	public static class PropertyPath implements Comparable<PropertyPath> {
		private final Property property;

		@Nullable
		private final PropertyPath parentPropertyPath;
		private final int depth;

		public PropertyPath(Property property, @Nullable PropertyPath parentPropertyPath, int depth) {
			this.property = property;
			this.parentPropertyPath = parentPropertyPath;
			this.depth = depth;
		}

		public Property getProperty() {
			return property;
		}

		public int getDepth() {
			return depth;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			PropertyPath that = (PropertyPath)obj;
			return depth == that.depth
				&& property.equals(that.property)
				&& Objects.equals(parentPropertyPath, that.parentPropertyPath);
		}

		@Override
		public int hashCode() {
			return Objects.hash(property, parentPropertyPath, depth);
		}

		@Override
		public int compareTo(PropertyPath obj) {
			return Integer.compare(obj.depth, this.depth);
		}
	}
}
