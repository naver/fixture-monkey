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
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class MapKeyElementProperty implements Property {
	private final Property mapProperty;

	private final Property keyProperty;

	private final int sequence;

	public MapKeyElementProperty(Property mapProperty, Property keyProperty, int sequence) {
		this.mapProperty = mapProperty;
		this.keyProperty = keyProperty;
		this.sequence = sequence;
	}

	@Override
	public JvmType getJvmType() {
		return this.keyProperty.getJvmType();
	}

	public Property getMapProperty() {
		return mapProperty;
	}

	public Property getKeyProperty() {
		return this.keyProperty;
	}

	public int getSequence() {
		return sequence;
	}

	@Override
	@Nullable
	public String getName() {
		return null;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return this.keyProperty.getAnnotations();
	}

	@Override
	public Boolean isNullable() {
		return false;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		MapKeyElementProperty that = (MapKeyElementProperty)obj;
		return mapProperty.equals(that.mapProperty)
			&& keyProperty.equals(that.keyProperty);
	}

	@Override
	public int hashCode() {
		return Objects.hash(mapProperty, keyProperty);
	}
}
