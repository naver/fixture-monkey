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

import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class DefaultNullInjectGenerator implements NullInjectGenerator {
	public static final double NOT_NULL_INJECT = 0.0d;
	public static final double DEFAULT_NULL_INJECT = 0.2d;
	public static final double ALWAYS_NULL_INJECT = 1.0d;
	public static final List<String> DEFAULT_NULLABLE_ANNOTATION_TYPES = Collections.unmodifiableList(
		Arrays.asList(
			"javax.annotation.Nullable",
			"org.springframework.lang.Nullable",
			"edu.umd.cs.findbugs.annotations.Nullable",
			"org.jetbrains.annotations.Nullable",
			"com.sun.istack.internal.Nullable",
			"androidx.annotation.Nullable",
			"android.support.annotation.Nullable",
			"org.checkerframework.checker.nullness.qual.Nullable",
			"org.eclipse.jdt.annotation.Nullable",
			"org.eclipse.jgit.annotations.Nullable",
			"org.jmlspecs.annotation.Nullable"
		)
	);

	public static final List<String> DEFAULT_NULL_ANNOTATION_TYPES = Collections.singletonList(
		"javax.validation.constraints.Null");
	public static final List<String> DEFAULT_NOTNULL_ANNOTATION_TYPES = Collections.unmodifiableList(
		Arrays.asList(
			"javax.annotation.Nonnull",
			"org.springframework.lang.NonNull",
			"edu.umd.cs.findbugs.annotations.NonNull",
			"org.jetbrains.annotations.NotNull",
			"com.sun.istack.internal.NotNull",
			"androidx.annotation.NonNull",
			"android.support.annotation.NonNull",
			"javax.validation.constraints.NotNull",
			"lombok.NonNull",
			"org.checkerframework.checker.nullness.qual.NonNull",
			"org.eclipse.jdt.annotation.NonNull",
			"org.eclipse.jgit.annotations.NonNull",
			"org.jmlspecs.annotation.NonNull",
			"org.netbeans.api.annotations.common.NonNull"
		)
	);

	private final double defaultNullInject;
	private final boolean nullableContainer;
	private final boolean defaultNotNull;

	private final boolean nullableElement;
	private final Set<String> nullAnnotationTypes;
	private final Set<String> nullableAnnotationTypes;
	private final Set<String> notNullAnnotationTypes;

	public DefaultNullInjectGenerator() {
		this(
			DEFAULT_NULL_INJECT,
			false,
			false,
			false,
			new HashSet<>(DEFAULT_NULL_ANNOTATION_TYPES),
			new HashSet<>(DEFAULT_NULLABLE_ANNOTATION_TYPES),
			new HashSet<>(DEFAULT_NOTNULL_ANNOTATION_TYPES)
		);
	}

	public DefaultNullInjectGenerator(
		double defaultNullInject,
		boolean nullableContainer,
		boolean defaultNotNull,
		boolean nullableElement,
		Set<String> nullAnnotationTypes,
		Set<String> nullableAnnotationTypes,
		Set<String> notNullAnnotationTypes
	) {
		this.defaultNullInject = defaultNullInject;
		this.nullableContainer = nullableContainer;
		this.defaultNotNull = defaultNotNull;
		this.nullableElement = nullableElement;
		this.nullAnnotationTypes = nullAnnotationTypes;
		this.nullableAnnotationTypes = nullableAnnotationTypes;
		this.notNullAnnotationTypes = notNullAnnotationTypes;
	}

	public static DefaultNullInjectGenerator of(
		double defaultNullInject,
		boolean nullableContainer,
		boolean defaultNotNull,
		boolean nullableElement,
		Set<Class<? extends Annotation>> nullAnnotationTypes,
		Set<Class<? extends Annotation>> nullableAnnotationTypes,
		Set<Class<? extends Annotation>> notNullAnnotationTypes
	) {
		return new DefaultNullInjectGenerator(
			defaultNullInject,
			nullableContainer,
			defaultNotNull,
			nullableElement,
			nullAnnotationTypes.stream().map(Class::getName).collect(toSet()),
			nullableAnnotationTypes.stream().map(Class::getName).collect(toSet()),
			notNullAnnotationTypes.stream().map(Class::getName).collect(toSet())
		);
	}

	@Override
	public double generate(ObjectPropertyGeneratorContext context) {
		if (context.isRootContext()) {
			return NOT_NULL_INJECT;
		}

		if (Types.getActualType(context.getProperty().getType()).isPrimitive()) {
			return NOT_NULL_INJECT;
		}

		Boolean nullable = context.getProperty().isNullable();
		if (nullable == null) {
			nullable = !this.defaultNotNull;
		}

		Set<String> annotations = context.getProperty().getAnnotations().stream()
			.map(it -> it.annotationType().getName())
			.collect(toSet());

		boolean isNull = annotations.stream().anyMatch(this.nullAnnotationTypes::contains);

		if (isNull) {
			return ALWAYS_NULL_INJECT;
		}

		if (!nullable) {
			boolean hasNullableAnnotation = annotations.stream()
				.anyMatch(this.nullableAnnotationTypes::contains);
			if (hasNullableAnnotation) {
				nullable = true;
			}
		}

		if (nullable) {
			boolean hasNotNullAnnotation = annotations.stream()
				.anyMatch(this.notNullAnnotationTypes::contains);
			if (hasNotNullAnnotation) {
				nullable = false;
			}
		}

		if (context.isContainer() && context.getProperty().isNullable() == null) {
			nullable = this.nullableContainer;
		}

		if (context.getOwnerProperty() != null && context.getOwnerProperty().getContainerProperty() != null) {
			nullable = this.nullableElement;
		}

		return nullable ? this.defaultNullInject : NOT_NULL_INJECT;
	}
}
