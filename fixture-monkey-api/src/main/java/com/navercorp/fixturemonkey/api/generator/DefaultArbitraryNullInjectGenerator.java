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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class DefaultArbitraryNullInjectGenerator implements ArbitraryNullInjectGenerator {
	public static final List<String> DEFAULT_NULLABLE_ANNOTATION_TYPES = Arrays.asList(
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
	);
	public static final List<String> DEFAULT_NOTNULL_ANNOTATION_TYPES = Arrays.asList(
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
	);

	private final double defaultNullInject;
	private final boolean nullableContainer;
	private final boolean defaultNotNull;
	private final Set<String> nullableAnnotationTypes;
	private final Set<String> notNullAnnotationTypes;

	public DefaultArbitraryNullInjectGenerator() {
		this(
			0.2d,
			false,
			false,
			new HashSet<>(DEFAULT_NULLABLE_ANNOTATION_TYPES),
			new HashSet<>(DEFAULT_NOTNULL_ANNOTATION_TYPES)
		);
	}

	public DefaultArbitraryNullInjectGenerator(
		double defaultNullInject,
		boolean nullableContainer,
		boolean defaultNotNull,
		Set<String> nullableAnnotationTypes,
		Set<String> notNullAnnotationTypes
	) {
		this.defaultNullInject = defaultNullInject;
		this.nullableContainer = nullableContainer;
		this.defaultNotNull = defaultNotNull;
		this.nullableAnnotationTypes = nullableAnnotationTypes;
		this.notNullAnnotationTypes = notNullAnnotationTypes;
	}

	public static DefaultArbitraryNullInjectGenerator of(
		double defaultNullInject,
		boolean nullableContainer,
		boolean defaultNotNull,
		Set<Class<? extends Annotation>> nullableAnnotationTypes,
		Set<Class<? extends Annotation>> notNullAnnotationTypes
	) {
		return new DefaultArbitraryNullInjectGenerator(
			defaultNullInject,
			nullableContainer,
			defaultNotNull,
			nullableAnnotationTypes.stream().map(Class::getName).collect(toSet()),
			notNullAnnotationTypes.stream().map(Class::getName).collect(toSet())
		);
	}

	@Override
	public double generate(
		ArbitraryPropertyGeneratorContext context,
		@Nullable ArbitraryContainerInfo containerInfo
	) {
		if (Types.getActualType(context.getProperty().getType()).isPrimitive()) {
			return 0.0d;
		}

		boolean nullable = !this.defaultNotNull;
		if (containerInfo != null) {
			nullable = this.nullableContainer;
		}

		Set<String> annotations = context.getProperty().getAnnotations().stream()
			.map(it -> it.annotationType().getName())
			.collect(toSet());

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

		return nullable ? this.defaultNullInject : 0.0d;
	}
}
