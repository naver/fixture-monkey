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
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Specifies actual property comprise an instance.
 * It should have a unique name.
 * <p>
 * Implementation should override {@code equals(Object)} and {@code hashCode())} as well.
 */
@API(since = "0.4.0", status = Status.MAINTAINED)
public interface Property {
	/**
	 * Returns the type of property.
	 *
	 * @return the type of property
	 */
	Type getType();

	/**
	 * Returns the annotatedType of property.
	 *
	 * @return the annotatedType of property
	 */
	AnnotatedType getAnnotatedType();

	/**
	 * Returns the property name.
	 * It should be unique in properties comprise an instance.
	 * It might be used for referencing {@code ObjectNode}.
	 *
	 * @return name of the property
	 */
	@Nullable
	String getName();

	/**
	 * Returns the annotations annotated in the property.
	 *
	 * @return annotations of the property
	 */
	List<Annotation> getAnnotations();

	/**
	 * Returns the annotation of given type.
	 *
	 * @param annotationClass the type of annotation to get
	 * @param <T>             annotation type
	 * @return the annotation of given type or null
	 */
	default <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationClass) {
		return this.getAnnotations().stream()
			.filter(it -> it.annotationType() == annotationClass)
			.map(annotationClass::cast)
			.findFirst();
	}

	/**
	 * Returns a value of the property in {@code instance}.
	 *
	 * @param instance an instance which has the property
	 * @return a value of the property in {@code instance}
	 */
	@Nullable
	Object getValue(Object instance);

	/**
	 * Returns whether this property is nullable or not.
	 *
	 * @return {@code null} represents it could not determine whether it is nullable.
	 * {@code true} represents it is nullable.
	 * {@code false} represents it is not nullable.
	 */
	@Nullable
	@SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
	default Boolean isNullable() {
		return null;
	}
}
