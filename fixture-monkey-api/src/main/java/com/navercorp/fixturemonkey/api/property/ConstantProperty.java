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
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

@API(since = "1.0.17", status = Status.EXPERIMENTAL)
public final class ConstantProperty implements Property {
	private final JvmType jvmType;
	@Nullable
	private final String propertyName;
	@Nullable
	private final Object constantValue;

	public ConstantProperty(
		JvmType jvmType,
		@Nullable String propertyName,
		@Nullable Object constantValue
	) {
		this.jvmType = jvmType;
		this.propertyName = propertyName;
		this.constantValue = constantValue;
	}

	@Override
	public JvmType getJvmType() {
		return this.jvmType;
	}

	@Nullable
	@Override
	public String getName() {
		return propertyName;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return this.jvmType.getAnnotations();
	}

	@Nullable
	public Object getConstantValue() {
		return constantValue;
	}
}
