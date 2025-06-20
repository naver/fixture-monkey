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

package com.navercorp.fixturemonkey.tree;

import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.2.0", status = Status.EXPERIMENTAL)
public final class NodeTypePredicate implements NextNodePredicate {
	private final Class<?> type;
	private final boolean exact;

	public NodeTypePredicate(Class<?> type) {
		this(type, true);
	}

	public NodeTypePredicate(Class<?> type, boolean exact) {
		this.type = Objects.requireNonNull(type, "type must not be null");
		this.exact = exact;
	}

	public Class<?> getType() {
		return type;
	}

	public boolean isExact() {
		return exact;
	}

	@Override
	public boolean test(ObjectProperty currentObjectProperty) {
		Class<?> actualType = Types.getActualType(currentObjectProperty.getProperty().getType());
		if (exact) {
			return type == actualType;
		}
		return type.isAssignableFrom(actualType);
	}
}
