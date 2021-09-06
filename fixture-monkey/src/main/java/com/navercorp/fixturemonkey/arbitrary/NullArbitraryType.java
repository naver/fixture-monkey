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

package com.navercorp.fixturemonkey.arbitrary;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class NullArbitraryType extends ArbitraryType {
	public static final NullArbitraryType INSTANCE = new NullArbitraryType();

	private NullArbitraryType() {
		super(null);
	}

	@Override
	public List<Annotation> getAnnotations() {
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public Annotation getAnnotation(Class annotationType) {
		return null;
	}

	@Override
	public ArbitraryType getGenericArbitraryType(int index) {
		throw new IllegalArgumentException("Null ArbitraryType can not have generics");
	}

	@Override
	public ArbitraryType getArrayArbitraryType() {
		throw new IllegalArgumentException("Null ArbitraryType can not be array");
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public boolean isMapEntry() {
		return false;
	}

	@Override
	public boolean isMap() {
		return false;
	}

	@Override
	public boolean isGenericType() {
		return false;
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isStream() {
		return false;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isEnum() {
		return false;
	}

	@Override
	public boolean isInterface() {
		return false;
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public Class<?> getType() {
		return null;
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return null;
	}
}
