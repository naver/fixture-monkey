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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryGeneratorContext {
	private final ArbitraryProperty property;

	private final List<ArbitraryProperty> children;

	@Nullable
	private final ArbitraryGeneratorContext ownerContext;

	private final Function<ArbitraryGeneratorContext, Arbitrary<Object>> resolveArbitrary;

	public ArbitraryGeneratorContext(
		ArbitraryProperty property,
		List<ArbitraryProperty> children,
		@Nullable ArbitraryGeneratorContext ownerContext,
		Function<ArbitraryGeneratorContext, Arbitrary<Object>> resolveArbitrary
	) {
		this.property = property;
		this.children = new ArrayList<>(children);
		this.ownerContext = ownerContext;
		this.resolveArbitrary = resolveArbitrary;
	}

	public ArbitraryProperty getArbitraryProperty() {
		return this.property;
	}

	public Property getProperty() {
		return this.getArbitraryProperty().getProperty();
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

	@Nullable
	public ArbitraryGeneratorContext getOwnerContext() {
		return this.ownerContext;
	}

	public Function<ArbitraryGeneratorContext, Arbitrary<Object>> getResolveArbitrary() {
		return this.resolveArbitrary;
	}

	public boolean isRootContext() {
		return this.property.isRoot();
	}
}
