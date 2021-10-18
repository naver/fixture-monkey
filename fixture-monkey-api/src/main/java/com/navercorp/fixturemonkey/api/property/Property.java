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

package com.navercorp.fixturemonkey.api.property;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public interface Property {
	Class<?> getType();

	AnnotatedType getAnnotatedType();

	String getName();

	List<Annotation> getAnnotations();

	default <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationClass) {
		return this.getAnnotations().stream()
			.filter(it -> it.annotationType() == annotationClass)
			.map(annotationClass::cast)
			.findFirst();
	}

	@Nullable
	Object getValue(Object obj);
}
