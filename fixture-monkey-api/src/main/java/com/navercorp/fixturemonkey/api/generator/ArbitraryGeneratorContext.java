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

import static java.util.stream.Collectors.toMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyGeneratorContext;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyPath;
import com.navercorp.fixturemonkey.api.property.Traceable;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryGeneratorContext implements Traceable {
	private final Property resolvedProperty;
	private final ArbitraryProperty property;
	private final List<ArbitraryProperty> children;
	@Nullable
	private final ArbitraryGeneratorContext ownerContext;
	private final BiFunction<ArbitraryGeneratorContext, ArbitraryProperty, CombinableArbitrary<?>> resolveArbitrary;
	private final MonkeyGeneratorContext monkeyGeneratorContext;
	private final LazyArbitrary<PropertyPath> lazyPropertyPath;
	private final LazyArbitrary<Map<ArbitraryProperty, CombinableArbitrary<?>>> arbitraryListByArbitraryProperty =
		LazyArbitrary.lazy(this::initArbitraryListByArbitraryProperty);
	private final int generateUniqueMaxTries;
	private final double nullInject;
	private final AtomicReference<CombinableArbitrary<?>> generated =
		new AtomicReference<>(CombinableArbitrary.NOT_GENERATED);
	private final ArbitraryGeneratorLoggingContext loggingContext;

	public ArbitraryGeneratorContext(
		Property resolvedProperty,
		ArbitraryProperty property,
		List<ArbitraryProperty> children,
		@Nullable ArbitraryGeneratorContext ownerContext,
		BiFunction<ArbitraryGeneratorContext, ArbitraryProperty, CombinableArbitrary<?>> resolveArbitrary,
		LazyArbitrary<PropertyPath> lazyPropertyPath,
		MonkeyGeneratorContext monkeyGeneratorContext,
		int generateUniqueMaxTries,
		double nullInject,
		ArbitraryGeneratorLoggingContext loggingContext
	) {
		this.resolvedProperty = resolvedProperty;
		this.property = property;
		this.children = new ArrayList<>(children);
		this.ownerContext = ownerContext;
		this.resolveArbitrary = resolveArbitrary;
		this.lazyPropertyPath = lazyPropertyPath;
		this.monkeyGeneratorContext = monkeyGeneratorContext;
		this.generateUniqueMaxTries = generateUniqueMaxTries;
		this.nullInject = nullInject;
		this.loggingContext = loggingContext;
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

	public double getNullInject() {
		return nullInject;
	}

	public Map<ArbitraryProperty, CombinableArbitrary<?>> getCombinableArbitrariesByArbitraryProperty() {
		return arbitraryListByArbitraryProperty.getValue().entrySet().stream()
			.collect(toMap(Entry::getKey, Entry::getValue));
	}

	public Map<String, CombinableArbitrary<?>> getCombinableArbitrariesByResolvedName() {
		return arbitraryListByArbitraryProperty.getValue().entrySet().stream()
			.collect(toMap(it -> it.getKey().getObjectProperty().getResolvedPropertyName(), Entry::getValue));
	}

	public Map<String, CombinableArbitrary<?>> getCombinableArbitrariesByPropertyName() {
		return arbitraryListByArbitraryProperty.getValue().entrySet().stream()
			.collect(toMap(it -> it.getKey().getObjectProperty().getProperty().getName(), Entry::getValue));
	}

	public List<CombinableArbitrary<?>> getElementCombinableArbitraryList() {
		return new ArrayList<>(arbitraryListByArbitraryProperty.getValue().values());
	}

	@Nullable
	public ArbitraryGeneratorContext getOwnerContext() {
		return this.ownerContext;
	}

	public boolean isRootContext() {
		return this.property.getObjectProperty().isRoot();
	}

	public synchronized boolean isUniqueAndCheck(PropertyPath property, Object value) {
		return monkeyGeneratorContext.isUniqueAndCheck(property, value);
	}

	public void evictUnique(PropertyPath propertyPath) {
		monkeyGeneratorContext.evictUnique(propertyPath);
	}

	public PropertyPath getPropertyPath() {
		return lazyPropertyPath.getValue();
	}

	public int getGenerateUniqueMaxTries() {
		return generateUniqueMaxTries;
	}

	public ArbitraryGeneratorLoggingContext getLoggingContext() {
		return loggingContext;
	}

	public CombinableArbitrary<?> getGenerated() {
		return generated.get();
	}

	public void setGenerated(CombinableArbitrary<?> generated) {
		this.generated.set(generated);
	}

	private Map<ArbitraryProperty, CombinableArbitrary<?>> initArbitraryListByArbitraryProperty() {
		Map<ArbitraryProperty, CombinableArbitrary<?>> childrenValues = new LinkedHashMap<>();
		for (ArbitraryProperty child : this.getChildren()) {
			CombinableArbitrary<?> arbitrary = this.resolveArbitrary.apply(this, child);
			childrenValues.put(child, arbitrary);
		}

		return childrenValues;
	}
}
