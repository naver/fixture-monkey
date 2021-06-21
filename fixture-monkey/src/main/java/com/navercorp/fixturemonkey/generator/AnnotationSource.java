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

package com.navercorp.fixturemonkey.generator;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.Optional;

import javax.annotation.Nullable;

public final class AnnotationSource {
	@Nullable
	private final AnnotatedType annotatedType;

	public AnnotationSource(@Nullable AnnotatedType annotatedType) {
		this.annotatedType = annotatedType;
	}

	public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass) {
		if (annotatedType == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(annotatedType.getAnnotation(annotationClass));
	}
}
