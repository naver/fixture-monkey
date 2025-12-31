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

import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * It is for internal use only. It can be changed or removed at any time.
 */
@API(since = "1.1.0", status = Status.INTERNAL)
public final class DefaultTypeDefinition implements TypeDefinition {
	private final Property containerProperty;
	private final PropertyGenerator propertyGenerator;

	public DefaultTypeDefinition(
		Property containerProperty,
		LazyPropertyGenerator propertyGenerator
	) {
		this.containerProperty = containerProperty;
		this.propertyGenerator = propertyGenerator;
	}

	@Override
	public Property getResolvedProperty() {
		return this.containerProperty;
	}

	@Override
	public PropertyGenerator getPropertyGenerator() {
		return this.propertyGenerator;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		DefaultTypeDefinition that = (DefaultTypeDefinition)obj;
		return Objects.equals(containerProperty, that.containerProperty)
			&& Objects.equals(propertyGenerator, that.propertyGenerator);
	}

	@Override
	public int hashCode() {
		return Objects.hash(containerProperty, propertyGenerator);
	}
}
