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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class MapEntryElementProperty implements Property {
	private static final JvmType MAP_ENTRY_ELEMENT_JVM_TYPE = new ReflectiveJvmType(MapEntryElementType.class);

	private final Property mapEntryProperty;

	private final Property keyProperty;

	private final Property valueProperty;

	public MapEntryElementProperty(
		Property mapEntryProperty,
		Property keyProperty,
		Property valueProperty
	) {
		this.mapEntryProperty = mapEntryProperty;
		this.keyProperty = keyProperty;
		this.valueProperty = valueProperty;
	}

	public Property getMapEntryProperty() {
		return this.mapEntryProperty;
	}

	public Property getKeyProperty() {
		return this.keyProperty;
	}

	public Property getValueProperty() {
		return this.valueProperty;
	}

	@Override
	public JvmType getJvmType() {
		return MAP_ENTRY_ELEMENT_JVM_TYPE;
	}

	@Override
	@Nullable
	public String getName() {
		return null;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		MapEntryElementProperty that = (MapEntryElementProperty)obj;
		return mapEntryProperty.equals(that.mapEntryProperty)
			&& keyProperty.equals(that.keyProperty)
			&& valueProperty.equals(that.valueProperty);
	}

	@Override
	public int hashCode() {
		return Objects.hash(mapEntryProperty, keyProperty, valueProperty);
	}

	// This class only for type marking
	public static final class MapEntryElementType {
		private Object key;

		@Nullable
		private Object value;

		public Object getKey() {
			return this.key;
		}

		public void setKey(Object key) {
			this.key = key;
		}

		@Nullable
		public Object getValue() {
			return this.value;
		}

		public void setValue(@Nullable Object value) {
			this.value = value;
		}
	}
}
