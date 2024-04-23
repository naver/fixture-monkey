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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "1.0.17", status = Status.MAINTAINED)
public final class ConstantProperty implements Property {
	private final AnnotatedType annotatedType;
	@Nullable
	private final String propertyName;
	@Nullable
	private final Object constantValue;
	private final List<Annotation> annotations;

	public ConstantProperty(
		AnnotatedType annotatedType,
		@Nullable String propertyName,
		@Nullable Object constantValue,
		List<Annotation> annotations
	) {
		this.annotatedType = annotatedType;
		this.propertyName = propertyName;
		this.constantValue = constantValue;
		this.annotations = annotations;
	}

	@Override
	public Type getType() {
		return this.annotatedType.getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.annotatedType;
	}

	@Nullable
	@Override
	public String getName() {
		return propertyName;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return this.annotations;
	}

	@Nullable
	@Override
	public Object getValue(Object instance) {
		return constantValue;
	}
}
